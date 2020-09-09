// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gookeeper.ui;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.CoreScreenLayer;
import org.terasology.gookeeper.event.FeedGooeyEvent;
import org.terasology.gookeeper.event.FollowGooeyEvent;
import org.terasology.nui.widgets.UIButton;

public class GooeyActivateScreen extends CoreScreenLayer {

    @In
    private LocalPlayer localPlayer;

    private UIButton feedButton;
    private UIButton followButton;
    private EntityRef gooeyEntity;
    private EntityRef ownerEntity;

    @Override
    public void initialise() {
        feedButton = find("feedButton", UIButton.class);
        feedButton.subscribe(button -> {
            gooeyEntity.send(new FeedGooeyEvent(ownerEntity, gooeyEntity));
        });

        followButton = find("followButton", UIButton.class);
        followButton.subscribe(button -> {
            gooeyEntity.send(new FollowGooeyEvent(ownerEntity, gooeyEntity));
        });
    }

    public void setGooeyEntity(EntityRef entityRef) {
        gooeyEntity = entityRef;
    }

    public void setBreederEntity(EntityRef entityRef) {
        ownerEntity = entityRef;
    }

}