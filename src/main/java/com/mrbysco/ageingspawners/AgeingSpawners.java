package com.mrbysco.ageingspawners;

import com.mojang.logging.LogUtils;
import com.mrbysco.ageingspawners.config.SpawnerConfig;
import com.mrbysco.ageingspawners.handler.AgeHandler;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(Reference.MOD_ID)
public class AgeingSpawners {
	public static final GameRules.Key<GameRules.BooleanValue> AGE_SPAWNERS_RULE =
			GameRules.register("ageingSpawners", GameRules.Category.UPDATES, GameRules.BooleanValue.create(true));

	public static final Logger LOGGER = LogUtils.getLogger();

	public AgeingSpawners() {
		IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
		ModLoadingContext.get().registerConfig(Type.COMMON, SpawnerConfig.commonSpec);
		eventBus.register(SpawnerConfig.class);

		MinecraftForge.EVENT_BUS.register(new AgeHandler());
	}
}