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
package org.terasology.gookeeper.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.gookeeper.component.*;
import org.terasology.gookeeper.event.LeaveVisitBlockEvent;
import org.terasology.gookeeper.interfaces.EconomyManager;
import org.terasology.logic.characters.events.OnEnterBlockEvent;
import org.terasology.logic.delay.DelayManager;
import org.terasology.logic.delay.PeriodicActionTriggeredEvent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.Direction;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.minion.move.MinionMoveComponent;
import org.terasology.physics.Physics;
import org.terasology.registry.In;
import org.terasology.utilities.Assets;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.items.OnBlockItemPlaced;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RegisterSystem(RegisterMode.AUTHORITY)
public class VisitorSystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    @In
    private WorldProvider worldProvider;

    @In
    private Physics physicsRenderer;

    @In
    private BlockEntityRegistry blockEntityRegistry;

    @In
    private EntityManager entityManager;

    @In
    private DelayManager delayManager;

    @In
    private InventoryManager inventoryManager;

    @In
    private Time time;

    @In
    private LocalPlayer localPlayer;

    @In
    private PrefabManager prefabManager;

    @In
    private EconomyManager economySystem;

    private static final String delayEventId = "VISITOR_SPAWN_DELAY";
    private static final Logger logger = LoggerFactory.getLogger(VisitorSystem.class);
    private static final Optional<Prefab> visitorPrefab = Assets.getPrefab("visitor");
    private Random random = new FastRandom();

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void update(float delta) {
        for (EntityRef visitor : entityManager.getEntitiesWith(VisitorComponent.class)) {
            VisitorComponent visitorComponent = visitor.getComponent(VisitorComponent.class);

            if (visitorComponent.pensToVisit.isEmpty()) {
                economySystem.payEntranceFee(visitor);

                int cutoffRNG = random.nextInt(0, 10);

                for (EntityRef visitBlock : entityManager.getEntitiesWith(VisitBlockComponent.class, LocationComponent.class)) {
                    VisitBlockComponent visitBlockComponent = visitBlock.getComponent(VisitBlockComponent.class);

                    if (visitBlockComponent.cutoffFactor <= cutoffRNG) {
                        visitorComponent.pensToVisit.add(visitBlock);
                    }
                }

                for (EntityRef exitBlock : entityManager.getEntitiesWith(VisitorExitComponent.class, LocationComponent.class)) {
                    visitorComponent.pensToVisit.add(exitBlock);
                }

                visitor.saveComponent(visitorComponent);
            }
        }
    }

    /**
     * Receives OnBlockItemPlaced event that is sent when a visit block is placed and hence updates the attribute value
     * of the corresponding VisitorBlockComponent
     *
     * @param event,entity   The OnBlockItemPlaced event
     */
    @ReceiveEvent
    public void onBlockPlaced(OnBlockItemPlaced event, EntityRef entity) {
        BlockComponent blockComponent = event.getPlacedBlock().getComponent(BlockComponent.class);
        VisitBlockComponent visitBlockComponent = event.getPlacedBlock().getComponent(VisitBlockComponent.class);
        VisitorEntranceComponent visitorEntranceComponent = event.getPlacedBlock().getComponent(VisitorEntranceComponent.class);

        if (blockComponent != null && visitBlockComponent != null) {
            Vector3i targetBlock = blockComponent.getPosition();
            EntityRef pen = getClosestPen(new Vector3f(targetBlock.x, targetBlock.y, targetBlock.z));

            if (pen != EntityRef.NULL) {
                visitBlockComponent.type = pen.getComponent(PenBlockComponent.class).type;
                visitBlockComponent.cutoffFactor = pen.getComponent(PenBlockComponent.class).cutoffFactor;

                event.getPlacedBlock().saveComponent(visitBlockComponent);
            }
        } else if (blockComponent != null && visitorEntranceComponent != null) {
            delayManager.addPeriodicAction(event.getPlacedBlock(), delayEventId, visitorEntranceComponent.initialDelay, visitorEntranceComponent.visitorSpawnRate);
        }
    }

    private EntityRef getClosestPen(Vector3f location) {
        EntityRef closestPen = EntityRef.NULL;
        float minDistance = 100f;

        for (EntityRef pen : entityManager.getEntitiesWith(PenBlockComponent.class, LocationComponent.class)) {
            BlockComponent blockComponent = pen.getComponent(BlockComponent.class);

            Vector3f blockPos = new Vector3f(blockComponent.getPosition().x, blockComponent.getPosition().y, blockComponent.getPosition().z);
            if (Vector3f.distance(blockPos, location) < minDistance) {
                minDistance = Vector3f.distance(blockPos, location);
                closestPen = pen;
            }
        }

        return closestPen;
    }

//    private List<EntityRef> getPenBlocks(EntityRef penBlock) {
//        List<EntityRef> neighbours = new ArrayList<>();
//
//
//    }

    /**
     * Receives LeaveVisitBlockEvent sent to a visitor entity when it leaves a visit block, and moves towards the next.
     * This is used to add credits to the players wallet depending on the type of gooeys associated with this visit block,
     * hence rarer the gooey, more would be the pay off.
     *
     * @param event,entity   The LeaveVisitBlockEvent event and the visitor entity to which it is sent
     */
    @ReceiveEvent
    public void onLeaveVisitBlock(LeaveVisitBlockEvent event, EntityRef entityRef) {
        EntityRef blockEntity = event.getVisitBlock();
        EntityRef visitor = event.getVisitor();

        MinionMoveComponent minionMoveComponent = event.getVisitor().getComponent(MinionMoveComponent.class);

        if (blockEntity.hasComponent(VisitBlockComponent.class) && visitor.hasComponent(VisitorComponent.class) && minionMoveComponent != null) {
            VisitorComponent visitorComponent = visitor.getComponent(VisitorComponent.class);
            Vector3f blockPos = blockEntity.getComponent(LocationComponent.class).getWorldPosition();
            if (visitorComponent.pensToVisit.contains(blockEntity) && Vector3f.distance(minionMoveComponent.target, blockPos) <= 1f) {
                economySystem.payVisitFee(visitor, blockEntity);
                visitorComponent.pensToVisit.remove(blockEntity);
            }

            visitor.saveComponent(visitorComponent);
        }
    }

    /**
     * Receives PeriodicActionTriggeredEvent sent to a visitor entrance block entity hence triggering the periodic visitor spawning
     *
     * @param event,entity   The PeriodicActionTriggeredEvent event and the visitor entrance block entity to which it is sent
     */
    @ReceiveEvent(components = {VisitorEntranceComponent.class})
    public void onPeriodicAction(PeriodicActionTriggeredEvent event, EntityRef entityRef) {
        LocationComponent locationComponent = entityRef.getComponent(LocationComponent.class);
        Vector3f blockPos = locationComponent.getWorldPosition().addY(1f);

        Vector3f spawnPos = blockPos;
        Vector3f dir = new Vector3f(locationComponent.getWorldDirection());
        dir.y = 0;
        if (dir.lengthSquared() > 0.001f) {
            dir.normalize();
        } else {
            dir.set(Direction.FORWARD.getVector3f());
        }
        Quat4f rotation = Quat4f.shortestArcQuat(Direction.FORWARD.getVector3f(), dir);

        if (visitorPrefab.isPresent() && visitorPrefab.get().getComponent(LocationComponent.class) != null) {
            entityManager.create(visitorPrefab.get(), spawnPos, rotation);
        }
    }
}
