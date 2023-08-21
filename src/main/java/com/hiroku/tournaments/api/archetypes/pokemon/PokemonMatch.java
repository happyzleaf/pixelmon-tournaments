package com.hiroku.tournaments.api.archetypes.pokemon;

import com.hiroku.tournaments.Tournaments;
import com.hiroku.tournaments.Zones;
import com.hiroku.tournaments.api.Match;
import com.hiroku.tournaments.api.Tournament;
import com.hiroku.tournaments.api.events.match.MatchStartEvent;
import com.hiroku.tournaments.api.rule.types.DeciderRule;
import com.hiroku.tournaments.api.rule.types.RuleBase;
import com.hiroku.tournaments.config.TournamentConfig;
import com.hiroku.tournaments.obj.MatchStartResult;
import com.hiroku.tournaments.obj.Side;
import com.hiroku.tournaments.obj.Team;
import com.hiroku.tournaments.obj.Zone;
import com.hiroku.tournaments.rules.general.BattleType;
import com.hiroku.tournaments.rules.general.BattleType.TeamsComposition;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.battles.controller.BattleControllerBase;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.battles.rules.BattleRules;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.enums.battle.EnumBattleEndCause;
import com.pixelmonmod.pixelmon.enums.battle.EnumBattleType;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PokemonMatch extends Match {
	public PokemonMatch(Side side1, Side side2) {
		super(side1, side2);
	}

	/**
	 * The BattleControllerBase for the battle (if the match battle is active)
	 */
	public BattleControllerBase bcb;

	/**
	 * Whether side 1 has flagged the battle as being bugged.
	 */
	public boolean side1BugFlag;
	/**
	 * Whether side 2 has flagged the battle as being bugged.
	 */
	public boolean side2BugFlag;

	/**
	 * Whether the match's battle ending should be listened to. This is for internal purposes.
	 */
	public boolean listenToBattleEnd = true;

	/**
	 * Heals a particular user
	 */
	public void heal(User user) {
		if (user.isOnline()) {
			PlayerPartyStorage storage = Pixelmon.storageManager.getParty((EntityPlayerMP) user.getPlayer().get());//PixelmonStorage.pokeBallManager.getPlayerStorage((EntityPlayerMP)user.getPlayer().get()).get();
			storage.heal();//.healAllPokemon((World) user.getPlayer().get().getWorld());
		}
	}

	/**
	 * Gets whether a battle has been flagged as bugged by both sides.
	 */
	public boolean isBattleBugged() {
		return side1BugFlag && side2BugFlag;
	}

	/**
	 * Attempts to start a {@link Match}. This will:
	 * <li>attempt to start the match in {@link Match}.start(),</li>
	 * <li>if it succeeds, teleport the players of the match to the respective zones, if possible,</li>
	 * <li>if it fails because of an error, treat it like a crashed battle and rely on deciding rules,</li>
	 * <li>if it fails because of insufficient Pokemon, an offline player or broken rule the not-offending team is given the win.</li>
	 *
	 * @param - The {@link Match} being started.
	 */
	@Override
	public void start(Tournament tournament, boolean rematch) {
		matchActive = true;
		bcb = null;

		for (Side side : sides) {
			for (Team team : side.teams) {
				for (User user : team.users) {
					EntityPlayerMP player = (EntityPlayerMP) user.getPlayer().get();

					BattleControllerBase bcb = BattleRegistry.getSpectatedBattle(player);
					if (bcb != null)
						bcb.removeSpectator(player);
					bcb = BattleRegistry.getBattle(player);
					if (bcb != null)
						bcb.endBattle(EnumBattleEndCause.FORCE);
				}
			}
		}

		Zone zone = Zones.INSTANCE.getZone(this);
		if (zone != null)
			zone.sendPlayersToZone(this);

		Sponge.getScheduler().createSyncExecutor(Tournaments.INSTANCE).schedule(() ->
		{
			tournament.getModes().forEach(mode -> mode.onMatchStart(tournament, this));
			MatchStartResult result = start(rematch);
			if (result instanceof MatchStartResult.Success)
				Tournaments.EVENT_BUS.post(new MatchStartEvent(this, zone));
			else if (result instanceof MatchStartResult.InsufficientPokemon) {
				Zones.INSTANCE.matchEnded(this);
				Side side = this.getSide(((MatchStartResult.InsufficientPokemon) result).user.getUniqueId());
				sendMessage(tournament.getMessageProvider().getInsufficientPokemonMessage(side));
				tournament.matchEnds(this, this.getOtherSide(side), side);
			} else if (result instanceof MatchStartResult.PlayerOffline) {
				Zones.INSTANCE.matchEnded(this);
				Side side = getSide(((MatchStartResult.PlayerOffline) result).user.getUniqueId());
				sendMessage(tournament.getMessageProvider().getPlayersOfflineMessage(side));
				tournament.matchEnds(this, getOtherSide(side), side);
			} else if (result instanceof MatchStartResult.RuleBroken) {
				Zones.INSTANCE.matchEnded(this);

				MatchStartResult.RuleBroken ruleResult = (MatchStartResult.RuleBroken) result;

				sendMessage(tournament.getMessageProvider().getRuleBreakMessage(ruleResult));

				Side side = ((MatchStartResult.RuleBroken) result).side;
				tournament.matchEnds(this, this.getOtherSide(side), side);
			} else if (result instanceof MatchStartResult.BattleRuleBroken) {
				Zones.INSTANCE.matchEnded(this);

				MatchStartResult.BattleRuleBroken ruleResult = (MatchStartResult.BattleRuleBroken) result;
				sendMessage(tournament.getMessageProvider().getBattleRuleBreakMessage(ruleResult));

				Side side = ((MatchStartResult.BattleRuleBroken) result).side;
				tournament.matchEnds(this, this.getOtherSide(side), side);
			} else if (result instanceof MatchStartResult.Error) {
				Zones.INSTANCE.matchEnded(this);
				((MatchStartResult.Error) result).exception.printStackTrace();
				handleCrashedBattle();
			}
		}, 5, TimeUnit.SECONDS);
	}

	// The contents of this function may seem unnecessarily complicated, but I am both trying to plan for every possibility and 
	// also making it a bit easier in the long run for when triple or rotation battles are added, as well as 3-sided battles
	public MatchStartResult start(boolean rematch) {
		boolean heal = !rematch && Tournament.instance().getRuleSet().healingAllowed();

		if (heal)
			for (Side side : sides)
				for (Team team : side.teams)
					team.users.forEach(this::heal);

		try {
			TeamsComposition composition = TeamsComposition.SINGLE;
			BattleType rule = Tournament.instance().getRuleSet().getRule(BattleType.class);
			if (rule != null)
				composition = rule.composition;

			int neededPokemon = composition.neededPokemon;

			ArrayList<BattleParticipant> side1Participants = new ArrayList<BattleParticipant>();
			ArrayList<BattleParticipant> side2Participants = new ArrayList<BattleParticipant>();

			BattleRules br = new BattleRules(Tournament.instance().getRuleSet().br.exportText());
			br.battleType = composition == TeamsComposition.SINGLE ? EnumBattleType.Single : EnumBattleType.Double;

			for (int sideIndex = 0; sideIndex < 2; sideIndex++) {
				Side side = sides[sideIndex];

				RuleBase brokenSideRule = Tournament.instance().getRuleSet().getBrokenRule(side);
				if (brokenSideRule != null)
					return new MatchStartResult.RuleBroken(side, null, null, brokenSideRule);

				int numOnline = side.getNumPlayers(false);
				int membersRemaining = numOnline;
				ArrayList<EntityPixelmon> sidePokemon = new ArrayList<EntityPixelmon>();

				teamloop:
				for (Team team : side.teams) {
					RuleBase brokenTeamRule = Tournament.instance().getRuleSet().getBrokenRule(team);
					if (brokenTeamRule != null)
						return new MatchStartResult.RuleBroken(side, team, null, brokenTeamRule);

					for (User user : team.users) {
						// Running this in here because a PlayerOffline result DOES need a user. This will break the loop anyway.
						if (numOnline == 0)
							return new MatchStartResult.PlayerOffline(team, user);

						if (!user.isOnline())
							continue;

						PlayerPartyStorage storage = Pixelmon.storageManager.getParty((EntityPlayerMP) user.getPlayer().get());

						RuleBase brokenPlayerRule = Tournament.instance().getRuleSet().getBrokenRule(user.getPlayer().get(), storage);
						if (brokenPlayerRule != null)
							return new MatchStartResult.RuleBroken(side, team, user, brokenPlayerRule);

						String validateBR = br.validateTeam(storage.getTeam());
						if (validateBR != null)
							return new MatchStartResult.BattleRuleBroken(side, team, user, validateBR);

						EntityPixelmon firstPokemon = storage.getAndSendOutFirstAblePokemon((Entity) user.getPlayer().get());
						EntityPixelmon secondPokemon = null;
						if (numOnline == 1 || (sidePokemon.size() - neededPokemon > 1 && membersRemaining == 1)) {
							// Having a side with 1 person with 0 Pokémon is always insufficient.
							if (firstPokemon == null)
								return new MatchStartResult.InsufficientPokemon(team, user);
							else if (neededPokemon == 2) {
								Pokemon secondPk = storage.findOne(pokemon -> pokemon.canBattle() && firstPokemon.getPokemonData() != pokemon);
								if (secondPk != null)
									secondPokemon = secondPk.getOrSpawnPixelmon((Entity) user.getPlayer().get());
								if (secondPokemon == null)
									return new MatchStartResult.InsufficientPokemon(team, user);
								sidePokemon.add(secondPokemon);
							}
						}
						if (firstPokemon != null) {
							sidePokemon.add(firstPokemon);
							BattleParticipant bp;
							if (secondPokemon != null)
								bp = new PlayerParticipant((EntityPlayerMP) user, firstPokemon, secondPokemon);
							else
								bp = new PlayerParticipant((EntityPlayerMP) user, firstPokemon);
							if (sideIndex == 0)
								side1Participants.add(bp);
							else
								side2Participants.add(bp);
						}
						membersRemaining--;
						if (sidePokemon.size() == neededPokemon)
							break teamloop;
					}
				}
			}

			BattleParticipant[] side1ParticipantsArr = new BattleParticipant[side1Participants.size()];
			for (int i = 0; i < side1ParticipantsArr.length; i++)
				side1ParticipantsArr[i] = side1Participants.get(i);
			BattleParticipant[] side2ParticipantsArr = new BattleParticipant[side2Participants.size()];
			for (int i = 0; i < side2ParticipantsArr.length; i++)
				side2ParticipantsArr[i] = side2Participants.get(i);

			this.bcb = new BattleControllerBase(side1ParticipantsArr, side2ParticipantsArr, br);
			BattleRegistry.registerBattle(bcb);

			this.side1BugFlag = this.side2BugFlag = false;

			return MatchStartResult.success();
		} catch (Exception exc) {
			return new MatchStartResult.Error(exc);
		}
	}

	@Override
	public void forceEnd() {
		this.listenToBattleEnd = false;
		if (this.bcb != null && !this.bcb.battleEnded) {
			try {
				bcb.endBattle(EnumBattleEndCause.FORCE);
			} catch (Exception e) {
				Tournaments.log("Couldn't force end battle properly due to some Pixelmon issue. Let's hope it doesn't break anything");
				BattleRegistry.deRegisterBattle(this.bcb);
			}
		}
		this.listenToBattleEnd = true;
	}

	@Override
	public Text getStateText() {
		if (bcb != null)
			return Text.of(TextColors.YELLOW, "*");
		return super.getStateText();
	}

	/**
	 * Handles a crashed battle by first checking for any crash decider rules. If a decision is made,
	 * that {@link Side} will be declared the winner. If a decision could not be made or there were no
	 * decider rules, a rematch will be scheduled.
	 */
	public void handleCrashedBattle() {
		List<DeciderRule> deciders = Tournament.instance().getRuleSet().getCrashDeciderRules();
		Side winningSide = null;
		for (DeciderRule rule : deciders)
			if ((winningSide = rule.decideWinner(this)) != null)
				break;

		if (winningSide == null) {
			sendMessage(Tournament.instance().getMessageProvider().getMatchErrorMessage(this));
			Sponge.getScheduler().createSyncExecutor(Tournaments.INSTANCE).schedule(() ->
			{
				if (Tournament.instance().round.contains(this))
					start(Tournament.instance(), true);
			}, TournamentConfig.INSTANCE.timeBeforeMatch - 5, TimeUnit.SECONDS);
		} else
			Tournament.instance().matchEnds(this, winningSide, getOtherSide(winningSide));
	}

	/**
	 * Gets the {@link PokemonMatch} for the given {@link BattleControllerBase}, if one exists.
	 */
	public static PokemonMatch getMatch(BattleControllerBase bcb) {
		if (Tournament.instance() == null)
			return null;
		for (Match match : Tournament.instance().round)
			if (match instanceof PokemonMatch)
				if (((PokemonMatch) match).bcb == bcb)
					return (PokemonMatch) match;
		return null;
	}
}
