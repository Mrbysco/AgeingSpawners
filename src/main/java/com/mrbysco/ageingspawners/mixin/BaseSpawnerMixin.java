package com.mrbysco.ageingspawners.mixin;

import com.mojang.datafixers.util.Either;
import com.mrbysco.ageingspawners.handler.AgeHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(BaseSpawner.class)
public abstract class BaseSpawnerMixin {
	@Shadow
	@Nullable
	public abstract Either<BlockEntity, Entity> getOwner();

	@ModifyArg(method = "serverTick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/level/ServerLevel;tryAddFreshEntityWithPassengers(Lnet/minecraft/world/entity/Entity;)Z"),
			index = 0)
	public Entity ageingServerTick(Entity entity) {
		if (!(entity instanceof Mob)) {
			AgeHandler.handleSpawner(entity.level(), getOwner(), entity);
		}
		return entity;
	}
}
