package com.happyzleaf.tournaments.text;

import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class Text extends StringTextComponent {
    public static final Text WIP = of(TextFormatting.RED, "WIP");
    public static final Text ERROR = of(TextFormatting.RED, "ERROR");

    public Text(Object... texts) {
//		super(Arrays.stream(texts).filter(Objects::nonNull).map(o -> {
//			if (o instanceof Text) {
//				return ((Text) o).getText();
//			}
//
//			return o.toString();
//		}).collect(Collectors.joining()));
        super("");
        for (Object text : texts) {
            if (text == null) continue;

            if (text instanceof ITextComponent) {
                appendSibling((ITextComponent) text);
            } else {
                appendString(text.toString());
            }
        }
    }

    public Text onClick(@Nullable PlayerEntity recipient, TextAction.Callback action) {
        String command = TextAction.register(recipient, action);
        ClickEvent event = new ClickEvent(ClickEvent.Action.RUN_COMMAND, command);
        modifyStyle(style -> style.setClickEvent(event));
        return this;
    }

    public Text onClick(@Nullable CommandSource recipient, TextAction.Callback action) {
        return onClick(recipient == null || !(recipient.getEntity() instanceof PlayerEntity) ? null : (PlayerEntity) recipient.getEntity(), action);
    }

    public Text onClick(TextAction.Callback action) {
        return onClick((PlayerEntity) null, action);
    }

    public Text onHover(Text text) {
        HoverEvent event = new HoverEvent(HoverEvent.Action.SHOW_TEXT, text);
        modifyStyle(style -> style.setHoverEvent(event));
        return this;
    }

    // TODO this MIGHT not account for the siblings... do that???
    //      test and make sure
    public String toPlain() {
//		StringBuilder result = new StringBuilder(getText());
//		for (ITextComponent sibling : getSiblings()) {
//			result.append(sibling.toString())
//		}
//		return result.toString();
        return getString();
    }

    public String serialize() {
        return getText().replace("\u00A7", "&");
    }

    public static Text of(Object... texts) {
        return new Text(texts);
    }

    public static Text deserialize(String text) {
        return new Text(text.replace("&", "\u00A7"));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final List<Object> texts = new ArrayList<>();

        public Builder append(Text text) {
            this.texts.add(text);
            return this;
        }

        public Text build() {
            return new Text(texts.toArray());
        }
    }
}
