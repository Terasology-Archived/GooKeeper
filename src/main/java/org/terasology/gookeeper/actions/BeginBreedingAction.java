// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gookeeper.actions;

import org.terasology.engine.logic.behavior.BehaviorAction;
import org.terasology.engine.logic.behavior.core.Actor;
import org.terasology.engine.logic.behavior.core.BaseAction;
import org.terasology.engine.logic.behavior.core.BehaviorState;
import org.terasology.engine.logic.characters.CharacterMovementComponent;


@BehaviorAction(name = "BeginBreeding")
public class BeginBreedingAction extends BaseAction {

    @Override
    public BehaviorState modify(Actor actor, BehaviorState state) {
        CharacterMovementComponent characterMovementComponent = actor.getComponent(CharacterMovementComponent.class);
        characterMovementComponent.speedMultiplier = 0f;

        actor.save(characterMovementComponent);
        return BehaviorState.SUCCESS;
    }

}
