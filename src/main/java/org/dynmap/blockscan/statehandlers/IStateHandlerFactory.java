package org.dynmap.blockscan.statehandlers;

import java.awt.List;
import java.util.Arrays;
import java.util.Collection;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;

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
     * This method is used to examining the BlockStateContainer of a block to determine if the state mapper can handle the given block
     * @param block - Block object
     * @param bsc - BlockStateContainer object
     * @returns IStateHandler if the handler factory believes it can handle this block type, null otherwise
     */
    public IStateHandler canHandleBlockState(Block block, BlockStateContainer bsc);
    
    
    /**
     * Utility method : test for presence of property with given list of values
     * @param bsc - BlockStateContainer
     * @param prop - property name
     * @param values - list of values
     * @return matching property, or null
     */
    public static IProperty<?> findMatchingProperty(BlockStateContainer bsc, String prop, String[] values) {
        IProperty<?> p = bsc.getProperty(prop);
        if (p == null) {
            return null;
        }
        int cnt = 0;
        for (Comparable<?> v : p.getAllowedValues()) {
            String val = v.toString();
            boolean match = false;
            for (String mval : values) {
                if (mval.equals(val)) {
                    match = true;
                    break;
                }
            }
            if (!match) {
                return null;
            }
            cnt++;
        }
        if (cnt != values.length) {
            return null;
        }
        return p;
    }
    // Well-known value set : boolean
    public static final String booleanValues[] = { "true", "false" };
    // Well-known value set : stair shapes 
    public static final String stairShapeValues[] = { "straight", "inner_left", "inner_right", "outer_left", "outer_right" };
    // Well-known value set : facing (NSEW)
    public static final String facingValues[] = { "north", "south", "east", "west" };
    /**
     * Utility method : test for presence of property with boolean values
     * @param bsc - BlockStateContainer
     * @param prop - property name
     * @return matching property, or null
     */
    public static IProperty<?> findMatchingBooleanProperty(BlockStateContainer bsc, String prop) {
        return findMatchingProperty(bsc, prop, booleanValues);
    }
}
