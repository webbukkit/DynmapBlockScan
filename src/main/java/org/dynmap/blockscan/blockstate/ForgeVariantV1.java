package org.dynmap.blockscan.blockstate;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.dynmap.blockscan.model.BlockElement;
import org.dynmap.blockscan.model.BlockModel;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

// Container for parsed JSON encoding of Variant from blockstate
public class ForgeVariantV1 {
	public String model;
    // Texture aliases
    public Map<String, String> textures = Collections.emptyMap();
	public Integer x;
	public Integer y;
	public Boolean uvlock;
    public Integer weight;
    public Boolean smooth_lighting;
    public Boolean gui3d;
    public Map<String, ForgeVariantV1List> submodel;
    public String simple_submodel;
    public Map<String, String> custom;

	@Override
	public int hashCode() {
		return model.hashCode() ^ x ^ (y << 8) ^ (weight << 16) ^ (uvlock?12345:0);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj instanceof ForgeVariantV1) {
			ForgeVariantV1 vobj = (ForgeVariantV1) obj;
			return (model.equals(vobj.model) && (x == vobj.x) && (y == vobj.y) && (uvlock == vobj.uvlock) && (weight == vobj.weight));
		}
		return false;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("{");
		sb.append("model=").append(model);
		if (x != 0)
			sb.append(",x=").append(x);
		if (y != 0)
			sb.append(",y=").append(y);
		if (uvlock)
			sb.append(",uvlock=true");
		if (weight != 1)
			sb.append(",weight=").append(weight);
		sb.append("}");
		return sb.toString();
	}
	
   // Custom deserializer - handles singleton and list formats
    public static class Deserializer implements JsonDeserializer<ForgeVariantV1> {
        @Override
        public ForgeVariantV1 deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
            ForgeVariantV1 v = new ForgeVariantV1();
            JsonObject jobj = element.getAsJsonObject();
            if (jobj.has("model")) {
                v.model = jobj.get("model").getAsString();
            }
            if (jobj.has("textures")) {
                v.textures = context.deserialize(jobj.get("textures"), Map.class);
            }
            if (jobj.has("x")) {
                v.x = context.deserialize(jobj.get("x"), Integer.class);
            }
            if (jobj.has("y")) {
                v.y = context.deserialize(jobj.get("y"), Integer.class);
            }
            if (jobj.has("uvlock")) {
                v.uvlock = context.deserialize(jobj.get("uvlock"), Boolean.class);
            }
            if (jobj.has("weight")) {
                v.weight = context.deserialize(jobj.get("weight"), Integer.class);
            }
            if (jobj.has("smooth_lighting")) {
                v.smooth_lighting = context.deserialize(jobj.get("smooth_lighting"), Boolean.class);
            }
            if (jobj.has("gui3d")) {
                v.gui3d = context.deserialize(jobj.get("gui3d"), Boolean.class);
            }
            if (jobj.has("submodel")) {
                JsonElement submod = jobj.get("submodel");
                if (submod.isJsonPrimitive()) { // Simple value
                    v.simple_submodel = submod.getAsString();
                }
                else {  // Else, map
                    v.submodel = new HashMap<String, ForgeVariantV1List>();
                    for (Entry<String, JsonElement> vv : submod.getAsJsonObject().entrySet()) {
                        v.submodel.put(vv.getKey(), context.deserialize(vv.getValue(), ForgeVariantV1List.class));
                    }
                }
            }
            if (jobj.has("custom")) {
                v.custom = context.deserialize(jobj.get("custom"), Map.class);
            }
            return v;
        }
    }

}
