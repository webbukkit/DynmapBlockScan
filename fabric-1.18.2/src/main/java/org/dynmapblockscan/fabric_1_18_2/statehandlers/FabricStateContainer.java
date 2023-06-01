package org.dynmapblockscan.fabric_1_18_2.statehandlers;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.*;
import net.minecraft.state.property.Property;
import net.minecraft.util.StringIdentifiable;
import org.dynmapblockscan.core.statehandlers.StateContainer;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FabricStateContainer extends StateContainer {

	public FabricStateContainer(Block blk, Set<String> renderprops, Map<String, List<String>> propMap) {
		List<BlockState> bsl = blk.getStateManager().getStates();
		BlockState defstate = blk.getDefaultState();
		if (renderprops == null) {
			renderprops = new HashSet<String>();
			for (String pn : propMap.keySet()) {
				renderprops.add(pn);
			}
		}
		// Build table of render properties and valid values
		for (String pn : propMap.keySet()) {
			if (!renderprops.contains(pn)) {
				continue;
			}
			this.renderProperties.put(pn, propMap.get(pn));
		}

		this.defStateIndex = 0;
		int idx = 0;
		for (BlockState bs : bsl) {
			ImmutableMap.Builder<String,String> bld = ImmutableMap.builder();
			for (Property<?> ent : bs.getProperties()) {
				String pn = ent.getName();
				if (renderprops.contains(pn)) {	// If valid render property
					Comparable<?> v = bs.get(ent);
					if (v instanceof StringIdentifiable) {
						v = ((StringIdentifiable)v).asString();
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
				if (!prev.hasMeta(idx)) {
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
		else if (blk instanceof CropBlock) {
            type = WellKnownBlockClasses.CROPS;
		}
		else if (blk instanceof FlowerBlock) {
		    type = WellKnownBlockClasses.FLOWER;
		}
		else if (blk instanceof TallPlantBlock) {
            type = WellKnownBlockClasses.TALLGRASS;
		}
		else if (blk instanceof VineBlock) {
		    type = WellKnownBlockClasses.VINES;
		}
        else if (blk instanceof PlantBlock) {
            type = WellKnownBlockClasses.BUSH;
        }
        else if (blk instanceof GrassBlock) {
            type = WellKnownBlockClasses.GRASS;
        }
        else if (blk instanceof LightBlock) {
            type = WellKnownBlockClasses.LIQUID;
        }
	}
}
