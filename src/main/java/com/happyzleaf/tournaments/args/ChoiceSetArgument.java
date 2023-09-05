package com.happyzleaf.tournaments.args;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ChoiceSetArgument implements ArgumentType<String> {
    public static final SimpleCommandExceptionType CHOICE_UNKNOWN = new SimpleCommandExceptionType(new StringTextComponent(TextFormatting.RED + "Unknown choice"));

    private final Set<String> set = new HashSet<>();

    public static ChoiceSetArgument choiceSet(Set<String> set) {
        return new ChoiceSetArgument(set);
    }

    private ChoiceSetArgument(Set<String> set) {
        this.set.addAll(set);
    }

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        String name = reader.readUnquotedString();
        if (set.contains(name)) {
            return name;
        } else {
            throw CHOICE_UNKNOWN.create();
        }
    }

    public static <S> SuggestionProvider<S> suggest(Set<String> set) {
        return (context, builder) -> ISuggestionProvider.suggest(set, builder);
    }

    public <S> SuggestionProvider<S> suggest() {
        return suggest(set);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return ISuggestionProvider.suggest(set, builder);
    }

    @Override
    public Collection<String> getExamples() {
        return set;
    }
}
