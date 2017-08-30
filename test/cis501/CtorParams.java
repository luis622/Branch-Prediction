package cis501;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Helper class for easily creating a Collection<Object[]>, used with JUnit's
 * Parameterized.Parameters test parameterization annotation
 */
public class CtorParams implements Collection<Object[]> {

    private Set<Object[]> args = new HashSet<>();

    /** Bundle args as an Object[] and add it to the collection of argument arrays */
    public CtorParams(Object... os) {
        this.args.add(os);
    }

    /** Bundle args as an Object[] and add it to the collection of argument arrays */
    public CtorParams p(Object... os) {
        this.args.add(os);
        return this;
    }

    @Override
    public int size() {
        return args.size();
    }

    @Override
    public boolean isEmpty() {
        return args.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return args.contains(o);
    }

    @Override
    public Iterator<Object[]> iterator() {
        return args.iterator();
    }

    @Override
    public Object[] toArray() {
        return args.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return args.toArray(a);
    }

    @Override
    public boolean add(Object[] e) {
        return args.add(e);
    }

    @Override
    public boolean remove(Object o) {
        return args.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return args.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends Object[]> c) {
        return args.addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return args.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return args.retainAll(c);
    }

    @Override
    public void clear() {
        args.clear();
    }
}
