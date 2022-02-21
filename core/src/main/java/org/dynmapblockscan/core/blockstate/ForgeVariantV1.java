package org.dynmapblockscan.core.blockstate;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.dynmapblockscan.core.blockstate.ForgeVariantV1;
import org.dynmapblockscan.core.model.BlockModel;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

// Container for parsed JSON encoding of Variant from blockstate
public class ForgeVariantV1 {
	public String model;
    // Texture aliases
    public Map<String, String> textures = new HashMap<String, String>();
	public Integer x;
	public Integer y;
	public Integer z;
	public Boolean uvlock;
    public Integer weight;
    public Boolean smooth_lighting;
    public Boolean gui3d;
    public Map<String, ForgeVariantV1List> submodel;
    public Map<String, Object> custom;
    
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
		if (model != null) {
		    sb.append("model=").append(model);
		}
		if ((x != null) && (x != 0)) {
			sb.append(",x=").append(x);
		}
		if ((y != null) && (y != 0)) {
			sb.append(",y=").append(y);
		}
		if ((z != null) && (z != 0)) {
			sb.append(",z=").append(y);
		}
		if ((uvlock != null) && uvlock.booleanValue()) {
			sb.append(",uvlock=true");
		}
		if ((weight != null) && (weight != 1)) {
			sb.append(",weight=").append(weight);
		}
		if ((textures != null) && (textures.isEmpty() == false)) {
		    sb.append(",textures=" + textures);
		}
		if ((submodel != null) && (submodel.isEmpty() == false)) {
		    sb.append(",submodel=" + submodel);
		}
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
            if (jobj.has("z")) {
                v.z = context.deserialize(jobj.get("z"), Integer.class);
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
                v.submodel = new HashMap<String, ForgeVariantV1List>();
                if (submod.isJsonPrimitive()) { // Simple value
                    ForgeVariantV1 v1 = new ForgeVariantV1();
                    v1.model = submod.getAsString();
                    v.submodel.put("simple", new ForgeVariantV1List(Collections.singletonList(v1)));
                }
                else {  // Else, map
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
    
    public void applyValues(ForgeVariantV1 src, boolean no_submodels) {
        if (src == null) {
            return;
        }
        if (this.model == null) {
            this.model = src.model;
        }
        // Add undefined values
        for (Entry<String, String> ent : src.textures.entrySet()) {
            if (this.textures.containsKey(ent.getKey()) == false) {
                this.textures.put(ent.getKey(), ent.getValue());
            }
        }
        if (this.x == null) {
            this.x = src.x;
        }
        if (this.y == null) {
            this.y = src.y;
        }
        if (this.z == null) {
            this.z = src.z;
        }
        if (this.uvlock == null) {
            this.uvlock = src.uvlock;
        }
        if (this.weight == null) {
            this.weight = src.weight;
        }
        if (this.smooth_lighting == null) {
            this.smooth_lighting = src.smooth_lighting;
        }
        if (this.gui3d == null) {
            this.gui3d = src.gui3d;
        }
        if (src.custom != null) {
            if (this.custom == null) {
                this.custom = new HashMap<String, Object>();
            }
            for (Entry<String, Object> ent : src.custom.entrySet()) {
            	String key = ent.getKey().toString();
                if (this.custom.containsKey(key) == false) {
                	String val = ent.getValue().toString();
                    this.custom.put(key, val);
                }
            }
        }
        if ((!no_submodels) && (src.submodel != null)) {
            if (this.submodel == null) {
                this.submodel = new HashMap<String, ForgeVariantV1List>();
            }
            for (Entry<String, ForgeVariantV1List> ent : src.submodel.entrySet()) {
                ForgeVariantV1List v1l = this.submodel.get(ent.getKey());
                if (v1l == null) {
                    v1l = new ForgeVariantV1List();
                    this.submodel.put(ent.getKey(),  v1l);
                }
                // Apply values corresponding to source to submodels
                v1l.applyValues(ent.getValue(), true);
            }
        }
    }
    // Apply default to parent variants, and parent variant values to submodels
    public void applyDefaults(ForgeVariantV1 defs) {        
        // Apply defaults to variant
        if (defs != null) {
            this.applyValues(defs, true);
        }
        // Apply our values (including defaults) to submodels
        if (this.submodel != null) {
            for (Entry<String, ForgeVariantV1List> ent : this.submodel.entrySet()) {
                ForgeVariantV1List v1l = this.submodel.get(ent.getKey());
                for (ForgeVariantV1 v1 : v1l.variantList) {
                    v1.applyValues(this, false);
                }
            }
        }
    }
    // Generate vanilla variant AND add generated model to models, if needed
    public Variant generateVanilla(Map<String, BlockModel> models) {
        String mod = this.model;
        if ((this.textures != null) && (this.textures.isEmpty() == false) && (this.model != null)) {    // Customized?  Need model
            BlockModel bm = new BlockModel(mod, this.textures);
            // Generate model ID
            int idx = models.size();
            String modid = "dynmapblockscan:block/forgemodel" + idx;
            while (models.containsKey(modid) == true) {
                idx++;
                modid = "dynmapblockscan:block/forgemodel" + idx;
            }
            // Add model to model table
            models.put(modid, bm);
            // Switch our model reference to corresponding blockstate level model reference
            mod = "dynmapblockscan:block/forgemodel" + idx;
        }
        // Now build vanilla variant
        Variant var = new Variant(mod, this.x, this.y, this.z, this.uvlock, this.weight);
        // Now, do same for submodels : add them to list for variant
        if (this.submodel != null) {
            List<Variant> vlist = new ArrayList<Variant>();
            for (ForgeVariantV1List subv : this.submodel.values()) {
                // Only going to handle singletons for submodels....
                if (subv.variantList.size() > 0) {
                    Variant sv = subv.variantList.get(0).generateVanilla(models);
                    if (sv != null) {
                        vlist.add(sv);
                    }
                }
            }
            var.subvariants = new VariantList(vlist);
        }
        return var;
    }
}
