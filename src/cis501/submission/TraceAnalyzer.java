package cis501.submission;

import cis501.ITraceAnalyzer;
import cis501.Insn;

public class TraceAnalyzer implements ITraceAnalyzer {

    @Override
    public String author() {
        return "<your name here>";
    }

    @Override
    public void run(Iterable<Insn> iiter) {
        for (Insn insn : iiter) {
            // TODO: your code here
        }
    }

    @Override
    public double avgInsnSize() {
        return 0.0;
    }

    @Override
    public double insnBandwidthIncreaseWithoutThumb() {
        return 1.0;
    }

    @Override
    public String mostCommonInsnCategory() {
        return null;
    }

    @Override
    public double fractionOfDirectBranchOffsetsLteNBits(int bits) {
        return 0;
    }

}
