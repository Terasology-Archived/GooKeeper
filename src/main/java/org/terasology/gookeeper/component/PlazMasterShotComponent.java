// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gookeeper.component;

import org.terasology.gestalt.entitysystem.component.Component;

public class PlazMasterShotComponent implements Component<PlazMasterShotComponent> {
    public float velocity = 0.7f;

    @Override
    public void copyFrom(PlazMasterShotComponent other) {
        this.velocity = other.velocity;
    }
}
