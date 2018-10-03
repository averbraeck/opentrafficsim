package org.opentrafficsim.road.gtu.lane.perception.categories;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.categories.Anticipation.NeighborTriplet;
import org.opentrafficsim.road.gtu.lane.perception.mental.AdaptationSituationalAwareness;

/**
 * Estimation of neighbor headway, speed and acceleration.
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 6 apr. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface Estimation
{
    /** No estimation errors. */
    Estimation NONE = new Estimation()
    {
        @Override
        public NeighborTriplet estimate(final LaneBasedGTU perceivingGtu, final LaneBasedGTU perceivedGtu,
                final Length distance, final boolean downstream, final Time when) throws ParameterException
        {
            return new NeighborTriplet(distance, perceivedGtu.getSpeed(), perceivedGtu.getAcceleration());
        }
    };

    /** Underestimation based on situational awareness. */
    Estimation UNDERESTIMATION = new Estimation()
    {
        @Override
        public NeighborTriplet estimate(final LaneBasedGTU perceivingGtu, final LaneBasedGTU perceivedGtu,
                final Length distance, final boolean downstream, final Time when) throws ParameterException
        {
            double factor = 1.0 - (perceivingGtu.getParameters().getParameter(AdaptationSituationalAwareness.SA_MAX)
                    - perceivingGtu.getParameters().getParameter(AdaptationSituationalAwareness.SA));
            double delta = (perceivedGtu.getOdometer().si - perceivedGtu.getOdometer(when).si)
                    - (perceivingGtu.getOdometer().si - perceivingGtu.getOdometer(when).si);
            if (!downstream)
            {
                delta = -delta; // faster leader increases the headway, faster follower reduces the headway
            }
            Length headway = Length.createSI((distance.si + delta) * factor);
            double egoSpeed = perceivingGtu.getSpeed(when).si;
            Speed speed = Speed.createSI(egoSpeed + factor * (perceivedGtu.getSpeed(when).si - egoSpeed));
            Acceleration acceleration = perceivedGtu.getAcceleration(when);
            return new NeighborTriplet(headway, speed, acceleration);
        }
    };

    /**
     * Estimate headway, speed and acceleration.
     * @param perceivingGtu LaneBasedGTU; perceiving GTU
     * @param perceivedGtu LaneBasedGTU; perceived GTU
     * @param distance Length; actual headway at 'now' (i.e. not at 'when' if there is a reaction time)
     * @param downstream boolean; downstream (or upstream) neighbor
     * @param when Time; moment of perception, reaction time included
     * @return NeighborTriplet; perceived headway, speed and acceleration
     * @throws ParameterException on invalid parameter value or if parameter is not available
     */
    NeighborTriplet estimate(LaneBasedGTU perceivingGtu, LaneBasedGTU perceivedGtu, Length distance, boolean downstream,
            Time when) throws ParameterException;
}
