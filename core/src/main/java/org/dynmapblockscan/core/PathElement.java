package org.dynmapblockscan.core;

import java.util.Arrays;

public class PathElement {
    public String[] modids;

    public PathElement(String mid) {
        modids = new String[]{ mid };
    }

    public void addModId(String mid) {
        modids = Arrays.copyOf(modids, modids.length + 1);
        modids[modids.length - 1] = mid;
    }
}
