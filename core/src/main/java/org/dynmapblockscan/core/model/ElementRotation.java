package org.dynmapblockscan.core.model;

import java.util.Arrays;

import org.dynmapblockscan.core.model.ElementRotation;

public class ElementRotation {
	public float[] origin = { 8.0f, 8.0f, 8.0f };
	public String axis = "x";
	public float angle = 0.0f;
	public boolean rescale = false;
	
	public ElementRotation() {}
	
	public ElementRotation(ElementRotation src) {
		origin = Arrays.copyOf(src.origin, src.origin.length);
		axis = src.axis;
		angle = src.angle;
		rescale = src.rescale;
	}
}
