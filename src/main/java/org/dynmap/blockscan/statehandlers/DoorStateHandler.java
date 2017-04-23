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
     * This method is used to examining the BlockStateContainer of a block to determine if the state mapper can handle the given block
     * @param block - Block object
     * @param bsc - BlockStateContainer object
     * @returns IStateHandler if the handler factory believes it can handle this block type, null otherwise
     */
    public IStateHandler canHandleBlockState(Block block, BlockStateContainer bsc) {
        List<IBlockState> state = bsc.getValidStates();
        // Doors have 5 properties and 64 valid states
        if ((bsc.getProperties().size() != 5) || (state.size() != 64)) {
            return null;
        }
        IProperty<?> facing = IStateHandlerFactory.findMatchingProperty(bsc, "facing", FACING);
        IProperty<?> half = IStateHandlerFactory.findMatchingProperty(bsc, "half", HALF);
        IProperty<?> hinge = IStateHandlerFactory.findMatchingProperty(bsc, "hinge", HINGE);
        if ((facing == null) || (half == null) || (hinge == null)) {
            return null;
        }
        IProperty<?> open = IStateHandlerFactory.findMatchingBooleanProperty(bsc, "open");
        IProperty<?> powered = IStateHandlerFactory.findMatchingBooleanProperty(bsc, "powered");
        if ((open == null) || (powered == null)) {
            return null;
        }
        IBlockState[] metavalues = new IBlockState[64];
        for (IBlockState s : state) {
            int idx = getFacingIndex(s.getValue(facing).toString()) +
                    getHingeIndex(s.getValue(hinge).toString()) +
                    getHalfIndex(s.getValue(half).toString()) +
                    getPoweredIndex(s.getValue(powered).toString()) +
                    getOpenIndex(s.getValue(open).toString());
            metavalues[idx] = s;
        }
        // Return handler object
        return new OurHandler(metavalues);
    }
    // Return index for given facing value (offset by 4 bits)
    private static int getFacingIndex(String value) {
        for (int i = 0; i < FACING.length; i++) {
            if (FACING[i].equals(value)) {
                return 16 * i;
            }
        }
        return 0;
    }
    // Return index for given hinge value (offset by 2 bits)
    private static int getHingeIndex(String value) {
        for (int i = 0; i < HINGE.length; i++) {
            if (HINGE[i].equals(value)) {
                return 4 * i;
            }
        }
        return 0;
    }
    // Return index for given half value (offset by 3 bits)
    private static int getHalfIndex(String value) {
        for (int i = 0; i < HALF.length; i++) {
            if (HALF[i].equals(value)) {
                return 8 * i;
            }
        }
        return 0;
    }
    // Return index for given powered value (offset by 1 bits)
    private static int getPoweredIndex(String value) {
        if (value.equals("true"))
            return 2;
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
        
        OurHandler(IBlockState[] states) {
            string_values = new String[64];
            map_values = new Map[64];
            for (int i = 0; i < 64; i++) {
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
