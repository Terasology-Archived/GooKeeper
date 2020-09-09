// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gookeeper.component;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.prefab.Prefab;

public class SlimePodItemComponent implements Component {
    /**
     * The prefab that is actually thrown as a projectile
     */
    public Prefab launchPrefab;

    /**
     * The number of slime pods available in the launcher
     */
    public int slimePods = 5;
}
