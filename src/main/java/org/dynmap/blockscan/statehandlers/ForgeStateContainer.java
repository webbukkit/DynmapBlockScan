package org.dynmap.blockscan.statehandlers;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.ImmutableMap;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BushBlock;
import net.minecraft.block.CropsBlock;
import net.minecraft.block.FlowerBlock;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.GrassBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.TallGrassBlock;
import net.minecraft.block.VineBlock;
import net.minecraft.util.IStringSerializable;

public class ForgeStateContainer extends StateContainer {

	public ForgeStateContainer(Block blk, Set<String> renderprops, Map<String, List<String>> propMap) {
		List<BlockState> bsl = blk.getStateContainer().getValidStates();
		BlockState defstate = blk.getDefaultState();
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
		int idx = 0;
		for (BlockState bs : bsl) {
			ImmutableMap.Builder<String,String> bld = ImmutableMap.builder();
			// func_235904_r_ == getProperties
			for (net.minecraft.state.Property<?> ent : bs.func_235904_r_()) {
				String pn = ent.getName();
				if (renderprops.contains(pn)) {	// If valid render property
					Comparable<?> v = bs.get(ent);
					if (v instanceof IStringSerializable) {
						v = ((IStringSerializable)v).getName();
					}
					bld.put(pn, v.toString());
				}
			}
			StateRec sr = new StateRec(idx, bld.build());
			int prev_sr = records.indexOf(sr);
			if (prev_sr < 0) {
				if (bs.equals(defstate)) {
					this.defStateIndex = records.size();
				}
				records.add(sr);
			}
			else {
				StateRec prev = records.get(prev_sr);
				if (prev.hasMeta(idx) == false) {
					sr = new StateRec(prev, idx);
					records.set(prev_sr, sr);
					if (bs.equals(defstate)) {
						this.defStateIndex = prev_sr;
					}
				}
			}
			idx++;
		}
		// Check for well-known block types
		if (blk instanceof LeavesBlock) {
		    type = WellKnownBlockClasses.LEAVES;
		}
		else if (blk instanceof CropsBlock) {
            type = WellKnownBlockClasses.CROPS;
		}
		else if (blk instanceof FlowerBlock) {
		    type = WellKnownBlockClasses.FLOWER;
		}
		else if (blk instanceof TallGrassBlock) {
            type = WellKnownBlockClasses.TALLGRASS;
		}
		else if (blk instanceof VineBlock) {
		    type = WellKnownBlockClasses.VINES;
		}
        else if (blk instanceof BushBlock) {
            type = WellKnownBlockClasses.BUSH;
        }
        else if (blk instanceof GrassBlock) {
            type = WellKnownBlockClasses.GRASS;
        }
        else if (blk instanceof FlowingFluidBlock) {
            type = WellKnownBlockClasses.LIQUID;
        }
	}
}
