package org.dynmap.blockscan.forge_1_14_4.statehandlers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.dynmap.blockscan.forge_1_14_4.statehandlers.StateContainer.StateRec;

/**
 * This state handler is used for blocks which preserve a simple 1-1 correlation between 
 * metadata values and block state, except for also having a 'snowy' parameter (referring to having
 * snow on top, as for grass blocks)
 * 
 * @author Mike Primm
 */
public class SnowyMetadataStateHandler implements IStateHandlerFactory {
    /**
     * This method is used to examining the StateContainer of a block to determine if the state mapper can handle the given block
     * @param bsc - StateContainer object
     * @returns IStateHandler if the handler factory believes it can handle this block type, null otherwise
     */
    public IStateHandler canHandleBlockState(StateContainer bsc) {
        boolean snowy = IStateHandlerFactory.findMatchingBooleanProperty(bsc, "snowy");
        if (!snowy) {
            return null;
        }
        List<StateRec> state = bsc.getValidStates();
        // More states than metadata values - cannot handle this
        if (state.size() > 32) {
            return null;
        }
        StateRec[] metavalues = new StateRec[16];
        StateRec[] snowmetavalues = new StateRec[16];
        for (StateRec s : state) {
            String is_snowy = s.getValue("snowy");
            for (int meta : s.metadata) {
            	// If out of range, or duplicate, we cannot handle
            	if ((meta < 0) || (meta > 15)) {
            		return null;
            	}
            	else if (is_snowy.equals("false")) {
            		if (metavalues[meta] != null) {
            			return null;
            		}
            		else {
            			metavalues[meta] = s;
            		}
            	}
            	else {
            		if (snowmetavalues[meta] != null) {
            			return null;
            		}
            		else {
            			snowmetavalues[meta] = s;
            		}
            	}
            }
        }
        // Fill in any missing metadata with default state
        for (int i = 0; i < metavalues.length; i++) {
            if (metavalues[i] == null) {
                metavalues[i] = bsc.getDefaultState();
            }
            if (snowmetavalues[i] == null) {
                snowmetavalues[i] = bsc.getDefaultState();
            }
        }
        // Return handler object
        return new OurHandler(metavalues, snowmetavalues);
    }

    class OurHandler implements IStateHandler {
        private String[] string_values;
        private Map<String, String>[] map_values;
        
        @SuppressWarnings("unchecked")
		OurHandler(StateRec[] states, StateRec[] snowstates) {
            string_values = new String[32];
            map_values = new Map[32];
            // Handle snowy states first
            for (int i = 0; i < 16; i++) {
                StateRec bs = snowstates[i];
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
            // Handle non-snow states second
            for (int i = 0; i < 16; i++) {
                StateRec bs = states[i];
                HashMap<String, String> m = new HashMap<String,String>();
                StringBuilder sb = new StringBuilder();
                for (Entry<String, String> p : bs.getProperties().entrySet()) {
                    if (sb.length() > 0) sb.append(",");
                    sb.append(p.getKey()).append("=").append(p.getValue());
                    m.put(p.getKey(), p.getValue());
                }
                map_values[16+i] = m;
                string_values[16+i] = sb.toString();
            }
        }
        @Override
        public String getName() {
            return "SnowyMetadataState";
        }
        @Override
        public int getBlockStateIndex(int blockid, int blockmeta) {
            return blockmeta; //TODO: we need the logic for looking for snow above here
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
