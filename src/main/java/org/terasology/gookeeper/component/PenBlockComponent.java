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
package org.terasology.gookeeper.component;

import org.terasology.entitySystem.Component;
import org.terasology.world.block.ForceBlockActive;

@ForceBlockActive
public class PenBlockComponent implements Component {
    /**
     * Defines the type of gooeys housed in a pen made up of this block
     */
    public String type = "undefined";

    /**
     * The likelihood factor of visitors "visiting" this pen. Depends upon the rarity of gooeys housed within
     */
    public int cutoffFactor;

    /**
     * ID of the pen constructed using this block
     */
    public int penNumber = 0;

    public boolean penIDSet = false;
}
