// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gookeeper.component;

import org.terasology.gestalt.entitysystem.component.Component;

public class EconomyComponent implements Component<EconomyComponent> {
    public float playerWalletCredit = 2000f;

    @Override
    public void copy(EconomyComponent other) {
        this.playerWalletCredit = other.playerWalletCredit;
    }
}
