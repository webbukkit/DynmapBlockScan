package org.dynmapblockscan.core;

public interface BlockScanLog {
    public void debug(String s);
    public void info(String s);
    public void severe(Throwable t);
    public void severe(String s);
    public void severe(String s, Throwable t);
    public void verboseinfo(String s);
    public void warning(String s);
    public void warning(String s, Throwable t);
}


