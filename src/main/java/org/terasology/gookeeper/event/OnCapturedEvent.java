// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gookeeper.event;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;
import org.terasology.gookeeper.component.SlimePodComponent;

public class OnCapturedEvent implements Event {

    private final EntityRef owner;
    private final SlimePodComponent slimePodComponent;

    public OnCapturedEvent(EntityRef owner, SlimePodComponent slimePodComponent) {
        this.owner = owner;
        this.slimePodComponent = slimePodComponent;
    }

    public EntityRef getOwner() {
        return owner;
    }

    public SlimePodComponent getSlimePodComponent() {
        return slimePodComponent;
    }
}
