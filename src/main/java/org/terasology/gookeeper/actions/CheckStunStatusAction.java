// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gookeeper.actions;

import org.terasology.engine.core.Time;
import org.terasology.engine.logic.behavior.BehaviorAction;
import org.terasology.engine.logic.behavior.core.Actor;
import org.terasology.engine.logic.behavior.core.BaseAction;
import org.terasology.engine.logic.behavior.core.BehaviorState;
import org.terasology.engine.logic.characters.CharacterMovementComponent;
import org.terasology.engine.registry.In;
import org.terasology.gookeeper.component.GooeyComponent;

@BehaviorAction(name = "CheckStunStatus")
public class CheckStunStatusAction extends BaseAction {
    @In
    private Time time;

    private float stunTime = 0f;

    @Override
    public BehaviorState modify(Actor actor, BehaviorState state) {
        BehaviorState status = getBehaviorStateWithoutReturn(actor);

        if (status == BehaviorState.SUCCESS) {
            GooeyComponent gooeyComponent = actor.getComponent(GooeyComponent.class);
            gooeyComponent.isStunned = false;
            actor.getEntity().saveComponent(gooeyComponent);
        }
        return BehaviorState.SUCCESS;
    }

    private BehaviorState getBehaviorStateWithoutReturn(Actor actor) {
        CharacterMovementComponent characterMovementComponent = actor.getComponent(CharacterMovementComponent.class);

        stunTime += time.getGameDelta();
        if (stunTime < actor.getComponent(GooeyComponent.class).stunTime) {
            characterMovementComponent.speedMultiplier = 0f;
            actor.getEntity().saveComponent(characterMovementComponent);
            return BehaviorState.FAILURE;
        } else {
            characterMovementComponent.speedMultiplier = 1f;
            actor.getEntity().saveComponent(characterMovementComponent);
            stunTime = 0f;
            return BehaviorState.SUCCESS;
        }
    }
}
