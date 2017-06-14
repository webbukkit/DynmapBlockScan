package org.dynmap.blockscan.blockstate;

import java.util.List;
import java.util.Map;
import java.util.Set;

// Variant of condition based on OR followed by list of base conditions
public class ORCondition implements Condition {
	public List<BaseCondition> conditions;
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{OR=").append(conditions).append("}");
		return sb.toString();
	}

	// Check for condition match : matches if any values in condition match corresponding values in provided properties
	public boolean matches(Map<String, String> props) {
		for (BaseCondition c : conditions) {
			if (c.matches(props))
				return true;
		}
		return false;
	}
	// Add distinct property keys to set
	public void addPropKeys(Set<String> props) {
		for (BaseCondition c : conditions) {
			c.addPropKeys(props);
		}
	}
}
