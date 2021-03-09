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
package org.terasology.gookeeper.component;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.entity.EntityRef;

public class VisitBlockComponent implements Component {
    /**
     * Boolean whether the block is currently occupied by a visitor
     */
    public boolean isOccupied = false;

    /**
     * The type of pen to which the visit block is attached
     */
    public String type = "undefined";

    /**
     * The cutoff factor for the visit block depending upon the rarity of the housed gooeys
     */
    public float cutoffFactor;

    /**
     * ID of the pen associated with this visit block
     */
    public int penNumber;

    /**
     * The number of gooeys housed in the corresponding pen
     */
    public int gooeyQuantity = 0;

    /**
     * The owner entity of this block
     */
    public EntityRef owner = EntityRef.NULL;
}
