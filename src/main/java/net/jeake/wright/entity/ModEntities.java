package net.jeake.wright.entity;

import net.jeake.wright.Wright;
import net.jeake.wright.entity.custom.MonoplaneEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEntities {
    public static final EntityType<MonoplaneEntity> PLANE = Registry.register(Registries.ENTITY_TYPE,
            Identifier.of(Wright.MOD_ID, "monoplane"),
            EntityType.Builder.create(MonoplaneEntity::new, SpawnGroup.MISC)
                    .dimensions(1.5f, 1.0f)
                    .build());

    public static void registerModEntities() {
        Wright.LOGGER.info("Registering Mod Entities for " + Wright.MOD_ID);
    }
}
