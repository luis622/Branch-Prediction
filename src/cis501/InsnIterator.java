package cis501;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Stack;
import java.util.zip.GZIPInputStream;

public class InsnIterator implements Iterator<Insn>, Iterable<Insn> {

    private final BufferedReader reader;
    private final int LIMIT;
    private final Stack<Insn> pbBuffer = new Stack<>();
    private int insnsProcessed = 0;

    /**
     * @param filename The path to the compressed trace file
     * @param limit    Stop after processing this many insns. If -1, process the entire trace.
     */
    public InsnIterator(String filename, int limit) {
        if (-1 == limit) {
            LIMIT = Integer.MAX_VALUE; // no limit
        } else {
            LIMIT = limit;
        }

        InputStreamReader isr = null;
        try {
            isr = new InputStreamReader(new GZIPInputStream(new FileInputStream(filename)), "US-ASCII");
        } catch (IOException e) {
            e.printStackTrace();
        }
        reader = new BufferedReader(isr);
    }

    public boolean hasNext() {
        if (Thread.interrupted()) {
            throw new IllegalStateException("Interrupted!");
        }
        try {
            return (!pbBuffer.isEmpty()) || (insnsProcessed < LIMIT && reader.ready());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Insn next() {
        if (Thread.interrupted()) {
            throw new IllegalStateException("Interrupted!");
        }
        try {
            if (!pbBuffer.isEmpty()) {
                return pbBuffer.pop();
            }
            String ln = reader.readLine();
            insnsProcessed++;
            return new Insn(ln);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Push the given insn i back into the iterator. If the following method call is next(), then
     * next() will return i. Insns that are put back will be returned via next() in LIFO order.
     */
    public void putBack(Insn i) {
        pbBuffer.push(i);
    }

    public Iterator<Insn> iterator() {
        return this;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

}
