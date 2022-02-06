package org.dynmap.blockscan_1_14_4.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

// Top level container class for JSON parsed Block Model data
public class BlockModel implements TextureReferences {
	// Parser for BlockState
	private static Gson GSON;
	// Elements of model
	public final List<BlockElement> elements = Collections.emptyList();
	// Ambient occlusion
    public final boolean ambientOcclusion = true;
    // Texture aliases
    public final Map<String, String> textures;
    // Parent model ID
    public final String parent;
    // Resolved reference to loaded parent model
    public BlockModel parentModel = null;
    
    public BlockModel() {
        this.parent = null;
        this.textures = Collections.emptyMap();
    }
    public BlockModel(String par, Map<String, String> txt) {
        this.textures = new HashMap<String, String>();
        if (txt != null) {
            for (Entry<String, String> ent : txt.entrySet()) {
                this.textures.put(ent.getKey(), ent.getValue());
            }
        }
        if (par != null) {
            String[] tok = par.split(":");
            if (tok.length == 1) {
                par = "minecraft:block/" + tok[0];
            }
            else {
                par = tok[0] + ":block/" + tok[1];
            }
            this.parent = par;
        }
        else {
            this.parent = null;
        }
    }
    @Override
	public String toString() {
		StringBuilder sb = new StringBuilder("{");
		if (parent != null) {
			sb.append(" parent=").append(parent);
		}
		if (ambientOcclusion) {
			sb.append(" ambientOcclusion=true");
		}
		if (textures.isEmpty() == false) {
			sb.append(" textures=").append(textures);
		}
		if (elements.isEmpty() == false) {
			sb.append(" elements=").append(elements);
		}
		sb.append("}");
		return sb.toString();
	}
	
	// Build Gson parser for BlockState
	public static Gson buildParser() {
		Gson g = GSON;
		if (g == null) {
			GsonBuilder gb = new GsonBuilder();	// Start with builder
			gb.registerTypeAdapter(BlockElement.class, new BlockElement.Deserializer());
			g = gb.create();
			GSON = g;
		}
		return g;
	}
	
	/**
	 * Find texture by ID
	 * 
	 * @param txtid - texture reference value (possibly with # leading)
	 * @return resolved and normalized texture name, or null if not found
	 */
	@Override
	public String findTextureByID(String txtid) {
		while(txtid.startsWith("#")) {
			BlockModel mod = this;
			boolean match = false;
			txtid = txtid.substring(1);
			while (!match) {
				// If txtid key in textures definition
				if (mod.textures.containsKey(txtid)) {
					match = true;
					txtid = mod.textures.get(txtid);	// Match : get value
				}
				else {	// Else, move to parent
					mod = mod.parentModel;
					// Not found, we're done
					if (mod == null) {
						return null;
					}
				}
			}
		}
		// We have resolved value: make sure its normalized texture reference
		if (txtid.indexOf(':') < 0) {
			txtid = "minecraft:" + txtid;
		}
		return txtid;
	}
}
