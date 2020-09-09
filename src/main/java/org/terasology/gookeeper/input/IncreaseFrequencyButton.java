// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gookeeper.input;

import org.lwjgl.input.Keyboard;
import org.terasology.engine.input.BindButtonEvent;
import org.terasology.engine.input.DefaultBinding;
import org.terasology.engine.input.RegisterBindButton;
import org.terasology.nui.input.InputType;

/**
 *
 */
@RegisterBindButton(id = "freq_increase", description = "Increase PlazMaster's frequency")
@DefaultBinding(type = InputType.KEY, id = Keyboard.KEY_SEMICOLON)
public class IncreaseFrequencyButton extends BindButtonEvent {
}
