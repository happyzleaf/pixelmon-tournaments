package com.hiroku.tournaments.rewards;

import com.happyzleaf.tournaments.text.Text;
import com.hiroku.tournaments.api.reward.RewardBase;
import com.hiroku.tournaments.util.GsonUtils;
import com.hiroku.tournaments.util.TournamentUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Util;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

		items = new ArrayList<>();

		// TODO: Since arg is a string and is passed right into Sponge.getServer().getPlayer(arg) I suppose it is the username
		ServerPlayerEntity player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByUsername(arg);
		if (player == null) {
			try {
				Map<String, Object> map = GsonUtils.uglyGson.fromJson(arg, Map.class);
				for (Object o : map.values()) {
					items.add(ItemStack.read(GsonUtils.nbtFromMap((Map<String, Object>) o)));
				}
			} catch (Exception e) {
				throw new Exception("Invalid player name: " + arg);
			}
		} else {
			for (ItemStack is : items) {
				TournamentUtils.giveItemsToPlayer(player, is.copy());
			}
		}

		// Not sure why this is done so late, but I'll leave it there
		if (items.isEmpty()) {
			throw new IllegalArgumentException("No items in inventory!");
		}
	}

	@Override
	public void give(PlayerEntity player) {
		if (!items.isEmpty()) {
			player.sendMessage(Text.of(TextFormatting.DARK_GREEN, "You've received item rewards!"), Util.DUMMY_UUID);
			for (ItemStack stack : items)
				TournamentUtils.giveItemsToPlayer(player, stack.copy());
		}
	}

	@Override
	public Text getDisplayText() {
		Text.Builder builder = Text.builder();
		if (items.isEmpty())
			return null;
		builder.append(Text.of(TextFormatting.GOLD, "Items: "));
		// TODO:                                                                            is this ok with the translation?  vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
		builder.append(Text.of(TextFormatting.DARK_AQUA, items.get(0).getCount() == 1 ? "" : (items.get(0).getCount() + " "), items.get(0).getDisplayName().getString()));
		for (int i = 1; i < items.size(); i++) {
			builder.append(Text.of(TextFormatting.GOLD, ", "));
			// TODO:                                                                                                  also this?  vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
			builder.append(Text.of(TextFormatting.DARK_AQUA, items.get(i).getCount() == 1 ? "" : (items.get(i).getCount() + " "), items.get(i).getDisplayName().getString()));
		}
		return builder.build();
	}

	@Override
	public boolean visibleToAll() {
		return true;
	}

	@Override
	public String getSerializationString() {
		CompoundNBT superTag = new CompoundNBT();
		for (int i = 0; i < items.size(); i++) {
			superTag.put("itemstack-" + i, items.get(i).write(new CompoundNBT()));
		}

		return "items:" + GsonUtils.uglyGson.toJson(GsonUtils.nbtToMap(superTag));
	}
}
