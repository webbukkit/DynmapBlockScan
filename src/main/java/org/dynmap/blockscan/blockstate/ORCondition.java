package org.dynmap.blockscan.blockstate;

import java.util.List;

// Variant of condition based on OR followed by list of base conditions
public class ORCondition implements Condition {
	public List<BaseCondition> conditions;
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{OR=").append(conditions).append("}");
		return sb.toString();
	}

}
