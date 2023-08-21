package fr.pokepixel.pokewiki;

import com.pixelmonmod.pixelmon.api.pokemon.PokemonFactory;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static fr.pokepixel.pokewiki.gui.ChoiceForm.openChoiceFormGUI;
import static fr.pokepixel.pokewiki.gui.DisplayInfo.displayInfoGUI;

public class ForgeEvents {
	@SubscribeEvent
	public void onShiftRightClick(PlayerInteractEvent.EntityInteract event) {
		if (!PokeWiki.config.allowRightClickToOpenWiki) {
			return;
		}

		if (event.getTarget() instanceof PixelmonEntity && event.getPlayer().isSneaking() && event.getPlayer().inventory.getCurrentItem().isEmpty()) {
			ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
			PixelmonEntity pokemon = (PixelmonEntity) event.getTarget();
			if (pokemon.getSpecies().getForms(false).size() > 1) {
				openChoiceFormGUI(player, pokemon.getSpecies());
			} else {
				displayInfoGUI(player, PokemonFactory.create(pokemon.getSpecies()));
			}
		}
	}
}
