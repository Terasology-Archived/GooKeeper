// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gookeeper.component;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.logic.destruction.EngineDamageTypes;

public class PlazMasterComponent implements Component {
    /**
     * The max. distance till which the cannon is viable.
     */
    public float maxDistance;

    /**
     * The max. number of charges in the tank.
     */
    public float maxCharges;

    /**
     * The current number of charges in the tank.
     */
    public float charges;

    /**
     * Time required for the rifle to recover to it's original stance after shooting.
     */
    public float shotRecoveryTime;

    /**
     * The rate of fire of the cannon (per sec).
     */
    public float rateOfFire;

    /**
     * The maximum frequency at which the cannon can be set to.
     */
    public float maxFrequency = 1000f;

    /**
     * The frequency at which the cannon is set to currently.
     */
    public float frequency;

    /**
     * The damage each plasma shot does
     */
    public int damageAmount = 6;

    public Prefab damageType = EngineDamageTypes.PHYSICAL.get();
}
