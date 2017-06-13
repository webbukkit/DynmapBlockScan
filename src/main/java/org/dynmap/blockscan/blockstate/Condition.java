package org.dynmap.blockscan.blockstate;

import java.lang.reflect.Type;
import java.util.ArrayList;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

// Container for conditions
public interface Condition {
	// Custom deserializer to handle distinction between BaseCondition, ANDCondition and ORCondition
    public static class Deserializer implements JsonDeserializer<Condition> {
    	@Override
        public Condition deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
    		Condition cond;
    		JsonObject obj = element.getAsJsonObject();
    		if (obj.has("OR")) {	// Has OR field
    			JsonArray oval = obj.get("OR").getAsJsonArray();	// Get OR values as array
    			ORCondition ocond = new ORCondition();
    			ocond.conditions = new ArrayList<BaseCondition>();
                for (JsonElement jsonelement : oval) {
                	ocond.conditions.add((BaseCondition)context.deserialize(jsonelement, BaseCondition.class));
                }
                cond = ocond;
    		}
    		else if (obj.has("AND")) {	// Has AND field
    			JsonArray aval = obj.get("AND").getAsJsonArray();	// Get OR values as array
    			ANDCondition acond = new ANDCondition();
    			acond.conditions = new ArrayList<BaseCondition>();
                for (JsonElement jsonelement : aval) {
                	acond.conditions.add((BaseCondition)context.deserialize(jsonelement, BaseCondition.class));
                }
                cond = acond;
    		}
    		else {	// Else, just base conditional
    			cond  = (BaseCondition)context.deserialize(obj, BaseCondition.class);
    		}
    		return cond;
        }
    }
}
