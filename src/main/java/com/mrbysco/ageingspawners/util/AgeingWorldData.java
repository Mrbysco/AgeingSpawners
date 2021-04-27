package com.mrbysco.ageingspawners.util;

import com.mrbysco.ageingspawners.Reference;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import java.util.HashMap;
import java.util.Map;

public class AgeingWorldData extends WorldSavedData {
	private static final String DATA_NAME = Reference.MOD_ID + "_world_data";

	private final Map<ResourceLocation, Map<BlockPos, Integer>> worldSpawnerMap = new HashMap<>();

	public AgeingWorldData() {
		super(DATA_NAME);
	}

	@Override
	public void read(CompoundNBT compound) {
		for(String nbtName : compound.keySet()) {
			ListNBT dimensionNBTList = new ListNBT();
			if(compound.getTagId(nbtName) == 9) {
				INBT nbt = compound.get(nbtName);
				if(nbt instanceof ListNBT) {
					ListNBT listNBT = (ListNBT) nbt;
					if (!listNBT.isEmpty() && listNBT.getTagType() != Constants.NBT.TAG_COMPOUND) {
						return;
					}

					dimensionNBTList = listNBT;
				}
			}
			if(!dimensionNBTList.isEmpty()) {
				Map<BlockPos, Integer> locationMap = new HashMap<>();
				for (int i = 0; i < dimensionNBTList.size(); ++i) {
					CompoundNBT tag = dimensionNBTList.getCompound(i);
					if(tag.contains("BlockPos") && tag.contains("Amount")) {
						BlockPos blockPos = BlockPos.fromLong(tag.getLong("BlockPos"));
						int amount = tag.getInt("Amount");

						locationMap.put(blockPos, amount);
					}
				}
				worldSpawnerMap.put(new ResourceLocation(nbtName), locationMap);
			}
		}
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		for (Map.Entry<ResourceLocation, Map<BlockPos, Integer>> dimensionEntry : worldSpawnerMap.entrySet()) {
			ResourceLocation dimensionLocation = dimensionEntry.getKey();
			Map<BlockPos, Integer> savedPositions = dimensionEntry.getValue();

			ListNBT dimensionStorage = new ListNBT();
			for (Map.Entry<BlockPos, Integer> entry : savedPositions.entrySet()) {
				CompoundNBT positionTag = new CompoundNBT();
				positionTag.putLong("BlockPos", entry.getKey().toLong());
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

	public static AgeingWorldData get(World world) {
		if (!(world instanceof ServerWorld)) {
			throw new RuntimeException("Attempted to get the data from a client world. This is wrong.");
		}
		ServerWorld overworld = world.getServer().getWorld(World.OVERWORLD);

		DimensionSavedDataManager storage = overworld.getSavedData();
		return storage.getOrCreate(AgeingWorldData::new, DATA_NAME);
	}
}
