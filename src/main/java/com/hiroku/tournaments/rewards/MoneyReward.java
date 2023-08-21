package com.hiroku.tournaments.rewards;

import java.math.BigDecimal;
import java.util.Optional;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.hiroku.tournaments.Tournaments;
import com.hiroku.tournaments.api.reward.RewardBase;

/**
 * Reward that gives money to the player.
 * 
 * @author Hiroku
 */
public class MoneyReward extends RewardBase
{
	/** How much money should be given */
	public double amount;
	
	public MoneyReward(String arg) throws Exception
	{
		super(arg);
		
		amount = Double.parseDouble(arg);
	}

	@Override
	public void give(Player player)
	{
		EconomyService economy = Sponge.getServiceManager().provide(EconomyService.class).get();
		Optional<UniqueAccount> optAcc = economy.getOrCreateAccount(player.getUniqueId());
		
		if (optAcc.isPresent())
		{
			optAcc.get().deposit(economy.getDefaultCurrency(), BigDecimal.valueOf(amount), Cause.builder().insert(0, Tournaments.INSTANCE).build(EventContext.empty()));
			player.sendMessage(Text.of(TextColors.DARK_GREEN, "You were rewarded ", economy.getDefaultCurrency().getSymbol(), amount));
		}
		else
			player.sendMessage(Text.of(TextColors.RED, "There was an error giving you the cash reward of ", economy.getDefaultCurrency().getSymbol(), amount));
	}

	@Override
	public Text getDisplayText()
	{
		return Text.of(TextColors.GOLD, "Money: ", TextColors.DARK_AQUA, amount);
	}

	@Override
	public boolean visibleToAll()
	{
		return true;
	}

	@Override
	public String getSerializationString()
	{
		return "cashmoney:" + amount;
	}
}
