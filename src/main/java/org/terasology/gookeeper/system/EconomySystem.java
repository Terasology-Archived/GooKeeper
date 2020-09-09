// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gookeeper.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.prefab.PrefabManager;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.engine.logic.common.DisplayNameComponent;
import org.terasology.engine.logic.console.commandSystem.annotations.Command;
import org.terasology.engine.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.engine.logic.console.commandSystem.annotations.Sender;
import org.terasology.engine.logic.permission.PermissionManager;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.physics.Physics;
import org.terasology.engine.registry.In;
import org.terasology.engine.registry.Share;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.engine.utilities.Assets;
import org.terasology.engine.utilities.random.FastRandom;
import org.terasology.engine.utilities.random.Random;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.entity.BlockCommands;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.gookeeper.component.EconomyComponent;
import org.terasology.gookeeper.component.GooeyComponent;
import org.terasology.gookeeper.component.PlazMasterComponent;
import org.terasology.gookeeper.component.PurchasableComponent;
import org.terasology.gookeeper.component.SlimePodItemComponent;
import org.terasology.gookeeper.component.UpgradableComponent;
import org.terasology.gookeeper.component.VisitBlockComponent;
import org.terasology.gookeeper.component.VisitorComponent;
import org.terasology.gookeeper.component.VisitorEntranceComponent;
import org.terasology.gookeeper.interfaces.EconomyManager;
import org.terasology.inventory.logic.InventoryManager;
import org.terasology.inventory.logic.ItemCommands;

@RegisterSystem(RegisterMode.AUTHORITY)
@Share(value = EconomyManager.class)
public class EconomySystem extends BaseComponentSystem implements UpdateSubscriberSystem, EconomyManager {

    private static final Logger logger = LoggerFactory.getLogger(EconomySystem.class);
    private static final float baseEntranceFee = 100f;
    private static final float baseVisitFee = 10f;
    private static boolean setHud = false;
    private final Random random = new FastRandom();
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
    @In
    private BlockCommands blockCommands;
    @In
    private ItemCommands itemCommands;

    @Override
    public void update(float delta) {
        if (!setHud && localPlayer.getCharacterEntity().hasComponent(EconomyComponent.class)) {
            nuiManager.getHUD().addHUDElement("PlayerHud");
            setHud = true;
        }
    }

    /**
     * This command can be used to purchase utility blocks (such as pen blocks, visit blocks, etc.)
     *
     * @param client the player entity who sent this command
     * @param itemPrefabName prefabId or blockName of the item to be purchased
     * @param amount the quantity to be purchased
     * @param shapeUriParam blockShapeName
     * @return appropriate message regarding the status of the transaction
     */
    @Command(shortDescription = "Purchase utility blocks",
            requiredPermission = PermissionManager.NO_PERMISSION)
    public String purchase(@Sender EntityRef client,
                           @CommandParam("prefabId or blockName") String itemPrefabName,
                           @CommandParam(value = "amount", required = false) Integer amount,
                           @CommandParam(value = "blockShapeName", required = false) String shapeUriParam) {
        EntityRef player = client.getComponent(ClientComponent.class).character;
        EconomyComponent economyComponent = player.getComponent(EconomyComponent.class);

        PurchasableComponent purchasableComponent =
                Assets.getPrefab(itemPrefabName + "Fenced").get().getComponent(PurchasableComponent.class);
        int quantityParam = amount != null ? amount : purchasableComponent.baseQuantity;

        if (economyComponent != null && economyComponent.playerWalletCredit - (quantityParam * purchasableComponent.basePrice) > 0f) {
            String message = blockCommands.giveBlock(client, itemPrefabName, amount, shapeUriParam);
            if (message != null) {
                economyComponent.playerWalletCredit -= quantityParam * purchasableComponent.basePrice;
                player.saveComponent(economyComponent);
                return "Successfully purchased " + itemPrefabName;
            } else {
                return "Couldn't find requested block.";
            }
        } else {
            return "You dont have sufficient balance to purchase.";
        }
    }

