// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gookeeper.component;

import com.google.common.collect.Lists;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.ArrayList;
import java.util.List;

public class HungerComponent implements Component<HungerComponent> {
    /**
     * The block items which are allowed to be consumed by the gooey entity
     */
    public List<String> food = new ArrayList<>();

    /**
     * The time to be elapsed before the entity's health starts dropping
     */
    public long timeBeforeHungry = 70000;

    /**
     * The time interval in which the entity's health starts dropping unless fed
     */
    public long healthDecreaseInterval = 20000;

    /**
     * The amount of health lost every 'healthDecreaseInterval'
     */
    public float healthDecreaseAmount = 2f;

    @Override
    public void copy(HungerComponent other) {
        this.food = Lists.newArrayList(other.food);
        this.timeBeforeHungry = other.timeBeforeHungry;
        this.healthDecreaseInterval = other.healthDecreaseInterval;
        this.healthDecreaseAmount = other.healthDecreaseAmount;
    }
}
