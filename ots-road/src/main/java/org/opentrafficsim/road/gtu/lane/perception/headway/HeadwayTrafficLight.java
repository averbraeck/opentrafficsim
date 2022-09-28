package org.opentrafficsim.road.gtu.lane.perception.headway;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLight;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLightColor;

/**
 * Container for a reference to information about a (lane based) traffic light and a headway to the traffic light.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class HeadwayTrafficLight extends AbstractHeadwayLaneBasedObject
{
    /** */
    private static final long serialVersionUID = 20160410L;

    /** the traffic light object for further observation, can not be null. */
    private final TrafficLight trafficLight;

    /**
     * Construct a new Headway information object, for a traffic light ahead of us (or behind us, although that does not seem
     * very useful).
     * @param trafficLight TrafficLight; the traffic light object for further observation, can not be null.
     * @param distance Length; the distance to the traffic light, distance cannot be null.
     * @throws GTUException when id is null, or parameters are inconsistent
     */
    public HeadwayTrafficLight(final TrafficLight trafficLight, final Length distance) throws GTUException
    {
        super(ObjectType.TRAFFICLIGHT, id(trafficLight), distance, trafficLight.getLane());
        this.trafficLight = trafficLight;
    }

    /**
     * Get the id of the traffic light; throw an exception if traffic light is null.
     * @param trafficLight TrafficLight; the traffic light object for further observation, can not be null.
     * @return he id of the traffic light.
     * @throws GTUException when the trafficLight object is null
     */
    private static String id(final TrafficLight trafficLight) throws GTUException
    {
        Throw.when(trafficLight == null, GTUException.class, "Headway constructor: trafficLight == null");
        return trafficLight.getId();
    }

    /**
     * @return the traffic light color.
     */
    public final TrafficLightColor getTrafficLightColor()
    {
        return this.trafficLight.getTrafficLightColor();
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "HeadwayTrafficLight [trafficLight=" + this.trafficLight + "]";
    }
}
