package org.dynmap.blockscan.blockstate;

import java.util.List;

// Variant of condition based on AND followed by list of base conditions
public class ANDCondition implements Condition {
	public List<BaseCondition> conditions;
}
