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
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.gookeeper.Constants;
import org.terasology.gookeeper.component.GooeyComponent;
import org.terasology.gookeeper.component.BreedingBlockComponent;
import org.terasology.gookeeper.component.MatingComponent;
import org.terasology.gookeeper.component.AggressiveComponent;
import org.terasology.gookeeper.component.NeutralComponent;
import org.terasology.gookeeper.component.FriendlyComponent;
import org.terasology.gookeeper.component.FactorComponent;
import org.terasology.gookeeper.component.HungerComponent;
import org.terasology.gookeeper.event.AfterGooeyBreedingEvent;
import org.terasology.gookeeper.event.BeginBreedingEvent;
import org.terasology.gookeeper.event.SelectForBreedingEvent;
import org.terasology.logic.behavior.BehaviorComponent;
import org.terasology.logic.behavior.asset.BehaviorTree;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.logic.characters.StandComponent;
import org.terasology.logic.characters.WalkComponent;
import org.terasology.logic.characters.events.OnEnterBlockEvent;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.logic.delay.DelayManager;
import org.terasology.logic.delay.DelayedActionTriggeredEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.network.ColorComponent;
import org.terasology.physics.components.RigidBodyComponent;
import org.terasology.physics.components.TriggerComponent;
import org.terasology.physics.components.shapes.BoxShapeComponent;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.skeletalmesh.SkeletalMesh;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.logic.MeshComponent;
import org.terasology.rendering.logic.SkeletalMeshComponent;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.utilities.Assets;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;
import org.terasology.world.BlockEntityRegistry;

import java.math.RoundingMode;

@RegisterSystem(RegisterMode.AUTHORITY)
@Share(value = BreedingSystem.class)
public class BreedingSystem extends BaseComponentSystem {

    @In
    private BlockEntityRegistry blockEntityRegistry;

    @In
    private LocalPlayer localPlayer;

    @In
    private AssetManager assetManager;

    @In
    private NUIManager nuiManager;

    @In
    private EntityManager entityManager;

    @In
    private PrefabManager prefabManager;

    @In
    private DelayManager delayManager;

    private static final Logger logger = LoggerFactory.getLogger(BreedingSystem.class);
    private Random random = new FastRandom();

    /**
     * This method is called when a gooey steps on a breeding block
     *
     * @param event     The OnEnterBlockEvent
     * @param entity    the gooey entity
     */
    @ReceiveEvent
    public void onEnterBlock(OnEnterBlockEvent event, EntityRef entity, GooeyComponent gooeyComponent) {
        LocationComponent loc = entity.getComponent(LocationComponent.class);
        Vector3f pos = loc.getWorldPosition();
        pos.setY(pos.getY() -1);

        EntityRef blockEntity = blockEntityRegistry.getExistingBlockEntityAt(new Vector3i(pos, RoundingMode.HALF_UP));

        if (blockEntity.hasComponent(BreedingBlockComponent.class)) {
            FollowComponent followComponent = entity.getComponent(FollowComponent.class);
            BreedingBlockComponent breedingBlockComponent = blockEntity.getComponent(BreedingBlockComponent.class);

            if (followComponent.entityToFollow != EntityRef.NULL && breedingBlockComponent.parentGooey == EntityRef.NULL && gooeyComponent.isCaptured) {
                breedingBlockComponent.parentGooey = entity;
                blockEntity.saveComponent(breedingBlockComponent);
                entity.send(new SelectForBreedingEvent(followComponent.entityToFollow, entity));
            }
        }
    }

    /**
     * Receives the SelectForBreedingEvent when the "activated" gooey is chosen for breeding.
     *
     * @param event
     * @param gooeyEntity
     * @param gooeyComponent
     */
    @ReceiveEvent
    public void onBreedingGooey(SelectForBreedingEvent event, EntityRef gooeyEntity, GooeyComponent gooeyComponent) {
        logger.info("Selected for breeding...");

        MatingComponent matingComponent;

        if (!gooeyEntity.hasComponent(MatingComponent.class)) {
            matingComponent = new MatingComponent();
        } else {
            matingComponent = gooeyEntity.getComponent(MatingComponent.class);
        }

        matingComponent.selectedForMating = !matingComponent.selectedForMating;

        if (matingComponent.selectedForMating) {
            setMatingAttributes(gooeyEntity, matingComponent);
        }

        if (matingComponent.selectedForMating && matingComponent.matingWithEntity != EntityRef.NULL) {
            gooeyEntity.send(new BeginBreedingEvent(event.getInstigator(), gooeyEntity, matingComponent.matingWithEntity));
        }
    }

