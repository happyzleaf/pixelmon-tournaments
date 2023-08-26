package com.hiroku.tournaments.rules.player;

import com.hiroku.tournaments.Tournaments;
import com.hiroku.tournaments.api.Tournament;
import com.hiroku.tournaments.api.rule.types.PlayerRule;
import com.hiroku.tournaments.api.rule.types.RuleBase;
import com.hiroku.tournaments.api.tiers.Tier;
import com.hiroku.tournaments.obj.Team;
import com.hiroku.tournaments.util.PokemonUtils;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonSpec;
import com.pixelmonmod.pixelmon.api.pokemon.SpecFlag;
import com.pixelmonmod.pixelmon.api.storage.PCStorage;
import com.pixelmonmod.pixelmon.api.storage.StoragePosition;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.util.helpers.CollectionHelper;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * When applied on a tournament, random Pokémon are provided and automatically taken back.
 *
 * @author Hiroku
 */
public class RandomPokemon extends PlayerRule {
	public static Executor removeRentalExecutor = Executors.newSingleThreadExecutor();

	public List<EnumSpecies> globalPool = new ArrayList<>();
	public HashMap<UUID, Integer> rerollsRemaining = new HashMap<>();

	public List<Tier> tiers = new ArrayList<>();
	public PokemonSpec spec = new PokemonSpec();
	public boolean rentalOnly = false;
	public boolean localDuplicates = false;
	public boolean globalDuplicates = true;
	public int numPokemon;
	public int maxRerolls = 1;

