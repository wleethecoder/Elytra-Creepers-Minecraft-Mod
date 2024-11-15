package com.leecrafts.elytracreepers.capability.custom;

import com.leecrafts.elytracreepers.util.NeuralNetwork;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.UnknownNullability;

public class NeuralHandler implements INeuralHandler, INBTSerializable<CompoundTag> {

    private final NeuralNetwork neuralNetwork;
    private int num;

    public NeuralHandler() {
        this.num = 0;
        this.neuralNetwork = new NeuralNetwork();
    }

    @Override
    public NeuralNetwork getNetwork() {
        return this.neuralNetwork;
    }

    @Override
    public int getNum() {
        return 0;
    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("num", this.num);
        return nbt;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        this.num = nbt.getInt("num");
    }

}
