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
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.gookeeper.component.GooeyComponent;
import org.terasology.gookeeper.component.MatingComponent;
import org.terasology.gookeeper.event.BreedGooeyEvent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;

@RegisterSystem(RegisterMode.AUTHORITY)
@Share(value = BreedingSystem.class)
public class BreedingSystem extends BaseComponentSystem {

    @In
    private LocalPlayer localPlayer;

    @In
    private AssetManager assetManager;

    @In
    private NUIManager nuiManager;

    private static final Logger logger = LoggerFactory.getLogger(BreedingSystem.class);
    private Random random = new FastRandom();

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

        gooeyEntity.addOrSaveComponent(matingComponent);
    }
}