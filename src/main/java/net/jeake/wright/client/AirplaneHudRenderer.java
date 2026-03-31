package net.jeake.wright.client;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.jeake.wright.entity.custom.AbstractAirplaneEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

public class AirplaneHudRenderer implements HudRenderCallback {

    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        
        // Only render HUD if player is riding an airplane
        if (client.player != null && client.player.getVehicle() instanceof AbstractAirplaneEntity airplane) {
            renderAirplaneHUD(drawContext, airplane, client);
        }
    }

    private void renderAirplaneHUD(DrawContext drawContext, AbstractAirplaneEntity airplane, MinecraftClient client) {
        TextRenderer textRenderer = client.textRenderer;
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        // Position the HUD in the top-left corner
        int hudX = 10;
        int hudY = 10;
        int lineHeight = 12;

        // Get airplane data
        float throttle = airplane.getThrottle();
        float enginePower = airplane.getEnginePower();
        Vec3d velocity = airplane.getVelocity();
        double speed = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z); // Horizontal speed
        float pitch = airplane.getPitch();
        float roll = airplane.getRoll();
        float yaw = airplane.getYaw();
        
        // Format and display the information
        String throttleText = String.format("Throttle: %.1f%%", throttle * 100);
        String enginePowerText = String.format("Engine Power: %.1f%%", enginePower * 100);
        String speedText = String.format("Speed: %.2f m/s", speed);
        String velocityText = String.format("Velocity: X:%.2f Y:%.2f Z:%.2f", velocity.x, velocity.y, velocity.z);
        String altitudeText = String.format("Altitude: %.1f", airplane.getY());
        String pitchText = String.format("Pitch: %.1f°", pitch);
        String rollText = String.format("Roll: %.1f°", roll);
        String yawText = String.format("Heading: %.1f°", yaw);

        // Background for better readability
        int hudWidth = 220;
        int hudHeight = 110;
        drawContext.fill(hudX - 5, hudY - 5, hudX + hudWidth, hudY + hudHeight, 0x80000000); // Semi-transparent black

        // Draw the text
        drawContext.drawText(textRenderer, Text.literal("=== AIRPLANE STATUS ==="), hudX, hudY, 0xFFFFFF, false);
        drawContext.drawText(textRenderer, Text.literal(throttleText), hudX, hudY + lineHeight, 0x00FF00, false);
        drawContext.drawText(textRenderer, Text.literal(enginePowerText), hudX, hudY + lineHeight * 2, 0x00FFFF, false);
        drawContext.drawText(textRenderer, Text.literal(speedText), hudX, hudY + lineHeight * 3, 0xFFFF00, false);
        drawContext.drawText(textRenderer, Text.literal(altitudeText), hudX, hudY + lineHeight * 4, 0xAAFFAA, false);
        drawContext.drawText(textRenderer, Text.literal(pitchText), hudX, hudY + lineHeight * 5, 0xFFAAFF, false);
        drawContext.drawText(textRenderer, Text.literal(rollText), hudX, hudY + lineHeight * 6, 0xAAFFFF, false);
        drawContext.drawText(textRenderer, Text.literal(yawText), hudX, hudY + lineHeight * 7, 0xFFFFAA, false);
        drawContext.drawText(textRenderer, Text.literal(velocityText), hudX, hudY + lineHeight * 8, 0xFFAAAA, false);
    }

    public static void register() {
        HudRenderCallback.EVENT.register(new AirplaneHudRenderer());
    }
}
