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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.management.AssetManager;
import org.terasology.behaviors.components.FollowComponent;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.OnAddedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.gookeeper.Constants;
import org.terasology.gookeeper.component.GooeyComponent;
import org.terasology.gookeeper.component.HungerComponent;
import org.terasology.gookeeper.component.MatingComponent;
import org.terasology.gookeeper.event.AfterGooeyFedEvent;
import org.terasology.gookeeper.event.BreedGooeyEvent;
import org.terasology.gookeeper.event.FeedGooeyEvent;
import org.terasology.gookeeper.ui.GooeyActivateScreen;
import org.terasology.logic.behavior.BehaviorComponent;
import org.terasology.logic.behavior.asset.BehaviorTree;
import org.terasology.logic.characters.CharacterHeldItemComponent;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.logic.delay.DelayManager;
import org.terasology.logic.delay.DelayedActionTriggeredEvent;
import org.terasology.logic.delay.PeriodicActionTriggeredEvent;
import org.terasology.logic.health.DoDestroyEvent;
import org.terasology.logic.health.EngineDamageTypes;
import org.terasology.logic.health.HealthComponent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.network.ClientComponent;
import org.terasology.physics.Physics;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.layers.ingame.inventory.GetItemTooltip;
import org.terasology.rendering.nui.widgets.TooltipLine;
import org.terasology.utilities.Assets;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.worldlyTooltipAPI.events.GetTooltipIconEvent;
import org.terasology.worldlyTooltipAPI.events.GetTooltipNameEvent;

@RegisterSystem(RegisterMode.AUTHORITY)
@Share(value = BreedingSystem.class)
public class BreedingSystem extends BaseComponentSystem {

    @In
    private LocalPlayer localPlayer;

    @In
    private AssetManager assetManager;

    @In
    private NUIManager nuiManager;

    private static final Logger logger = LoggerFactory.getLogger(BreedingSystem.class);
    private Random random = new FastRandom();

    /**
     * Receives the BreedGooeyEvent when the "activated" gooey is chosen for breeding.
     *
     * @param event
     * @param gooeyEntity
     * @param gooeyComponent
     */
    @ReceiveEvent
    public void onBreedingGooey(BreedGooeyEvent event, EntityRef gooeyEntity, GooeyComponent gooeyComponent, HungerComponent hungerComponent) {
        CharacterHeldItemComponent characterHeldItemComponent = event.getInstigator().getComponent(CharacterHeldItemComponent.class);

        if (characterHeldItemComponent != null && characterHeldItemComponent.selectedItem.getComponent(DisplayNameComponent.class) != null) {
            EntityRef item = characterHeldItemComponent.selectedItem;
            String itemName = item.getComponent(DisplayNameComponent.class).name;

            if (!itemName.isEmpty() && hungerComponent.food.contains(itemName)) {
                logger.info("Selected for breeding ...");
                FollowComponent followComponent = gooeyEntity.getComponent(FollowComponent.class);
                BehaviorComponent behaviorComponent = gooeyEntity.getComponent(BehaviorComponent.class);
                MatingComponent matingComponent = new MatingComponent();

                followComponent.entityToFollow = event.getInstigator();
                behaviorComponent.tree = assetManager.getAsset("GooKeeper:breedingBehavior", BehaviorTree.class).get();
                CharacterMovementComponent characterMovementComponent = gooeyEntity.getComponent(CharacterMovementComponent.class);
                characterMovementComponent.jumpSpeed = 12f;

                gooeyEntity.saveComponent(followComponent);
                gooeyEntity.saveComponent(behaviorComponent);
                gooeyEntity.addOrSaveComponent(matingComponent);
                gooeyEntity.saveComponent(characterMovementComponent);
            }
        }
        nuiManager.closeScreen("GooKeeper:gooeyActivateScreen");
    }
}