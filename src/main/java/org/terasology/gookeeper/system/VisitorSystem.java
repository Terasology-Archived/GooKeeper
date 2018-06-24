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
import org.terasology.fences.ConnectsToFencesComponent;
import org.terasology.gookeeper.component.*;
import org.terasology.gookeeper.event.LeaveVisitBlockEvent;
import org.terasology.gookeeper.interfaces.EconomyManager;
import org.terasology.logic.characters.events.OnEnterBlockEvent;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.delay.DelayManager;
import org.terasology.logic.delay.PeriodicActionTriggeredEvent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.location.Location;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.Direction;
import org.terasology.math.Side;
import org.terasology.math.SideBitFlag;
import org.terasology.math.TeraMath;
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
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.family.MultiConnectFamily;
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
    private BlockManager blockManager;

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
    private static int penIdCounter = 1;
    private static final Optional<Prefab> visitorPrefab = Assets.getPrefab("visitor");
    private Random random = new FastRandom();
    private static int numOfPenBlocks = 0;

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
            Vector3f targetBlock = blockComponent.getPosition().toVector3f();
            EntityRef pen = getClosestPen(targetBlock);

            if (pen != EntityRef.NULL) {
                PenBlockComponent penBlockComponent = pen.getComponent(PenBlockComponent.class);

                visitBlockComponent.type = penBlockComponent.type;
                visitBlockComponent.cutoffFactor = penBlockComponent.cutoffFactor;
                visitBlockComponent.penNumber = penIdCounter;
                penBlockComponent.penNumber = penIdCounter;

                pen.saveComponent(penBlockComponent);
                event.getPlacedBlock().saveComponent(visitBlockComponent);
                setNeighbouringBlocksID(pen);

                penIdCounter++;
            }
        } else if (blockComponent != null && visitorEntranceComponent != null) {
            delayManager.addPeriodicAction(event.getPlacedBlock(), delayEventId, visitorEntranceComponent.initialDelay, visitorEntranceComponent.visitorSpawnRate);
        }
    }

    private EntityRef getClosestPen(Vector3f location) {
        EntityRef closestPen = EntityRef.NULL;
        float minDistance = 100f;

        for (EntityRef pen : entityManager.getEntitiesWith(PenBlockComponent.class, BlockComponent.class)) {
            BlockComponent blockComponent = pen.getComponent(BlockComponent.class);

            Vector3f blockPos = blockComponent.getPosition().toVector3f();
            if (Vector3f.distance(blockPos, location) < minDistance) {
                minDistance = Vector3f.distance(blockPos, location);
                closestPen = pen;
            }
        }

        return closestPen;
    }

    private void setNeighbouringBlocksID(EntityRef penBlock) {
        BlockComponent blockComponent = penBlock.getComponent(BlockComponent.class);
        PenBlockComponent penBlockComponent = penBlock.getComponent(PenBlockComponent.class);

        if (penBlockComponent != null && !penBlockComponent.penIDSet) {

            Byte sides = SideBitFlag.getSides(Side.LEFT, Side.RIGHT,Side.FRONT,Side.BACK);

            for (Side side : SideBitFlag.getSides(sides)) {
                Vector3i neighborLocation = new Vector3i(blockComponent.getPosition());
                neighborLocation.add(side.getVector3i());

                EntityRef neighborEntity = blockEntityRegistry.getEntityAt(neighborLocation);
                BlockComponent blockComponent1 = neighborEntity.getComponent(BlockComponent.class);

                if (blockComponent1 != null && neighborEntity.hasComponent(ConnectsToFencesComponent.class) && neighborEntity.hasComponent(PenBlockComponent.class)) {
                    PenBlockComponent penBlockComponent1 = neighborEntity.getComponent(PenBlockComponent.class);
                    if (penBlockComponent1.type.equals(penBlockComponent.type)) {
                        penBlockComponent1.penNumber = penIdCounter;

                        neighborEntity.saveComponent(penBlockComponent1);
                    }
                    penBlockComponent.penIDSet = true;
                    penBlock.saveComponent(penBlockComponent);
                    setNeighbouringBlocksID(neighborEntity);
                }
            }
        }
    }

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

    /**
     * Receives ActivateEvent when the targeted pen block is activated, and prints the ID of the corresponding pen.
     *
     * @param event,entity   The ActivateEvent, the instigator entity
     */
    @ReceiveEvent
    public void onSlimePodActivate(ActivateEvent event, EntityRef entity) {
        PenBlockComponent penBlockComponent= event.getTarget().getComponent(PenBlockComponent.class);
        VisitBlockComponent visitBlockComponent = event.getTarget().getComponent(VisitBlockComponent.class);
        BlockComponent blockComponent = event.getTarget().getComponent(BlockComponent.class);

        if (blockComponent != null && penBlockComponent != null) {
            logger.info("Pen Type: " + penBlockComponent.type);
            logger.info("Pen ID: " + penBlockComponent.penNumber);
        } else if (blockComponent != null && visitBlockComponent != null) {
            logger.info("Visit Block Type: " + visitBlockComponent.type);
            logger.info("Visit Block ID: " + visitBlockComponent.penNumber);
            logger.info("Visit Block Gooey Count: " + visitBlockComponent.gooeyQuantity);
        }
    }
}
