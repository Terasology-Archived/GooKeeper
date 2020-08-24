// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gookeeper.ui;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.gookeeper.event.FeedGooeyEvent;
import org.terasology.gookeeper.event.FollowGooeyEvent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.nui.widgets.UIButton;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;

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