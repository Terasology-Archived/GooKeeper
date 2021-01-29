// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gookeeper.actions;

import org.terasology.behaviors.components.AttackOnHitComponent;
import org.terasology.behaviors.components.FleeOnHitComponent;
import org.terasology.behaviors.components.FleeingComponent;
import org.terasology.behaviors.components.FollowComponent;
import org.terasology.engine.Time;
import org.terasology.gookeeper.component.AggressiveComponent;
import org.terasology.gookeeper.component.FriendlyComponent;
import org.terasology.gookeeper.component.NeutralComponent;
import org.terasology.logic.behavior.BehaviorAction;
import org.terasology.logic.behavior.core.Actor;
import org.terasology.logic.behavior.core.BaseAction;
import org.terasology.logic.behavior.core.BehaviorState;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.registry.In;


@BehaviorAction(name = "AfterStunAction")
public class AfterStunAction extends BaseAction {


    @In
    private Time time;

    @In
    private LocalPlayer localPlayer;

    private float stunTime = 0f;

    @Override
    public BehaviorState modify(Actor actor, BehaviorState state) {
        getBehaviorStateWithoutReturn(actor);

        return BehaviorState.SUCCESS;
    }

    private void getBehaviorStateWithoutReturn(Actor actor) {

        if (actor.hasComponent(AggressiveComponent.class)) {
            AttackOnHitComponent attackOnHitComponent = actor.getComponent(AttackOnHitComponent.class);
            attackOnHitComponent.instigator = localPlayer.getCharacterEntity();
            attackOnHitComponent.timeWhenHit =  time.getGameTimeInMs();
            actor.save(attackOnHitComponent);

            FollowComponent followComponent = new FollowComponent();
            followComponent.entityToFollow = localPlayer.getCharacterEntity();
            actor.save(followComponent);
        } else if (actor.hasComponent(NeutralComponent.class)) {
            AttackOnHitComponent attackOnHitComponent = actor.getComponent(AttackOnHitComponent.class);
            attackOnHitComponent.instigator = localPlayer.getCharacterEntity();
            attackOnHitComponent.timeWhenHit =  time.getGameTimeInMs();
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

            CharacterMovementComponent characterMovementComponent = actor.getComponent(CharacterMovementComponent.class);
            characterMovementComponent.speedMultiplier = actor.getComponent(FleeOnHitComponent.class).speedMultiplier;
            actor.save(characterMovementComponent);
            actor.save(fleeOnHitComponent);
        }
    }

}
