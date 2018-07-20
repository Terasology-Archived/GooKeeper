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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.management.AssetManager;
import org.terasology.behaviors.components.FollowComponent;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.gookeeper.component.BreedingBlockComponent;
import org.terasology.gookeeper.component.GooeyComponent;
import org.terasology.gookeeper.component.MatingComponent;
import org.terasology.gookeeper.event.BreedGooeyEvent;
import org.terasology.gookeeper.event.OnCapturedEvent;
import org.terasology.logic.characters.events.OnEnterBlockEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;
import org.terasology.world.BlockEntityRegistry;

import java.math.RoundingMode;

@RegisterSystem(RegisterMode.AUTHORITY)
@Share(value = BreedingSystem.class)
public class BreedingSystem extends BaseComponentSystem {

    @In
    private BlockEntityRegistry blockEntityRegistry;

    @In
    private LocalPlayer localPlayer;

    @In
    private AssetManager assetManager;

    @In
    private NUIManager nuiManager;

    @In
    private EntityManager entityManager;

    private static final Logger logger = LoggerFactory.getLogger(BreedingSystem.class);
    private Random random = new FastRandom();

    /**
     * This method is called upon when a gooey that is following the player is brought to
     * the love shack and it steps over a breeding block.
     *
     * Receives OnEnterBlockEvent when a gooey entity steps over a breeding block.
     *
     * @param event     The OnEnterBlockEvent
     * @param entity    the gooey entity
     */
    @ReceiveEvent(components = {GooeyComponent.class})
    public void onEnterBlock(OnEnterBlockEvent event, EntityRef entity) {
        LocationComponent loc = entity.getComponent(LocationComponent.class);
        Vector3f pos = loc.getWorldPosition();
        pos.setY(pos.getY() -1);

        EntityRef blockEntity = blockEntityRegistry.getExistingBlockEntityAt(new Vector3i(pos, RoundingMode.HALF_UP));

        if (blockEntity.hasComponent(BreedingBlockComponent.class)) {
            FollowComponent followComponent = entity.getComponent(FollowComponent.class);
            BreedingBlockComponent breedingBlockComponent = blockEntity.getComponent(BreedingBlockComponent.class);

            if (followComponent.entityToFollow != EntityRef.NULL && breedingBlockComponent.parentGooey == EntityRef.NULL) {
                breedingBlockComponent.parentGooey = entity;
                entity.send(new BreedGooeyEvent(followComponent.entityToFollow, entity));
                blockEntity.saveComponent(breedingBlockComponent);
            }
        }
    }

    /**
     * Receives the BreedGooeyEvent when the "activated" gooey is chosen for breeding.
     *
     * @param event
     * @param gooeyEntity
     * @param gooeyComponent
     */
    @ReceiveEvent
    public void onBreedingGooey(BreedGooeyEvent event, EntityRef gooeyEntity, GooeyComponent gooeyComponent) {
        logger.info("Selected for breeding...");
        MatingComponent matingComponent = new MatingComponent();

        matingComponent.selectedForMating = !matingComponent.selectedForMating;

        for (EntityRef breedingBlock : entityManager.getEntitiesWith(BreedingBlockComponent.class)) {
            if (breedingBlock.getOwner().equals(event.getInstigator())) {
                BreedingBlockComponent breedingBlockComponent = breedingBlock.getComponent(BreedingBlockComponent.class);

                if (!breedingBlockComponent.parentGooey.equals(gooeyEntity)) {
                    matingComponent.matingWithEntity = breedingBlockComponent.parentGooey;
                    break;
                }
            }
        }

        gooeyEntity.addOrSaveComponent(matingComponent);
    }
}