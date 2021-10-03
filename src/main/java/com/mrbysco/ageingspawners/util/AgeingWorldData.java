package com.mrbysco.ageingspawners.util;

import com.mrbysco.ageingspawners.Reference;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraftforge.common.util.Constants;

import java.util.HashMap;
import java.util.Map;

public class AgeingWorldData extends SavedData {
	private static final String DATA_NAME = Reference.MOD_ID + "_world_data";

	private final Map<ResourceLocation, Map<BlockPos, Integer>> worldSpawnerMap = new HashMap<>();

	public AgeingWorldData(Map<ResourceLocation, Map<BlockPos, Integer>> map) {
		if(!map.isEmpty()) {
			this.worldSpawnerMap.clear();
			this.worldSpawnerMap.putAll(map);
		}
	}

	public AgeingWorldData() {
		this(new HashMap<>());
	}

	public static AgeingWorldData load(CompoundTag compound) {
		Map<ResourceLocation, Map<BlockPos, Integer>> map = new HashMap<>();
		for(String nbtName : compound.getAllKeys()) {
			ListTag dimensionNBTList = new ListTag();
			if(compound.getTagType(nbtName) == 9) {
				Tag nbt = compound.get(nbtName);
				if(nbt instanceof ListTag listNBT) {
					if (!listNBT.isEmpty() && listNBT.getElementType() != Constants.NBT.TAG_COMPOUND) {
						continue;
					}

					dimensionNBTList = listNBT;
				}
			}
			if(!dimensionNBTList.isEmpty()) {
				Map<BlockPos, Integer> locationMap = new HashMap<>();
				for (int i = 0; i < dimensionNBTList.size(); ++i) {
					CompoundTag tag = dimensionNBTList.getCompound(i);
					if(tag.contains("BlockPos") && tag.contains("Amount")) {
						BlockPos blockPos = BlockPos.of(tag.getLong("BlockPos"));
						int amount = tag.getInt("Amount");

						locationMap.put(blockPos, amount);
					}
				}
				map.put(new ResourceLocation(nbtName), locationMap);
			}
		}
		return new AgeingWorldData(map);
	}

	@Override
	public CompoundTag save(CompoundTag compound) {
		for (Map.Entry<ResourceLocation, Map<BlockPos, Integer>> dimensionEntry : worldSpawnerMap.entrySet()) {
			ResourceLocation dimensionLocation = dimensionEntry.getKey();
			Map<BlockPos, Integer> savedPositions = dimensionEntry.getValue();

			ListTag dimensionStorage = new ListTag();
			for (Map.Entry<BlockPos, Integer> entry : savedPositions.entrySet()) {
				CompoundTag positionTag = new CompoundTag();
				positionTag.putLong("BlockPos", entry.getKey().asLong());
				positionTag.putInt("Amount", entry.getValue());
				dimensionStorage.add(positionTag);
			}
			compound.put(dimensionLocation.toString(), dimensionStorage);
		}
		return compound;
	}

	public Map<BlockPos, Integer> getMapFromWorld(ResourceLocation dimensionLocation) {
		return worldSpawnerMap.getOrDefault(dimensionLocation, new HashMap<>());
	}

	public void setMapForWorld(ResourceLocation dimensionLocation, Map<BlockPos, Integer> locationMap) {
		worldSpawnerMap.put(dimensionLocation, locationMap);
	}

	public static AgeingWorldData get(Level world) {
		if (!(world instanceof ServerLevel)) {
			throw new RuntimeException("Attempted to get the data from a client world. This is wrong.");
		}
		ServerLevel overworld = world.getServer().getLevel(Level.OVERWORLD);

		DimensionDataStorage storage = overworld.getDataStorage();
		return storage.computeIfAbsent(AgeingWorldData::load, AgeingWorldData::new, DATA_NAME);
	}
}
