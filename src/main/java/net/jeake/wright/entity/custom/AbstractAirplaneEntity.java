package net.jeake.wright.entity.custom;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.jeake.wright.Wright;
import net.jeake.wright.event.KeyInputHandler;
import net.jeake.wright.networking.packet.PlaneSyncS2CPayload;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.VehicleEntity;
import net.minecraft.registry.tag.FluidTags;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractAirplaneEntity extends VehicleEntity {

    private int lerpTicks = 0;
    private double x;
    private double y;
    private double z;
    private float pitch;
    private float roll;
    private float yaw;

    Vec3d currentPos;
    Vec3d lastPos;

    Vec3d currentRotation;
    Vec3d lastRotation;

    Vec3d currentVelocity;
    Vec3d lastVelocity;

    Vec3d currentRotationVelocity;
    Vec3d lastRotationVelocity;

    float throttle;
    float THROTTLE_INCREMENT = 0.01F;
    float enginePower;
    float ENGINE_POWER_INCREMENT = 0.001F;
    float MAX_SPEED = 10F;

    int BASE_WEIGHT;
    final int MAX_PASSENGERS = 1;

    public AbstractAirplaneEntity(EntityType<?> type, World world) {
        super(type, world);
        this.roll = 0f;
        this.currentVelocity = new Vec3d(0, 0, 0);
        this.currentRotationVelocity = new Vec3d(0, 0, 0);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        //Cannot be null
        super.initDataTracker(builder);
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        ActionResult actionResult = super.interact(player, hand);
        if (actionResult != ActionResult.PASS) {
            return actionResult;
        } else if (player.shouldCancelInteraction()) {
            return ActionResult.PASS;
        } else {
            if (!this.getWorld().isClient) {

                return player.startRiding(this) ? ActionResult.CONSUME : ActionResult.PASS;
            } else {
                return ActionResult.SUCCESS;
            }
        }
    }

    @Override
    public boolean isCollidable() {
        return true;
    }

    @Override
    public boolean canHit() {
        return !this.isRemoved();
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengerList().size() < this.getMaxPassengers() && !this.isSubmergedIn(FluidTags.WATER);
    }

    protected int getMaxPassengers() {
        return MAX_PASSENGERS;
    }

    @Nullable
    public LivingEntity getControllingPassenger() {
        Entity firstPassenger = this.getFirstPassenger();
        LivingEntity controllingPassenger;
        if (firstPassenger instanceof LivingEntity livingEntity) {
            controllingPassenger = livingEntity;
        } else {
            controllingPassenger = super.getControllingPassenger();
        }
        return controllingPassenger;
    }

    public float getRoll() {
        return this.roll;
    }

    public void setRoll(float roll) {
        this.roll = roll;
    }

    public Vec3d getRotation() {
        return new Vec3d(this.getPitch(), this.getRoll(), this.getYaw());
    }

    @Override
    public void tick() {
        // Handle inputs
        if (!getPassengerList().isEmpty()) {
            Entity pilot = this.getFirstPassenger();
            if (pilot != null) {
                if (pilot instanceof PlayerEntity) {
                    this.handleInputs();
                }
            }
        }

        // Match engine power to desired throttle
        if (this.throttle > this.enginePower) {
            this.enginePower += Math.min(ENGINE_POWER_INCREMENT, this.throttle - this.enginePower);
        } else if (this.throttle < this.enginePower) {
            this.enginePower -= Math.min(ENGINE_POWER_INCREMENT, this.enginePower - this.throttle);
        }

        if (this.isLogicalSideForUpdatingMovement()) {
            if (!this.getWorld().isClient) {
                // Sync the entity with each player
                for (PlayerEntity player : this.getWorld().getPlayers()) {
                    ServerPlayNetworking.send((ServerPlayerEntity) player, PlaneSyncS2CPayload.sync(this));
                }
            }
            this.move(MovementType.SELF, this.getVelocity());
            this.rotate(this.getRotationVelocity());
        }

        super.tick();
        this.updatePositionAndRotation();
    }

    private void setRotation(float pitch, float roll, float yaw) {
        this.setPitch(pitch);
        this.setRoll(roll);
        this.setYaw(yaw);
    }

    private void rotate(Vec3d rotationVelocity) {
        this.setRotation(this.getPitch() + (float) rotationVelocity.x,
                    this.getRoll() + (float) rotationVelocity.y,
                    this.getYaw() + (float) rotationVelocity.z);
    }

    private void setRotationVelocity(float pitch, float roll, float yaw) {
        this.currentRotationVelocity = new Vec3d(pitch, roll, yaw);
    }

    private Vec3d getRotationVelocity() {
        return this.currentRotationVelocity;
    }

    @Override
    protected void lerpPosAndRotation(int step, double x, double y, double z, double yaw, double pitch) {
        double d = (double)1.0F / (double)step;
        double e = MathHelper.lerp(d, this.getX(), x);
        double f = MathHelper.lerp(d, this.getY(), y);
        double g = MathHelper.lerp(d, this.getZ(), z);
        float h = (float)MathHelper.lerpAngleDegrees(d, this.getYaw(), yaw);
        float i = (float)MathHelper.lerp(d, this.getPitch(), pitch);
        this.setPosition(e, f, g);
        this.setRotation(h, i);
    }

    private void updatePositionAndRotation() {
        if (this.isLogicalSideForUpdatingMovement()) {
            this.lerpTicks = 0;
            this.updateTrackedPosition(this.getX(), this.getY(), this.getZ());
        }

        if (this.lerpTicks > 0) {
            this.lerpPosAndRotation(this.lerpTicks, this.x, this.y, this.z, this.yaw, this.pitch);
            this.lerpTicks--;
        }
    }

    @Override
    public void updateTrackedPositionAndAngles(double x, double y, double z, float yaw, float pitch, int interpolationSteps) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.lerpTicks = 10;
    }

    @Override
    public double getLerpTargetX() {
        return this.lerpTicks > 0 ? this.x : this.getX();
    }

    @Override
    public double getLerpTargetY() {
        return this.lerpTicks > 0 ? this.y : this.getY();
    }

    @Override
    public double getLerpTargetZ() {
        return this.lerpTicks > 0 ? this.z : this.getZ();
    }

    @Override
    public float getLerpTargetPitch() {
        return this.lerpTicks > 0 ? this.pitch : this.getPitch();
    }

    @Override
    public float getLerpTargetYaw() {
        return this.lerpTicks > 0 ? this.yaw : this.getYaw();
    }

    private void setThrottle(float throttle) {
        this.throttle = Math.round(MathHelper.clamp(throttle, 0, 1) * 1000) / 1000.0F;
    }

    private float getThrottle() {
        return this.throttle;
    }

    private void handleInputs() {
        if (KeyInputHandler.throttleUpKey.isPressed()) {
            this.setThrottle(this.getThrottle() + THROTTLE_INCREMENT);
            if (this.getControllingPassenger() instanceof PlayerEntity player) {
                Wright.LOGGER.debug("Throttle Up: {}", this.getThrottle());
                player.sendMessage(Text.of("Throttle Up: " + this.getThrottle()));
            }
        }

        if (KeyInputHandler.throttleDownKey.isPressed()) {
            this.setThrottle(this.getThrottle() - THROTTLE_INCREMENT);
            if (this.getControllingPassenger() instanceof PlayerEntity player) {
                Wright.LOGGER.debug("Throttle Down: {}", this.getThrottle());
                player.sendMessage(Text.of("Throttle Down: " + this.getThrottle()));
            }
        }
    }
}
