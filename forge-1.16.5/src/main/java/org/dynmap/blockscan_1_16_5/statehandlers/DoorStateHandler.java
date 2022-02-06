package org.dynmap.blockscan_1_16_5.statehandlers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.dynmap.blockscan_1_16_5.statehandlers.StateContainer.StateRec;

/**
 * This state handler is used for door blocks.  It expects a match on standard door properties and values.
 * 
 * @author Mike Primm
 */
public class DoorStateHandler implements IStateHandlerFactory {
    private static final String[] FACING = IStateHandlerFactory.facingValues;
    private static final String[] HALF = { "upper", "lower" };
    private static final String[] HINGE = { "left", "right" };
    // facing=north,half=upper,hinge=left,open=true,powered=true
    /** 
     * This method is used to examining the StateContainer of a block to determine if the state mapper can handle the given block
     * @param bsc - StateContainer object
     * @returns IStateHandler if the handler factory believes it can handle this block type, null otherwise
     */
    public IStateHandler canHandleBlockState(StateContainer bsc) {
        List<StateRec> state = bsc.getValidStates();
        // Doors have 4 rendering properties and 32 valid states
        if ((bsc.getProperties().size() != 4) || (state.size() != 32)) {
            return null;
        }
        boolean facing = IStateHandlerFactory.findMatchingProperty(bsc, "facing", FACING);
        boolean half = IStateHandlerFactory.findMatchingProperty(bsc, "half", HALF);
        boolean hinge = IStateHandlerFactory.findMatchingProperty(bsc, "hinge", HINGE);
        if ((!facing) || (!half) || (!hinge)) {
            return null;
        }
        boolean open = IStateHandlerFactory.findMatchingBooleanProperty(bsc, "open");
        if (!open) {
            return null;
        }
        StateRec[] metavalues = new StateRec[32];
        for (StateRec s : state) {
            int idx = getFacingIndex(s.getValue("facing")) +
                    getHingeIndex(s.getValue("hinge")) +
                    getHalfIndex(s.getValue("half")) +
                    getOpenIndex(s.getValue("open"));
            metavalues[idx] = s;
        }
        // Return handler object
        return new OurHandler(metavalues);
    }
    // Return index for given facing value (offset by 4 bits)
    private static int getFacingIndex(String value) {
        for (int i = 0; i < FACING.length; i++) {
            if (FACING[i].equals(value)) {
                return 8 * i;
            }
        }
        return 0;
    }
    // Return index for given hinge value (offset by 2 bits)
    private static int getHingeIndex(String value) {
        for (int i = 0; i < HINGE.length; i++) {
            if (HINGE[i].equals(value)) {
                return 2 * i;
            }
        }
        return 0;
    }
    // Return index for given half value (offset by 3 bits)
    private static int getHalfIndex(String value) {
        for (int i = 0; i < HALF.length; i++) {
            if (HALF[i].equals(value)) {
                return 4 * i;
            }
        }
        return 0;
    }
    // Return index for given open value (offset by 0 bits)
    private static int getOpenIndex(String value) {
        if (value.equals("true"))
            return 1;
        return 0;
    }

    class OurHandler implements IStateHandler {
        private String[] string_values;
        private Map<String, String>[] map_values;
        
        @SuppressWarnings("unchecked")
		OurHandler(StateRec[] states) {
            string_values = new String[32];
            map_values = new Map[32];
            for (int i = 0; i < 32; i++) {
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
            return "DoorMetadataState";
        }
        @Override
        public int getBlockStateIndex(int blockid, int blockmeta) {
            return blockmeta; //TODO: we need the logic for looking for stair shape
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
