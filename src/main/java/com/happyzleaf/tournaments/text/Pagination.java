package com.happyzleaf.tournaments.text;

import com.happyzleaf.tournaments.HappyzUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ICommandSource;
import net.minecraft.util.Util;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;

public class Pagination {
    private static final int MAX_LINE_CHARS = 55;
    private static final Text NEWLINE = Text.of("\n"); // Apparently Minecraft is not consistent with its newlines, but hopefully we won't encounter problems
    private static final Text SPACE = Text.of(" ");

    private final Text title;
    private final Text padding;
    private final Text header;
    private final Text footer;
    private final int linesPerPage;

    private final int pages;
    private final List<Text> contents;

    private Pagination(Text title, Text header, Text footer, Text padding, List<Text> contents, int linesPerPage) {
        this.title = title;
        this.header = header;
        this.footer = footer;
        this.padding = padding;
        this.linesPerPage = linesPerPage;

        this.pages = (int) ((double) contents.size() / linesPerPage);
        this.contents = contents;
    }

    private static void repeat(Text.Builder builder, Text text, int times) {
        for (int i = 0; i < times; ++i) {
            builder.append(text);
        }
    }

    private static void centered(Text.Builder builder, Text padding, Text text) {
        int textLen = text.toPlain().length();
        int paddingLen = padding.toPlain().length();
        int paddingAmountTop = (int) ((MAX_LINE_CHARS - textLen - 2d) / (paddingLen * 2d));

        repeat(builder, padding, paddingAmountTop);
        builder.append(SPACE);
        builder.append(text);
        builder.append(SPACE);
        repeat(builder, padding, paddingAmountTop);
    }

    private Text get(int page) {
        if (page < 0 || page >= this.pages) {
            return null;
        }

        int pos = page * this.linesPerPage;
        List<Text> lines = HappyzUtils.subListSafe(this.contents, pos, pos + this.linesPerPage);

        Text.Builder builder = Text.builder();

        centered(builder, this.padding, this.title);
        builder.append(this.header);
        this.contents.forEach(builder::append);
        builder.append(this.footer);

        Text backArrow = Text.of("<");
        if (page > 0) {
            backArrow.onClick((src, ctx) -> {
                ctx.keepAlive();
                src.sendMessage(get(page - 1), Util.DUMMY_UUID);
            });
        }

        Text nextArrow = Text.of(">");
        if (page < this.pages) {
            nextArrow.onClick((src, ctx) -> {
                ctx.keepAlive();
                src.sendMessage(get(page + 1), Util.DUMMY_UUID);
            });
        }

        centered(builder, padding, Text.of(backArrow, " " + (page + 1) + "/" + pages + " ", nextArrow));

        return builder.build();
    }

    public static Pagination.Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Text title = Text.of(TextFormatting.GREEN, "Title");
        private Text padding = Text.of(TextFormatting.DARK_GREEN, "-");
        private Text header = Text.of(TextFormatting.GREEN, "Header");
        private Text footer = Text.of(TextFormatting.GREEN, "Footer");
        private int linesPerPage = 10;
        private List<Text> contents = new ArrayList<>();

        public Builder title(Text title) {
            this.title = title;
            return this;
        }

        public Builder padding(Text padding) {
            this.padding = padding;
            return this;
        }

        public Builder header(Text header) {
            this.header = header;
            return this;
        }

        public Builder footer(Text footer) {
            this.footer = footer;
            return this;
        }

        public Builder linesPerPage(int linesPerPage) {
            this.linesPerPage = linesPerPage;
            return this;
        }

        public Builder contents(List<Text> contents) {
            this.contents.addAll(contents);
            return this;
        }

        private Pagination build() {
            return new Pagination(this.title, this.padding, this.header, this.footer, this.contents, this.linesPerPage);
        }

        public void sendTo(ICommandSource source) {
            Text firstPage = build().get(0);
            if (firstPage != null) {
                source.sendMessage(firstPage, Util.DUMMY_UUID);
            }
        }

        public void sendTo(CommandSource source) {
            Text firstPage = build().get(0);
            if (firstPage != null) {
                source.sendFeedback(firstPage, true);
            }
        }
    }
}
