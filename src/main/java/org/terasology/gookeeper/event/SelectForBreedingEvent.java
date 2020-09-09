// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gookeeper.event;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;

public class SelectForBreedingEvent implements Event {

    private final EntityRef instigator;
    private final EntityRef gooey;

    public SelectForBreedingEvent(EntityRef instigator, EntityRef gooey) {
        this.instigator = instigator;
        this.gooey = gooey;
    }

    public EntityRef getGooey() {
        return gooey;
    }

    public EntityRef getInstigator() {
        return instigator;
    }
}
