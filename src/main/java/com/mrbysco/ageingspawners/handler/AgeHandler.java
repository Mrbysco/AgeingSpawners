package com.mrbysco.ageingspawners.handler;

import com.mrbysco.ageingspawners.config.SpawnerConfig;
import com.mrbysco.ageingspawners.util.AgeingHelper;
import com.mrbysco.ageingspawners.util.AgeingWorldData;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.spawner.AbstractSpawner;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Map;

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
						handleBlacklist((World)world, spawner, registryName);
						break;
					case WHITELIST:
						handleWhitelist((World)world, spawner, registryName);
						break;
				}
			}
		}
	}


	public void handleBlacklist(World world, AbstractSpawner spawner, ResourceLocation registryName) {
		if(!AgeingHelper.blacklistContains(registryName)) {
			this.ageTheSpawner(world, spawner, SpawnerConfig.SERVER.blacklistMaxSpawnCount.get());
		} else {
			BlockPos pos = spawner.getSpawnerPosition();
			ResourceLocation dimensionLocation = world.getDimensionKey().getLocation();
			AgeingWorldData worldData = AgeingWorldData.get(world);
			Map<BlockPos, Integer> locationMap = worldData.getMapFromWorld(dimensionLocation);
			locationMap.remove(pos);
			worldData.setMapForWorld(dimensionLocation, locationMap);
			worldData.markDirty();
		}
	}

	public void handleWhitelist(World world, AbstractSpawner spawner, ResourceLocation registryName) {
		if(AgeingHelper.whitelistContains(registryName)) {
			int maxSpawnCount = AgeingHelper.getMaxSpawnCount(registryName);
			this.ageTheSpawner(world, spawner, maxSpawnCount);
		} else {
			BlockPos pos = spawner.getSpawnerPosition();
			ResourceLocation dimensionLocation = world.getDimensionKey().getLocation();
			AgeingWorldData worldData = AgeingWorldData.get(world);
			Map<BlockPos, Integer> locationMap = worldData.getMapFromWorld(dimensionLocation);
			locationMap.remove(pos);
			worldData.setMapForWorld(dimensionLocation, locationMap);
			worldData.markDirty();
		}
	}

	public void ageTheSpawner(World world, AbstractSpawner spawner, int maxCount) {
		BlockPos spawnerPos = spawner.getSpawnerPosition();
		ResourceLocation dimensionLocation = world.getDimensionKey().getLocation();
		AgeingWorldData worldData = AgeingWorldData.get(world);
		Map<BlockPos, Integer> locationMap = worldData.getMapFromWorld(dimensionLocation);

		if(world.getTileEntity(spawnerPos) != null && world.getTileEntity(spawnerPos) instanceof MobSpawnerTileEntity) {
			int spawnCount = locationMap.getOrDefault(spawnerPos, 0);
			spawnCount++;
			if(spawnCount >= maxCount) {
				world.removeBlock(spawnerPos, false);
				locationMap.remove(spawnerPos);
			} else {
				locationMap.put(spawnerPos, spawnCount);
			}
		}
		worldData.setMapForWorld(dimensionLocation, locationMap);
		worldData.markDirty();
	}

	@SubscribeEvent
	public void breakEvent(BreakEvent event) {
		if(!event.getWorld().isRemote()) {
			BlockPos pos = event.getPos();
			World world = (World) event.getWorld();
			ResourceLocation dimensionLocation = world.getDimensionKey().getLocation();
			Map<BlockPos, Integer> locationMap = AgeingWorldData.get(world).getMapFromWorld(dimensionLocation);
			locationMap.remove(pos);
		}
	}
}
