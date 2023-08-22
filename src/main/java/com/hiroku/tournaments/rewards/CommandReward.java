package com.hiroku.tournaments.rewards;

import com.happyzleaf.tournaments.Text;
import com.hiroku.tournaments.api.reward.RewardBase;

/**
 * Reward which executes a command with the player's username inserted in place of {{player}}
 *
 * @author Hiroku
 */
public class CommandReward extends RewardBase {
	public Text displayText = Text.of("");
	public String command = "";

	public CommandReward(String arg) throws Exception {
		super(arg);

		if (arg.trim().equals(""))
			throw new IllegalArgumentException("Missing arguments: text and command");

		if (arg.contains("text:")) {
			String subArg = arg.substring(arg.indexOf("text:") + 5).split(";")[0];
			displayText = TextSerializers.FORMATTING_CODE.deserialize(subArg);
		}
		if (arg.contains("cmd:"))
			command = arg.substring(arg.indexOf("cmd:") + 4).split(";")[0];

		if (command.equals(""))
			throw new IllegalArgumentException("Missing argument: cmd");
	}

	@Override
	public void give(Player player) {
		if (command.startsWith("/"))
			command = command.replace("/", "");
		Sponge.getGame().getCommandManager().process(Sponge.getServer().getConsole(), command.replaceAll("\\{\\{player\\}\\}", player.getName()));
	}

	@Override
	public String getSerializationString() {
		return "command:text:" + TextSerializers.FORMATTING_CODE.serialize(displayText) + ";cmd:" + command;
	}

	@Override
	public Text getDisplayText() {
		return displayText;
	}

	@Override
	public boolean visibleToAll() {
		return !displayText.toPlain().equals("");
	}
}
