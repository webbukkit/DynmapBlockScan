package org.dynmap.blockscan.blockstate;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;


// Container class for list of Variant objects for JSON parsed BlockState data
// Requires special JSON deserializer as can be single Variant object OR list of them (in JSON)
public class ForgeVariantV1List {
	public List<ForgeVariantV1> variantList;
	
	public ForgeVariantV1List() {
	    this.variantList = new ArrayList<ForgeVariantV1>();
	}
	
	public ForgeVariantV1List(List<ForgeVariantV1> lst) {
		this.variantList = lst;
	}
	
	public String toString() {
		return variantList.toString();
	}
	
	// Custom deserializer - handles singleton and list formats
    public static class Deserializer implements JsonDeserializer<ForgeVariantV1List> {
    	@Override
        public ForgeVariantV1List deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
            List<ForgeVariantV1> list = Lists.<ForgeVariantV1>newArrayList();
            // If element is an array, parse list
            if (element.isJsonArray()) {
            	// Fetch array
                JsonArray jsonarray = element.getAsJsonArray();
                // Add all elements as Variants to thelist
                for (JsonElement jsonelement : jsonarray) {
                    list.add(context.deserialize(jsonelement, ForgeVariantV1.class));
                }
            }
            else {	// Else, just one Variant - add just it
                list.add(context.deserialize(element, ForgeVariantV1.class));
            }
            return new ForgeVariantV1List(list);
        }
    }
    
    public void applyValues(ForgeVariantV1List src, boolean no_submodels) {
        // Make sure list is big enough for values to be applied
        while (src.variantList.size() > this.variantList.size()) {
            ForgeVariantV1 newv1 = new ForgeVariantV1();
            this.variantList.add(newv1);
        }
        for (int i = 0; i < src.variantList.size(); i++) {
            this.variantList.get(i).applyValues(src.variantList.get(i), no_submodels);
        }
    }
}
