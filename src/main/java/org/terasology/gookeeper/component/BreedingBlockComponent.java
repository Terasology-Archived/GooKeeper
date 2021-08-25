// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gookeeper.component;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.gestalt.entitysystem.component.Component;

public class BreedingBlockComponent implements Component<BreedingBlockComponent> {
    /**
     * The gooey entity which is to be involved in mating
     */
    public EntityRef parentGooey = EntityRef.NULL;

    @Override
    public void copyFrom(BreedingBlockComponent other) {
        this.parentGooey = other.parentGooey;
    }
}
