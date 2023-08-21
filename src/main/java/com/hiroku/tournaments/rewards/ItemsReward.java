package com.hiroku.tournaments.rewards;

import com.hiroku.tournaments.api.reward.RewardBase;
import com.hiroku.tournaments.util.GsonUtils;
import com.hiroku.tournaments.util.TournamentUtils;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.*;

/**
 * A reward that gives the player a set of items.
 *
 * @author Hiroku
 */
public class ItemsReward extends RewardBase {
	/**
	 * The items to give to the player
	 */
	public final List<ItemStack> items;

	@SuppressWarnings("unchecked")
	public ItemsReward(String arg) throws Exception {
		super(arg);

		items = new ArrayList<ItemStack>();

		Optional<Player> optPlayer;

		try {
			optPlayer = Sponge.getServer().getPlayer(arg);
		} catch (Exception e) {
			optPlayer = Optional.empty();
		}
		if (!optPlayer.isPresent()) {
			try {
				Map<String, Object> map = GsonUtils.uglyGson.fromJson(arg, Map.class);
				for (Object o : map.values())
					items.add((ItemStack) (Object) new net.minecraft.item.ItemStack(GsonUtils.nbtFromMap((Map<String, Object>) o)));
			} catch (Exception e) {
				throw new Exception("Invalid player name: " + arg);
			}
		} else {
			Player player = optPlayer.get();

			player.getInventory().slots().forEach(slot ->
			{
				Optional<ItemStack> stack = slot.poll();
				if (stack.isPresent())
					items.add(stack.get().copy());
			});
		}

		if (items.isEmpty())
			throw new IllegalArgumentException("No items in inventory!");
	}

	@Override
	public void give(Player player) {
		if (!items.isEmpty()) {
			player.sendMessage(Text.of(TextColors.DARK_GREEN, "You've received item rewards!"));
			for (ItemStack stack : items)
				TournamentUtils.giveItemsToPlayer(player, stack.copy());
		}
	}

	@Override
	public Text getDisplayText() {
		Text.Builder builder = Text.builder();
		if (items.isEmpty())
			return null;
		builder.append(Text.of(TextColors.GOLD, "Items: "));
		builder.append(Text.of(TextColors.DARK_AQUA, items.get(0).getQuantity() == 1 ? "" : (items.get(0).getQuantity() + " "), items.get(0).getTranslation().get(Locale.ENGLISH)));
		for (int i = 1; i < items.size(); i++) {
			builder.append(Text.of(TextColors.GOLD, ", "));
			int q = items.get(i).getQuantity();
			builder.append(Text.of(TextColors.DARK_AQUA, q == 1 ? "" : (q + " "), items.get(i).getTranslation().get(Locale.ENGLISH)));
		}
		return builder.build();
	}

	@Override
	public boolean visibleToAll() {
		return true;
	}

	@Override
	public String getSerializationString() {
		NBTTagCompound superTag = new NBTTagCompound();
		for (int i = 0; i < items.size(); i++)
			superTag.setTag("itemstack-" + i, ((net.minecraft.item.ItemStack) (Object) items.get(i)).writeToNBT(new NBTTagCompound()));

		return "items:" + GsonUtils.uglyGson.toJson(GsonUtils.nbtToMap(superTag));
	}
}
