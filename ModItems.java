package com.example.wayfinder;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, WayfinderMod.MOD_ID);

    public static final RegistryObject<Item> WAYFINDER_RING = ITEMS.register("wayfinder_ring",
            () -> new WayfinderRingItem(new Item.Properties().durability(20).rarity(Rarity.RARE)));
}
