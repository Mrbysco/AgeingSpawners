package com.mrbysco.ageingspawners;

import com.mrbysco.ageingspawners.config.SpawnerConfig;
import com.mrbysco.ageingspawners.handler.AgeHandler;
import com.mrbysco.ageingspawners.proxy.CommonProxy;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;

@Mod(modid = Reference.MOD_ID,
		name = Reference.MOD_NAME,
		version = Reference.VERSION,
		acceptedMinecraftVersions = Reference.ACCEPTED_VERSIONS,
		dependencies = Reference.DEPENDENCIES)
public class AgeingSpawners {
	@Instance(Reference.MOD_ID)
	public static AgeingSpawners instance;

	@SidedProxy(clientSide = Reference.CLIENT_PROXY_CLASS, serverSide = Reference.SERVER_PROXY_CLASS)
	public static CommonProxy proxy;

	public static final Logger logger = LogManager.getLogger(Reference.MOD_ID);

	public HashMap<ResourceLocation, Integer> whitelistMap = new HashMap<>();

	@EventHandler
	public void PreInit(FMLPreInitializationEvent event) {
		logger.info("Registering Ageing Spawners' Config");
		MinecraftForge.EVENT_BUS.register(new SpawnerConfig());

		proxy.PreInit();
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		proxy.Init();

		logger.info("Registering Ageing Spawners' Handlers");
		MinecraftForge.EVENT_BUS.register(new AgeHandler());
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		proxy.PostInit();
	}
}
