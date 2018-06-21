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
import org.terasology.gookeeper.component.PenBlockComponent;
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
@Share(value = EconomyManager.class)
public class EconomySystem extends BaseComponentSystem implements UpdateSubscriberSystem, EconomyManager {

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

    private static final Logger logger = LoggerFactory.getLogger(EconomySystem.class);
    private Random random = new FastRandom();
    private static final float baseEntranceFee = 100f;
    private static final float baseVisitFee = 10f;
    private static boolean setHud = false;

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void update(float delta) {
        // Have to add the wallet hud this way since adding in within initialise() results in the economy component to be null
        if (!setHud) {
            for (EntityRef wallet : entityManager.getEntitiesWith(EconomyComponent.class)) {
                EconomyComponent economyComponent = wallet.getComponent(EconomyComponent.class);
                if (economyComponent != null) {
                    nuiManager.getHUD().addHUDElement("WalletHud");
                    setHud = true;
                }
            }
        }
    }

    /**
     * This function is to be called by a visitor entity when it gets spawned into the world
     * Adds up credit in the player wallet in form of an entrance fee.
     *
     * @param visitor The visitor entity
     */
    @Override
    public void payEntranceFee (EntityRef visitor) {
        for (EntityRef wallet : entityManager.getEntitiesWith(EconomyComponent.class)) {
            EconomyComponent economyComponent = wallet.getComponent(EconomyComponent.class);
            economyComponent.playerWalletCredit += baseEntranceFee;
            wallet.saveComponent(economyComponent);
        }
    }

    /**
     * This function is to be called by a visitor entity when it visits a particular visit block attached to a pen,
     * and depending upon the rarity and number of gooeys in the pen, credits get added accordingly.
     *
     * @param visitor,visitBlock The visitor entity, the visit block entity
     */

    //TODO: add the credits based on the gooey's profit payoff factor, and also consider the number of gooeys in pen
    @Override
    public void payVisitFee (EntityRef visitor, EntityRef visitBlock) {
        for (EntityRef wallet : entityManager.getEntitiesWith(EconomyComponent.class)) {
            EconomyComponent economyComponent = wallet.getComponent(EconomyComponent.class);
            VisitBlockComponent visitBlockComponent = visitBlock.getComponent(VisitBlockComponent.class);

            Prefab gooeyPrefab = prefabManager.getPrefab("GooKeeper:"+ visitBlockComponent.type);

            if (gooeyPrefab != null && gooeyPrefab.hasComponent(GooeyComponent.class)) {
                float profitPayOff = gooeyPrefab.getComponent(GooeyComponent.class).profitPayOff;

                economyComponent.playerWalletCredit += baseVisitFee * profitPayOff;
                wallet.saveComponent(economyComponent);
            }
        }
    }

    /**
     * Receives ActivateEvent when the held player wallet block is activated, and printing the current balance in the wallet.
     *
     * @param event,entity   The ActivateEvent event, the wallet entity
     */
    @ReceiveEvent(components = {EconomyComponent.class})
    public void onActivate(ActivateEvent event, EntityRef entity) {
        EconomyComponent economyComponent = entity.getComponent(EconomyComponent.class);
        if (economyComponent != null) {
            logger.info("Current Wallet Balance: " + economyComponent.playerWalletCredit);
        }
    }
}
