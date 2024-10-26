package com.leecrafts.elytracreepers.item;

import com.leecrafts.elytracreepers.ElytraCreepers;
import com.leecrafts.elytracreepers.item.custom.NeuralElytra;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ElytraCreepers.MODID);

    public static final DeferredItem<Item> NEURAL_ELYTRA = ITEMS.register(
            "neural_elytra", () -> new NeuralElytra(new Item.Properties().durability(432).rarity(Rarity.UNCOMMON)));

    public static void regsiter(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

}
