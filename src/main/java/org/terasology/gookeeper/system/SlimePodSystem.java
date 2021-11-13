// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gookeeper.system;

import org.joml.Vector3f;
import org.joml.Vector3i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.behaviors.components.FollowComponent;
import org.terasology.engine.core.Time;
import org.terasology.engine.entitySystem.entity.EntityBuilder;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.engine.logic.characters.GazeMountPointComponent;
import org.terasology.engine.logic.characters.events.OnEnterBlockEvent;
import org.terasology.engine.logic.common.ActivateEvent;
import org.terasology.engine.logic.delay.DelayManager;
import org.terasology.module.health.components.HealthComponent;
import org.terasology.module.inventory.systems.InventoryManager;
import org.terasology.engine.logic.inventory.PickupComponent;
import org.terasology.engine.logic.inventory.events.DropItemEvent;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.physics.HitResult;
import org.terasology.engine.physics.Physics;
import org.terasology.engine.physics.StandardCollisionGroup;
import org.terasology.engine.physics.components.shapes.BoxShapeComponent;
import org.terasology.engine.physics.events.CollideEvent;
import org.terasology.engine.physics.events.ImpulseEvent;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.logic.MeshComponent;
import org.terasology.engine.rendering.logic.SkeletalMeshComponent;
import org.terasology.engine.utilities.random.FastRandom;
import org.terasology.engine.utilities.random.Random;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.BlockComponent;
import org.terasology.gookeeper.Constants;
import org.terasology.gookeeper.component.GooeyComponent;
import org.terasology.gookeeper.component.HungerComponent;
import org.terasology.gookeeper.component.SlimePodComponent;
import org.terasology.gookeeper.component.SlimePodItemComponent;
import org.terasology.gookeeper.event.OnCapturedEvent;
import org.terasology.math.TeraMath;

import static org.joml.RoundingMode.HALF_UP;

