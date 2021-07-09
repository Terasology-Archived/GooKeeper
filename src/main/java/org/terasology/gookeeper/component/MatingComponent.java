// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gookeeper.component;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.gestalt.entitysystem.component.Component;

public class MatingComponent implements Component<MatingComponent> {
    public EntityRef matingWithEntity = EntityRef.NULL;
    public boolean selectedForMating = false;
}
