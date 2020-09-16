// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gookeeper.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.behaviors.minion.move.MinionMoveComponent;
import org.terasology.engine.core.Time;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.prefab.PrefabManager;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.engine.logic.common.ActivateEvent;
import org.terasology.engine.logic.delay.DelayManager;
import org.terasology.engine.logic.delay.PeriodicActionTriggeredEvent;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.math.Direction;
import org.terasology.engine.math.Side;
import org.terasology.engine.math.SideBitFlag;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.physics.Physics;
import org.terasology.engine.registry.In;
import org.terasology.engine.utilities.Assets;
import org.terasology.engine.utilities.random.FastRandom;
import org.terasology.engine.utilities.random.Random;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.BlockComponent;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.items.OnBlockItemPlaced;
import org.terasology.fences.ConnectsToFencesComponent;
import org.terasology.gookeeper.Constants;
import org.terasology.gookeeper.component.PenBlockComponent;
import org.terasology.gookeeper.component.VisitBlockComponent;
import org.terasology.gookeeper.component.VisitorComponent;
import org.terasology.gookeeper.component.VisitorEntranceComponent;
import org.terasology.gookeeper.component.VisitorExitComponent;
import org.terasology.gookeeper.event.LeaveVisitBlockEvent;
import org.terasology.gookeeper.interfaces.EconomyManager;
import org.terasology.inventory.logic.InventoryManager;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;

import java.util.Optional;

@RegisterSystem(RegisterMode.AUTHORITY)
public class VisitorSystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    private static final Logger logger = LoggerFactory.getLogger(VisitorSystem.class);
    private static final Optional<Prefab> visitorPrefab = Assets.getPrefab("visitor");
    private static int penIdCounter = 1;
    private final Random random = new FastRandom();
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
    @In
    private NetworkSystem networkSystem;

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

                for (EntityRef visitBlock : entityManager.getEntitiesWith(VisitBlockComponent.class,
                        LocationComponent.class)) {
                    VisitBlockComponent visitBlockComponent = visitBlock.getComponent(VisitBlockComponent.class);

                    if (visitBlockComponent.cutoffFactor <= cutoffRNG) {
                        visitorComponent.pensToVisit.add(visitBlock);
                    }
                }

                for (EntityRef exitBlock : entityManager.getEntitiesWith(VisitorExitComponent.class,
                        LocationComponent.class)) {
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
     * @param event The OnBlockItemPlaced event
     * @param entity The corresponding block entity
     */
    @ReceiveEvent
    public void onBlockPlaced(OnBlockItemPlaced event, EntityRef entity) {
        BlockComponent blockComponent = event.getPlacedBlock().getComponent(BlockComponent.class);
        VisitBlockComponent visitBlockComponent = event.getPlacedBlock().getComponent(VisitBlockComponent.class);
        VisitorEntranceComponent visitorEntranceComponent =
                event.getPlacedBlock().getComponent(VisitorEntranceComponent.class);

        if (blockComponent != null && visitBlockComponent != null) {
            Vector3f targetBlock = blockComponent.getPosition().toVector3f();
            EntityRef pen = getClosestPen(targetBlock);

            if (pen != EntityRef.NULL) {
                visitBlockComponent.owner = event.getInstigator();
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
            visitorEntranceComponent.owner = event.getInstigator();
            event.getPlacedBlock().saveComponent(visitorEntranceComponent);

            delayManager.addPeriodicAction(event.getPlacedBlock(), Constants.visitorSpawnDelayEventID,
                    visitorEntranceComponent.initialDelay, visitorEntranceComponent.visitorSpawnRate);
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

    /**
     * This method is used to set the penNumber ID to all the connected pen block entities of similar type
     *
     * @param penBlock
     */
    private void setNeighbouringBlocksID(EntityRef penBlock) {
        BlockComponent blockComponent = penBlock.getComponent(BlockComponent.class);
        PenBlockComponent penBlockComponent = penBlock.getComponent(PenBlockComponent.class);

        if (penBlockComponent != null && !penBlockComponent.penIDSet) {

            Byte sides = SideBitFlag.getSides(Side.LEFT, Side.RIGHT, Side.FRONT, Side.BACK);

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
     * This is used to add credits to the players wallet depending on the type of gooeys associated with this visit
     * block, hence rarer the gooey, more would be the pay off.
     *
     * @param event the LeaveVisitBlockEvent event
     * @param entityRef the visitor entity to which it is sent
     */
    @ReceiveEvent
    public void onLeaveVisitBlock(LeaveVisitBlockEvent event, EntityRef entityRef) {
        EntityRef blockEntity = event.getVisitBlock();
        EntityRef visitor = event.getVisitor();

        MinionMoveComponent minionMoveComponent = event.getVisitor().getComponent(MinionMoveComponent.class);

        if (blockEntity.hasComponent(VisitBlockComponent.class) && visitor.hasComponent(VisitorComponent.class) && minionMoveComponent != null) {
            VisitorComponent visitorComponent = visitor.getComponent(VisitorComponent.class);
            Vector3f blockPos = blockEntity.getComponent(LocationComponent.class).getWorldPosition();
            if (visitorComponent.pensToVisit.contains(blockEntity) && Vector3f.distance(minionMoveComponent.target,
                    blockPos) <= 1f) {
                economySystem.payVisitFee(visitor, blockEntity);
                visitorComponent.pensToVisit.remove(blockEntity);
            }

            visitor.saveComponent(visitorComponent);
        }
    }

    /**
     * Receives PeriodicActionTriggeredEvent sent to a visitor entrance block entity hence triggering the periodic
     * visitor spawning
     *
     * @param event the PeriodicActionTriggeredEvent event
     * @param entity the visitor entrance block entity to which it is sent
     */
    @ReceiveEvent(components = {VisitorEntranceComponent.class})
    public void onPeriodicAction(PeriodicActionTriggeredEvent event, EntityRef entityRef) {
        if (event.getActionId().equals(Constants.visitorSpawnDelayEventID)) {
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
                EntityRef visitor = entityManager.create(visitorPrefab.get(), spawnPos, rotation);
                VisitorComponent visitorComponent = visitor.getComponent(VisitorComponent.class);
                visitorComponent.visitorEntranceBlock = entityRef;
                visitor.saveComponent(visitorComponent);
            }
        }
    }

    /**
     * Receives ActivateEvent when the targeted pen block is activated, and prints the ID of the corresponding pen.
     *
     * @param event
     * @param entity
     */
    @ReceiveEvent
    public void onActivate(ActivateEvent event, EntityRef entity) {
        PenBlockComponent penBlockComponent = entity.getComponent(PenBlockComponent.class);
        VisitBlockComponent visitBlockComponent = entity.getComponent(VisitBlockComponent.class);
        BlockComponent blockComponent = entity.getComponent(BlockComponent.class);

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
