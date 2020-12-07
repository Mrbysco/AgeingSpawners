package com.mrbysco.ageingspawners.util;

import com.mrbysco.ageingspawners.AgeingSpawners;
import com.mrbysco.ageingspawners.config.SpawnerConfig;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AgeingHelper {
	public static int getDefaultMax() {
		return SpawnerConfig.general.maxSpawnCount;
	}

	public static boolean blacklistContains(ResourceLocation registryName) {
		String[] blacklist = SpawnerConfig.blacklist.blacklist;
		List<ResourceLocation> blacklistList = new ArrayList<>();
		for (String s : blacklist) {
			blacklistList.add(new ResourceLocation(s));
		}
		return blacklistList.contains(registryName);
	}

	public static int getMaxSpawnCount(ResourceLocation registryName) {
		return AgeingSpawners.instance.whitelistMap.getOrDefault(registryName, getDefaultMax());
	}

	public static HashMap<ResourceLocation, Integer> getWhitelistMap() {
		HashMap<ResourceLocation, Integer> map = new HashMap<>();
		String[] whitelist = SpawnerConfig.whitelist.whitelist;
		for (String info : whitelist) {
			if (info.contains(";")) {
				String[] infoArray = info.split(";");
				if (infoArray.length > 1) {
					int max = getDefaultMax();
					if(NumberUtils.isParsable(infoArray[1])) {
						max = Integer.parseInt(infoArray[1]);
					} else {
						AgeingSpawners.logger.error(String.format("Unparsable number found after %s in the Ageing Spawners config Whitelist section", infoArray[0]));
					}
					map.put(new ResourceLocation(infoArray[0]), max);
				}
			}
		}
		return map;
	}
}
