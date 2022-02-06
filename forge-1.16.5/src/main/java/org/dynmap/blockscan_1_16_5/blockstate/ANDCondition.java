package org.dynmap.blockscan_1_16_5.blockstate;

import java.util.List;
import java.util.Map;
import java.util.Set;

// Variant of condition based on AND followed by list of base conditions
public class ANDCondition implements Condition {
	public List<BaseCondition> conditions;
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{AND=").append(conditions).append("}");
		return sb.toString();
	}
	
	// Check for condition match : matches if any values in condition match corresponding values in provided properties
	public boolean matches(Map<String, String> props) {
		for (BaseCondition c : conditions) {
			if (c.matches(props) == false)
				return false;
		}
		return true;
	}
	// Add distinct property keys to set
	public void addPropKeys(Set<String> props) {
		for (BaseCondition c : conditions) {
			c.addPropKeys(props);
		}
	}
}
