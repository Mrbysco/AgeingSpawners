package com.mrbysco.ageingspawners.mixin;

import com.mrbysco.ageingspawners.handler.AgeHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BaseSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(BaseSpawner.class)
public class BaseSpawnerMixin {
	@ModifyArg(method = "serverTick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/level/ServerLevel;tryAddFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)Z"),
			index = 0)
	public Entity ageingServerTick(Entity entity) {
		if (!(entity instanceof Mob)) {
			AgeHandler.handleSpawner(entity.level, (BaseSpawner) (Object) this, entity);
		}
		return entity;
	}
}
