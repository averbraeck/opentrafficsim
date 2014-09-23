package nl.tudelft.otsim.Utilities;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Traverse a List in reverse order.
 * <br />
 * Derived from <a href="http://stackoverflow.com/questions/1098117/can-one-do-a-for-each-loop-in-java-in-reverse-order">Can
 * one do a for each loop in java in reverse order? - Stack Overflow</a>.
 *
 * @param <T>; List to traverse in reverse order
 */
@SuppressWarnings("javadoc")
public class Reversed<T> implements Iterable<T> {
    private final List<T> original;

    public Reversed(List<T> original) {
        this.original = original;
    }

    /**
     * Implement Iterable<T>
     */
    @Override
	public Iterator<T> iterator() {
        final ListIterator<T> i = original.listIterator(original.size());

        return new Iterator<T>() {
            @Override
			public boolean hasNext() { return i.hasPrevious(); }
            
            @Override
			public T next() { return i.previous(); }
            
            @Override
			public void remove() { i.remove(); }
        };
    }

    /**
     * Create an Iterator to traverse a List in reverse order.
     * @param original List<T>; List to traverse
     * @return Iterator; to traverse the List in reverse order
     */
    public static <T> Reversed<T> reversed(List<T> original) {
        return new Reversed<T>(original);
    }    
    
}