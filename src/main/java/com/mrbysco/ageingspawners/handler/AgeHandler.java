package com.mrbysco.ageingspawners.handler;

import com.mrbysco.ageingspawners.config.SpawnerConfig;
import com.mrbysco.ageingspawners.util.AgeingHelper;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;

public class AgeHandler {
	@SubscribeEvent
	public void SpawnEvent(LivingSpawnEvent.SpecialSpawn event) {
		if (!event.getWorld().isRemote && event.getSpawner() != null) {
			switch (SpawnerConfig.general.spawnerMode) {
				case BLACKLIST:
					handleBlacklist(event);
					break;
				case WHITELIST:
					handleWhitelist(event);
					break;
			}
		}
	}

	public void handleBlacklist(LivingSpawnEvent.SpecialSpawn event) {
		MobSpawnerBaseLogic spawnerLogic = event.getSpawner();
		if(!AgeingHelper.blacklistContains(spawnerLogic.getEntityId())) {
			this.ageTheSpawner(event, SpawnerConfig.blacklist.maxSpawnCount);
		}
	}

	public void handleWhitelist(LivingSpawnEvent.SpecialSpawn event) {
		MobSpawnerBaseLogic spawnerLogic = event.getSpawner();
		if(AgeingHelper.whitelistContains(spawnerLogic.getEntityId())) {
			int maxSpawnCount = AgeingHelper.getMaxSpawnCount(spawnerLogic.getEntityId());
			this.ageTheSpawner(event, maxSpawnCount);
		}
	}

	public static HashMap<BlockPos, Integer> spawnerMap = new HashMap<>();

	public void ageTheSpawner(LivingSpawnEvent.SpecialSpawn event, int maxCount) {
		MobSpawnerBaseLogic spawnerLogic = event.getSpawner();
		World world = event.getWorld();
		BlockPos spawnerPos = spawnerLogic.getSpawnerPosition();
		if(world.getTileEntity(spawnerPos) != null && world.getTileEntity(spawnerPos) instanceof TileEntityMobSpawner) {
			int spawnCount = 0;
			if(spawnerMap.containsKey(spawnerPos)) {
				spawnCount = spawnerMap.get(spawnerPos);
			}
			spawnCount++;
			if(spawnCount >= maxCount) {
				world.setBlockState(spawnerPos, Blocks.AIR.getDefaultState());
				spawnerMap.remove(spawnerPos);
			} else {
				spawnerMap.put(spawnerPos, Integer.valueOf(spawnCount));
			}
		}
	}
}
