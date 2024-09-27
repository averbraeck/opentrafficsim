package org.opentrafficsim.road.gtu.lane.perception;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Supplier;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;

/**
 * Iterable that additionally provides support for PerceptionCollectors. These gather raw data, to only 'perceive' the result.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <H> headway type
 * @param <U> underlying object type
 */
public interface PerceptionCollectable<H extends Headway, U> extends PerceptionIterable<H>
{

    /**
     * Collect the underlying objects in to a perceived result. This methodology is loosely based on Stream.collect().
     * @param collector collector
     * @param <C> collection result type
     * @param <I> intermediate type
     * @return collection result
     */
    default <C, I> C collect(final PerceptionCollector<C, ? super U, I> collector)
    {
        return collect(collector.getIdentity(), collector.getAccumulator(), collector.getFinalizer());
    }

    /**
     * Collect the underlying objects in to a perceived result. This methodology is loosely based on Stream.collect().
     * @param identity the initial intermediate result value
     * @param accumulator accumulator
     * @param finalizer finalizer
     * @param <C> collection result type
     * @param <I> intermediate type
     * @return collection result
     */
    <C, I> C collect(Supplier<I> identity, PerceptionAccumulator<? super U, I> accumulator, Function<I, C> finalizer);

    /**
     * Returns an iterator over the underlying objects.
     * @return iterator
     */
    Iterator<U> underlying();

    /**
     * Returns an iterator over the underlying objects coupled with the distance.
     * @return iterator
     */
    Iterator<UnderlyingDistance<U>> underlyingWithDistance();

    /**
     * Combination of an accumulator and a finalizer.
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     * @param <C> collection result type
     * @param <U> underlying object type
     * @param <I> intermediate result type
     */
    interface PerceptionCollector<C, U, I>
    {
        /**
         * Returns the identity value, the initial intermediate value.
         * @return identity value, the initial intermediate value
         */
        Supplier<I> getIdentity();

        /**
         * Returns the accumulator.
         * @return accumulator
         */
        PerceptionAccumulator<U, I> getAccumulator();

        /**
         * Returns the finalizer.
         * @return finalizer
         */
        Function<I, C> getFinalizer();
    }

    /**
     * Accumulates an object one at a time in to an accumulating intermediate result.
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     * @param <U> underlying object type
     * @param <I> intermediate result type
     */
    interface PerceptionAccumulator<U, I>
    {
        /**
         * Accumulate the next object to intermediate result.
         * @param intermediate intermediate result before accumulation of object
         * @param object next object to include
         * @param distance distance to the considered object
         * @return intermediate result after accumulation of object
         */
        Intermediate<I> accumulate(Intermediate<I> intermediate, U object, Length distance);
    }

    /**
     * Wrapper of intermediate result with info for the iterator algorithm.
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>]
     * @param <I> intermediate result type
     */
    class Intermediate<I>
    {
        /** Number of underlying object being iterated. */
        private int number = 0;

        /** Intermediate object. */
        private I object;

        /** Whether to stop accumulating. */
        private boolean stop = false;

        /**
         * Constructor.
         * @param object identity value
         */
        public Intermediate(final I object)
        {
            this.object = object;
        }

        /**
         * Get intermediate object.
         * @return intermediate object
         */
        public I getObject()
        {
            return this.object;
        }

        /**
         * Set intermediate object.
         * @param object intermediate object
         */
        public void setObject(final I object)
        {
            this.object = object;
        }

        /**
         * Returns the number of the underlying object currently being accumulated, starts at 0 for the first.
         * @return number of the underlying object currently being accumulated
         */
        public int getNumber()
        {
            return this.number;
        }

        /**
         * Method for the iterator to increase the underlying object number.
         */
        public void step()
        {
            this.number++;
        }

        /**
         * Method for the accumulator to indicate the iterator can stop.
         */
        public void stop()
        {
            this.stop = true;
        }

        /**
         * Method for the iterator to check if it can stop.
         * @return whether the iterator can stop
         */
        public boolean isStop()
        {
            return this.stop;
        }
    }

    /**
     * Wrapper for object and its distance.
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     * @param <U> underlying object type
     */
    class UnderlyingDistance<U> implements Comparable<UnderlyingDistance<?>>
    {
        /** Object. */
        private final U object;

        /** Distance. */
        private final Length distance;

        /**
         * @param object object
         * @param distance distance
         */
        public UnderlyingDistance(final U object, final Length distance)
        {
            this.object = object;
            this.distance = distance;
        }

        /**
         * @return object.
         */
        public U getObject()
        {
            return this.object;
        }

        /**
         * @return distance.
         */
        public Length getDistance()
        {
            return this.distance;
        }

        /** {@inheritDoc} */
        @Override
        public int compareTo(final UnderlyingDistance<?> o)
        {
            return this.distance.compareTo(o.distance);
        }
    }

}
