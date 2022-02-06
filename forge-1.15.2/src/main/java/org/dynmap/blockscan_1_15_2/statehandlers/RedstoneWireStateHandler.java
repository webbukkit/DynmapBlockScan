package org.dynmap.blockscan_1_15_2.statehandlers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.dynmap.blockscan_1_15_2.statehandlers.StateContainer.StateRec;

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
     * This method is used to examining the StateContainer of a block to determine if the state mapper can handle the given block
     * @param bsc - StateContainer object
     * @returns IStateHandler if the handler factory believes it can handle this block type, null otherwise
     */
    public IStateHandler canHandleBlockState(StateContainer bsc) {
        boolean power = IStateHandlerFactory.findMatchingProperty(bsc, "power", POWER);
        if (!power) {
            return null;
        }
        boolean north = IStateHandlerFactory.findMatchingProperty(bsc, "north", SIDE);
        boolean south = IStateHandlerFactory.findMatchingProperty(bsc, "south", SIDE);
        boolean east = IStateHandlerFactory.findMatchingProperty(bsc, "east", SIDE);
        boolean west = IStateHandlerFactory.findMatchingProperty(bsc, "west", SIDE);
        if ((!north) || (!south) || (!east) || (!west)) {
            return null;
        }
        List<StateRec> state = bsc.getValidStates();
        // Wrong number of valid states?
        if (state.size() != STATECNT) {
            return null;
        }
        StateRec[] metavalues = new StateRec[STATECNT];
        for (StateRec s : state) {
            int index = Integer.parseInt(s.getValue("power")) * POWER_OFF;
            index += getSideIndex(s.getValue("north")) * NORTH_OFF;
            index += getSideIndex(s.getValue("south")) * SOUTH_OFF;
            index += getSideIndex(s.getValue("east")) * EAST_OFF;
            index += getSideIndex(s.getValue("west")) * WEST_OFF;
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
        
        @SuppressWarnings("unchecked")
		OurHandler(StateRec[] states) {
            string_values = new String[STATECNT];
            map_values = new Map[STATECNT];
            for (int i = 0; i < STATECNT; i++) {
                StateRec bs = states[i];
                HashMap<String, String> m = new HashMap<String,String>();
                StringBuilder sb = new StringBuilder();
                for (Entry<String, String> p : bs.getProperties().entrySet()) {
                    if (sb.length() > 0) sb.append(",");
                    sb.append(p.getKey()).append("=").append(p.getValue());
                    m.put(p.getKey(), p.getValue());
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
