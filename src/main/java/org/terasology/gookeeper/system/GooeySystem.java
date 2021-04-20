// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gookeeper.system;

import com.google.common.collect.Lists;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.behaviors.components.AttackOnHitComponent;
import org.terasology.behaviors.components.FindNearbyPlayersComponent;
import org.terasology.behaviors.components.FollowComponent;
import org.terasology.biomesAPI.Biome;
import org.terasology.biomesAPI.BiomeRegistry;
import org.terasology.core.world.CoreBiome;
import org.terasology.engine.entitySystem.entity.EntityBuilder;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.prefab.PrefabManager;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.engine.logic.behavior.BehaviorComponent;
import org.terasology.engine.logic.characters.CharacterHeldItemComponent;
import org.terasology.engine.logic.characters.CharacterMovementComponent;
import org.terasology.engine.logic.characters.StandComponent;
import org.terasology.engine.logic.characters.WalkComponent;
import org.terasology.engine.logic.characters.events.HorizontalCollisionEvent;
import org.terasology.engine.logic.common.DisplayNameComponent;
import org.terasology.engine.logic.delay.DelayManager;
import org.terasology.module.health.components.HealthComponent;
import org.terasology.module.health.events.OnDamagedEvent;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.assets.skeletalmesh.SkeletalMesh;
import org.terasology.engine.rendering.logic.SkeletalMeshComponent;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.engine.utilities.random.FastRandom;
import org.terasology.engine.utilities.random.Random;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockComponent;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.chunks.Chunks;
import org.terasology.engine.world.sun.CelestialSystem;
import org.terasology.gookeeper.component.AggressiveComponent;
import org.terasology.gookeeper.component.FriendlyComponent;
import org.terasology.gookeeper.component.GooeyComponent;
import org.terasology.gookeeper.component.HungerComponent;
import org.terasology.gookeeper.component.NeutralComponent;
import org.terasology.gookeeper.component.PenBlockComponent;
import org.terasology.gookeeper.component.SlimePodComponent;
import org.terasology.gookeeper.component.VisitBlockComponent;
import org.terasology.gookeeper.event.FollowGooeyEvent;
import org.terasology.gookeeper.event.OnCapturedEvent;
import org.terasology.gookeeper.event.OnStunnedEvent;

import java.util.ArrayList;
import java.util.List;

