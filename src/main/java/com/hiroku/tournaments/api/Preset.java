package com.hiroku.tournaments.api;

import com.happyzleaf.tournaments.text.Text;
import com.hiroku.tournaments.api.reward.RewardBase;
import com.hiroku.tournaments.api.rule.RuleSet;
import com.hiroku.tournaments.obj.Zone;
import com.pixelmonmod.pixelmon.battles.api.rules.BattleRules;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

/**
 * A preset of rules, rewards, and zones.
 *
 * @author Hiroku
 */
public class Preset {
	/**
	 * The {@link RuleSet} for a preset.
	 */
	public RuleSet ruleSet;
	/**
	 * The {@link RewardBase}s for a preset.
	 */
	public List<RewardBase> rewards;
	/**
	 * The {@link Zone}s that should be used for tournaments of this preset. If empty, all will be used.
	 */
	public List<Zone> zones;
	/**
	 * The BattleRules for the preset.
	 */
	public BattleRules battleRules;

	public Preset(RuleSet ruleSet, List<RewardBase> rewards, List<Zone> zones) {
		this.ruleSet = ruleSet;
		this.rewards = rewards;
		this.zones = zones;
		this.battleRules = ruleSet.br;
	}

	public Text getDisplayText() {
		Text.Builder builder = Text.builder().append(ruleSet.getDisplayText());
		builder.append(Text.of("\n\n", TextFormatting.GOLD, TextFormatting.UNDERLINE, "Rewards:"));
		for (RewardBase reward : rewards)
			if (reward.getDisplayText() != null)
				builder.append(Text.of("\n", reward.getDisplayText()));
		if (!zones.isEmpty())
			builder.append(Text.of("\n", TextFormatting.GOLD, "Zones: ", TextFormatting.DARK_AQUA, zones.size()));
		for (String battleRule : battleRules.exportText().split("\n"))
			builder.append(Text.of("\n", TextFormatting.DARK_AQUA, battleRule));
		return builder.build();
	}
}