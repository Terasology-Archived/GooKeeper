// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gookeeper.component;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.gestalt.entitysystem.component.Component;

public class VisitorEntranceComponent implements Component<VisitorEntranceComponent> {
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
