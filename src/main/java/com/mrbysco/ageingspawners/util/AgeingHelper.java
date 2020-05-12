package com.mrbysco.ageingspawners.util;

import com.mrbysco.ageingspawners.config.SpawnerConfig;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class AgeingHelper {
	public static boolean blacklistContains(ResourceLocation registryName) {
		String[] blacklist = SpawnerConfig.blacklist.blacklist;
		List<ResourceLocation> blacklistList = new ArrayList<>();
		for(int i = 0; i < blacklist.length; i++) {
			blacklistList.add(new ResourceLocation(blacklist[i]));
		}
		return blacklistList.contains(registryName);
	}

	public static boolean whitelistContains(ResourceLocation registryName) {
		String[] whitelist = SpawnerConfig.whitelist.whitelist;
		List<ResourceLocation> whitelistList = new ArrayList<>();
		for(int i = 0; i < whitelist.length; i++) {
			String info = whitelist[i];

			if(info.contains(";")) {
				String[] infoArray = info.split(";");
				if(infoArray.length > 1) {
					whitelistList.add(new ResourceLocation(infoArray[0]));
				}
			} else {
				whitelistList.add(new ResourceLocation(info));
			}
		}
		return whitelistList.contains(registryName);
	}

	public static int getMaxSpawnCount(ResourceLocation registryName) {
		String[] whitelist = SpawnerConfig.whitelist.whitelist;
		for(int i = 0; i < whitelist.length; i++) {
			String info = whitelist[i];

			if(info.contains(";")) {
				String[] infoArray = info.split(";");
				if(infoArray.length > 1 && new ResourceLocation(infoArray[0]).equals(registryName)) {
					return Integer.valueOf(infoArray[1]);
				}
			}
		}
		return SpawnerConfig.whitelist.maxSpawnCount;
	}
}
