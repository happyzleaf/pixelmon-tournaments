package com.hiroku.tournaments.rules.player;

import com.happyzleaf.tournaments.text.Text;
import com.happyzleaf.tournaments.User;
import com.hiroku.tournaments.Tournaments;
import com.hiroku.tournaments.api.Tournament;
import com.hiroku.tournaments.api.rule.types.PlayerRule;
import com.hiroku.tournaments.api.rule.types.RuleBase;
import com.hiroku.tournaments.api.tiers.Tier;
import com.hiroku.tournaments.obj.Team;
import com.hiroku.tournaments.util.PixelmonUtils;
import com.hiroku.tournaments.util.TournamentUtils;
import com.pixelmonmod.api.pokemon.PokemonSpecification;
import com.pixelmonmod.api.pokemon.PokemonSpecificationProxy;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonFactory;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import com.pixelmonmod.pixelmon.api.storage.PCStorage;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.util.helpers.CollectionHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.text.TextFormatting;

import java.util.*;

/**
 * When applied on a tournament, random Pokémon are provided and automatically taken back.
 *
 * @author Hiroku
 */
public class RandomPokemon extends PlayerRule {
	public List<Species> globalPool = new ArrayList<>();
	public HashMap<UUID, Integer> rerollsRemaining = new HashMap<>();

	public List<Tier> tiers = new ArrayList<>();
	public final PokemonSpecification spec;
	public boolean rentalOnly = false;
	public boolean localDuplicates = false;
	public boolean globalDuplicates = true;
	public int numPokemon;
	public int maxRerolls = 1;

	public RandomPokemon(String arg) throws Exception {
		super(arg);

		List<String> args = new ArrayList<>(Arrays.asList(arg.split(",")));
		args.add("untradeable");
		args.add("unbreedable");
		args.add("rental"); // TODO: is this a problem? What happens to the spec object?
		spec = PokemonSpecificationProxy.create(args);

		for (String argument : args) {
			if (Tier.parse(argument) != null)
				tiers.add(Tier.parse(argument));
			else if (argument.equalsIgnoreCase("rentalonly"))
				rentalOnly = true;
			else if (argument.equalsIgnoreCase("localduplicates"))
				localDuplicates = true;
			else if (argument.equalsIgnoreCase("!globalduplicates"))
				globalDuplicates = false;
			else if (argument.toLowerCase().contains("reroll")) {
				try {
					maxRerolls = Integer.parseInt(argument.split(":")[1]);
					if (maxRerolls < -1)
						throw new IllegalArgumentException("Invalid number of rerolls. Must not be below -1");
				} catch (Exception e) {
				}
			} else if (argument.toLowerCase().contains("pokemon")) {
				try {
					numPokemon = Integer.parseInt(argument.split(":")[1]);
					if (numPokemon < 1)
						throw new IllegalArgumentException("Invalid number of Pokémon. Must be larger than zero");
				} catch (Exception e) {
				}
			}
		}

		if (numPokemon < 1) {
			throw new Exception("Missing argument: Number of Pokémon to provide");
		}

		if (Tournament.instance() != null) {
			Tiers rule = Tournament.instance().getRuleSet().getRule(Tiers.class);
			if (rule == null && !tiers.isEmpty()) {
				String classesStr = tiers.get(0).key;
				for (int i = 1; i < tiers.size(); i++)
					classesStr += "," + tiers.get(i).key;
				Tournament.instance().getRuleSet().addRule(new Tiers(classesStr));
			}
		}
	}

	@Override
	public boolean passes(PlayerEntity player, PlayerPartyStorage storage) {
		if (!this.rentalOnly)
			return true;

		return storage.findOne(pokemon -> !pokemon.isEgg() && !PixelmonUtils.isRental(pokemon)) == null;
	}

	@Override
	public Text getBrokenRuleText(PlayerEntity player) {
		return Text.of(TextFormatting.DARK_AQUA, player.getName(), TextFormatting.RED, " brought non-rental Pokémon into the tournament!");
	}

	@Override
	public boolean duplicateAllowed(RuleBase other) {
		for (Tier tier : tiers)
			if (!((RandomPokemon) other).tiers.contains(tier))
				((RandomPokemon) other).tiers.add(tier);
		return false;
	}

