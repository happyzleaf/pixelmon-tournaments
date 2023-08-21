package fr.pokepixel.pokewiki.util;

import com.pixelmonmod.pixelmon.api.pokemon.Element;
import com.pixelmonmod.pixelmon.api.pokemon.stats.BattleStatsType;
import net.minecraft.util.text.TextFormatting;

public class ColorUtils {
	public static TextFormatting getTypeColor(Element type) {
		switch (type) {
			case FIRE:
				return TextFormatting.RED;
			case WATER:
			case FLYING:
				return TextFormatting.BLUE;
			case ELECTRIC:
				return TextFormatting.YELLOW;
			case GRASS:
			case BUG:
				return TextFormatting.GREEN;
			case ICE:
			case MYSTERY:
				return TextFormatting.AQUA;
			case FIGHTING:
				return TextFormatting.DARK_RED;
			case POISON:
				return TextFormatting.DARK_PURPLE;
			case GROUND:
				return TextFormatting.GOLD;
			case PSYCHIC:
			case FAIRY:
				return TextFormatting.LIGHT_PURPLE;
			case ROCK:
				return TextFormatting.DARK_GRAY;
			case GHOST:
			case STEEL:
				return TextFormatting.GRAY;
			case DRAGON:
				return TextFormatting.DARK_BLUE;
			case DARK:
				return TextFormatting.BLACK;
			default:
				return TextFormatting.WHITE;
		}
	}

	public static TextFormatting getStatColor(BattleStatsType stat) {
		switch (stat) {
			case HP:
				return TextFormatting.GREEN;
			case ATTACK:
				return TextFormatting.RED;
			case DEFENSE:
				return TextFormatting.GOLD;
			case SPECIAL_ATTACK:
				return TextFormatting.LIGHT_PURPLE;
			case SPECIAL_DEFENSE:
				return TextFormatting.YELLOW;
			case SPEED:
				return TextFormatting.BLUE;
			default:
				return TextFormatting.WHITE;
		}
	}
}
