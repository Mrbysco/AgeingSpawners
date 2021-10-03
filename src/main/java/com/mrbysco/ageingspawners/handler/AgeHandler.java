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
		if (!event.getWorld().isClientSide() && event.isSpawner()) {
			AbstractSpawner spawner = event.getSpawner();
			IWorld world = event.getWorld();
			if(spawner != null) {
				ResourceLocation registryName = event.getEntityLiving().getType().getRegistryName();
				switch (SpawnerConfig.COMMON.spawnerMode.get()) {
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
			this.ageTheSpawner(world, spawner, SpawnerConfig.COMMON.blacklistMaxSpawnCount.get());
		} else {
			BlockPos pos = spawner.getPos();
			ResourceLocation dimensionLocation = world.dimension().location();
			AgeingWorldData worldData = AgeingWorldData.get(world);
			Map<BlockPos, Integer> locationMap = worldData.getMapFromWorld(dimensionLocation);
			locationMap.remove(pos);
			worldData.setMapForWorld(dimensionLocation, locationMap);
			worldData.setDirty();
		}
	}

	public void handleWhitelist(World world, AbstractSpawner spawner, ResourceLocation registryName) {
		if(AgeingHelper.whitelistContains(registryName)) {
			int maxSpawnCount = AgeingHelper.getMaxSpawnCount(registryName);
			this.ageTheSpawner(world, spawner, maxSpawnCount);
		} else {
			BlockPos pos = spawner.getPos();
			ResourceLocation dimensionLocation = world.dimension().location();
			AgeingWorldData worldData = AgeingWorldData.get(world);
			Map<BlockPos, Integer> locationMap = worldData.getMapFromWorld(dimensionLocation);
			locationMap.remove(pos);
			worldData.setMapForWorld(dimensionLocation, locationMap);
			worldData.setDirty();
		}
	}

	public void ageTheSpawner(World world, AbstractSpawner spawner, int maxCount) {
		BlockPos spawnerPos = spawner.getPos();
		ResourceLocation dimensionLocation = world.dimension().location();
		AgeingWorldData worldData = AgeingWorldData.get(world);
		Map<BlockPos, Integer> locationMap = worldData.getMapFromWorld(dimensionLocation);

		if(world.getBlockEntity(spawnerPos) != null && world.getBlockEntity(spawnerPos) instanceof MobSpawnerTileEntity) {
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
		worldData.setDirty();
	}

	@SubscribeEvent
	public void breakEvent(BreakEvent event) {
		if(!event.getWorld().isClientSide()) {
			BlockPos pos = event.getPos();
			World world = (World) event.getWorld();
			ResourceLocation dimensionLocation = world.dimension().location();
			Map<BlockPos, Integer> locationMap = AgeingWorldData.get(world).getMapFromWorld(dimensionLocation);
			locationMap.remove(pos);
		}
	}
}
