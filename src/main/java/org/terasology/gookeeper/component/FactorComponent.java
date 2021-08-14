// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gookeeper.component;

import org.terasology.gestalt.entitysystem.component.Component;

public abstract class FactorComponent<T extends FactorComponent> implements Component<T> {
    public float magnitude;

    @Override
    public void copyFrom(T other) {
        this.magnitude = other.magnitude;
    }
}
