package org.dynmap.blockscan.forge_1_15_2.blockstate;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

// Container for conditions
public interface Condition {
	// Check for condition match : matches if any values in condition match corresponding values in provided properties
	public boolean matches(Map<String, String> props);
	// Add distinct property keys to set
	public void addPropKeys(Set<String> props);
	
	// Custom deserializer to handle distinction between BaseCondition, ANDCondition and ORCondition
    public static class Deserializer implements JsonDeserializer<Condition> {
    	@Override
        public Condition deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
    		Condition cond;
    		JsonObject obj = element.getAsJsonObject();
    		if (obj.isJsonPrimitive()) {	// If simple conditional (key in variants)
    			cond = new BaseCondition(obj.getAsString());
    		}
    		else if (obj.has("OR")) {	// Has OR field
    			JsonArray oval = obj.get("OR").getAsJsonArray();	// Get OR values as array
    			ORCondition ocond = new ORCondition();
    			ocond.conditions = new ArrayList<BaseCondition>();
                for (JsonElement jsonelement : oval) {
                	ocond.conditions.add(parseBaseCondition(jsonelement.getAsJsonObject()));
                }
                cond = ocond;
    		}
    		else if (obj.has("AND")) {	// Has AND field
    			JsonArray aval = obj.get("AND").getAsJsonArray();	// Get OR values as array
    			ANDCondition acond = new ANDCondition();
    			acond.conditions = new ArrayList<BaseCondition>();
                for (JsonElement jsonelement : aval) {
                	acond.conditions.add(parseBaseCondition(jsonelement.getAsJsonObject()));
                }
                cond = acond;
    		}
    		else {	// Else, just base conditional
    			cond = parseBaseCondition(obj);
    		}
    		return cond;
        }
    	private BaseCondition parseBaseCondition(JsonObject jsonelement) {
			ImmutableMap.Builder<String,String> bld = ImmutableMap.builder();
			for (Entry<String, JsonElement> k : jsonelement.entrySet()) {
				bld.put(k.getKey(), k.getValue().getAsString());
			}
			return new BaseCondition(bld.build());
    	}
    }
}
