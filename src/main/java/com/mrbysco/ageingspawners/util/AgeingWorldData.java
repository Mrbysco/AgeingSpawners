package com.mrbysco.ageingspawners.util;

import com.mrbysco.ageingspawners.AgeingSpawners;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.HashMap;
import java.util.Map;

public class AgeingWorldData extends SavedData {
	private static final String DATA_NAME = AgeingSpawners.MOD_ID + "_world_data";

	private final Map<ResourceLocation, Map<BlockPos, SpawnerInfo>> worldSpawnerMap = new HashMap<>();

	public AgeingWorldData(Map<ResourceLocation, Map<BlockPos, SpawnerInfo>> map) {
		if (!map.isEmpty()) {
			this.worldSpawnerMap.clear();
			this.worldSpawnerMap.putAll(map);
		}
	}

	public AgeingWorldData() {
		this(new HashMap<>());
	}

	public static AgeingWorldData load(CompoundTag compound) {
		Map<ResourceLocation, Map<BlockPos, SpawnerInfo>> map = new HashMap<>();
		for (String nbtName : compound.getAllKeys()) {
			ListTag dimensionNBTList = new ListTag();
			if (compound.getTagType(nbtName) == 9) {
				Tag nbt = compound.get(nbtName);
				if (nbt instanceof ListTag listNBT) {
					if (!listNBT.isEmpty() && listNBT.getElementType() != CompoundTag.TAG_COMPOUND) {
						continue;
					}

					dimensionNBTList = listNBT;
				}
			}
			if (!dimensionNBTList.isEmpty()) {
				Map<BlockPos, SpawnerInfo> posMap = new HashMap<>();
				for (int i = 0; i < dimensionNBTList.size(); ++i) {
					CompoundTag tag = dimensionNBTList.getCompound(i);
					if (tag.contains("BlockPos") && tag.contains("Amount")) {
						BlockPos blockPos = BlockPos.of(tag.getLong("BlockPos"));
						int amount = tag.getInt("Amount");
						boolean playerPlaced = tag.getBoolean("PlayerPlaced");

						posMap.put(blockPos, new SpawnerInfo(amount, playerPlaced));
					}
				}
				map.put(new ResourceLocation(nbtName), posMap);
			}
		}
		return new AgeingWorldData(map);
	}

	@Override
	public CompoundTag save(CompoundTag compound) {
		for (Map.Entry<ResourceLocation, Map<BlockPos, SpawnerInfo>> dimensionEntry : worldSpawnerMap.entrySet()) {
			ResourceLocation dimensionLocation = dimensionEntry.getKey();
			Map<BlockPos, SpawnerInfo> savedPositions = dimensionEntry.getValue();

			ListTag dimensionStorage = new ListTag();
			for (Map.Entry<BlockPos, SpawnerInfo> entry : savedPositions.entrySet()) {
				SpawnerInfo info = entry.getValue();
				CompoundTag positionTag = new CompoundTag();
				positionTag.putLong("BlockPos", entry.getKey().asLong());
				positionTag.putInt("Amount", info.spawnCount());
				positionTag.putBoolean("PlayerPlaced", info.playerPlaced());
				dimensionStorage.add(positionTag);
			}
			compound.put(dimensionLocation.toString(), dimensionStorage);
		}
		return compound;
	}

	public Map<BlockPos, SpawnerInfo> getMapFromWorld(ResourceLocation dimensionLocation) {
		return worldSpawnerMap.getOrDefault(dimensionLocation, new HashMap<>());
	}

	public void setMapForWorld(ResourceLocation dimensionLocation, Map<BlockPos, SpawnerInfo> spawnerInfoList) {
		worldSpawnerMap.put(dimensionLocation, spawnerInfoList);
	}

	public static AgeingWorldData get(Level level) {
		if (!(level instanceof ServerLevel)) {
			throw new RuntimeException("Attempted to get the data from a client world. This is wrong.");
		}
		ServerLevel overworld = level.getServer().getLevel(Level.OVERWORLD);

		DimensionDataStorage storage = overworld.getDataStorage();
		return storage.computeIfAbsent(AgeingWorldData::load, AgeingWorldData::new, DATA_NAME);
	}

	public record SpawnerInfo(Integer spawnCount, boolean playerPlaced) {
	}
}
