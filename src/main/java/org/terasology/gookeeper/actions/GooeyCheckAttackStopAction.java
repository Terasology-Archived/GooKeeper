// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gookeeper.actions;

import org.joml.Vector3f;
import org.terasology.behaviors.components.AttackOnHitComponent;
import org.terasology.behaviors.components.FollowComponent;
import org.terasology.engine.logic.behavior.BehaviorAction;
import org.terasology.engine.logic.behavior.core.Actor;
import org.terasology.engine.logic.behavior.core.BaseAction;
import org.terasology.engine.logic.behavior.core.BehaviorState;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.nui.properties.Range;


@BehaviorAction(name = "CheckAttackStop")
public class GooeyCheckAttackStopAction extends BaseAction {

    @Range(max = 40)
    private float maxDistance = 10f;

    /**
     * Makes the character follow a player within a given range Sends FAILURE when the distance is greater than
     * maxDistance
     */
    @Override
    public BehaviorState modify(Actor actor, BehaviorState state) {
        BehaviorState status = getBehaviorStateWithoutReturn(actor);
        if (status == BehaviorState.FAILURE) {
            AttackOnHitComponent attackOnHitComponent = actor.getComponent(AttackOnHitComponent.class);
            attackOnHitComponent.instigator = null;
            actor.getEntity().saveComponent(attackOnHitComponent);
            actor.getEntity().removeComponent(FollowComponent.class);
        }
        return status;
    }

    private BehaviorState getBehaviorStateWithoutReturn(Actor actor) {
        LocationComponent actorLocationComponent = actor.getComponent(LocationComponent.class);
        if (actorLocationComponent == null) {
            return BehaviorState.FAILURE;
        }
        Vector3f actorPosition = actorLocationComponent.getWorldPosition(new Vector3f());
        float maxDistance = actor.hasComponent(AttackOnHitComponent.class)
                ? actor.getComponent(AttackOnHitComponent.class).maxDistance : this.maxDistance;

        float maxDistanceSquared = maxDistance * maxDistance;
        FollowComponent followWish = actor.getComponent(FollowComponent.class);
        if (followWish == null || followWish.entityToFollow == null) {
            return BehaviorState.FAILURE;
        }

        LocationComponent locationComponent = followWish.entityToFollow.getComponent(LocationComponent.class);
        if (locationComponent == null) {
            return BehaviorState.FAILURE;
        }
        if (locationComponent.getWorldPosition(new Vector3f()).distanceSquared(actorPosition) <= maxDistanceSquared) {
            return BehaviorState.SUCCESS;
        }
        return BehaviorState.FAILURE;
    }

}
