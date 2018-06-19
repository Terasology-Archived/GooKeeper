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
import org.terasology.registry.In;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.layers.hud.CoreHudWidget;
import org.terasology.rendering.nui.widgets.UIText;

public class WalletHud extends CoreHudWidget {
    @In
    private EntityManager entityManager;

    private UIText walletBalance;
    private EntityRef walletEntity;
    private EconomyComponent economyComponent;

    @Override
    public void initialise() {
        for (EntityRef entity : entityManager.getEntitiesWith(EconomyComponent.class)) {
            walletEntity = entity;
            economyComponent = entity.getComponent(EconomyComponent.class);
        }
        if (walletEntity != EntityRef.NULL) {
            bindText(economyComponent);
        }
    }

    public void bindText (EconomyComponent component) {
        if (component != null) {
            walletBalance = find("walletBalance", UIText.class);
            walletBalance.bindText(new ReadOnlyBinding<String>() {
                @Override
                public String get() {
                    return "Player Credits: " + String.valueOf(component.playerWalletCredit);
                }
            });
        }
    }
}
