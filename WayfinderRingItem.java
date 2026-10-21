package com.example.wayfinder;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;

public class WayfinderRingItem extends Item {
    private static final TagKey<Structure> VILLAGE_TAG =
            TagKey.create(Registries.STRUCTURE, new ResourceLocation("minecraft", "village"));
    private static final int SEARCH_RADIUS_CHUNKS = 100;
    private static final int COOLDOWN_TICKS = 20 * 60;

    public WayfinderRingItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide || !(level instanceof ServerLevel serverLevel)) {
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        // 1. Find the nearest generated village.
        BlockPos village = serverLevel.findNearestMapStructure(
                VILLAGE_TAG, player.blockPosition(), SEARCH_RADIUS_CHUNKS, false);
        if (village == null) {
            player.displayClientMessage(Component.translatable("message.wayfinder.no_village"), true);
            return InteractionResultHolder.fail(stack);
        }

        // 2. Find a safe standing spot near the village's center.
        BlockPos destination = findSafeSpot(serverLevel, village);
        if (destination == null) {
            player.displayClientMessage(Component.translatable("message.wayfinder.no_safe_spot"), true);
            return InteractionResultHolder.fail(stack);
        }

        // 3. Work out where the 3x2 portal goes (two blocks in front of the player).
        Direction facing = player.getDirection();
        Direction.Axis axis = facing.getAxis() == Direction.Axis.Z ? Direction.Axis.X : Direction.Axis.Z;
        Direction side = axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
        BlockPos center = player.blockPosition().relative(facing, 2);

        List<BlockPos> cells = new ArrayList<>();
        for (int w = -1; w <= 1; w++) {
            for (int h = 0; h <= 1; h++) {
                BlockPos p = center.above(h).relative(side, w);
                if (level.isOutsideBuildHeight(p) || !level.getBlockState(p).canBeReplaced()) {
                    player.displayClientMessage(Component.translatable("message.wayfinder.no_room"), true);
                    return InteractionResultHolder.fail(stack);
                }
                cells.add(p);
            }
        }

        // 4. Open the portal.
        BlockState base = ModBlocks.VILLAGE_PORTAL.get().defaultBlockState()
                .setValue(VillagePortalBlock.AXIS, axis);
        for (BlockPos p : cells) {
            boolean isCenter = p.equals(center);
            level.setBlock(p, base.setValue(VillagePortalBlock.CENTER, isCenter), 3);
            if (level.getBlockEntity(p) instanceof VillagePortalEntity portal) {
                portal.setTarget(destination);
            }
        }
        level.playSound(null, center, SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.5F);

        // 5. Cooldown + durability.
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        player.awardStat(Stats.ITEM_USED.get(this));
        stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));

        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    /** Searches outward from the village origin for a safe 2-high standing spot. */
    @Nullable
    private static BlockPos findSafeSpot(ServerLevel level, BlockPos origin) {
        for (int r = 0; r <= 24; r += 2) {
            for (int dx = -r; dx <= r; dx += 2) {
                for (int dz = -r; dz <= r; dz += 2) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != r) continue;
                    BlockPos spot = standableAt(level, origin.getX() + dx, origin.getZ() + dz);
                    if (spot != null) return spot;
                }
            }
        }
        return null;
    }

    @Nullable
    private static BlockPos standableAt(ServerLevel level, int x, int z) {
        level.getChunk(x >> 4, z >> 4); // loads/generates the chunk
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        if (y <= level.getMinBuildHeight() + 1) return null;

        BlockPos feet = new BlockPos(x, y, z);
        BlockPos floor = feet.below();
        BlockState below = level.getBlockState(floor);

        if (!below.isFaceSturdy(level, floor, Direction.UP)) return null;
        if (!below.getFluidState().isEmpty()) return null;
        if (below.is(Blocks.MAGMA_BLOCK) || below.is(Blocks.CACTUS)
                || below.is(Blocks.CAMPFIRE) || below.is(Blocks.SOUL_CAMPFIRE)) return null;
        if (!level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()) return null;
        if (!level.getBlockState(feet.above()).getCollisionShape(level, feet.above()).isEmpty()) return null;
        if (!level.getFluidState(feet).isEmpty()) return null;
        return feet;
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(Items.GOLD_INGOT);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.wayfinder.wayfinder_ring").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.wayfinder.repair").withStyle(ChatFormatting.GRAY));
    }
}
