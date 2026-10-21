package com.example.wayfinder;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class VillagePortalEntity extends BlockEntity {
    /** Portal stays open for 30 seconds. */
    public static final int LIFETIME_TICKS = 20 * 30;
    private static final double RADIUS = 1.0;

    @Nullable
    private BlockPos target;
    private int age;

    public VillagePortalEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.VILLAGE_PORTAL.get(), pos, state);
    }

    @Nullable
    public BlockPos getTarget() {
        return target;
    }

    public void setTarget(BlockPos target) {
        this.target = target;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (target != null) tag.putLong("Target", target.asLong());
        tag.putInt("Age", age);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        target = tag.contains("Target") ? BlockPos.of(tag.getLong("Target")) : null;
        age = tag.getInt("Age");
    }

    public static void tick(Level level, BlockPos pos, BlockState state, VillagePortalEntity be) {
        if (level.isClientSide) {
            be.clientTick(level, pos, state);
            return;
        }
        be.age++;
        if (be.age >= LIFETIME_TICKS) {
            level.removeBlock(pos, false);
        }
    }

    private void clientTick(Level level, BlockPos pos, BlockState state) {
        if (!state.getValue(VillagePortalBlock.CENTER)) return;

        Direction.Axis axis = state.getValue(VillagePortalBlock.AXIS);
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 1.0;
        double cz = pos.getZ() + 0.5;
        RandomSource r = level.random;

        // Spinning, sparking ring.
        for (int i = 0; i < 8; i++) {
            double a = r.nextDouble() * Math.PI * 2.0;
            double cos = Math.cos(a);
            double sin = Math.sin(a);
            double px = axis == Direction.Axis.X ? cx + cos * RADIUS : cx;
            double pz = axis == Direction.Axis.Z ? cz + cos * RADIUS : cz;
            double py = cy + sin * RADIUS;
            double vAlong = -sin * 0.04;
            double vy = cos * 0.04;
            double vx = axis == Direction.Axis.X ? vAlong : 0.0;
            double vz = axis == Direction.Axis.Z ? vAlong : 0.0;
            level.addParticle(ParticleTypes.FLAME, px, py, pz, vx, vy, vz);
            if (i % 2 == 0) {
                level.addParticle(ParticleTypes.ELECTRIC_SPARK, px, py, pz, vx * 2.0, vy * 2.0, vz * 2.0);
            }
        }

        // Shimmer inside the ring.
        double a2 = r.nextDouble() * Math.PI * 2.0;
        double d = Math.sqrt(r.nextDouble()) * RADIUS * 0.9;
        double ix = axis == Direction.Axis.X ? cx + Math.cos(a2) * d : cx;
        double iz = axis == Direction.Axis.Z ? cz + Math.cos(a2) * d : cz;
        level.addParticle(ParticleTypes.END_ROD, ix, cy + Math.sin(a2) * d, iz, 0.0, 0.0, 0.0);

        if (level.getGameTime() % 40 == 0) {
            level.playLocalSound(cx, cy, cz, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.6F, 0.8F, false);
        }
    }
}
