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
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.logic.health.EngineDamageTypes;

public class PlazMasterComponent implements Component {
    /**
     * The max. distance till which the cannon is viable.
     */
    public float maxDistance;

    /**
     *  The max. number of charges in the tank.
     */
    public float maxCharges;

    /**
     *  The current number of charges in the tank.
     */
    public float charges;

    /**
     *  Time required for the rifle to recover to it's original stance after shooting.
     */
    public float shotRecoveryTime;

    /**
     *  The rate of fire of the cannon (per sec).
     */
    public float rateOfFire;

    /**
     *  The maximumum frequency at which the cannon can be set to.
     */
    public float maxFrequency = 1000f;

    /**
     *  The frequency at which the cannon is set to currently.
     */
    public float frequency;

    /**
     * The damage each plasma shot does
     */
    public int damageAmount = 6;

    public Prefab damageType = EngineDamageTypes.PHYSICAL.get();
}
