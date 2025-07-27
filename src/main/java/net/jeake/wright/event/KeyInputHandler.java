package net.jeake.wright.event;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.jeake.wright.networking.packet.ThrottleC2SPayload;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class KeyInputHandler {

    public static final String KEY_CATEGORY_WRIGHT = "key.category.wrightmod.plane_controls";

    public static final String KEY_THROTTLE_UP = "key.wrightmod.throttle_up";
    public static final String KEY_THROTTLE_DOWN = "key.wrightmod.throttle_down";
    public static final String KEY_ROLL_LEFT = "key.wrightmod.roll_left";
    public static final String KEY_ROLL_RIGHT = "key.wrightmod.roll_right";
    public static final String KEY_PITCH_UP = "key.wrightmod.pitch_up";
    public static final String KEY_PITCH_DOWN = "key.wrightmod.pitch_down";

    public static KeyBinding throttleUpKey;
    public static KeyBinding throttleDownKey;
    public static KeyBinding rollLeftKey;
    public static KeyBinding rollRightKey;
    public static KeyBinding pitchUpKey;
    public static KeyBinding pitchDownKey;

    public static void registerKeyInputs() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
//            if (throttleUpKey.wasPressed()) {
//                // Handle throttle up key press
//                assert client.player != null;
//                client.player.sendMessage(Text.of("Throttle Up Pressed"));
//                ThrottleC2SPayload payload = new ThrottleC2SPayload(true);
//                ClientPlayNetworking.send(payload);
//            }
//            if (throttleDownKey.wasPressed()) {
//                // Handle throttle down key press
//                assert client.player != null;
//                client.player.sendMessage(Text.of("Throttle Down Pressed"));
//                ThrottleC2SPayload payload = new ThrottleC2SPayload(false);
//                ClientPlayNetworking.send(payload);
//            }
//            if (rollLeftKey.wasPressed()) {
//                // Handle roll left key press
//                assert client.player != null;
//                client.player.sendMessage(Text.of("Roll Left Pressed"));
//            }
//            if (rollRightKey.wasPressed()) {
//                // Handle roll right key press
//                assert client.player != null;
//                client.player.sendMessage(Text.of("Roll Right Pressed"));
//            }
//            if (pitchUpKey.wasPressed()) {
//                // Handle pitch up key press
//                assert client.player != null;
//                client.player.sendMessage(Text.of("Pitch Up Pressed"));
//            }
//            if (pitchDownKey.wasPressed()) {
//                // Handle pitch down key press
//                assert client.player != null;
//                client.player.sendMessage(Text.of("Pitch Down Pressed"));
//            }
        });
    }

    public static void register() {
        throttleUpKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                KEY_THROTTLE_UP,
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_KP_7,
                KEY_CATEGORY_WRIGHT
        ));

        throttleDownKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                KEY_THROTTLE_DOWN,
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_KP_9,
                KEY_CATEGORY_WRIGHT
        ));

        rollLeftKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                KEY_ROLL_LEFT,
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_KP_4,
                KEY_CATEGORY_WRIGHT
        ));

        rollRightKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                KEY_ROLL_RIGHT,
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_KP_6,
                KEY_CATEGORY_WRIGHT
        ));

        pitchUpKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                KEY_PITCH_UP,
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_KP_5,
                KEY_CATEGORY_WRIGHT
        ));

        pitchDownKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                KEY_PITCH_DOWN,
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_KP_8,
                KEY_CATEGORY_WRIGHT
        ));

        registerKeyInputs();
    }

}
