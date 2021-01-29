// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gookeeper.actions;

import org.terasology.logic.behavior.BehaviorAction;
import org.terasology.logic.behavior.core.Actor;
import org.terasology.logic.behavior.core.BaseAction;
import org.terasology.logic.behavior.core.BehaviorState;
import org.terasology.logic.characters.CharacterMovementComponent;


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
