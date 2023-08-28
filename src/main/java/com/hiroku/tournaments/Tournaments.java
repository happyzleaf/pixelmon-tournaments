package com.hiroku.tournaments;

import com.happyzleaf.tournaments.args.ChoiceSetArgument;
import com.happyzleaf.tournaments.args.UserArgument;
import com.happyzleaf.tournaments.text.TextAction;
import com.hiroku.tournaments.api.reward.RewardTypeRegistrar;
import com.hiroku.tournaments.api.rule.RuleTypeRegistrar;
import com.hiroku.tournaments.api.tiers.TierLoader;
import com.hiroku.tournaments.commands.TournamentCommand;
import com.hiroku.tournaments.commands.elo.EloCommand;
import com.hiroku.tournaments.config.TournamentConfig;
import com.hiroku.tournaments.elo.EloStorage;
import com.hiroku.tournaments.listeners.*;
import com.hiroku.tournaments.rewards.CommandReward;
import com.hiroku.tournaments.rewards.ItemsReward;
import com.hiroku.tournaments.rewards.MoneyReward;
import com.hiroku.tournaments.rewards.PokemonReward;
import com.hiroku.tournaments.rules.decider.HealthTotal;
import com.hiroku.tournaments.rules.decider.PartyCount;
import com.hiroku.tournaments.rules.general.BattleType;
import com.hiroku.tournaments.rules.general.EloType;
import com.hiroku.tournaments.rules.general.SetParty;
import com.hiroku.tournaments.rules.general.TeamCap;
import com.hiroku.tournaments.rules.player.*;
import com.hiroku.tournaments.rules.team.MaxTeamElo;
import com.hiroku.tournaments.rules.team.MinTeamElo;
import com.hiroku.tournaments.rules.team.PartyMax;
import com.hiroku.tournaments.rules.team.PartyMin;
import com.hiroku.tournaments.util.PluginLogger;
import com.hiroku.tournaments.util.TournamentUtils;
import com.pixelmonmod.api.pokemon.PokemonSpecificationProxy;
import com.pixelmonmod.api.pokemon.requirement.impl.HasSpecFlagRequirement;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.config.ForceBattleEndResult;
import com.pixelmonmod.pixelmon.api.config.PixelmonConfigProxy;
import net.minecraft.command.arguments.ArgumentSerializer;
import net.minecraft.command.arguments.ArgumentTypes;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.BusBuilder;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.Arrays;
import java.util.Collections;

/**
 * Base plugin class for the Tournaments plugin/API
 *
 * @author Hiroku
 * @author happyz
 */
@Mod("tournaments")
public class Tournaments {
	public static final String VERSION = "3.0.0";

	public static Tournaments INSTANCE;
	public static final PluginLogger LOGGER = new PluginLogger("tournaments");
	public static final IEventBus EVENT_BUS = BusBuilder.builder().build();

	public static final BattleListener BATTLE_LISTENER = new BattleListener();
	public static final ExperienceListener EXPERIENCE_LISTENER = new ExperienceListener();
	public static final BreedListener BREED_LISTENER = new BreedListener();
	public static final TradeListener TRADE_LISTENER = new TradeListener();
	public static final LoginListener LOGIN_LISTENER = new LoginListener();

	public static void log(String msg) {
		LOGGER.log("Tournaments \u00BB " + msg);
	}

