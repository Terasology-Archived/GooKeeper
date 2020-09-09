// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gookeeper.component;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.entity.EntityRef;

import java.util.ArrayList;
import java.util.List;

public class VisitorComponent implements Component {
    /**
     * The list of visit block entities to be visited
     */
    public List<EntityRef> pensToVisit = new ArrayList<>();

    /**
     * The associated visitor entrance block from where the NPC got spawned
     */
    public EntityRef visitorEntranceBlock = EntityRef.NULL;
}
