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
package org.terasology.gookeeper.ui;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.gookeeper.event.BreedGooeyEvent;
import org.terasology.gookeeper.event.FeedGooeyEvent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.widgets.UIButton;

public class GooeyActivateScreen extends CoreScreenLayer {

    @In
    private LocalPlayer localPlayer;

    private UIButton feedButton;
    private UIButton breedButton;
    private EntityRef gooeyEntity;

    @Override
    public void initialise() {
        feedButton = find("feedButton", UIButton.class);
        feedButton.subscribe(button -> {
            gooeyEntity.send(new FeedGooeyEvent(localPlayer.getClientEntity(), gooeyEntity));
        });

        breedButton = find("breedButton", UIButton.class);
        breedButton.subscribe(button -> {
            gooeyEntity.send(new BreedGooeyEvent(localPlayer.getClientEntity(), gooeyEntity));
        });
    }

    public void setGooeyEntity(EntityRef entityRef) {
        gooeyEntity = entityRef;
    }
}