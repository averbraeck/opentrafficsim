package org.opentrafficsim.road.gtu.lane.perception;

import java.util.function.Supplier;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;

/**
 * Iterable that additionally provides support for PerceptionCollectors. These gather raw data, to only 'perceive' the result.
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 1 mrt. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <H> headway type
 * @param <U> underlying object type
 */
public interface PerceptionCollectable<H extends Headway, U> extends PerceptionIterable<H>
{

    /**
     * Collect the underlying objects in to a perceived result. This methodology is loosely based on Stream.collect().
     * @param collector PerceptionCollector; collector
     * @param <C> collection result type
     * @param <I> intermediate type
     * @return C; collection result
     */
    default <C, I> C collect(final PerceptionCollector<C, ? super U, I> collector)
    {
        return collect(collector.getIdentity(), collector.getAccumulator(), collector.getFinalizer());
    }

    /**
     * Collect the underlying objects in to a perceived result. This methodology is loosely based on Stream.collect().
     * @param identity Supplier; the initial intermediate result value
     * @param accumulator PerceptionAccumulator; accumulator
     * @param finalizer PerceptionFinalizer; finalizer
     * @param <C> collection result type
     * @param <I> intermediate type
     * @return C; collection result
     */
    <C, I> C collect(Supplier<I> identity, PerceptionAccumulator<? super U, I> accumulator,
            PerceptionFinalizer<C, I> finalizer);

    /**
     * Combination of an accumulator and a finalizer.
     * <p>
     * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 28 feb. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     * @param <C> collection result type
     * @param <U> underlying object type
     * @param <I> intermediate result type
     */
    public interface PerceptionCollector<C, U, I>
    {
        /**
         * Returns the identity value, the initial intermediate value.
         * @return I; identity value, the initial intermediate value
         */
        Supplier<I> getIdentity();

        /**
         * Returns the accumulator.
         * @return PerceptionAccumulator; accumulator
         */
        PerceptionAccumulator<U, I> getAccumulator();

        /**
         * Returns the finalizer.
         * @return PerceptionFinalizer; finalizer
         */
        PerceptionFinalizer<C, I> getFinalizer();
    }

    /**
     * Accumulates an object one at a time in to an accumulating intermediate result.
     * <p>
     * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 28 feb. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     * @param <U> underlying object type
     * @param <I> intermediate result type
     */
    public interface PerceptionAccumulator<U, I>
    {
        /**
         * Accumulate the next object to intermediate result.
         * @param intermediate I; intermediate result before accumulation of object
         * @param object U; next object to include
         * @param distance Length; distance to the considered object
         * @return I; intermediate result after accumulation of object
         */
        Intermediate<I> accumulate(Intermediate<I> intermediate, U object, Length distance);
    }

    /**
     * Translates the last intermediate result of an accumulator in to the collection output.
     * <p>
     * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 28 feb. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     * @param <C> collection result type
     * @param <I> intermediate result type
     */
    public interface PerceptionFinalizer<C, I>
    {
        /**
         * Translate the last intermediate result in to a final result.
         * @param intermediate I; last intermediate result
         * @return C; final result
         */
        C collect(I intermediate);
    }

    /**
     * Wrapper of intermediate result with info for the iterator algorithm.
     * <p>
     * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 3 apr. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>]
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
         * @param object I; identity value
         */
        public Intermediate(final I object)
        {
            this.object = object;
        }

        /**
         * Get intermediate object.
         * @return I; intermediate object
         */
        public I getObject()
        {
            return this.object;
        }
        
        /**
         * Set intermediate object.
         * @param object I; intermediate object
         */
        public void setObject(final I object)
        {
            this.object = object;
        }

        /**
         * Returns the number of the underlying object currently being accumulated, starts at 0 for the first.
         * @return int; number of the underlying object currently being accumulated
         */
        public int getNumber()
        {
            return this.number;
        }

        /**
         * Method for the iterator the increase the underlying object number.
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
         * @return boolean; whether the iterator can stop
         */
        public boolean isStop()
        {
            return this.stop;
        }
    }

}
