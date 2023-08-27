package com.hiroku.tournaments.commands;

import com.happyzleaf.tournaments.text.Text;
import com.happyzleaf.tournaments.User;
import com.happyzleaf.tournaments.args.ChoiceSetArgument;
import com.hiroku.tournaments.api.Tournament;
import com.hiroku.tournaments.enums.TournamentStates;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TextFormatting;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.hiroku.tournaments.util.CommandUtils.getOptArgument;

public class IgnoreCommand implements Command<CommandSource> {
	private static final Set<String> CHOICES = new HashSet<>(Arrays.asList("yes", "no"));

	public LiteralArgumentBuilder<CommandSource> create() {
		return Commands.literal("ignore")
//				.description(Text.of("Toggles tournament messages"))
				.requires(source -> User.hasPermission(source, "tournaments.command.common.ignore"))
				.executes(this)
				.then(
						Commands.argument("yes/no", ChoiceSetArgument.choiceSet(CHOICES))
								.executes(this)
				);
	}

	@Override
	public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
		if (Tournament.instance() == null || Tournament.instance().state == TournamentStates.CLOSED) {
			context.getSource().sendFeedback(Text.of(TextFormatting.RED, "There's nothing to ignore; there's no tournament open"), true);
			return 0;
		}

		PlayerEntity player = context.getSource().asPlayer();
		boolean ignore = !Tournament.instance().ignoreList.contains(player.getUniqueID());

		Boolean choice = getOptArgument(context, "yes/no", String.class).map(s -> s.equals("yes")).orElse(null);
		if (choice != null) {
			ignore = choice;
		}

		Tournament.instance().ignoreList.remove(player.getUniqueID());
		if (ignore)
			Tournament.instance().ignoreList.add(player.getUniqueID());

		context.getSource().sendFeedback(Tournament.instance().getMessageProvider().getIgnoreToggleMessage(ignore), true);

		return 1;
	}
}
