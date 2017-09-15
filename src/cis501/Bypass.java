package cis501;

import java.util.EnumSet;

/** The various kinds of bypassing supported by the pipeline */
public enum Bypass {
    MX, WX, WM;

    /** No bypasses enabled */
    public final static EnumSet<Bypass> NO_BYPASS = EnumSet.noneOf(Bypass.class);

    /** Full bypassing enabled, i.e., MX, WX and WM */
    public final static EnumSet<Bypass> FULL_BYPASS = EnumSet.allOf(Bypass.class);
}