    /**
     * Receives the BeginBreedingEvent when the actual breeding process is to be initiated.
     *
     * @param event
     * @param gooeyEntity
     * @param gooeyComponent
     */
    @ReceiveEvent
    public void onBeginBreedingProcess(BeginBreedingEvent event, EntityRef gooeyEntity, GooeyComponent gooeyComponent) {
        delayManager.addDelayedAction(gooeyEntity, Constants.spawnGooeyEggEventID, random.nextLong(4000, 7000));
    }

    /**
     * Receives the AfterGooeyBreedingEvent when the breeding process is to be ended
     *
     * @param event
     * @param gooeyEntity
     * @param gooeyComponent
     * @param matingComponent
     */
    @ReceiveEvent
    public void onBreedingProcessEnd(AfterGooeyBreedingEvent event, EntityRef gooeyEntity, GooeyComponent gooeyComponent, MatingComponent matingComponent) {
        EntityRef offspringEntity = event.getOffspringGooey();

        SkeletalMeshComponent skeletalMeshComponent = offspringEntity.getComponent(SkeletalMeshComponent.class);
        Material gooeyMaterial = getOffspringMaterial(gooeyEntity, matingComponent.matingWithEntity, offspringEntity);

        if (gooeyMaterial != null) {
            skeletalMeshComponent.material = gooeyMaterial;
        }
        skeletalMeshComponent.mesh = null;
        offspringEntity.saveComponent(skeletalMeshComponent);

        DisplayNameComponent displayNameComponent = offspringEntity.getComponent(DisplayNameComponent.class);
        displayNameComponent.name = gooeyEntity.getComponent(DisplayNameComponent.class).name;
        offspringEntity.saveComponent(displayNameComponent);

        MatingComponent matingComponent1 = matingComponent.matingWithEntity.getComponent(MatingComponent.class);

        matingComponent.selectedForMating = false;
        matingComponent1.selectedForMating = false;

        CharacterMovementComponent characterMovementComponent = gooeyEntity.getComponent(CharacterMovementComponent.class);
        CharacterMovementComponent characterMovementComponent1 = matingComponent.matingWithEntity.getComponent(CharacterMovementComponent.class);

        if (characterMovementComponent != null) {
            characterMovementComponent.speedMultiplier = 1f;
            gooeyEntity.saveComponent(characterMovementComponent);
        }

        if (characterMovementComponent1 != null) {
            characterMovementComponent1.speedMultiplier = 1f;
            matingComponent.matingWithEntity.saveComponent(characterMovementComponent1);
        }
    }

    /**
     * Receives DelayedActionTriggeredEvent, which spawns the gooey egg
     *
     * @param event     The DelayedActionTriggeredEvent event
     * @param gooeyEntity    The entity to which the event is sent
     */
    @ReceiveEvent
    public void onDelayedAction(DelayedActionTriggeredEvent event, EntityRef gooeyEntity) {
        if (event.getActionId().equals(Constants.spawnGooeyEggEventID)) {
            spawnGooeyEgg(gooeyEntity);
        } else if (event.getActionId().equals(Constants.hatchEggEventID)) {
            hatchGooeyEgg(gooeyEntity);
        }
    }

    private Component getDominantComponent(AggressiveComponent aggressiveComponent, NeutralComponent neutralComponent, FriendlyComponent friendlyComponent) {
        if (aggressiveComponent.magnitude >= neutralComponent.magnitude && aggressiveComponent.magnitude >= friendlyComponent.magnitude) {
            return aggressiveComponent;
        } else if (neutralComponent.magnitude >= aggressiveComponent.magnitude && neutralComponent.magnitude >= friendlyComponent.magnitude) {
            return neutralComponent;
        } else {
            return friendlyComponent;
        }
    }

    private  <T extends FactorComponent> T getChildFactor(T childComponent, EntityRef parent1, EntityRef parent2) {
        FactorComponent source1 = parent1.getComponent(childComponent.getClass());
        FactorComponent source2 = parent2.getComponent(childComponent.getClass());

        childComponent.magnitude = 0f;

        if (source1 != null) {
            childComponent.magnitude += source1.magnitude;
        }

        if (source2 != null) {
            childComponent.magnitude += source2.magnitude;
        }

        childComponent.magnitude /= 2f;
        return childComponent;
    }

