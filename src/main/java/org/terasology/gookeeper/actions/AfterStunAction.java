// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gookeeper.actions;

import org.terasology.behaviors.components.AttackOnHitComponent;
import org.terasology.behaviors.components.FleeOnHitComponent;
import org.terasology.behaviors.components.FleeingComponent;
import org.terasology.behaviors.components.FollowComponent;
import org.terasology.engine.core.Time;
import org.terasology.engine.logic.behavior.BehaviorAction;
import org.terasology.engine.logic.behavior.core.Actor;
import org.terasology.engine.logic.behavior.core.BaseAction;
import org.terasology.engine.logic.behavior.core.BehaviorState;
import org.terasology.engine.logic.characters.CharacterMovementComponent;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.registry.In;
import org.terasology.gookeeper.component.AggressiveComponent;
import org.terasology.gookeeper.component.FriendlyComponent;
import org.terasology.gookeeper.component.NeutralComponent;


@BehaviorAction(name = "AfterStunAction")
public class AfterStunAction extends BaseAction {


    private final float stunTime = 0f;
    @In
    private Time time;
    @In
    private LocalPlayer localPlayer;

    @Override
    public BehaviorState modify(Actor actor, BehaviorState state) {
        getBehaviorStateWithoutReturn(actor);

        return BehaviorState.SUCCESS;
    }

    private void getBehaviorStateWithoutReturn(Actor actor) {

        if (actor.hasComponent(AggressiveComponent.class)) {
            AttackOnHitComponent attackOnHitComponent = actor.getComponent(AttackOnHitComponent.class);
            attackOnHitComponent.instigator = localPlayer.getCharacterEntity();
            attackOnHitComponent.timeWhenHit = time.getGameTimeInMs();
            actor.save(attackOnHitComponent);

            FollowComponent followComponent = new FollowComponent();
            followComponent.entityToFollow = localPlayer.getCharacterEntity();
            actor.save(followComponent);
        } else if (actor.hasComponent(NeutralComponent.class)) {
            AttackOnHitComponent attackOnHitComponent = actor.getComponent(AttackOnHitComponent.class);
            attackOnHitComponent.instigator = localPlayer.getCharacterEntity();
            attackOnHitComponent.timeWhenHit = time.getGameTimeInMs();
            actor.save(attackOnHitComponent);

            FollowComponent followComponent = new FollowComponent();
            followComponent.entityToFollow = localPlayer.getCharacterEntity();
            actor.save(followComponent);
        } else if (actor.hasComponent(FriendlyComponent.class)) {
            FleeOnHitComponent fleeOnHitComponent = actor.getComponent(FleeOnHitComponent.class);
            FleeingComponent fleeComponent = actor.getComponent(FleeingComponent.class);
            fleeComponent.instigator = localPlayer.getCharacterEntity();
            fleeComponent.minDistance = actor.getComponent(FleeOnHitComponent.class).minDistance;
            actor.save(fleeComponent);

            CharacterMovementComponent characterMovementComponent =
                    actor.getComponent(CharacterMovementComponent.class);
            characterMovementComponent.speedMultiplier = actor.getComponent(FleeOnHitComponent.class).speedMultiplier;
            actor.save(characterMovementComponent);
            actor.save(fleeOnHitComponent);
        }
    }

}
