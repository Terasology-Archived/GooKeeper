// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gookeeper.actions;

import org.terasology.behaviors.minion.move.MinionMoveComponent;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.behavior.BehaviorAction;
import org.terasology.engine.logic.behavior.core.Actor;
import org.terasology.engine.logic.behavior.core.BaseAction;
import org.terasology.engine.logic.behavior.core.BehaviorState;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.gookeeper.component.VisitBlockComponent;
import org.terasology.gookeeper.component.VisitorComponent;
import org.terasology.math.geom.Vector3f;

import java.util.Random;

@BehaviorAction(name = "check_target_occupied")
public class CheckTargetOccupiedAction extends BaseAction {

    private final transient Random random = new Random();

    @Override
    public BehaviorState modify(Actor actor, BehaviorState state) {
        MinionMoveComponent moveComponent = actor.getComponent(MinionMoveComponent.class);
        VisitorComponent visitorComponent = actor.getComponent(VisitorComponent.class);
        LocationComponent locationComponent = actor.getComponent(LocationComponent.class);

        if (moveComponent.currentBlock != null) {
            for (EntityRef pen : visitorComponent.pensToVisit) {
                LocationComponent penLocationComponent = pen.getComponent(LocationComponent.class);
                if (Vector3f.distance(moveComponent.target, penLocationComponent.getWorldPosition()) <= 1f) {
                    VisitBlockComponent visitBlockComponent = pen.getComponent(VisitBlockComponent.class);

                    if (visitBlockComponent != null && !visitBlockComponent.isOccupied) {
                        return BehaviorState.SUCCESS;
                    } else {
                        int currentPenIndex = visitorComponent.pensToVisit.indexOf(pen);
                        int newPenIndex = RNG(currentPenIndex, visitorComponent.pensToVisit.size());
                        moveComponent.target =
                                visitorComponent.pensToVisit.get(newPenIndex).getComponent(LocationComponent.class).getWorldPosition();
                        return BehaviorState.SUCCESS;
                    }
                }
            }
            return BehaviorState.SUCCESS;
        } else {
            return BehaviorState.FAILURE;
        }
    }

    private int RNG(int oldNumber, int size) {
        int newNumber = random.nextInt(size);

        if (newNumber != oldNumber) {
            return newNumber;
        } else {
            return RNG(newNumber, size);
        }
    }
}
