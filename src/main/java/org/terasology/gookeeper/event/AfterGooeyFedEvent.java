// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gookeeper.event;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.gestalt.entitysystem.event.Event;

public class AfterGooeyFedEvent implements Event {

    private EntityRef instigator;
    private EntityRef gooey;
    private EntityRef item;

    public AfterGooeyFedEvent(EntityRef instigator, EntityRef gooey, EntityRef item) {
        this.instigator = instigator;
        this.gooey = gooey;
        this.item = item;
    }

    public EntityRef getGooey() {
        return gooey;
    }

    public EntityRef getInstigator() {
        return instigator;
    }

    public EntityRef getItem() {
        return item;
    }
}
