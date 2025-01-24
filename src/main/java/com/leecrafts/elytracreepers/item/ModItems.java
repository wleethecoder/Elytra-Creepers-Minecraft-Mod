package com.leecrafts.elytracreepers.item;

import com.leecrafts.elytracreepers.ElytraCreepers;
import com.leecrafts.elytracreepers.item.custom.NeuralElytra;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.equipment.EquipmentModels;
import net.minecraft.world.item.equipment.Equippable;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ElytraCreepers.MODID);

    public static final DeferredItem<Item> NEURAL_ELYTRA = ITEMS.register(
            "neural_elytra", () -> new NeuralElytra(new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ElytraCreepers.MODID, "neural_elytra")))
                    .durability(432)
                    .rarity(Rarity.UNCOMMON)
                    .component(DataComponents.GLIDER, Unit.INSTANCE)
                    .component(
                            DataComponents.EQUIPPABLE,
                            Equippable.builder(EquipmentSlot.CHEST)
                                    .setEquipSound(SoundEvents.ARMOR_EQUIP_ELYTRA)
                                    .setModel(EquipmentModels.ELYTRA)
                                    .setDamageOnHurt(false)
                                    .build())
                    .repairable(Items.PHANTOM_MEMBRANE)
            ));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

}
