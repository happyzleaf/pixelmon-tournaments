package fr.pokepixel.pokewiki;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.pokepixel.pokewiki.command.PokeWikiCommand;
import fr.pokepixel.pokewiki.command.PokeWikiReloadCommand;
import fr.pokepixel.pokewiki.data.Config;
import fr.pokepixel.pokewiki.data.CustomInfo;
import fr.pokepixel.pokewiki.data.Lang;
import fr.pokepixel.pokewiki.translation.Translation;
import fr.pokepixel.pokewiki.util.Utils;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

import static fr.pokepixel.pokewiki.util.GsonUtils.getAllCustom;

@Mod("pokewiki")
public class PokeWiki {
	private static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("pokewiki/config.json");
	private static final Path LANG_PATH = FMLPaths.CONFIGDIR.get().resolve("pokewiki/lang.json");
	public static final Path CUSTOM_INFO_PATH = FMLPaths.CONFIGDIR.get().resolve("pokewiki/customspawninfo.json");

	public static final Logger LOGGER = LogManager.getLogger();
	public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

	public static Translation translation;

	public static Config config;
	public static Lang lang;
	public static HashMap<String, List<CustomInfo.CustomSpawnPokemonInfo>> customSpawnPokemonInfoListInfo;

	public PokeWiki() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
	}

	public void setup(FMLCommonSetupEvent event) {
		MinecraftForge.EVENT_BUS.addListener(this::onServerAboutToStart);
		MinecraftForge.EVENT_BUS.addListener(this::onServerStarting);
		MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
	}

	@SubscribeEvent
	public void onServerAboutToStart(FMLServerAboutToStartEvent event) {
		CUSTOM_INFO_PATH.getParent().toFile().mkdir();
		boolean check = CUSTOM_INFO_PATH.toFile().exists();
		if (!check) {
			PrintWriter start;
			try {
				start = new PrintWriter(CUSTOM_INFO_PATH.toFile(), "UTF-8");
				List<CustomInfo.CustomSpawnPokemonInfo> customSpawnPokemonInfos = Lists.newArrayList();
				customSpawnPokemonInfos.add(new CustomInfo.CustomSpawnPokemonInfo("MissingNo", Lists.newArrayList("Who knows ?")));
				HashMap<String, List<CustomInfo.CustomSpawnPokemonInfo>> defaultHashMap = new HashMap<>();
				defaultHashMap.put("MissingNo", customSpawnPokemonInfos);
				start.write(GSON.toJson(new CustomInfo(defaultHashMap)));
				start.close();
			} catch (FileNotFoundException | UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

		reload();
		MinecraftForge.EVENT_BUS.register(new ForgeEvents());
	}

	public static void reload() {
		lang = Utils.readOrCreate(Lang.class, LANG_PATH, Lang::new);
		config = Utils.readOrCreate(Config.class, CONFIG_PATH, Config::new);
		translation = new Translation(config.lang, "pixelmon");
	}

	public void onServerStarting(FMLServerStartingEvent event) {
		customSpawnPokemonInfoListInfo = getAllCustom();
	}

	public void onRegisterCommands(RegisterCommandsEvent event) {
		LiteralCommandNode<CommandSource> node = event.getDispatcher().register(PokeWikiCommand.build());
		event.getDispatcher().register(Commands.literal("pwiki").redirect(node));
		event.getDispatcher().register(PokeWikiReloadCommand.build());
	}
}
