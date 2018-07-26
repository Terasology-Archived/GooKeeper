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
package org.terasology.gookeeper.event;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.Event;

public class AfterGooeyBreedingEvent implements Event {

    private EntityRef gooey;
    private EntityRef matingWithEntity;
    private EntityRef offspringGooey;

    public AfterGooeyBreedingEvent(EntityRef gooey, EntityRef matingWithEntity, EntityRef offspringGooey) {
        this.gooey = gooey;
        this.matingWithEntity = matingWithEntity;
        this.offspringGooey = offspringGooey;
    }

    public EntityRef getGooey() {
        return gooey;
    }

    public EntityRef getMatingWithEntity() { return matingWithEntity; }

    public EntityRef getOffspringGooey() { return offspringGooey; }
}
