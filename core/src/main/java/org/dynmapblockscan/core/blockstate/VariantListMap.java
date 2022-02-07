package org.dynmapblockscan.core.blockstate;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.dynmapblockscan.core.blockstate.VariantListMap;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class VariantListMap {
	public final Map<BaseCondition, VariantList> map;
	
	public VariantListMap(Map<BaseCondition, VariantList> m) {
		map = m;
	}
	
	// Custom deserializer to handle BaseCondition key
    public static class Deserializer implements JsonDeserializer<VariantListMap> {
    	@Override
        public VariantListMap deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
    		JsonObject obj = element.getAsJsonObject();
			Map<BaseCondition, VariantList> bld = new HashMap<BaseCondition, VariantList>();
			for (Entry<String, JsonElement> k : obj.entrySet()) {
				bld.put(new BaseCondition(k.getKey()), (VariantList)context.deserialize(k.getValue(), VariantList.class));
			}
			return new VariantListMap(bld);
        }
    }

    @Override
    public String toString() {
    	return map.toString();
    }
    
    @Override
    public int hashCode() {
    	return map.hashCode();
    }
    
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj instanceof VariantListMap) {
			VariantListMap vobj = (VariantListMap) obj;
			return map.equals(vobj.map);
		}
		return false;
	}
}
