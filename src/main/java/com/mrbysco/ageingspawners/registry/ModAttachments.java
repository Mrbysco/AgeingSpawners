package com.mrbysco.ageingspawners.registry;

import com.mrbysco.ageingspawners.AgeingSpawners;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModAttachments {

	public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, AgeingSpawners.MOD_ID);

	public static final Supplier<AttachmentType<GlobalPos>> LINKED_SPAWNER = ATTACHMENT_TYPES.register(
			"linked_spawner", () -> AttachmentType.builder(() -> GlobalPos.of(Level.OVERWORLD, BlockPos.ZERO))
					.serialize(GlobalPos.CODEC).build());
}
