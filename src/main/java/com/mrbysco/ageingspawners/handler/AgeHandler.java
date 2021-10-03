package com.mrbysco.ageingspawners.handler;

import com.mrbysco.ageingspawners.config.SpawnerConfig;
import com.mrbysco.ageingspawners.util.AgeingHelper;
import com.mrbysco.ageingspawners.util.AgeingWorldData;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BaseSpawner;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Map;

public class AgeHandler {

	@SubscribeEvent
	public void SpawnEvent(LivingSpawnEvent.CheckSpawn event) {
		if (!event.getWorld().isClientSide() && event.isSpawner()) {
			BaseSpawner spawner = event.getSpawner();
			LevelAccessor world = event.getWorld();
			if(event.isSpawner()) {
				ResourceLocation registryName = event.getEntityLiving().getType().getRegistryName();
				switch (SpawnerConfig.COMMON.spawnerMode.get()) {
					case BLACKLIST -> handleBlacklist((Level) world, spawner, registryName);
					case WHITELIST -> handleWhitelist((Level) world, spawner, registryName);
				}
			}
		}
	}

	public void handleBlacklist(Level world, BaseSpawner spawner, ResourceLocation registryName) {
		if(!AgeingHelper.blacklistContains(registryName)) {
			this.ageTheSpawner(world, spawner, SpawnerConfig.COMMON.blacklistMaxSpawnCount.get());
		} else {
			if(spawner.getSpawnerBlockEntity() != null) {
				BlockPos pos = spawner.getSpawnerBlockEntity().getBlockPos();
				ResourceLocation dimensionLocation = world.dimension().location();
				AgeingWorldData worldData = AgeingWorldData.get(world);
				Map<BlockPos, Integer> locationMap = worldData.getMapFromWorld(dimensionLocation);
				locationMap.remove(pos);
				worldData.setMapForWorld(dimensionLocation, locationMap);
				worldData.setDirty();
			}
		}
	}

	public void handleWhitelist(Level world, BaseSpawner spawner, ResourceLocation registryName) {
		if(AgeingHelper.whitelistContains(registryName)) {
			int maxSpawnCount = AgeingHelper.getMaxSpawnCount(registryName);
			this.ageTheSpawner(world, spawner, maxSpawnCount);
		} else {
			if(spawner.getSpawnerBlockEntity() != null) {
				BlockPos pos = spawner.getSpawnerBlockEntity().getBlockPos();
				ResourceLocation dimensionLocation = world.dimension().location();
				AgeingWorldData worldData = AgeingWorldData.get(world);
				Map<BlockPos, Integer> locationMap = worldData.getMapFromWorld(dimensionLocation);
				locationMap.remove(pos);
				worldData.setMapForWorld(dimensionLocation, locationMap);
				worldData.setDirty();
			}
		}
	}

	public void ageTheSpawner(Level world, BaseSpawner spawner, int maxCount) {
		if(spawner.getSpawnerBlockEntity() != null) {
			BlockPos pos = spawner.getSpawnerBlockEntity().getBlockPos();
			ResourceLocation dimensionLocation = world.dimension().location();
			AgeingWorldData worldData = AgeingWorldData.get(world);
			Map<BlockPos, Integer> locationMap = worldData.getMapFromWorld(dimensionLocation);

			if(world.getBlockEntity(pos) != null && world.getBlockEntity(pos) instanceof SpawnerBlockEntity) {
				int spawnCount = locationMap.getOrDefault(pos, 0);
				spawnCount++;
				if(spawnCount >= maxCount) {
					world.removeBlock(pos, false);
					locationMap.remove(pos);
				} else {
					locationMap.put(pos, spawnCount);
				}
			}
			worldData.setMapForWorld(dimensionLocation, locationMap);
			worldData.setDirty();
		}
	}

	@SubscribeEvent
	public void breakEvent(BreakEvent event) {
		if(!event.getWorld().isClientSide()) {
			BlockPos pos = event.getPos();
			Level world = (Level) event.getWorld();
			ResourceLocation dimensionLocation = world.dimension().location();
			Map<BlockPos, Integer> locationMap = AgeingWorldData.get(world).getMapFromWorld(dimensionLocation);
			locationMap.remove(pos);
		}
	}
}
