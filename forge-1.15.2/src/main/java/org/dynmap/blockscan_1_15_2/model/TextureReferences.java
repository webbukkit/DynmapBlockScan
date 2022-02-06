package org.dynmap.blockscan_1_15_2.model;

public interface TextureReferences {
	/**
	 * Find texture by ID
	 * 
	 * @param txtid - texture reference value (possibly with # leading)
	 * @return resolved and normalized texture name, or null if not found
	 */
	public String findTextureByID(String txtid);

}
