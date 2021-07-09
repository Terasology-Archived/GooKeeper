// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gookeeper.component;

import org.terasology.gestalt.entitysystem.component.Component;

public class PurchasableComponent implements Component<PurchasableComponent> {
    /**
     * The base price to purchase a single block of this item
     */
    public float basePrice = 500f;

    /**
     * The base quantity of this item to be purchased if quantity isn't mentioned explicitly
     */
    public int baseQuantity = 16;
}
