package org.opentrafficsim.road.gtu.lane.perception.categories.neighbors;

import java.util.ArrayList;
import java.util.List;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.control.ControlTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.perception.headway.GtuStatus;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtu;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtuSimple;

/**
 * Default CACC sensors. This returns all information except desired speed for the first leader and CACC leaders. Remaining
 * leaders are provided null information.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class DefaultCaccSensors implements HeadwayGtuType
{

    /** {@inheritDoc} */
    @Override
    public HeadwayGtu createDownstreamGtu(final LaneBasedGtu perceivingGtu, final LaneBasedGtu perceivedGtu,
            final Length distance) throws GtuException, ParameterException
    {
        Time t;
        try
        {
            t = perceivingGtu.getSimulator().getSimulatorAbsTime()
                    .minus(((ControlTacticalPlanner) perceivingGtu.getTacticalPlanner()).getSettings()
                            .getParameter(LongitudinalControllerPerception.DELAY));
        }
        catch (ClassCastException exception)
        {
            if (!(perceivingGtu.getTacticalPlanner() instanceof ControlTacticalPlanner))
            {
                throw new GtuException("DefaultCaccSensors relies on the tactical planner being a ControlTacticalPlanner",
                        exception);
            }
            throw new GtuException(exception);
        }
        String id = perceivedGtu.getId();
        GtuType gtuType = perceivedGtu.getType();
        Length length = perceivedGtu.getLength();
        Length width = perceivedGtu.getWidth();
        Speed v = perceivedGtu.getSpeed(t);
        Acceleration a = perceivedGtu.getAcceleration(t);
        Speed desiredSpeed = null;
        List<GtuStatus> status = new ArrayList<>();
        if (perceivedGtu.isBrakingLightsOn(t))
        {
            status.add(GtuStatus.BRAKING_LIGHTS);
        }
        switch (perceivedGtu.getTurnIndicatorStatus(t))
        {
            case HAZARD:
                status.add(GtuStatus.EMERGENCY_LIGHTS);
                break;
            case LEFT:
                status.add(GtuStatus.LEFT_TURNINDICATOR);
                break;
            case RIGHT:
                status.add(GtuStatus.RIGHT_TURNINDICATOR);
                break;
            default:
                break;
        }
        return new HeadwayGtuSimple(id, gtuType, distance, length, width, v, a, desiredSpeed,
                status.toArray(new GtuStatus[status.size()]));
    }

    /** {@inheritDoc} */
    @Override
    public HeadwayGtu createUpstreamGtu(final LaneBasedGtu perceivingGtu, final LaneBasedGtu perceivedGtu,
            final Length distance) throws GtuException, ParameterException
    {
        throw new UnsupportedOperationException("Default CACC sensors can only determine leaders.");
    }

    /** {@inheritDoc} */
    @Override
    public HeadwayGtu createParallelGtu(final LaneBasedGtu perceivingGtu, final LaneBasedGtu perceivedGtu,
            final Length overlapFront, final Length overlap, final Length overlapRear) throws GtuException
    {
        throw new UnsupportedOperationException("Default CACC sensors can only determine leaders.");
    }

}
