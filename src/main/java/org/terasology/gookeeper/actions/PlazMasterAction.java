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
package org.terasology.gookeeper.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.gookeeper.component.GooeyComponent;
import org.terasology.gookeeper.component.PlazMasterComponent;
import org.terasology.gookeeper.event.OnStunnedEvent;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.health.DoDamageEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.physics.CollisionGroup;
import org.terasology.physics.HitResult;
import org.terasology.physics.Physics;
import org.terasology.physics.StandardCollisionGroup;
import org.terasology.registry.In;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;

@RegisterSystem(RegisterMode.AUTHORITY)
public class PlazMasterAction extends BaseComponentSystem {
    @In
    private WorldProvider worldProvider;

    @In
    private Physics physicsRenderer;

    @In
    private BlockEntityRegistry blockEntityRegistry;

    @In
    private EntityManager entityManager;

    @In
    private Time time;

    @In
    private LocalPlayer localPlayer;

    private CollisionGroup filter = StandardCollisionGroup.ALL;
    private static final Logger logger = LoggerFactory.getLogger(PlazMasterAction.class);
    private float lastTime = 0f;
    private Random random = new FastRandom();

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

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
                dir = new Vector3f(event.getDirection().x + random.nextFloat(-0.15f, -0.15f) * timeDiff,  event.getDirection().y + random.nextFloat(-0.15f, 0.15f) * timeDiff, event.getDirection().z + random.nextFloat(-0.15f, 0.15f) * timeDiff);
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
                    }
                } else {
                    logger.info("Adjust the frequency!");
                }
            } else {
                hitEntity.send(new DoDamageEvent(plazMasterComponent.damageAmount, plazMasterComponent.damageType));
            }
            plazMasterComponent.charges --;
            plazMasterComponent.charges = TeraMath.clamp(plazMasterComponent.maxCharges, 0f, plazMasterComponent.maxCharges);

            lastTime = time.getGameTime();
        }
    }
}
