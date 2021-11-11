// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.gookeeper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.terasology.engine.logic.behavior.asset.BehaviorTree;
import org.terasology.engine.registry.In;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.moduletestingenvironment.MTEExtension;
import org.terasology.moduletestingenvironment.extension.Dependencies;

import java.util.stream.Stream;

@Tag("Mte")
@ExtendWith(MTEExtension.class)
@Dependencies({"ModuleTestingEnvironment", "GooKeeper"})
@Disabled("GooKeeper don't support server side and MTE")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BehaviorsLoadingTest {

    @In
    AssetManager assetManager;

    public Stream<Arguments> behaviors() {
        return assetManager.getAvailableAssets(BehaviorTree.class)
                .stream()
                .map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("behaviors")
    void behaviorsTest(ResourceUrn urn) {
        Assertions.assertDoesNotThrow(() ->
                assetManager.getAsset(urn, BehaviorTree.class));
    }
}
