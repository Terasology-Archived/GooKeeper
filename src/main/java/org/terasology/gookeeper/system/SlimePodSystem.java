/*
 * Copyright 2017 MovingBlocks
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.management.AssetManager;
import org.terasology.audio.StaticSound;
import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.gookeeper.component.GooeyComponent;
import org.terasology.gookeeper.component.SlimePodComponent;
import org.terasology.gookeeper.component.SlimePodItemComponent;
import org.terasology.gookeeper.event.OnCapturedEvent;
import org.terasology.logic.behavior.BehaviorComponent;
import org.terasology.logic.behavior.asset.BehaviorTree;
import org.terasology.logic.characters.CharacterHeldItemComponent;
import org.terasology.logic.characters.GazeMountPointComponent;
import org.terasology.logic.characters.events.OnEnterBlockEvent;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.events.DropItemEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.network.Client;
import org.terasology.network.ClientComponent;
import org.terasology.physics.HitResult;
import org.terasology.physics.Physics;
import org.terasology.physics.StandardCollisionGroup;
import org.terasology.physics.components.shapes.BoxShapeComponent;
import org.terasology.physics.events.ImpulseEvent;
import org.terasology.protobuf.EntityData;
import org.terasology.registry.In;
import org.terasology.rendering.logic.MeshComponent;
import org.terasology.rendering.logic.SkeletalMeshComponent;
import org.terasology.utilities.Assets;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;

import java.math.RoundingMode;

@RegisterSystem(RegisterMode.AUTHORITY)
public class SlimePodSystem extends BaseComponentSystem implements UpdateSubscriberSystem {

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

    private static final Logger logger = LoggerFactory.getLogger(SlimePodSystem.class);
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
     * @param event,entity   The ActivateEvent, the instigator entity
     */
    @ReceiveEvent(components = {SlimePodComponent.class})
    public void onActivate(ActivateEvent event, EntityRef entity) {
        BehaviorTree capturedBT = assetManager.getAsset("GooKeeper:capturedGooey", BehaviorTree.class).get();

        SlimePodComponent slimePodComponent = entity.getComponent(SlimePodComponent.class);
        Vector3f blockPos;
        if (entity.getComponent(LocationComponent.class) != null) {
            blockPos = new Vector3f(entity.getComponent(LocationComponent.class).getWorldPosition());
        } else {
            blockPos = new Vector3f(localPlayer.getPosition().add(1f, 0f, 0f));
        }
        slimePodComponent.isActivated = !slimePodComponent.isActivated;

        if (slimePodComponent.capturedEntity != EntityRef.NULL) {
            EntityRef releasedGooey = slimePodComponent.capturedEntity;
            LocationComponent locationComponent = releasedGooey.getComponent(LocationComponent.class);
            locationComponent.setWorldPosition(blockPos);
            releasedGooey.saveComponent(locationComponent);

            for (int i = 0; i < slimePodComponent.disabledComponents.size(); i++) {
                releasedGooey.addOrSaveComponent(slimePodComponent.disabledComponents.get(i));
            }
            releasedGooey.getComponent(SkeletalMeshComponent.class).mesh = slimePodComponent.capturedGooeyMesh;

            BehaviorComponent behaviorComponent = releasedGooey.getComponent(BehaviorComponent.class);
            behaviorComponent.tree = capturedBT;
            releasedGooey.saveComponent(behaviorComponent);

            entity.destroy();
        }
    }

    /**
     * Receives ActivateEvent when the held/targeted slime pod item is activated, releasing the captured gooey.
     *
     * @param event,entity   The ActivateEvent, the instigator entity
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
                box.extents = mesh.mesh.getAABB().getExtents().scale(2.0f);
                entityBuilder.addOrSaveComponent(box);
            }
            locationComponent.setWorldScale(0.3f);

            entityBuilder.saveComponent(locationComponent);

            GazeMountPointComponent gaze = localPlayer.getCharacterEntity().getComponent(GazeMountPointComponent.class);
            if (gaze != null) {
                locationComponent.setWorldPosition(localPlayer.getPosition().add(gaze.translate).add(finalDir.scale(0.3f)));
            }

            entityBuilder.setPersistent(false);
            EntityRef slimePodEntity = entityBuilder.build();

            Vector3f position = localPlayer.getViewPosition();
            Vector3f direction = localPlayer.getViewDirection();

            Vector3f maxAllowedDistanceInDirection = direction.mul(1.5f);
            HitResult hitResult = physics.rayTrace(position, direction, 1.5f, StandardCollisionGroup.CHARACTER, StandardCollisionGroup.WORLD);
            if (hitResult.isHit()) {
                Vector3f possibleNewPosition = hitResult.getHitPoint();
                maxAllowedDistanceInDirection = possibleNewPosition.sub(position);
            }

            Vector3f newPosition = position;
            newPosition.add(maxAllowedDistanceInDirection.mul(0.9f));

            slimePodEntity.send(new DropItemEvent(newPosition));
            slimePodEntity.send(new ImpulseEvent(dir.mul(200f)));

            slimePodItemComponent.slimePods --;
        }
    }

    /**
     * This method is not appropriate for the poke-ball'ish slime pods, instead a different bear-trap
     * type of slime pod can be introduced.
     * Receives OnEnterBlockEvent when a gooey entity steps over a slime pod.
     *
     * @param event,entity   The OnEnterBlockEvent, the gooey entity
     */
    @ReceiveEvent
    public void onEnterBlock(OnEnterBlockEvent event, EntityRef entity) {
        LocationComponent loc = entity.getComponent(LocationComponent.class);
        Vector3f pos = loc.getWorldPosition();
        pos.setY(pos.getY() -1);

        EntityRef blockEntity = blockEntityRegistry.getExistingBlockEntityAt(new Vector3i(pos, RoundingMode.HALF_UP));

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
     * This method is computes the probability of capturing a gooey entity within a slime pod, based on its type
     * and distance from the slime pod.
     *
     * @param slimePodEntity,gooeyEntity   slime pod item entity, the gooey entity
     */
    private float tryToCapture (EntityRef slimePodEntity, EntityRef gooeyEntity) {
        SlimePodComponent slimePodComponent = slimePodEntity.getComponent(SlimePodComponent.class);
        GooeyComponent gooeyComponent = gooeyEntity.getComponent(GooeyComponent.class);
        LocationComponent slimePodLocation = slimePodEntity.getComponent(LocationComponent.class);
        LocationComponent gooeyLocation = gooeyEntity.getComponent(LocationComponent.class);

        if (slimePodComponent == null || !slimePodComponent.isActivated) {
            return 0f;
        }

        float distanceFromGooey = Vector3f.distance(slimePodLocation.getWorldPosition(), gooeyLocation.getWorldPosition());
        if (distanceFromGooey > slimePodComponent.maxDistance) {
            return 0f;
        }

        float captureProbability = TeraMath.fastAbs(distanceFromGooey - slimePodComponent.maxDistance) * gooeyComponent.captureProbabiltyFactor;
        return captureProbability;
    }

    /**
     * Sends a `OnCapturedEvent` to the gooey entity, and sets the `SlimePodComponent` capturedEntity to the corresponding gooey entity.
     *
     * @param slimePodComponent,gooeyEntity   slime pod component, the gooey entity
     */
    private void captureGooey (SlimePodComponent slimePodComponent, EntityRef gooeyEntity) {
        GooeyComponent gooeyComponent = gooeyEntity.getComponent(GooeyComponent.class);

        if (slimePodComponent != null) {
            if (slimePodComponent.isActivated && slimePodComponent.capturedEntity == EntityRef.NULL && gooeyComponent.isStunned) {
                gooeyEntity.send(new OnCapturedEvent(localPlayer.getCharacterEntity(), slimePodComponent));
            }
        }
    }
}
