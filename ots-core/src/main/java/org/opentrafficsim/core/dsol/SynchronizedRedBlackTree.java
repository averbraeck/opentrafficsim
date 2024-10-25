package org.opentrafficsim.core.dsol;

import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.TreeSet;

import nl.tudelft.simulation.dsol.eventlists.EventListInterface;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEventInterface;

/**
 * A SynchronizedRedBlackTree implementation of the eventlistInterface. This implementation is based on Java's TreeSet.
 * <p>
 * (c) copyright 2002-2005 <a href="https://www.simulation.tudelft.nl">Delft University of Technology </a>, the Netherlands.
 * <br>
 * See for project information <a href="https://www.simulation.tudelft.nl"> www.simulation.tudelft.nl </a> <br>
 * License of use: <a href="http://www.gnu.org/copyleft/lesser.html">Lesser General Public License (LGPL) </a>, no warranty.
 * @author <a href="https://www.linkedin.com/in/peterhmjacobs">Peter Jacobs </a>
 * @param <T> the type of simulation time, e.g. SimTimeCalendarLong or SimTimeDouble or SimTimeDoubleUnit.
 * @since 1.5
 */
public class SynchronizedRedBlackTree<T extends Number & Comparable<T>> implements EventListInterface<T>, Serializable
{
    /** The default serial version UID for serializable classes. */
    private static final long serialVersionUID = 1L;

    /** wrapped treeset. */
    private TreeSet<SimEventInterface<T>> tree = new TreeSet<>();

    /**
     * Constructs a new <code>SynchronizedRedBlackTree</code>.
     */
    public SynchronizedRedBlackTree()
    {
    }

    @Override
    public SimEventInterface<T> removeFirst()
    {
        synchronized (this.tree)
        {
            SimEventInterface<T> first = this.first();
            this.remove(first);
            return first;
        }
    }

    /**
     * we re-implemented the first method. Instead of throwing exceptions if the tree is empty, we return a null value.
     * @see java.util.TreeSet#first()
     * @return the first SimEvent in the tree.
     */
    @Override
    public SimEventInterface<T> first()
    {
        synchronized (this.tree)
        {
            try
            {
                return this.tree.first();
            }
            catch (NoSuchElementException noSuchElementException)
            {
                return null;
            }
        }
    }

    @Override
    public int size()
    {
        synchronized (this.tree)
        {
            return this.tree.size();
        }
    }

    @Override
    public boolean isEmpty()
    {
        synchronized (this.tree)
        {
            return this.tree.isEmpty();
        }
    }

    @Override
    public boolean contains(final SimEventInterface<T> o)
    {
        synchronized (this.tree)
        {
            return this.tree.contains(o);
        }
    }

    @Override
    public Iterator<SimEventInterface<T>> iterator()
    {
        synchronized (this.tree)
        {
            return this.tree.iterator();
        }
    }

    @Override
    public void add(final SimEventInterface<T> e)
    {
        synchronized (this.tree)
        {
            this.tree.add(e);
        }
    }

    @Override
    public boolean remove(final SimEventInterface<T> o)
    {
        synchronized (this.tree)
        {
            return this.tree.remove(o);
        }
    }

    @Override
    public void clear()
    {
        this.tree.clear();
    }

    @Override
    public String toString()
    {
        return "SynchronizedRedBlackTree [" + this.tree.size() + " events]";
    }

}
