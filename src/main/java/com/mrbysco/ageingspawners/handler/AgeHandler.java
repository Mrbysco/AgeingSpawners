package com.mrbysco.ageingspawners.handler;

import com.mrbysco.ageingspawners.SpawnerSaveData;
import com.mrbysco.ageingspawners.config.SpawnerConfig;
import com.mrbysco.ageingspawners.config.SpawnerConfig.EnumAgeingMode;
import com.mrbysco.ageingspawners.util.AgeingHelper;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;

public class AgeHandler {
	@SubscribeEvent
	public void SpawnEvent(LivingSpawnEvent.SpecialSpawn event) {
		if (!event.getWorld().isRemote && event.getSpawner() != null) {
			ResourceLocation registryName = EntityList.getKey(event.getEntityLiving());
			handleSpawnEvent(event, registryName, SpawnerConfig.general.spawnerMode);
		}
	}

	public void handleSpawnEvent(LivingSpawnEvent.SpecialSpawn event, ResourceLocation registryName, EnumAgeingMode mode) {
		MobSpawnerBaseLogic spawnerLogic = event.getSpawner();
		if(spawnerLogic != null) {
			if(!AgeingHelper.blacklistContains(registryName)) {
				int maxSpawnCount = AgeingHelper.getDefaultMax();
				if(mode == EnumAgeingMode.WHITELIST) {
					maxSpawnCount = AgeingHelper.getMaxSpawnCount(registryName);
				}
				this.ageTheSpawner(event, maxSpawnCount);
			} else {
				SpawnerSaveData data = SpawnerSaveData.getForWorld(DimensionManager.getWorld(0));
				BlockPos pos = spawnerLogic.getSpawnerPosition();
				int dimension = event.getEntity().dimension;
				HashMap<BlockPos, Integer> spawnerMap = data.getDimensionMap(dimension);
				spawnerMap.remove(pos);
				data.putDimensionMap(dimension, spawnerMap);
				data.markDirty();
			}
		}
	}

	public void ageTheSpawner(LivingSpawnEvent.SpecialSpawn event, int maxCount) {
		MobSpawnerBaseLogic spawnerLogic = event.getSpawner();
		WorldServer world = (WorldServer)event.getWorld();
		if(spawnerLogic != null) {
			BlockPos spawnerPos = spawnerLogic.getSpawnerPosition();
			SpawnerSaveData data = SpawnerSaveData.getForWorld(DimensionManager.getWorld(0));
			int dimension = event.getEntity().dimension;
			HashMap<BlockPos, Integer> spawnerMap = data.getDimensionMap(dimension);
			if(world.getTileEntity(spawnerPos) != null && world.getTileEntity(spawnerPos) instanceof TileEntityMobSpawner) {
				int spawnCount = spawnerMap.getOrDefault(spawnerPos, 0);
				spawnCount++;
				if(spawnCount >= maxCount) {
					if(SpawnerConfig.general.spawnerMode != EnumAgeingMode.REGENERATE) {
						world.setBlockState(spawnerPos, Blocks.AIR.getDefaultState());
						spawnerMap.remove(spawnerPos);
						data.putDimensionMap(dimension, spawnerMap);
						data.markDirty();
					}
					event.setCanceled(true);
				} else {
					spawnerMap.put(spawnerPos, spawnCount);
					data.putDimensionMap(dimension, spawnerMap);
					data.markDirty();
				}
			}
		}
	}

	@SubscribeEvent
	public void breakEvent(BreakEvent event) {
		if(!event.getWorld().isRemote) {
			BlockPos pos = event.getPos();
			WorldServer world = (WorldServer)event.getWorld();
			int dimension = world.provider.getDimension();
			SpawnerSaveData data = SpawnerSaveData.getForWorld(DimensionManager.getWorld(0));
			HashMap<BlockPos, Integer> spawnerMap = data.getDimensionMap(dimension);
			if(!spawnerMap.isEmpty()) {
				spawnerMap.remove(pos);
				data.putDimensionMap(dimension, spawnerMap);
				data.markDirty();
			}
		}
	}
}
