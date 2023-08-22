package com.happyzleaf.tournaments;

import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.Arrays;
import java.util.stream.Collectors;

public class Text extends StringTextComponent {
	public static final Text WIP = of(TextFormatting.RED, "WIP");

	public Text(Object... texts) {
		super(Arrays.stream(texts).map(o -> (String) o).collect(Collectors.joining()));
	}

	public static Text of(Object... texts) {
		return new Text(texts);
	}
}
