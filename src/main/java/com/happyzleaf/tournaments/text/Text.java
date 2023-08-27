package com.happyzleaf.tournaments.text;

import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Text extends StringTextComponent {
    public static final Text WIP = of(TextFormatting.RED, "WIP");
    public static final Text ERROR = of(TextFormatting.RED, "ERROR");

    private final List<Object> objs;

    private Text(List<Object> objs) {
        super("");
        this.objs = objs.stream()
                .map(obj -> {
                    if (obj instanceof TextFormatting) {
                        return obj;
                    }

                    return obj instanceof ITextComponent ? obj : new StringTextComponent(obj.toString());
                })
                .peek(obj -> {
                    if (obj instanceof ITextComponent) {
                        appendSibling((ITextComponent) obj);
                    }
                })
                .collect(Collectors.toList());
        deepWalk(this.getStyle());
    }

    private Style deepWalk(Style current) {
        this.setStyle(this.getStyle().mergeStyle(current));

        for (Object obj : this.objs) {
            if (obj instanceof TextFormatting) {
                TextFormatting formatting = (TextFormatting) obj;
                current = current.applyFormatting(formatting);
            } else if (obj instanceof Text) {
                Text text = (Text) obj;
                current = text.deepWalk(current);
            } else {
                TextComponent component = (TextComponent) obj;
                current = component.getStyle().mergeStyle(current);
                component.setStyle(current);
            }
        }

        return current;
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
    //      also make sure that textformatting don't appear. They shouldn't
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
        return new Text(Arrays.asList(texts));
    }

    public static Text deserialize(String text) {
        return new Text(Collections.singletonList(text.replace("&", "\u00A7")));
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
            return new Text(texts);
        }
    }
}
