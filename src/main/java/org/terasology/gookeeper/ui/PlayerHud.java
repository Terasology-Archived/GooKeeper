// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gookeeper.ui;

import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.layers.hud.CoreHudWidget;
import org.terasology.gookeeper.component.EconomyComponent;
import org.terasology.gookeeper.component.PlazMasterComponent;
import org.terasology.gookeeper.component.SlimePodItemComponent;
import org.terasology.inventory.logic.InventoryManager;
import org.terasology.nui.databinding.ReadOnlyBinding;
import org.terasology.nui.widgets.UIText;

public class PlayerHud extends CoreHudWidget {
    @In
    private EntityManager entityManager;

    @In
    private LocalPlayer localPlayer;

    @In
    private InventoryManager inventoryManager;

    @Override
    public void initialise() {
        EntityRef player = localPlayer.getCharacterEntity();
        EconomyComponent economyComponent = player.getComponent(EconomyComponent.class);
        bindWalletText(economyComponent);

        for (int i = 0; i < inventoryManager.getNumSlots(player); i++) {
            EntityRef itemInSlot = inventoryManager.getItemInSlot(player, i);

            if (itemInSlot != EntityRef.NULL && itemInSlot.hasComponent(SlimePodItemComponent.class)) {
                SlimePodItemComponent slimePodItemComponent = itemInSlot.getComponent(SlimePodItemComponent.class);
                bindSlimePodText(slimePodItemComponent);
            } else if (itemInSlot != EntityRef.NULL && itemInSlot.hasComponent(PlazMasterComponent.class)) {
                PlazMasterComponent plazMasterComponent = itemInSlot.getComponent(PlazMasterComponent.class);
                bindPlazmasterText(plazMasterComponent);
            }
        }
    }

    public void bindWalletText(EconomyComponent component) {
        if (component != null) {
            UIText walletBalance;

            walletBalance = find("walletBalance", UIText.class);
            walletBalance.bindText(new ReadOnlyBinding<String>() {
                @Override
                public String get() {
                    return "Wallet Balance: " + component.playerWalletCredit;
                }
            });
        }
    }

    public void bindSlimePodText(SlimePodItemComponent component) {
        if (component != null) {
            UIText slimePodQuantity;

            slimePodQuantity = find("slimePodAmount", UIText.class);
            slimePodQuantity.bindText(new ReadOnlyBinding<String>() {
                @Override
                public String get() {
                    return "Slime Pods: " + component.slimePods;
                }
            });
        }
    }

    public void bindPlazmasterText(PlazMasterComponent component) {
        if (component != null) {
            UIText plazmasterCharge;
            UIText plazmasterFrequency;

            plazmasterCharge = find("plazmasterCharges", UIText.class);
            plazmasterCharge.bindText(new ReadOnlyBinding<String>() {
                @Override
                public String get() {
                    return "PlazMaster Charges: " + component.charges;
                }
            });

            plazmasterFrequency = find("plazmasterFrequency", UIText.class);
            plazmasterFrequency.bindText(new ReadOnlyBinding<String>() {
                @Override
                public String get() {
                    return "PlazMaster Frequency: " + component.frequency;
                }
            });
        }
    }
}
