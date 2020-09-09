// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gookeeper.actions;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.behavior.BehaviorAction;
import org.terasology.engine.logic.behavior.core.Actor;
import org.terasology.engine.logic.behavior.core.BaseAction;
import org.terasology.engine.logic.behavior.core.BehaviorState;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.gookeeper.component.VisitorComponent;
import org.terasology.gookeeper.event.LeaveVisitBlockEvent;
import org.terasology.math.geom.Vector3f;
import org.terasology.minion.move.MinionMoveComponent;

@BehaviorAction(name = "visitor_exit")
public class VisitorExitAction extends BaseAction {

    @Override
    public BehaviorState modify(Actor actor, BehaviorState state) {
        MinionMoveComponent minionMoveComponent = actor.getComponent(MinionMoveComponent.class);
        VisitorComponent visitorComponent = actor.getComponent(VisitorComponent.class);

        int penListSize = visitorComponent.pensToVisit.size();
        EntityRef exitBlock = visitorComponent.pensToVisit.get(penListSize - 1);

        if (Vector3f.distance(minionMoveComponent.target,
                exitBlock.getComponent(LocationComponent.class).getWorldPosition()) <= 1f) {
            actor.getEntity().destroy();
            return BehaviorState.SUCCESS;
        } else {
            for (EntityRef pen : visitorComponent.pensToVisit) {
                if (Vector3f.distance(minionMoveComponent.target,
                        pen.getComponent(LocationComponent.class).getWorldPosition()) <= 1f) {
                    actor.getEntity().send(new LeaveVisitBlockEvent(actor.getEntity(), pen));
                    break;
                }
            }
            return BehaviorState.SUCCESS;
        }
    }
}
