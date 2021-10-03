package com.mrbysco.ageingspawners.util;

import com.mrbysco.ageingspawners.config.SpawnerConfig;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.List;

public class AgeingHelper {
	public static boolean blacklistContains(ResourceLocation registryName) {
		List<? extends String> blacklist = SpawnerConfig.COMMON.blacklist.get();
		List<ResourceLocation> blacklistList = new ArrayList<>();
		for (String s : blacklist) {
			if(!s.isEmpty()) {
				blacklistList.add(new ResourceLocation(s));
			}
		}
		return blacklistList.contains(registryName);
	}

	public static boolean whitelistContains(ResourceLocation registryName) {
		List<? extends String> whitelist = SpawnerConfig.COMMON.whitelist.get();
		List<ResourceLocation> whitelistList = new ArrayList<>();
		for (String info : whitelist) {
			if(!info.isEmpty()) {
				if (info.contains(";")) {
					String[] infoArray = info.split(";");
					if (infoArray.length > 1) {
						whitelistList.add(new ResourceLocation(infoArray[0]));
					}
				} else {
					whitelistList.add(new ResourceLocation(info));
				}
			}
		}
		return whitelistList.contains(registryName);
	}

	public static int getMaxSpawnCount(ResourceLocation registryName) {
		List<? extends String> whitelist = SpawnerConfig.COMMON.whitelist.get();
		for (String info : whitelist) {
			if(!info.isEmpty()) {
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
