package com.hiroku.tournaments.rewards;

import com.happyzleaf.tournaments.Text;
import com.hiroku.tournaments.Tournaments;
import com.hiroku.tournaments.api.reward.RewardBase;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TextFormatting;

import java.math.BigDecimal;

/**
 * Reward that gives money to the player.
 *
 * @author Hiroku
 */
// TODO: economy
public class MoneyReward extends RewardBase {
	/**
	 * How much money should be given
	 */
	public double amount;

	public MoneyReward(String arg) throws Exception {
		super(arg);

		amount = Double.parseDouble(arg);
	}

	@Override
	public void give(PlayerEntity player) {
//		EconomyService economy = Sponge.getServiceManager().provide(EconomyService.class).get();
//		Optional<UniqueAccount> optAcc = economy.getOrCreateAccount(player.getUniqueId());
//
//		if (optAcc.isPresent()) {
//			optAcc.get().deposit(economy.getDefaultCurrency(), BigDecimal.valueOf(amount), Cause.builder().insert(0, Tournaments.INSTANCE).build(EventContext.empty()));
//			player.sendMessage(Text.of(TextFormatting.DARK_GREEN, "You were rewarded ", economy.getDefaultCurrency().getSymbol(), amount));
//		} else
//			player.sendMessage(Text.of(TextFormatting.RED, "There was an error giving you the cash reward of ", economy.getDefaultCurrency().getSymbol(), amount));
	}

	@Override
	public Text getDisplayText() {
		return Text.of(TextFormatting.GOLD, "Money: ", TextFormatting.DARK_AQUA, amount);
	}

	@Override
	public boolean visibleToAll() {
		return true;
	}

	@Override
	public String getSerializationString() {
		return "cashmoney:" + amount;
	}
}
