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
package org.terasology.gookeeper.event;

import org.terasology.engine.entitySystem.event.AbstractConsumableEvent;

/**
 * This event is sent whenever an entity requires a change in it's behavior tree.
 * Using this event several behavior trees can be chained together to facilitate
 * an event driven behavior tree org.terasology.gookeeper.event.
 */
public class UpdateBehaviorEvent extends AbstractConsumableEvent {

}
