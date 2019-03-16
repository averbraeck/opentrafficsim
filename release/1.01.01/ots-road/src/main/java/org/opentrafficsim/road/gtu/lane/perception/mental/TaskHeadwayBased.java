package org.opentrafficsim.road.gtu.lane.perception.mental;

import java.util.function.Supplier;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.exceptions.Try;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.perception.EgoPerception;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable.Intermediate;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable.PerceptionAccumulator;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable.PerceptionCollector;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable.PerceptionFinalizer;

/**
 * Task class that translates a (composite) headway in to a task demand.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 25 jun. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public abstract class TaskHeadwayBased extends AbstractTask
{

    /**
     * Constructor.
     * @param id String; id
     */
    public TaskHeadwayBased(final String id)
    {
        super(id);
    }

    /** Current speed. */
    private Speed speed;

    /** {@inheritDoc} */
    @Override
    public double calculateTaskDemand(final LanePerception perception, final LaneBasedGTU gtu, final Parameters parameters)
            throws ParameterException
    {
        double a = gtu.getAcceleration().si;
        double b = parameters.getParameter(ParameterTypes.B).si;
        double tMin = parameters.getParameter(ParameterTypes.TMIN).si;
        double hMin = a < -b ? (1.0 - (a + b) / (8.0 - b)) * tMin : tMin;
        EgoPerception<?, ?> ego = perception.getPerceptionCategoryOrNull(EgoPerception.class);
        Try.execute(() -> ego.updateSpeed(), "Could not update perception of ego speed.");
        this.speed = ego.getSpeed();
        Duration h = getHeadway(perception, gtu, parameters);
        if (h == null)
        {
            return 0.0; // no task demand
        }
        return h.si <= hMin ? 1.0 : (h.si > 3.0 ? 0.5 : 1.0 - (1.0 - 0.5) * (h.si - hMin) / (3.0 - hMin));
    }

    /**
     * Returns the current speed to translate a distance headway to a time headway.
     * @return Speed; speed
     */
    protected Speed getSpeed()
    {
        return this.speed;
    }

    /**
     * Returns a collector for the task demand.
     * @param perception LanePerception; perception
     * @param gtu LaneBasedGTU; gtu
     * @param parameters Parameters; parameters
     * @return Duration; headway, {@code null} of none.
     * @throws ParameterException on invalid parameter
     */
    protected abstract Duration getHeadway(LanePerception perception, LaneBasedGTU gtu, Parameters parameters)
            throws ParameterException;

    /**
     * Simple collector implementation to obtain time headway.
     * <p>
     * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 3 apr. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    public static class TaskHeadwayCollector implements PerceptionCollector<Duration, LaneBasedGTU, Duration>
    {

        /** Own speed. */
        private final Speed speed;

        /**
         * Constructor.
         * @param speed Speed; speed
         */
        public TaskHeadwayCollector(final Speed speed)
        {
            this.speed = speed;
        }

        /** {@inheritDoc} */
        @Override
        public Supplier<Duration> getIdentity()
        {
            return new Supplier<Duration>()
            {
                @Override
                public Duration get()
                {
                    return null; // if no leader
                }
            };
        }

        /** {@inheritDoc} */
        @Override
        public PerceptionAccumulator<LaneBasedGTU, Duration> getAccumulator()
        {
            return new PerceptionAccumulator<LaneBasedGTU, Duration>()
            {
                @SuppressWarnings("synthetic-access")
                @Override
                public Intermediate<Duration> accumulate(final Intermediate<Duration> intermediate, final LaneBasedGTU object,
                        final Length distance)
                {
                    intermediate.setObject(distance.divideBy(TaskHeadwayCollector.this.speed));
                    intermediate.stop(); // need only 1 leader
                    return intermediate;
                }
            };
        }

        /** {@inheritDoc} */
        @Override
        public PerceptionFinalizer<Duration, Duration> getFinalizer()
        {
            return new PerceptionFinalizer<Duration, Duration>()
            {
                @Override
                public Duration collect(final Duration intermediate)
                {
                    return intermediate == null ? intermediate : (intermediate.gt0() ? intermediate : Duration.ZERO);
                }
            };
        }

    }

}
