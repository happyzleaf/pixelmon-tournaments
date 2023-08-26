package com.happyzleaf.tournaments;

import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Text extends StringTextComponent {
	public static final Text WIP = of(TextFormatting.RED, "WIP");
	public static final Text ERROR = of(TextFormatting.RED, "ERROR");

	public Text(Object... texts) {
		super(Arrays.stream(texts).filter(Objects::nonNull).map(o -> {
			if (o instanceof Text) {
				return ((Text) o).getText();
			}

			return o.toString();
		}).collect(Collectors.joining()));
	}

	public String toPlain() {
		return getText();
	}

	public String serialize() {
		return getText().replace("\u00A7", "&");
	}

	public static Builder builder() {
		return new Builder();
	}

	public static Text of(Object... texts) {
		return new Text(texts);
	}

	public static Text deserialize(String text) {
		return new Text(text.replace("&", "\u00A7"));
	}

	public static class Builder {
		private final List<Object> texts = new ArrayList<>();

		public Builder append(Text text) {
			this.texts.add(text.getText());
			return this;
		}

		public Text build() {
			return new Text(texts.toArray());
		}
	}
}
