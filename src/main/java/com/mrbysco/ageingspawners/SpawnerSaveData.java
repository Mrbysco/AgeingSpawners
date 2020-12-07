package com.mrbysco.ageingspawners;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.HashMap;
import java.util.Map;

public class SpawnerSaveData extends WorldSavedData {
    private static final String DATA_NAME = Reference.MOD_ID + "_spawner_data";

    public HashMap<Integer, HashMap<BlockPos, Integer>> spawnerMap;

    public SpawnerSaveData(String name) {
        super(name);
        this.spawnerMap = new HashMap<>();
    }

    public SpawnerSaveData() {
        this(DATA_NAME);
    }

    public HashMap<Integer, HashMap<BlockPos, Integer>> getSpawnerMap() {
        return spawnerMap;
    }

    public HashMap<BlockPos, Integer> getDimensionMap(int dimension) {
        return spawnerMap.getOrDefault(dimension, new HashMap<>());
    }

    public void putDimensionMap(int dimension, HashMap<BlockPos, Integer> map) {
        spawnerMap.put(Integer.valueOf(dimension), map);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        if(!compound.isEmpty()) {
            HashMap<Integer, HashMap<BlockPos, Integer>> map = new HashMap<>();
            for(String key : compound.getKeySet()) {
                if(compound.getTag(key) instanceof NBTTagCompound) {
                    NBTTagCompound compound2 = (NBTTagCompound)compound.getTag(key);
                    if(NumberUtils.isParsable(key)) {
                        for(String key2 : compound2.getKeySet()) {
                            if(compound2.getTag(key2) instanceof NBTTagInt) {
                                if(NumberUtils.isParsable(key2) && compound2.getTag(key2) instanceof NBTTagInt) {
                                    HashMap<BlockPos, Integer> map2 = new HashMap<>();
                                    map2.put(BlockPos.fromLong(Long.parseLong(key2)), compound2.getInteger(key2));
                                    map.put(Integer.parseInt(key), map2);
                                }
                            }
                        }
                    }
                }
            }
            this.spawnerMap = map;
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        for (Map.Entry<Integer, HashMap<BlockPos, Integer>> entry : spawnerMap.entrySet()) {
            NBTTagCompound dimensionCompound = new NBTTagCompound();
            for (Map.Entry<BlockPos, Integer> entry2 : entry.getValue().entrySet()) {
                dimensionCompound.setInteger(String.valueOf(entry2.getKey().toLong()), entry2.getValue());
            }
            compound.setTag(String.valueOf(entry.getKey()), dimensionCompound);
        }
        return compound;
    }

    public static SpawnerSaveData getForWorld(World world) {
        MapStorage storage = world.getPerWorldStorage();
        SpawnerSaveData data = (SpawnerSaveData) storage.getOrLoadData(SpawnerSaveData.class, DATA_NAME);
        if (data == null) {
            data = new SpawnerSaveData();
            storage.setData(DATA_NAME, data);
        }
        return data;
    }
}
