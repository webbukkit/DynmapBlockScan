package org.dynmapblockscan.core.blockstate;

import org.dynmap.modsupport.BlockSide;

public enum ElementFace {
	// Keep ordinal order
    DOWN(BlockSide.BOTTOM, "down"),
    UP(BlockSide.TOP, "up"),
    NORTH(BlockSide.NORTH, "north"),
    SOUTH(BlockSide.SOUTH, "south"),
    WEST(BlockSide.WEST, "west"),
    EAST(BlockSide.EAST, "east");

	public final BlockSide side;
	public final String face;	// Name of face in model file elements
	
	ElementFace(BlockSide s, String f) {
		side = s;
		face = f;
	}	
	public static ElementFace byFace(String f) {
		for (ElementFace bf : ElementFace.values()) {
			if (bf.face.equals(f)) {
				return bf;
			}
		}
		return null;
	}
	public static ElementFace bySide(BlockSide bs) {
		for (ElementFace bf : ElementFace.values()) {
			if (bf.side == bs) {
				return bf;
			}
		}
		return null;		
	}
}
