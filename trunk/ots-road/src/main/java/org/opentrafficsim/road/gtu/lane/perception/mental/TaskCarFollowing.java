package org.opentrafficsim.road.gtu.lane.perception.mental;

import java.util.function.Supplier;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.Try;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable.Intermediate;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable.PerceptionAccumulator;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable.PerceptionCollector;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable.PerceptionFinalizer;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.mental.Fuller.Task;

/**
 * Task demand for car-following.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 3 apr. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class TaskCarFollowing implements Task
{

    /** {@inheritDoc} */
    @Override
    public double demand(final LanePerception perception, final LaneBasedGTU gtu, final Parameters parameters)
            throws ParameterException
    {
        NeighborsPerception neighbors = Try.assign(() -> perception.getPerceptionCategory(NeighborsPerception.class),
                "NeighborsPerception not available.");
        Try.execute(() -> neighbors.updateLeaders(RelativeLane.CURRENT), "Exception while updating leaders.");
        return neighbors.getLeaders(RelativeLane.CURRENT).collect(new TaskCarFollowingCollector(gtu, parameters));
    }

    /**
     * Simple collector implementation to determine car-following task demand.
     * <p>
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 3 apr. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    public static class TaskCarFollowingCollector implements PerceptionCollector<Double, LaneBasedGTU, Double>
    {

        /** GTU. */
        final LaneBasedGTU gtu;

        /** Parameters. */
        final Parameters parameters;

        /**
         * Constructor.
         * @param gtu LaneBasedGTU; gtu
         * @param parameters Parameters; parameters
         */
        public TaskCarFollowingCollector(final LaneBasedGTU gtu, final Parameters parameters)
        {
            this.gtu = gtu;
            this.parameters = parameters;
        }

        /** {@inheritDoc} */
        @Override
        public Supplier<Double> getIdentity()
        {
            return new Supplier<Double>()
            {
                @Override
                public Double get()
                {
                    return 0.0; // if no leader
                }
            };
        }

        /** {@inheritDoc} */
        @Override
        public PerceptionAccumulator<LaneBasedGTU, Double> getAccumulator()
        {
            return new PerceptionAccumulator<LaneBasedGTU, Double>()
            {
                @Override
                public Intermediate<Double> accumulate(final Intermediate<Double> intermediate, final LaneBasedGTU object,
                        final Length distance)
                {
                    double a = TaskCarFollowingCollector.this.gtu.getAcceleration().si;
                    Parameters params = TaskCarFollowingCollector.this.parameters;
                    double b = -params.getParameterOrNull(ParameterTypes.B).si;
                    double Tmin = params.getParameterOrNull(ParameterTypes.TMIN).si;
                    double hMin = a < b ? (1.0 + a / -8.0) * Tmin : Tmin;
                    double h = distance.si / TaskCarFollowingCollector.this.gtu.getSpeed().si;
                    intermediate.setObject(h <= hMin ? 1.0 : (h > 3.0 ? 0.5 : 1.0 - (1.0 - 0.5) * (h - hMin) / (3.0 - hMin)));
                    intermediate.stop(); // need only 1 leader
                    return intermediate;
                }
            };
        }

        /** {@inheritDoc} */
        @Override
        public PerceptionFinalizer<Double, Double> getFinalizer()
        {
            return new PerceptionFinalizer<Double, Double>()
            {
                @Override
                public Double collect(final Double intermediate)
                {
                    return intermediate;
                }
            };
        }

    }

}
