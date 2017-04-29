package org.dynmap.blockscan.statehandlers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.ImmutableMap;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;

/**
 * This state handler is used for blocks which preserve a simple 1-1 correlation between 
 * metadata values and block state, except for also having cardinal direction and 'up'
 * adjacency sensitivity, as for Fire blocks.
 * 
 * @author Mike Primm
 */
public class NSEWUConnectedMetadataStateHandler implements IStateHandlerFactory {
    private static final int CONNECTCNT = 2 * 2 * 2 * 2 * 2;    // NSEWU permutations
    private static final int UP_OFF = 1;
    private static final int NORTH_OFF = 2;
    private static final int SOUTH_OFF = 4;
    private static final int EAST_OFF = 8;
    private static final int WEST_OFF = 16;
    /** 
     * This method is used to examining the BlockStateContainer of a block to determine if the state mapper can handle the given block
     * @param block - Block object
     * @param bsc - BlockStateContainer object
     * @returns IStateHandler if the handler factory believes it can handle this block type, null otherwise
     */
    public IStateHandler canHandleBlockState(Block block, BlockStateContainer bsc) {
        IProperty<?> up = IStateHandlerFactory.findMatchingBooleanProperty(bsc, "up");
        IProperty<?> north = IStateHandlerFactory.findMatchingBooleanProperty(bsc, "north");
        IProperty<?> south = IStateHandlerFactory.findMatchingBooleanProperty(bsc, "south");
        IProperty<?> east = IStateHandlerFactory.findMatchingBooleanProperty(bsc, "east");
        IProperty<?> west = IStateHandlerFactory.findMatchingBooleanProperty(bsc, "west");
        if ((up == null) || (north == null) || (south == null) || (east == null) || (west == null)) {
            return null;
        }
        List<IBlockState> state = bsc.getValidStates();
        IBlockState[][] metavalues = new IBlockState[16][];
        for (int i = 0; i < 16; i++) {
            metavalues[i] = new IBlockState[CONNECTCNT];
        }
        for (IBlockState s : state) {
            int meta = block.getMetaFromState(s);   // Lookup meta for this state
            int index = getBoolIndex(s.getValue(up), UP_OFF);
            index += getBoolIndex(s.getValue(north), NORTH_OFF);
            index += getBoolIndex(s.getValue(south), SOUTH_OFF);
            index += getBoolIndex(s.getValue(east), EAST_OFF);
            index += getBoolIndex(s.getValue(west), WEST_OFF);
            // If out of range, or duplicate, we cannot handle
            if ((meta < 0) || (meta > 15)) {
                return null;
            }
            if (metavalues[meta][index] != null) {
                return null;
            }
            else {
                metavalues[meta][index] = s;
            }
        }
        // Fill in any missing metadata with default state
        for (int i = 0; i < metavalues.length; i++) {
            for (int j = 0; j < CONNECTCNT; j++) {
                if (metavalues[i][j] == null) {
                    metavalues[i][j] = block.getDefaultState();
                }
            }
        }
        // Return handler object
        return new OurHandler(metavalues);
    }
    private static final int getBoolIndex(Object v, int off) {
        return v.toString().equals("true")?off:0;
    }
    class OurHandler implements IStateHandler {
        private String[] string_values;
        private Map<String, String>[] map_values;
        
        OurHandler(IBlockState[][] states) {
            string_values = new String[16 * CONNECTCNT];
            map_values = new Map[16 * CONNECTCNT];
            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < CONNECTCNT; j++) {
                    IBlockState bs = states[i][j];
                    HashMap<String, String> m = new HashMap<String,String>();
                    StringBuilder sb = new StringBuilder();
                    for (Entry<IProperty<?>, Comparable<?>> p : bs.getProperties().entrySet()) {
                        if (sb.length() > 0) sb.append(",");
                        sb.append(p.getKey().getName()).append("=").append(p.getValue().toString());
                        m.put(p.getKey().getName(), p.getValue().toString());
                    }
                    map_values[16*j + i] = m;
                    string_values[16*j + i] = sb.toString();
                }
            }
        }
        @Override
        public String getName() {
            return "NSEWUConnectedMetadataState";
        }
        @Override
        public int getBlockStateIndex(int blockid, int blockmeta) {
            return blockmeta; //TODO: we need the logic for looking for adjacent blocks (NSWEU)
        }
        @Override
        public Map<String, String>[] getBlockStateValueMaps() {
            return map_values;
        }
        @Override
        public String[] getBlockStateValues() {
            return string_values;
        }
    }
}
