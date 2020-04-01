package org.opentrafficsim.core.dsol;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.TreeSet;

import nl.tudelft.simulation.dsol.eventlists.EventListInterface;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEventInterface;
import nl.tudelft.simulation.dsol.simtime.SimTime;

/**
 * A SynchronizedRedBlackTree implementation of the eventlistInterface. This implementation is based on Java's TreeSet.
 * <p>
 * (c) copyright 2002-2005 <a href="http://www.simulation.tudelft.nl">Delft University of Technology </a>, the Netherlands. <br>
 * See for project information <a href="http://www.simulation.tudelft.nl"> www.simulation.tudelft.nl </a> <br>
 * License of use: <a href="http://www.gnu.org/copyleft/lesser.html">Lesser General Public License (LGPL) </a>, no warranty.
 * @author <a href="https://www.linkedin.com/in/peterhmjacobs">Peter Jacobs </a>
 * @version $Revision: 1.2 $ $Date: 2010/08/10 11:36:45 $
 * @param <T> the type of simulation time, e.g. SimTimeCalendarLong or SimTimeDouble or SimTimeDoubleUnit.
 * @since 1.5
 */
public class SynchronizedRedBlackTree<T extends SimTime<?, ?, T>> implements EventListInterface<T>, Serializable
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

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public SimEventInterface<T> removeFirst()
    {
        synchronized (this.tree)
        {
            SimEventInterface<T> first = this.first();
            this.remove(first);
            return first;
        }
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public SimEventInterface<T> removeLast()
    {
        synchronized (this.tree)
        {
            SimEventInterface<T> last = this.last();
            this.remove(last);
            return last;
        }
    }

    /**
     * we re-implemented the first method. Instead of throwing exceptions if the tree is empty, we return a null value.
     * @see java.util.TreeSet#first()
     * @return the first SimEvent in the tree.
     */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
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

    /**
     * we re-implemented the last method. Instead of throwing exceptions if the tree is empty, we return a null value.
     * @see java.util.TreeSet#last()
     * @return the last SimEvent in the tree.
     */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public SimEventInterface<T> last()
    {
        synchronized (this.tree)
        {
            try
            {
                return this.tree.last();
            }
            catch (NoSuchElementException noSuchElementException)
            {
                return null;
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public Comparator<? super SimEventInterface<T>> comparator()
    {
        synchronized (this.tree)
        {
            return this.tree.comparator();
        }
    }

    /** {@inheritDoc} */
    @Override
    public SortedSet<SimEventInterface<T>> subSet(final SimEventInterface<T> fromElement, final SimEventInterface<T> toElement)
    {
        synchronized (this.tree)
        {
            return this.tree.subSet(fromElement, toElement);
        }
    }

    /** {@inheritDoc} */
    @Override
    public SortedSet<SimEventInterface<T>> headSet(final SimEventInterface<T> toElement)
    {
        synchronized (this.tree)
        {
            return this.tree.headSet(toElement);
        }
    }

    /** {@inheritDoc} */
    @Override
    public SortedSet<SimEventInterface<T>> tailSet(final SimEventInterface<T> fromElement)
    {
        synchronized (this.tree)
        {
            return this.tree.tailSet(fromElement);
        }
    }

    /** {@inheritDoc} */
    @Override
    public int size()
    {
        synchronized (this.tree)
        {
            return this.tree.size();
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEmpty()
    {
        synchronized (this.tree)
        {
            return this.tree.isEmpty();
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean contains(final Object o)
    {
        synchronized (this.tree)
        {
            return this.tree.contains(o);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<SimEventInterface<T>> iterator()
    {
        synchronized (this.tree)
        {
            return this.tree.iterator();
        }
    }

    /** {@inheritDoc} */
    @Override
    public Object[] toArray()
    {
        synchronized (this.tree)
        {
            return this.tree.toArray();
        }
    }

    /** {@inheritDoc} */
    @Override
    public <X> X[] toArray(final X[] a)
    {
        synchronized (this.tree)
        {
            return this.tree.toArray(a);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean add(final SimEventInterface<T> e)
    {
        synchronized (this.tree)
        {
            return this.tree.add(e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean remove(final Object o)
    {
        synchronized (this.tree)
        {
            return this.tree.remove(o);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean containsAll(final Collection<?> c)
    {
        synchronized (this.tree)
        {
            return this.tree.containsAll(c);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean addAll(final Collection<? extends SimEventInterface<T>> c)
    {
        synchronized (this.tree)
        {
            return this.tree.addAll(c);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean retainAll(final Collection<?> c)
    {
        synchronized (this.tree)
        {
            return this.tree.retainAll(c);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean removeAll(final Collection<?> c)
    {
        synchronized (this.tree)
        {
            return this.tree.removeAll(c);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void clear()
    {
        this.tree.clear();
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "SynchronizedRedBlackTree [" + this.tree.size() + " events]";
    }

}
