package cis501;

public interface ITraceAnalyzer {

    /** @return the name of the author of this assignment. */
    public String author();

    /**
     * Run over the trace of insns and compute various summary data. The summary data can be queried
     * via the other methods in this interface.
     */
    public void run(Iterable<Insn> ui);

    /**
     * @return the average size of the insns in the trace, in bytes.
     */
    public double avgInsnSize();

    /**
     * Returns the increase in required insn bandwidth (insn bytes read) that would occur if all
     * 2-byte Thumb insns were 4-byte ARM insns instead. E.g., if all insns were Thumb insns, the
     * increase would double the bandwidth so 2.0 should be returned. If no insns were Thumb insns,
     * the increase would be 1x (i.e., no increase) so 1.0 should be returned.
     *
     * @return a value in the range [1,2]
     */
    public double insnBandwidthIncreaseWithoutThumb();

    /**
     * Returns the fraction of all direct branches whose offsets can be encoded with <= the given
     * number of bits. E.g., with 5 bits, return the fraction of all direct branches whose offsets
     * can be encoded with 5 bits or fewer.
     *
     * @param bits a value in the range [1,32]
     * @return a fraction in the range [0,1.0]
     */
    public double fractionOfDirectBranchOffsetsLteNBits(int bits);

    /**
     * Returns the most common category of insns found in the trace, as one of the strings "load",
     * "store", "unconditionalbranch", "conditionalbranch" or "other".
     */
    public String mostCommonInsnCategory();
}
