package com.example.wayfinder;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, WayfinderMod.MOD_ID);

    public static final RegistryObject<BlockEntityType<VillagePortalEntity>> VILLAGE_PORTAL =
            BLOCK_ENTITIES.register("village_portal",
                    () -> BlockEntityType.Builder.of(VillagePortalEntity::new,
                            ModBlocks.VILLAGE_PORTAL.get()).build(null));
}
