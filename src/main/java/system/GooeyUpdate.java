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

import org.terasology.audio.StaticSound;
import org.terasology.audio.events.PlaySoundEvent;
import org.terasology.behaviors.components.FollowComponent;

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
import org.terasology.logic.delay.DelayedActionComponent;
import org.terasology.logic.delay.DelayedActionTriggeredEvent;
import org.terasology.logic.health.OnDamagedEvent;
import org.terasology.logic.actions.ExplosionActionComponent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.registry.In;
import org.terasology.utilities.Assets;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.BlockManager;

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
    private String delayActionID = "DELAY_ACTION_ID";
    private Optional<Prefab> damageType = Assets.getPrefab("Creepers:Creeper");
    private Optional<StaticSound> fuseAudio = Assets.getSound("Creepers:creeperFuse");
    private Optional<StaticSound> explosionAudio = Assets.getSound("Creepers:creeperExplode");

    @Override
    public void update (float delta) {
        for (EntityRef entity : entityManager.getEntitiesWith(GooeyComponent.class, FollowComponent.class)) {
            GooeyComponent gooeyComponent = entity.getComponent(GooeyComponent.class);

        }
    }

    @ReceiveEvent
    public void onDamage(OnDamagedEvent event, EntityRef entity) {
        return;
    }

    @ReceiveEvent
    public void onDelayedExplosion(DelayedActionTriggeredEvent event, EntityRef entityRef,
                                   ExplosionActionComponent explosionComp) {
        FollowComponent followComponent = entityRef.getComponent(FollowComponent.class);
        Vector3f currentActorLocation = entityRef.getComponent(LocationComponent.class).getWorldPosition();
        Vector3f entityFollowingLocation = followComponent.entityToFollow.getComponent(LocationComponent.class).getWorldPosition();

        if (event.getActionId().equals(delayActionID)) {
            entityRef.destroy();
        }
    }
}
