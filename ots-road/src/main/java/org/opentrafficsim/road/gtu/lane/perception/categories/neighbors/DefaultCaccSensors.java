package org.opentrafficsim.road.gtu.lane.perception.categories.neighbors;

import java.util.ArrayList;
import java.util.List;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.control.ControlTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.perception.headway.GTUStatus;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTU;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTUSimple;

/**
 * Default CACC sensors. This returns all information except desired speed for the first leader and CACC leaders. Remaining
 * leaders are provided null information.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class DefaultCaccSensors implements HeadwayGtuType
{

    /** {@inheritDoc} */
    @Override
    public HeadwayGTU createDownstreamGtu(final LaneBasedGTU perceivingGtu, final LaneBasedGTU perceivedGtu,
            final Length distance) throws GTUException, ParameterException
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
                throw new GTUException("DefaultCaccSensors relies on the tactical planner being a ControlTacticalPlanner",
                        exception);
            }
            throw new GTUException(exception);
        }
        String id = perceivedGtu.getId();
        GTUType gtuType = perceivedGtu.getGTUType();
        Length length = perceivedGtu.getLength();
        Length width = perceivedGtu.getWidth();
        Speed v = perceivedGtu.getSpeed(t);
        Acceleration a = perceivedGtu.getAcceleration(t);
        Speed desiredSpeed = null;
        List<GTUStatus> status = new ArrayList<>();
        if (perceivedGtu.isBrakingLightsOn(t))
        {
            status.add(GTUStatus.BRAKING_LIGHTS);
        }
        switch (perceivedGtu.getTurnIndicatorStatus(t))
        {
            case HAZARD:
                status.add(GTUStatus.EMERGENCY_LIGHTS);
                break;
            case LEFT:
                status.add(GTUStatus.LEFT_TURNINDICATOR);
                break;
            case RIGHT:
                status.add(GTUStatus.RIGHT_TURNINDICATOR);
                break;
            default:
                break;
        }
        return new HeadwayGTUSimple(id, gtuType, distance, length, width, v, a, desiredSpeed,
                status.toArray(new GTUStatus[status.size()]));
    }

    /** {@inheritDoc} */
    @Override
    public HeadwayGTU createUpstreamGtu(final LaneBasedGTU perceivingGtu, final LaneBasedGTU perceivedGtu,
            final Length distance) throws GTUException, ParameterException
    {
        throw new UnsupportedOperationException("Default CACC sensors can only determine leaders.");
    }

    /** {@inheritDoc} */
    @Override
    public HeadwayGTU createParallelGtu(final LaneBasedGTU perceivingGtu, final LaneBasedGTU perceivedGtu,
            final Length overlapFront, final Length overlap, final Length overlapRear) throws GTUException
    {
        throw new UnsupportedOperationException("Default CACC sensors can only determine leaders.");
    }

}
