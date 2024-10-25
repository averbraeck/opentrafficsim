package org.opentrafficsim.road.gtu.lane.perception.headway;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLight;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLightColor;

/**
 * Container for a reference to information about a (lane based) traffic light and a headway to the traffic light.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public class HeadwayTrafficLightReal extends AbstractHeadwayLaneBasedObject implements HeadwayTrafficLight
{
    /** */
    private static final long serialVersionUID = 20160410L;

    /** the traffic light object for further observation, can not be null. */
    private final TrafficLight trafficLight;

    /** Whether we can turn on red. */
    private final boolean turnOnRed;

    /**
     * Construct a new Headway information object, for a traffic light ahead of us (or behind us, although that does not seem
     * very useful).
     * @param trafficLight the traffic light object for further observation, can not be null.
     * @param distance the distance to the traffic light, distance cannot be null.
     * @param turnOnRed whether the perceiving GTU may turn on red.
     * @throws GtuException when id is null, or parameters are inconsistent
     */
    public HeadwayTrafficLightReal(final TrafficLight trafficLight, final Length distance, final boolean turnOnRed)
            throws GtuException
    {
        super(ObjectType.TRAFFICLIGHT, id(trafficLight), distance, trafficLight.getLane());
        this.trafficLight = trafficLight;
        this.turnOnRed = turnOnRed;
    }

    /**
     * Get the id of the traffic light; throw an exception if traffic light is null.
     * @param trafficLight the traffic light object for further observation, can not be null.
     * @return he id of the traffic light.
     * @throws GtuException when the trafficLight object is null
     */
    private static String id(final TrafficLight trafficLight) throws GtuException
    {
        Throw.when(trafficLight == null, GtuException.class, "Headway constructor: trafficLight == null");
        return trafficLight.getId();
    }

    @Override
    public final TrafficLightColor getTrafficLightColor()
    {
        return this.trafficLight.getTrafficLightColor();
    }

    @Override
    public final boolean canTurnOnRed()
    {
        return this.turnOnRed;
    }

    @Override
    public final String toString()
    {
        return "HeadwayTrafficLight [trafficLight=" + this.trafficLight + "]";
    }
}