	@Override
	public String getSerializationString() {
		StringBuilder line = new StringBuilder("randompokemon:numpokemon:" + numPokemon + ",maxrerolls:" + maxRerolls);
		if (this.rentalOnly)
			line.append(",rentalonly");
		String specString = spec.toString();
		if (!specString.equals(""))
			line.append(",").append(specString);
		for (Tier tier : tiers)
			line.append(",").append(tier.key);
		if (localDuplicates)
			line.append(",localduplicates");
		if (!globalDuplicates)
			line.append(",!globalduplicates");
		return line.toString();
	}

	@Override
	public Text getDisplayText() {
		return Text.of(TextFormatting.GOLD, "Random Pokémon: ", TextFormatting.DARK_AQUA, numPokemon, " provided. ",
				(rentalOnly ? "Players must use the Pokémon provided" : "Players may choose to use other Pokémon"),
				(maxRerolls > 0 ? (". " + maxRerolls + " rerolls") : ". No rerolls"));
	}

	@Override
	public boolean visibleToAll() {
		return true;
	}

	@Override
	public boolean canRuleBeAdded(Tournament tournament, RuleBase rule) {
		if (rule instanceof Tiers)
			((Tiers) rule).synchronize(this);

		return true;
	}

	@Override
	public boolean canTeamJoin(Tournament tournament, Team team, boolean forced) {
		if (!forced && this.maxRerolls >= 0) {
			for (User user : team.users) {
				if (this.rerollsRemaining.containsKey(user.id) && this.rerollsRemaining.get(user.id) < 1) {
					if (team.users.size() > 1)
						team.sendMessage(Text.of(TextFormatting.RED,
								"At least one member of your team has attempted to join/reroll too many times!"));
					else
						team.sendMessage(Text.of(TextFormatting.RED,
								"Too many joins/rerolls! You may not join."));
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public void onTeamJoin(Tournament tournament, Team team, boolean forced) {
		for (User user : team.users) {
			if (removeRentalPokemon(user, false)) {
				giveRandomPokemon(user);
			}
		}
	}

	@Override
	public void onTeamLeave(Tournament tournament, Team team, boolean forced) {
		for (User user : team.users) {
			removeRentalPokemon(user, true);
		}
	}

	@Override
	public void onTeamKnockedOut(Tournament tournament, Team team) {
		for (User user : team.users) {
			removeRentalPokemon(user, true);
		}
	}

	@Override
	public void onTeamForfeit(Tournament tournament, Team team, boolean forced) {
		for (User user : team.users) {
			removeRentalPokemon(user, true);
		}
	}

	@Override
	public void onTournamentEnd(Tournament tournament, List<User> winners) {
		for (User user : winners) {
			removeRentalPokemon(user, true);
		}

		globalPool = new ArrayList<>();
		rerollsRemaining = new HashMap<>();
	}

	/**
	 * @param user          The {@link User} to check.
	 * @param validateParty Whether the party should be validated after the rental pokémon have been removed.
	 * @return {@code true} if the user was online and the check could've been made. {@code false} otherwise.
	 */
	public static boolean removeRentalPokemon(User user, boolean validateParty) {
		PlayerEntity player = user.getPlayer();
		if (player == null) return false;

		// PC
		PCStorage pc = user.getPC();
		List<Pokemon> toRemove = new ArrayList<>();
		for (Pokemon pokemon : pc.getAll()) {
			if (pokemon != null && PixelmonUtils.isRental(pokemon)) {
				toRemove.add(pokemon);
			}
		}

		for (Pokemon pokemon : toRemove) {
			TournamentUtils.giveItemsToPlayer(player, pokemon.getHeldItem());
			Tournaments.log("Took rental Pokémon: " + pokemon.getSpecies().getLocalizedName() + " from " + user.getName() + "'s PC");
			pc.set(pokemon.getPosition(), null);
		}

		// Party
		PlayerPartyStorage party = user.getParty();
		for (Pokemon pokemon : party.getTeam()) {
			if (PixelmonUtils.isRental(pokemon)) {
				TournamentUtils.giveItemsToPlayer(player, pokemon.getHeldItem());
				Tournaments.log("Took rental Pokémon: " + pokemon.getSpecies().getLocalizedName() + " from " + user.getName() + "'s party");
				party.set(pokemon.getPosition(), null);
			}
		}

		if (validateParty) {
			boolean hasNonEgg = party.findOne(pokemon -> !pokemon.isEgg()) != null;
			if (!hasNonEgg) {
				// Won't have space to get a normal Pokémon; make some space.
				if (!party.hasSpace())
					pc.transfer(party, party.findOne(Pokemon::isEgg).getPosition(), pc.getFirstEmptyPosition());

				Pokemon replacement = pc.findOne(pokemon -> !pokemon.isEgg() && !PixelmonUtils.isRental(pokemon));

				if (replacement != null) {
					party.transfer(pc, replacement.getPosition(), party.getFirstEmptyPosition());
					Tournaments.log("Moved " + user.getName() + "'s " + replacement.getSpecies().getLocalizedName() + " into their party");
				}
			}
		}

		return true;
	}

	/**
	 * @param user The {@link User} to give the random pokémon to. Must be online.
	 */
	public void giveRandomPokemon(User user) {
		PlayerEntity player = user.getPlayer();
		if (player == null) return;

		List<Species> pool = new ArrayList<>();
		for (Species species : PixelmonSpecies.getAll())
			if (!PixelmonSpecies.MELTAN.getValueUnsafe().equals(species) && PixelmonSpecies.MELMETAL.getValueUnsafe().equals(species))
				pool.add(species);
		for (Tier tier : tiers)
			tier.filter(pool);
		if (!globalDuplicates)
			pool.removeIf(p -> globalPool.contains(p));

		PlayerPartyStorage party = user.getParty();
		PCStorage pc = user.getPC();

		for (Pokemon pokemon : party.getAll())
			if (pokemon != null)
				pc.transfer(party, pokemon.getPosition(), pc.getFirstEmptyPosition());

		for (int i = 0; i < numPokemon; i++) {
			if (pool.isEmpty()) {
				if (!globalDuplicates) {
					if (!globalPool.isEmpty()) {
						// If there are no more globally unique Pokémon left to give, clear the list and start again
						// Point being that this should MINIMISE duplicate species throughout the tournament
						globalPool.clear();
						i--;
						continue;
					}
				}
				Tournaments.log("Serious problem: The random Pokémon pool is empty. Are your Pokémon tiers broken?");
				return;
			}

			Pokemon pokemon = PokemonFactory.create(CollectionHelper.getRandomElement(pool));
			spec.apply(pokemon);

			party.add(pokemon);

			if (!globalDuplicates)
				globalPool.add(pokemon.getSpecies());
			if (!localDuplicates)
				pool.removeIf(p -> p == pokemon.getSpecies());

			player.sendMessage(Text.of(TextFormatting.DARK_GREEN, "You were provided with a ", TextFormatting.DARK_AQUA, pokemon.getSpecies().getLocalizedName()), Util.DUMMY_UUID);
			Tournaments.log("Given a " + pokemon.getSpecies().getLocalizedName() + " to " + user.getName() + " marked with the rental tag");
		}

		if (!rerollsRemaining.containsKey(user.id)) {
			rerollsRemaining.put(user.id, this.maxRerolls);
		} else {
			rerollsRemaining.put(user.id, rerollsRemaining.get(user.id) - 1);
		}

		int remaining = rerollsRemaining.get(user.id);
		if (remaining > 0) {
			player.sendMessage(Text.of(TextFormatting.GRAY, "To get a new set of random Pokémon, you may use ", TextFormatting.DARK_AQUA, "/tournament reroll"), Util.DUMMY_UUID);
			if (this.maxRerolls > -1) {
				player.sendMessage(Text.of(TextFormatting.GRAY, "Rerolls remaining: ", TextFormatting.DARK_AQUA, remaining), Util.DUMMY_UUID);
			}
		}
	}

	public boolean attemptReroll(User user) {
		if (maxRerolls > -1 && rerollsRemaining.containsKey(user.id) && rerollsRemaining.get(user.id) < 1) {
			user.sendMessage(Text.of(TextFormatting.RED, "You may not reroll."));
			return false;
		}
		removeRentalPokemon(user, false);
		giveRandomPokemon(user);
		return true;
	}
}
