// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gookeeper.component;

import org.terasology.engine.world.block.ForceBlockActive;
import org.terasology.gestalt.entitysystem.component.Component;

@ForceBlockActive
public class PenBlockComponent implements Component<PenBlockComponent> {
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

    @Override
    public void copy(PenBlockComponent other) {
        this.type = other.type;
        this.cutoffFactor = other.cutoffFactor;
        this.penNumber = other.penNumber;
        this.penIDSet = other.penIDSet;

    }
}
