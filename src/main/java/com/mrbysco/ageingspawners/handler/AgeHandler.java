package com.mrbysco.ageingspawners.handler;

import com.mrbysco.ageingspawners.config.SpawnerConfig;
import com.mrbysco.ageingspawners.util.AgeingHelper;
import com.mrbysco.ageingspawners.util.AgeingWorldData;
import com.mrbysco.ageingspawners.util.AgeingWorldData.SpawnerInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.EntityPlaceEvent;
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
				Map<BlockPos, SpawnerInfo> locationMap = worldData.getMapFromWorld(dimensionLocation);
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
				Map<BlockPos, SpawnerInfo> locationMap = worldData.getMapFromWorld(dimensionLocation);
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
			Map<BlockPos, SpawnerInfo> locationMap = worldData.getMapFromWorld(dimensionLocation);

			if(world.getBlockEntity(pos) != null && world.getBlockEntity(pos) instanceof SpawnerBlockEntity) {
				SpawnerInfo info = locationMap.getOrDefault(pos, new SpawnerInfo(0, false));
				boolean playerPlaced = info.playerPlaced();
				boolean flag = SpawnerConfig.COMMON.playerPlacedOnly.get();
				if (!flag || (flag && playerPlaced)) {
					int spawnCount = info.spawnCount();
					spawnCount++;
					if(spawnCount >= maxCount) {
						world.removeBlock(pos, false);
						locationMap.remove(pos);
					} else {
						locationMap.put(pos, new SpawnerInfo(spawnCount, playerPlaced));
					}
					worldData.setMapForWorld(dimensionLocation, locationMap);
					worldData.setDirty();
				}
			}
		}
	}

	@SubscribeEvent
	public void placeEvent(EntityPlaceEvent event) {
		if(!event.getWorld().isClientSide() && event.getPlacedBlock().is(Blocks.SPAWNER) && event.getEntity() instanceof Player) {
			BlockPos pos = event.getPos();
			Level world = (Level) event.getWorld();
			ResourceLocation dimensionLocation = world.dimension().location();
			AgeingWorldData worldData = AgeingWorldData.get(world);
			Map<BlockPos, SpawnerInfo> locationMap = worldData.getMapFromWorld(dimensionLocation);
			SpawnerInfo info = locationMap.getOrDefault(pos, new SpawnerInfo(0, true));
			locationMap.put(pos, info);
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
			AgeingWorldData worldData = AgeingWorldData.get(world);
			Map<BlockPos, SpawnerInfo> locationMap = worldData.getMapFromWorld(dimensionLocation);
			locationMap.remove(pos);
			worldData.setMapForWorld(dimensionLocation, locationMap);
			worldData.setDirty();
		}
	}
}
