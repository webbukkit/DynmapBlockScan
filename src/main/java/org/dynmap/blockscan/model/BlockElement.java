package org.dynmap.blockscan.model;

import java.util.Collections;
import java.util.Map;

// Container for parsed JSON elements from block model
public class BlockElement {
    public float[] from;
    public float[] to;
    public ElementRotation rotation = null;
    public Map<String, BlockFace> faces = Collections.emptyMap();
    public boolean shade = true;
}
