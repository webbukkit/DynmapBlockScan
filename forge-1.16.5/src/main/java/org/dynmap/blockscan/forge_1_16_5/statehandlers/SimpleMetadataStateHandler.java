package org.dynmap.blockscan.forge_1_16_5.statehandlers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.dynmap.blockscan.forge_1_16_5.statehandlers.StateContainer.StateRec;

/**
 * This state handler is used for blocks which preserve a simple 1-1 correlation between 
 * metadata values and block state
 * 
 * @author Mike Primm
 */
public class SimpleMetadataStateHandler implements IStateHandlerFactory {
    private boolean strict;
    
    public SimpleMetadataStateHandler(boolean isStrict) {
        strict = isStrict;
    }
    /**
     * This method is used to examining the StateContainer of a block to determine if the state mapper can handle the given block
     * @param bsc - StateContainer object
     * @returns IStateHandler if the handler factory believes it can handle this block type, null otherwise
     */
    public IStateHandler canHandleBlockState(StateContainer bsc) {
        List<StateRec> state = bsc.getValidStates();
        // More states than metadata values - cannot handle this
        if (strict && (state.size() > 16)) {
            return null;
        }
        StateRec[] metavalues = new StateRec[16];
        for (StateRec s : state) {
        	for (int meta : s.metadata) {
        		// If out of range, or duplicate, we cannot handle
        		if ((meta < 0) || (meta > 15) || (metavalues[meta] != null)) {
        		    if (strict) {
        		        return null;
        		    }
        		    else {
        		        continue;
        		    }
        		}
        		metavalues[meta] = s;
        	}
        }
        // Fill in any missing metadata with default state
        for (int i = 0; i < metavalues.length; i++) {
            if (metavalues[i] == null) {
                metavalues[i] = bsc.getDefaultState();
            }
        }
        // Return handler object
        return new OurHandler(metavalues);
    }

    class OurHandler implements IStateHandler {
        private String[] string_values;
        private Map<String, String>[] map_values;
        
        @SuppressWarnings("unchecked")
		OurHandler(StateRec[] states) {
            string_values = new String[16];
            map_values = new Map[16];
            for (int i = 0; i < 16; i++) {
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
            if (strict)
                return "SimpleMetadataState";
            else
                return "DefaultMetadataState";
        }
        @Override
        public int getBlockStateIndex(int blockid, int blockmeta) {
            return blockmeta;
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
