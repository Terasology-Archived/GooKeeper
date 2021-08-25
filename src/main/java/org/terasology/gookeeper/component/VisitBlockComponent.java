// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gookeeper.component;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.gestalt.entitysystem.component.Component;

public class VisitBlockComponent implements Component<VisitBlockComponent> {
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

    @Override
    public void copyFrom(VisitBlockComponent other) {
        this.isOccupied = other.isOccupied;
        this.type = other.type;
        this.cutoffFactor = other.cutoffFactor;
        this.penNumber = other.penNumber;
        this.gooeyQuantity = other.gooeyQuantity;
        this.owner = other.owner;
    }
}
