package fr.pokepixel.pokewiki.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.pixelmonmod.pixelmon.api.command.PixelmonCommandUtils;
import fr.pokepixel.pokewiki.PokeWiki;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;

import static fr.pokepixel.pokewiki.util.GsonUtils.getAllCustom;

public class PokeWikiReloadCommand {
	public static LiteralArgumentBuilder<CommandSource> build() {
		return Commands.literal("pokewikireload")
				.requires(source -> PixelmonCommandUtils.hasPermission(source, "pokewiki.reload"))
				.executes(context -> {
					PokeWiki.reload();
					PokeWiki.customSpawnPokemonInfoListInfo = getAllCustom();
					context.getSource().sendFeedback(new StringTextComponent("\u00A7aConfig and Lang reloaded!"), false);

					return 1;
				});
	}
}
