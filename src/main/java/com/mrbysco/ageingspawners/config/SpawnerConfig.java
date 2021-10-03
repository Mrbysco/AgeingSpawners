package com.mrbysco.ageingspawners.config;

import com.mrbysco.ageingspawners.AgeingSpawners;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.EnumValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.List;

public class SpawnerConfig {
	public enum EnumAgeingMode {
		BLACKLIST,
		WHITELIST
	}

	public static class Common {
		public final EnumValue<EnumAgeingMode> spawnerMode;

		public final IntValue whitelistMaxSpawnCount;
		public final ConfigValue<List<? extends String>> whitelist;

		public final IntValue blacklistMaxSpawnCount;
		public final ConfigValue<List<? extends String>> blacklist;


		Common(ForgeConfigSpec.Builder builder) {
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
					.defineListAllowEmpty(Collections.singletonList("whitelist"), () -> Collections.singletonList(""), o -> (o instanceof String));

			builder.pop();
			builder.comment("Blacklist settings")
					.push("Blacklist");

			blacklistMaxSpawnCount = builder
					.comment("Decides the amount of spawns a spawner can have in BLACKLIST mode [Default: 20]")
					.defineInRange("blacklistMaxSpawnCount", 20, 1, Integer.MAX_VALUE);

			blacklist = builder
					.comment("Decides which mobs don't age a spawner (requires spawnerMode to be set to BLACKLIST) \n" +
							"[syntax: 'modid:entity']")
					.defineListAllowEmpty(Collections.singletonList("blacklist"), () -> Collections.singletonList(""), o -> (o instanceof String));

			builder.pop();
		}
	}

	public static final ForgeConfigSpec commonSpec;
	public static final Common COMMON;

	static {
		final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
		commonSpec = specPair.getRight();
		COMMON = specPair.getLeft();
	}

	@SubscribeEvent
	public static void onLoad(final ModConfigEvent.Loading configEvent) {
		AgeingSpawners.logger.debug("Loaded Ageing Spawners' config file {}", configEvent.getConfig().getFileName());
	}

	@SubscribeEvent
	public static void onFileChange(final ModConfigEvent.Reloading configEvent) {
		AgeingSpawners.logger.debug("Ageing Spawners' config just got changed on the file system!");
	}
}