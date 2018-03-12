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
package system;

import com.google.common.collect.Lists;
import org.terasology.behaviors.components.FollowComponent;

import org.terasology.core.world.CoreBiome;
import org.terasology.creepers.component.GooeyComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.delay.DelayManager;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.logic.health.OnDamagedEvent;
import org.terasology.registry.In;
import org.terasology.utilities.Assets;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.biomes.Biome;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.event.OnChunkGenerated;

import java.util.Optional;

@RegisterSystem(RegisterMode.AUTHORITY)
public class GooeyUpdate extends BaseComponentSystem implements UpdateSubscriberSystem {
    @In
    private WorldProvider worldProvider;

    @In
    private BlockEntityRegistry blockEntityRegistry;

    @In
    private EntityManager entityManager;

    @In
    private BlockManager blockManager;

    @In
    private DelayManager delayManager;

    private Random random = new FastRandom();
    private Optional<Prefab> redGooeyPrefab = Assets.getPrefab("GooKeeper:redgooey");

    private Biome desertBiome = CoreBiome.DESERT;

    private int counter = 0;

    private  int SPAWN_CHANCE_IN_PERCENT = 2;
    private  int MAX_GOOEY_GROUP_SIZE = 2;

    @Override
    public void update (float delta) {
        for (EntityRef entity : entityManager.getEntitiesWith(GooeyComponent.class, FollowComponent.class)) {
            GooeyComponent gooeyComponent = entity.getComponent(GooeyComponent.class);

        }
    }

    @ReceiveEvent
    public void onChunkGenerated(OnChunkGenerated event, EntityRef worldEntity) {
        boolean trySpawn = SPAWN_CHANCE_IN_PERCENT > random.nextInt(100);
        if (!trySpawn) {
            return;
        }
        Vector3i chunkPos = event.getChunkPos();
        tryGooeySpawn(chunkPos);
    }

    /**
     * Attempts to spawn GOOEY on the specified chunk. The number of GOOEYs spawned will depend on probabiliy
     * configurations defined earlier.
     *
     * @param chunkPos   The chunk which the game will try to spawn GOOEYs on
     */
    private void tryGooeySpawn(Vector3i chunkPos) {
        if (isValidSpawnPosition(chunkPos) && counter < 2) {
            for (int i = 0; i < MAX_GOOEY_GROUP_SIZE; i++) {
                spawnGooey(chunkPos);
            }
            counter += 1;
        }
    }

    /**
     * Spawns the gooey at the location specified by the parameter.
     *
     * @param location   The location where the gooey is to be spawned
     */
    private void spawnGooey(Vector3i location) {
        Vector3f floatVectorLocation = location.toVector3f();
        Vector3f yAxis = new Vector3f(0, 1, 0);
        float randomAngle = (float) (random.nextFloat()*Math.PI*2);
        Quat4f rotation = new Quat4f(yAxis, randomAngle);
        entityManager.create(redGooeyPrefab.get(), floatVectorLocation, rotation);
    }

    /**
     * Check blocks at and around the target position and check if it's a valid spawning spot
     *
     * @param pos   The block to be checked if it's a valid spot for spawning
     * @return A boolean with the value of true if the block is a valid spot for spawing
     */
    private boolean isValidSpawnPosition(Vector3i pos) {
        Vector3i above = new Vector3i(pos.x, pos.y+1, pos.z);
        if (!worldProvider.getBlock(above).equals(BlockManager.AIR_ID)) {
            return false;
        }
        if (worldProvider.getBiome(pos).equals(desertBiome))
            return true;
        else
            return false;
    }

    @ReceiveEvent
    public void onDamage(OnDamagedEvent event, EntityRef entity) {
        return;
    }
}
