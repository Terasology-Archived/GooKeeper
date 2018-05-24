/*
 * Copyright 2017 MovingBlocks
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

import org.terasology.behaviors.components.AttackOnHitComponent;
import org.terasology.behaviors.components.FleeComponent;
import org.terasology.behaviors.components.FleeOnHitComponent;
import org.terasology.behaviors.components.FollowComponent;
import org.terasology.engine.Time;
import org.terasology.gookeeper.component.AggressiveComponent;
import org.terasology.gookeeper.component.FriendlyComponent;
import org.terasology.gookeeper.component.GooeyComponent;
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
            FleeComponent fleeComponent = actor.getComponent(FleeComponent.class);
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
