// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gookeeper.actions;

import org.joml.RoundingMode;
import org.joml.Vector3f;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.behavior.BehaviorAction;
import org.terasology.engine.logic.behavior.core.Actor;
import org.terasology.engine.logic.behavior.core.BaseAction;
import org.terasology.engine.logic.behavior.core.BehaviorState;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.gookeeper.component.VisitBlockComponent;
import org.terasology.gookeeper.component.VisitorComponent;
import org.terasology.module.behaviors.components.MinionMoveComponent;

import java.util.Random;

@BehaviorAction(name = "set_target_to_visit_block")
public class SetTargetToVisitBlockAction extends BaseAction {

    private transient Random random = new Random();

    @Override
    public BehaviorState modify(Actor actor, BehaviorState result) {
        MinionMoveComponent moveComponent = actor.getComponent(MinionMoveComponent.class);
        VisitorComponent visitorComponent = actor.getComponent(VisitorComponent.class);

        if (visitorComponent.pensToVisit.size() > 0) {
            int penIndex = getRandomPenIndex(visitorComponent);
            EntityRef penToVisit = visitorComponent.pensToVisit.get(penIndex);

            Vector3f worldPosition = penToVisit.getComponent(LocationComponent.class).getWorldPosition(new Vector3f());
            moveComponent.target.set(worldPosition, RoundingMode.FLOOR);
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

