package org.opentrafficsim.road.gtu.lane.perception.categories;

import java.util.function.Supplier;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.LinearDensity;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable.Intermediate;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable.PerceptionAccumulator;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable.PerceptionCollector;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable.PerceptionFinalizer;
import org.opentrafficsim.road.gtu.lane.perception.categories.AnticipationDensity.CountAndDistance;

/**
 * Collector to determine density based on GTUs.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class AnticipationDensity implements PerceptionCollector<LinearDensity, Gtu, CountAndDistance>
{

    /** {@inheritDoc} */
    @Override
    public Supplier<CountAndDistance> getIdentity()
    {
        return new Supplier<CountAndDistance>()
        {
            /** {@inheritDoc} */
            @Override
            public CountAndDistance get()
            {
                return new CountAndDistance();
            }
        };
    }

    /** {@inheritDoc} */
    @Override
    public PerceptionAccumulator<Gtu, CountAndDistance> getAccumulator()
    {
        return new PerceptionAccumulator<Gtu, CountAndDistance>()
        {
            /** {@inheritDoc} */
            @Override
            public Intermediate<CountAndDistance> accumulate(final Intermediate<CountAndDistance> intermediate,
                    final Gtu object, final Length distance)
            {
                intermediate.getObject().increaseCount();
                intermediate.getObject().setDistance(distance);
                return intermediate;
            }
        };
    }

    /** {@inheritDoc} */
    @Override
    public PerceptionFinalizer<LinearDensity, CountAndDistance> getFinalizer()
    {
        return new PerceptionFinalizer<LinearDensity, CountAndDistance>()
        {
            /** {@inheritDoc} */
            @Override
            public LinearDensity collect(final CountAndDistance intermediate)
            {
                return LinearDensity.instantiateSI(intermediate.getDistance().si / intermediate.getCount());
            }
        };
    }

    /**
     * Intermediate data to determine density.
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    public static class CountAndDistance
    {

        /** Count. */
        private int count;

        /** Distance. */
        private Length distance;

        /**
         * @return count.
         */
        public int getCount()
        {
            return this.count;
        }

        /**
         * Increases the GTU count by 1.
         */
        public void increaseCount()
        {
            this.count++;
        }

        /**
         * @return distance.
         */
        public Length getDistance()
        {
            return this.distance;
        }

        /**
         * @param distance Length; set distance.
         */
        public void setDistance(final Length distance)
        {
            this.distance = distance;
        }
    }

}
