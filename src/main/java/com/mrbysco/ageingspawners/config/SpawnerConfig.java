package com.mrbysco.ageingspawners.config;

import com.mrbysco.ageingspawners.AgeingSpawners;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.EnumValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class SpawnerConfig {
	public enum EnumAgeingMode {
		BLACKLIST,
		WHITELIST
	}

	public static class Server {
		public final EnumValue<EnumAgeingMode> spawnerMode;

		public final IntValue whitelistMaxSpawnCount;
		public final ConfigValue<List<? extends String>> whitelist;

		public final IntValue blacklistMaxSpawnCount;
		public final ConfigValue<List<? extends String>> blacklist;


		Server(ForgeConfigSpec.Builder builder) {
			builder.comment("General settings")
					.push("General");

			spawnerMode = builder
					.comment("Decides whether the spawner is on blacklist or whitelist-only mode [Default: WHITELIST]")
					.defineEnum("spawnerMode", EnumAgeingMode.WHITELIST);

			builder.pop();
			builder.comment("Whitelist settings")
					.push("Whitelist");

			whitelistMaxSpawnCount = builder
					.comment("Decides default amount of spawns a spawner can have in WHITELIST mode unless specified [Default: 20]")
					.defineInRange("whitelistMaxSpawnCount", 20, 1, Integer.MAX_VALUE);

			whitelist = builder
					.comment("Decides which mobs age a spawner (requires spawnerMode to be set to WHITELIST) \n" +
							"[syntax: 'modid:entity;times' or 'modid:entity' ] \n" +
							"[example: 'minecraft:pig;5' ]")
					.defineList("whitelist", new ArrayList<>(), o -> (o instanceof String));

			builder.pop();
			builder.comment("Blacklist settings")
					.push("Blacklist");

			blacklistMaxSpawnCount = builder
					.comment("Decides the amount of spawns a spawner can have in BLACKLIST mode [Default: 20]")
					.defineInRange("blacklistMaxSpawnCount", 20, 1, Integer.MAX_VALUE);

			blacklist = builder
					.comment("Decides which mobs don't age a spawner (requires spawnerMode to be set to BLACKLIST) \n" +
							"[syntax: 'modid:entity']")
					.defineList("blacklist", new ArrayList<>(), o -> (o instanceof String));

			builder.pop();
		}
	}

	public static final ForgeConfigSpec serverSpec;
	public static final SpawnerConfig.Server SERVER;

	static {
		final Pair<Server, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(SpawnerConfig.Server::new);
		serverSpec = specPair.getRight();
		SERVER = specPair.getLeft();
	}

	@SubscribeEvent
	public static void onLoad(final ModConfig.Loading configEvent) {
		AgeingSpawners.logger.debug("Loaded Ageing Spawners' config file {}", configEvent.getConfig().getFileName());
	}

	@SubscribeEvent
	public static void onFileChange(final ModConfig.ModConfigEvent configEvent) {
		AgeingSpawners.logger.debug("Ageing Spawners' config just got changed on the file system!");
	}
}