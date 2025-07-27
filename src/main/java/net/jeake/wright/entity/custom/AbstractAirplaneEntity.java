package net.jeake.wright.entity.custom;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
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
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

public abstract class AbstractAirplaneEntity extends VehicleEntity {

    private int lerpTicks = 0;
    private double x;
    private double y;
    private double z;
    private float pitch;
    private float roll;
    private float yaw;

    // Quaternion for aircraft orientation
    private Quaternionf orientation = new Quaternionf();

    Vec3d currentPos;
    Vec3d lastPos;

    Vec3d currentRotation;
    Vec3d lastRotation;

    Vec3d currentVelocity;
    Vec3d lastVelocity;

    Vec3d currentRotationVelocity;
    Vec3d lastRotationVelocity;

    float throttle;
    float THROTTLE_INCREMENT = 0.05F;
    float enginePower;
    float ENGINE_POWER_INCREMENT = 0.01F;
    float MAX_SPEED = 1F;

    // Rotation control constants
    float PITCH_INCREMENT = 4.0F; // degrees per tick
    float ROLL_INCREMENT = 5.0F;  // degrees per tick
    float YAW_INCREMENT = 3.0F;   // degrees per tick
    // Removed MAX_PITCH and MAX_ROLL for unlimited aerobatic freedom

    int BASE_WEIGHT;
    final int MAX_PASSENGERS = 1;

