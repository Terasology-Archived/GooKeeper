/*
 * Copyright 2018 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.gookeeper.actions;

import org.joml.Vector3f;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.behavior.BehaviorAction;
import org.terasology.engine.logic.behavior.core.Actor;
import org.terasology.engine.logic.behavior.core.BaseAction;
import org.terasology.engine.logic.behavior.core.BehaviorState;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.gookeeper.component.VisitorComponent;
import org.terasology.gookeeper.event.LeaveVisitBlockEvent;
import org.terasology.minion.move.MinionMoveComponent;

@BehaviorAction(name = "visitor_exit")
public class VisitorExitAction extends BaseAction {

    @Override
    public BehaviorState modify(Actor actor, BehaviorState state) {
        MinionMoveComponent minionMoveComponent = actor.getComponent(MinionMoveComponent.class);
        VisitorComponent visitorComponent = actor.getComponent(VisitorComponent.class);

        int penListSize = visitorComponent.pensToVisit.size();
        EntityRef exitBlock = visitorComponent.pensToVisit.get(penListSize - 1);

        Vector3f exitBlockPosition = exitBlock.getComponent(LocationComponent.class).getWorldPosition(new Vector3f());
        if (Vector3f.distance(minionMoveComponent.target.x(), minionMoveComponent.target.y(),
                minionMoveComponent.target.z(), exitBlockPosition.x(), exitBlockPosition.y(), exitBlockPosition.z()) <= 1f) {
            actor.getEntity().destroy();
            return BehaviorState.SUCCESS;
        } else {
            for (EntityRef pen : visitorComponent.pensToVisit) {
                Vector3f penPosition = pen.getComponent(LocationComponent.class).getWorldPosition(new Vector3f());
                if (Vector3f.distance(minionMoveComponent.target.x(), minionMoveComponent.target.y(),
                        minionMoveComponent.target.z(), penPosition.x(), penPosition.y(), penPosition.z()) <= 1f) {
                    actor.getEntity().send(new LeaveVisitBlockEvent(actor.getEntity(), pen));
                    break;
                }
            }
            return BehaviorState.SUCCESS;
        }
    }
}
