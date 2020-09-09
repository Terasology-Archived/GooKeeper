// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gookeeper.event;

import org.terasology.engine.entitySystem.event.AbstractConsumableEvent;

/**
 * This event is sent whenever an entity requires a change in it's behavior tree. Using this event several behavior
 * trees can be chained together to facilitate an event driven behavior tree org.terasology.gookeeper.event.
 */
public class UpdateBehaviorEvent extends AbstractConsumableEvent {

}
