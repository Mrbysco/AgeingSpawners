package com.mrbysco.ageingspawners.util;

import com.mrbysco.ageingspawners.config.SpawnerConfig;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AgeingHelper {

	public static final Map<String, Boolean> blacklistCache = new HashMap<>();
	public static final Map<String, Boolean> whitelistCache = new HashMap<>();

	public static boolean blacklistContains(ResourceLocation registryName) {
		return blacklistCache.computeIfAbsent(registryName.toString(), (value) -> {
			List<? extends String> blacklist = SpawnerConfig.COMMON.blacklist.get();
			return blacklist.contains(value);
		});
	}

	public static boolean whitelistContains(ResourceLocation registryName) {
		return whitelistCache.computeIfAbsent(registryName.toString(), (value) -> {
			List<? extends String> whitelist = SpawnerConfig.COMMON.whitelist.get();
			List<String> whitelistList = whitelist.stream().map(info -> {
				if (!info.isEmpty()) {
					if (info.contains(";")) {
						String[] infoArray = info.split(";");
						if (infoArray.length > 1) {
							return infoArray[0];
						}
					}
					return info;
				}
				return "";
			}).filter(info -> !info.isEmpty()).toList();
			return whitelistList.contains(value);
		});
	}

	public static int getMaxSpawnCount(ResourceLocation registryName) {
		List<? extends String> whitelist = SpawnerConfig.COMMON.whitelist.get();
		for (String info : whitelist) {
			if (!info.isEmpty()) {
				if (info.contains(";")) {
					String[] infoArray = info.split(";");
					if (infoArray.length > 1 && new ResourceLocation(infoArray[0]).equals(registryName)) {
						return NumberUtils.toInt(infoArray[1]);
					}
				}
			}
		}
		return SpawnerConfig.COMMON.whitelistMaxSpawnCount.get();
	}
}
