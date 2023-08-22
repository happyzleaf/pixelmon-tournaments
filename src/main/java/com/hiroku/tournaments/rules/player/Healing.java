package com.hiroku.tournaments.rules.player;

import com.hiroku.tournaments.api.Match;
import com.hiroku.tournaments.api.Tournament;
import com.hiroku.tournaments.api.rule.types.PlayerRule;
import com.hiroku.tournaments.api.rule.types.RuleBase;
import com.hiroku.tournaments.obj.Side;
import com.hiroku.tournaments.obj.Team;
import com.hiroku.tournaments.util.PokemonState;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.util.helpers.CollectionHelper;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Rule demanding that players not heal (or by extension, add to their party) their Pokémon between battles. Healing defaults to being
 * allowed. If it is allowed, players will be healed prior to each battle.
 *
 * @author Hiroku
 */
public class Healing extends PlayerRule {
	/**
	 * Whether healing is allowed. If true, Pokémon will be healed prior to each battle.
	 */
	public final boolean healingAllowed;
	/**
	 * A mapping from player UUID to a list of the previously saved states of a player's party. This is updated after each battle.
	 */
	public HashMap<UUID, ArrayList<PokemonState>> states = new HashMap<>();

	public Healing(String arg) throws Exception {
		super(arg);

		healingAllowed = Boolean.parseBoolean(arg);
	}

	@Override
	public boolean passes(Player player, PlayerPartyStorage storage) {
		if (healingAllowed)
			return true;

		ArrayList<PokemonState> playerStates = states.get(player.getUniqueId());
		if (playerStates != null) {
			for (PokemonState state : playerStates) {
				Pokemon pokemon = storage.find(state.id);

				if (pokemon != null) {
					// Did they heal this Pokémon? If so, bad bad.
					if (state.hasHealed(pokemon))
						return false;
				}

				// This pokemon state was missing! If it's dead... well ok that's probably fine to remove as long as it wasn't replaced.
			}
			// They have changed their party. That's totally uncool.
			return storage.findOne(pokemon -> !pokemon.isEgg() && CollectionHelper.find(playerStates, state -> state.id.equals(pokemon.getUUID())) == null) != null;
		}
		return true;
	}

	/**
	 * Saves all of the party state data for this match's players.
	 *
	 * @param match - The match whose players are getting their party states saved.
	 */
	@Override
	public void onMatchEnd(Tournament tournament, Match match, Side winningSide, Side losingSide) {
		for (Side side : match.sides) {
			for (Team team : side.teams) {
				for (User user : team.users) {
					try {
						PlayerPartyStorage storage = Pixelmon.storageManager.getParty(user.getUniqueId());
						ArrayList<PokemonState> playerStates = new ArrayList<>();
						for (Pokemon pokemon : storage.getTeam())
							playerStates.add(new PokemonState(pokemon));
						this.states.put(user.getUniqueId(), playerStates);
					} catch (NoSuchElementException nsee) {
						nsee.printStackTrace();
					}
				}
			}
		}
	}

	@Override
	public Text getDisplayText() {
		return Text.of(TextColors.GOLD, "Healing between battles: ", TextColors.DARK_AQUA, healingAllowed ? "Allowed." : "Not allowed.");
	}

	@Override
	public boolean duplicateAllowed(RuleBase other) {
		return false;
	}

	@Override
	public boolean visibleToAll() {
		return true;
	}

	@Override
	public Text getBrokenRuleText(Player player) {
		return Text.of(TextColors.DARK_AQUA, player.getName(), TextColors.RED, " has healed at least one of their Pokémon! ", TextStyles.ITALIC, "Disqualified!");
	}

	@Override
	public String getSerializationString() {
		return "healing:" + healingAllowed;
	}
}
