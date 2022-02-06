package org.dynmap.blockscan_1_14_4.statehandlers;

import java.util.List;

/**
 * This interface is used to define 'state handlers' for Dynmap
 * These are tools for mapping map data to corresponding BlockState selectors for a given type of blocks
 * Ultimately, this is logically replicating the code within the Block classes themselves, so that Dynmap
 * can use them safely and with performance from threads outside the MC server's threads.
 * 
 * @author Mike Primm
 *
 */
public interface IStateHandlerFactory {
    /**
     * This method is used to examining the StateContainer of a block to determine if the state mapper can handle the given block
     * @param sc - StateContainer object
     * @returns IStateHandler if the handler factory believes it can handle this block type, null otherwise
     */
    public IStateHandler canHandleBlockState(StateContainer sc);
    
    
    /**
     * Utility method : test for presence of property with given list of values
     * @param sc - StateContainer
     * @param prop - property name
     * @param values - list of values
     * @return true if matching, false if not
     */
    public static boolean findMatchingProperty(StateContainer bsc, String prop, String[] values) {
    	List<String> vals = bsc.renderProperties.get(prop);
    	if (vals == null) {
    		return false;
    	}
    	if (values.length != vals.size()) {
    		return false;
    	}
        int cnt = 0;
        for (String v : vals) {
            boolean match = false;
            for (String mval : values) {
                if (mval.equals(v)) {
                    match = true;
                    break;
                }
            }
            if (!match) {
                return false;
            }
            cnt++;
        }
        if (cnt != values.length) {
            return false;
        }
        return true;
    }
    // Well-known value set : boolean
    public static final String booleanValues[] = { "true", "false" };
    // Well-known value set : stair shapes 
    public static final String stairShapeValues[] = { "straight", "inner_left", "inner_right", "outer_left", "outer_right" };
    // Well-known value set : facing (NSEW)
    public static final String facingValues[] = { "north", "south", "east", "west" };
    /**
     * Utility method : test for presence of property with boolean values
     * @param bsc - StateContainer
     * @param prop - property name
     * @return true if matching, false if not
     */
    public static boolean findMatchingBooleanProperty(StateContainer bsc, String prop) {
        return findMatchingProperty(bsc, prop, booleanValues);
    }
}
