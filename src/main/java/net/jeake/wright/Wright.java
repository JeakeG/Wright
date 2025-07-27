package net.jeake.wright;

import net.fabricmc.api.ModInitializer;
import net.jeake.wright.block.ModBlocks;
import net.jeake.wright.component.ModDataComponentTypes;
import net.jeake.wright.entity.ModEntities;
import net.jeake.wright.item.ModItemGroups;
import net.jeake.wright.item.ModItems;
import net.jeake.wright.networking.ModPackets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Wright implements ModInitializer {
    public static final String MOD_ID = "wrightmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModItemGroups.registerItemGroups();

        ModItems.registerModItems();
        ModBlocks.registerModBlocks();
        ModEntities.registerModEntities();

        ModDataComponentTypes.registerDataComponentTypes();

        ModPackets.registerC2SPackets();
    }
}
