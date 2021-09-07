// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gookeeper.event;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.gestalt.entitysystem.event.Event;

public class LeaveVisitBlockEvent implements Event {

    private EntityRef visitor;
    private EntityRef visitBlock;

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
