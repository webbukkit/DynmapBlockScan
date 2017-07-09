package org.dynmap.blockscan;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.dynmap.blockscan.blockstate.BaseCondition;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

// Container for JSON-encoded block state overrides (to handle bogus Mojang block state file name options
public class BlockStateOverrides {
	public Map<String, Map<String, BlockStateOverride>> overrides = Collections.emptyMap();
    public Map<String, Map<String, BlockTintOverride[]>> tinting = Collections.emptyMap();
	
	public static class BlockStateOverride {
		public String baseNameProperty = null;	// If property used for base name, this is property name
		public String nameSuffix = "";			// If base name has suffix
		public String blockStateName = null;	// If defined, this is name of block state resource
	}
    public static class BlockTintOverride {
        public BaseCondition state;    // Condition for matching block state (null=all states)
        public String[] colormap;    // Colormap resource
        
        // Custom deserializer
        public static class Deserializer implements JsonDeserializer<BlockTintOverride> {
            @Override
            public BlockTintOverride deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
                BlockTintOverride ovr = new BlockTintOverride();
                JsonObject obj = element.getAsJsonObject();
                if (obj.has("state")) {
                    ovr.state = new BaseCondition(obj.get("state").getAsString());
                }
                if (obj.has("colormap")) {
                    JsonArray ary = obj.getAsJsonArray("colormap");
                    ovr.colormap = new String[ary.size()];
                    for (int i = 0; i < ary.size(); i++) {
                        ovr.colormap[i] = ary.get(i).getAsString();
                    }
                }
                return ovr;
            }
        }
    }
	
	public BlockStateOverride getOverride(String modid, String blkid) {
		Map<String, BlockStateOverride> map = overrides.get(modid);
		if (map != null) {
			return map.get(blkid);
		}
		return null;
	}
	
    public BlockTintOverride getTinting(String modid, String blkid, Map<String,String> prop) {
        Map<String, BlockTintOverride[]> modmatch = tinting.get(modid);
        if (modmatch != null) {
            BlockTintOverride[] blkmatch = modmatch.get(blkid);
            if (blkmatch != null) {
                for (BlockTintOverride ovr : blkmatch) {
                    if ((ovr.state == null) || (ovr.state.matches(prop))) {
                        return ovr;
                    }
                }
            }
        }
        return null;
    }
    
    public void merge(BlockStateOverrides ovr) {
        // Merge in block state overrides
        for (Entry<String, Map<String, BlockStateOverride>> mod : ovr.overrides.entrySet()) {
            String modid = mod.getKey();
            if (overrides.containsKey(modid) == false) {
                overrides.put(modid, new HashMap<String, BlockStateOverride>());
            }
            Map<String, BlockStateOverride> bso = overrides.get(modid);
            for (Entry<String, BlockStateOverride> blk : mod.getValue().entrySet()) {
                bso.put(blk.getKey(), blk.getValue());
            }
        }
        // Merge in tinting overrides
        for (Entry<String, Map<String, BlockTintOverride[]>> mod : ovr.tinting.entrySet()) {
            String modid = mod.getKey();
            if (tinting.containsKey(modid) == false) {
                tinting.put(modid, new HashMap<String, BlockTintOverride[]>());
            }
            Map<String, BlockTintOverride[]> bto = tinting.get(modid);
            for (Entry<String, BlockTintOverride[]> blk : mod.getValue().entrySet()) {
                bto.put(blk.getKey(), blk.getValue());
            }
        }
    }
    
}
