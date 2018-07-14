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

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.Event;

public class FeedGooeyEvent implements Event {

    private EntityRef instigator;
    private EntityRef gooey;

    public FeedGooeyEvent(EntityRef instigator, EntityRef gooey) {
        this.instigator = instigator;
        this.gooey = gooey;
    }

    public EntityRef getGooey() {
        return gooey;
    }

    public EntityRef getInstigator() {
        return instigator;
    }
}