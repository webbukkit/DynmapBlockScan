package org.dynmap.blockscan.forge_1_15_2.statehandlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;

// Portable container for block state record data
public class StateContainer {
	public static class StateRec {
		public final ImmutableMap<String, String> keyValuePairs;
		public final int metadata[];
		
		public StateRec(int meta, ImmutableMap<String,String> kvp) {
			keyValuePairs = kvp;
			metadata = new int[] { meta };
		}
		
		public StateRec(StateRec prev, int new_meta) {
			keyValuePairs = prev.keyValuePairs;
			int[] newmeta = new int[prev.metadata.length + 1];
			System.arraycopy(prev.metadata, 0, newmeta, 0, prev.metadata.length);
			newmeta[prev.metadata.length] = new_meta;
			metadata = newmeta;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj instanceof StateRec) {
				StateRec sr = (StateRec) obj;
				return (sr.keyValuePairs.equals(keyValuePairs));
			}
			return false;
		}
		
		public boolean hasMeta(int meta) {
			for (int i = 0; i < metadata.length; i++) {
				if (metadata[i] == meta)
					return true;
			}
			return false;
		}
		
		@Override
		public int hashCode() {
			return keyValuePairs.hashCode();
		}
		
		public String getValue(String prop) {
			return keyValuePairs.get(prop);
		}
		
		public Map<String, String> getProperties() {
			return keyValuePairs;
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("{ values=").append(keyValuePairs).append(",meta=").append(Arrays.toString(metadata)).append("}");
			return sb.toString();
		}
	}
	
    public enum WellKnownBlockClasses {
        NONE,
        LEAVES,
        CROPS,
        FLOWER,
        TALLGRASS,
        BUSH,
        GRASS,
        LIQUID,
        VINES
    }

	protected Map<String, List<String>> renderProperties = new HashMap<String, List<String>>();
	protected final List<StateRec> records = new ArrayList<StateRec>();
	protected int defStateIndex;
	protected WellKnownBlockClasses type = WellKnownBlockClasses.NONE;
	
	public List<StateRec> getValidStates() {
		return records;
	}
	
	public StateRec getDefaultState() {
		return records.get(defStateIndex);
	}
	
	public Set<String> getProperties() {
		return renderProperties.keySet();
	}
	
	public List<String> getProperty(String prop) {
		return renderProperties.get(prop);
	}
	
	public WellKnownBlockClasses getBlockType() {
	    return type;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{ properties=").append(renderProperties).append(", records=").append(records).append(",defStateindex=").append(defStateIndex).append("}");
		return sb.toString();
	}
}
