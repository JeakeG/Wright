package net.jeake.wright.item;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.jeake.wright.Wright;
import net.jeake.wright.item.custom.MonoplaneItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {

    public static final Item PINK_GARNET = registerItem("pink_garnet", new Item(new Item.Settings()));

    public static final Item MONOPLANE = registerItem("monoplane", new MonoplaneItem(new Item.Settings()));

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(Wright.MOD_ID, name), item);
    }

    public static void registerModItems() {
        Wright.LOGGER.info("Registering Mod Items for " + Wright.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> {
            entries.add(PINK_GARNET);
            entries.add(MONOPLANE);
        });
    }

}