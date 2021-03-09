// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gookeeper.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.management.AssetManager;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnAddedComponent;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.prefab.PrefabManager;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.characters.CharacterHeldItemComponent;
import org.terasology.engine.logic.common.ActivateEvent;
import org.terasology.engine.logic.common.DisplayNameComponent;
import org.terasology.engine.logic.delay.DelayManager;
import org.terasology.engine.logic.delay.DelayedActionTriggeredEvent;
import org.terasology.engine.logic.delay.PeriodicActionTriggeredEvent;
import org.terasology.engine.logic.health.DoDestroyEvent;
import org.terasology.engine.logic.health.EngineDamageTypes;
import org.terasology.engine.logic.health.HealthComponent;
import org.terasology.engine.logic.inventory.InventoryManager;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.registry.In;
import org.terasology.engine.registry.Share;
import org.terasology.engine.rendering.assets.texture.TextureRegionAsset;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.engine.rendering.nui.layers.ingame.inventory.GetItemTooltip;
import org.terasology.engine.utilities.Assets;
import org.terasology.gookeeper.Constants;
import org.terasology.gookeeper.component.GooeyComponent;
import org.terasology.gookeeper.component.HungerComponent;
import org.terasology.gookeeper.event.AfterGooeyFedEvent;
import org.terasology.gookeeper.event.FeedGooeyEvent;
import org.terasology.gookeeper.ui.GooeyActivateScreen;
import org.terasology.nui.widgets.TooltipLine;
import org.terasology.worldlyTooltipAPI.events.GetTooltipIconEvent;
import org.terasology.worldlyTooltipAPI.events.GetTooltipNameEvent;

import java.util.Optional;

@RegisterSystem(RegisterMode.AUTHORITY)
@Share(value = HungerSystem.class)
public class HungerSystem extends BaseComponentSystem {

    @In
    private EntityManager entityManager;

    @In
    private LocalPlayer localPlayer;

    @In
    private InventoryManager inventoryManager;

    @In
    private AssetManager assetManager;

    @In
    private PrefabManager prefabManager;

    @In
    private DelayManager delayManager;

    @In
    private NUIManager nuiManager;

    private static final Logger logger = LoggerFactory.getLogger(HungerSystem.class);

    /**
     * Adds health degradation action for the already captured gooey entities.
     *
     * @param event,entity,gooeyComponent,hungerComponent
     */
    @ReceiveEvent
    public void onGooeyActivated(OnAddedComponent event, EntityRef entity, GooeyComponent gooeyComponent, HungerComponent hungerComponent) {
        if (gooeyComponent.isCaptured) {
            delayManager.addPeriodicAction(entity, Constants.healthDecreaseEventID, hungerComponent.timeBeforeHungry, hungerComponent.healthDecreaseInterval);
            delayManager.addDelayedAction(entity, Constants.gooeyDeathEventID, gooeyComponent.lifeTime);
        }
    }

    /**
     * Receives ActivateEvent when the targeted gooey is 'activated' and then provides an interactive screen to the player.
     *
     * @param event,entity,gooeyComponent   The ActivateEvent, the gooey entity, the GooeyComponent of the corresponding entity
     */
    @ReceiveEvent
    public void onGooeyActivated(ActivateEvent event, EntityRef gooeyEntity, GooeyComponent gooeyComponent) {
        if (!nuiManager.isOpen("GooKeeper:gooeyActivateScreen") && gooeyComponent.isCaptured) {
            GooeyActivateScreen gooeyActivateScreen = nuiManager.pushScreen("GooKeeper:gooeyActivateScreen", GooeyActivateScreen.class);
            gooeyActivateScreen.setGooeyEntity(gooeyEntity);
            gooeyActivateScreen.setBreederEntity(event.getInstigator());
        } else {
            nuiManager.closeScreen("GooKeeper:gooeyActivateScreen");
        }
    }

