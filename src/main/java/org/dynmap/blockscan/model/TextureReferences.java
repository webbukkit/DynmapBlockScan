package org.dynmap.blockscan.model;

public interface TextureReferences {
	/**
	 * Find texture by ID
	 * 
	 * @param txtid - texture reference value (possibly with # leading)
	 * @return resolved and normalized texture name, or null if not found
	 */
	public String findTextureByID(String txtid);

}
