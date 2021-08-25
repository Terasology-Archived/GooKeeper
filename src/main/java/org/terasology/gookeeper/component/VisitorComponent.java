// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gookeeper.component;

import com.google.common.collect.Lists;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.ArrayList;
import java.util.List;

public class VisitorComponent implements Component<VisitorComponent> {
    /**
     * The list of visit block entities to be visited
     */
    public List<EntityRef> pensToVisit = new ArrayList<>();

    /**
     * The associated visitor entrance block from where the NPC got spawned
     */
    public EntityRef visitorEntranceBlock = EntityRef.NULL;

    @Override
    public void copyFrom(VisitorComponent other) {
        this.visitorEntranceBlock = other.visitorEntranceBlock;
        this.pensToVisit = Lists.newArrayList(other.pensToVisit);
    }
}
