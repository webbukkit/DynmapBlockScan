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
 * This state handler is used for blocks which use the connection pattern of redstone wire,
 * for which the power= attribute is the metadata, and each of the 4 cardinal directions is
 * either up, side or none
 * 
 * @author Mike Primm
 */
public class RedstoneWireStateHandler implements IStateHandlerFactory {
    private static final String POWER[] = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15" };
    private static final String SIDE[] = { "none", "side", "up" };
    private static final int STATECNT = POWER.length * SIDE.length * SIDE.length * SIDE.length * SIDE.length;
    private static final int POWER_OFF = SIDE.length * SIDE.length * SIDE.length * SIDE.length;
    private static final int NORTH_OFF = SIDE.length * SIDE.length * SIDE.length;
    private static final int SOUTH_OFF = SIDE.length * SIDE.length;
    private static final int EAST_OFF = SIDE.length;
    private static final int WEST_OFF = 1;
    
    /**
     * This method is used to examining the BlockStateContainer of a block to determine if the state mapper can handle the given block
     * @param block - Block object
     * @param bsc - BlockStateContainer object
     * @returns IStateHandler if the handler factory believes it can handle this block type, null otherwise
     */
    public IStateHandler canHandleBlockState(Block block, BlockStateContainer bsc) {
        IProperty<?> power = IStateHandlerFactory.findMatchingProperty(bsc, "power", POWER);
        if (power == null) {
            return null;
        }
        IProperty<?> north = IStateHandlerFactory.findMatchingProperty(bsc, "north", SIDE);
        IProperty<?> south = IStateHandlerFactory.findMatchingProperty(bsc, "south", SIDE);
        IProperty<?> east = IStateHandlerFactory.findMatchingProperty(bsc, "east", SIDE);
        IProperty<?> west = IStateHandlerFactory.findMatchingProperty(bsc, "west", SIDE);
        if ((north == null) || (south == null) || (east == null) || (west == null)) {
            return null;
        }
        List<IBlockState> state = bsc.getValidStates();
        // Wrong number of valid states?
        if (state.size() != STATECNT) {
            return null;
        }
        IBlockState[] metavalues = new IBlockState[STATECNT];
        for (IBlockState s : state) {
            int index = Integer.parseInt(s.getValue(power).toString()) * POWER_OFF;
            index += getSideIndex(s.getValue(north).toString()) * NORTH_OFF;
            index += getSideIndex(s.getValue(south).toString()) * SOUTH_OFF;
            index += getSideIndex(s.getValue(east).toString()) * EAST_OFF;
            index += getSideIndex(s.getValue(west).toString()) * WEST_OFF;
            metavalues[index] = s;
        }
        // Return handler object
        return new OurHandler(metavalues);
    }
    
    private static final int getSideIndex(String value) {
        for (int i = 0; i < SIDE.length; i++) {
            if (SIDE[i].equals(value))
                return i;
        }
        return 0;
    }

    class OurHandler implements IStateHandler {
        private String[] string_values;
        private Map<String, String>[] map_values;
        
        OurHandler(IBlockState[] states) {
            string_values = new String[STATECNT];
            map_values = new Map[STATECNT];
            for (int i = 0; i < STATECNT; i++) {
                IBlockState bs = states[i];
                HashMap<String, String> m = new HashMap<String,String>();
                StringBuilder sb = new StringBuilder();
                for (Entry<IProperty<?>, Comparable<?>> p : bs.getProperties().entrySet()) {
                    if (sb.length() > 0) sb.append(",");
                    sb.append(p.getKey().getName()).append("=").append(p.getValue().toString());
                    m.put(p.getKey().getName(), p.getValue().toString());
                }
                map_values[i] = m;
                string_values[i] = sb.toString();
            }
        }
        @Override
        public String getName() {
            return "RedstoneWireMetadataState";
        }
        @Override
        public int getBlockStateIndex(int blockid, int blockmeta) {
            return (blockmeta * POWER_OFF); //TODO: we need the logic for looking for adjacent wires
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
