// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gookeeper.event;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;

public class OnStunnedEvent implements Event {

    private EntityRef instigator;

    public OnStunnedEvent(EntityRef instigator) {
        this.instigator = instigator;
    }

    public EntityRef getInstigator() {
        return instigator;
    }
}
