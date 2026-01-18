package org.opentrafficsim.road.network.lane;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.djutils.draw.function.ContinuousPiecewiseLinearFunction;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.point.Point2d;
import org.djutils.event.EventType;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;
import org.opentrafficsim.base.geometry.OtsLine2d;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.road.network.LaneKeepingPolicy;
import org.opentrafficsim.road.network.RoadNetwork;

/**
 * A CrossSectionLink is a link with lanes where GTUs can possibly switch between lanes.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public class CrossSectionLink extends Link
{
    /** List of cross-section elements. */
    private final List<CrossSectionElement> crossSectionElementList = new ArrayList<>();

    /** List of lanes. */
    private final List<Lane> lanes = new ArrayList<>();

    /** List of shoulders. */
    private final List<Shoulder> shoulders = new ArrayList<>();

    /** The policy to generally keep left, keep right, or keep lane. */
    private final LaneKeepingPolicy laneKeepingPolicy;

    /** Priority. */
    private Priority priority = Priority.NONE;

    /** Line over which GTUs enter or leave the link at the start node. */
    private PolyLine2d startLine;

    /** Line over which GTUs enter or leave the link at the end node. */
    private PolyLine2d endLine;

    /**
     * The (regular, not timed) event type for pub/sub indicating the addition of a Lane to a CrossSectionLink. <br>
     * Payload: Object[] { String networkId, String linkId, String LaneId, int laneNumber } <br>
     * TODO work in a different way with lane numbers to align to standard lane numbering.
     */
    public static final EventType LANE_ADD_EVENT = new EventType("LINK.LANE.ADD",
            new MetaData("Lane data", "Lane data",
                    new ObjectDescriptor[] {new ObjectDescriptor("Network id", "Network id", String.class),
                            new ObjectDescriptor("Link id", "Link id", String.class),
                            new ObjectDescriptor("Lane id", "Lane id", String.class),
                            new ObjectDescriptor("Lane number", "Lane number", Integer.class)}));

    /**
     * The (regular, not timed) event type for pub/sub indicating the removal of a Lane from a CrossSectionLink. <br>
     * Payload: Object[] { String networkId, String linkId, String LaneId } <br>
     * TODO allow for the removal of a Lane; currently this is not possible.
     */
    public static final EventType LANE_REMOVE_EVENT = new EventType("LINK.LANE.REMOVE",
            new MetaData("Lane data", "Lane data",
                    new ObjectDescriptor[] {new ObjectDescriptor("Network id", "Network id", String.class),
                            new ObjectDescriptor("Link id", "Link id", String.class),
                            new ObjectDescriptor("Lane id", "Lane id", String.class),
                            new ObjectDescriptor("Lane number", "Lane number", Integer.class)}));

    /**
     * Construction of a cross section link.
     * @param network the network
     * @param id the link id.
     * @param startNode the start node (directional).
     * @param endNode the end node (directional).
     * @param linkType the link type
     * @param designLine the design line of the Link
     * @param elevation elevation given over fractional length, may be {@code null}.
     * @param laneKeepingPolicy the policy to generally keep left, keep right, or keep lane
     * @throws NetworkException if link already exists in the network, if name of the link is not unique, or if the start node
     *             or the end node of the link are not registered in the network.
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public CrossSectionLink(final RoadNetwork network, final String id, final Node startNode, final Node endNode,
            final LinkType linkType, final OtsLine2d designLine, final ContinuousPiecewiseLinearFunction elevation,
            final LaneKeepingPolicy laneKeepingPolicy) throws NetworkException
    {
        super(network, id, startNode, endNode, linkType, designLine, elevation);
        this.laneKeepingPolicy = laneKeepingPolicy;
    }

    @Override
    public RoadNetwork getNetwork()
    {
        return (RoadNetwork) super.getNetwork();
    }

    /**
     * Add a cross section element at the end of the list. <br>
     * <b>Note:</b> LEFT is seen as a positive lateral direction, RIGHT as a negative lateral direction.
     * @param cse the cross section element to add.
     */
    protected final void addCrossSectionElement(final CrossSectionElement cse)
    {
        this.crossSectionElementList.add(cse);
        if (cse instanceof Lane)
        {
            if (cse instanceof Shoulder)
            {
                this.shoulders.add((Shoulder) cse);
            }
            else
            {
                this.lanes.add((Lane) cse);
                fireTimedEvent(LANE_ADD_EVENT,
                        new Object[] {getNetwork().getId(), getId(), cse.getId(), this.lanes.indexOf(cse)},
                        getSimulator().getSimulatorTime());
            }
        }
    }

    /**
     * Retrieve a safe copy of the cross section element list.
     * @return the cross section element list.
     */
    public final List<CrossSectionElement> getCrossSectionElementList()
    {
        return this.crossSectionElementList == null ? new ArrayList<>() : new ArrayList<>(this.crossSectionElementList);
    }

    /**
     * Retrieve the lane keeping policy.
     * @return the lane keeping policy on this CrossSectionLink
     */
    public final LaneKeepingPolicy getLaneKeepingPolicy()
    {
        return this.laneKeepingPolicy;
    }

    /**
     * Find a cross section element with a specified id.
     * @param id the id to search for
     * @return the cross section element with the given id, empty if not found
     */
    public final Optional<CrossSectionElement> getCrossSectionElement(final String id)
    {
        for (CrossSectionElement cse : this.crossSectionElementList)
        {
            if (cse.getId().equals(id))
            {
                return Optional.of(cse);
            }
        }
        return Optional.empty();
    }

    /**
     * Return a safe copy of the list of lanes of this CrossSectionLink.
     * @return the list of lanes.
     */
    public final List<Lane> getLanes()
    {
        return new ArrayList<>(this.lanes);
    }

    /**
     * Return a safe copy of the list of shoulders of this CrossSectionLink.
     * @return the list of lanes.
     */
    public final List<Shoulder> getShoulders()
    {
        return new ArrayList<>(this.shoulders);
    }

    /**
     * Return a safe copy of the list of lanes and shoulders of this CrossSectionLink.
     * @return the list of lanes.
     */
    public final List<Lane> getLanesAndShoulders()
    {
        List<Lane> all = new ArrayList<>(this.lanes);
        all.addAll(this.shoulders);
        return all;
    }

    /**
     * Sets the priority.
     * @return priority.
     */
    public final Priority getPriority()
    {
        return this.priority;
    }

    /**
     * Returns the priority.
     * @param priority set priority.
     */
    public final void setPriority(final Priority priority)
    {
        this.priority = priority;
    }

    /**
     * Returns the line over which GTUs enter and leave the link at the start node.
     * @return line over which GTUs enter and leave the link at the start node
     */
    public PolyLine2d getStartLine()
    {
        if (this.startLine == null)
        {
            double left = Double.NaN;
            double right = Double.NaN;
            for (Lane lane : getLanesAndShoulders())
            {
                double half = lane.getBeginWidth().si * .5;
                if (!Double.isNaN(left))
                {
                    left = Math.max(left, lane.getOffsetAtBegin().si + half);
                    right = Math.min(right, lane.getOffsetAtBegin().si - half);
                }
                else
                {
                    left = lane.getOffsetAtBegin().si + half;
                    right = lane.getOffsetAtBegin().si - half;
                }
            }
            Point2d start = getDesignLine().getFirst();
            double heading = getStartNode().getHeading().si + .5 * Math.PI;
            double cosHeading = Math.cos(heading);
            double sinHeading = Math.sin(heading);
            // Note: right is negative so same sign before cos and sin
            Point2d leftPoint = new Point2d(start.x + cosHeading * left, start.y + sinHeading * left);
            Point2d rightPoint = new Point2d(start.x + cosHeading * right, start.y + sinHeading * right);
            this.startLine = new PolyLine2d(leftPoint, rightPoint);
        }
        return this.startLine;
    }

    /**
     * Returns the line over which GTUs enter and leave the link at the end node.
     * @return line over which GTUs enter and leave the link at the end node
     */
    public PolyLine2d getEndLine()
    {
        if (this.endLine == null)
        {
            double left = Double.NaN;
            double right = Double.NaN;
            for (Lane lane : getLanesAndShoulders())
            {
                double half = lane.getEndWidth().si * .5;
                if (!Double.isNaN(left))
                {
                    left = Math.max(left, lane.getOffsetAtEnd().si + half);
                    right = Math.min(right, lane.getOffsetAtEnd().si - half);
                }
                else
                {
                    left = lane.getOffsetAtEnd().si + half;
                    right = lane.getOffsetAtEnd().si - half;
                }
            }
            Point2d start = getDesignLine().getLast();
            double heading = getEndNode().getHeading().si + .5 * Math.PI;
            double cosHeading = Math.cos(heading);
            double sinHeading = Math.sin(heading);
            Point2d leftPoint = new Point2d(start.x + cosHeading * left, start.y + sinHeading * left);
            Point2d rightPoint = new Point2d(start.x + cosHeading * right, start.y + sinHeading * right);
            this.endLine = new PolyLine2d(leftPoint, rightPoint);
        }
        return this.endLine;
    }

    @Override
    public final String toString()
    {
        return "CrossSectionLink [name=" + this.getId() + ", nodes=" + getStartNode().getId() + "-" + getEndNode().getId()
                + ", crossSectionElementList=" + this.crossSectionElementList + ", lanes=" + this.lanes + ", laneKeepingPolicy="
                + this.laneKeepingPolicy + "]";
    }

    /**
     * Priority of a link.
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    public enum Priority
    {
        /** Traffic has priority. */
        PRIORITY,

        /** No priority. */
        NONE,

        /** Yield. */
        YIELD,

        /** Need to stop. */
        STOP,

        /** Priority according to all-stop rules. */
        ALL_STOP,

        /** Priority at bus stop, i.e. bus has right of way if it wants to leave the bus stop. */
        BUS_STOP;

        /**
         * Returns whether this is priority.
         * @return whether this is priority
         */
        public boolean isPriority()
        {
            return this.equals(PRIORITY);
        }

        /**
         * Returns whether this is none.
         * @return whether this is none
         */
        public boolean isNone()
        {
            return this.equals(NONE);
        }

        /**
         * Returns whether this is yield.
         * @return whether this is yield
         */
        public boolean isYield()
        {
            return this.equals(YIELD);
        }

        /**
         * Returns whether this is stop.
         * @return whether this is stop
         */
        public boolean isStop()
        {
            return this.equals(STOP);
        }

        /**
         * Returns whether this is all-stop.
         * @return whether this is all-stop
         */
        public boolean isAllStop()
        {
            return this.equals(ALL_STOP);
        }

        /**
         * Returns whether this is bus stop.
         * @return whether this is bus stop
         */
        public boolean isBusStop()
        {
            return this.equals(BUS_STOP);
        }

    }

}
