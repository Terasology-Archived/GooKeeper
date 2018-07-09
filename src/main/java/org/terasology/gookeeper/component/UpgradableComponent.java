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

public class UpgradableComponent implements Component {
    /**
     * The current tier of the item (Base tier is 0)
     */
    public int currentTier = 0;

    /**
     * The base price to upgrade a tier
     */
    public float baseUpgradePrice = 500f;

    /**
     * The base quantity multiplier for the corresponding upgradable attribute of the item
     */
    public int baseQuantityMultiplier = 2;

    /**
     * The quantity to be multiplied with the baseQuantityMultiplier to yield the final quantity
     */
    public int baseQuantity = 5;
}
