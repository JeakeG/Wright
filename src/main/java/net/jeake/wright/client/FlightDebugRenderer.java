package net.jeake.wright.client;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.jeake.wright.entity.custom.AbstractAirplaneEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;


public class FlightDebugRenderer {

    public static void register() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(FlightDebugRenderer::render);
    }

    private static void render(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || context.consumers() == null) return;
        if (!(client.player.getVehicle() instanceof AbstractAirplaneEntity airplane)) return;

        Vec3d cameraPos = context.camera().getPos();
        Vec3d origin = airplane.getPos().add(0, 1.5, 0);

        MatrixStack matrices = context.matrixStack();
        matrices.push();
        matrices.translate(origin.x - cameraPos.x, origin.y - cameraPos.y, origin.z - cameraPos.z);

        VertexConsumer lines = context.consumers().getBuffer(RenderLayer.LINES);
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        // World down (gravity reference)
        drawArrow(lines, matrix, 0, 0, 0, 0, -2, 0, 1.0f, 0.2f, 0.2f);

        // Lift direction comes from the airplane entity so rendering and physics share one source of truth.
        float tickDelta = context.tickCounter().getTickDelta(true);
        Vec3d liftDirection = airplane.getLiftVector(tickDelta);
        drawArrow(lines, matrix, 0, 0, 0,
            (float) (liftDirection.x * 2.0),
            (float) (liftDirection.y * 2.0),
            (float) (liftDirection.z * 2.0),
            0.2f, 1.0f, 0.2f);

        // Thrust/forward direction for debugging propulsion and nose orientation.
        Vec3d thrustDirection = airplane.getThrustVector(tickDelta);
        drawArrow(lines, matrix, 0, 0, 0,
            (float) (thrustDirection.x * 2.0),
            (float) (thrustDirection.y * 2.0),
            (float) (thrustDirection.z * 2.0),
            0.2f, 0.6f, 1.0f);

        // Drag direction (opposite forward axis) for debugging aerodynamic resistance.
        Vec3d dragDirection = airplane.getDragVector(tickDelta);
        drawArrow(lines, matrix, 0, 0, 0,
            (float) (dragDirection.x * 2.0),
            (float) (dragDirection.y * 2.0),
            (float) (dragDirection.z * 2.0),
            1.0f, 0.75f, 0.2f);

        matrices.pop();
    }

    /**
     * Draws an arrow from (x1,y1,z1) to (x2,y2,z2) using LINES — a shaft plus a 4-spoke arrowhead at the tip.
     * The arrowhead spokes are computed from two axes perpendicular to the arrow direction.
     */
    private static void drawArrow(VertexConsumer lines, Matrix4f matrix,
                                   float x1, float y1, float z1,
                                   float x2, float y2, float z2,
                                   float r, float g, float b) {
        float dx = x2 - x1, dy = y2 - y1, dz = z2 - z1;
        float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        float nx = dx / len, ny = dy / len, nz = dz / len;

        // Shaft
        line(lines, matrix, x1, y1, z1, x2, y2, z2, nx, ny, nz, r, g, b);

        // Build two axes perpendicular to the arrow direction for the arrowhead spokes
        Vector3f dir = new Vector3f(nx, ny, nz);
        // Pick a reference vector that isn't parallel to dir
        Vector3f ref = (Math.abs(ny) < 0.9f) ? new Vector3f(0, 1, 0) : new Vector3f(1, 0, 0);
        Vector3f perp1 = dir.cross(ref, new Vector3f()).normalize();
        Vector3f perp2 = dir.cross(perp1, new Vector3f());

        float headLen = 0.35f;
        float spread = 0.25f;
        float bx = x2 - nx * headLen;
        float by = y2 - ny * headLen;
        float bz = z2 - nz * headLen;

        line(lines, matrix, bx + perp1.x * spread, by + perp1.y * spread, bz + perp1.z * spread, x2, y2, z2, nx, ny, nz, r, g, b);
        line(lines, matrix, bx - perp1.x * spread, by - perp1.y * spread, bz - perp1.z * spread, x2, y2, z2, nx, ny, nz, r, g, b);
        line(lines, matrix, bx + perp2.x * spread, by + perp2.y * spread, bz + perp2.z * spread, x2, y2, z2, nx, ny, nz, r, g, b);
        line(lines, matrix, bx - perp2.x * spread, by - perp2.y * spread, bz - perp2.z * spread, x2, y2, z2, nx, ny, nz, r, g, b);
    }

    /** Emits one line segment (two vertices) into the LINES buffer. */
    private static void line(VertexConsumer consumer, Matrix4f matrix,
                              float x1, float y1, float z1,
                              float x2, float y2, float z2,
                              float nx, float ny, float nz,
                              float r, float g, float b) {
        consumer.vertex(matrix, x1, y1, z1).color(r, g, b, 1.0f).normal(nx, ny, nz);
        consumer.vertex(matrix, x2, y2, z2).color(r, g, b, 1.0f).normal(nx, ny, nz);
    }
}
