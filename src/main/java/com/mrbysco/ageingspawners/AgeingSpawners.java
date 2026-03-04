package com.mrbysco.ageingspawners;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mrbysco.ageingspawners.config.SpawnerConfig;
import com.mrbysco.ageingspawners.handler.AgeHandler;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRuleType;
import net.minecraft.world.level.gamerules.GameRuleTypeVisitor;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

import java.util.function.ToIntFunction;

@Mod(AgeingSpawners.MOD_ID)
public class AgeingSpawners {
	public static final String MOD_ID = "ageingspawners";
	public static final Logger LOGGER = LogUtils.getLogger();

	private static final DeferredRegister<GameRule<?>> GAME_RULES = DeferredRegister.create(Registries.GAME_RULE, MOD_ID);

	public static final DeferredHolder<GameRule<?>, GameRule<Boolean>> AGE_SPAWNERS_RULE = registerBoolean("ageing_spawners", GameRuleCategory.UPDATES, true);

	public AgeingSpawners(IEventBus eventBus, Dist dist, ModContainer container) {
		container.registerConfig(ModConfig.Type.COMMON, SpawnerConfig.commonSpec);
		eventBus.register(SpawnerConfig.class);

		GAME_RULES.register(eventBus);

		NeoForge.EVENT_BUS.register(new AgeHandler());

		if (dist.isClient()) {
			container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
		}
	}

	private static DeferredHolder<GameRule<?>, GameRule<Boolean>> registerBoolean(String name, GameRuleCategory category, boolean defaultValue) {
		return register(name, category, GameRuleType.BOOL, BoolArgumentType.bool(), Codec.BOOL, defaultValue, FeatureFlagSet.of(), GameRuleTypeVisitor::visitBoolean, p_460985_ -> p_460985_ ? 1 : 0);
	}

	private static <T> DeferredHolder<GameRule<?>, GameRule<T>> register(String name, GameRuleCategory category, GameRuleType gameRuleType, ArgumentType<T> argument, Codec<T> valueCodec, T defaultValue, FeatureFlagSet requiredFeatures, GameRules.VisitorCaller<T> visitorCaller, ToIntFunction<T> commandResultFunction) {
		return GAME_RULES.register(name, () -> new GameRule<>(category, gameRuleType, argument, visitorCaller, valueCodec, commandResultFunction, defaultValue, requiredFeatures));
	}
}