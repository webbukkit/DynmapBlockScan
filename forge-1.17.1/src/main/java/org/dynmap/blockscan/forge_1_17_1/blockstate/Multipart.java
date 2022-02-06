package org.dynmap.blockscan.forge_1_17_1.blockstate;

public class Multipart {
	public Condition when;	// Condition field for part - needs custom handler
	public VariantList apply;	// VariantList of model references : either singleton or list
	
	public String toString() {
		StringBuilder sb = new StringBuilder("{");
		if (when != null) {
			sb.append("when=").append(when);
		}
		if (apply != null) {
			sb.append(" apply=").append(apply);
		}
		sb.append("}");
		return sb.toString();
	}
}
