// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gookeeper.component;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.entity.EntityRef;

public class VisitorEntranceComponent implements Component {
    /**
     * The initial delay before the spawning begins
     */
    public long initialDelay = 2000;

    /**
     * The frequency of spawning the visitor NPCs
     */
    public long visitorSpawnRate = 7000;

    /**
     * The owner entity of this block
     */
    public EntityRef owner = EntityRef.NULL;
}
