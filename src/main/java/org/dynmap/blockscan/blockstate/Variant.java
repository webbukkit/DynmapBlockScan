package org.dynmap.blockscan.blockstate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.dynmap.blockscan.model.BlockElement;
import org.dynmap.blockscan.model.BlockModel;

// Container for parsed JSON encoding of Variant from blockstate
public class Variant {
	public String model;
	public int x = 0;
	public int y = 0;
	public boolean uvlock = false;
	public int weight = 1;
	// Normalized model ID
	public String modelID;
	// Generated and resolved element list
	public List<BlockElement> elements;
	
	// Generate and resolve element list for variant
	public boolean generateElements(Map<String, BlockModel> models) {
		BlockModel basemod = models.get(modelID);	// Loop up our model
		if (basemod == null) {
			return false;
		}
		// Find topmost elements
		BlockModel elemmod = basemod;
		while (elemmod.elements.isEmpty()) {
			elemmod = elemmod.parentModel;	// Get parent model
			if (elemmod == null) {	// Not found, we've got no elements
				return false;
			}
		}
		// Now, we're going to build copies of the elements with resolved models
		elements = new ArrayList<BlockElement>();
		for (BlockElement elem : elemmod.elements) {
			elements.add(new BlockElement(elem, basemod));
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		return model.hashCode() ^ x ^ (y << 8) ^ (weight << 16) ^ (uvlock?12345:0);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj instanceof Variant) {
			Variant vobj = (Variant) obj;
			return (model.equals(vobj.model) && (x == vobj.x) && (y == vobj.y) && (uvlock == vobj.uvlock) && (weight == vobj.weight));
		}
		return false;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("{");
		sb.append("model=").append(model);
		if (x != 0)
			sb.append(",x=").append(x);
		if (y != 0)
			sb.append(",y=").append(y);
		if (uvlock)
			sb.append(",uvlock=true");
		if (weight != 1)
			sb.append(",weight=").append(weight);
		sb.append("}");
		return sb.toString();
	}
}
