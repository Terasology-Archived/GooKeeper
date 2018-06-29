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

import java.util.ArrayList;
import java.util.List;

public class HungerComponent implements Component {
    /**
     * The block items which are allowed to be consumed by the gooey entity
     */
    public List<String> foodBlockNames = new ArrayList<>();

    /**
     * The time to be elapsed before the entity's health starts dropping
     */
    public long timeBeforeHungry = 70000;

    /**
     * The time interval in which the entity's health starts dropping unless fed
     */
    public long healthDecreaseInterval = 20000;

    /**
     * The amount of health lost every 'healthDecreaseInterval'
     */
    public float healthDecreaseAmount = 2f;
}
