package org.dynmap.blockscan;

import java.util.Collections;
import java.util.Map;

// Container for JSON-encoded block state overrides (to handle bogus Mojang block state file name options
public class BlockStateOverrides {
	public Map<String, Map<String, BlockStateOverride>> overrides = Collections.emptyMap();
	
	public static class BlockStateOverride {
		public String baseNameProperty = null;	// If property used for base name, this is property name
		public String nameSuffix = "";			// If base name has suffix
		public String blockStateName = null;	// If defined, this is name of block state resource
	}
	
	
	public BlockStateOverride getOverride(String modid, String blkid) {
		Map<String, BlockStateOverride> map = overrides.get(modid);
		if (map != null) {
			return map.get(blkid);
		}
		return null;
	}
}
