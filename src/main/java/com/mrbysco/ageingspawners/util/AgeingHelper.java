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
		List<ResourceLocation> blacklistList = new ArrayList<>();
		for(int i = 0; i < whitelist.length; i++) {
			String[] info = whitelist[i].split(";");
			if(info.length > 1) {
				blacklistList.add(new ResourceLocation(info[0]));
			}
		}
		return blacklistList.contains(registryName);
	}

	public static int getMaxSpawnCount(ResourceLocation registryName) {
		String[] whitelist = SpawnerConfig.whitelist.whitelist;
		for(int i = 0; i < whitelist.length; i++) {
			String[] info = whitelist[i].split(";");
			if(info.length > 1 && new ResourceLocation(info[0]).equals(registryName)) {
				return Integer.valueOf(info[1]);
			}
		}
		return 0;
	}
}
