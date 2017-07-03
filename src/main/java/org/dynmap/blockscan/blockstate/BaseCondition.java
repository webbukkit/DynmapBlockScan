package org.dynmap.blockscan.blockstate;

import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.dynmap.blockscan.DynmapBlockScanPlugin;

import com.google.common.collect.ImmutableMap;

public class BaseCondition implements Condition {
	public final ImmutableMap<String, String> keyValuePairs;
	// Only defined and populated with keys that have conditional values (x|y|z)
	private final ImmutableMap<String, String[]> keyConditionalValuePairs;
	private final String unmatchedValue;
	
	public BaseCondition(ImmutableMap<String, String> v) {
		keyValuePairs = v;
		ImmutableMap.Builder<String,String[]> bld = null;
		for (Entry<String, String> ent : v.entrySet()) {
			String entval = ent.getValue();
			if (entval.indexOf('|') >= 0) {
				// Lazy build the builder - this is rare
				if (bld == null) {
					bld = ImmutableMap.builder();
				}
				bld.put(ent.getKey(), entval.split("|"));
			}
		}
		if (bld != null) {
			keyConditionalValuePairs = bld.build();
		}
		else {
			keyConditionalValuePairs = null;
		}
		unmatchedValue = "";
	}
	
	// Condition for variant key - comma separated list of attrib=value
	public BaseCondition(String key) {
		String uv = "";
		ImmutableMap.Builder<String,String> bld = ImmutableMap.builder();
		String[] tok = key.split(",");	// Split on commas
		for (String t : tok) {
			String[] kv = t.split("=");	// Split on equals
			if (kv.length == 2) {
				bld.put(kv[0], kv[1]);		// Add to map
			}
			else {
				// 'normal' - nothing to do
				uv = kv[0];
			}
		}
		keyValuePairs = bld.build();
		keyConditionalValuePairs = null;	// These never have conditionals
		unmatchedValue = uv;
	}
	
	@Override
	public String toString() {
	    if (unmatchedValue.equals("")) {
	        return keyValuePairs.toString();
	    }
	    else {
	        return unmatchedValue;
	    }
	}
	
	@Override
	public int hashCode() {
		return keyValuePairs.hashCode() ^ unmatchedValue.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj instanceof BaseCondition) {
			BaseCondition bc = (BaseCondition) obj;
			return bc.keyValuePairs.equals(keyValuePairs) && bc.unmatchedValue.equals(this.unmatchedValue);
		}
		return false;
	}
	
	// Check for condition match : matches if any values in condition match corresponding values in provided properties
	public boolean matches(Map<String, String> props) {
	    boolean is_match = true;
		for (Entry<String, String> es : keyValuePairs.entrySet()) {
			String esk = es.getKey();
			String v = props.get(esk);
			if (v == null) {
				return false;
			}
			// If we have conditional, and this is one of them
			if ((this.keyConditionalValuePairs != null) && this.keyConditionalValuePairs.containsKey(esk)) {
				String[] vals = this.keyConditionalValuePairs.get(esk);
				boolean match = false;
				for (String mv : vals) {
					if (v.equals(mv)) {
						match = true;
						break;
					}
				}
				if (!match) return false;
			}
			else if (v.equals(es.getValue()) == false) {	// Value miss?
				return false;
			}
		}
		// If no conditions, only match if no unmatched value or 'normal' (skip 'inventory' and the like)
		if (this.keyValuePairs.isEmpty()) {
		    is_match = (unmatchedValue.equals("") || (unmatchedValue.equals("normal")));
		}
		return is_match;
	}
	// Add distinct property keys to set
	public void addPropKeys(Set<String> props) {
		props.addAll(keyValuePairs.keySet());
	}
}
