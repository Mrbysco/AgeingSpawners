package com.mrbysco.ageingspawners.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mrbysco.ageingspawners.AgeingSpawners;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.HashMap;
import java.util.Map;

public class AgeingWorldData extends SavedData {
	private static final String DATA_NAME = AgeingSpawners.MOD_ID + "_world_data";

	private static final Codec<Map<BlockPos, SpawnerInfo>> SPAWNER_MAP_CODEC = Codec.unboundedMap(
			BlockPos.CODEC,
			SpawnerInfo.CODEC
	);

	public static final Codec<AgeingWorldData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
					Codec.unboundedMap(ResourceLocation.CODEC, SPAWNER_MAP_CODEC).fieldOf("worldSpawnerMap").forGetter(data -> data.worldSpawnerMap))
			.apply(inst, AgeingWorldData::new));

	private final Map<ResourceLocation, Map<BlockPos, SpawnerInfo>> worldSpawnerMap = new HashMap<>();

	public AgeingWorldData(Map<ResourceLocation, Map<BlockPos, SpawnerInfo>> map) {
		this.worldSpawnerMap.clear();
		this.worldSpawnerMap.putAll(map);
	}

	public AgeingWorldData() {
		this(new HashMap<>());
	}

	public Map<BlockPos, SpawnerInfo> getMapFromWorld(ResourceLocation dimensionLocation) {
		return worldSpawnerMap.getOrDefault(dimensionLocation, new HashMap<>());
	}

	public void setMapForWorld(ResourceLocation dimensionLocation, Map<BlockPos, SpawnerInfo> spawnerInfoList) {
		worldSpawnerMap.put(dimensionLocation, spawnerInfoList);
	}

	public static SavedDataType<AgeingWorldData> type() {
		return new SavedDataType<>(DATA_NAME, AgeingWorldData::new, CODEC, null);
	}

	public static AgeingWorldData get(Level level) {
		if (!(level instanceof ServerLevel)) {
			throw new RuntimeException("Attempted to get the data from a client world. This is wrong.");
		}
		ServerLevel overworld = level.getServer().getLevel(Level.OVERWORLD);

		assert overworld != null;
		DimensionDataStorage storage = overworld.getDataStorage();
		return storage.computeIfAbsent(type());
	}

	public record SpawnerInfo(Integer spawnCount, boolean playerPlaced) {
		public static final Codec<SpawnerInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.INT.fieldOf("spawnCount").forGetter(SpawnerInfo::spawnCount),
				Codec.BOOL.fieldOf("playerPlaced").forGetter(SpawnerInfo::playerPlaced)
		).apply(instance, SpawnerInfo::new));
	}
}
