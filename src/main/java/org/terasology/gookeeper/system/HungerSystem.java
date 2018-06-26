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
package org.terasology.gookeeper.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.management.AssetManager;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.gookeeper.component.EconomyComponent;
import org.terasology.gookeeper.component.GooeyComponent;
import org.terasology.gookeeper.component.VisitBlockComponent;
import org.terasology.gookeeper.interfaces.EconomyManager;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.physics.Physics;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;

@RegisterSystem(RegisterMode.AUTHORITY)
@Share(value = HungerSystem.class)
public class HungerSystem extends BaseComponentSystem implements UpdateSubscriberSystem {

    @In
    private WorldProvider worldProvider;

    @In
    private EntityManager entityManager;

    @In
    private BlockEntityRegistry blockEntityRegistry;

    @In
    private LocalPlayer localPlayer;

    @In
    private InventoryManager inventoryManager;

    @In
    private Physics physics;

    @In
    private AssetManager assetManager;

    @In
    private PrefabManager prefabManager;

    @In
    private NUIManager nuiManager;

    private static final Logger logger = LoggerFactory.getLogger(HungerSystem.class);
    private Random random = new FastRandom();

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void update(float delta) {
    }
}