package com.mrbysco.ageingspawners.handler;

import com.mrbysco.ageingspawners.AgeingSpawners;
import com.mrbysco.ageingspawners.config.SpawnerConfig;
import com.mrbysco.ageingspawners.util.AgeingHelper;
import com.mrbysco.ageingspawners.util.AgeingWorldData;
import com.mrbysco.ageingspawners.util.SpawnerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Map;

public class AgeHandler {
	@SubscribeEvent
	public void SpawnEvent(LivingSpawnEvent.SpecialSpawn event) {
		if (!event.getWorld().isRemote && event.getSpawner() != null && event.getWorld() instanceof WorldServer) {
			WorldServer serverWorld = (WorldServer) event.getWorld();
			if (serverWorld.getGameRules().getBoolean(AgeingSpawners.AGE_SPAWNERS_RULE)) {
				handleSpawner(serverWorld, event.getSpawner(), event.getEntity());
			}
		}
	}

	public static void handleSpawner(WorldServer level, MobSpawnerBaseLogic spawner, Entity entity) {
		ResourceLocation registryName = EntityList.getKey(entity);
		if (SpawnerConfig.general.spawnerMode == SpawnerConfig.EnumAgeingMode.BLACKLIST) {
			handleBlacklist(level, spawner, registryName);
		} else {
			handleWhitelist(level, spawner, registryName);
		}
	}

	private static void handleBlacklist(WorldServer world, MobSpawnerBaseLogic spawner, ResourceLocation registryName) {
		if (!AgeingHelper.blacklistContains(registryName)) {
			ageTheSpawner(world, spawner, SpawnerConfig.blacklist.blacklistMaxSpawnCount);
		} else {
			if (spawner != null) {
				BlockPos pos = spawner.getSpawnerPosition();
				int dimension = world.provider.getDimension();
				AgeingWorldData worldData = AgeingWorldData.get(DimensionManager.getWorld(0));
				Map<BlockPos, SpawnerInfo> locationMap = worldData.getMapFromDimension(dimension);
				locationMap.remove(pos);
				worldData.setMapForDimension(dimension, locationMap);
				worldData.setDirty(true);
			}
		}
	}

	private static void handleWhitelist(WorldServer world, MobSpawnerBaseLogic spawner, ResourceLocation registryName) {
		if (AgeingHelper.whitelistContains(registryName)) {
			int maxSpawnCount = AgeingHelper.getMaxSpawnCount(registryName);
			ageTheSpawner(world, spawner, maxSpawnCount);
		} else {
			if (spawner != null) {
				BlockPos pos = spawner.getSpawnerPosition();
				int dimension = world.provider.getDimension();
				AgeingWorldData worldData = AgeingWorldData.get(DimensionManager.getWorld(0));
				Map<BlockPos, SpawnerInfo> locationMap = worldData.getMapFromDimension(dimension);
				locationMap.remove(pos);
				worldData.setMapForDimension(dimension, locationMap);
				worldData.setDirty(true);
			}
		}
	}

	private static void ageTheSpawner(WorldServer world, MobSpawnerBaseLogic spawner, int maxCount) {
		if (spawner != null) {
			BlockPos pos = spawner.getSpawnerPosition();
			int dimension = world.provider.getDimension();
			AgeingWorldData worldData = AgeingWorldData.get(DimensionManager.getWorld(0));
			Map<BlockPos, SpawnerInfo> locationMap = worldData.getDimensionMap(dimension);

			if (world.getTileEntity(pos) != null && world.getTileEntity(pos) instanceof TileEntityMobSpawner) {
				SpawnerInfo info = locationMap.getOrDefault(pos, new SpawnerInfo(0, false));
				boolean playerPlaced = info.playerPlaced();
				boolean flag = SpawnerConfig.general.playerPlacedOnly;
				if (!flag || (flag && playerPlaced)) {
					int spawnCount = info.spawnCount();
					spawnCount++;
					if (spawnCount >= maxCount) {
						world.destroyBlock(pos, false);
						locationMap.remove(pos);
					} else {
						locationMap.put(pos, new SpawnerInfo(spawnCount, playerPlaced));
					}
					worldData.setMapForDimension(dimension, locationMap);
					worldData.setDirty(true);
				}
			}
		}
	}

	@SubscribeEvent
	public void placeEvent(BlockEvent.EntityPlaceEvent event) {
		if (!event.getWorld().isRemote && event.getWorld() instanceof WorldServer &&
				event.getPlacedBlock().getBlock() == Blocks.MOB_SPAWNER && event.getEntity() instanceof EntityPlayer) {
			WorldServer world = (WorldServer) event.getWorld();
			int dimension = world.provider.getDimension();
			BlockPos pos = event.getPos();
			AgeingWorldData worldData = AgeingWorldData.get(DimensionManager.getWorld(0));
			Map<BlockPos, SpawnerInfo> locationMap = worldData.getDimensionMap(dimension);
			SpawnerInfo info = locationMap.getOrDefault(pos, new SpawnerInfo(0, true));
			locationMap.put(pos, info);
			worldData.setMapForDimension(dimension, locationMap);
			worldData.setDirty(true);
		}
	}

	@SubscribeEvent
	public void breakEvent(BreakEvent event) {
		if (!event.getWorld().isRemote && event.getWorld() instanceof WorldServer) {
			BlockPos pos = event.getPos();
			WorldServer world = (WorldServer) event.getWorld();
			int dimension = world.provider.getDimension();
			AgeingWorldData worldData = AgeingWorldData.get(DimensionManager.getWorld(0));
			Map<BlockPos, SpawnerInfo> locationMap = worldData.getDimensionMap(dimension);
			locationMap.remove(pos);
			worldData.setMapForDimension(dimension, locationMap);
			worldData.setDirty(true);
		}
	}
}
