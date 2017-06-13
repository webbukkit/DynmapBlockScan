package org.dynmap.blockscan.blockstate;

import java.util.List;

// Variant of condition based on AND followed by list of base conditions
public class ANDCondition implements Condition {
	public List<BaseCondition> conditions;
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{AND=").append(conditions).append("}");
		return sb.toString();
	}
}