    /**
     * Receives FeedGooeyEvent when the "activated" gooey entity is chose to be fed.
     *
     * @param event
     * @param gooeyEntity
     * @param gooeyComponent
     */
    @ReceiveEvent
    public void onFeedingGooey(FeedGooeyEvent event, EntityRef gooeyEntity, GooeyComponent gooeyComponent, HungerComponent hungerComponent) {
        HealthComponent healthComponent = gooeyEntity.getComponent(HealthComponent.class);
        CharacterHeldItemComponent characterHeldItemComponent = event.getInstigator().getComponent(CharacterHeldItemComponent.class);

        if (characterHeldItemComponent != null && characterHeldItemComponent.selectedItem.getComponent(DisplayNameComponent.class) != null) {
            EntityRef item = characterHeldItemComponent.selectedItem;
            String itemName = item.getComponent(DisplayNameComponent.class).name;

            if (!itemName.isEmpty() && hungerComponent.food.contains(itemName)) {
                logger.info("Gooey Health: " + healthComponent.currentHealth);

                delayManager.cancelPeriodicAction(gooeyEntity, Constants.healthDecreaseEventID);
                gooeyEntity.send(new AfterGooeyFedEvent(event.getInstigator(), gooeyEntity, item));
            }
        }

        nuiManager.closeScreen("GooKeeper:gooeyActivateScreen");
    }

    /**
     * Receives AfterGooeyFedEvent when the targeted gooey is fed the held food block and hence resets the health to max.
     *
     * @param event,entity   The AfterGooeyFedEvent, the gooey entity
     */
    @ReceiveEvent(components = {GooeyComponent.class})
    public void onGooeyFed(AfterGooeyFedEvent event, EntityRef entityRef) {
        HealthComponent healthComponent = entityRef.getComponent(HealthComponent.class);
        HungerComponent hungerComponent = entityRef.getComponent(HungerComponent.class);

        healthComponent.currentHealth = healthComponent.maxHealth;
        delayManager.addPeriodicAction(entityRef, Constants.healthDecreaseEventID, hungerComponent.timeBeforeHungry, hungerComponent.healthDecreaseInterval);

        event.getItem().destroy();
        entityRef.saveComponent(healthComponent);
    }

    /**
     * Receives PeriodicActionTriggeredEvent when the gooey entity's health is decreased
     *
     * @param event,entity   The PeriodicActionTriggeredEvent, the gooey entity
     */
    @ReceiveEvent(components = {GooeyComponent.class})
    public void onGooeyHealthDecrease(PeriodicActionTriggeredEvent event, EntityRef entity) {
        if (event.getActionId().equals(Constants.healthDecreaseEventID)) {
            HealthComponent healthComponent = entity.getComponent(HealthComponent.class);
            HungerComponent hungerComponent = entity.getComponent(HungerComponent.class);

            healthComponent.currentHealth -= hungerComponent.healthDecreaseAmount;
            entity.saveComponent(healthComponent);

            if (healthComponent.currentHealth <= 0) {
                entity.send(new DoDestroyEvent(EntityRef.NULL, EntityRef.NULL, EngineDamageTypes.PHYSICAL.get()));
            }
        }
    }


    /**
     * Receives DelayedActionTriggeredEvent when the gooey entity's life span terminates
     *
     * @param event,entity   The DelayedActionTriggeredEvent, the gooey entity
     */
    @ReceiveEvent(components = {GooeyComponent.class})
    public void onGooeyDestroy(DelayedActionTriggeredEvent event, EntityRef entity) {
        if (event.getActionId().equals(Constants.gooeyDeathEventID)) {
            entity.send(new DoDestroyEvent(EntityRef.NULL, EntityRef.NULL, EngineDamageTypes.PHYSICAL.get()));
        }
    }

    @ReceiveEvent
    public void addAttributesToTooltip(GetItemTooltip event, EntityRef entity, GooeyComponent gooeyComponent, HungerComponent hungerComponent) {
        for (String food : hungerComponent.food) {
            event.getTooltipLines().add(new TooltipLine("Can eat : " + food));
        }
        event.getTooltipLines().add(new TooltipLine("PlazMaster frequency required: " + gooeyComponent.stunFrequency));
    }

    @ReceiveEvent(components = GooeyComponent.class)
    public void setIcon(GetTooltipIconEvent event, EntityRef entityRef) {
        Optional<TextureRegionAsset> textureRegion = Assets.getTextureRegion("GooKeeper:"+ entityRef.getComponent(DisplayNameComponent.class).name + "Tooltip");
        if (textureRegion.isPresent()) {
            event.setIcon(textureRegion.get());
        }
    }

    @ReceiveEvent(components = {GooeyComponent.class})
    public void setName(GetTooltipNameEvent event, EntityRef entityRef) {
        event.setName(entityRef.getComponent(DisplayNameComponent.class).name);
    }
}
