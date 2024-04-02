package org.opentrafficsim.road.network.lane.object.trafficlight;

import java.util.LinkedHashSet;
import java.util.Set;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.event.EventType;
import org.djutils.exceptions.Throw;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.object.AbstractLaneBasedObject;
import org.opentrafficsim.road.network.lane.object.LaneBasedObject;

/**
 * Standard implementation of a traffic light.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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

    /** Node that we can turn to through red. */
    private Set<Node> turnOnRed = null;

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

    /**
     * Add node GTUs may turn to through red.
     * @param node Node; node.
     */
    public void addTurnOnRed(final Node node)
    {
        if (this.turnOnRed == null)
        {
            this.turnOnRed = new LinkedHashSet<>();
        }
        this.turnOnRed.add(node);
    }

    /**
     * Whether a GTU can turn on red.
     * @param route Route; route.
     * @param gtuType GtuType; GTU type.
     * @return boolean; whether a GTU can turn on red.
     */
    public boolean canTurnOnRed(final Route route, final GtuType gtuType)
    {
        if (this.turnOnRed == null)
        {
            return false;
        }
        try
        {
            // move until next link split (link will be link before split)
            Link link = this.getLane().getLink();
            Set<Link> next = link.getEndNode().nextLinks(gtuType, link);
            while (next.size() == 1)
            {
                link = next.iterator().next();
                next = link.getEndNode().nextLinks(gtuType, link);
            }
            // check if next node in the route, beyond the split, is ok to turn to on red
            Node endNode = link.getEndNode();
            int nodeIndex = route.indexOf(endNode);
            if (nodeIndex < 0 || nodeIndex == route.size() - 1)
            {
                return false;
            }
            return this.turnOnRed.contains(route.getNode(nodeIndex + 1));
        }
        catch (NetworkException ex)
        {
            // we explicitly use the previous link which should be connected, and check the number of nodes in the route
            throw new RuntimeException(ex);
        }
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "SimpleTrafficLight [trafficLightColor=" + getTrafficLightColor() + "]";
    }

}
