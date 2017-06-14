package org.dynmap.blockscan.statehandlers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.ImmutableMap;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;

public class ForgeStateContainer extends StateContainer {

	public ForgeStateContainer(Block blk, BlockStateContainer bsc, Set<String> renderprops) {
		List<IBlockState> bsl = bsc.getValidStates();
		IBlockState defstate = blk.getDefaultState();
		if (renderprops == null) {
			renderprops = new HashSet<String>();
			for (IProperty<?> p : bsc.getProperties()) {
				renderprops.add(p.getName());
			}
		}
		// Build table of render properties and valid values
		for (IProperty<?> p : bsc.getProperties()) {
			String pn = p.getName();
			if (renderprops.contains(pn) == false) {
				continue;
			}
			ArrayList<String> pvals = new ArrayList<String>();
			for (Comparable<?> val : p.getAllowedValues()) {
				pvals.add(val.toString());
			}
			this.renderProperties.put(pn, pvals);
		}
		
		this.defStateIndex = 0;
		for (IBlockState bs : bsl) {
			int meta = blk.getMetaFromState(bs);	// Get meta for state
			ImmutableMap.Builder<String,String> bld = ImmutableMap.builder();
			for (Entry<IProperty<?>, Comparable<?>> ent : bs.getProperties().entrySet()) {
				String pn = ent.getKey().getName();
				if (renderprops.contains(pn)) {	// If valid render property
					bld.put(pn, ent.getValue().toString());
				}
			}
			StateRec sr = new StateRec(meta, bld.build());
			if (records.contains(sr) == false) {
				if (bs.equals(defstate)) {
					this.defStateIndex = records.size();
				}
				records.add(sr);
			}
		}
	}
}
