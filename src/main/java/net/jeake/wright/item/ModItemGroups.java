package net.jeake.wright.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.jeake.wright.Wright;
import net.jeake.wright.block.ModBlocks;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroups {

    public static final ItemGroup PINK_GARNET_ITEMS_GROUP = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(Wright.MOD_ID, "wrightmod_items"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(ModItems.MONOPLANE))
                    .displayName(Text.translatable("itemgroup.wrightmod.wrightmod_items"))
                    .entries((displayContext, entries) -> {
                        entries.add(ModItems.PINK_GARNET);
                        entries.add(ModItems.MONOPLANE);
                    })
                    .build());

    public static final ItemGroup PINK_GARNET_BLOCKS_GROUP = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(Wright.MOD_ID, "pink_garnet_blocks"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(ModBlocks.PINK_GARNET_BLOCK))
                    .displayName(Text.translatable("itemgroup.wrightmod.pink_garnet_blocks"))
                    .entries((displayContext, entries) -> {
                        entries.add(ModBlocks.PINK_GARNET_BLOCK);
                    })
                    .build());

    public static void registerItemGroups() {
        Wright.LOGGER.info("Registering Item Groups for " + Wright.MOD_ID);
    }

}
