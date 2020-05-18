package com.mrbysco.ageingspawners.handler;

import com.mrbysco.ageingspawners.AgeingSpawners;
import com.mrbysco.ageingspawners.config.SpawnerConfig;
import com.mrbysco.ageingspawners.util.AgeingHelper;
import net.minecraft.block.Blocks;
import net.minecraft.entity.SpawnReason;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.spawner.AbstractSpawner;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;

public class AgeHandler {

	@SubscribeEvent
	public void SpawnEvent(LivingSpawnEvent.CheckSpawn event) {
		if (!event.getWorld().isRemote() && event.isSpawner()) {
			ResourceLocation registryName = event.getEntityLiving().getType().getRegistryName();
			switch (SpawnerConfig.SERVER.spawnerMode.get()) {
				case BLACKLIST:
					handleBlacklist(event, registryName);
					break;
				case WHITELIST:
					handleWhitelist(event, registryName);
					break;
			}
		}
	}

	public static HashMap<BlockPos, Integer> spawnerMap = new HashMap<>();

	public void handleBlacklist(LivingSpawnEvent.CheckSpawn event, ResourceLocation registryName) {
		AbstractSpawner spawner = event.getSpawner();
		if(!AgeingHelper.blacklistContains(registryName)) {
			this.ageTheSpawner(event, SpawnerConfig.SERVER.blacklistMaxSpawnCount.get());
		} else {
			BlockPos pos = spawner.getSpawnerPosition();
			if(spawnerMap.containsKey(pos)) {
				spawnerMap.remove(pos);
			}
		}
	}

	public void handleWhitelist(LivingSpawnEvent.CheckSpawn event, ResourceLocation registryName) {
		AbstractSpawner spawner = event.getSpawner();
		if(AgeingHelper.whitelistContains(registryName)) {
			int maxSpawnCount = AgeingHelper.getMaxSpawnCount(registryName);
			this.ageTheSpawner(event, maxSpawnCount);
		} else {
			BlockPos pos = spawner.getSpawnerPosition();
			if(spawnerMap.containsKey(pos)) {
				spawnerMap.remove(pos);
			}
		}
	}

	public void ageTheSpawner(LivingSpawnEvent.CheckSpawn event, int maxCount) {
		AbstractSpawner spawner = event.getSpawner();
		IWorld world = event.getWorld();
		BlockPos spawnerPos = spawner.getSpawnerPosition();
		if(world.getTileEntity(spawnerPos) != null && world.getTileEntity(spawnerPos) instanceof MobSpawnerTileEntity) {
			int spawnCount = spawnerMap.containsKey(spawnerPos) ? spawnerMap.get(spawnerPos) : 0;
			spawnCount++;
			if(spawnCount >= maxCount) {
				world.setBlockState(spawnerPos, Blocks.AIR.getDefaultState(), 3);
				spawnerMap.remove(spawnerPos);
			} else {
				spawnerMap.put(spawnerPos, Integer.valueOf(spawnCount));
			}
		}
	}

	@SubscribeEvent
	public void breakEvent(BreakEvent event) {
		if(!event.getWorld().isRemote()) {
			BlockPos pos = event.getPos();
			if(!spawnerMap.isEmpty() && spawnerMap.containsKey(pos)) {
				spawnerMap.remove(pos);
			}
		}
	}
}
