package org.opentrafficsim.road.gtu.lane.tactical.util;

import static org.opentrafficsim.base.parameters.ParameterTypeNumeric.NumericConstraint.POSITIVE;

import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.djunits.unit.AccelerationUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeAcceleration;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayTrafficLight;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

import nl.tudelft.simulation.language.Throw;

/**
 * Static methods regarding traffic lights for composition in tactical planners.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version May 13, 2016 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public final class TrafficLightUtil
{
    /** Maximum deceleration for stopping for yellow traffic light. */
    public static final ParameterTypeAcceleration B_YELLOW =
            new ParameterTypeAcceleration("bYellow", "Maximum deceleration for stopping for yellow traffic light.",
                    new Acceleration(3.5, AccelerationUnit.SI), POSITIVE);

    /**
     * Do not instantiate.
     */
    private TrafficLightUtil()
    {
        //
    }

    /**
     * Returns an acceleration as response to a set of traffic lights, being positive infinity if ignored. The response is
     * governed by the car-following model in case a traffic light is yellow or red. A constant deceleration to stop is also
     * calculated, and the highest acceleration of both is used. If this value is below -bYellow (B_YELLOW), the traffic light
     * is ignored, which usually occurs only during the yellow phase. By using the highest acceleration of the car-following
     * model and the constant deceleration, it is ensured that comfortable deceleration is applied if approaching a red traffic
     * light from far away, while strong deceleration is only applied if required and appropriately represents stopping for
     * yellow.
     * @param parameters parameters
     * @param headwayTrafficLights set of headway traffic lights
     * @param carFollowingModel car following model
     * @param speed speed
     * @param speedLimitInfo speed limit info
     * @return acceleration as response to a traffic light, being positive infinity if ignored
     * @throws ParameterException if a parameter is not defined
     * @throws NullPointerException if any input is null
     * @throws IllegalArgumentException if the traffic light is not downstream
     */
    public static Acceleration respondToTrafficLights(final Parameters parameters,
            final Set<HeadwayTrafficLight> headwayTrafficLights, final CarFollowingModel carFollowingModel, final Speed speed,
            final SpeedLimitInfo speedLimitInfo) throws ParameterException
    {
        Throw.whenNull(headwayTrafficLights, "Traffic light set may not be null.");
        Acceleration a = new Acceleration(Double.POSITIVE_INFINITY, AccelerationUnit.SI);
        for (HeadwayTrafficLight headwayTrafficLight : headwayTrafficLights)
        {
            Acceleration aLight = respondToTrafficLight(parameters, headwayTrafficLight, carFollowingModel,
                    speed, speedLimitInfo);
            a = Acceleration.min(a, aLight);
        }
        return a;
    }

    /**
     * Returns an acceleration as response to a traffic light, being positive infinity if ignored. The response is governed by
     * the car-following model in case the traffic light is yellow or red. A constant deceleration to stop is also calculated,
     * and the highest acceleration of both is used. If this value is below -bYellow (B_YELLOW), the traffic light is ignored,
     * which usually occurs only during the yellow phase. By using the highest acceleration of the car-following model and the
     * constant deceleration, it is ensured that comfortable deceleration is applied if approaching a red traffic light from far
     * away, while strong deceleration is only applied if required and appropriately represents stopping for yellow.
     * @param parameters parameters
     * @param headwayTrafficLight headway traffic light
     * @param carFollowingModel car following model
     * @param speed speed
     * @param speedLimitInfo speed limit info
     * @return acceleration as response to a traffic light, being positive infinity if ignored
     * @throws ParameterException if a parameter is not defined
     * @throws NullPointerException if any input is null
     * @throws IllegalArgumentException if the traffic light is not downstream
     */
    public static Acceleration respondToTrafficLight(final Parameters parameters,
            final HeadwayTrafficLight headwayTrafficLight, final CarFollowingModel carFollowingModel, final Speed speed,
            final SpeedLimitInfo speedLimitInfo) throws ParameterException
    {
        Throw.whenNull(parameters, "Parameters may not be null.");
        Throw.whenNull(headwayTrafficLight, "Traffic light may not be null.");
        Throw.whenNull(carFollowingModel, "Car-following model may not be null.");
        Throw.whenNull(speed, "Speed may not be null.");
        Throw.whenNull(speedLimitInfo, "Speed limit info may not be null.");
        Throw.when(!headwayTrafficLight.isAhead(), IllegalArgumentException.class, "Traffic light must be downstream.");
        if (headwayTrafficLight.getTrafficLightColor().isRed() || headwayTrafficLight.getTrafficLightColor().isYellow())
        {
            // deceleration from car-following model
            SortedMap<Length, Speed> leaders = new TreeMap<>();
            leaders.put(headwayTrafficLight.getDistance(), Speed.ZERO);
            Acceleration a = carFollowingModel.followingAcceleration(parameters, speed, speedLimitInfo, leaders);
            // compare to constant deceleration
            Length s0 = parameters.getParameter(ParameterTypes.S0);
            if (headwayTrafficLight.getDistance().gt(s0)) // constant acceleration not applicable if within s0
            {
                // constant acceleration is -.5*v^2/s, where s = distance-s0 > 0
                Acceleration aConstant = CarFollowingUtil.constantAccelerationStop(carFollowingModel, parameters,
                        speed, headwayTrafficLight.getDistance());
                a = Acceleration.max(a, aConstant);
            }
            // return a if a > -b
            if (a.gt(parameters.getParameter(B_YELLOW).neg()))
            {
                return a;
            }
        }
        // ignore traffic light
        return new Acceleration(Double.POSITIVE_INFINITY, AccelerationUnit.SI);
    }

}
