package org.opentrafficsim.road.network.lane;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.djutils.event.EventType;
import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;
import org.opentrafficsim.core.geometry.DirectedPoint;
import org.opentrafficsim.core.geometry.OtsLine3d;
import org.opentrafficsim.core.geometry.OtsPoint3d;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OtsLink;
import org.opentrafficsim.road.network.OtsRoadNetwork;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;

/**
 * A CrossSectionLink is a link with lanes where GTUs can possibly switch between lanes.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public class CrossSectionLink extends OtsLink implements Serializable
{
    /** */
    private static final long serialVersionUID = 20141015L;

    /** List of cross-section elements. */
    private final List<CrossSectionElement> crossSectionElementList = new ArrayList<>();

    /** List of lanes. */
    private final List<Lane> lanes = new ArrayList<>();

    /** The policy to generally keep left, keep right, or keep lane. */
    private final LaneKeepingPolicy laneKeepingPolicy;

    /** Priority. */
    private Priority priority = Priority.NONE;

    /** Line over which GTUs enter or leave the link at the start node. */
    private OtsLine3d startLine;

    /** Line over which GTUs enter or leave the link at the end node. */
    private OtsLine3d endLine;

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
     * @param network OtsRoadNetwork; the network
     * @param id String; the link id.
     * @param startNode OtsRoaNode; the start node (directional).
     * @param endNode OtsRoaNode; the end node (directional).
     * @param linkType LinkType; the link type
     * @param designLine OtsLine3d; the design line of the Link
     * @param laneKeepingPolicy LaneKeepingPolicy; the policy to generally keep left, keep right, or keep lane
     * @throws NetworkException if link already exists in the network, if name of the link is not unique, or if the start node
     *             or the end node of the link are not registered in the network.
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public CrossSectionLink(final OtsRoadNetwork network, final String id, final OtsRoadNode startNode,
            final OtsRoadNode endNode, final LinkType linkType, final OtsLine3d designLine,
            final LaneKeepingPolicy laneKeepingPolicy) throws NetworkException
    {
        super(network, id, startNode, endNode, linkType, designLine);
        this.laneKeepingPolicy = laneKeepingPolicy;
    }

    /** {@inheritDoc} */
    @Override
    public OtsRoadNetwork getNetwork()
    {
        return (OtsRoadNetwork) super.getNetwork();
    }

    /**
     * Add a cross section element at the end of the list. <br>
     * <b>Note:</b> LEFT is seen as a positive lateral direction, RIGHT as a negative lateral direction.
     * @param cse CrossSectionElement; the cross section element to add.
     */
    protected final void addCrossSectionElement(final CrossSectionElement cse)
    {
        this.crossSectionElementList.add(cse);
        if (cse instanceof Lane)
        {
            this.lanes.add((Lane) cse);
            fireTimedEvent(LANE_ADD_EVENT, new Object[] {getNetwork().getId(), getId(), cse.getId(), this.lanes.indexOf(cse)},
                    getSimulator().getSimulatorTime());
        }
    }

    /**
     * Retrieve a safe copy of the cross section element list.
     * @return List&lt;CrossSectionElement&gt;; the cross section element list.
     */
    public final List<CrossSectionElement> getCrossSectionElementList()
    {
        return this.crossSectionElementList == null ? new ArrayList<>() : new ArrayList<>(this.crossSectionElementList);
    }

    /**
     * Retrieve the lane keeping policy.
     * @return LaneKeepingPolicy; the lane keeping policy on this CrossSectionLink
     */
    public final LaneKeepingPolicy getLaneKeepingPolicy()
    {
        return this.laneKeepingPolicy;
    }

    /**
     * Find a cross section element with a specified id.
     * @param id String; the id to search for
     * @return CrossSectionElement; the cross section element with the given id, or null if not found
     */
    public final CrossSectionElement getCrossSectionElement(final String id)
    {
        for (CrossSectionElement cse : this.crossSectionElementList)
        {
            if (cse.getId().equals(id))
            {
                return cse;
            }
        }
        return null;
    }

    /**
     * Return a safe copy of the list of lanes of this CrossSectionLink.
     * @return List&lt;Lane&gt;; the list of lanes.
     */
    public final List<Lane> getLanes()
    {
        return this.lanes == null ? new ArrayList<>() : new ArrayList<>(this.lanes);
    }

    /**
     * @return priority.
     */
    public final Priority getPriority()
    {
        return this.priority;
    }

    /**
     * @param priority Priority; set priority.
     */
    public final void setPriority(final Priority priority)
    {
        this.priority = priority;
    }

    /**
     * Returns the line over which GTUs enter and leave the link at the start node.
     * @return OtsLine3d; line over which GTUs enter and leave the link at the start node
     */
    public OtsLine3d getStartLine()
    {
        if (this.startLine == null)
        {
            double left = Double.NaN;
            double right = Double.NaN;
            for (Lane lane : this.lanes)
            {
                double half = lane.getBeginWidth().si * .5;
                if (!Double.isNaN(left))
                {
                    left = Math.max(left, lane.getDesignLineOffsetAtBegin().si + half);
                    right = Math.min(right, lane.getDesignLineOffsetAtBegin().si - half);
                }
                else
                {
                    left = lane.getDesignLineOffsetAtBegin().si + half;
                    right = lane.getDesignLineOffsetAtBegin().si - half;
                }
            }
            OtsPoint3d start = getStartNode().getPoint();
            double heading = getStartNode().getHeading() + .5 * Math.PI;
            double cosHeading = Math.cos(heading);
            double sinHeading = Math.sin(heading);
            OtsPoint3d leftPoint = new OtsPoint3d(start.x + cosHeading * left, start.y + sinHeading * left);
            OtsPoint3d rightPoint = new OtsPoint3d(start.x - cosHeading * right, start.y - sinHeading * right);
            this.startLine = Try.assign(() -> new OtsLine3d(leftPoint, rightPoint), "Invalid startline on CrossSectionLink.");
        }
        return this.startLine;
    }

    /**
     * Returns the line over which GTUs enter and leave the link at the end node.
     * @return OtsLine3d; line over which GTUs enter and leave the link at the end node
     */
    public OtsLine3d getEndLine()
    {
        if (this.endLine == null)
        {
            double left = Double.NaN;
            double right = Double.NaN;
            for (Lane lane : this.lanes)
            {
                double half = lane.getEndWidth().si * .5;
                if (!Double.isNaN(left))
                {
                    left = Math.max(left, lane.getDesignLineOffsetAtEnd().si + half);
                    right = Math.min(right, lane.getDesignLineOffsetAtEnd().si - half);
                }
                else
                {
                    left = lane.getDesignLineOffsetAtEnd().si + half;
                    right = lane.getDesignLineOffsetAtEnd().si - half;
                }
            }
            OtsPoint3d start = getEndNode().getPoint();
            DirectedPoint p = Try.assign(() -> getEndNode().getLocation(), "Unexpected remote exception.");
            double heading = p.getRotZ() + .5 * Math.PI;
            double cosHeading = Math.cos(heading);
            double sinHeading = Math.sin(heading);
            OtsPoint3d leftPoint = new OtsPoint3d(start.x + cosHeading * left, start.y + sinHeading * left);
            OtsPoint3d rightPoint = new OtsPoint3d(start.x + cosHeading * right, start.y + sinHeading * right);
            this.endLine = Try.assign(() -> new OtsLine3d(leftPoint, rightPoint), "Invalid endline on CrossSectionLink.");
        }
        return this.endLine;
    }

    /** {@inheritDoc} */
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
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    public enum Priority
    {
        /** Traffic has priority. */
        PRIORITY,

        /** No priority. */
        NONE,

        /** Turn on red. */
        TURN_ON_RED,

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
         * Returns whether this is turn on red.
         * @return whether this is turn on red
         */
        public boolean isTurnOnRed()
        {
            return this.equals(TURN_ON_RED);
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