	public Tournaments() {
		INSTANCE = this;

		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onSetup);
	}

	private void onSetup(FMLCommonSetupEvent event) {
		MinecraftForge.EVENT_BUS.addListener(this::onStart);
		MinecraftForge.EVENT_BUS.addListener(this::onCommands);

		ArgumentTypes.register("user", UserArgument.class, new ArgumentSerializer<>(UserArgument::user));
		ArgumentTypes.register("choiceset", ChoiceSetArgument.class, new ArgumentSerializer<>(() -> ChoiceSetArgument.choiceSet(Collections.emptySet())));
	}

	private void onStart(FMLServerAboutToStartEvent event) {
		log("Initializing Tournaments version " + VERSION + ", last updated for Pixelmon " + Pixelmon.VERSION + "...");
		log("This platform was written by Hiroku and happyz!");

		TournamentUtils.createDir("config/tournaments");
		TournamentUtils.createDir("data/tournaments");

		Pixelmon.EVENT_BUS.register(BATTLE_LISTENER);
		Pixelmon.EVENT_BUS.register(EXPERIENCE_LISTENER);
		Pixelmon.EVENT_BUS.register(BREED_LISTENER);
		Pixelmon.EVENT_BUS.register(TRADE_LISTENER);
		MinecraftForge.EVENT_BUS.register(LOGIN_LISTENER);

		registerDefaultRules();
		registerDefaultRewards();

		TournamentConfig.load();
		TierLoader.load();
		Zones.load();
		Presets.load();
		EloStorage.load();

		if (PixelmonConfigProxy.getBattle().getForceEndBattleResult() != ForceBattleEndResult.ABNORMAL) {
			// TODO: check if this is still in pixelmon.hocon
			log("WARNING: forceEndBattleResult in pixelmon.hocon should be set to 2. Ending bugged tournament battles will not go well!");
		}

		// TODO: placeholder?
//		if (Sponge.getPluginManager().isLoaded("placeholderapi"))
//			EloPlaceholder.addPlaceholder();

		PokemonSpecificationProxy.register(new HasSpecFlagRequirement("rental"));
	}

	public void onCommands(RegisterCommandsEvent event) {
		event.getDispatcher().register(TextAction.build());
		event.getDispatcher().register(new TournamentCommand().create());
		event.getDispatcher().register(new EloCommand().create());
	}

	public void registerDefaultRules() {
		RuleTypeRegistrar.registerRuleType(Arrays.asList("setparty", "setlevel"), SetParty.class);
		RuleTypeRegistrar.registerRuleType(Arrays.asList("randompokemon", "randoms"), RandomPokemon.class);
		RuleTypeRegistrar.registerRuleType(Arrays.asList("helditems", "helditemsallowed"), HeldItems.class);
		RuleTypeRegistrar.registerRuleType(Arrays.asList("bannedability", "bannedabilities", "disallowedability", "disallowedabilities"), DisallowedAbility.class);
		RuleTypeRegistrar.registerRuleType(Arrays.asList("pokemontiers", "pokemonsets", "tiers", "sets", "tier", "set"), Tiers.class);
		RuleTypeRegistrar.registerRuleType(Arrays.asList("partymax", "partymaximum"), PartyMax.class);
		RuleTypeRegistrar.registerRuleType(Arrays.asList("partymin", "partyminimum"), PartyMin.class);
		RuleTypeRegistrar.registerRuleType(Arrays.asList("levelmax", "levelmaximum", "maxlevel", "maximumlevel"), LevelMax.class);
		RuleTypeRegistrar.registerRuleType(Arrays.asList("levelmin", "levelminimum", "minlevel", "minimumlevel"), LevelMin.class);
		RuleTypeRegistrar.registerRuleType(Arrays.asList("healing", "healallowed", "healingallowed"), Healing.class);
		RuleTypeRegistrar.registerRuleType(Collections.singletonList("battletype"), BattleType.class);
		RuleTypeRegistrar.registerRuleType(Arrays.asList("teamcap", "teams"), TeamCap.class);
		RuleTypeRegistrar.registerRuleType(Arrays.asList("partycount", "partydecider", "deciderparty"), PartyCount.class);
		RuleTypeRegistrar.registerRuleType(Arrays.asList("healthtotal", "healthdecider", "deciderhealth"), HealthTotal.class);
		RuleTypeRegistrar.registerRuleType(Arrays.asList("bannedmove", "bannedmoves", "disallowedmove", "disallowedmoves"), DisallowedMove.class);
		RuleTypeRegistrar.registerRuleType(Arrays.asList("bannedpokemon", "bannedpokemons", "disallowedpokemon", "disallowedpokemons"), DisallowedPokemon.class);

		// Elo rules
		RuleTypeRegistrar.registerRuleType(Collections.singletonList("elotype"), EloType.class);
		RuleTypeRegistrar.registerRuleType(Arrays.asList("minelo", "minimumelo"), MinElo.class);
		RuleTypeRegistrar.registerRuleType(Arrays.asList("maxelo", "maximumelo"), MaxElo.class);
		RuleTypeRegistrar.registerRuleType(Arrays.asList("minteamelo", "minimumteamelo"), MinTeamElo.class);
		RuleTypeRegistrar.registerRuleType(Arrays.asList("maxteamelo", "maximumteamelo"), MaxTeamElo.class);
	}

	public void registerDefaultRewards() {
		RewardTypeRegistrar.registerRewardType(Arrays.asList("command", "cmd"), CommandReward.class);
		RewardTypeRegistrar.registerRewardType(Arrays.asList("poke", "pokemon", "mon"), PokemonReward.class);
		RewardTypeRegistrar.registerRewardType(Arrays.asList("items", "item"), ItemsReward.class);
		RewardTypeRegistrar.registerRewardType(Arrays.asList("money", "cash", "cashmoney", "bank", "coin", "dosh", "$$$"), MoneyReward.class);
	}
}
