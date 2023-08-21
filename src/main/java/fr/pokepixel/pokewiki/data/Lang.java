package fr.pokepixel.pokewiki.data;

public class Lang {
	public GeneralLang general = new GeneralLang();

	public static class GeneralLang {
		public String backToFormSelection = "&bClick to go back to form selection";
		public String pokemonType = "&bType:";
		public String eggGroups = "&bEgg group: %egggroups%";
		public String eggSteps = "&bEggsteps needed: %eggsteps%";
		public String evo = "&bClick to see the evolutions:";
		public String noEvo = "&cThere is no evolution for this pokemon!";
		public String spawnInfo = "&bSpawn info:";
		public String loreSpawnInfo1 = "&eClick to see the spawn list";
		public String loreSpawnInfo2 = "&cThis pokemon doesn't spawn naturally!";
		public String catchRate = "&bCatch Rate:";
		public String baseRate = "&eBase rate: %baserate%%";
		public String genderless = "&aGenderless";
		public String malePercent = "&aMale chance: &e%malepercent%";
		public String femalePercent = "&aFemale chance: &e%femalepercent%";
		public String ability = "&bAbility:";
		public String normalAbility = "&e%ability%";
		public String hiddenAbility = "&6%hiddenability% (Hidden)";
		public String breed = "&bBreed:";
		public String drops = "&bDrops:";
		public String movesByLevel = "&bMoves by level:";
		public String typeEffectiveness = "&bType effectiveness:";
		public String baseStats = "&bBase Stats:";
		public String evYield = "&bEV Yield:";
		public String tutorMoves = "&bTutor Moves:";
		public String tmhmMoves = "&bTM/HM Moves:";
		public String trMoves = "&bTR Moves:";
		public String eggMoves = "&bEgg Moves:";
		public String mainGUITitle = "&eWiki";
		public String formGUITitle = "&cChoose the form";
	}

	public SpawnLang spawn = new SpawnLang();

	public static class SpawnLang {
		public String spawnGUITitle = "&cSpawn Info";
		public String back = "&cBack:";
		public String typeOfLocation = "&eType of spawn location: &6%spawnlocation%";
		public String minLevel = "&eMinimum level: &6%minlevel%";
		public String maxLevel = "&eMaximum level: &6%maxlevel%";
		public String heldItems = "&eHeld items: &6%helditems%";
		public String biomes = "&eBiomes: %biomes%";
		public String nearbyBlocks = "&eNearby blocks: %nearbyblocks%";
		public String baseBlocks = "&eBase blocks: %baseblocks%";
		public String rarity = "&eRarity: %rarity%";
		public String weathers = "&eWeather: %weathers%";
		public String times = "&eTimes: %times%";
	}

	public EvolutionLang evolution = new EvolutionLang();

	public static class EvolutionLang {
		public String evoGUITitle = "&cEvolution Info";
		public String back = "&cBack:";
		public String levelingUp = "&aLeveling up ";
		public String levelNumber = "&ato level %level%";
		public String exposedToItem = "&bWhen exposed to %item%";
		public String tradedWith = "&dTrading with %pokemon%";
		public String traded = "&dTrading";
		public String chanceCondition = "&3%chance% percent chance";
		public String evoRockCondition = "&bWithin %range% blocks of a %rockname%";
		public String friendshipCondition = "&bFriendship: %friendship%";
		public String genderCondition = "&bGender: %gender%";
		public String heldItemCondition = "&bHeld item: %helditem%";
		public String aboveAltitudeCondition = "&bAbove altitude: %altitude%";
		public String levelCondition = "&bStarting at level: %level%";
		public String moveCondition = "&bKnowing move: %move%";
		public String moveTypeCondition = "&bWith a move of type: %movetype%";
		public String withPokemonCondition = "&bWith these Pokémon in party: %pokemonlist%";
		public String withTypeCondition = "&bWith Pokémon of these types in party: %typelist%";
		public String withFormCondition = "&bWith Pokémon of these forms in party: %formlist%";
		public String statRatioCondition = "&bWith a stat ratio of %ratio% between %stat1% and %stat2%";
		public String timeCondition = "&bDuring: %time%";
		public String weatherCondition = "&bWith weather: %weather%";
		public String scrollCondition = "&bWith Scroll: %scroll% at range %range%";
		public String battleCriticalCondition = "&bWith critical: %crit%";
		public String recoilCondition = "&bWith Recoil damage: %recoil%";
		public String natureCondition = "&bWith nature: %natures%";
		public String absenceOfHealCondition = "&bWith Health absence: (WIP)";
		public String statusPersistCondition = "&bWith Status: (WIP)";
		public String withinStructureCondition = "&bWithin Structure: (WIP)";
	}

	public OtherLang other = new OtherLang();

	public static class OtherLang {
		public String pokemonNotFound = "&cThis pokemon do not exist!";
	}
}