    private Material getOffspringMaterial(EntityRef parent1, EntityRef parent2, EntityRef offspringEntity) {
        Material offspringMaterial;

        Material parent1Mat = parent1.getComponent(SkeletalMeshComponent.class).material;
        Material parent2Mat = parent2.getComponent(SkeletalMeshComponent.class).material;

        ColorComponent parent1Color = parent1.getComponent(ColorComponent.class);
        ColorComponent parent2Color = parent2.getComponent(ColorComponent.class);
        ColorComponent offspringGooeyColor = new ColorComponent();

        if (parent1Mat == parent2Mat) {
            return parent1Mat;
        }

        int redValue = parent1Color.color.r() + parent2Color.color.r();
        int blueValue = parent1Color.color.b() + parent2Color.color.b();
        int yellowValue = parent1Color.color.g() + parent2Color.color.g();

        offspringGooeyColor.color = new Color(redValue, yellowValue, blueValue);
        String materialName = "";
        if (redValue > 0f) {
            materialName += ((int)(Math.ceil(offspringGooeyColor.color.r()/128))) + "r";
        }
        if (yellowValue > 0f) {
            materialName += ((int)(Math.ceil(offspringGooeyColor.color.g()/128))) + "y";
        }
        if (blueValue > 0f) {
            materialName += ((int)(Math.ceil(offspringGooeyColor.color.b()/128))) + "b";
        }

        offspringEntity.addOrSaveComponent(offspringGooeyColor);
        offspringMaterial = Assets.getMaterial(materialName).orElse(null);

        return offspringMaterial;
    }

    private void setMatingAttributes(EntityRef gooeyEntity, MatingComponent matingComponent) {
        for (EntityRef breedingBlock : entityManager.getEntitiesWith(BreedingBlockComponent.class)) {
            //if (breedingBlock.getOwner().equals(event.getInstigator())) {
            BreedingBlockComponent breedingBlockComponent = breedingBlock.getComponent(BreedingBlockComponent.class);

            if (breedingBlockComponent.parentGooey != EntityRef.NULL && !breedingBlockComponent.parentGooey.equals(gooeyEntity)) {
                EntityRef matingWithGooey = breedingBlockComponent.parentGooey;
                MatingComponent matingComponent1;

                matingComponent.matingWithEntity = matingWithGooey;

                if (!matingWithGooey.hasComponent(MatingComponent.class)) {
                    matingComponent1 = new MatingComponent();
                } else {
                    matingComponent1 = matingWithGooey.getComponent(MatingComponent.class);
                }

                matingComponent1.selectedForMating = true;
                matingComponent1.matingWithEntity = gooeyEntity;

                matingWithGooey.addOrSaveComponent(matingComponent1);
                breedingBlockComponent.parentGooey = EntityRef.NULL;
                breedingBlock.saveComponent(breedingBlockComponent);
                break;
            }
            //}
        }

        if (matingComponent.matingWithEntity != EntityRef.NULL) {
            for (EntityRef breedingBlock : entityManager.getEntitiesWith(BreedingBlockComponent.class)) {
                BreedingBlockComponent breedingBlockComponent1 = breedingBlock.getComponent(BreedingBlockComponent.class);

                if (breedingBlockComponent1.parentGooey.equals(gooeyEntity)) {
                    breedingBlockComponent1.parentGooey = EntityRef.NULL;
                    breedingBlock.saveComponent(breedingBlockComponent1);
                    break;
                }
            }
        }

        gooeyEntity.addOrSaveComponent(matingComponent);
    }