	public RandomPokemon(String arg) throws Exception {
		super(arg);

		String[] args = arg.split(",");

		spec = PokemonSpec.from(args);
		if (spec.extraSpecs == null)
			spec.extraSpecs = new ArrayList<>();
		spec.extraSpecs.add(new SpecFlag("untradeable"));
		spec.extraSpecs.add(new SpecFlag("unbreedable"));

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
	public boolean passes(Player player, PlayerPartyStorage storage) {
		if (!this.rentalOnly)
			return true;

		return storage.findOne(pokemon -> !pokemon.isEgg() && !new PokemonSpec("rental").matches(pokemon)) == null;
	}

	@Override
	public Text getBrokenRuleText(Player player) {
		return Text.of(TextColors.DARK_AQUA, player.getName(), TextColors.RED, " brought non-rental Pokémon into the tournament!");
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
		String line = "randompokemon:numpokemon:" + numPokemon + ",maxrerolls:" + maxRerolls;
		if (this.rentalOnly)
			line += ",rentalonly";
		String specString = PokemonUtils.serializePokemonSpec(spec);
		if (!specString.equals(""))
			line += "," + specString;
		for (Tier tier : tiers)
			line += "," + tier.key;
		if (localDuplicates)
			line += ",localduplicates";
		if (!globalDuplicates)
			line += ",!globalduplicates";
		return line;
	}

	@Override
	public Text getDisplayText() {
		return Text.of(TextColors.GOLD, "Random Pokémon: ", TextColors.DARK_AQUA, numPokemon, " provided. ",
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
				if (this.rerollsRemaining.containsKey(user.getUniqueId()) && this.rerollsRemaining.get(user.getUniqueId()) < 1) {
					if (team.users.size() > 1)
						team.sendMessage(Text.of(TextColors.RED,
								"At least one member of your team has attempted to join/reroll too many times!"));
					else
						team.sendMessage(Text.of(TextColors.RED,
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
			if (user.getPlayer().isPresent()) {
				removeRentalPokemon(user.getPlayer().get(), false);
				giveRandomPokemon(user.getUniqueId());
			}
		}
	}

	@Override
	public void onTeamLeave(Tournament tournament, Team team, boolean forced) {
		for (User user : team.users)
			if (user.getPlayer().isPresent())
				removeRentalPokemon(user.getPlayer().get(), true);
	}

	@Override
	public void onTeamKnockedOut(Tournament tournament, Team team) {
		for (User user : team.users)
			if (user.getPlayer().isPresent())
				removeRentalPokemon(user.getPlayer().get(), true);
	}

	@Override
	public void onTeamForfeit(Tournament tournament, Team team, boolean forced) {
		for (User user : team.users)
			if (user.getPlayer().isPresent())
				removeRentalPokemon(user.getPlayer().get(), true);
	}

	@Override
	public void onTournamentEnd(Tournament tournament, List<User> winners) {
		for (User user : winners)
			if (user.getPlayer().isPresent())
				removeRentalPokemon(user.getPlayer().get(), true);

		globalPool = new ArrayList<>();
		rerollsRemaining = new HashMap<>();
	}

	public static void removeRentalPokemon(User user, boolean validateParty) {
		PlayerPartyStorage party = Pixelmon.storageManager.getParty(user.getUniqueId());
		PCStorage pc = Pixelmon.storageManager.getPCForPlayer(user.getUniqueId());
		Pokemon[] pcPokemon = pc.getAll();
		PokemonSpec rental = new PokemonSpec("rental");
		for (Pokemon pokemon : party.getTeam()) {
			if (rental.matches(pokemon)) {
				PokemonUtils.stripHeldItem(user, pokemon);
				Tournaments.log("Took rental Pokémon: " + pokemon.getSpecies().getLocalizedName() + " from " + user.getName() + "'s party");
				party.set(pokemon.getPosition(), null);
			}
		}

		removeRentalExecutor.execute(() ->
		{
			List<Pokemon> toRemove = new ArrayList<>();
			for (Pokemon pokemon : pcPokemon)
				if (pokemon != null && rental.matches(pokemon))
					toRemove.add(pokemon);
			if (toRemove.isEmpty())
				return;

			FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() ->
			{
				for (Pokemon pokemon : toRemove) {
					StoragePosition position = pokemon.getPosition();
					boolean isStillThere = position != null && pc.get(pokemon.getPosition()) == pokemon;
					if (isStillThere) {
						PokemonUtils.stripHeldItem(user, pokemon);
						Tournaments.log("Took rental Pokémon: " + pokemon.getSpecies().getLocalizedName() + " from " + user.getName() + "'s PC");
						pc.set(pokemon.getPosition(), null);
					}
				}
			});
		});

		if (validateParty) {
			boolean hasNonEgg = party.findOne(pokemon -> !pokemon.isEgg()) != null;
			if (!hasNonEgg) {
				// Won't have space to get a normal Pokémon; make some space.
				if (!party.hasSpace())
					pc.transfer(party, party.findOne(Pokemon::isEgg).getPosition(), pc.getFirstEmptyPosition());

				Pokemon replacement = pc.findOne(pokemon -> !pokemon.isEgg() && !pokemon.isInRanch() && !rental.matches(pokemon));

				if (replacement != null) {
					party.transfer(pc, replacement.getPosition(), party.getFirstEmptyPosition());
					Tournaments.log("Moved " + user.getName() + "'s " + replacement.getSpecies().getLocalizedName() + " into their party");
				}
			}
		}
	}

	public void giveRandomPokemon(UUID uuid) {
		List<EnumSpecies> pool = new ArrayList<>();
		for (EnumSpecies p : EnumSpecies.values())
			if (p != EnumSpecies.Meltan && p != EnumSpecies.Melmetal)
				pool.add(p);
		for (Tier tier : tiers)
			tier.filter(pool);
		if (!globalDuplicates)
			pool.removeIf(p -> globalPool.contains(p));

		PlayerPartyStorage party = Pixelmon.storageManager.getParty(uuid);
		PCStorage pc = Pixelmon.storageManager.getPCForPlayer(uuid);

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

			Pokemon pokemon = Pixelmon.pokemonFactory.create(CollectionHelper.getRandomElement(pool));
			spec.apply(pokemon);
			new PokemonSpec("untradeable", "unbreedable", "rental").apply(pokemon);

			party.add(pokemon);

			if (!globalDuplicates)
				globalPool.add(pokemon.getSpecies());
			if (!localDuplicates)
				pool.removeIf(p -> p == pokemon.getSpecies());
			Optional<Player> optPlayer = Sponge.getServer().getPlayer(uuid);
			if (optPlayer.isPresent()) {
				optPlayer.get().sendMessage(Text.of(TextColors.DARK_GREEN, "You were provided with a ", TextColors.DARK_AQUA, pokemon.getSpecies().getLocalizedName()));
				Tournaments.log("Given a " + pokemon.getSpecies().getLocalizedName() + " to " + optPlayer.get().getName() + " marked with the rental tag");
			}
		}
		if (!rerollsRemaining.containsKey(uuid))
			rerollsRemaining.put(uuid, this.maxRerolls);
		else
			rerollsRemaining.put(uuid, rerollsRemaining.get(uuid) - 1);

		Optional<Player> optPlayer = Sponge.getServer().getPlayer(uuid);
		if (optPlayer.isPresent()) {
			int remaining = rerollsRemaining.get(uuid);
			if (remaining > 0) {
				optPlayer.get().sendMessage(Text.of(TextColors.GRAY, "To get a new set of random Pokémon, you may use ", TextColors.DARK_AQUA, "/tournament reroll"));
				if (this.maxRerolls > -1)
					optPlayer.get().sendMessage(Text.of(TextColors.GRAY, "Rerolls remaining: ", TextColors.DARK_AQUA, remaining));
			}
		}
	}

	public boolean attemptReroll(Player player) {
		if (maxRerolls > -1 && rerollsRemaining.containsKey(player.getUniqueId()) && rerollsRemaining.get(player.getUniqueId()) < 1) {
			player.sendMessage(Text.of(TextColors.RED, "You may not reroll."));
			return false;
		}
		removeRentalPokemon(player, false);
		giveRandomPokemon(player.getUniqueId());
		return true;
	}
}
