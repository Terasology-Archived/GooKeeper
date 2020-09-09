// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gookeeper.event;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;

public class AfterGooeyBreedingEvent implements Event {

    private final EntityRef gooey;
    private final EntityRef matingWithEntity;
    private final EntityRef offspringGooey;

    public AfterGooeyBreedingEvent(EntityRef gooey, EntityRef matingWithEntity, EntityRef offspringGooey) {
        this.gooey = gooey;
        this.matingWithEntity = matingWithEntity;
        this.offspringGooey = offspringGooey;
    }

    public EntityRef getGooey() {
        return gooey;
    }

    public EntityRef getMatingWithEntity() {
        return matingWithEntity;
    }

    public EntityRef getOffspringGooey() {
        return offspringGooey;
    }
}
