package com.hiroku.tournaments.rules.player;

import com.hiroku.tournaments.api.rule.types.PlayerRule;
import com.hiroku.tournaments.api.rule.types.RuleBase;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.battles.attacks.AttackBase;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.List;

public class DisallowedMove extends PlayerRule {
	public List<String> moves = new ArrayList<>();

	public DisallowedMove(String arg) throws Exception {
		super(arg);

		String[] splits = arg.split(",");
		for (String name : splits) {
			String str = name.replace("_", " ");
			if (AttackBase.getAttackBase(str).isPresent())
				moves.add(str);
			else
				throw new Exception("Invalid move. These are case sensitive, and without spaces. e.g. Tackle. Use _ instead of space");
		}
	}

	@Override
	public boolean passes(Player player, PlayerPartyStorage storage) {
		for (Pokemon pokemon : storage.getTeam())
			for (Attack attack : pokemon.getMoveset())
				if (moves.contains(attack.getActualMove().getLocalizedName()))
					return false;
		return true;
	}

	@Override
	public Text getBrokenRuleText(Player player) {
		return Text.of(TextColors.DARK_AQUA, player.getName(), TextColors.RED, " has a Pokémon with a disallowed move!");
	}

	@Override
	public Text getDisplayText() {
		Text.Builder builder = Text.builder();
		builder.append(Text.of(TextColors.DARK_AQUA, moves.get(0)));
		for (int i = 1; i < moves.size(); i++)
			builder.append(Text.of(TextColors.GOLD, ", ", TextColors.DARK_AQUA, moves.get(i)));
		return Text.of(TextColors.GOLD, "Disallowed move(s): ", builder.build());
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
		String serialize = moves.get(0);
		for (int i = 1; i < moves.size(); i++)
			serialize += "," + moves.get(i);
		return "disallowedmoves:" + serialize;
	}

	@Override
	public boolean visibleToAll() {
		return true;
	}
}
