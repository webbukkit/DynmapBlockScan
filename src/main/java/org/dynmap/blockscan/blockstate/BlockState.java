package org.dynmap.blockscan.blockstate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

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
	
	// Property value based nested state mapping
	public String nestedProp = null;
	public Map<String, BlockState> nestedValueMap = null;
	
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
	
    // Build list of Variant lists from parsed block state and match given properties
    public List<VariantList> getMatchingVariants(Map<String, String> prop) {
    	List<VariantList> vlist = new ArrayList<VariantList>();
    	
    	// If bstate has variant list map, walk it - only match first one
    	if (this.variants != null) {
    		for (Entry<BaseCondition, org.dynmap.blockscan.blockstate.VariantList> var : this.variants.map.entrySet()) {
    			if(var.getKey().matches(prop)) {	// Matching property?
    				// Only one will match for 'variants', so quit
    				vlist.add(var.getValue());
    				break;
    			}
    		}
    	}
    	// If bstate has multipart, walk it - accumulate all matches
    	if (this.multipart != null) {
    		for (Multipart mp : this.multipart) {
    			if (mp.when == null) {	// Unconditional?
    				vlist.add(mp.apply);	// Add it
    			}
    			else if (mp.when.matches(prop)) {	// Conditional matches?
    				vlist.add(mp.apply);	// Add it
    			}
    		}
    	}
    	// If nested, process request
    	if (this.nestedProp != null) {
    		String pval = prop.get(this.nestedProp);
    		if (pval != null) {
    			BlockState bs = this.nestedValueMap(pval);
    			if (bs != null) {
    				vlist = bs.getMatchingVariants(prop);
    			}
    		}
    	}
    	return vlist;
    }
	private BlockState nestedValueMap(String pval) {
		// TODO Auto-generated method stub
		return null;
	}

}
