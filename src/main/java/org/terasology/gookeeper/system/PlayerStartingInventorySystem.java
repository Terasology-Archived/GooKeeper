/*
 * Copyright 2018 MovingBlocks
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

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.logic.inventory.action.RemoveItemAction;
import org.terasology.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.registry.In;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.items.BlockItemFactory;

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
