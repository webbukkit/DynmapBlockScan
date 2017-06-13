package org.dynmap.blockscan.blockstate;

import java.util.List;

// Variant of condition based on OR followed by list of base conditions
public class ORCondition implements Condition {
	public List<BaseCondition> conditions;
}
