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

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.gookeeper.component.VisitBlockComponent;
import org.terasology.gookeeper.component.VisitorComponent;
import org.terasology.gookeeper.event.LeaveVisitBlockEvent;
import org.terasology.gookeeper.interfaces.EconomyManager;
import org.terasology.logic.behavior.BehaviorAction;
import org.terasology.logic.behavior.core.Actor;
import org.terasology.logic.behavior.core.BaseAction;
import org.terasology.logic.behavior.core.BehaviorState;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.minion.move.MinionMoveComponent;
import org.terasology.registry.In;

@BehaviorAction(name = "visitor_exit")
public class VisitorExitAction extends BaseAction {

    @Override
    public BehaviorState modify(Actor actor, BehaviorState state) {
        MinionMoveComponent minionMoveComponent = actor.getComponent(MinionMoveComponent.class);
        VisitorComponent visitorComponent = actor.getComponent(VisitorComponent.class);

        int penListSize = visitorComponent.pensToVisit.size();
        EntityRef exitBlock = visitorComponent.pensToVisit.get(penListSize - 1);

        if (Vector3f.distance(minionMoveComponent.target, exitBlock.getComponent(LocationComponent.class).getWorldPosition()) <= 1f) {
            actor.getEntity().destroy();
            return BehaviorState.SUCCESS;
        } else {
            for (EntityRef pen : visitorComponent.pensToVisit) {
                if (Vector3f.distance(minionMoveComponent.target, pen.getComponent(LocationComponent.class).getWorldPosition()) <= 1f) {
                    actor.getEntity().send(new LeaveVisitBlockEvent(actor.getEntity(), pen));
                    break;
                }
            }
            return BehaviorState.SUCCESS;
        }
    }
}
