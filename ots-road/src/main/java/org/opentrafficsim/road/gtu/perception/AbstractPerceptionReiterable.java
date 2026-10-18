package org.opentrafficsim.road.gtu.perception;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Try;
import org.opentrafficsim.base.DistancedObject;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.road.gtu.perception.object.PerceivedObject;
import org.opentrafficsim.road.network.object.LaneBasedObject;

/**
 * This class uses a single primary iterator which a subclass defines, and makes sure that all elements are only looked up and
 * created once. It does so by storing the elements in a list. All calls to {@code iterator()} return an iterator which iterates
 * over the list. If an iterator runs to the end of the list, the primary iterator is requested to add an element if it has one.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Alexander Verbraeck
 * @author Peter Knoppers
 * @author Wouter Schakel
 * @param <O> perceiving object type (an {@code O} is perceiving a {@code U} as a {@code P})
 * @param <P> perceived object type (an {@code O} is perceiving a {@code U} as a {@code P})
 * @param <U> underlying object type (an {@code O} is perceiving a {@code U} as a {@code P})
 */
public abstract class AbstractPerceptionReiterable<O extends LaneBasedObject, P extends PerceivedObject, U>
        implements PerceptionCollectable<P, U>
{

    /** List of entries that wrap an object at a distance and car provide a perceived version of the object. */
    private final List<Entry> entries = new ArrayList<>();

    /** Primary iterator. */
    private Iterator<DistancedObject<U>> primaryIterator;

    /** Perceiving object. */
    private final O perceivingObject;

    /**
     * Constructor.
     * @param perceivingObject perceiving object.
     */
    protected AbstractPerceptionReiterable(final O perceivingObject)
    {
        this.perceivingObject = perceivingObject;
    }

    /**
     * Returns the perceiving object.
     * @return perceiving object.
     */
    public O getObject()
    {
        return this.perceivingObject;
    }

    /**
     * Returns the primary iterator.
     * @return primary iterator
     */
    final Iterator<DistancedObject<U>> getPrimaryIterator()
    {
        if (this.primaryIterator == null)
        {
            this.primaryIterator = primaryIterator();
        }
        return this.primaryIterator;
    }

    /**
     * Returns the primary iterator. This method is called once by AbstractPerceptionReiterable.
     * @return primary iterator
     */
    protected abstract Iterator<DistancedObject<U>> primaryIterator();

    /**
     * Returns a perceived version of the underlying object.
     * @param object underlying object
     * @param distance distance to the object
     * @return perceived version of the underlying object
     * @throws ParameterException on invalid parameter value or missing parameter
     */
    protected abstract P perceive(U object, Length distance) throws ParameterException;

    @Override
    public synchronized P first()
    {
        if (!assureIndex(0))
        {
            throw new NoSuchElementException();
        }
        return this.entries.get(0).getPerceived();
    }

    @Override
    public boolean isEmpty()
    {
        return !assureIndex(0);
    }

    @Override
    public Iterator<P> iterator()
    {
        return new PerceptionIterator<>((i) -> AbstractPerceptionReiterable.this.entries.get(i).getPerceived());
    }

    @Override
    public <C, I> C collect(final Supplier<I> identity, final PerceptionAccumulator<? super U, I> accumulator,
            final Function<I, C> finalizer)
    {
        Intermediate<I> intermediate = new Intermediate<>(identity.get());
        int index = 0;
        while (assureIndex(index) && !intermediate.isStop())
        {
            Entry next = this.entries.get(index++);
            intermediate =
                    accumulator.accumulate(intermediate, next.underlyingDistance.object(), next.underlyingDistance.distance());
            intermediate.step();
        }
        return finalizer.apply(intermediate.getObject());
    }

    @Override
    public Iterator<U> underlying()
    {
        return new PerceptionIterator<>((i) -> AbstractPerceptionReiterable.this.entries.get(i).underlyingDistance.object());
    }

    @Override
    public Iterator<DistancedObject<U>> underlyingWithDistance()
    {
        return new PerceptionIterator<>((i) -> AbstractPerceptionReiterable.this.entries.get(i).underlyingDistance);
    }

    /**
     * Helper method that assures that a next entry is available, if the primary iterator has a next value. This method may be
     * used by any process that derives from the primary iterator.
     * @param index index of next entry
     * @return next entry
     */
    private synchronized boolean assureIndex(final int index)
    {
        while (this.entries.size() <= index && getPrimaryIterator().hasNext())
        {
            this.entries.add(new Entry(getPrimaryIterator().next()));
        }
        return index < this.entries.size();
    }

    /**
     * Iterator that uses an index while assuring elements are in the wrapped array if the primary iterator has more elements.
     * @param <T> element type
     */
    private final class PerceptionIterator<T> implements Iterator<T>
    {

        /** Index. */
        private int index;

        /** Function. */
        private final IntFunction<T> function;

        /**
         * Constructor.
         * @param function function
         */
        private PerceptionIterator(final IntFunction<T> function)
        {
            this.function = function;
        }

        @Override
        public boolean hasNext()
        {
            return assureIndex(this.index);
        }

        @Override
        public T next()
        {
            if (!assureIndex(this.index))
            {
                throw new NoSuchElementException();
            }
            return this.function.apply(this.index++);
        }

    }

    /**
     * Entry of a perceived element.
     */
    private class Entry
    {

        /** Actual object at a distance. */
        private final DistancedObject<U> underlyingDistance;

        /** Perceived object. */
        private P value;

        /**
         * Constructor.
         * @param underlyingDistance actual object at a distance
         */
        Entry(final DistancedObject<U> underlyingDistance)
        {
            this.underlyingDistance = underlyingDistance;
        }

        /**
         * Returns the perceived version of the object.
         * @return the perceived version of the object
         */
        synchronized P getPerceived()
        {
            if (this.value == null)
            {
                this.value = Try.assign(() -> perceive(this.underlyingDistance.object(), this.underlyingDistance.distance()),
                        "Parameter exception during perception of object.");
            }
            return this.value;
        }

    }

}
