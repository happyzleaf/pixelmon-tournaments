package com.hiroku.tournaments.api;

import com.happyzleaf.tournaments.text.Text;
import com.hiroku.tournaments.api.reward.RewardBase;
import com.hiroku.tournaments.api.rule.RuleSet;
import com.hiroku.tournaments.obj.Zone;
import com.hiroku.tournaments.util.PixelmonUtils;
import com.pixelmonmod.pixelmon.battles.api.rules.BattleProperty;
import com.pixelmonmod.pixelmon.battles.api.rules.BattleRules;
import com.pixelmonmod.pixelmon.battles.api.rules.PropertyValue;
import net.minecraft.util.text.TextFormatting;

import java.util.List;
import java.util.Map;

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
        Text.Builder builder = Text.builder()
                .append(ruleSet.getDisplayText(), "\n\n", TextFormatting.GOLD, TextFormatting.UNDERLINE, "Rewards:");
        for (RewardBase reward : rewards) {
            if (reward.getDisplayText() != null) {
                builder.append("\n", reward.getDisplayText());
            }
        }

        if (!zones.isEmpty()) {
            builder.append("\n", TextFormatting.GOLD, "Zones: ", TextFormatting.DARK_AQUA, zones.size());
        }

        for (Map.Entry<BattleProperty<?>, PropertyValue<?>> entry : PixelmonUtils.getBRProperties(battleRules).entrySet()) {
            builder.append("\n", TextFormatting.DARK_AQUA, entry.getKey().getId()).append(": ").append(entry.getValue().get());
        }

        return builder.build();
    }
}