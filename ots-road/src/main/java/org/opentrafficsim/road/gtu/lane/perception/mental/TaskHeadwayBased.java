package org.opentrafficsim.road.gtu.lane.perception.mental;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.perception.EgoPerception;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;

/**
 * Task class that translates a (composite) headway in to a task demand.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public abstract class TaskHeadwayBased extends AbstractTask
{

    /**
     * Constructor.
     * @param id id
     */
    public TaskHeadwayBased(final String id)
    {
        super(id);
    }

    /** Current speed. */
    private Speed speed;

    /** {@inheritDoc} */
    @Override
    public double calculateTaskDemand(final LanePerception perception, final LaneBasedGtu gtu, final Parameters parameters)
            throws ParameterException
    {
        double a = gtu.getAcceleration().si;
        double b = parameters.getParameter(ParameterTypes.B).si;
        double tMin = parameters.getParameter(ParameterTypes.TMIN).si;
        double hMin = a < -b ? (1.0 - (a + b) / (8.0 - b)) * tMin : tMin;
        EgoPerception<?, ?> ego = perception.getPerceptionCategoryOrNull(EgoPerception.class);
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
     * @return speed
     */
    protected Speed getSpeed()
    {
        return this.speed;
    }

    /**
     * Returns a collector for the task demand.
     * @param perception perception
     * @param gtu gtu
     * @param parameters parameters
     * @return headway, {@code null} of none.
     * @throws ParameterException on invalid parameter
     */
    protected abstract Duration getHeadway(LanePerception perception, LaneBasedGtu gtu, Parameters parameters)
            throws ParameterException;

}
