package com.mrbysco.ageingspawners.util;

import com.mrbysco.ageingspawners.Reference;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

import java.util.HashMap;
import java.util.Map;

public class AgeingWorldData extends WorldSavedData {
	private static final String DATA_NAME = Reference.MOD_ID + "_spawner_data";

	public Map<Integer, Map<BlockPos, SpawnerInfo>> spawnerMap;

	public AgeingWorldData(String name) {
		super(name);
		this.spawnerMap = new HashMap<>();
	}

	public AgeingWorldData() {
		this(DATA_NAME);
	}

	public Map<Integer, Map<BlockPos, SpawnerInfo>> getSpawnerMap() {
		return spawnerMap;
	}

	public Map<BlockPos, SpawnerInfo> getDimensionMap(int dimension) {
		return spawnerMap.getOrDefault(dimension, new HashMap<>());
	}

	public Map<BlockPos, SpawnerInfo> getMapFromDimension(int dimension) {
		return spawnerMap.getOrDefault(dimension, new HashMap<>());
	}

	public void setMapForDimension(int dimension, Map<BlockPos, SpawnerInfo> spawnerInfoList) {
		spawnerMap.put(dimension, spawnerInfoList);
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		Map<Integer, Map<BlockPos, SpawnerInfo>> map = new HashMap<>();
		for (String nbtName : compound.getKeySet()) {
			NBTTagList dimensionNBTList = new NBTTagList();
			if (compound.getTagId(nbtName) == 9) {
				NBTBase nbt = compound.getTag(nbtName);
				if (nbt instanceof NBTTagList) {
					NBTTagList listNBT = (NBTTagList)nbt;
					if (!listNBT.isEmpty() && listNBT.getTagType() != 9) {
						continue;
					}

					dimensionNBTList = listNBT;
				}
			}
			if (!dimensionNBTList.isEmpty()) {
				Map<BlockPos, SpawnerInfo> posMap = new HashMap<>();
				for (int i = 0; i < dimensionNBTList.tagCount(); ++i) {
					NBTTagCompound tag = dimensionNBTList.getCompoundTagAt(i);
					if (tag.hasKey("BlockPos") && tag.hasKey("Amount")) {
						BlockPos blockPos = BlockPos.fromLong(tag.getLong("BlockPos"));
						int amount = tag.getInteger("Amount");
						boolean playerPlaced = tag.getBoolean("PlayerPlaced");

						posMap.put(blockPos, new SpawnerInfo(amount, playerPlaced));
					}
				}
				map.put(Integer.valueOf(nbtName), posMap);
			}
		}

		this.spawnerMap = map;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		for (Map.Entry<Integer, Map<BlockPos, SpawnerInfo>> dimensionEntry : spawnerMap.entrySet()) {
			int dimension = dimensionEntry.getKey();
			Map<BlockPos, SpawnerInfo> savedPositions = dimensionEntry.getValue();
			NBTTagList dimensionStorage = new NBTTagList();
			for (Map.Entry<BlockPos, SpawnerInfo> entry : savedPositions.entrySet()) {
				SpawnerInfo info = entry.getValue();
				NBTTagCompound positionTag = new NBTTagCompound();
				positionTag.setLong("BlockPos", entry.getKey().toLong());
				positionTag.setInteger("Amount", info.spawnCount());
				positionTag.setBoolean("PlayerPlaced", info.playerPlaced());
				dimensionStorage.appendTag(positionTag);
			}
			compound.setTag(String.valueOf(dimension), dimensionStorage);
		}
		return compound;
	}

	public static AgeingWorldData get(World world) {
		MapStorage storage = world.getPerWorldStorage();
		AgeingWorldData data = (AgeingWorldData) storage.getOrLoadData(AgeingWorldData.class, DATA_NAME);
		if (data == null) {
			data = new AgeingWorldData();
			storage.setData(DATA_NAME, data);
		}
		return data;
	}
}
