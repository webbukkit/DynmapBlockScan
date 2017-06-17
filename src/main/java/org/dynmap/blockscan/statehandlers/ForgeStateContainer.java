package org.dynmap.blockscan.statehandlers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.ImmutableMap;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;

public class ForgeStateContainer extends StateContainer {

	public ForgeStateContainer(Block blk, Set<String> renderprops, Map<String, List<String>> propMap) {
		List<IBlockState> bsl = blk.getBlockState().getValidStates();
		IBlockState defstate = blk.getDefaultState();
		if (renderprops == null) {
			renderprops = new HashSet<String>();
			for (String pn : propMap.keySet()) {
				renderprops.add(pn);
			}
		}
		// Build table of render properties and valid values
		for (String pn : propMap.keySet()) {
			if (renderprops.contains(pn) == false) {
				continue;
			}
			this.renderProperties.put(pn, propMap.get(pn));
		}
		
		this.defStateIndex = 0;
		for (IBlockState bs : bsl) {
			int meta = blk.getMetaFromState(bs);	// Get meta for state
			ImmutableMap.Builder<String,String> bld = ImmutableMap.builder();
			for (Entry<IProperty<?>, Comparable<?>> ent : bs.getProperties().entrySet()) {
				String pn = ent.getKey().getName();
				if (renderprops.contains(pn)) {	// If valid render property
					Comparable<?> v = ent.getValue();
					if (v instanceof IStringSerializable) {
						v = ((IStringSerializable)v).getName();
					}
					bld.put(pn, v.toString());
				}
			}
			StateRec sr = new StateRec(meta, bld.build());
			int prev_sr = records.indexOf(sr);
			if (prev_sr < 0) {
				if (bs.equals(defstate)) {
					this.defStateIndex = records.size();
				}
				records.add(sr);
			}
			else {
				StateRec prev = records.get(prev_sr);
				if (prev.hasMeta(meta) == false) {
					sr = new StateRec(prev, meta);
					records.set(prev_sr, sr);
					if (bs.equals(defstate)) {
						this.defStateIndex = prev_sr;
					}
				}
			}
		}
	}
}
