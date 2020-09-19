package me.soapsuds.panamax.client;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import me.soapsuds.panamax.Panamax;
import me.soapsuds.panamax.utils.ToggleableClippingHelper;
import net.java.games.input.Keyboard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;



public class ClientEvents {
	public static final String MAIN_CATEGORY = "keys.panamax.main";
	public static KeyBinding CAMERA_MODE = new KeyBinding("key.panamax.camera_mode", GLFW.GLFW_KEY_RIGHT_CONTROL, MAIN_CATEGORY);
	public static KeyBinding ZOOM_IN = new KeyBinding("key.panamax.zoom.in", GLFW.GLFW_KEY_KP_EQUAL, MAIN_CATEGORY);
	public static KeyBinding ZOOM_OUT = new KeyBinding("key.panamax.zoom.out", GLFW.GLFW_KEY_MINUS, MAIN_CATEGORY);
	public static KeyBinding ROTATE_LEFT = new KeyBinding("key.panamax.rotate.left", GLFW.GLFW_KEY_LEFT, MAIN_CATEGORY);
	public static KeyBinding ROTATE_RIGHT = new KeyBinding("key.panamax.rotate.right", GLFW.GLFW_KEY_RIGHT, MAIN_CATEGORY);
	public static KeyBinding ROTATE_UP = new KeyBinding("key.panamax.rotate.up", GLFW.GLFW_KEY_UP, MAIN_CATEGORY);
	public static KeyBinding ROTATE_DOWN = new KeyBinding("key.panamax.rotate.down", GLFW.GLFW_KEY_DOWN, MAIN_CATEGORY);
	public static KeyBinding CLIP = new KeyBinding("key.panamax.clip", GLFW.GLFW_KEY_INSERT, MAIN_CATEGORY);
	public static KeyBinding ROTATE_TOP = new KeyBinding("key.panamax.rotate.top", GLFW.GLFW_KEY_HOME, MAIN_CATEGORY);
	public static KeyBinding ROTATE_FRONT = new KeyBinding("key.panamax.rotate.front", GLFW.GLFW_KEY_PAGE_DOWN, MAIN_CATEGORY);
    public static KeyBinding ROTATE_SIDE = new KeyBinding("key.panamax.rotate.side", GLFW.GLFW_KEY_END, MAIN_CATEGORY);
	public static KeyBinding CAMERA_MODIFIER = new KeyBinding("key.panamax.camera.modifier", GLFW.GLFW_KEY_LEFT_CONTROL, MAIN_CATEGORY);
	
	private static ToggleableClippingHelper clipperHelper;
	private static Minecraft mc = Minecraft.getInstance();
	private boolean switchPerspective;
	private boolean isClipperEnabled;
	private boolean enabled;
	private boolean freeCam;
	private boolean clip;
	private float zoom;
    private float xRot;
    private float yRot;
    
    private int tick;
    private int tickPrevious;
    private double partialPrevious;
    
    private static final float ZOOM_STEP = 0.5f;
    private static final float ROTATE_STEP = 15;
    private static final float ROTATE_SPEED = 4;
    private static final float SECONDS_PER_TICK = 1f/20f;
    
    public ClientEvents() {
    	reset();
    }
    
	public static void registerClientObjects() {
		ClientRegistry.registerKeyBinding(CAMERA_MODE);
		ClientRegistry.registerKeyBinding(ZOOM_IN);
		ClientRegistry.registerKeyBinding(ZOOM_OUT);
		ClientRegistry.registerKeyBinding(ROTATE_LEFT);
		ClientRegistry.registerKeyBinding(ROTATE_RIGHT);
		ClientRegistry.registerKeyBinding(ROTATE_UP);
		ClientRegistry.registerKeyBinding(ROTATE_DOWN);
		ClientRegistry.registerKeyBinding(ROTATE_TOP);
		ClientRegistry.registerKeyBinding(ROTATE_FRONT);
		ClientRegistry.registerKeyBinding(ROTATE_SIDE);
		ClientRegistry.registerKeyBinding(CLIP);
		ClientRegistry.registerKeyBinding(CAMERA_MODIFIER);
	}
	
	@SubscribeEvent
    public void onClientTick(ClientTickEvent event) {
		if (!enabled || event.phase != Phase.START) {
            return;
        }
        tick++;
	}
	
	@SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
		boolean mod = modifierKeyPressed();
		if(ClientEvents.CAMERA_MODE.isPressed()) {
			//Switch through modes
			if (mod) {
	             freeCam = !freeCam;
	             System.out.println(freeCam);
	         } else {
	        	 toggleCameraMode();
	         } 
		}
		else if (CLIP.isKeyDown()) {
		     clip = !clip;
		} else if (ROTATE_TOP.isKeyDown()) {
		     xRot = mod ? -90 : 90;
		     yRot = 0;
		} else if (ROTATE_FRONT.isKeyDown()) {
		     xRot = 0;
		     yRot = mod ? -90 : 90;
		} else if (ROTATE_SIDE.isKeyDown()) {
		     xRot = 0;
		     yRot = mod ? 180 : 0;
		}
		
