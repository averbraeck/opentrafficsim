package org.opentrafficsim.road.gtu.lane.perception;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.djutils.exceptions.Throw;
import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;

/**
 * Wraps a {@code PerceptionCollectable} and only iterates over all objects that are accepted by a predicate.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 * @param <H> headway type
 * @param <U> underlying object type
 */
public class PerceptionCollectableFiltered<H extends Headway, U> implements PerceptionCollectable<H, U>
{

    /** Iterator of headway objects. */
    private final Iterator<H> iteratorH;

    /** Iterator of underlying objects. */
    private final Iterator<U> iteratorU;

    /** Filter predicate. */
    private final Predicate<H> predicate;

    /** First entry. */
    private Entry<H, U> first;

    /** Last entry (so far). */
    private Entry<H, U> last;

    /**
     * Constructor.
     * @param collectable PerceptionCollectable&lt;H, U&gt;; collectable to filter.
     * @param predicate Predicate&lt;H&gt;; predicate, should return {@code true} for items that remain in the collectable.
     */
    public PerceptionCollectableFiltered(final PerceptionCollectable<H, U> collectable, final Predicate<H> predicate)
    {
        this.iteratorH = collectable.iterator();
        this.iteratorU = collectable.underlying();
        this.predicate = predicate;
    }

    /** {@inheritDoc} */
    @Override
    public H first()
    {
        if (this.first == null)
        {
            if (!prepareNext())
            {
                return null;
            }
        }
        return this.first.h;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEmpty()
    {
        return this.first != null || prepareNext();
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<H> iterator()
    {
        return new FilterIterator<>((entry) -> entry.h);
    }

    /** {@inheritDoc} */
    @Override
    public <C, I> C collect(final Supplier<I> identity, final PerceptionAccumulator<? super U, I> accumulator,
            final PerceptionFinalizer<C, I> finalizer)
    {
        Intermediate<I> i = new Intermediate<>(identity.get());
        Iterator<UnderlyingDistance<U>> iterator = underlyingWithDistance();
        while (iterator.hasNext())
        {
            UnderlyingDistance<U> u = iterator.next();
            i = accumulator.accumulate(i, u.getObject(), u.getDistance());
            if (i.isStop())
            {
                break;
            }
        }
        return finalizer.collect(i.getObject());
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<U> underlying()
    {
        return new FilterIterator<>((entry) -> entry.u);
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<UnderlyingDistance<U>> underlyingWithDistance()
    {
        return new FilterIterator<>((entry) -> new UnderlyingDistance<>(entry.u, entry.h.getDistance()));
    }

    /**
     * Prepares a next entry with headway and underlying object.
     * @return boolean; whether a next entry was found.
     */
    private boolean prepareNext()
    {
        if (this.iteratorH.hasNext())
        {
            H h = this.iteratorH.next();
            U u = this.iteratorU.next();
            if (!this.predicate.test(h))
            {
                return prepareNext();
            }
            Entry<H, U> entry = new Entry<>(h, u);
            if (this.first == null)
            {
                this.first = entry;
                this.last = entry;
            }
            else
            {
                this.last.next = entry;
                this.last = entry;
            }
            return true;
        }
        return false;
    }

    /**
     * Entry to hold a headway and underlying object, and a reference to possible next entry (i.e. forming a linked list).
     * <p>
     * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     * @param <H> headway type
     * @param <U> underlying object type
     */
    private static class Entry<H, U>
    {
        /** Headway object. */
        private final H h;

        /** Underlying object. */
        private final U u;

        /** Next entry, if any. */
        private Entry<H, U> next;

        /**
         * Constructor;
         * @param h H; headway object.
         * @param u U; underlying object.
         */
        public Entry(final H h, final U u)
        {
            this.h = h;
            this.u = u;
        }
    }

    /**
     * Iterator for headway objects, underlying objects, and underlying with distance objects. This class will use
     * {@code prepareNext()} in the encompassing class to find a next entry when necessary.
     * <p>
     * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     * @param <R> return type
     */
    private class FilterIterator<R> implements Iterator<R>
    {
        /** Converter. */
        private final Function<Entry<H, U>, R> converter;

        /** Last entry. */
        private Entry<H, U> last = null;

        /** Next entry. */
        private Entry<H, U> next = null;

        /**
         * Constructor.
         * @param converter Function&lt;Entry&lt;H, U&gt;, R&gt;; converter to return type.
         */
        public FilterIterator(final Function<Entry<H, U>, R> converter)
        {
            this.converter = converter;
        }

        /** {@inheritDoc} */
        @Override
        public boolean hasNext()
        {
            if (this.next == null && this.last == null)
            {
                if (PerceptionCollectableFiltered.this.first == null)
                {
                    prepareNext();
                }
                this.next = PerceptionCollectableFiltered.this.first;
            }
            else if (this.last != null)
            {
                if (this.last.next == null)
                {
                    prepareNext();
                }
                this.next = this.last.next;
            }
            return this.next != null;
        }

        /** {@inheritDoc} */
        @Override
        public R next()
        {
            Throw.when(!hasNext(), NoSuchElementException.class, "Iterator has no more elements.");
            this.last = this.next;
            this.next = this.last.next;
            return this.converter.apply(this.last);
        }
    }

}
