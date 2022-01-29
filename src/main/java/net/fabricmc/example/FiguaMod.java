package net.fabricmc.example;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.example.lua.LuaManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The primary class of the mod. It initializes the mod for fabric,
 * and also keeps track of various global things for us.
 * Has some utility methods as well.
 */
public class FiguaMod implements ClientModInitializer {

	public static final String MODID = "figua";
	public static final Logger LOGGER = LogManager.getLogger(MODID);


	@Override
	public void onInitializeClient() {
		LOGGER.info("Start Figua Initialization");
		LOGGER.info("Setting up Lua natives...");
		LuaManager.setupLuaNatives();
		LOGGER.info("Lua natives complete.");
		ClientTickEvents.END_CLIENT_TICK.register(FiguaMod::tick);
	}

	//TODO: make an actual tick function that isn't just for my purposes developing in single player
	private static void tick(MinecraftClient minecraftClient) {

	}

	/**
	 * Shorthand for getting the path to the resources/assets/figua folder.
	 * @return The path to the figua assets folder.
	 */
	public static Path getAssetPath() {
		return FabricLoader.getInstance().getModContainer(MODID).get().getRootPath().resolve("assets/figua");
	}

	/**
	 * Gets the path to the figua folder inside .minecraft.
	 * TODO: Add config, make path configurable
	 */
	public static Path getFiguaPath() {
		Path dir = FabricLoader.getInstance().getGameDir().normalize().resolve("figua");
		try {
			Files.createDirectories(dir);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return dir;
	}

}
