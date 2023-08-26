package com.hiroku.tournaments.rules.player;

import com.happyzleaf.tournaments.Text;
import com.hiroku.tournaments.api.rule.types.PlayerRule;
import com.hiroku.tournaments.api.rule.types.RuleBase;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;

public class DisallowedMove extends PlayerRule {
	public List<String> moves = new ArrayList<>();

	public DisallowedMove(String arg) throws Exception {
		super(arg);

		String[] splits = arg.split(",");
		for (String name : splits) {
			String str = name.replace("_", " ");
			if (Attack.hasAttack(str))
				moves.add(str);
			else
				throw new Exception("Invalid move. These are case sensitive, and without spaces. e.g. Tackle. Use _ instead of space");
		}
	}

	@Override
	public boolean passes(PlayerEntity player, PlayerPartyStorage storage) {
		for (Pokemon pokemon : storage.getTeam())
			for (Attack attack : pokemon.getMoveset())
				if (moves.contains(attack.getActualMove().getLocalizedName()))
					return false;
		return true;
	}

	@Override
	public Text getBrokenRuleText(PlayerEntity player) {
		return Text.of(TextFormatting.DARK_AQUA, player.getName(), TextFormatting.RED, " has a Pokémon with a disallowed move!");
	}

	@Override
	public Text getDisplayText() {
		Text.Builder builder = Text.builder();
		builder.append(Text.of(TextFormatting.DARK_AQUA, moves.get(0)));
		for (int i = 1; i < moves.size(); i++)
			builder.append(Text.of(TextFormatting.GOLD, ", ", TextFormatting.DARK_AQUA, moves.get(i)));
		return Text.of(TextFormatting.GOLD, "Disallowed move(s): ", builder.build());
	}

	@Override
	public boolean duplicateAllowed(RuleBase other) {
		// Transfers this rule's ability list into the rule that's about to replace it.
		DisallowedMove disallowed = (DisallowedMove) other;
		for (String ability : this.moves)
			if (!disallowed.moves.contains(ability))
				disallowed.moves.add(ability);

		return false;
	}

	@Override
	public String getSerializationString() {
		StringBuilder serialize = new StringBuilder(moves.get(0));
		for (int i = 1; i < moves.size(); i++)
			serialize.append(",").append(moves.get(i));
		return "disallowedmoves:" + serialize;
	}

	@Override
	public boolean visibleToAll() {
		return true;
	}
}
