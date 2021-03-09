// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gookeeper.actions;

import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.behavior.BehaviorAction;
import org.terasology.engine.logic.behavior.core.Actor;
import org.terasology.engine.logic.behavior.core.BaseAction;
import org.terasology.engine.logic.characters.CharacterMoveInputEvent;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.gookeeper.component.VisitBlockComponent;
import org.terasology.gookeeper.component.VisitorComponent;
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

        CharacterMoveInputEvent wantedInput = new CharacterMoveInputEvent(0, 0, 0, new Vector3f(0),
                false, false, false, (long) (actor.getDelta() * 1000));
        actor.getEntity().send(wantedInput);
    }
}
