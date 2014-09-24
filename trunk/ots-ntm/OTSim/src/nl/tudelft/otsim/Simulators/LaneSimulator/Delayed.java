package nl.tudelft.otsim.Simulators.LaneSimulator;

/**
 * Shell for a delayed object of any class defined by <tt>E</tt>. An internal
 * memory vector stores objects you put in. Note that for the delay to be
 * constant, the number of objects put in and requested should be 1 each time
 * step. Besides an object of type <tt>E</tt> also <tt>null</tt> can be put into 
 * the memory. This will also be returned as <tt>null</tt>. Note that this even
 * works for Double and Integer as primitive types cannot be used for generics. 
 * For example: <tt>jDelayed&#60;double&#62;</tt> won't work but 
 * <tt>jDelayed&#60;Double&#62;</tt> will.
 * @param <E> Class of the jDelayed objects.
 */
public class Delayed<E> {

    /** Memory vector */
    protected java.util.ArrayList<E> memory;

    /**
     * Constructor setting a fixed delay given a certain time step. The delay
     * will be an integer multiple of the time step.
     * @param delay Delay in the unit of <tt>dt</tt>.
     * @param dt Time step.
     */
    public Delayed(double delay, double dt) {
        // create memory vector
        int n = (int) Math.round(delay/dt);
        memory = new java.util.ArrayList<E>(n);
        // fill memory with null
        for (int i=0; i<n; i++)
            memory.add(null);
    }

    /**
     * Add an object to memory.
     * @param in Object to be put into memory.
     */
    public void put(E in) {
        // append element
        memory.add(in);
    }

    /**
     * Returns oldest object from memory and removes it from memory.
     * @return Oldest object from memory.
     */
    public E get() {
        // get and remove first element
        E out = memory.get(0);
        memory.remove(0);
        return out;
    }
}