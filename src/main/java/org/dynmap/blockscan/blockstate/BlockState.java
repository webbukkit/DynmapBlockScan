package org.dynmap.blockscan.blockstate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

// Top level container class for JSON parsed BlockState data
public class BlockState {
	// Parser for BlockState
	private static Gson GSON;

	// "variants" map: key is state string, value is either Variant or list of Variant object (need special parser)
	public VariantListMap variants;
	// "multipart" list: each record is a MultiPart
	public List<Multipart> multipart;
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (variants != null) {
			sb.append("variants={").append(variants).append("}");
		}
		if (multipart != null) {
			sb.append("multipart=[").append(multipart).append("]");
		}
		return sb.toString();
	}
	// Build Gson parser for BlockState
	public static Gson buildParser() {
		Gson g = GSON;
		if (g == null) {
			GsonBuilder gb = new GsonBuilder();	// Start with builder
			gb.registerTypeAdapter(VariantList.class, new VariantList.Deserializer()); // Add VariantList handler
			gb.registerTypeAdapter(VariantListMap.class, new VariantListMap.Deserializer()); // Add VariantListMap handler
			gb.registerTypeAdapter(Condition.class, new Condition.Deserializer()); // Add Condition handler1
			g = gb.create();
			GSON = g;
		}
		return g;
	}
	// Get render properties
	public Set<String> getRenderProps() {
		HashSet<String> props = new HashSet<String>();
		if (variants != null) {
			for (BaseCondition cond : variants.map.keySet()) {
				cond.addPropKeys(props);
			}
		}
		if (multipart != null) {
			for (Multipart mp : multipart) {
				if (mp.when != null) {
					mp.when.addPropKeys(props);
				}
			}
		}
		return props;
	}
}
