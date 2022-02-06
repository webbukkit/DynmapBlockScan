package org.dynmap.blockscan.forge_1_16_5.statehandlers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.dynmap.blockscan.forge_1_16_5.statehandlers.StateContainer.StateRec;

/**
 * This state handler is used for blocks which preserve a simple 1-1 correlation between 
 * metadata values and block state, except for also having a 'shape' parameter (referring to 
 * having a modified shape due to adjacent blocks, as for stair blocks)
 * 
 * @author Mike Primm
 */
public class StairMetadataStateHandler implements IStateHandlerFactory {
    private static final String[] SHAPES = IStateHandlerFactory.stairShapeValues;
    
    /** 
     * This method is used to examining the StateContainer of a block to determine if the state mapper can handle the given block
     * @param bsc - StateContainer object
     * @returns IStateHandler if the handler factory believes it can handle this block type, null otherwise
     */
    public IStateHandler canHandleBlockState(StateContainer bsc) {
        boolean shape = IStateHandlerFactory.findMatchingProperty(bsc, "shape", SHAPES);
        if (!shape) {
            return null;
        }
        // Make sure we have facing and half too - to be sure it's a stair
        if ((bsc.getProperty("facing") == null) || (bsc.getProperty("half") == null)) {
            return null;
        }
        List<StateRec> state = bsc.getValidStates();
        StateRec[][] metavalues = new StateRec[16][];
        for (int i = 0; i < 16; i++) {
            metavalues[i] = new StateRec[SHAPES.length];
        }
        for (StateRec s : state) {
            int shapeval = getShapeIndex(s.getValue("shape"));
            for (int meta : s.metadata) {
            	// If out of range, or duplicate, we cannot handle
            	if ((meta < 0) || (meta > 15)) {
            		return null;
            	}
            	if (metavalues[meta][shapeval] != null) {
            		return null;
            	}
            	else {
            		metavalues[meta][shapeval] = s;
            	}
            }
        }
        // Fill in any missing metadata with default state
        for (int i = 0; i < metavalues.length; i++) {
            for (int j = 0; j < SHAPES.length; j++) {
                if (metavalues[i][j] == null) {
                    metavalues[i][j] = bsc.getDefaultState();
                }
            }
        }
        // Return handler object
        return new OurHandler(metavalues);
    }
    
    private static final int getShapeIndex(String v) {
        for (int i = 0; i < SHAPES.length; i++) {
            if (SHAPES[i].equals(v)) {
                return i;
            }
        }
        return 0;
    }

    class OurHandler implements IStateHandler {
        private String[] string_values;
        private Map<String, String>[] map_values;
        
        @SuppressWarnings("unchecked")
		OurHandler(StateRec[][] states) {
            string_values = new String[16*SHAPES.length];
            map_values = new Map[16*SHAPES.length];
            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < SHAPES.length; j++) {
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
            return "StairMetadataState";
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
