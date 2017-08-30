package cis501.submission;

import cis501.ITraceAnalyzer;
import cis501.InsnIterator;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TraceAnalyzerSampleTest {

    private static ITraceAnalyzer subm = new TraceAnalyzer();

    @BeforeClass
    public static void setUpClass() throws Exception {
        // Run the trace before any tests are run. Then, test the results of the run.

        // TODO: replace the name of trace file here
        InsnIterator uiter = new InsnIterator("path/to/trace-file", -1);
        subm.run(uiter);
    }

    /** Simple do-nothing test to verify that the test suite is being run. */
    @Test
    public void testNop() {
        assertTrue(true);
    }

    /** The trace's actual average insn size, so you can check your implementation. */
    @Test
    public void testAvgInsnSize() {
        assertEquals(2.70, subm.avgInsnSize(), 0.01);
    }

    // add more tests here!

}
