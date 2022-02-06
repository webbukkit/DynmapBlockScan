package org.dynmap.blockscan.forge_1_15_2.statehandlers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.dynmap.blockscan.forge_1_15_2.statehandlers.StateContainer.StateRec;

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
     * This method is used to examining the StateContainer of a block to determine if the state mapper can handle the given block
     * @param bsc - StateContainer object
     * @returns IStateHandler if the handler factory believes it can handle this block type, null otherwise
     */
    public IStateHandler canHandleBlockState(StateContainer bsc) {
        boolean up = IStateHandlerFactory.findMatchingBooleanProperty(bsc, "up");
        boolean north = IStateHandlerFactory.findMatchingBooleanProperty(bsc, "north");
        boolean south = IStateHandlerFactory.findMatchingBooleanProperty(bsc, "south");
        boolean east = IStateHandlerFactory.findMatchingBooleanProperty(bsc, "east");
        boolean west = IStateHandlerFactory.findMatchingBooleanProperty(bsc, "west");
        if ((!up) || (!north) || (!south) || (!east) || (!west)) {
            return null;
        }
        List<StateRec> state = bsc.getValidStates();
        StateRec[][] metavalues = new StateRec[16][];
        for (int i = 0; i < 16; i++) {
            metavalues[i] = new StateRec[CONNECTCNT];
        }
        for (StateRec s : state) {
            int index = getBoolIndex(s.getValue("up"), UP_OFF);
            index += getBoolIndex(s.getValue("north"), NORTH_OFF);
            index += getBoolIndex(s.getValue("south"), SOUTH_OFF);            
            index += getBoolIndex(s.getValue("east"), EAST_OFF);
            index += getBoolIndex(s.getValue("west"), WEST_OFF);
            for (int meta : s.metadata) {
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
        }
        // Fill in any missing metadata with default state
        for (int i = 0; i < metavalues.length; i++) {
            for (int j = 0; j < CONNECTCNT; j++) {
                if (metavalues[i][j] == null) {
                    metavalues[i][j] = bsc.getDefaultState();
                }
            }
        }
        // Return handler object
        return new OurHandler(metavalues);
    }
    private static final int getBoolIndex(String v, int off) {
        return v.equals("true")?off:0;
    }
    class OurHandler implements IStateHandler {
        private String[] string_values;
        private Map<String, String>[] map_values;
        
        @SuppressWarnings("unchecked")
		OurHandler(StateRec[][] states) {
            string_values = new String[16 * CONNECTCNT];
            map_values = new Map[16 * CONNECTCNT];
            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < CONNECTCNT; j++) {
                    StateRec bs = states[i][j];
                    HashMap<String, String> m = new HashMap<String,String>();
                    StringBuilder sb = new StringBuilder();
                    for (Entry<String, String> p : bs.getProperties().entrySet()) {
                        if (sb.length() > 0) sb.append(",");
                        sb.append(p.getKey()).append("=").append(p.getValue());
                        m.put(p.getKey(), p.getValue());
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
