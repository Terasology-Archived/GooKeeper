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

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.world.block.ForceBlockActive;

import java.util.List;

public class SlimePodComponent implements Component {
    /**
     * The entity captured in the slime pod
     */
    public EntityRef capturedEntity = EntityRef.NULL;

    /**
     * The type of slime pod (since different types of gooeys require different types of slime pods)
     */
    public String podType;

    /**
     * The boolean whether the slime pod has been activated by the player or not
     */
    public boolean isActivated = true;

    /**
     * The list of components disabled from the entity
     */
    public List<Component> disabledComponents;
}
