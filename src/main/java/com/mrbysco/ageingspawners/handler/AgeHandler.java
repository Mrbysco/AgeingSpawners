package com.mrbysco.ageingspawners.handler;

import com.mojang.datafixers.util.Either;
import com.mrbysco.ageingspawners.AgeingSpawners;
import com.mrbysco.ageingspawners.config.SpawnerConfig;
import com.mrbysco.ageingspawners.registry.ModAttachments;
import com.mrbysco.ageingspawners.util.AgeingHelper;
import com.mrbysco.ageingspawners.util.AgeingWorldData;
import com.mrbysco.ageingspawners.util.AgeingWorldData.SpawnerInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.level.BlockEvent.BreakEvent;
import net.neoforged.neoforge.event.level.BlockEvent.EntityPlaceEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

public class AgeHandler {

	@SubscribeEvent
	public void SpawnEvent(FinalizeSpawnEvent event) {
		if (!event.getLevel().isClientSide() && event.getSpawner() != null) {
			ServerLevel serverLevel = event.getLevel().getLevel();
			if (serverLevel.getGameRules().getBoolean(AgeingSpawners.AGE_SPAWNERS_RULE)) {
				Mob entity = event.getEntity();
				if (SpawnerConfig.COMMON.onlyCountKilled.get()) {
					entity.setData(ModAttachments.LINKED_SPAWNER, GlobalPos.of(entity.level().dimension(), event.getSpawner().left().orElseThrow().getBlockPos()));
				} else {
					Either<BlockEntity, Entity> spawner = event.getSpawner();
					Optional<BlockEntity> optionalBlockEntity = spawner.left();
					@Nullable BlockPos spawnerPos = optionalBlockEntity.isPresent() ? optionalBlockEntity.orElseThrow().getBlockPos() : null;
					handleSpawner(serverLevel, spawnerPos, entity);
				}
			}
		}
	}

	public static void handleSpawner(Level level, @Nullable Either<BlockEntity, Entity> spawner, Entity entity) {
		if (spawner == null) return;
		Optional<BlockEntity> optionalBlockEntity = spawner.left();
		@Nullable BlockPos spawnerPos = optionalBlockEntity.isPresent() ? optionalBlockEntity.orElseThrow().getBlockPos() : null;
		handleSpawner(level, spawnerPos, entity);
	}

	public static void handleSpawner(Level level, @Nullable BlockPos spawnerPos, Entity entity) {
		ResourceLocation registryName = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
		switch (SpawnerConfig.COMMON.spawnerMode.get()) {
			case BLACKLIST -> handleBlacklist(level, spawnerPos, registryName);
			case WHITELIST -> handleWhitelist(level, spawnerPos, registryName);
		}
	}

	/**
	 * Handles the spawner when the spawner mode is set to BLACKLIST.
	 */
	private static void handleBlacklist(Level level, BlockPos spawnerPos, ResourceLocation registryName) {
		if (!AgeingHelper.blacklistContains(registryName)) {
			ageTheSpawner(level, spawnerPos, SpawnerConfig.COMMON.blacklistMaxSpawnCount.get());
		} else {
			handleElse(level, spawnerPos);
		}
	}

	/**
	 * Handles the spawner when the spawner mode is set to WHITELIST.
	 */
	private static void handleWhitelist(Level level, BlockPos spawnerPos, ResourceLocation registryName) {
		if (AgeingHelper.whitelistContains(registryName)) {
			int maxSpawnCount = AgeingHelper.getMaxSpawnCount(registryName);
			ageTheSpawner(level, spawnerPos, maxSpawnCount);
		} else {
			handleElse(level, spawnerPos);
		}
	}

	/**
	 * Handles the spawner when it does not meet the conditions of the blacklist or whitelist.
	 */
	private static void handleElse(Level level, BlockPos spawnerPos) {
		if (spawnerPos != null) {
			ResourceLocation dimensionLocation = level.dimension().location();
			AgeingWorldData worldData = AgeingWorldData.get(level);
			Map<BlockPos, SpawnerInfo> locationMap = worldData.getMapFromWorld(dimensionLocation);
			locationMap.remove(spawnerPos);
			worldData.setMapForWorld(dimensionLocation, locationMap);
			worldData.setDirty();
		}
	}

	/**
	 * Ages the spawner by incrementing its spawn count and removing it if it reaches the maximum count.
	 */
	private static void ageTheSpawner(Level level, BlockPos spawnerPos, int maxCount) {
		if (spawnerPos != null) {
			ResourceLocation dimensionLocation = level.dimension().location();
			AgeingWorldData worldData = AgeingWorldData.get(level);
			Map<BlockPos, SpawnerInfo> locationMap = worldData.getMapFromWorld(dimensionLocation);

			if (level.getBlockEntity(spawnerPos) != null && level.getBlockEntity(spawnerPos) instanceof SpawnerBlockEntity) {
				SpawnerInfo info = locationMap.getOrDefault(spawnerPos, new SpawnerInfo(0, false));
				boolean playerPlaced = info.playerPlaced();
				boolean flag = SpawnerConfig.COMMON.playerPlacedOnly.get();
				if (!flag || (flag && playerPlaced)) {
					int spawnCount = info.spawnCount();
					spawnCount++;
					if (spawnCount >= maxCount) {
						level.removeBlock(spawnerPos, false);
						locationMap.remove(spawnerPos);
					} else {
						locationMap.put(spawnerPos, new SpawnerInfo(spawnCount, playerPlaced));
					}
					worldData.setMapForWorld(dimensionLocation, locationMap);
					worldData.setDirty();
				}
			}
		}
	}

	@SubscribeEvent
	public void placeEvent(EntityPlaceEvent event) {
		if (!event.getLevel().isClientSide() && event.getLevel() instanceof ServerLevelAccessor serverLevelAccessor && event.getPlacedBlock().is(Blocks.SPAWNER) && event.getEntity() instanceof Player) {
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

	@SubscribeEvent
	public void killEvent(LivingDeathEvent event) {
		LivingEntity livingEntity = event.getEntity();
		Level level = livingEntity.level();
		if (!level.isClientSide() && livingEntity.hasData(ModAttachments.LINKED_SPAWNER)) {
			GlobalPos spawnerPos = livingEntity.getData(ModAttachments.LINKED_SPAWNER);
			MinecraftServer server = level.getServer();
			if (server != null) {
				ServerLevel serverLevel = server.getLevel(spawnerPos.dimension());
				if (serverLevel == null) return;
				handleSpawner(serverLevel, spawnerPos.pos(), livingEntity);
			}
		}
	}
}
