package me.soapsuds.panamax.client;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ClientEvents {
	public static KeyBinding CAMERA_MODE;
	
	public static void registerClientObjects() {
		ClientRegistry.registerKeyBinding(CAMERA_MODE = new KeyBinding("key.panamax.camera_mode", GLFW.GLFW_KEY_KP_ADD, "keys.panamax.main"));
	}
	
	@SubscribeEvent
    public static void onKeyInput(InputEvent.KeyInputEvent event) {
		if(CAMERA_MODE.isPressed()) {
			//Switch through modes
		}
	}
	

}
