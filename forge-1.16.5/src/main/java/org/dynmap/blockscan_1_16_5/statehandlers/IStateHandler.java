package org.dynmap.blockscan_1_16_5.statehandlers;

import java.util.Map;

/**
 * This interface is used to define 'state handlers' for Dynmap
 * These are tools for mapping map data to corresponding BlockState selectors for a given type of blocks
 * Ultimately, this is logically replicating the code within the Block classes themselves, so that Dynmap
 * can use them safely and with performance from threads outside the MC server's threads.
 * 
 * @author Mike Primm
 *
 */
public interface IStateHandler {
    /**
     * Get handler ID
     * @return ID string
     */
    public String getName();
    /**
     * Map block data to corresponding state index
     * @param blockid : block ID
     * @param blockmeta : blockmeta
     */
    public int getBlockStateIndex(int blockid, int blockmeta);
    /**
     * Get ordered list of block state value maps (param=value) for each valid state
     * @return ordered list of block state maps
     */
    public Map<String,String>[] getBlockStateValueMaps();
    /**
     * Get ordered list of block state value strings for each valid state
     * @return ordered list of block state strings (canonical)
     */
    public String[] getBlockStateValues();
}
