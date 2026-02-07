package org.opentrafficsim.road.network.lane.object.trafficlight;

import java.util.LinkedHashSet;
import java.util.Set;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.event.EventType;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;
import org.opentrafficsim.base.OtsRuntimeException;
import org.opentrafficsim.base.logger.Logger;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.perception.Historical;
import org.opentrafficsim.core.perception.HistoricalValue;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.object.AbstractLaneBasedObject;
import org.opentrafficsim.road.network.lane.object.LaneBasedObject;

/**
 * Standard implementation of a traffic light.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class TrafficLight extends AbstractLaneBasedObject
{
    /** The color of the traffic light. */
    private final Historical<TrafficLightColor> trafficLightColor;

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
     * @param id traffic light id
     * @param lane lane where the traffic light is located
     * @param longitudinalPosition position of the traffic light on the lane, in the design direction
     * @param height the elevation of the traffic light
     * @throws NetworkException on failure to place the object
     */
    public TrafficLight(final String id, final Lane lane, final Length longitudinalPosition, final Length height)
            throws NetworkException
    {
        super(id, lane, longitudinalPosition, LaneBasedObject.makeLine(lane, longitudinalPosition), height);
        this.trafficLightColor = new HistoricalValue<>(getSimulator().getReplication().getHistoryManager(getSimulator()), this,
                TrafficLightColor.RED);
        init();
    }

    /**
     * Construct an AbstractTrafficLight at default elevation (use only on roads at elevation 0).
     * @param id traffic light id
     * @param lane lane where the traffic light is located
     * @param longitudinalPosition position of the traffic light on the lane, in the design direction
     * @throws NetworkException on failure to place the object
     */
    public TrafficLight(final String id, final Lane lane, final Length longitudinalPosition) throws NetworkException
    {
        this(id, lane, longitudinalPosition, DEFAULT_TRAFFICLIGHT_ELEVATION);
    }

    /**
     * Get the current traffic light color.
     * @return current traffic light color.
     */
    public final TrafficLightColor getTrafficLightColor()
    {
        return this.trafficLightColor.get();
    }

    /**
     * Get the traffic light color in the past.
     * @param time simulation time to obtain traffic light color.
     * @return current traffic light color.
     */
    public final TrafficLightColor getTrafficLightColor(final Duration time)
    {
        return this.trafficLightColor.get(time);
    }

    /**
     * Set the new traffic light color.
     * @param trafficLightColor set the trafficLightColor
     */
    public final void setTrafficLightColor(final TrafficLightColor trafficLightColor)
    {
        this.trafficLightColor.set(trafficLightColor);
        fireTimedEvent(TRAFFICLIGHT_CHANGE_EVENT, new Object[] {getId(), this, trafficLightColor},
                getSimulator().getSimulatorTime());
    }

    /**
     * Add node GTUs may turn to through red.
     * @param node node.
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
     * @param route route.
     * @param gtuType GTU type.
     * @return whether a GTU can turn on red.
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
            Link link = getLane().getLink();
            Set<Link> next = link.getEndNode().getLinks().toSet(); // .nextLinks(gtuType, link);
            next.remove(link);
            while (next.size() == 1)
            {
                link = next.iterator().next();
                next = link.getEndNode().getLinks().toSet(); // .nextLinks(gtuType, link);
                next.remove(link);
            }
            // check if next node in the route, beyond the split, is ok to turn to on red
            Node nextEndNode;
            if (route != null)
            {
                int nodeIndex = route.indexOf(link.getEndNode());
                if (nodeIndex < 0 || nodeIndex == route.size() - 1)
                {
                    return false;
                }
                nextEndNode = route.getNode(nodeIndex + 1);
            }
            else
            {
                next = link.getEndNode().nextLinks(gtuType, link);
                if (next.size() == 1)
                {
                    nextEndNode = next.iterator().next().getEndNode();
                }
                else
                {
                    Logger.ots().warn("GTU without route cannot determine whether it can turn on red at node {}.",
                            link.getEndNode());
                    return false;
                }
            }
            return this.turnOnRed.contains(nextEndNode);
        }
        catch (NetworkException ex)
        {
            // we explicitly use the previous link which should be connected, and check the number of nodes in the route
            throw new OtsRuntimeException(ex);
        }
    }

    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "TrafficLight [trafficLightColor=" + getTrafficLightColor() + "]";
    }

}
