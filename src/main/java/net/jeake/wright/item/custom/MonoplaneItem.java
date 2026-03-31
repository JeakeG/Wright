package net.jeake.wright.item.custom;

import net.jeake.wright.entity.ModEntities;
import net.jeake.wright.entity.custom.MonoplaneEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.VehicleEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.event.GameEvent;

public class MonoplaneItem extends Item {

    public MonoplaneItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        BlockPos blockPos = context.getBlockPos().add(0, 1, 0);

        VehicleEntity entity = new MonoplaneEntity(ModEntities.PLANE, context.getWorld());
//        entity.setPos(blockPos.getX(), blockPos.getY()+1, blockPos.getZ());
        entity.refreshPositionAndAngles(blockPos, context.getPlayerYaw(), 0);

        if (!context.getWorld().isClient()) {
            context.getWorld().spawnEntity(entity);
            context.getWorld().emitGameEvent(player, GameEvent.ENTITY_PLACE, blockPos);

            if (!player.isCreative()) {
                context.getStack().decrement(1);
            }
        }

        return super.useOnBlock(context);
    }

}
