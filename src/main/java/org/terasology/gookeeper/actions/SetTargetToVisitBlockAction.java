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
import org.terasology.engine.registry.In;
import org.terasology.gookeeper.component.VisitBlockComponent;
import org.terasology.gookeeper.component.VisitorComponent;
import org.terasology.pathfinding.componentSystem.PathfinderSystem;

import java.util.Random;

@BehaviorAction(name = "set_target_to_visit_block")
public class SetTargetToVisitBlockAction extends BaseAction {

    private final transient Random random = new Random();
    @In
    private PathfinderSystem pathfinderSystem;

    @Override
    public BehaviorState modify(Actor actor, BehaviorState result) {
        MinionMoveComponent moveComponent = actor.getComponent(MinionMoveComponent.class);
        VisitorComponent visitorComponent = actor.getComponent(VisitorComponent.class);

        if (moveComponent.currentBlock != null && visitorComponent.pensToVisit.size() > 0) {
            int penIndex = getRandomPenIndex(visitorComponent);
            EntityRef penToVisit = visitorComponent.pensToVisit.get(penIndex);

            moveComponent.target = penToVisit.getComponent(LocationComponent.class).getWorldPosition();
            actor.save(moveComponent);
        } else {
            return BehaviorState.FAILURE;
        }
        return BehaviorState.SUCCESS;
    }

    private int getRandomPenIndex(VisitorComponent visitorComponent) {
        int penIndex = random.nextInt(visitorComponent.pensToVisit.size());

        EntityRef penToVisit = visitorComponent.pensToVisit.get(penIndex);

        if (penToVisit != EntityRef.NULL) {
            VisitBlockComponent visitBlockComponent = penToVisit.getComponent(VisitBlockComponent.class);

            if (visitBlockComponent.gooeyQuantity > 0) {
                return penIndex;
            } else {
                if (visitorComponent.pensToVisit.size() > 1) {
                    visitorComponent.pensToVisit.remove(penIndex);
                    getRandomPenIndex(visitorComponent);
                } else {
                    return 0;
                }
            }
        }
        return 0;
    }
}

