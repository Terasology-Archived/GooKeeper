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
package org.terasology.gookeeper.ui;

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.gookeeper.component.EconomyComponent;
import org.terasology.gookeeper.component.PlazMasterComponent;
import org.terasology.gookeeper.component.SlimePodItemComponent;
import org.terasology.registry.In;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.layers.hud.CoreHudWidget;
import org.terasology.rendering.nui.widgets.UIText;

public class PlayerHud extends CoreHudWidget {
    @In
    private EntityManager entityManager;


    @Override
    public void initialise() {
        for (EntityRef entity : entityManager.getEntitiesWith(EconomyComponent.class)) {
            EconomyComponent economyComponent = entity.getComponent(EconomyComponent.class);
            bindWalletText(economyComponent);
        }

        for (EntityRef entity : entityManager.getEntitiesWith(SlimePodItemComponent.class)) {
            SlimePodItemComponent slimePodItemComponent = entity.getComponent(SlimePodItemComponent.class);
            bindSlimePodText(slimePodItemComponent);
        }

        for (EntityRef entity : entityManager.getEntitiesWith(PlazMasterComponent.class)) {
            PlazMasterComponent plazMasterComponent = entity.getComponent(PlazMasterComponent.class);
            bindPlazmasterText(plazMasterComponent);
        }
    }

    public void bindWalletText (EconomyComponent component) {
        if (component != null) {
            UIText walletBalance;

            walletBalance = find("walletBalance", UIText.class);
            walletBalance.bindText(new ReadOnlyBinding<String>() {
                @Override
                public String get() {
                    return "Player Credits: " + String.valueOf(component.playerWalletCredit);
                }
            });
        }
    }

    public void bindSlimePodText (SlimePodItemComponent component) {
        if (component != null) {
            UIText slimePodQuantity;

            slimePodQuantity = find("slimePodAmount", UIText.class);
            slimePodQuantity.bindText(new ReadOnlyBinding<String>() {
                @Override
                public String get() {
                    return "Slime Pods: " + String.valueOf(component.slimePods);
                }
            });
        }
    }

    public void bindPlazmasterText (PlazMasterComponent component) {
        if (component != null) {
            UIText plazmasterCharge;

            plazmasterCharge = find("plazmasterCharges", UIText.class);
            plazmasterCharge.bindText(new ReadOnlyBinding<String>() {
                @Override
                public String get() {
                    return "PlazMaster Charges: " + String.valueOf(component.charges);
                }
            });
        }
    }
}
