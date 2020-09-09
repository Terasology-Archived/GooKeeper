// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gookeeper.component;

import org.terasology.engine.entitySystem.Component;

import java.util.ArrayList;
import java.util.List;

public class HungerComponent implements Component {
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
}
