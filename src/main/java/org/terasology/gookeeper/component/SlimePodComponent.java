/*
 * Copyright 2017 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.gookeeper.component;

import com.google.common.collect.Lists;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.assets.skeletalmesh.SkeletalMesh;
import org.terasology.world.block.ForceBlockActive;

import java.util.List;

@ForceBlockActive
public class SlimePodComponent implements Component {
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
