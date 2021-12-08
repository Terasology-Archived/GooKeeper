// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gookeeper.input;

import org.terasology.engine.input.BindButtonEvent;
import org.terasology.engine.input.DefaultBinding;
import org.terasology.engine.input.RegisterBindButton;
import org.terasology.input.InputType;
import org.terasology.input.Keyboard;

@RegisterBindButton(id = "freq_increase", description = "Increase PlazMaster's frequency")
@DefaultBinding(type = InputType.KEY, id = Keyboard.KeyId.SEMICOLON)
public class IncreaseFrequencyButton extends BindButtonEvent {
}
