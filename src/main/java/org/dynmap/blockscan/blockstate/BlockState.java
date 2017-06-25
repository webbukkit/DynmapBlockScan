package org.dynmap.blockscan.blockstate;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.inventory.ContainerShulkerBox;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.client.model.ForgeBlockStateV1;
import net.minecraftforge.client.model.ForgeBlockStateV1.Variant;

// Top level container class for JSON parsed BlockState data
public class BlockState {
	// Parser for BlockState
	private static Gson GSON;

	// Forge marker version (0=vanilla)
	public int forge_marker = 0;
	// "variants" map: key is state string, value is either Variant or list of Variant object (need special parser)
	public VariantListMap variants;
	// "multipart" list: each record is a MultiPart
	public List<Multipart> multipart;
	
	// Forge specific
	public ForgeVariantV1 defaults;
	public Map<String, ForgeVariantV1List> forge_variants;
	
	// Property value based nested state mapping
	public String nestedProp = null;
	public Map<String, BlockState> nestedValueMap = null;
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (variants != null) {
			sb.append("variants={").append(variants).append("}");
		}
		if (multipart != null) {
			sb.append("multipart=[").append(multipart).append("]");
		}
		return sb.toString();
	}
	// Build Gson parser for BlockState
	public static Gson buildParser() {
		Gson g = GSON;
		if (g == null) {
			GsonBuilder gb = new GsonBuilder();	// Start with builder
			gb.registerTypeAdapter(BlockState.class, new BlockState.Deserializer());
			gb.registerTypeAdapter(VariantList.class, new VariantList.Deserializer()); // Add VariantList handler
            gb.registerTypeAdapter(ForgeVariantV1List.class, new ForgeVariantV1List.Deserializer()); // Add ForgeVariantV1List handler
            gb.registerTypeAdapter(ForgeVariantV1.class, new ForgeVariantV1.Deserializer()); // Add ForgeVariantV1 handler
			gb.registerTypeAdapter(VariantListMap.class, new VariantListMap.Deserializer()); // Add VariantListMap handler
			gb.registerTypeAdapter(Condition.class, new Condition.Deserializer()); // Add Condition handler1
			g = gb.create();
			GSON = g;
		}
		return g;
	}
	// Get render properties
	public Set<String> getRenderProps() {
		HashSet<String> props = new HashSet<String>();
		if (variants != null) {
			for (BaseCondition cond : variants.map.keySet()) {
				cond.addPropKeys(props);
			}
		}
		if (multipart != null) {
			for (Multipart mp : multipart) {
				if (mp.when != null) {
					mp.when.addPropKeys(props);
				}
			}
		}
		return props;
	}
	
    // Build list of Variant lists from parsed block state and match given properties
    public List<VariantList> getMatchingVariants(Map<String, String> prop) {
    	List<VariantList> vlist = new ArrayList<VariantList>();
    	
    	// If bstate has variant list map, walk it - only match first one
    	if (this.variants != null) {
    		for (Entry<BaseCondition, org.dynmap.blockscan.blockstate.VariantList> var : this.variants.map.entrySet()) {
    			if(var.getKey().matches(prop)) {	// Matching property?
    				// Only one will match for 'variants', so quit
    				vlist.add(var.getValue());
    				break;
    			}
    		}
    	}
    	// If bstate has multipart, walk it - accumulate all matches
    	if (this.multipart != null) {
    		for (Multipart mp : this.multipart) {
    			if (mp.when == null) {	// Unconditional?
    				vlist.add(mp.apply);	// Add it
    			}
    			else if (mp.when.matches(prop)) {	// Conditional matches?
    				vlist.add(mp.apply);	// Add it
    			}
    		}
    	}
    	// If nested, process request
    	if (this.nestedProp != null) {
    		String pval = prop.get(this.nestedProp);
    		if (pval != null) {
    			BlockState bs = this.nestedValueMap(pval);
    			if (bs != null) {
    				vlist = bs.getMatchingVariants(prop);
    			}
    		}
    	}
    	return vlist;
    }
	private BlockState nestedValueMap(String pval) {
		// TODO Auto-generated method stub
		return null;
	}
	
   // Custom deserializer - handles forge vs vanilla
    public static class Deserializer implements JsonDeserializer<BlockState> {
        @Override
        public BlockState deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
            BlockState bs = new BlockState();
            // See if we have forge marker
            JsonObject obj = element.getAsJsonObject();
            if (obj.has("forge_marker")) {
                bs.forge_marker = obj.get("forge_marker").getAsInt();
                if (obj.has("defaults")) {
                    bs.defaults = context.deserialize(obj.getAsJsonObject("defaults"), ForgeVariantV1.class);
                }
                // Go through variants
                bs.forge_variants = new HashMap<String, ForgeVariantV1List>();
                if (obj.has("variants")) {
                    for (Entry<String, JsonElement> e : obj.get("variants").getAsJsonObject().entrySet()) {
                        if (e.getValue().isJsonArray()) {
                            bs.forge_variants.put(e.getKey(), context.deserialize(e.getValue(), ForgeVariantV1List.class));
                        }
                        else {
                            JsonObject vobj = e.getValue().getAsJsonObject();
                            // If first element is an object (versus a value)
                            if(vobj.entrySet().iterator().next().getValue().isJsonObject()) {
                                // Assume all subelements are values for key=value test
                                for (Entry<String, JsonElement> se : vobj.entrySet()) {
                                    bs.forge_variants.put(e.getKey() + "=" + se.getKey(), context.deserialize(se.getValue(), ForgeVariantV1List.class));
                                }
                            }
                            else {
                                bs.forge_variants.put(e.getKey(), context.deserialize(e.getValue(), ForgeVariantV1List.class));
                            }
                        }
                    }
                }
            }
            else if (obj.has("multipart")) {
                bs.multipart = new ArrayList<Multipart>();
                for (JsonElement je : obj.getAsJsonArray("multipart")) {
                    bs.multipart.add(context.deserialize(je, Multipart.class));
                }
            }
            else if (obj.has("variants")) {
                bs.variants = context.deserialize(obj.get("variants"), VariantListMap.class);
            }
            return bs;
        }
    }

}
