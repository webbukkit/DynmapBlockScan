package org.dynmap.blockscan.forge_1_18.statehandlers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.dynmap.blockscan.forge_1_18.statehandlers.StateContainer.StateRec;

/**
 * This state handler is used for blocks which preserve a simple 1-1 correlation between 
 * metadata values and block state, except for also having a 'occupied' parameter (referring to having
 * an occupant, as for bed blocks)
 * 
 * @author Mike Primm
 */
public class BedMetadataStateHandler implements IStateHandlerFactory {
    /**
     * This method is used to examining the BlockStateContainer of a block to determine if the state mapper can handle the given block
     * @param bsc - StateContainer object
     * @returns IStateHandler if the handler factory believes it can handle this block type, null otherwise
     */
	@Override
    public IStateHandler canHandleBlockState(StateContainer bsc) {
		if (!IStateHandlerFactory.findMatchingBooleanProperty(bsc, "occupied")) {
			return null;
		}
        List<StateRec> state = bsc.getValidStates();
        // More states than metadata values - cannot handle this
        if (state.size() > 32) {
            return null;
        }
        StateRec[] metavalues = new StateRec[16];
        StateRec[] occupiedmetavalues = new StateRec[16];
        for (StateRec s : state) {
            String is_occupied = s.getValue("occupied");
            for (int meta : s.metadata) {
            	// If out of range, or duplicate, we cannot handle
            	if ((meta < 0) || (meta > 15)) {
            		return null;
            	}
            	else if (is_occupied.equals("false")) {
            		if (metavalues[meta] != null) {
            			return null;
            		}
            		else {
            			metavalues[meta] = s;
            		}
            	}
            	else {
            		if (occupiedmetavalues[meta] != null) {
            			return null;
            		}
            		else {
            			occupiedmetavalues[meta] = s;
            		}
            	}
            }
        }
        // Fill in any missing metadata with default state
        for (int i = 0; i < metavalues.length; i++) {
            if (metavalues[i] == null) {
                metavalues[i] = bsc.getDefaultState();
            }
            if (occupiedmetavalues[i] == null) {
                occupiedmetavalues[i] = bsc.getDefaultState();
            }
        }
        // Return handler object
        return new OurHandler(metavalues, occupiedmetavalues);
    }

    class OurHandler implements IStateHandler {
        private String[] string_values;
        private Map<String, String>[] map_values;
        
        @SuppressWarnings("unchecked")
		OurHandler(StateRec[] states, StateRec[] occupiedstates) {
            string_values = new String[32];
            map_values = new Map[32];
            // Handle occupied states first
            for (int i = 0; i < 16; i++) {
                StateRec bs = occupiedstates[i];
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
            // Handle unoccupied states second
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
            return "BedMetadataState";
        }
        @Override
        public int getBlockStateIndex(int blockid, int blockmeta) {
            return blockmeta; //TODO: we need the logic for looking for occupant
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