    public AbstractAirplaneEntity(EntityType<?> type, World world) {
        super(type, world);
        this.roll = 0f;
        this.currentVelocity = new Vec3d(0, 0, 0);
        this.currentRotationVelocity = new Vec3d(0, 0, 0);
        // Initialize quaternion with identity (no rotation)
        this.orientation = new Quaternionf();
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

    public float getEnginePower() {
        return this.enginePower;
    }

    @Override
    public void updatePassengerPosition(Entity passenger, PositionUpdater positionUpdater) {
        super.updatePassengerPosition(passenger, positionUpdater);
        
        // Camera is now free - no forced rotation following
    }

    @Override
    public void tick() {
        // Initialize quaternion from current angles on first tick
        if (this.age == 1) {
            this.updateQuaternionFromEulerAngles();
        }
        
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

        // Update movement based on engine power
        this.updateMovement();

        // Sync visual rotation with flight physics
        this.syncVisualRotation();

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

    private void updateMovement() {
        // Calculate forward movement based on engine power
        double speed = this.enginePower * MAX_SPEED;
        
        // Convert angles to radians for trigonometry
        double yawRadians = Math.toRadians(this.getYaw());
        double pitchRadians = Math.toRadians(this.getPitch());
        
        // Calculate forward direction based on current orientation
        double forwardX = -Math.sin(yawRadians) * Math.cos(pitchRadians);
        double forwardY = -Math.sin(pitchRadians);
        double forwardZ = Math.cos(yawRadians) * Math.cos(pitchRadians);
        
        // Calculate velocity in forward direction
        Vec3d velocity = new Vec3d(forwardX * speed, forwardY * speed, forwardZ * speed);
        
        // Set the velocity
        this.setVelocity(velocity);
        this.currentVelocity = velocity;
    }

    private void syncVisualRotation() {
        // Update the entity's visual rotation to match flight physics
        // The yaw is already being set correctly, but we need to handle pitch and roll
        
        // Update the entity's pitch to match our custom pitch
        super.setPitch(this.getPitch());
        
        // For roll, we'll need to handle this in the renderer since Minecraft entities don't natively support roll
        // The roll value is already accessible via getRoll() for the renderer to use
        
        // Update yaw (this should already be working)
        super.setYaw(this.getYaw());
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

    public float getThrottle() {
        return this.throttle;
    }

    private void handleInputs() {
        // Throttle controls
        if (KeyInputHandler.throttleUpKey.isPressed()) {
            this.setThrottle(this.getThrottle() + THROTTLE_INCREMENT);
        }

        if (KeyInputHandler.throttleDownKey.isPressed()) {
            this.setThrottle(this.getThrottle() - THROTTLE_INCREMENT);
        }

        // Create rotation quaternions for each axis relative to aircraft
        Quaternionf pitchRotation = new Quaternionf();
        Quaternionf rollRotation = new Quaternionf();
        Quaternionf yawRotation = new Quaternionf();
        
        // Pitch controls - rotate around aircraft's local Z axis (nose to tail)
        if (KeyInputHandler.pitchUpKey.isPressed()) {
            pitchRotation = pitchRotation.rotateZ((float)Math.toRadians(-PITCH_INCREMENT)); // Nose up
        }
        if (KeyInputHandler.pitchDownKey.isPressed()) {
            pitchRotation = pitchRotation.rotateZ((float)Math.toRadians(PITCH_INCREMENT)); // Nose down
        }
        
        // Roll controls - rotate around aircraft's local X axis (wing tip to wing tip)
        if (KeyInputHandler.rollLeftKey.isPressed()) {
            rollRotation = rollRotation.rotateX((float)Math.toRadians(-ROLL_INCREMENT)); // Left wing down
        }
        if (KeyInputHandler.rollRightKey.isPressed()) {
            rollRotation = rollRotation.rotateX((float)Math.toRadians(ROLL_INCREMENT)); // Right wing down
        }
        
        // Yaw controls - rotate around aircraft's local Y axis (vertical)
        if (KeyInputHandler.yawLeftKey.isPressed()) {
            yawRotation = yawRotation.rotateY((float)Math.toRadians(-YAW_INCREMENT)); // Nose left
        }
        if (KeyInputHandler.yawRightKey.isPressed()) {
            yawRotation = yawRotation.rotateY((float)Math.toRadians(YAW_INCREMENT)); // Nose right
        }

        // Update the orientation quaternion based on input rotations
        // Apply the rotations in the correct order for aircraft: Roll -> Pitch -> Yaw
        // This ensures proper aircraft movement behavior
        this.orientation = this.orientation.mul(rollRotation);
        this.orientation = this.orientation.mul(pitchRotation);
        this.orientation = this.orientation.mul(yawRotation);

        // Update the Euler angles from the quaternion
        this.updateEulerAnglesFromQuaternion();
    }
    
    private void updateEulerAnglesFromQuaternion() {
        double qx = this.orientation.x;
        double qy = this.orientation.y;
        double qz = this.orientation.z;
        double qw = this.orientation.w;

        // Test for singularity
        double test = qx * qy + qz * qw;
        if (test > 0.499999) { // singularity at north pole
            this.setYaw((float) Math.toDegrees(2 * Math.atan2(qx, qw)));
            this.setPitch((float) Math.toDegrees(Math.PI / 2));
            this.setRoll(0);
        }
        else if (test < -0.499999) { // singularity at south pole
            this.setYaw((float) Math.toDegrees(-2 * Math.atan2(qx, qw)));
            this.setPitch((float) Math.toDegrees(-Math.PI / 2));
            this.setRoll(0);
        } else {
            double sqx = qx * qx;
            double sqy = qy * qy;
            double sqz = qz * qz;
            this.setYaw((float) Math.toDegrees(Math.atan2(2 * qy * qw - 2 * qx * qz, 1 - 2 * sqy - 2 * sqz)));
            this.setPitch((float) Math.toDegrees(Math.asin(2 * test)));
            this.setRoll((float) Math.toDegrees(Math.atan2(2 * qx * qw - 2 * qy * qz, 1 - 2 * sqx - 2 * sqz)));
        }
        
    }
    
    private void updateQuaternionFromEulerAngles() {
        // Convert degrees to radians
        double rollRad = Math.toRadians(this.getRoll());
        double pitchRad = Math.toRadians(this.getPitch());
        double yawRad = Math.toRadians(this.getYaw());
        
        // Calculate quaternion components
        float cy = (float) Math.cos(yawRad * 0.5);
        float sy = (float) Math.sin(yawRad * 0.5);
        float cp = (float) Math.cos(pitchRad * 0.5);
        float sp = (float) Math.sin(pitchRad * 0.5);
        float cr = (float) Math.cos(rollRad * 0.5);
        float sr = (float) Math.sin(rollRad * 0.5);
        
        // Create quaternion from Euler angles
        this.orientation = new Quaternionf(
            cr * cp * cy + sr * sp * sy,
            sr * cp * cy - cr * sp * sy,
            cr * sp * cy + sr * cp * sy,
            cr * cp * sy - sr * sp * cy
        );
    }
}
