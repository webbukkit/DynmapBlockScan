package org.dynmap.blockscan_1_16_5.blockstate;

import java.lang.reflect.Type;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;


// Container class for list of Variant objects for JSON parsed BlockState data
// Requires special JSON deserializer as can be single Variant object OR list of them (in JSON)
public class VariantList {
	public List<Variant> variantList;
	
	public VariantList(List<Variant> lst) {
		this.variantList = lst;
	}
	
	public String toString() {
		return variantList.toString();
	}
	
	// Custom deserializer - handles singleton and list formats
    public static class Deserializer implements JsonDeserializer<VariantList> {
    	@Override
        public VariantList deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
            List<Variant> list = Lists.<Variant>newArrayList();
            // If element is an array, parse list
            if (element.isJsonArray()) {
            	// Fetch array
                JsonArray jsonarray = element.getAsJsonArray();
                // Add all elements as Variants to thelist
                for (JsonElement jsonelement : jsonarray) {
                    list.add((Variant)context.deserialize(jsonelement, Variant.class));
                }
            }
            else {	// Else, just one Variant - add just it
                list.add((Variant)context.deserialize(element, Variant.class));
            }
            return new VariantList(list);
        }
    }
}
