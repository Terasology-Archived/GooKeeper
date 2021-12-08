// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gookeeper.system;

import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.items.BlockItemFactory;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.module.inventory.components.InventoryComponent;
import org.terasology.module.inventory.events.RemoveItemAction;
import org.terasology.module.inventory.systems.InventoryManager;

@RegisterSystem(RegisterMode.AUTHORITY)
public class PlayerStartingInventorySystem extends BaseComponentSystem {
    @In
    BlockManager blockManager;
    @In
    InventoryManager inventoryManager;
    @In
    EntityManager entityManager;

    @ReceiveEvent(components = InventoryComponent.class)
    public void onPlayerSpawnedEvent(OnPlayerSpawnedEvent event, EntityRef player) {
        BlockItemFactory blockFactory = new BlockItemFactory(entityManager);

        // Remove the already existing, unnecessary items
        for (int i = 0; i < inventoryManager.getNumSlots(player); i++) {
            EntityRef itemInSlot = inventoryManager.getItemInSlot(player, i);

            if (itemInSlot != EntityRef.NULL) {
                player.send(new RemoveItemAction(player, itemInSlot, true));
            }
        }

        inventoryManager.giveItem(player, EntityRef.NULL, entityManager.create("StructureTemplates:toolbox"));
        inventoryManager.giveItem(player, EntityRef.NULL, entityManager.create("GooKeeper:plazmaster"));
        inventoryManager.giveItem(player, EntityRef.NULL, entityManager.create("GooKeeper:slimepod"));
        inventoryManager.giveItem(player, EntityRef.NULL, blockFactory.newInstance(blockManager.getBlockFamily("GooKeeper:yellowpen"), 32));
        inventoryManager.giveItem(player, EntityRef.NULL, blockFactory.newInstance(blockManager.getBlockFamily("GooKeeper:redpen"), 32));
        inventoryManager.giveItem(player, EntityRef.NULL, blockFactory.newInstance(blockManager.getBlockFamily("GooKeeper:bluepen"), 32));
        inventoryManager.giveItem(player, EntityRef.NULL, blockFactory.newInstance(blockManager.getBlockFamily("GooKeeper:visitorentrance"), 1));
        inventoryManager.giveItem(player, EntityRef.NULL, blockFactory.newInstance(blockManager.getBlockFamily("GooKeeper:visitorexit"), 1));
        inventoryManager.giveItem(player, EntityRef.NULL, blockFactory.newInstance(blockManager.getBlockFamily("GooKeeper:visitblock"), 20));
        inventoryManager.giveItem(player, EntityRef.NULL, blockFactory.newInstance(blockManager.getBlockFamily("GooKeeper:breedingblock"), 2));
    }
}
