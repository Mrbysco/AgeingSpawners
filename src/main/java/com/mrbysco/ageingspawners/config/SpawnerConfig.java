package com.mrbysco.ageingspawners.config;

import com.mrbysco.ageingspawners.AgeingSpawners;
import com.mrbysco.ageingspawners.util.AgeingHelper;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.config.ModConfigEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.List;

public class SpawnerConfig {
	public enum EnumAgeingMode {
		BLACKLIST,
		WHITELIST
	}

	public static class Common {
		public final ModConfigSpec.EnumValue<EnumAgeingMode> spawnerMode;
		public final BooleanValue playerPlacedOnly;

		public final IntValue whitelistMaxSpawnCount;
		public final ModConfigSpec.ConfigValue<List<? extends String>> whitelist;

		public final IntValue blacklistMaxSpawnCount;
		public final ModConfigSpec.ConfigValue<List<? extends String>> blacklist;


		Common(ModConfigSpec.Builder builder) {
			builder.comment("General settings")
					.push("General");

			spawnerMode = builder
					.comment("Decides whether the spawner is on blacklist, whitelist-only or player_placed mode [Default: WHITELIST]")
					.defineEnum("spawnerMode", EnumAgeingMode.WHITELIST);

			playerPlacedOnly = builder
					.comment("Decides whether only player placed spawners age [Default: false]")
					.define("playerPlacedOnly", false);

			builder.pop();
			builder.comment("Whitelist settings")
					.push("Whitelist");

			whitelistMaxSpawnCount = builder
					.comment("Decides default spawnCount of spawns a spawner can have in WHITELIST mode unless specified [Default: 20]")
					.defineInRange("whitelistMaxSpawnCount", 20, 1, Integer.MAX_VALUE);

			whitelist = builder
					.comment("Decides which mobs age a spawner (requires spawnerMode to be set to WHITELIST)",
							"[syntax: 'modid:entity;times' or 'modid:entity' ]",
							"[example: 'minecraft:pig;5' ]")
					.defineListAllowEmpty(Collections.singletonList("whitelist"), () -> Collections.singletonList(""), o -> (o instanceof String));

			builder.pop();
			builder.comment("Blacklist settings")
					.push("Blacklist");

			blacklistMaxSpawnCount = builder
					.comment("Decides the spawnCount of spawns a spawner can have in BLACKLIST mode [Default: 20]")
					.defineInRange("blacklistMaxSpawnCount", 20, 1, Integer.MAX_VALUE);

			blacklist = builder
					.comment("Decides which mobs don't age a spawner (requires spawnerMode to be set to BLACKLIST) \n" +
							"[syntax: 'modid:entity']")
					.defineListAllowEmpty(Collections.singletonList("blacklist"), () -> Collections.singletonList(""), o -> (o instanceof String));

			builder.pop();
		}
	}

	public static final ModConfigSpec commonSpec;
	public static final Common COMMON;

	static {
		final Pair<Common, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(Common::new);
		commonSpec = specPair.getRight();
		COMMON = specPair.getLeft();
	}

	@SubscribeEvent
	public static void onLoad(final ModConfigEvent.Loading configEvent) {
		AgeingSpawners.LOGGER.debug("Loaded Ageing Spawners' config file {}", configEvent.getConfig().getFileName());
		AgeingHelper.blacklistCache.clear();
		AgeingHelper.whitelistCache.clear();
	}

	@SubscribeEvent
	public static void onFileChange(final ModConfigEvent.Reloading configEvent) {
		AgeingSpawners.LOGGER.warn("Ageing Spawners' config just got changed on the file system!");
		AgeingHelper.blacklistCache.clear();
		AgeingHelper.whitelistCache.clear();
	}
}