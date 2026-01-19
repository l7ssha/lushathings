package xyz.l7ssha.lushathings.blockentity;

import com.hollingsworth.arsnouveau.api.source.ISourceCap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import xyz.l7ssha.lushathings.lushathings;
import xyz.l7ssha.lushathings.EnergyStorageWrapper;

// TODO: https://github.com/62832/ArsEnergistique/blob/master/src/main/java/gripe/_90/arseng/block/entity/MESourceJarBlockEntity.java
public class EternalFireSourceRecombinatorBlockEntity extends BlockEntity implements ISourceCap {
    private static final int MAX_SOURCE_STORAGE = 4096;
    private static final int MAX_FE_STORAGE = 1_000_000;
    private static final int MAX_FE_CONSUME_PER_TICK = 512_000;

    private int storedSource = 0;

    private final EnergyStorageWrapper energyStorage = new EnergyStorageWrapper(MAX_FE_STORAGE, MAX_FE_CONSUME_PER_TICK, () -> {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    });

    private final IEnergyStorage externalEnergyStorage = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return energyStorage.receiveEnergy(maxReceive, simulate);
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return energyStorage.getEnergyStored();
        }

        @Override
        public int getMaxEnergyStored() {
            return energyStorage.getMaxEnergyStored();
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    };

    public EternalFireSourceRecombinatorBlockEntity(BlockPos pos, BlockState blockState) {
        super(lushathings.ETERNAL_FIRE_SOURCE_RECOMBINATOR_BLOCK_ENTITY.get(), pos, blockState);
    }

    public IEnergyStorage getEnergyStorage() {
        return externalEnergyStorage;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, EternalFireSourceRecombinatorBlockEntity blockEntity) {
        if (level.isClientSide) {
            return;
        }

        if ((level.getGameTime() % 10L) == 0L) {
            BlockPos firePos = pos.below();
            BlockPos sandPos = firePos.below();

            boolean hasSoulFire = level.getBlockState(firePos).is(Blocks.SOUL_FIRE);
            boolean hasSoulSand = level.getBlockState(sandPos).is(Blocks.SOUL_SAND);

            if (!hasSoulFire || !hasSoulSand) {
                return;
            }
        }

        int remainingSourceSpace = MAX_SOURCE_STORAGE - blockEntity.storedSource;
        if (remainingSourceSpace <= 0) {
            return;
        }

        long feNeededForRemaining = (long) Math.ceil(MAX_FE_CONSUME_PER_TICK * Math.pow(remainingSourceSpace / (double) MAX_SOURCE_STORAGE, 2));
        int feToConsume = (int) Math.min(MAX_FE_CONSUME_PER_TICK, Math.min(Integer.MAX_VALUE, feNeededForRemaining));
        if (feToConsume <= 0) {
            return;
        }

        int feConsumed = blockEntity.energyStorage.extractEnergy(feToConsume, false);
        if (feConsumed <= 0) {
            return;
        }

        int sourceGenerated = (int) Math.floor(MAX_SOURCE_STORAGE * Math.sqrt(feConsumed / (double) MAX_FE_CONSUME_PER_TICK));
        sourceGenerated = Math.min(sourceGenerated, remainingSourceSpace);
        if (sourceGenerated > 0) {
            blockEntity.storedSource += sourceGenerated;
            blockEntity.setChanged(level, pos, state);

            if (level.random.nextFloat() < 0.01F) {
                BlockPos firePos = pos.below();
                if (level.getBlockState(firePos).is(Blocks.SOUL_FIRE)) {
                    level.setBlockAndUpdate(firePos, Blocks.AIR.defaultBlockState());
                }
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        tag.putInt("source", storedSource);
        tag.put("eternal_fire_recombinator.energy", energyStorage.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        storedSource = tag.getInt("source");

        if (tag.contains("eternal_fire_recombinator.energy")) {
            energyStorage.deserializeNBT(registries, tag.get("eternal_fire_recombinator.energy"));
        }
    }

    @Override
    public boolean canAcceptSource(int source) {
        return false;
    }

    @Override
    public boolean canProvideSource(int source) {
        return true;
    }

    @Override
    public int getMaxExtract() {
        return 1024;
    }

    @Override
    public int getMaxReceive() {
        return 0;
    }

    @Override
    public int getSource() {
        return storedSource;
    }

    @Override
    public int getSourceCapacity() {
        return MAX_SOURCE_STORAGE;
    }

    @Override
    public void setSource(int source) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setSource'");
    }

    @Override
    public void setMaxSource(int max) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setMaxSource'");
    }

    @Override
    public int receiveSource(int source, boolean simulate) {
        return 0;
    }

    @Override
    public int extractSource(int source, boolean simulate) {
        var toExtract = Math.min(storedSource, source);

        if (toExtract > 0 && !simulate) {
            storedSource -= toExtract;
        }

        return toExtract;
    }
}
