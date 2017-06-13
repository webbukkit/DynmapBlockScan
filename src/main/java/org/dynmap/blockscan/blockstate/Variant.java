package org.dynmap.blockscan.blockstate;

// Container for parsed JSON encoding of Variant from blockstate
public class Variant {
	public String model;
	public int x = 0;
	public int y = 0;
	public boolean uvlock = false;
	public int weight = 1;
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("model=").append(model);
		if (x != 0)
			sb.append(",x=").append(x);
		if (y != 0)
			sb.append(",y=").append(y);
		if (uvlock)
			sb.append(",uvlock=true");
		if (weight != 1)
			sb.append(",weight=").append(weight);
		return sb.toString();
	}
}
