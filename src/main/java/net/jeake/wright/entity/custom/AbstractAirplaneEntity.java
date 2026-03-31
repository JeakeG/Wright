package net.jeake.wright.entity.custom;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.jeake.wright.event.KeyInputHandler;
import net.jeake.wright.networking.packet.PlaneSyncS2CPayload;
import net.jeake.wright.util.MathUtil;
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
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public abstract class AbstractAirplaneEntity extends VehicleEntity {

    private int lerpTicks = 0;
    private double x;
    private double y;
    private double z;
    private float pitch;
    private float roll;
    private float yaw;

    private Quaternionf orientation;

    Vec3d currentPos;
    Vec3d lastPos;

    Vec3d currentRotation;
    Vec3d lastRotation;

    Vec3d currentVelocity;
    Vec3d lastVelocity;

    // Quaternion-based visual smoothing — avoids Euler discontinuities during slerp
    private Quaternionf smoothedOrientation = new Quaternionf();
    private Quaternionf prevSmoothedOrientation = new Quaternionf();
    private float rotationSmoothFactor = 0.2f;

    float throttle;
    float THROTTLE_INCREMENT = 0.05F;
    float enginePower;
    float ENGINE_POWER_INCREMENT = 0.01F;
    float MAX_SPEED = 1F;

    // Rotation control constants
    float PITCH_INCREMENT = 4.0F; // degrees per tick
    float ROLL_INCREMENT = 5.0F;  // degrees per tick
    float YAW_INCREMENT = 3.0F;   // degrees per tick

    int BASE_WEIGHT;
    final int MAX_PASSENGERS = 1;

    public AbstractAirplaneEntity(EntityType<?> type, World world) {
        super(type, world);
        this.roll = 0f;
        this.currentVelocity = new Vec3d(0, 0, 0);
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

    public Quaternionf getOrientation() {
        return this.orientation;
    }

    public void setOrientation(Quaternionf orientation) {
        this.orientation = orientation;
    }

    @Override
    public void updatePassengerPosition(Entity passenger, PositionUpdater positionUpdater) {
        super.updatePassengerPosition(passenger, positionUpdater);
        
        // Camera yaw/pitch are handled in CameraMixin each frame using tickDelta.
        // Setting them here (tick-rate, 20Hz) conflicted with mouse input, causing jitter.
    }

    @Override
    public void tick() {
        // Initialize quaternions from current angles on first tick
        if (this.age == 1) {
            this.setOrientation(MathUtil.euler2Quaternion(this.getAngles()));
            this.smoothedOrientation = new Quaternionf(this.orientation);
            this.prevSmoothedOrientation = new Quaternionf(this.orientation);
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
        // Snapshot previous smoothed orientation for sub-tick interpolation
        this.prevSmoothedOrientation = new Quaternionf(this.smoothedOrientation);
        // Slerp toward the physics quaternion — avoids Euler discontinuities entirely
        this.smoothedOrientation.slerp(this.orientation, rotationSmoothFactor);

        // Keep vanilla pitch/yaw in sync so the camera and passenger positioning stay correct
        float[] angles = MathUtil.quaternion2Euler(this.smoothedOrientation);
        super.setPitch(angles[1]);
        super.setYaw(angles[0]);
    }

    /** Returns Euler angles [yaw, pitch, roll] from a sub-tick slerp between prev and current smoothed orientation. */
    private float[] getSmoothedEuler(float tickDelta) {
        Quaternionf interpolated = new Quaternionf(this.prevSmoothedOrientation).slerp(this.smoothedOrientation, tickDelta);
        return MathUtil.quaternion2Euler(interpolated);
    }

    /** Returns the sub-tick interpolated smoothed orientation quaternion directly, without Euler extraction. */
    public Quaternionf getSmoothedOrientation(float tickDelta) {
        return new Quaternionf(this.prevSmoothedOrientation).slerp(this.smoothedOrientation, tickDelta);
    }

    // Tick-accurate getters (used for passenger yaw/pitch sync)
    public float getVisualRoll() {
        return MathUtil.quaternion2Euler(this.smoothedOrientation)[2];
    }

    public float getVisualPitch() {
        return MathUtil.quaternion2Euler(this.smoothedOrientation)[1];
    }

    public float getVisualYaw() {
        return MathUtil.quaternion2Euler(this.smoothedOrientation)[0];
    }

    // Sub-tick interpolated getters for smooth rendering at high framerates
    public float getVisualRoll(float tickDelta) {
        return getSmoothedEuler(tickDelta)[2];
    }

    public float getVisualPitch(float tickDelta) {
        return getSmoothedEuler(tickDelta)[1];
    }

    public float getVisualYaw(float tickDelta) {
        return getSmoothedEuler(tickDelta)[0];
    }

    /**
     * Returns the aircraft's local lift axis in world space using visual (interpolated) rotation.
     * This matches the renderer's yaw/pitch/roll composition so debug vectors and visuals align.
     */
    public Vec3d getLiftVector(float tickDelta) {
        return this.computeLiftVector(
            this.getVisualYaw(tickDelta),
            this.getVisualPitch(tickDelta),
            this.getVisualRoll(tickDelta)
        );
    }

    /**
     * Returns the aircraft's local lift axis in world space from the current physics orientation.
     * Use this overload for simulation/tick logic where sub-tick interpolation is not needed.
     */
    public Vec3d getLiftVector() {
        float[] angles = MathUtil.quaternion2Euler(this.orientation);
        return this.computeLiftVector(angles[0], angles[1], angles[2]);
    }

    /**
     * Returns the aircraft's local forward axis in world space using visual (interpolated) rotation.
     * Useful for render-time debug vectors and effects that should match visual orientation.
     */
    public Vec3d getThrustVector(float tickDelta) {
        return this.computeThrustVector(
            this.getVisualYaw(tickDelta),
            this.getVisualPitch(tickDelta),
            this.getVisualRoll(tickDelta)
        );
    }

    /**
     * Returns the aircraft's local forward axis in world space from current physics orientation.
     * Use this overload for simulation/tick logic where sub-tick interpolation is not needed.
     */
    public Vec3d getThrustVector() {
        float[] angles = MathUtil.quaternion2Euler(this.orientation);
        return this.computeThrustVector(angles[0], angles[1], angles[2]);
    }

    /**
     * Returns the aircraft's local drag axis in world space using visual (interpolated) rotation.
     * Drag points opposite the aircraft forward axis.
     */
    public Vec3d getDragVector(float tickDelta) {
        return this.computeDragVector(
            this.getVisualYaw(tickDelta),
            this.getVisualPitch(tickDelta),
            this.getVisualRoll(tickDelta)
        );
    }

    /**
     * Returns the aircraft's local drag axis in world space from current physics orientation.
     * Use this overload for simulation/tick logic where sub-tick interpolation is not needed.
     */
    public Vec3d getDragVector() {
        float[] angles = MathUtil.quaternion2Euler(this.orientation);
        return this.computeDragVector(angles[0], angles[1], angles[2]);
    }

    private Vec3d computeLiftVector(float yaw, float pitch, float roll) {
        // Mirror the exact transform sequence used by rendering:
        // scale(1,-1,-1) -> yaw(Y) -> pitch(X) -> roll(Z, negated), then transform local -Y.
        Matrix3f transform = new Matrix3f()
            .identity()
            .scale(1.0F, -1.0F, -1.0F)
            .rotateY((float) Math.toRadians(yaw))
            .rotateX((float) Math.toRadians(pitch))
            .rotateZ((float) Math.toRadians(-roll));

        Vector3f lift = transform.transform(new Vector3f(0.0F, -1.0F, 0.0F)).normalize();
        return new Vec3d(lift.x, lift.y, lift.z);
    }

    private Vec3d computeThrustVector(float yaw, float pitch, float roll) {
        // Same transform chain as computeLiftVector; local -Z is aircraft forward in model space.
        Matrix3f transform = new Matrix3f()
            .identity()
            .scale(1.0F, -1.0F, -1.0F)
            .rotateY((float) Math.toRadians(yaw))
            .rotateX((float) Math.toRadians(pitch))
            .rotateZ((float) Math.toRadians(-roll));

        Vector3f thrust = transform.transform(new Vector3f(0.0F, 0.0F, -1.0F)).normalize();
        return new Vec3d(thrust.x, thrust.y, thrust.z);
    }

    private Vec3d computeDragVector(float yaw, float pitch, float roll) {
        // Same transform chain as computeThrustVector; local +Z is opposite aircraft forward.
        Matrix3f transform = new Matrix3f()
            .identity()
            .scale(1.0F, -1.0F, -1.0F)
            .rotateY((float) Math.toRadians(yaw))
            .rotateX((float) Math.toRadians(pitch))
            .rotateZ((float) Math.toRadians(-roll));

        Vector3f drag = transform.transform(new Vector3f(0.0F, 0.0F, 1.0F)).normalize();
        return new Vec3d(drag.x, drag.y, drag.z);
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
        float[] angles = MathUtil.quaternion2Euler(this.orientation);
        this.setAngles(angles[0], angles[1], angles[2]);
    }

    private void setAngles(float yaw, float pitch, float roll) {
        this.setYaw(yaw);
        this.setPitch(pitch);
        this.setRoll(roll);
    }

    private float[] getAngles() {
        return new float[]{this.getPitch(), this.getYaw(), this.getRoll()};
    }

}