    private void spawnGooeyEgg(EntityRef gooeyEntity) {
        Prefab eggPrefab = prefabManager.getPrefab("GooKeeper:gooeyEgg");
        Prefab offpsringPrefab = prefabManager.getPrefab("GooKeeper:gooeyHatchling");

        EntityBuilder entityBuilder = entityManager.newBuilder(eggPrefab);
        EntityBuilder entityBuilder1 = entityManager.newBuilder(offpsringPrefab);

        LocationComponent locationComponent = entityBuilder.getComponent(LocationComponent.class);
        LocationComponent locationComponent1 = entityBuilder1.getComponent(LocationComponent.class);

        Vector3f parent1Location = gooeyEntity.getComponent(LocationComponent.class).getWorldPosition();
        Vector3f parent2Location = gooeyEntity.getComponent(MatingComponent.class).matingWithEntity.getComponent(LocationComponent.class).getWorldPosition();

        Vector3f middleLocation = new Vector3f((parent1Location.x + parent2Location.x)/2f, parent1Location.y + 1f, (parent2Location.z + parent2Location.z)/2f);
        locationComponent.setWorldPosition(middleLocation);
        locationComponent1.setWorldPosition(middleLocation);
        locationComponent.setWorldScale(0.5f);

        entityBuilder.saveComponent(locationComponent);
        entityBuilder1.saveComponent(locationComponent1);

        AggressiveComponent aggressiveComponent = new AggressiveComponent();
        FriendlyComponent friendlyComponent = new FriendlyComponent();
        NeutralComponent neutralComponent = new NeutralComponent();

        MatingComponent matingComponent = gooeyEntity.getComponent(MatingComponent.class);

        aggressiveComponent = getChildFactor(aggressiveComponent, gooeyEntity, matingComponent.matingWithEntity);
        neutralComponent = getChildFactor(neutralComponent, gooeyEntity, matingComponent.matingWithEntity);
        friendlyComponent = getChildFactor(friendlyComponent, gooeyEntity, matingComponent.matingWithEntity);

        Component dominantComponent = getDominantComponent(aggressiveComponent, neutralComponent, friendlyComponent);

        entityBuilder1.addOrSaveComponent(dominantComponent);
        entityBuilder1.addOrSaveComponent(gooeyEntity.getComponent(HungerComponent.class));

        GooeyComponent offspringGooeyComponent = gooeyEntity.getComponent(GooeyComponent.class);
        offspringGooeyComponent.isCaptured = false;
        entityBuilder1.addOrSaveComponent(offspringGooeyComponent);

        DisplayNameComponent offspringNameComponent = gooeyEntity.getComponent(DisplayNameComponent.class);
        offspringNameComponent.name = gooeyEntity.getComponent(DisplayNameComponent.class).name;
        entityBuilder1.saveComponent(offspringNameComponent);

        EntityRef eggEntity = entityBuilder.build();
        EntityRef offspringGooey = entityBuilder1.build();
        gooeyEntity.send(new AfterGooeyBreedingEvent(gooeyEntity, matingComponent.matingWithEntity, offspringGooey));

        long timeToHatch = random.nextLong(2000, 6000);
        delayManager.addDelayedAction(offspringGooey, Constants.hatchEggEventID, timeToHatch);
        delayManager.addDelayedAction(eggEntity, Constants.hatchEggEventID, timeToHatch);
    }

    private void hatchGooeyEgg(EntityRef gooeyEntity) {
        /*
            During this event, the gooeyEntity attribute is the newly generated gooey offspring,
            This is where we "hatch" the egg to give the entity the necessary components.
         */
        if (!gooeyEntity.hasComponent(GooeyComponent.class)) {
            /* This means that the entity here is the egg prefab. */
            gooeyEntity.destroy();
            return;
        }
        Prefab referenceGooeyPrefab = prefabManager.getPrefab("GooKeeper:blueGooey");

        LocationComponent locationComponent = gooeyEntity.getComponent(LocationComponent.class);
        locationComponent.setWorldScale(1f);
        gooeyEntity.saveComponent(locationComponent);

        BehaviorComponent behaviorComponent = new BehaviorComponent();
        behaviorComponent.tree = Assets.get("GooKeeper:gooey", BehaviorTree.class).get();
        gooeyEntity.addComponent(behaviorComponent);

        FollowComponent followComponent = gooeyEntity.getComponent(FollowComponent.class);
        followComponent.entityToFollow = EntityRef.NULL;
        gooeyEntity.saveComponent(followComponent);

        BoxShapeComponent boxShapeComponent = referenceGooeyPrefab.getComponent(BoxShapeComponent.class);
        gooeyEntity.addComponent(boxShapeComponent);

        TriggerComponent triggerComponent = referenceGooeyPrefab.getComponent(TriggerComponent.class);
        gooeyEntity.addComponent(triggerComponent);

        SkeletalMeshComponent skeletalMeshComponent = gooeyEntity.getComponent(SkeletalMeshComponent.class);
        skeletalMeshComponent.mesh = referenceGooeyPrefab.getComponent(SkeletalMeshComponent.class).mesh;
        gooeyEntity.saveComponent(skeletalMeshComponent);
    }
}
