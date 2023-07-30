package com.mrbysco.ageingspawners;

import com.mrbysco.ageingspawners.config.SpawnerConfig;
import com.mrbysco.ageingspawners.handler.AgeHandler;
import net.minecraft.world.GameRules;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Reference.MOD_ID,
		name = Reference.MOD_NAME,
		version = Reference.VERSION,
		acceptedMinecraftVersions = Reference.ACCEPTED_VERSIONS,
		dependencies = Reference.DEPENDENCIES)
public class AgeingSpawners {
	@Instance(Reference.MOD_ID)
	public static AgeingSpawners instance;

	public static final Logger logger = LogManager.getLogger(Reference.MOD_ID);

	public static final String AGE_SPAWNERS_RULE = Reference.MOD_ID + ".ageingSpawners";

	@EventHandler
	public void PreInit(FMLPreInitializationEvent event) {
		logger.info("Registering Ageing Spawners' Config");
		MinecraftForge.EVENT_BUS.register(new SpawnerConfig());
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		logger.info("Registering Ageing Spawners' Handlers");
		MinecraftForge.EVENT_BUS.register(new AgeHandler());
	}

	@EventHandler
	public void serverStarting(FMLServerStartingEvent event) {
		GameRules gameRules = event.getServer().getWorld(0).getGameRules();
		gameRules.addGameRule(AGE_SPAWNERS_RULE, "true", GameRules.ValueType.BOOLEAN_VALUE);
	}
}
