package com.example.wayfinder;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, WayfinderMod.MOD_ID);

    public static final RegistryObject<Block> VILLAGE_PORTAL = BLOCKS.register("village_portal",
            () -> new VillagePortalBlock(BlockBehaviour.Properties.of()
                    .noCollission()
                    .noOcclusion()
                    .noLootTable()
                    .strength(-1.0F, 3600000.0F)
                    .lightLevel(state -> 12)
                    .sound(SoundType.AMETHYST)
                    .pushReaction(PushReaction.BLOCK)));
}
