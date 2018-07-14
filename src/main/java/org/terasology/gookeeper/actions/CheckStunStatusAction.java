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
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.geom.Vector3f;
import org.terasology.registry.In;
import org.terasology.rendering.nui.properties.Range;


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
