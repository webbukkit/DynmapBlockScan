package org.dynmapblockscan.core.blockstate;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.dynmapblockscan.core.AbstractBlockScanBase;
import org.dynmapblockscan.core.blockstate.BSBlockState;
import org.dynmapblockscan.core.model.BlockModel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

// Top level container class for JSON parsed BlockState data
public class BSBlockState {
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
	public Map<BaseCondition, ForgeVariantV1List> forge_variants;
	
	// Property value based nested state mapping
	public String nestedProp = null;
	public Map<String, BSBlockState> nestedValueMap = null;
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (variants != null) {
			sb.append("variants={").append(variants).append("} ");
		}
		if (multipart != null) {
			sb.append("multipart=[").append(multipart).append("] ");
		}
		if (nestedProp != null) {
			sb.append("nestedProp=" + nestedProp + " ");
		}
		if (nestedValueMap != null) {
			sb.append("nestedValueMap=[").append(nestedValueMap).append("] ");
		}
		if (defaults != null) {
			sb.append("defaults=").append(defaults).append(" ");
		}
		if (forge_variants != null) {
			sb.append("forge_variants=[").append(forge_variants).append("] ");			
		}
		return sb.toString();
	}
	// Build Gson parser for BlockState
	public static Gson buildParser() {
		Gson g = GSON;
		if (g == null) {
			GsonBuilder gb = new GsonBuilder();	// Start with builder
			gb.registerTypeAdapter(BSBlockState.class, new BSBlockState.Deserializer());
			gb.registerTypeAdapter(Variant.class, new Variant.Deserializer()); // Add Variant handler
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
		if (forge_variants != null) {
		    for (Entry<BaseCondition, ForgeVariantV1List> var : forge_variants.entrySet()) {
		        BaseCondition bc = var.getKey();
		        bc.addPropKeys(props);    // Add keys from variant conditions
		    }
		}
		return props;
	}
	
    // Build list of Variant lists from parsed block state and match given properties
	// Note: returned value is list of alternative lists: all elements of top level list apply, with one random choice
	// picked from the sublist (VariantList) when more than one is present
    public List<VariantList> getMatchingVariants(Map<String, String> prop, Map<String, BlockModel> models) {
    	List<VariantList> vlist = new ArrayList<VariantList>();
    	
    	// If bstate has variant list map, walk it - only match first one
    	if (this.variants != null) {
    		for (Entry<BaseCondition, org.dynmapblockscan.core.blockstate.VariantList> var : this.variants.map.entrySet()) {
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
    	// If forge variants, walk it - accumulate all matches
    	if (this.forge_variants != null) {
    	    ForgeVariantV1List resolved = buildResolvedForgeList(prop);
    	    ArrayList<Variant> vanilla = new ArrayList<Variant>();
    	    // Now, generate vanilla variant for each
    	    for (ForgeVariantV1 var : resolved.variantList) {
    	        Variant v = var.generateVanilla(models);
    	        if (v != null) {
    	            vanilla.add(v);
    	        }
    	    }
    	    vlist.add(new VariantList(vanilla));
    	}
    	// If nested, process request
    	if (this.nestedProp != null) {
    		String pval = prop.get(this.nestedProp);
    		if (pval != null) {
    			BSBlockState bs = this.nestedValueMap(pval);
    			if (bs != null) {
    				vlist = bs.getMatchingVariants(prop, models);
    			}
    		}
    	}
    	return vlist;
    }
	private BSBlockState nestedValueMap(String pval) {
		// TODO Auto-generated method stub
		return null;
	}
	
   // Custom deserializer - handles forge vs vanilla
    public static class Deserializer implements JsonDeserializer<BSBlockState> {
        @Override
        public BSBlockState deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
            BSBlockState bs = new BSBlockState();
            // See if we have forge marker
            JsonObject obj = element.getAsJsonObject();
            bs.forge_marker = 0;
            if (obj.has("forge_marker")) {
                bs.forge_marker = obj.get("forge_marker").getAsInt();
            }
            if (bs.forge_marker == 1) {
                if (obj.has("defaults")) {
                    bs.defaults = context.deserialize(obj.getAsJsonObject("defaults"), ForgeVariantV1.class);
                }
                // Go through variants
                bs.forge_variants = new HashMap<BaseCondition, ForgeVariantV1List>();
                if (obj.has("variants")) {
                    for (Entry<String, JsonElement> e : obj.get("variants").getAsJsonObject().entrySet()) {
                        if (e.getValue().isJsonArray()) {
                            bs.forge_variants.put(new BaseCondition(e.getKey()), context.deserialize(e.getValue(), ForgeVariantV1List.class));
                        }
                        else {
                            JsonObject vobj = e.getValue().getAsJsonObject();
                            // If first element is an object (versus a value)
                            if(vobj.entrySet().iterator().hasNext() && vobj.entrySet().iterator().next().getValue().isJsonObject()) {
                                // Assume all subelements are values for key=value test
                                for (Entry<String, JsonElement> se : vobj.entrySet()) {
                                    bs.forge_variants.put(new BaseCondition(e.getKey() + "=" + se.getKey()), context.deserialize(se.getValue(), ForgeVariantV1List.class));
                                }
                            }
                            else {
                                bs.forge_variants.put(new BaseCondition(e.getKey()), context.deserialize(e.getValue(), ForgeVariantV1List.class));
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
    // Build resolved forge variant list
    private ForgeVariantV1List buildResolvedForgeList(Map<String,String> prop) {
        // Create empty initial list
        ForgeVariantV1List varlist = new ForgeVariantV1List();
        // Now, see what variants also match, and apply them too
        for (Entry<BaseCondition, ForgeVariantV1List> var : this.forge_variants.entrySet()) {
            if (var.getKey().matches(prop)) {
                varlist.applyValues(var.getValue(), false);
            }
        }
        // Apply defaults to resulting list
        for (ForgeVariantV1 var : varlist.variantList) {
            var.applyDefaults(defaults);
        }
        
        return varlist;
    }

}
