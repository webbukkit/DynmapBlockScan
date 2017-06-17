package org.dynmap.blockscan.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraft.client.renderer.block.model.BlockPart;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.util.ResourceLocation;

// Top level container class for JSON parsed Block Model data
public class BlockModel {
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
			g = gb.create();
			GSON = g;
		}
		return g;
	}
}
