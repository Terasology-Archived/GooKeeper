// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gookeeper.component;

import com.google.common.collect.Lists;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.rendering.assets.skeletalmesh.SkeletalMesh;
import org.terasology.engine.world.block.ForceBlockActive;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.List;

@ForceBlockActive
public class SlimePodComponent implements Component<SlimePodComponent> {
    /**
     * The prefab that is actually thrown as a projectile
     */
    public Prefab launchPrefab;

    /**
     * The entity captured in the slime pod
     */
    public EntityRef capturedEntity = EntityRef.NULL;

    /**
     * The type of slime pod (since different types of gooeys require different types of slime pods)
     */
    public String podType;

    /**
     * The max distance the slime pod can be from the gooey to capture it
     */
    public float maxDistance = 10f;

    /**
     * The boolean whether the slime pod has been activated by the player or not
     */
    public boolean isActivated = true;

    /**
     * The list of components disabled from the entity
     */
    public List<Component> disabledComponents = Lists.newArrayList();

    /**
     * The mesh corresponding to the captured gooey (used while releasing form pod)
     */
    public SkeletalMesh capturedGooeyMesh;
}
