package com.mrbysco.ageingspawners.config;

import com.mrbysco.ageingspawners.Reference;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = Reference.MOD_ID)
@Config.LangKey("ageingspawners.config.title")
public class SpawnerConfig {

	@Config.Comment({"General settings"})
	public static General general = new General();

	@Config.Comment({"Whitelist settings"})
	public static Whitelist whitelist = new Whitelist();

	@Config.Comment({"Blacklist settings"})
	public static Blacklist blacklist = new Blacklist();

	public enum EnumAgeingMode {
		BLACKLIST,
		WHITELIST
	}

	public static class General{
		@Config.Comment("Decides whether the spawner is on blacklist or whitelist-only mode [Default: WHITELIST]")
		public EnumAgeingMode spawnerMode = EnumAgeingMode.WHITELIST;
	}

	public static class Whitelist{
		@Config.Comment("Decides which mobs age a spawner (requires spawnerMode to be set to WHITELIST) \n" +
				"[syntax: 'modid:entity;times' ]" +
				"[example: 'minecraft:pig|3' ]")
		@Config.Name("Whitelist")
		public String[] whitelist = new String[]{};
	}

	public static class Blacklist{
		@Config.Comment("Decides the amount of spawns a spawner can have in BLACKLIST mode [syntax: modid:entity]")
		@Config.Name("Max Spawn Count")
		public int maxSpawnCount = 20;

		@Config.Comment("Decides which mobs don't age a spawner (requires spawnerMode to be set to BLACKLIST) [syntax: modid:entity]")
		@Config.Name("Blacklist")
		public String[] blacklist = new String[]{};
	}

	@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
	private static class EventHandler {

		@SubscribeEvent
		public static void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event) {
			if (event.getModID().equals(Reference.MOD_ID)) {
				ConfigManager.sync(Reference.MOD_ID, Config.Type.INSTANCE);
			}
		}
	}
}