		if (mod) {            
            updateZoomAndRotation(1);
            
            // snap values to step units
            xRot = Math.round(xRot / ROTATE_STEP) * ROTATE_STEP;
            yRot = Math.round(yRot / ROTATE_STEP) * ROTATE_STEP;
            zoom = Math.round(zoom / ZOOM_STEP) * ZOOM_STEP;
        }
		
	}
	
	@SubscribeEvent
	public void onRenderWorld(RenderWorldLastEvent event) {
		Minecraft mc = Minecraft.getInstance();
		ActiveRenderInfo info = mc.gameRenderer.getActiveRenderInfo();
		Matrix4f proj = event.getProjectionMatrix();
		MatrixStack mtx = event.getMatrixStack();
		clipperHelper = ToggleableClippingHelper.getClippingHelper(mtx, proj, info);
	}
	
	@SubscribeEvent
	public void cameraSetup(CameraSetup event) {
		ActiveRenderInfo info = event.getRenderer().getActiveRenderInfo();
		if (switchPerspective) {
			MatrixStack stack = new MatrixStack();
		}
		
	}
	
	@SubscribeEvent
    public void onFogDensity(EntityViewRenderEvent.FogDensity event) {
		if (!switchPerspective()) {
            return;
        }
		MatrixStack stack = new MatrixStack();
 		ActiveRenderInfo info = event.getRenderer().getActiveRenderInfo();
 		Quaternion quart = info.getRotation();
 		Matrix4f matrix = event.getRenderer().getProjectionMatrix(info, (float) event.getRenderPartialTicks(), false);
		if (!modifierKeyPressed()) {
            int ticksElapsed = tick - tickPrevious;
            double partial = event.getRenderPartialTicks();
            double elapsed = ticksElapsed + (partial - partialPrevious);
            elapsed *= SECONDS_PER_TICK * ROTATE_SPEED;
            updateZoomAndRotation(elapsed);
            
            tickPrevious = tick;
            partialPrevious = partial;
        }
		float width = zoom * (mc.getMainWindow().getScaledWidth() / (float) mc.getMainWindow().getScaledHeight());
        float height = zoom;
        RenderSystem.matrixMode(org.lwjgl.opengl.GL11.GL_PROJECTION);
        RenderSystem.loadIdentity();
        RenderSystem.ortho(-width, width, -height, height, clip ? 0 : -9999, 9999);
     
		if (freeCam) {
            xRot = mc.player.rotationPitch;
            yRot = mc.player.rotationYaw - 180;
        }
		RenderSystem.matrixMode(org.lwjgl.opengl.GL11.GL_MODELVIEW);
		RenderSystem.loadIdentity();
		RenderSystem.rotatef(xRot, 1, 0, 0);
		RenderSystem.rotatef(yRot, 0, 1, 0);
		//Fix particle rotation
		if (!freeCam) {
            float pitch = xRot;
            float yaw = yRot + 180;
           
            quart.set(MathHelper.cos(yaw * (float) Math.PI / 180f),quart.getY() ,quart.getZ(), quart.getW());
            quart.set(quart.getX(),quart.getY() ,MathHelper.sin(yaw * (float) Math.PI / 180f), quart.getW());
            quart.set(quart.getX(),quart.getY() , quart.getZ() * MathHelper.sin(yaw * (float) Math.PI / 180f), quart.getW());
            quart.set(quart.getX()  * MathHelper.sin(yaw * (float) Math.PI / 180f) ,quart.getY() , quart.getZ(), quart.getW());
            quart.set(MathHelper.cos(pitch * (float) Math.PI / 180f) ,quart.getY() , MathHelper.cos(pitch * (float) Math.PI / 180f), quart.getW());
            matrix.setIdentity();
            stack.getLast().getMatrix().mul(matrix);
    		stack.rotate(quart);
		}
	}
	
	public boolean isClipperEnabled() {
		return isClipperEnabled;
	}
	
	public boolean switchPerspective() {
        return switchPerspective;
    }
	
	private void reset() {
        freeCam = false;
        clip = false;
        
        zoom = 8;
        xRot = 30;
        yRot = -45;
        tick = 0;
        tickPrevious = 0;
        partialPrevious = 0;
    }
	
	 private void updateZoomAndRotation(double multi) {
//		System.out.println(zoom);
        if (ZOOM_IN.isKeyDown()) {
            zoom *= 1 - ZOOM_STEP * multi;
        }
        if (ZOOM_OUT.isKeyDown()) {
            zoom *= 1 + ZOOM_STEP * multi;
        }
        
        if (ROTATE_LEFT.isKeyDown()) {
            yRot += ROTATE_STEP * multi;
        }
        if (ROTATE_RIGHT.isKeyDown()) {
            yRot -= ROTATE_STEP * multi;
        }

        if (ROTATE_UP.isKeyDown()) {
            xRot += ROTATE_STEP * multi;
        }
        if (ROTATE_DOWN.isKeyDown()) {
            xRot -= ROTATE_STEP * multi;
        }
    }
	
	public void toggleCameraMode() {
		if(switchPerspective)
			disableCameraMode();
		else
			enableCameraMode();
	}
	
	public void enableCameraMode() {
		if (!switchPerspective) {
			System.out.println("camera enabled");
			isClipperEnabled = clipperHelper.isEnabled();
			clipperHelper.setEnabled(false);
		}
		switchPerspective = true;
	}
	
	public void disableCameraMode() {
		if (switchPerspective) {
			System.out.println("camera disabled");
			clipperHelper.setEnabled(isClipperEnabled);
		}
		switchPerspective = false;
	}
	
	public boolean modifierKeyPressed() {
        return CAMERA_MODIFIER.isKeyDown();
    }

}
