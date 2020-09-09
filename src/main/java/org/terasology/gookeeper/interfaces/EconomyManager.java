// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gookeeper.interfaces;

import org.terasology.engine.entitySystem.entity.EntityRef;

public interface EconomyManager {
    void payEntranceFee(EntityRef visitor);

    void payVisitFee(EntityRef visitor, EntityRef visitBlock);
}

