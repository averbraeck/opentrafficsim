package org.opentrafficsim.road.gtu.lane.perception.categories.neighbors;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.control.ControlTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedGtu;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedGtu.Maneuver;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedGtu.Signals;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedGtuSimple;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedObject.Kinematics;
import org.opentrafficsim.road.network.lane.object.LaneBasedObject;

/**
 * Default CACC sensors. This returns all information except desired speed for the first leader and CACC leaders. Remaining
 * leaders are provided null information.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class DefaultCaccSensors implements PerceivedGtuType
{

    /**
     * Constructor.
     */
    public DefaultCaccSensors()
    {
        //
    }

    /** {@inheritDoc} */
    @Override
    public PerceivedGtu createPerceivedGtu(final LaneBasedGtu perceivingGtu, final LaneBasedObject reference, final LaneBasedGtu perceivedGtu,
            final Length distance, final boolean downstream) throws GtuException, ParameterException
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
        return new PerceivedGtuSimple(id, gtuType, length, width,
                Kinematics.dynamicAhead(distance, v, a, true, length, perceivingGtu.getLength()), Signals.of(perceivedGtu),
                Maneuver.of(perceivedGtu));
    }

}
