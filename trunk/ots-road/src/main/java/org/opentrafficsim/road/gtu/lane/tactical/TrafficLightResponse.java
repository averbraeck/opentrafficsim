package org.opentrafficsim.road.gtu.lane.tactical;

import static org.opentrafficsim.core.gtu.behavioralcharacteristics.AbstractParameterType.Check.POSITIVE;

import java.util.SortedMap;
import java.util.TreeMap;

import org.djunits.unit.AccelerationUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.Throw;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypeAcceleration;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypes;
import org.opentrafficsim.road.gtu.lane.object.TrafficLight.TrafficLightColor;
import org.opentrafficsim.road.gtu.lane.perception.HeadwayTrafficLight;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version May 13, 2016 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public final class TrafficLightResponse
{
    /** Maximum deceleration for stopping for yellow traffic light. */
    public static final ParameterTypeAcceleration B_YELLOW = new ParameterTypeAcceleration("bYellow",
        "Maximum deceleration for stopping for yellow traffic light.", new Acceleration(3.5, AccelerationUnit.SI), POSITIVE);

    /**
     * Do not instantiate.
     */
    private TrafficLightResponse()
    {
        //
    }

    /**
     * Returns an acceleration as response to a traffic light, being positive infinity if ignored. The response is governed by
     * the car-following model in case the traffic light is yellow or red. A constant deceleration to stop is also calculated,
     * and the highest acceleration of both is used. If this value is below -bYellow (B_YELLOW), the traffic light is ignored,
     * which usually occurs only during the yellow phase. By using the highest acceleration of the car-following model and the
     * constant deceleration, it is ensured that comfortable deceleration is applied if approaching a red traffic light from far
     * away, while strong deceleration is only applied if required and appropriately represents stopping for yellow.
     * @param behavioralCharacteristics behavioral characteristics
     * @param headwayTrafficLight headway traffic light
     * @param carFollowingModel car following model
     * @param speed speed
     * @param speedLimitInfo speed limit info
     * @return acceleration as response to a traffic light, being positive infinity if ignored
     * @throws ParameterException if a parameter is not defined
     * @throws NullPointerException if any input is null
     * @throws IllegalArgumentException if the traffic light is not downstream
     */
    public static Acceleration respondToTrafficLight(final BehavioralCharacteristics behavioralCharacteristics,
        final HeadwayTrafficLight headwayTrafficLight, final CarFollowingModel carFollowingModel, final Speed speed,
        final SpeedLimitInfo speedLimitInfo) throws ParameterException
    {
        Throw.whenNull(behavioralCharacteristics, "Behavioral characteristics may not be null.");
        Throw.whenNull(headwayTrafficLight, "Traffic light may not be null.");
        Throw.whenNull(carFollowingModel, "Car-following model may not be null.");
        Throw.whenNull(speed, "Speed may not be null.");
        Throw.whenNull(speedLimitInfo, "Speed limit info may not be null.");
        Throw.when(!headwayTrafficLight.isAhead(), IllegalArgumentException.class, "Traffic light must be downstream.");
        if (headwayTrafficLight.getTrafficLight().getTrafficLightColor() == TrafficLightColor.RED
            || headwayTrafficLight.getTrafficLight().getTrafficLightColor() == TrafficLightColor.YELLOW)
        {
            // deceleration from car-following model
            SortedMap<Length, Speed> leaders = new TreeMap<>();
            leaders.put(headwayTrafficLight.getDistance(), Speed.ZERO);
            Acceleration a =
                carFollowingModel.followingAcceleration(behavioralCharacteristics, speed, speedLimitInfo, leaders);
            // compare to constant deceleration
            Length s0 = behavioralCharacteristics.getParameter(ParameterTypes.S0);
            if (headwayTrafficLight.getDistance().lt(s0)) // constant acceleration not applicable if within s0
            {
                // constant acceleration is -.5*v^2/s, where s = distance-s0 > 0
                Acceleration aConstant =
                    new Acceleration(-0.5 * speed.si * speed.si / (headwayTrafficLight.getDistance().si - s0.si),
                        AccelerationUnit.SI);
                a = a.gt(aConstant) ? a : aConstant;
            }
            // return a if a > -b
            if (a.gt(behavioralCharacteristics.getParameter(B_YELLOW).multiplyBy(-1.0)))
            {
                return a;
            }
        }
        // ignore traffic light
        return new Acceleration(Double.POSITIVE_INFINITY, AccelerationUnit.SI);
    }
}
