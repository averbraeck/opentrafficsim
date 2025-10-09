package org.opentrafficsim.road.gtu.lane.perception.categories;

import java.util.function.Function;
import java.util.function.Supplier;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.LinearDensity;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable.Intermediate;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable.PerceptionAccumulator;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable.PerceptionCollector;
import org.opentrafficsim.road.gtu.lane.perception.categories.AnticipationDensity.CountAndDistance;

/**
 * Collector to determine density based on GTUs.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class AnticipationDensity implements PerceptionCollector<LinearDensity, Gtu, CountAndDistance>
{

    /**
     * Constructor.
     */
    public AnticipationDensity()
    {
        //
    }

    @Override
    public Supplier<CountAndDistance> getIdentity()
    {
        return new Supplier<CountAndDistance>()
        {
            @Override
            public CountAndDistance get()
            {
                return new CountAndDistance();
            }
        };
    }

    @Override
    public PerceptionAccumulator<Gtu, CountAndDistance> getAccumulator()
    {
        return new PerceptionAccumulator<Gtu, CountAndDistance>()
        {
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

    @Override
    public Function<CountAndDistance, LinearDensity> getFinalizer()
    {
        return new Function<CountAndDistance, LinearDensity>()
        {
            @Override
            public LinearDensity apply(final CountAndDistance intermediate)
            {
                return LinearDensity.ofSI(intermediate.getDistance().si / intermediate.getCount());
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
     * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    public static class CountAndDistance
    {

        /** Count. */
        private int count;

        /** Distance. */
        private Length distance;

        /**
         * Constructor.
         */
        public CountAndDistance()
        {
            //
        }

        /**
         * Returns count.
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
         * Returns distance.
         * @return distance.
         */
        public Length getDistance()
        {
            return this.distance;
        }

        /**
         * Sets distance.
         * @param distance set distance.
         */
        public void setDistance(final Length distance)
        {
            this.distance = distance;
        }
    }

}
