// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gookeeper.component;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.entity.EntityRef;

public class MatingComponent implements Component {
    public EntityRef matingWithEntity = EntityRef.NULL;
    public boolean selectedForMating = false;
}
