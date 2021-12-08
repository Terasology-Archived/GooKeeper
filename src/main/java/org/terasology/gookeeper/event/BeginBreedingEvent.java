// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gookeeper.event;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.gestalt.entitysystem.event.Event;

public class BeginBreedingEvent implements Event {

    private EntityRef instigator;
    private EntityRef gooey;
    private EntityRef matingWithEntity;

    public BeginBreedingEvent(EntityRef instigator, EntityRef gooey, EntityRef matingWithEntity) {
        this.instigator = instigator;
        this.gooey = gooey;
    }

    public EntityRef getGooey() {
        return gooey;
    }

    public EntityRef getInstigator() {
        return instigator;
    }

    public EntityRef getMatingWithEntity() {
        return matingWithEntity;
    }
}
