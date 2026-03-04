package com.mrbysco.ageingspawners.handler;

import com.mojang.datafixers.util.Either;
import com.mrbysco.ageingspawners.AgeingSpawners;
import com.mrbysco.ageingspawners.config.SpawnerConfig;
import com.mrbysco.ageingspawners.util.AgeingHelper;
import com.mrbysco.ageingspawners.util.AgeingWorldData;
import com.mrbysco.ageingspawners.util.AgeingWorldData.SpawnerInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.level.BlockEvent.BreakEvent;
import net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent;

import java.util.Map;

public class AgeHandler {

	@SubscribeEvent
	public void SpawnEvent(FinalizeSpawnEvent event) {
		if (!event.getLevel().isClientSide() && event.getSpawner() != null) {
			ServerLevel serverLevel = event.getLevel().getLevel();
			if (serverLevel.getGameRules().get(AgeingSpawners.AGE_SPAWNERS_RULE.get())) {
				handleSpawner(serverLevel, event.getSpawner(), event.getEntity());
			}
		}
	}

	public static void handleSpawner(Level level, Either<BlockEntity, Entity> spawner, Entity entity) {
		Identifier registryName = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
		switch (SpawnerConfig.COMMON.spawnerMode.get()) {
			case BLACKLIST -> handleBlacklist(level, spawner, registryName);
			case WHITELIST -> handleWhitelist(level, spawner, registryName);
		}
	}

	private static void handleBlacklist(Level level, Either<BlockEntity, Entity> spawner, Identifier registryName) {
		if (!AgeingHelper.blacklistContains(registryName)) {
			ageTheSpawner(level, spawner, SpawnerConfig.COMMON.blacklistMaxSpawnCount.get());
		} else {
			if (spawner.left().isPresent()) {
				BlockPos pos = spawner.left().orElseThrow().getBlockPos();
				Identifier dimensionLocation = level.dimension().identifier();
				AgeingWorldData worldData = AgeingWorldData.get(level);
				Map<BlockPos, SpawnerInfo> locationMap = worldData.getMapFromWorld(dimensionLocation);
				locationMap.remove(pos);
				worldData.setMapForWorld(dimensionLocation, locationMap);
				worldData.setDirty();
			}
		}
	}

	private static void handleWhitelist(Level level, Either<BlockEntity, Entity> spawner, Identifier registryName) {
		if (AgeingHelper.whitelistContains(registryName)) {
			int maxSpawnCount = AgeingHelper.getMaxSpawnCount(registryName);
			ageTheSpawner(level, spawner, maxSpawnCount);
		} else {
			if (spawner.left().isPresent()) {
				BlockPos pos = spawner.left().orElseThrow().getBlockPos();
				Identifier dimensionLocation = level.dimension().identifier();
				AgeingWorldData worldData = AgeingWorldData.get(level);
				Map<BlockPos, SpawnerInfo> locationMap = worldData.getMapFromWorld(dimensionLocation);
				locationMap.remove(pos);
				worldData.setMapForWorld(dimensionLocation, locationMap);
				worldData.setDirty();
			}
		}
	}

	private static void ageTheSpawner(Level level, Either<BlockEntity, Entity> spawner, int maxCount) {
		if (spawner.left().isPresent()) {
			BlockPos pos = spawner.left().orElseThrow().getBlockPos();
			Identifier dimensionLocation = level.dimension().identifier();
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
			Identifier dimensionLocation = serverLevel.dimension().identifier();
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
			Identifier dimensionLocation = serverLevel.dimension().identifier();
			AgeingWorldData worldData = AgeingWorldData.get(serverLevel);
			Map<BlockPos, SpawnerInfo> locationMap = worldData.getMapFromWorld(dimensionLocation);
			locationMap.remove(pos);
			worldData.setMapForWorld(dimensionLocation, locationMap);
			worldData.setDirty();
		}
	}
}
