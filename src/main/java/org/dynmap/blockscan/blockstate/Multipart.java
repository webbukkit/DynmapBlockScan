package org.dynmap.blockscan.blockstate;

public class Multipart {
	public Condition when;	// Condition field for part - needs custom handler
	public VariantList apply;	// VariantList of model references : either singleton or list
}
