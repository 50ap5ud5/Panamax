package me.soapsuds.panamax.utils;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
/**
 * A implementation of ClippingHelper that can be toggled on or off
 * @implSpec Used to toggle between using vanilla clipping or modded
 *
 */
public class ToggleableClippingHelper extends ClippingHelper{
	private boolean enabled;
	/*
	 * first param is projection+camera. Camera is the view direction that matches the look vector
	 * second param is entity location
	 */
	public ToggleableClippingHelper(Matrix4f projectionMatrix, Matrix4f entityLocationMatrix) {
		super(projectionMatrix, entityLocationMatrix);
	}
	
	public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public boolean isEnabled() {
        return enabled;
    }

	@Override
	public boolean isBoundingBoxInFrustum(AxisAlignedBB aabbIn) {
		return this.enabled ? super.isBoundingBoxInFrustum(aabbIn) : true;
	}
	
	public static ToggleableClippingHelper getClippingHelper(MatrixStack mtx, Matrix4f proj, ActiveRenderInfo info) {
		ToggleableClippingHelper ch = (ToggleableClippingHelper)Minecraft.getInstance().worldRenderer.debugFixedClippingHelper;
		if(ch != null)
			return ch;
		ch = new ToggleableClippingHelper(mtx.getLast().getMatrix(), proj);
		Vector3d pos = info.getProjectedView();
		ch.setCameraPosition(pos.x, pos.y, pos.z);
		return ch;
	}

}