    /**
     * This command can be used to purchase utility blocks (such as pen blocks, visit blocks, etc.)
     *
     * @param client the player entity who sent this command
     * @param itemPrefabName prefabId of the item to be upgraded
     * @return appropriate message regarding the status of the transaction
     */
    @Command(shortDescription = "Upgrade your equipment (PlazMaster and Slime Pod Launcher",
            requiredPermission = PermissionManager.NO_PERMISSION)
    public String upgrade(@Sender EntityRef client,
                          @CommandParam("prefabId or blockName") String itemPrefabName) {
        EntityRef player = client.getComponent(ClientComponent.class).character;
        EconomyComponent economyComponent = player.getComponent(EconomyComponent.class);

        for (int i = 0; i < inventoryManager.getNumSlots(player); i++) {
            EntityRef itemInSlot = inventoryManager.getItemInSlot(player, i);
            DisplayNameComponent displayNameComponent = itemInSlot.getComponent(DisplayNameComponent.class);

            if (displayNameComponent != null && displayNameComponent.name.equals(itemPrefabName)) {
                UpgradableComponent upgradableComponent = itemInSlot.getComponent(UpgradableComponent.class);
                if (economyComponent != null && upgradableComponent != null && (economyComponent.playerWalletCredit - (upgradableComponent.baseUpgradePrice * upgradableComponent.currentTier) > 0f)) {
                    upgradeByType(itemInSlot, upgradableComponent);
                    economyComponent.playerWalletCredit -= upgradableComponent.baseUpgradePrice * upgradableComponent.currentTier;
                    player.saveComponent(economyComponent);
                    itemInSlot.saveComponent(upgradableComponent);
                    return "Successfully upgraded your " + displayNameComponent.name;
                } else {
                    return "You dont have sufficient balance to upgrade the item.";
                }
            }
        }

        return "Couldn't find the requested item to upgrade.";
    }

    private void upgradeByType(EntityRef item, UpgradableComponent upgradableComponent) {
        upgradableComponent.currentTier++;

        if (item.hasComponent(SlimePodItemComponent.class)) {
            SlimePodItemComponent slimePodItemComponent = item.getComponent(SlimePodItemComponent.class);
            slimePodItemComponent.slimePods =
                    upgradableComponent.baseQuantity * (1 + upgradableComponent.currentTier * upgradableComponent.baseQuantityMultiplier);
            item.saveComponent(slimePodItemComponent);
        } else if (item.hasComponent(PlazMasterComponent.class)) {
            PlazMasterComponent plazMasterComponent = item.getComponent(PlazMasterComponent.class);
            plazMasterComponent.charges =
                    upgradableComponent.baseQuantity * (1 + upgradableComponent.currentTier * upgradableComponent.baseQuantityMultiplier);
            plazMasterComponent.maxCharges = plazMasterComponent.charges;
            item.saveComponent(plazMasterComponent);
        }
    }

    /**
     * This function is to be called by a visitor entity when it gets spawned into the world Adds up credit in the
     * player wallet in form of an entrance fee.
     *
     * @param visitor The visitor entity
     */
    @Override
    public void payEntranceFee(EntityRef visitor) {
        VisitorComponent visitorComponent = visitor.getComponent(VisitorComponent.class);
        VisitorEntranceComponent visitorEntranceComponent =
                visitorComponent.visitorEntranceBlock.getComponent(VisitorEntranceComponent.class);
        EntityRef player = visitorEntranceComponent.owner;
        EconomyComponent economyComponent = player.getComponent(EconomyComponent.class);

        if (economyComponent != null) {
            economyComponent.playerWalletCredit += baseEntranceFee;
            player.saveComponent(economyComponent);
        }
    }

    /**
     * This function is to be called by a visitor entity when it visits a particular visit block attached to a pen, and
     * depending upon the rarity and number of gooeys in the pen, credits get added accordingly.
     *
     * @param visitor,visitBlock The visitor entity, the visit block entity
     */

    @Override
    public void payVisitFee(EntityRef visitor, EntityRef visitBlock) {
        VisitorComponent visitorComponent = visitor.getComponent(VisitorComponent.class);
        VisitorEntranceComponent visitorEntranceComponent =
                visitorComponent.visitorEntranceBlock.getComponent(VisitorEntranceComponent.class);
        EntityRef player = visitorEntranceComponent.owner;
        EconomyComponent economyComponent = player.getComponent(EconomyComponent.class);

        VisitBlockComponent visitBlockComponent = visitBlock.getComponent(VisitBlockComponent.class);

        Prefab gooeyPrefab = prefabManager.getPrefab("GooKeeper:" + visitBlockComponent.type);

        if (economyComponent != null && gooeyPrefab != null && gooeyPrefab.hasComponent(GooeyComponent.class)) {
            float profitPayOff = gooeyPrefab.getComponent(GooeyComponent.class).profitPayOff;

            economyComponent.playerWalletCredit += baseVisitFee * profitPayOff * (visitBlockComponent.gooeyQuantity / 3f);
            player.saveComponent(economyComponent);
        }
    }
}
