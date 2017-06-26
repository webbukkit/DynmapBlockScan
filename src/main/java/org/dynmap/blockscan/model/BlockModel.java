package org.dynmap.blockscan.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dynmap.blockscan.blockstate.BlockState;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.client.renderer.block.model.BlockPart;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.util.ResourceLocation;

// Top level container class for JSON parsed Block Model data
public class BlockModel implements TextureReferences {
	// Parser for BlockState
	private static Gson GSON;
	// Elements of model
	public final List<BlockElement> elements = Collections.emptyList();
	// Ambient occlusion
    public final boolean ambientOcclusion = true;
    // Texture aliases
    public final Map<String, String> textures = Collections.emptyMap();
    // Parent model ID
    public final String parent = null;
    // Resolved reference to loaded parent model
    public BlockModel parentModel = null;
    
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
		String basetxtid = txtid;
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
				else {	// Elxe, move to parent
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
