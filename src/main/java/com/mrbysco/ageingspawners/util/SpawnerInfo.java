package com.mrbysco.ageingspawners.util;

public class SpawnerInfo {
	private final int spawnCount;
	private final boolean playerPlaced;

	public SpawnerInfo(int spawnCount, boolean playerPlaced) {
		this.spawnCount = spawnCount;
		this.playerPlaced = playerPlaced;
	}

	public int spawnCount() {
		return spawnCount;
	}

	public boolean playerPlaced() {
		return playerPlaced;
	}
}
