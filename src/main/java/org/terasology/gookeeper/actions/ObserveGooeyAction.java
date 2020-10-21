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
import org.joml.Vector3fc;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.gookeeper.component.VisitBlockComponent;
import org.terasology.gookeeper.component.VisitorComponent;
import org.terasology.logic.behavior.BehaviorAction;
import org.terasology.logic.behavior.core.Actor;
import org.terasology.logic.behavior.core.BaseAction;
import org.terasology.logic.characters.CharacterMoveInputEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.JomlUtil;
import org.terasology.math.TeraMath;
import org.terasology.minion.move.MinionMoveComponent;

@BehaviorAction(name = "observe_gooey")
public class ObserveGooeyAction extends BaseAction {

    @Override
    public void construct(Actor actor) {

        // Calculating a lot of superfluous stuff to debug; this'll get cleaned up when stopping is figured out
        LocationComponent locationComponent = actor.getComponent(LocationComponent.class);
        MinionMoveComponent moveComponent = actor.getComponent(MinionMoveComponent.class);
        VisitorComponent visitorComponent = actor.getComponent(VisitorComponent.class);

        for (EntityRef pen : visitorComponent.pensToVisit) {
            Vector3f worldPosition = locationComponent.getWorldPosition(new Vector3f());
            Vector3f penPosition = pen.getComponent(LocationComponent.class).getWorldPosition(new Vector3f());
            if (Vector3f.distance(worldPosition.x(), worldPosition.y(), worldPosition.z(), penPosition.x(),
                    penPosition.y(), penPosition.z()) <= 1f) {
                VisitBlockComponent visitBlockComponent = pen.getComponent(VisitBlockComponent.class);

                if (visitBlockComponent != null) {
                    visitBlockComponent.isOccupied = true;
                    pen.saveComponent(visitBlockComponent);
                    break;
                }
            }
        }

        Vector3f worldPos = new Vector3f(locationComponent.getWorldPosition(new Vector3f()));
        Vector3f targetDirection = new Vector3f();
        targetDirection.sub((Vector3fc) moveComponent.target, worldPos);

        float yaw = (float) Math.atan2(targetDirection.x, targetDirection.z);
        float requestedYaw = 180f + yaw * TeraMath.RAD_TO_DEG;

        CharacterMoveInputEvent wantedInput = new CharacterMoveInputEvent(0, 0, 0, JomlUtil.from(new Vector3f(0)),
                false, false, false, (long) (actor.getDelta() * 1000));
        actor.getEntity().send(wantedInput);
    }
}
