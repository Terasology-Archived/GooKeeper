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
import org.terasology.audio.StaticSound;
import org.terasology.audio.events.PlaySoundEvent;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.gookeeper.component.GooeyComponent;
import org.terasology.gookeeper.component.PlazMasterComponent;
import org.terasology.gookeeper.component.PlazMasterShotComponent;
import org.terasology.gookeeper.event.OnStunnedEvent;
import org.terasology.gookeeper.input.DecreaseFrequencyButton;
import org.terasology.gookeeper.input.IncreaseFrequencyButton;
import org.terasology.logic.characters.CharacterHeldItemComponent;
import org.terasology.logic.characters.GazeMountPointComponent;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.delay.DelayManager;
import org.terasology.logic.delay.DelayedActionTriggeredEvent;
import org.terasology.logic.health.DoDamageEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.network.ClientComponent;
import org.terasology.physics.CollisionGroup;
import org.terasology.physics.HitResult;
import org.terasology.physics.Physics;
import org.terasology.physics.StandardCollisionGroup;
import org.terasology.registry.In;
import org.terasology.utilities.Assets;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;

@RegisterSystem(RegisterMode.AUTHORITY)
public class PlazMasterSystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    @In
    private WorldProvider worldProvider;

    @In
    private Physics physicsRenderer;

    @In
    private BlockEntityRegistry blockEntityRegistry;

    @In
    private EntityManager entityManager;

    @In
    private DelayManager delayManager;

    @In
    private Time time;

    @In
    private LocalPlayer localPlayer;

    private CollisionGroup filter = StandardCollisionGroup.ALL;
    private static final Logger logger = LoggerFactory.getLogger(PlazMasterSystem.class);
    private float lastTime = 0f;
    private Random random = new FastRandom();
    private PlazMasterComponent _plazMasterComponent = null;
    private static final Prefab arrowPrefab = Assets.getPrefab("GooKeeper:arrow").get();
    private StaticSound gunShotAudio = Assets.getSound("GooKeeper:PlasmaShot").get();
    private StaticSound gooeyHitAudio = Assets.getSound("GooKeeper:GooeyHit").get();

    private static final String eventID = "ARROW_DESTROY_EVENT_ID";

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void update(float delta) {
        if (_plazMasterComponent == null) {
            for (EntityRef entity : entityManager.getEntitiesWith(PlazMasterComponent.class)) {
                _plazMasterComponent = entity.getComponent(PlazMasterComponent.class);
                break;
            }
        }

        for (EntityRef projectile : entityManager.getEntitiesWith(PlazMasterShotComponent.class)) {
            LocationComponent location = projectile.getComponent(LocationComponent.class);
            PlazMasterShotComponent shot = projectile.getComponent(PlazMasterShotComponent.class);

            location.setWorldPosition(location.getWorldPosition().add(location.getWorldDirection().mul(shot.velocity)));
            projectile.saveComponent(location);
        }
    }

    /**
     * Receives ActivateEvent when the held PlazMaster item is activated, shooting a plasma pulse.
     *
     * @param event,entity,plazMasterComponent   The ActivateEvent, the instigator entity and the corresponding PlazMasterComponent of the activated item
     */
    @ReceiveEvent
    public void onActivate(ActivateEvent event, EntityRef entity, PlazMasterComponent plazMasterComponent) {
        if ((time.getGameTime() > lastTime + 1.0f / plazMasterComponent.rateOfFire) && plazMasterComponent.charges > 0f) {
            Vector3f target = event.getHitNormal();
            Vector3i blockPos = new Vector3i(target);
            Vector3f dir;
            Vector3f position = new Vector3f(event.getOrigin());
            if (time.getGameTime() > lastTime + plazMasterComponent.shotRecoveryTime) {
                // No recoil here; 100% accurate shot.
                dir = new Vector3f(event.getDirection());
            } else {
                // Add noise to this dir for simulating recoil.
                float timeDiff = TeraMath.fastAbs(time.getGameTime() - (lastTime + plazMasterComponent.shotRecoveryTime));
                dir = new Vector3f(event.getDirection().x + random.nextFloat(-0.05f, 0.05f) * timeDiff,  event.getDirection().y + random.nextFloat(-0.05f, 0.05f) * timeDiff, event.getDirection().z + random.nextFloat(-0.05f, 0.05f) * timeDiff);
            }

            HitResult result;
            result = physicsRenderer.rayTrace(position, dir, plazMasterComponent.maxDistance, filter);

            Block currentBlock = worldProvider.getBlock(blockPos);

            if (currentBlock.isDestructible()) {
                EntityBuilder builder = entityManager.newBuilder("Core:defaultBlockParticles");
                builder.getComponent(LocationComponent.class).setWorldPosition(target);
                builder.build();
            }
            EntityRef hitEntity = result.getEntity();
            if (hitEntity.hasComponent(GooeyComponent.class)) {
                GooeyComponent gooeyComponent = hitEntity.getComponent(GooeyComponent.class);
                logger.info("Hit Gooey!");
                if (TeraMath.fastAbs(gooeyComponent.stunFrequency - plazMasterComponent.frequency) <= 10f && !gooeyComponent.isStunned) {
                    // Begin the gooey wrangling.
                    gooeyComponent.stunChargesReq --;
                    if (gooeyComponent.stunChargesReq == 0) {
                        hitEntity.send(new OnStunnedEvent(localPlayer.getCharacterEntity()));
                        hitEntity.send(new DoDamageEvent(plazMasterComponent.damageAmount, plazMasterComponent.damageType, localPlayer.getCharacterEntity()));
                        hitEntity.send(new PlaySoundEvent(gooeyHitAudio, 0.8f));
                    }
                } else {
                    logger.info("Adjust the frequency!");
                }
            } else {
                hitEntity.send(new DoDamageEvent(plazMasterComponent.damageAmount, plazMasterComponent.damageType));
            }
            plazMasterComponent.charges --;
            plazMasterComponent.charges = TeraMath.clamp(plazMasterComponent.charges, 0f, plazMasterComponent.maxCharges);

            lastTime = time.getGameTime();

            EntityBuilder entityBuilder = entityManager.newBuilder(arrowPrefab);
            LocationComponent locationComponent = entityBuilder.getComponent(LocationComponent.class);

            Vector3f initialDir = locationComponent.getWorldDirection();
            Vector3f finalDir = new Vector3f(dir);
            finalDir.normalize();
            locationComponent.setWorldRotation(Quat4f.shortestArcQuat(initialDir, finalDir));

            locationComponent.setWorldScale(0.3f);

            entityBuilder.saveComponent(locationComponent);

            GazeMountPointComponent gaze = localPlayer.getCharacterEntity().getComponent(GazeMountPointComponent.class);
            if (gaze != null) {
                locationComponent.setWorldPosition(localPlayer.getPosition().add(gaze.translate).add(finalDir.scale(0.3f)));
            }

            entityBuilder.setPersistent(false);
            EntityRef arrowEntity = entityBuilder.build();

            arrowEntity.send(new PlaySoundEvent(gunShotAudio, 0.4f));

            delayManager.addDelayedAction(arrowEntity, eventID, 3000);
        }
    }

    /**
     * Receives DelayedActionTriggeredEvent, which deletes the plasma stub entity
     *
     * @param event,entity   The DelayedActionTriggeredEvent event, plasma stub entity to be destroyed
     */
    @ReceiveEvent
    public void onDelayedAction(DelayedActionTriggeredEvent event, EntityRef entityRef) {
        if (event.getActionId().equals(eventID)) {
            entityRef.destroy();
        }
    }

    // TODO: Instead of the current implementation, use itemHeld... and related funcs for multiplayer support.
    /**
     * Receives IncreaseFrequencyButton, which increases the plazmasters frequency.
     *
     * @param event,entity   The IncreaseFrequencyButton event
     */
    @ReceiveEvent(components = ClientComponent.class)
    public void onIncreaseFrequency(IncreaseFrequencyButton event, EntityRef entityRef) {
        ClientComponent clientComponent = entityRef.getComponent(ClientComponent.class);
        EntityRef player = clientComponent.character;
        EntityRef heldItem = player.getComponent(CharacterHeldItemComponent.class).selectedItem;

        if (heldItem.hasComponent(PlazMasterComponent.class)) {
            heldItem.getComponent(PlazMasterComponent.class).frequency += 10f;
            logger.info("Increased PlazMaster's Frequency!");
        }
    }

    /**
     * Receives DecreaseFrequencyButton, which decreases the plazmasters frequency.
     *
     * @param event,entity   The DecreaseFrequencyButton event
     */
    @ReceiveEvent(components = ClientComponent.class)
    public void onDecreaseFrequency(DecreaseFrequencyButton event, EntityRef entityRef) {
        ClientComponent clientComponent = entityRef.getComponent(ClientComponent.class);
        EntityRef player = clientComponent.character;
        EntityRef heldItem = player.getComponent(CharacterHeldItemComponent.class).selectedItem;

        if (heldItem.hasComponent(PlazMasterComponent.class)) {
            heldItem.getComponent(PlazMasterComponent.class).frequency -= 10f;
            logger.info("Decreased PlazMaster's Frequency!");
        }
    }
}
