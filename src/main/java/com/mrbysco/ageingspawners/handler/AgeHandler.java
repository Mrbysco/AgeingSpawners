package com.mrbysco.ageingspawners.handler;

import com.mrbysco.ageingspawners.config.SpawnerConfig;
import com.mrbysco.ageingspawners.util.AgeingHelper;
import net.minecraft.block.Blocks;
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
			AbstractSpawner spawner = event.getSpawner();
			IWorld world = event.getWorld();
			if(spawner != null) {
				ResourceLocation registryName = event.getEntityLiving().getType().getRegistryName();
				switch (SpawnerConfig.SERVER.spawnerMode.get()) {
					case BLACKLIST:
						handleBlacklist(world, spawner, registryName);
						break;
					case WHITELIST:
						handleWhitelist(world, spawner, registryName);
						break;
				}
			}
		}
	}

	public static HashMap<BlockPos, Integer> spawnerMap = new HashMap<>();

	public void handleBlacklist(IWorld world, AbstractSpawner spawner, ResourceLocation registryName) {
		if(!AgeingHelper.blacklistContains(registryName)) {
			this.ageTheSpawner(world, spawner, SpawnerConfig.SERVER.blacklistMaxSpawnCount.get());
		} else {
			BlockPos pos = spawner.getSpawnerPosition();
			spawnerMap.remove(pos);
		}
	}

	public void handleWhitelist(IWorld world, AbstractSpawner spawner, ResourceLocation registryName) {
		if(AgeingHelper.whitelistContains(registryName)) {
			int maxSpawnCount = AgeingHelper.getMaxSpawnCount(registryName);
			this.ageTheSpawner(world, spawner, maxSpawnCount);
		} else {
			BlockPos pos = spawner.getSpawnerPosition();
			spawnerMap.remove(pos);
		}
	}

	public void ageTheSpawner(IWorld world, AbstractSpawner spawner, int maxCount) {
		BlockPos spawnerPos = spawner.getSpawnerPosition();
		if(world.getTileEntity(spawnerPos) != null && world.getTileEntity(spawnerPos) instanceof MobSpawnerTileEntity) {
			int spawnCount = spawnerMap.getOrDefault(spawnerPos, 0);
			spawnCount++;
			if(spawnCount >= maxCount) {
				world.setBlockState(spawnerPos, Blocks.AIR.getDefaultState(), 3);
				spawnerMap.remove(spawnerPos);
			} else {
				spawnerMap.put(spawnerPos, spawnCount);
			}
		}
	}

	@SubscribeEvent
	public void breakEvent(BreakEvent event) {
		if(!event.getWorld().isRemote()) {
			BlockPos pos = event.getPos();
			if(!spawnerMap.isEmpty()) {
				spawnerMap.remove(pos);
			}
		}
	}
}