@RegisterSystem(RegisterMode.AUTHORITY)
public class SlimePodSystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    private static final Logger logger = LoggerFactory.getLogger(SlimePodSystem.class);

    @In
    private WorldProvider worldProvider;

    @In
    private EntityManager entityManager;

    @In
    private BlockEntityRegistry blockEntityRegistry;

    @In
    private LocalPlayer localPlayer;

    @In
    private InventoryManager inventoryManager;

    @In
    private Physics physics;

    @In
    private AssetManager assetManager;

    @In
    private DelayManager delayManager;

    @In
    private Time time;

    private Random random = new FastRandom();

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void update(float delta) {
        for (EntityRef entity : entityManager.getEntitiesWith(SlimePodComponent.class)) {
            SlimePodComponent slimePodComponent = entity.getComponent(SlimePodComponent.class);
            LocationComponent locationComponent = entity.getComponent(LocationComponent.class);
            if (locationComponent != null) {
                for (EntityRef gooeyEntity : entityManager.getEntitiesWith(GooeyComponent.class)) {
                    GooeyComponent gooeyComponent = gooeyEntity.getComponent(GooeyComponent.class);

                    if (gooeyComponent == null || gooeyComponent.isCaptured || !gooeyComponent.isStunned) {
                        continue;
                    }
                    boolean capture = tryToCapture(entity, gooeyEntity) > random.nextInt(100);
                    if (capture) {
                        captureGooey(slimePodComponent, gooeyEntity);
                    }
                }
            }
        }
    }

    /**
     * Receives ActivateEvent when the held slime pod launcher item is activated, shooting out a slime pod.
     *
     * @param event  The ActivateEvent
     * @param entity The instigator entity
     */
    @ReceiveEvent(components = {SlimePodComponent.class})
    public void onActivate(ActivateEvent event, EntityRef entity) {
        EntityRef blockEntity =
                blockEntityRegistry.getExistingBlockEntityAt(new org.joml.Vector3i(event.getTargetLocation(),
                HALF_UP));

        BlockComponent blockComponent = blockEntity.getComponent(BlockComponent.class);
        SlimePodComponent slimePodComponent = entity.getComponent(SlimePodComponent.class);
        Vector3f blockPos;
        if (blockEntity.hasComponent(BlockComponent.class)) {
            blockPos = new Vector3f(blockComponent.getPosition(new Vector3i())).add(0, 1f, 0);
        } else {
            return;
        }
        slimePodComponent.isActivated = !slimePodComponent.isActivated;

        if (slimePodComponent.capturedEntity != EntityRef.NULL) {
            EntityRef releasedGooey = slimePodComponent.capturedEntity;

            FollowComponent followComponent = releasedGooey.getComponent(FollowComponent.class);
            followComponent.entityToFollow = EntityRef.NULL;
            releasedGooey.saveComponent(followComponent);

            LocationComponent locationComponent = releasedGooey.getComponent(LocationComponent.class);
            locationComponent.setWorldPosition(blockPos);
            releasedGooey.saveComponent(locationComponent);

            for (int i = 0; i < slimePodComponent.disabledComponents.size(); i++) {
                releasedGooey.addOrSaveComponent(slimePodComponent.disabledComponents.get(i));
            }

            releasedGooey.getComponent(SkeletalMeshComponent.class).mesh = slimePodComponent.capturedGooeyMesh;

            HungerComponent hungerComponent = releasedGooey.getComponent(HungerComponent.class);

            HealthComponent healthComponent = releasedGooey.getComponent(HealthComponent.class);
            GooeyComponent gooeyComponent = releasedGooey.getComponent(GooeyComponent.class);
            healthComponent.currentHealth = healthComponent.maxHealth;
            releasedGooey.saveComponent(healthComponent);

            /* This adds the health degradation to the newly captured gooey entities */
            delayManager.addPeriodicAction(releasedGooey, Constants.HEALTH_DECREASE_EVENT_ID,
                    hungerComponent.timeBeforeHungry, hungerComponent.healthDecreaseInterval);
            /* Gives a life time to the entity */
            delayManager.addDelayedAction(entity, Constants.GOOEY_DEATH_EVENT_ID, gooeyComponent.lifeTime);

            entity.destroy();
        }
    }

    /**
     * Receives ActivateEvent when the held/targeted slime pod item is activated, releasing the captured gooey.
     *
     * @param event  The ActivateEvent
     * @param entity The instigator entity
     */
    @ReceiveEvent(components = {SlimePodItemComponent.class})
    public void onSlimePodActivate(ActivateEvent event, EntityRef entity) {

        SlimePodItemComponent slimePodItemComponent = entity.getComponent(SlimePodItemComponent.class);
        if (slimePodItemComponent.slimePods > 0) {
            EntityBuilder entityBuilder = entityManager.newBuilder(slimePodItemComponent.launchPrefab);
            LocationComponent locationComponent = entityBuilder.getComponent(LocationComponent.class);

            Vector3f dir = new Vector3f(event.getDirection());
            Vector3f finalDir = new Vector3f(dir);
            finalDir.normalize();

            if (entityBuilder.hasComponent(MeshComponent.class)) {
                MeshComponent mesh = entityBuilder.getComponent(MeshComponent.class);
                BoxShapeComponent box = new BoxShapeComponent();
                Vector3f extent = mesh.mesh.getAABB().extent(new Vector3f());
                box.extents = extent.mul(2.0f);
                entityBuilder.addOrSaveComponent(box);
            }
            locationComponent.setWorldScale(0.3f);

            entityBuilder.saveComponent(locationComponent);

            GazeMountPointComponent gaze = localPlayer.getCharacterEntity().getComponent(GazeMountPointComponent.class);
            if (gaze != null) {
                locationComponent.setWorldPosition(localPlayer.getPosition(new Vector3f()).add(gaze.translate).add(finalDir.mul(2f)));
            }

            entityBuilder.setPersistent(false);
            EntityRef slimePodEntity = entityBuilder.build();

            Vector3f position = localPlayer.getViewPosition(new Vector3f());
            Vector3f direction = localPlayer.getViewDirection(new Vector3f());

            Vector3f maxAllowedDistanceInDirection = direction.mul(1.5f);
            HitResult hitResult = physics.rayTrace(position, direction, 1.5f,
                    StandardCollisionGroup.CHARACTER, StandardCollisionGroup.WORLD);
            if (hitResult.isHit()) {
                Vector3f possibleNewPosition = hitResult.getHitPoint();
                maxAllowedDistanceInDirection = possibleNewPosition.sub(position);
            }

            Vector3f newPosition = position;
            newPosition.add(maxAllowedDistanceInDirection.mul(0.9f));

            slimePodEntity.send(new DropItemEvent(newPosition));
            slimePodEntity.send(new ImpulseEvent(dir.mul(125f)));

            slimePodItemComponent.slimePods--;
        }
    }

    /**
     * This method is not appropriate for the poke-ball'ish slime pods, instead a different bear-trap type of slime pod
     * can be introduced.
     * <p>
     * Receives OnEnterBlockEvent when a gooey entity steps over a slime pod.
     *
     * @param event  The OnEnterBlockEvent
     * @param entity The gooey entity
     */
    @ReceiveEvent(components = {GooeyComponent.class})
    public void onEnterBlock(OnEnterBlockEvent event, EntityRef entity) {
        LocationComponent loc = entity.getComponent(LocationComponent.class);
        Vector3f pos = loc.getWorldPosition(new Vector3f());
        pos.set(0, pos.y() - 1, 0);

        EntityRef blockEntity = blockEntityRegistry.getExistingBlockEntityAt(new Vector3i(pos, HALF_UP));

        if (blockEntity.hasComponent(SlimePodComponent.class) && entity.hasComponent(GooeyComponent.class)) {
            SlimePodComponent slimePodComponent = blockEntity.getComponent(SlimePodComponent.class);
            if (slimePodComponent.isActivated && slimePodComponent.capturedEntity == EntityRef.NULL) {
                slimePodComponent.capturedEntity = entity;
                blockEntity.saveComponent(slimePodComponent);
                entity.send(new OnCapturedEvent(localPlayer.getCharacterEntity(), slimePodComponent));
            }
        }
    }

    /**
     * This method is computes the probability of capturing a gooey entity within a slime pod, based on its type and
     * distance from the slime pod.
     *
     * @param slimePodEntity  slime pod item entity
     * @param gooeyEntity The gooey entity
     */
    private float tryToCapture(EntityRef slimePodEntity, EntityRef gooeyEntity) {
        SlimePodComponent slimePodComponent = slimePodEntity.getComponent(SlimePodComponent.class);
        GooeyComponent gooeyComponent = gooeyEntity.getComponent(GooeyComponent.class);
        LocationComponent slimePodLocation = slimePodEntity.getComponent(LocationComponent.class);
        LocationComponent gooeyLocation = gooeyEntity.getComponent(LocationComponent.class);

        if (slimePodComponent == null || !slimePodComponent.isActivated) {
            return 0f;
        }

        Vector3f slimePodPosition = slimePodLocation.getWorldPosition(new Vector3f());
        Vector3f gooeyPosition = gooeyLocation.getWorldPosition(new Vector3f());
        float distanceFromGooey = Vector3f
                .distance(slimePodPosition.x(), slimePodPosition.y(), slimePodPosition.z(), gooeyPosition.x(), gooeyPosition.y(), gooeyPosition.z());
        if (distanceFromGooey > slimePodComponent.maxDistance) {
            return 0f;
        }

        float captureProbability =
                TeraMath.fastAbs(distanceFromGooey - slimePodComponent.maxDistance) * gooeyComponent.captureProbabiltyFactor;
        return captureProbability;
    }

    /**
     * Sends a `OnCapturedEvent` to the gooey entity, and sets the `SlimePodComponent` capturedEntity to the
     * corresponding gooey entity.
     *
     * @param slimePodComponent  slime pod component
     * @param gooeyEntity The gooey entity
     */
    private void captureGooey(SlimePodComponent slimePodComponent, EntityRef gooeyEntity) {
        GooeyComponent gooeyComponent = gooeyEntity.getComponent(GooeyComponent.class);

        if (slimePodComponent != null) {
            if (slimePodComponent.isActivated && slimePodComponent.capturedEntity == EntityRef.NULL && gooeyComponent.isStunned) {
                gooeyEntity.send(new OnCapturedEvent(localPlayer.getCharacterEntity(), slimePodComponent));
            }
        }
    }

    /**
     * Whenever a player picks up a slime pod item, then depending upon whether the pod was containing a captured gooey
     * or not, it adds it back to the launcher
     * <p>
     * Receives CollideEvent when a player steps over a slime pod
     *
     * @param event  The CollideEvent
     * @param entity The slime pod item entity
     * @param pickupComponent  pickupComponent
     */
    @ReceiveEvent
    public void onBumpGiveItemToEntity(CollideEvent event, EntityRef entity, PickupComponent pickupComponent) {
        if (entity.hasComponent(SlimePodComponent.class)) {
            if (entity.getComponent(SlimePodComponent.class).capturedEntity.equals(EntityRef.NULL)) {
                for (EntityRef slimePodLauncher : entityManager.getEntitiesWith(SlimePodItemComponent.class)) {
                    SlimePodItemComponent slimePodItemComponent =
                            slimePodLauncher.getComponent(SlimePodItemComponent.class);
                    slimePodItemComponent.slimePods++;
                    slimePodLauncher.saveComponent(slimePodItemComponent);
                    entity.destroy();
                    break;
                }
            }
        }
    }
}
