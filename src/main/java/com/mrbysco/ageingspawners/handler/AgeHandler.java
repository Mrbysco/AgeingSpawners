package com.mrbysco.ageingspawners.handler;

import com.mrbysco.ageingspawners.AgeingSpawners;
import com.mrbysco.ageingspawners.config.SpawnerConfig;
import com.mrbysco.ageingspawners.util.AgeingHelper;
import com.mrbysco.ageingspawners.util.AgeingWorldData;
import com.mrbysco.ageingspawners.util.AgeingWorldData.SpawnerInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.level.BlockEvent.BreakEvent;
import net.minecraftforge.event.level.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;

public class AgeHandler {

	@SubscribeEvent
	public void SpawnEvent(LivingSpawnEvent.CheckSpawn event) {
		if (!event.getLevel().isClientSide() && event.isSpawner() && event.getLevel() instanceof ServerLevelAccessor serverLevelAccessor) {
			ServerLevel serverLevel = serverLevelAccessor.getLevel();
			if (serverLevel.getGameRules().getBoolean(AgeingSpawners.AGE_SPAWNERS_RULE)) {
				handleSpawner(serverLevel, event.getSpawner(), event.getEntity());
			}
		}
	}

	public static void handleSpawner(Level level, BaseSpawner spawner, Entity entity) {
		ResourceLocation registryName = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
		switch (SpawnerConfig.COMMON.spawnerMode.get()) {
			case BLACKLIST -> handleBlacklist(level, spawner, registryName);
			case WHITELIST -> handleWhitelist(level, spawner, registryName);
		}
	}

	private static void handleBlacklist(Level level, BaseSpawner spawner, ResourceLocation registryName) {
		if (!AgeingHelper.blacklistContains(registryName)) {
			ageTheSpawner(level, spawner, SpawnerConfig.COMMON.blacklistMaxSpawnCount.get());
		} else {
			if (spawner.getSpawnerBlockEntity() != null) {
				BlockPos pos = spawner.getSpawnerBlockEntity().getBlockPos();
				ResourceLocation dimensionLocation = level.dimension().location();
				AgeingWorldData worldData = AgeingWorldData.get(level);
				Map<BlockPos, SpawnerInfo> locationMap = worldData.getMapFromWorld(dimensionLocation);
				locationMap.remove(pos);
				worldData.setMapForWorld(dimensionLocation, locationMap);
				worldData.setDirty();
			}
		}
	}

	private static void handleWhitelist(Level level, BaseSpawner spawner, ResourceLocation registryName) {
		if (AgeingHelper.whitelistContains(registryName)) {
			int maxSpawnCount = AgeingHelper.getMaxSpawnCount(registryName);
			ageTheSpawner(level, spawner, maxSpawnCount);
		} else {
			if (spawner.getSpawnerBlockEntity() != null) {
				BlockPos pos = spawner.getSpawnerBlockEntity().getBlockPos();
				ResourceLocation dimensionLocation = level.dimension().location();
				AgeingWorldData worldData = AgeingWorldData.get(level);
				Map<BlockPos, SpawnerInfo> locationMap = worldData.getMapFromWorld(dimensionLocation);
				locationMap.remove(pos);
				worldData.setMapForWorld(dimensionLocation, locationMap);
				worldData.setDirty();
			}
		}
	}

	private static void ageTheSpawner(Level level, BaseSpawner spawner, int maxCount) {
		if (spawner.getSpawnerBlockEntity() != null) {
			BlockPos pos = spawner.getSpawnerBlockEntity().getBlockPos();
			ResourceLocation dimensionLocation = level.dimension().location();
			AgeingWorldData worldData = AgeingWorldData.get(level);
			Map<BlockPos, SpawnerInfo> locationMap = worldData.getMapFromWorld(dimensionLocation);

			if (level.getBlockEntity(pos) != null && level.getBlockEntity(pos) instanceof SpawnerBlockEntity) {
				SpawnerInfo info = locationMap.getOrDefault(pos, new SpawnerInfo(0, false));
				boolean playerPlaced = info.playerPlaced();
				boolean flag = SpawnerConfig.COMMON.playerPlacedOnly.get();
				if (!flag || (flag && playerPlaced)) {
					int spawnCount = info.spawnCount();
					spawnCount++;
					if (spawnCount >= maxCount) {
						level.removeBlock(pos, false);
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
		if (!event.getLevel().isClientSide() && event.getLevel() instanceof ServerLevelAccessor serverLevelAccessor &&
				event.getPlacedBlock().is(Blocks.SPAWNER) && event.getEntity() instanceof Player) {
			BlockPos pos = event.getPos();
			ServerLevel serverLevel = serverLevelAccessor.getLevel();
			ResourceLocation dimensionLocation = serverLevel.dimension().location();
			AgeingWorldData worldData = AgeingWorldData.get(serverLevel);
			Map<BlockPos, SpawnerInfo> locationMap = worldData.getMapFromWorld(dimensionLocation);
			SpawnerInfo info = locationMap.getOrDefault(pos, new SpawnerInfo(0, true));
			locationMap.put(pos, info);
			worldData.setMapForWorld(dimensionLocation, locationMap);
			worldData.setDirty();
		}
	}

	@SubscribeEvent
	public void breakEvent(BreakEvent event) {
		if (!event.getLevel().isClientSide() && event.getLevel() instanceof ServerLevelAccessor serverLevelAccessor) {
			BlockPos pos = event.getPos();
			ServerLevel serverLevel = serverLevelAccessor.getLevel();
			ResourceLocation dimensionLocation = serverLevel.dimension().location();
			AgeingWorldData worldData = AgeingWorldData.get(serverLevel);
			Map<BlockPos, SpawnerInfo> locationMap = worldData.getMapFromWorld(dimensionLocation);
			locationMap.remove(pos);
			worldData.setMapForWorld(dimensionLocation, locationMap);
			worldData.setDirty();
		}
	}
}
