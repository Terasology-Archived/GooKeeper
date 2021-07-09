// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gookeeper.component;

import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.gestalt.entitysystem.component.Component;

public class SlimePodItemComponent implements Component<SlimePodItemComponent> {
    /**
     * The prefab that is actually thrown as a projectile
     */
    public Prefab launchPrefab;

    /**
     * The number of slime pods available in the launcher
     */
    public int slimePods = 5;
}
