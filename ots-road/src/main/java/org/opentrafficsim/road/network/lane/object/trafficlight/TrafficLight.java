package org.opentrafficsim.road.network.lane.object.trafficlight;

import java.rmi.RemoteException;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.event.EventType;
import org.djutils.exceptions.Throw;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.object.AbstractLaneBasedObject;
import org.opentrafficsim.road.network.lane.object.LaneBasedObject;

/**
 * Standard implementation of a traffic light.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class TrafficLight extends AbstractLaneBasedObject
{
    /** */
    private static final long serialVersionUID = 20230216L;

    /** The color of the traffic light. */
    private TrafficLightColor trafficLightColor;

    /** The simulator to schedule events on. */
    private final OtsSimulatorInterface simulator;

    /** Default elevation of a traffic light (above zero; don't use this for lanes at non-zero elevation). */
    public static final Length DEFAULT_TRAFFICLIGHT_ELEVATION = new Length(1, LengthUnit.METER);

    /**
     * The <b>timed</b> event type for pub/sub indicating the change of color of a traffic light. <br>
     * Payload: Object[] {String trafficLightId, TrafficLight trafficLight, TrafficLightColor newColor}
     */
    public static final EventType TRAFFICLIGHT_CHANGE_EVENT = new EventType("TRAFFICLIGHT.CHANGE",
            new MetaData("Traffic light changed", "Color of traffic light has changed",
                    new ObjectDescriptor("Traffic light id", "Id of the traffic light", String.class),
                    new ObjectDescriptor("Traffic light", "The traffic light itself", TrafficLight.class),
                    new ObjectDescriptor("Traffic light color", "New traffic light color", TrafficLightColor.class)));

    /**
     * Construct an AbstractTrafficLight with specified elevation.
     * @param id String; traffic light id
     * @param lane Lane; lane where the traffic light is located
     * @param longitudinalPosition Length; position of the traffic light on the lane, in the design direction
     * @param simulator OtsSimulatorInterface; the simulator for animation and timed events
     * @param height Length; the elevation of the traffic light
     * @throws NetworkException on failure to place the object
     */
    public TrafficLight(final String id, final Lane lane, final Length longitudinalPosition,
            final OtsSimulatorInterface simulator, final Length height) throws NetworkException
    {
        super(id, lane, longitudinalPosition, LaneBasedObject.makeGeometry(lane, longitudinalPosition), height);

        Throw.whenNull(simulator, "Simulator may not be null");
        this.simulator = simulator;
        this.trafficLightColor = TrafficLightColor.RED;

        init();
    }

    /**
     * Construct an AbstractTrafficLight at default elevation (use only on roads at elevation 0).
     * @param id String; traffic light id
     * @param lane Lane; lane where the traffic light is located
     * @param longitudinalPosition Length; position of the traffic light on the lane, in the design direction
     * @param simulator OtsSimulatorInterface; the simulator for animation and timed events
     * @throws NetworkException on failure to place the object
     */
    public TrafficLight(final String id, final Lane lane, final Length longitudinalPosition,
            final OtsSimulatorInterface simulator) throws NetworkException
    {
        this(id, lane, longitudinalPosition, simulator, DEFAULT_TRAFFICLIGHT_ELEVATION);
    }

    /**
     * Get the current traffic light color.
     * @return TrafficLightColor; current traffic light color.
     */
    public final TrafficLightColor getTrafficLightColor()
    {
        return this.trafficLightColor;
    }

    /**
     * Set the new traffic light color.
     * @param trafficLightColor TrafficLightColor; set the trafficLightColor
     */
    public final void setTrafficLightColor(final TrafficLightColor trafficLightColor)
    {
        this.trafficLightColor = trafficLightColor;
        fireTimedEvent(TRAFFICLIGHT_CHANGE_EVENT, new Object[] {getId(), this, trafficLightColor},
                this.simulator.getSimulatorTime());
    }

    /** {@inheritDoc} */
    @Override
    public double getZ() throws RemoteException
    {
        return -0.0001;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "SimpleTrafficLight [trafficLightColor=" + getTrafficLightColor() + "]";
    }

}
