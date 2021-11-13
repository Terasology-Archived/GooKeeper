// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gookeeper.interfaces;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.gookeeper.component.VisitorComponent;

public interface EconomyManager {
    void payEntranceFee(VisitorComponent visitorComponent);

    void payVisitFee(VisitorComponent visitorComponent, EntityRef visitBlock);
}

