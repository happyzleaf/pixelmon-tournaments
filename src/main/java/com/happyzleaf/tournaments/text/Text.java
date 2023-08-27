package com.happyzleaf.tournaments.text;

import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Text extends StringTextComponent {
    public static final Text WIP = of(TextFormatting.RED, "WIP");
    public static final Text ERROR = of(TextFormatting.RED, "ERROR");

    private final List<Object> objs;

    private Text(List<Object> objs) {
        super("");

        this.objs = objs.stream().flatMap(obj -> {
            if (obj instanceof Text) {
                return ((Text) obj).objs.stream();
            } else {
                return Stream.of(obj);
            }
        }).collect(Collectors.toList());

        Style style = this.getStyle();
        for (Object obj : this.objs) {
            if (obj == null) continue;

            if (obj instanceof TextFormatting) {
                TextFormatting formatting = (TextFormatting) obj;
                style = style.applyFormatting(formatting);
            } else {
                TextComponent component = obj instanceof TextComponent ? (TextComponent) obj : new StringTextComponent(obj.toString());
                component.setStyle(component.getStyle().mergeStyle(style));
                this.appendSibling(component);
            }
        }

//        this.objs = objs;
//        walkStyle(this.getStyle());
    }

//    private void walkStyle(Style current) {
//        this.setStyle(current);
//
//        for (Object obj : this.objs) {
//            if (obj instanceof TextFormatting) {
//                TextFormatting formatting = (TextFormatting) obj;
//                current = current.applyFormatting(formatting);
//            } else {
//                TextComponent component = obj instanceof TextComponent ? (TextComponent) obj : new StringTextComponent(obj.toString());
//                if (component instanceof Text) {
//                    ((Text) component).walkStyle(current);
//                }
//
//                component.setStyle(current);
//
//                if (!this.siblings.contains(component)) {
//                    this.siblings.add(component);
//                }
//            }
//        }
//    }

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
