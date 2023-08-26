package com.happyzleaf.tournaments.args;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.IArgumentSerializer;
import net.minecraft.network.PacketBuffer;
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

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		return ISuggestionProvider.suggest(set.stream(), builder);
	}

	@Override
	public Collection<String> getExamples() {
		return set;
	}

	public static class Serializer implements IArgumentSerializer<ChoiceSetArgument> {
		@Override
		public void write(ChoiceSetArgument argument, PacketBuffer buffer) {
			buffer.writeInt(argument.set.size());
			for (String s : argument.set) {
				buffer.writeString(s);
			}
		}

		@Override
		public ChoiceSetArgument read(PacketBuffer buffer) {
			Set<String> set = new HashSet<>();

			int size = buffer.readInt();
			for (int i = 0; i < size; i++) {
				set.add(buffer.readString());
			}

			return new ChoiceSetArgument(set);
		}

		@Override
		public void write(ChoiceSetArgument argument, JsonObject json) {
			JsonArray array = new JsonArray();
			for (String s : argument.set) {
				array.add(s);
			}
			json.add("set", array);
		}
	}
}
