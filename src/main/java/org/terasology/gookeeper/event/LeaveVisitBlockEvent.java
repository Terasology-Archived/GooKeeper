// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gookeeper.event;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;

public class LeaveVisitBlockEvent implements Event {

    private final EntityRef visitor;
    private final EntityRef visitBlock;

    public LeaveVisitBlockEvent(EntityRef visitor, EntityRef visitBlock) {
        this.visitor = visitor;
        this.visitBlock = visitBlock;
    }

    public EntityRef getVisitor() {
        return visitor;
    }

    public EntityRef getVisitBlock() {
        return visitBlock;
    }
}
