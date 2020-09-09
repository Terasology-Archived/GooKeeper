// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gookeeper.component;

import org.terasology.engine.entitySystem.Component;

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
