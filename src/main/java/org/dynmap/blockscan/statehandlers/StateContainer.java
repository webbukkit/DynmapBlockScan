package org.dynmap.blockscan.statehandlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;

// Portable container for block state record data
public class StateContainer {
	public static class StateRec {
		public final ImmutableMap<String, String> keyValuePairs;
		public final int metadata;
		
		public StateRec(int meta, ImmutableMap<String,String> kvp) {
			keyValuePairs = kvp;
			metadata = meta;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj instanceof StateRec) {
				StateRec sr = (StateRec) obj;
				return (sr.metadata == metadata) && (sr.keyValuePairs.equals(keyValuePairs));
			}
			return false;
		}
		
		@Override
		public int hashCode() {
			return (metadata << 16) ^ keyValuePairs.hashCode();
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
			sb.append("{ values=").append(keyValuePairs).append(",meta=").append(metadata).append("}");
			return sb.toString();
		}
	}
	
	protected Map<String, List<String>> renderProperties = new HashMap<String, List<String>>();
	protected final List<StateRec> records = new ArrayList<StateRec>();
	protected int defStateIndex;
	
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
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{ properties=").append(renderProperties).append(", records=").append(records).append(",defStateindex=").append(defStateIndex).append("}");
		return sb.toString();
	}
}