@RegisterSystem
public class GooeySystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    @In
    private WorldProvider worldProvider;

    @In
    private BlockEntityRegistry blockEntityRegistry;

    @In
    private EntityManager entityManager;

    @In
    private BlockManager blockManager;

    @In
    private DelayManager delayManager;

    @In
    private PrefabManager prefabManager;

    @In
    private LocalPlayer localPlayer;

    @In
    private CelestialSystem celestialSystem;

    @In
    private NUIManager nuiManager;

    @In
    private BiomeRegistry biomeRegistry;

    private Random random = new FastRandom();
    private List<Prefab> gooeyPrefabs = new ArrayList();
    private SkeletalMesh gooeySkeletalMesh = null;

    private Block airBlock;

    private static final int NUM_OF_ENTITIES_ALLOWED = 10;
    private static int currentNumOfEntities = 0;
    private static final float MAX_DISTANCE_FROM_PLAYER = 60f;

    private static final Logger logger = LoggerFactory.getLogger(GooeySystem.class);

    @Override
    public void initialise() {
        airBlock = blockManager.getBlock(BlockManager.AIR_ID);

        for (Prefab prefab : prefabManager.listPrefabs(GooeyComponent.class)) {
            gooeyPrefabs.add(prefab);
        }

        celestialSystem.toggleSunHalting(0.5f);
    }

    @Override
    public void update (float delta) {
        for (EntityRef entity : entityManager.getEntitiesWith(GooeyComponent.class)) {
            if (gooeySkeletalMesh == null) {
                SkeletalMeshComponent skeleton = entity.getComponent(SkeletalMeshComponent.class);
                gooeySkeletalMesh = skeleton.mesh;
            }

            GooeyComponent gooeyComponent = entity.getComponent(GooeyComponent.class);
            LocationComponent locationComponent = entity.getComponent(LocationComponent.class);

            if (locationComponent != null) {
                Vector3f worldPosition = locationComponent.getWorldPosition(new Vector3f());
                Vector3f playerPosition = localPlayer.getPosition(new Vector3f());
                float distanceFromPlayer = worldPosition.distance(playerPosition);
                if (distanceFromPlayer > MAX_DISTANCE_FROM_PLAYER && !gooeyComponent.isCaptured) {
                    entity.destroy();
                    currentNumOfEntities--;
                }
            }

            HealthComponent healthComponent = entity.getComponent(HealthComponent.class);
            if (healthComponent != null && healthComponent.currentHealth <= 0) {
                entity.destroy();
            }
        }

        spawnNearPlayer();
    }

    /**
     * Implementation of distance-based visual culling for gooey entities
     *
     * @param entity,locationComponent   The corresponding gooey entity and its location component
     */
    private void cullDistantGooeys(EntityRef entity, LocationComponent locationComponent) {
        SkeletalMeshComponent skeleton = entity.getComponent(SkeletalMeshComponent.class);
        if (locationComponent != null) {
            Vector3f worldPosition = locationComponent.getWorldPosition(new Vector3f());
            Vector3f playerPosition = localPlayer.getPosition(new Vector3f());
            float distanceFromPlayer = Vector3f.distance(worldPosition.x(), worldPosition.y(), worldPosition.z(), playerPosition.x(), playerPosition.y(), playerPosition.z());
            if (distanceFromPlayer > 50f && skeleton.mesh != null) {
                skeleton.mesh = null;
            } else if (distanceFromPlayer <= 50f && skeleton.mesh == null) {
                skeleton.mesh = gooeySkeletalMesh;
            }
        }
    }

    /**
     * Try spawning gooeys based on the player's current world position
     */
    private void spawnNearPlayer () {
        Vector3f pos = localPlayer.getPosition(new Vector3f());
        Vector3i chunkPos = Chunks.toChunkPos((int) pos.x(), (int) pos.y(), (int) pos.z(), new Vector3i());
        for (Prefab gooey : gooeyPrefabs) {
            boolean trySpawn = (gooey.getComponent(GooeyComponent.class).SPAWN_CHANCE/10f) > random.nextInt(400);
            if (trySpawn) {
                tryGooeySpawn(gooey, chunkPos);
            }
        }
    }

    /**
     * Attempts to spawn gooey on the specified chunk. The number of gooeys spawned will depend on probability
     * configurations defined earlier
     *
     * @param gooey,chunkPos   The prefab to be spawned and the chunk which the game will try to spawn gooeys on
     */
    private void tryGooeySpawn(Prefab gooey, Vector3i chunkPos) {
        GooeyComponent gooeyComponent = gooey.getComponent(GooeyComponent.class);
        List<Vector3i> foundPositions = findGooeySpawnPositions(gooeyComponent, chunkPos);

        if (foundPositions.size() < 1) {
            return;
        }

        int maxGooeyCount = foundPositions.size();
        if (maxGooeyCount > gooeyComponent.MAX_GROUP_SIZE) {
            maxGooeyCount = gooeyComponent.MAX_GROUP_SIZE;
        }
        int gooeyCount = random.nextInt(maxGooeyCount - 1) + 1;

        for (int i = 0; i < gooeyCount; i++ ) {
            int randomIndex = random.nextInt(foundPositions.size());
            Vector3i randomSpawnPosition = foundPositions.remove(randomIndex);
            currentNumOfEntities ++;
            if (currentNumOfEntities <= NUM_OF_ENTITIES_ALLOWED) {
                spawnGooey(gooey, randomSpawnPosition);
            } else {
                currentNumOfEntities = NUM_OF_ENTITIES_ALLOWED;
            }
        }
    }

    private List<Vector3i> findGooeySpawnPositions(GooeyComponent gooeyComponent, Vector3i chunkPos) {
        Vector3i worldPos = new Vector3i(chunkPos);
        worldPos.mul(Chunks.SIZE_X, Chunks.SIZE_Y, Chunks.SIZE_Z);
        List<Vector3i> foundPositions = Lists.newArrayList();
        Vector3i blockPos = new Vector3i();
        for (int y = Chunks.SIZE_Y - 1; y >= 0; y--) {
            for (int z = 0; z < Chunks.SIZE_Z; z++) {
                for (int x = 0; x < Chunks.SIZE_X; x++) {
                    blockPos.set(x + worldPos.x, y + worldPos.y, z + worldPos.z);
                    if (isValidSpawnPosition(gooeyComponent, blockPos)) {
                        foundPositions.add(new Vector3i(blockPos));
                    }
                }
            }
        }
        return foundPositions;
    }

    /**
     * Spawns the gooey at the location specified by the parameter.
     *
     * @param gooey,location   Gooey prefab to be spawned and the location where the gooey is to be spawned
     */
    private void spawnGooey(Prefab gooey, Vector3i location) {
        Vector3f floatVectorLocation = new Vector3f(location);
        float randomAngle = (float) (random.nextFloat()*Math.PI*2);

        Vector3f playerPosition = localPlayer.getPosition(new Vector3f());
        float distanceFromPlayer = floatVectorLocation.distance(playerPosition);
        if (distanceFromPlayer < MAX_DISTANCE_FROM_PLAYER) {
            if (gooey.exists() && gooey.getComponent(LocationComponent.class) != null) {
                EntityBuilder entityBuilder = entityManager.newBuilder(gooey);
                LocationComponent locationComponent = entityBuilder.getComponent(LocationComponent.class);
                locationComponent.setWorldPosition(floatVectorLocation);
                locationComponent.setWorldRotation(new Quaternionf().setAngleAxis(randomAngle, 0, 1, 0));
                entityBuilder.build();
            }
        }
    }

    /**
     * Check blocks at and around the target position and check if it's a valid spawning spot
     *
     * @param gooeyComponent,pos   GooeyComponent of the particular gooey to be spawned & the block to be checked if it's a valid spot for spawning
     * @return A boolean with the value of true if the block is a valid spot for spawing
     */
    private boolean isValidSpawnPosition(GooeyComponent gooeyComponent, Vector3i pos) {
        Vector3i below = new Vector3i(pos.x, pos.y - 1, pos.z);
        Block blockBelow = worldProvider.getBlock(below);
        if (!blockBelow.equals(getBlockFromString(gooeyComponent.blockBelow))) {
            return false;
        }
        Block blockAtPosition = worldProvider.getBlock(pos);
        if (!blockAtPosition.isPenetrable()) {
            return false;
        }

        Vector3i above = new Vector3i(pos.x, pos.y + 1, pos.z);
        Block blockAbove = worldProvider.getBlock(above);
        if (!blockAbove.equals(airBlock)) {
            return false;
        }
        return biomeRegistry.getBiome(pos).map(biome ->
            gooeyComponent.biome.stream().anyMatch(s -> biome.equals(getBiomeFromString(s)))
        ).orElse(false);
    }

    /**
     * Returns the Biome from the string provided as an argument.
     *
     * @param biomeName   The name of the biome (String)
     * @return Corresponding biome
     */
    private Biome getBiomeFromString (String biomeName) {
        if (biomeName.equals("DESERT")) {
            return CoreBiome.DESERT;
        } else if (biomeName.equals("FOREST")) {
            return CoreBiome.FOREST;
        } else if (biomeName.equals("PLAINS")) {
            return CoreBiome.PLAINS;
        } else if (biomeName.equals("SNOW")) {
            return CoreBiome.SNOW;
        } else if (biomeName.equals("MOUNTAINS")) {
            return CoreBiome.MOUNTAINS;
        } else {
            return null;
        }
    }

    /**
     * Returns the Block from the string provided as an argument.
     *
     * @param blockName   The name of the block (String)
     * @return Corresponding block
     */
    private Block getBlockFromString (String blockName) {
        if (blockName != null) {
            return blockManager.getBlock(blockName);
        } else {
            return null;
        }
    }

    @ReceiveEvent
    public void onDamage(OnDamagedEvent event, EntityRef entity) {
        return;
    }

    /**
     * Receives OnStunnedEvent sent to a gooey entity when it gets stunned by PlazMaster.
     *
     * @param event,entity   The OnStunnedEvent and the gooey entity to which it is sent
     */
    @ReceiveEvent
    public void onStunned(OnStunnedEvent event, EntityRef entity) {
        GooeyComponent gooeyComponent = entity.getComponent(GooeyComponent.class);
        if (!gooeyComponent.isStunned) {
            gooeyComponent.isStunned = true;
            gooeyComponent.stunChargesReq = gooeyComponent.maxStunChargesReq;
        }
        return;
    }

    /**
     * Receives OnCapturedEvent sent to a gooey entity when it gets captured in a slime pod.
     *
     * @param event,entity   The OnCapturedEvent and the gooey entity to which it is sent
     */
    @ReceiveEvent
    public void onCaptured(OnCapturedEvent event, EntityRef entity) {
        SlimePodComponent slimePodComponent = event.getSlimePodComponent();
        slimePodComponent.capturedEntity = entity;

        GooeyComponent gooeyComponent = entity.getComponent(GooeyComponent.class);
        gooeyComponent.isCaptured = true;
        entity.saveComponent(gooeyComponent);

        slimePodComponent.disabledComponents.add(entity.getComponent(CharacterMovementComponent.class));
        slimePodComponent.disabledComponents.add(entity.getComponent(WalkComponent.class));
        slimePodComponent.disabledComponents.add(entity.getComponent(StandComponent.class));
        slimePodComponent.disabledComponents.add(entity.getComponent(BehaviorComponent.class));

        if (entity.hasComponent(AggressiveComponent.class)) {
            slimePodComponent.disabledComponents.add(entity.getComponent(AggressiveComponent.class));
            entity.removeComponent(AggressiveComponent.class);
            entity.removeComponent(FindNearbyPlayersComponent.class);
        } else if (entity.hasComponent(NeutralComponent.class)) {
            slimePodComponent.disabledComponents.add(entity.getComponent(NeutralComponent.class));
            entity.removeComponent(NeutralComponent.class);
            entity.removeComponent(AttackOnHitComponent.class);
        } else {
            slimePodComponent.disabledComponents.add(entity.getComponent(FriendlyComponent.class));
            entity.removeComponent(FriendlyComponent.class);
            entity.removeComponent(FindNearbyPlayersComponent.class);
        }

        // Disable the components to essentially disable the entity.
        entity.removeComponent(BehaviorComponent.class);
        entity.removeComponent(WalkComponent.class);
        entity.removeComponent(StandComponent.class);

        slimePodComponent.capturedGooeyMesh = entity.getComponent(SkeletalMeshComponent.class).mesh;
        entity.getComponent(SkeletalMeshComponent.class).mesh = null;
        entity.removeComponent(CharacterMovementComponent.class);
    }

    /**
     * Receives HorizontalCollisionEvent sent to a gooey entity when it collides with any entity. Here, it is used to detect collisions
     * with the pen blocks, and if the types match then the gooey is not allowed to jump over the block.
     *
     * Also, it adds to the total number of gooeys in a pen, which is stored in the corresponding
     * VisitBlockComponent
     *
     * @param event,entity   The HorizontalCollisionEvent and the gooey entity to which it is sent
     */
    @ReceiveEvent(components = {GooeyComponent.class})
    public void onBump(HorizontalCollisionEvent event, EntityRef entity) {
        GooeyComponent gooeyComponent = entity.getComponent(GooeyComponent.class);
        Vector3f collisionPosition = event.getLocation();

        EntityRef blockEntity = EntityRef.NULL;

        //TODO: Optimize the block entity retrieval procedure instead of having a proximity based search
        for (EntityRef entityRef : entityManager.getEntitiesWith(PenBlockComponent.class)) {
            LocationComponent blockPos = entityRef.getComponent(LocationComponent.class);

            if (blockPos == null) {
                continue;
            }

            Vector3f worldPosition = blockPos.getWorldPosition(new Vector3f());
            if (Vector3f.distance(worldPosition.x(), worldPosition.y(), worldPosition.z(), collisionPosition.x(), collisionPosition.y(), collisionPosition.z()) <= 3f) {
                blockEntity = entityRef;
            }
        }

        CharacterMovementComponent moveComp = entity.getComponent(CharacterMovementComponent.class);
        if (moveComp != null && blockEntity.hasComponent(PenBlockComponent.class) && gooeyComponent.isCaptured) {
            PenBlockComponent penBlockComponent = blockEntity.getComponent(PenBlockComponent.class);
            DisplayNameComponent displayNameComponent = entity.getComponent(DisplayNameComponent.class);
            FollowComponent followComponent = entity.getComponent(FollowComponent.class);

            if (penBlockComponent.type.equals(displayNameComponent.name) && followComponent.entityToFollow == EntityRef.NULL) {
                moveComp.jumpSpeed = 0f;

                // Pen number 0 signifies that it hasn't been set
                if (gooeyComponent.penNumber == 0) {
                    for (EntityRef visitBlock : entityManager.getEntitiesWith(VisitBlockComponent.class, BlockComponent.class)) {
                        VisitBlockComponent visitBlockComponent = visitBlock.getComponent(VisitBlockComponent.class);
                        if (visitBlockComponent.penNumber == penBlockComponent.penNumber) {
                            gooeyComponent.penNumber = visitBlockComponent.penNumber;
                            visitBlockComponent.gooeyQuantity++;
                            visitBlock.saveComponent(visitBlockComponent);
                            entity.saveComponent(gooeyComponent);
                            break;
                        }
                    }
                }
            } else {
                moveComp.jumpSpeed = 12f;
            }
            entity.saveComponent(moveComp);
        }
    }

    /**
     * Receives the FollowGooeyEvent when the "activated" gooey is to be made to follow the player character
     * when the player is holding a food item of the gooey's liking
     *
     * @param event
     * @param gooeyEntity
     * @param gooeyComponent
     * @param hungerComponent
     */
    @ReceiveEvent
    public void onGooeyFollowPlayer(FollowGooeyEvent event, EntityRef gooeyEntity, GooeyComponent gooeyComponent, HungerComponent hungerComponent) {
        FollowComponent followComponent = gooeyEntity.getComponent(FollowComponent.class);
        CharacterMovementComponent characterMovementComponent = gooeyEntity.getComponent(CharacterMovementComponent.class);

        if (followComponent.entityToFollow == event.getInstigator()) {
            followComponent.entityToFollow = EntityRef.NULL;
            characterMovementComponent.jumpSpeed = 0f;
            nuiManager.closeScreen("GooKeeper:gooeyActivateScreen");
            return;
        }

        CharacterHeldItemComponent characterHeldItemComponent = event.getInstigator().getComponent(CharacterHeldItemComponent.class);

        if (characterHeldItemComponent != null && characterHeldItemComponent.selectedItem.hasComponent(DisplayNameComponent.class)) {
            EntityRef item = characterHeldItemComponent.selectedItem;
            String itemName = item.getComponent(DisplayNameComponent.class).name;

            if (!itemName.isEmpty() && hungerComponent.food.contains(itemName)) {
                followComponent.entityToFollow = event.getInstigator();
                characterMovementComponent.jumpSpeed = 12f;

                gooeyEntity.saveComponent(characterMovementComponent);
                gooeyEntity.saveComponent(followComponent);
            }
        }
        nuiManager.closeScreen("GooKeeper:gooeyActivateScreen");
    }
}
