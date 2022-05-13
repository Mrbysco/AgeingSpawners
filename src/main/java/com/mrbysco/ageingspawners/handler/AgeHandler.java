package com.mrbysco.ageingspawners.handler;

import com.mrbysco.ageingspawners.config.SpawnerConfig;
import com.mrbysco.ageingspawners.util.AgeingHelper;
import com.mrbysco.ageingspawners.util.AgeingWorldData;
import com.mrbysco.ageingspawners.util.AgeingWorldData.SpawnerInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
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
			handleSpawner(event.getWorld(), event.getSpawner(), event.getEntity());
		}
	}

	public static void handleSpawner(LevelAccessor level, BaseSpawner spawner, Entity entity) {
		ResourceLocation registryName = entity.getType().getRegistryName();
		switch (SpawnerConfig.COMMON.spawnerMode.get()) {
			case BLACKLIST -> handleBlacklist((Level) level, spawner, registryName);
			case WHITELIST -> handleWhitelist((Level) level, spawner, registryName);
		}
	}

	private static void handleBlacklist(Level world, BaseSpawner spawner, ResourceLocation registryName) {
		if (!AgeingHelper.blacklistContains(registryName)) {
			ageTheSpawner(world, spawner, SpawnerConfig.COMMON.blacklistMaxSpawnCount.get());
		} else {
			if (spawner.getSpawnerBlockEntity() != null) {
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

	private static void handleWhitelist(Level world, BaseSpawner spawner, ResourceLocation registryName) {
		if (AgeingHelper.whitelistContains(registryName)) {
			int maxSpawnCount = AgeingHelper.getMaxSpawnCount(registryName);
			ageTheSpawner(world, spawner, maxSpawnCount);
		} else {
			if (spawner.getSpawnerBlockEntity() != null) {
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

	private static void ageTheSpawner(Level world, BaseSpawner spawner, int maxCount) {
		if (spawner.getSpawnerBlockEntity() != null) {
			BlockPos pos = spawner.getSpawnerBlockEntity().getBlockPos();
			ResourceLocation dimensionLocation = world.dimension().location();
			AgeingWorldData worldData = AgeingWorldData.get(world);
			Map<BlockPos, SpawnerInfo> locationMap = worldData.getMapFromWorld(dimensionLocation);

			if (world.getBlockEntity(pos) != null && world.getBlockEntity(pos) instanceof SpawnerBlockEntity) {
				SpawnerInfo info = locationMap.getOrDefault(pos, new SpawnerInfo(0, false));
				boolean playerPlaced = info.playerPlaced();
				boolean flag = SpawnerConfig.COMMON.playerPlacedOnly.get();
				if (!flag || (flag && playerPlaced)) {
					int spawnCount = info.spawnCount();
					spawnCount++;
					if (spawnCount >= maxCount) {
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
		if (!event.getWorld().isClientSide() && event.getPlacedBlock().is(Blocks.SPAWNER) && event.getEntity() instanceof Player) {
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
		if (!event.getWorld().isClientSide()) {
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
