package org.dynmapblockscan.core.blockstate;

import java.util.Map;

import org.dynmapblockscan.core.blockstate.ModelRotation;
import org.dynmapblockscan.core.util.Matrix3D;
import org.dynmapblockscan.core.util.Vector3D;

import java.util.HashMap;

public enum ModelRotation {
    X0_Y0(0, 0),
    X0_Y90(0, 90),
    X0_Y180(0, 180),
    X0_Y270(0, 270),
    X90_Y0(90, 0),
    X90_Y90(90, 90),
    X90_Y180(90, 180),
    X90_Y270(90, 270),
    X180_Y0(180, 0),
    X180_Y90(180, 90),
    X180_Y180(180, 180),
    X180_Y270(180, 270),
    X270_Y0(270, 0),
    X270_Y90(270, 90),
    X270_Y180(270, 180),
    X270_Y270(270, 270);

    private static final Map<Integer, ModelRotation> MAP_ROTATIONS = new HashMap<Integer, ModelRotation>();
    private final int combinedXY;
    private final int quartersX;
    private final int quartersY;
    private ElementFace facemap[];	// resulting face after this rotation, indexed by ordinal of initial EnumFace
    private int facerot[];	// Number of CW quarter rotations for face due to rotation
    private Matrix3D transform;
    
    private static int combineXY(int p_177521_0_, int p_177521_1_) {
        return p_177521_0_ * 360 + p_177521_1_;
    }

    private ModelRotation(int x, int y) {
        this.combinedXY = combineXY(x, y);
        this.quartersX = Math.abs(x / 90);
        this.quartersY = Math.abs(y / 90);
        facemap = new ElementFace[6];
        facerot = new int[6];
        for (ElementFace f : ElementFace.values()) {
        	facemap[f.ordinal()] = f;
        	facerot[f.ordinal()] = 0;
        }
        // Now rotate by number of quarters around X
        for (int i = 0; i < quartersX; i++) {
        	rotateX();
        }
        // Then rotate around Y
        for (int i = 0; i < quartersY; i++) {
        	rotateY();
        }
        // Invert map
        ElementFace[] newface = new ElementFace[6];
        int[] newrot = new int[6];
        for (int i = 0; i < 6; i++) {
            ElementFace f = facemap[i];
            newface[f.ordinal()] = ElementFace.values()[i];
            newrot[f.ordinal()] = facerot[i];
        }
        facemap = newface;
        facerot = newrot;
        // And produce transform matrix
        transform = new Matrix3D();    // Start with identity
        if (quartersX != 0) {   // If doing X rotation
            transform.rotateYZ(90.0 * quartersX);
        }
        if (quartersY != 0) {   // If doing Y rotation
            transform.rotateXZ(90.0 * quartersY);
        }
    }
    
    private void transferFace(ElementFace src, ElementFace dest, int rotation, ElementFace[] newface, int[] newrot) {
    	newface[dest.ordinal()] = facemap[src.ordinal()];
    	newrot[dest.ordinal()] = (facerot[src.ordinal()] + rotation) % 4;
    }
    private void rotateX() {
    	int[] newrot = new int[6];
    	ElementFace[] newface = new ElementFace[6];
    	// Pivot the X sides (W=-X, E=+X)
    	transferFace(ElementFace.WEST, ElementFace.WEST, 1, newface, newrot);
    	transferFace(ElementFace.EAST, ElementFace.EAST, 3, newface, newrot);
    	// Move others around +X
    	transferFace(ElementFace.SOUTH, ElementFace.UP, 0, newface, newrot);
    	transferFace(ElementFace.NORTH, ElementFace.DOWN, 2, newface, newrot);
    	transferFace(ElementFace.UP, ElementFace.NORTH, 2, newface, newrot); 
    	transferFace(ElementFace.DOWN, ElementFace.SOUTH, 0, newface, newrot);
    	// Replace map
    	facemap = newface;
    	facerot = newrot;
    }
    private void rotateY() {
    	int[] newrot = new int[6];
    	ElementFace[] newface = new ElementFace[6];
    	// Pivot the Y sides (D=-Y, U=+Y)
    	transferFace(ElementFace.UP, ElementFace.UP, 3, newface, newrot);
    	transferFace(ElementFace.DOWN, ElementFace.DOWN, 1, newface, newrot);
    	// Move others around +Y
    	transferFace(ElementFace.SOUTH, ElementFace.WEST, 0, newface, newrot);
    	transferFace(ElementFace.WEST, ElementFace.NORTH, 0, newface, newrot);
    	transferFace(ElementFace.NORTH, ElementFace.EAST, 0, newface, newrot);
    	transferFace(ElementFace.EAST, ElementFace.SOUTH, 0, newface, newrot);
    	// Replace map
    	facemap = newface;
    	facerot = newrot;
    }

    public ElementFace rotateFace(ElementFace facing) {
        return facemap[facing.ordinal()];
    }
    
    public int rotateFaceOrientation(ElementFace facing) {
    	return 90 * facerot[facing.ordinal()];
    }
    
    // Apply rotation transform to vector 'in place'
    public void transformVector(Vector3D v) {
        transform.transform(v);
    }
    
    public static ModelRotation getModelRotation(int x, int y) {
        ModelRotation rot = (ModelRotation)MAP_ROTATIONS.get(Integer.valueOf(combineXY(normalizeAngle(x, 360), normalizeAngle(y, 360))));
        if (rot == null) {
            rot = ModelRotation.X0_Y0;
        }
        return rot;
    }

    static {
        for (ModelRotation modelrotation : values()) {
            MAP_ROTATIONS.put(Integer.valueOf(modelrotation.combinedXY), modelrotation);
        }
    }
    public static int normalizeAngle(int p_180184_0_, int p_180184_1_)
    {
        return (p_180184_0_ % p_180184_1_ + p_180184_1_) % p_180184_1_;
    }
}