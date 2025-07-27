package net.jeake.wright.entity.custom;

import net.jeake.wright.item.custom.MonoplaneItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class MonoplaneEntity extends AbstractAirplaneEntity {

    final int MAX_PASSENGERS = 2;

    public MonoplaneEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {

    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {

    }

    @Override
    public Vec3d getPassengerRidingPos(Entity passenger) {
        return super.getPassengerRidingPos(passenger).add(0, -0.5, 0);
    }

    @Override
    public Item asItem() {
        return new MonoplaneItem(new Item.Settings());
    };

}
