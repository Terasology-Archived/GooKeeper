// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gookeeper.component;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.world.block.ForceBlockActive;

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
