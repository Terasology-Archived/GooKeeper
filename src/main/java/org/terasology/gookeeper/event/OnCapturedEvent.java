// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gookeeper.event;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;
import org.terasology.gookeeper.component.SlimePodComponent;

public class OnCapturedEvent implements Event {

    private EntityRef owner;
    private SlimePodComponent slimePodComponent;

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
