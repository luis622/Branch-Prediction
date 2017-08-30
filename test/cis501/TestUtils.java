package cis501;

import cis501.Insn;

import java.util.List;

/** Grab-bag class of utility functions used in various tests. */
public class TestUtils {

    /** Return a String representation of a List<Insn> */
    public static String i2s(List<Insn> insns) {
        String s = "\n[insns = \n";
        for (Insn i : insns) {
            s += "  " + i.toString() + "\n";
        }
        s += "]";
        return s;
    }

}
