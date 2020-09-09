// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gookeeper.component;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.entity.EntityRef;

public class BreedingBlockComponent implements Component {
    /**
     * The gooey entity which is to be involved in mating
     */
    public EntityRef parentGooey = EntityRef.NULL;
}
