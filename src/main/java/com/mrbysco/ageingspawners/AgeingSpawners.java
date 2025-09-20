package com.mrbysco.ageingspawners;

import com.mojang.logging.LogUtils;
import com.mrbysco.ageingspawners.config.SpawnerConfig;
import com.mrbysco.ageingspawners.handler.AgeHandler;
import net.minecraft.world.level.GameRules;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(AgeingSpawners.MOD_ID)
public class AgeingSpawners {
	public static final String MOD_ID = "ageingspawners";
	public static final GameRules.Key<GameRules.BooleanValue> AGE_SPAWNERS_RULE =
			GameRules.register("ageingSpawners", GameRules.Category.UPDATES, GameRules.BooleanValue.create(true));
	public static final Logger LOGGER = LogUtils.getLogger();

	public AgeingSpawners(IEventBus eventBus, Dist dist, ModContainer container) {
		container.registerConfig(ModConfig.Type.COMMON, SpawnerConfig.commonSpec);
		eventBus.register(SpawnerConfig.class);

		NeoForge.EVENT_BUS.register(new AgeHandler());

		if (dist.isClient()) {
			container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
		}
	}
}