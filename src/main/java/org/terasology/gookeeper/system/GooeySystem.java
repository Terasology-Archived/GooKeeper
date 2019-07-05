/*
 * Copyright 2018 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.gookeeper.system;

import com.google.common.collect.Lists;

import org.slf4j.LoggerFactory;
import org.terasology.behaviors.components.AttackOnHitComponent;
import org.terasology.behaviors.components.FindNearbyPlayersComponent;
import org.terasology.behaviors.components.FollowComponent;
import org.terasology.core.world.CoreBiome;
import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.gookeeper.component.*;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.gookeeper.event.FollowGooeyEvent;
import org.terasology.gookeeper.event.OnCapturedEvent;
import org.terasology.gookeeper.event.OnStunnedEvent;
import org.terasology.logic.behavior.BehaviorComponent;
import org.terasology.logic.characters.CharacterHeldItemComponent;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.logic.characters.StandComponent;
import org.terasology.logic.characters.WalkComponent;
import org.terasology.logic.characters.events.HorizontalCollisionEvent;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.logic.delay.DelayManager;
import org.terasology.logic.health.HealthComponent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.ChunkMath;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.logic.health.event.OnDamagedEvent;
import org.terasology.registry.In;
import org.terasology.rendering.assets.skeletalmesh.SkeletalMesh;
import org.terasology.rendering.logic.SkeletalMeshComponent;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.biomes.Biome;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.ChunkConstants;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.terasology.world.sun.CelestialSystem;

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
                float distanceFromPlayer = Vector3f.distance(locationComponent.getWorldPosition(), localPlayer.getPosition());
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
            float distanceFromPlayer = Vector3f.distance(locationComponent.getWorldPosition(), localPlayer.getPosition());
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
        Vector3f pos = localPlayer.getPosition();
        Vector3i chunkPos = ChunkMath.calcChunkPos((int) pos.x, (int) pos.y, (int) pos.z);
        for (Prefab gooey : gooeyPrefabs) {
            boolean trySpawn = (gooey.getComponent(GooeyComponent.class).SPAWN_CHANCE/10f) > random.nextInt(400);
            if (trySpawn) {
                tryGooeySpawn(gooey, chunkPos);
            }
        }
    }

    /**
     * When a new chunk gets generated, it tries to call the gooey spawning method
     *
     * @param event,worldEntity   The corresponding OnChunkGenerated event and the worldEntity ref
     */
//    @ReceiveEvent
//    public void onChunkGenerated(OnChunkGenerated event, EntityRef worldEntity) {
//        for (Optional<Prefab> gooey : gooeyPrefabs) {
//            boolean trySpawn = gooey.get().getComponent(GooeyComponent.class).SPAWN_CHANCE > random.nextInt(100);
//            if (!trySpawn) {
//                return;
//            }
//            Vector3i chunkPos = event.getChunkPos();
//            tryGooeySpawn(gooey, chunkPos);
//        }
//    }

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
        worldPos.mul(ChunkConstants.SIZE_X, ChunkConstants.SIZE_Y, ChunkConstants.SIZE_Z);
        List<Vector3i> foundPositions = Lists.newArrayList();
        Vector3i blockPos = new Vector3i();
        for (int y = ChunkConstants.SIZE_Y - 1; y >= 0; y--) {
            for (int z = 0; z < ChunkConstants.SIZE_Z; z++) {
                for (int x = 0; x < ChunkConstants.SIZE_X; x++) {
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
        Vector3f floatVectorLocation = location.toVector3f();
        Vector3f yAxis = new Vector3f(0, 1, 0);
        float randomAngle = (float) (random.nextFloat()*Math.PI*2);
        Quat4f rotation = new Quat4f(yAxis, randomAngle);

        float distanceFromPlayer = Vector3f.distance(new Vector3f((float)location.x, (float)location.y, (float)location.z), localPlayer.getPosition());
        if (distanceFromPlayer < MAX_DISTANCE_FROM_PLAYER) {
            if (gooey.exists() && gooey.getComponent(LocationComponent.class) != null) {
                EntityBuilder entityBuilder = entityManager.newBuilder(gooey);
                LocationComponent locationComponent = entityBuilder.getComponent(LocationComponent.class);
                locationComponent.setWorldPosition(floatVectorLocation);
                locationComponent.setWorldRotation(rotation);
                entityBuilder.build();
                //entityManager.create(gooey.get(), floatVectorLocation, rotation);
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
        for (int i = 0; i < gooeyComponent.biome.size(); i++) {
            if (worldProvider.getBiome(pos).equals(getBiomeFromString(gooeyComponent.biome.get(i)))) {
                return true;
            }
        }

        return false;
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
//            slimePodComponent.disabledComponents.add(entity.getComponent(FindNearbyPlayersComponent.class));

            entity.removeComponent(AggressiveComponent.class);
            entity.removeComponent(FindNearbyPlayersComponent.class);
        } else if (entity.hasComponent(NeutralComponent.class)) {
            slimePodComponent.disabledComponents.add(entity.getComponent(NeutralComponent.class));
//            slimePodComponent.disabledComponents.add(entity.getComponent(AttackOnHitComponent.class));

            entity.removeComponent(NeutralComponent.class);
            entity.removeComponent(AttackOnHitComponent.class);
        } else {
            slimePodComponent.disabledComponents.add(entity.getComponent(FriendlyComponent.class));
//            slimePodComponent.disabledComponents.add(entity.getComponent(FindNearbyPlayersComponent.class));

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

            if (Vector3f.distance(blockPos.getWorldPosition(), collisionPosition) <= 3f) {
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
