// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gookeeper.component;

import com.google.common.collect.Lists;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.List;
import java.util.Optional;

public class GooeyComponent implements Component<GooeyComponent> {
    /**
     *  The prefab corresponding to this gooey type
     */
    public Optional<Prefab> prefab;

    /**
     *  The profit factor. (i.e how much money does the player make from the visitors viewing the gooey)
     */
    public float profitPayOff;

    /**
     * The biome this type of gooey are to be found in.
     */
    public List<String> biome = Lists.newArrayList();

    /**
     * This attribute is checked while spawning the gooey.
     */
    public String blockBelow;

    /**
     * The percentage chance of spawning this type of gooey. (i.e rarity factor)
     */
    public float SPAWN_CHANCE;

    /**
     * The maximum number of gooeys in a group.
     */
    public int MAX_GROUP_SIZE;

    /**
     * The max. number of charges required to stun the gooey.
     */
    public int maxStunChargesReq = 3;

    /**
     * The number of charges required to stun the gooey. (current)
     */
    public int stunChargesReq = 3;

    /**
     * Time (in sec) for which the gooey remains stunned.
     */
    public float stunTime = 3f;

    /**
     * The PlazMaster 3000 cannon frequency required for stunning the gooey.
     */
    public float stunFrequency = 100f;

    /**
     * Bool regarding whether the gooey has been stunned by the player or not.
     */
    public boolean isStunned = false;

    /**
     * Bool regarding whether the gooey has been captured by the player or not.
     */
    public boolean isCaptured = false;

    /**
     * The probability factor of getting captured in a slime pod
     */
    public float captureProbabiltyFactor = 15f;

    /**
     * The pen ID in which the gooey is stored
     */
    public int penNumber = 0;

    /**
     * Lifetime of the entity
     */
    public long lifeTime = 1800000;

}
