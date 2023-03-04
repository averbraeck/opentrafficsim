package org.opentrafficsim.road.gtu.lane.perception;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
import org.djutils.multikeymap.MultiKeyMap;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * A LaneStructureRecord contains information about the lanes that can be accessed from this lane by a GtuType. It tells whether
 * there is a left and/or right lane by pointing to other LaneStructureRecords, and which successor LaneStructureRecord(s) there
 * are at the end of the lane of this LaneStructureRecord. All information (left, right, next) is calculated relative to the
 * driving direction of the GTU that owns this structure.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class RollingLaneStructureRecord implements LaneStructureRecord, Serializable
{
    /** */
    private static final long serialVersionUID = 20160400L;

    /** Cache of allows route information. */
    // TODO clear on network change, with an event listener?
    private static MultiKeyMap<Boolean> allowsRouteCache =
            new MultiKeyMap<>(Lane.class, Route.class, GtuType.class, Boolean.class);

    /** The lane of the LSR. */
    private final Lane lane;

    /** The left LSR or null if not available. Left and right are relative to the <b>driving</b> direction. */
    private RollingLaneStructureRecord left;

    /** Legal left lane change possibility. */
    private boolean mayChangeLeft;

    /** The right LSR or null if not available. Left and right are relative to the <b>driving</b> direction. */
    private RollingLaneStructureRecord right;

    /** Legal right lane change possibility. */
    private boolean mayChangeRight;

    /** Where this lane was cut-off resulting in no next lanes, if so. */
    private Length cutOffEnd = null;

    /** Where this lane was cut-off resulting in no prev lanes, if so. */
    private Length cutOffStart = null;

    /** Distance to start of the record, negative for backwards. */
    private Length startDistance;

    /**
     * The next LSRs. The list is empty if no LSRs are available. Next is relative to the driving direction, not to the design
     * line direction.
     */
    private List<RollingLaneStructureRecord> nextList = new ArrayList<>();

    /**
     * The previous LSRs. The list is empty if no LSRs are available. Previous is relative to the driving direction, not to the
     * design line direction.
     */
    private List<RollingLaneStructureRecord> prevList = new ArrayList<>();

    /** Record who's start start distance is used to calculate the start distance of this record. */
    private RollingLaneStructureRecord source;

    /** Start distance link between records. */
    private RecordLink sourceLink;

    /** Set of records who's starting position depends on this record. */
    private final Set<RollingLaneStructureRecord> dependentRecords = new LinkedHashSet<>();

    /**
     * Constructor.
     * @param lane Lane; lane
     * @param startDistanceSource RollingLaneStructureRecord; record on which the start distance is based
     * @param recordLink RecordLink; link type to source
     */
    public RollingLaneStructureRecord(final Lane lane, final RollingLaneStructureRecord startDistanceSource,
            final RecordLink recordLink)
    {
        this.lane = lane;
        this.source = startDistanceSource;
        this.sourceLink = recordLink;
        if (startDistanceSource != null)
        {
            startDistanceSource.dependentRecords.add(this);
        }
    }

    /**
     * @param lane Lane; the lane of the LSR
     * @param startDistance Length; distance to start of the record
     */
    public RollingLaneStructureRecord(final Lane lane, final Length startDistance)
    {
        this.lane = lane;
        this.startDistance = startDistance;

        this.source = null;
        this.sourceLink = null;
    }

    /** {@inheritDoc} */
    @Override
    public Length getLength()
    {
        return getLane().getLength();
    }

    /**
     * Change the source of the distance.
     * @param startDistanceSource RollingLaneStructureRecord; record on which the start distance is based
     * @param recordLink RecordLink; link type to source
     */
    final void changeStartDistanceSource(final RollingLaneStructureRecord startDistanceSource, final RecordLink recordLink)
    {
        // clear link
        if (this.source != null)
        {
            this.source.dependentRecords.remove(this);
        }
        // set new link
        this.source = startDistanceSource;
        this.sourceLink = recordLink;
        if (this.source != null)
        {
            this.source.dependentRecords.add(this);
        }
    }

    /**
     * Updates the start distance, including all records who's start distance depends on this value. Advised is to only initiate
     * this at the root record. Note that before this is invoked, all record-links should be updated.
     * @param fractionalPosition double; fractional position at the current cross-section
     * @param laneStructure RollingLaneStructure; parent lane structure
     */
    final void updateStartDistance(final double fractionalPosition, final RollingLaneStructure laneStructure)
    {
        this.startDistance = this.sourceLink.calculateStartDistance(this.source, this, fractionalPosition);
        for (RollingLaneStructureRecord record : this.dependentRecords)
        {
            record.updateStartDistance(fractionalPosition, laneStructure);
        }
    }

    /**
     * Returns the source of the start distance.
     * @return LaneStructureRecord; source of the start distance
     */
    final RollingLaneStructureRecord getStartDistanceSource()
    {
        return this.source;
    }

    /** {@inheritDoc} */
    @Override
    public final Node getFromNode()
    {
        return this.lane.getParentLink().getStartNode();
    }

    /** {@inheritDoc} */
    @Override
    public final Node getToNode()
    {
        return this.lane.getParentLink().getEndNode();
    }

    /**
     * @return whether the link to which this lane belongs splits, i.e. some of the parallel, connected lanes lead to a
     *         different destination than others
     */
    @Deprecated
    public final boolean isLinkSplit()
    {
        if (isCutOffEnd())
        {
            // if the end is a split, it's out of range
            return false;
        }
        Set<Node> toNodes = new LinkedHashSet<>();
        LaneStructureRecord lsr = this;
        while (lsr != null)
        {
            for (LaneStructureRecord next : lsr.getNext())
            {
                toNodes.add(next.getToNode());
            }
            lsr = lsr.getLeft();
        }
        lsr = this.getRight();
        while (lsr != null)
        {
            for (LaneStructureRecord next : lsr.getNext())
            {
                toNodes.add(next.getToNode());
            }
            lsr = lsr.getRight();
        }
        return toNodes.size() > 1;
    }

    /**
     * @return whether the link to which this lane belongs merges, i.e. some of the parallel, connected lanes follow from a
     *         different origin than others
     */
    public final boolean isLinkMerge()
    {
        if (isCutOffStart())
        {
            // if the start is a merge, it's out of range
            return false;
        }
        Set<Node> fromNodes = new LinkedHashSet<>();
        LaneStructureRecord lsr = this;
        while (lsr != null)
        {
            for (LaneStructureRecord prev : lsr.getPrev())
            {
                fromNodes.add(prev.getFromNode());
            }
            lsr = lsr.getLeft();
        }
        lsr = this.getRight();
        while (lsr != null)
        {
            for (LaneStructureRecord prev : lsr.getPrev())
            {
                fromNodes.add(prev.getFromNode());
            }
            lsr = lsr.getRight();
        }
        return fromNodes.size() > 1;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean allowsRoute(final Route route, final GtuType gtuType) throws NetworkException
    {
        return allowsRoute(route, gtuType, false);
    }

    /** {@inheritDoc} */
    @Override
    public final boolean allowsRouteAtEnd(final Route route, final GtuType gtuType) throws NetworkException
    {
        return allowsRoute(route, gtuType, true);
    }

    /**
     * Returns whether (the end of) this lane allows the route to be followed, using caching.
     * @param route Route; the route to follow
     * @param gtuType GtuType; gtu type
     * @param end boolean; whether to consider the end (or otherwise the lane itself, i.e. allow lane change from this lane)
     * @return whether the end of this lane allows the route to be followed
     * @throws NetworkException if no destination node
     */
    private boolean allowsRoute(final Route route, final GtuType gtuType, final boolean end) throws NetworkException
    {
        return allowsRouteCache.get(() -> Try.assign(() -> allowsRoute0(route, gtuType, end), "no destination"), this.lane,
                route, gtuType, end);
    }

    /**
     * Returns whether (the end of) this lane allows the route to be followed.
     * @param route Route; the route to follow
     * @param gtuType GtuType; gtu type
     * @param end boolean; whether to consider the end (or otherwise the lane itself, i.e. allow lane change from this lane)
     * @return whether the end of this lane allows the route to be followed
     * @throws NetworkException if no destination node
     */
    private boolean allowsRoute0(final Route route, final GtuType gtuType, final boolean end) throws NetworkException
    {
        // driving without route
        if (route == null)
        {
            return true;
        }

        // start with simple check
        int from = route.indexOf(getFromNode());
        int to = route.indexOf(getToNode());
        if (from == -1 || to == -1 || from != to - 1)
        {
            return leadsToRoute(route, gtuType, null);
        }

        // link is on the route, but lane markings may still prevent the route from being followed
        Set<LaneStructureRecord> currentSet = new LinkedHashSet<>();
        Set<LaneStructureRecord> nextSet = new LinkedHashSet<>();
        currentSet.add(this);

        boolean firstLoop = true;
        while (!currentSet.isEmpty())
        {

            boolean allCutOff = false;
            if (!firstLoop || end)
            {
                allCutOff = true;
                // move longitudinal
                for (LaneStructureRecord laneRecord : currentSet)
                {
                    allCutOff = allCutOff & laneRecord.isCutOffEnd();

                    to = route.indexOf(laneRecord.getToNode());
                    if (to == route.getNodes().size() - 2)
                    {
                        // check connector
                        for (Link link : laneRecord.getToNode().nextLinks(gtuType, laneRecord.getLane().getParentLink()))
                        {
                            if (link.isConnector())
                            {
                                if ((link.getStartNode().equals(laneRecord.getToNode())
                                        && link.getEndNode().equals(route.destinationNode()))
                                        || (link.getEndNode().equals(laneRecord.getToNode())
                                                && link.getStartNode().equals(route.destinationNode())))
                                {
                                    return true;
                                }
                            }
                        }
                    }

                    for (LaneStructureRecord next : laneRecord.getNext())
                    {
                        if (next.getToNode().equals(route.destinationNode()))
                        {
                            // reached destination, by definition ok
                            return true;
                        }
                        if (route.indexOf(next.getToNode()) == to + 1)
                        {
                            nextSet.add(next);
                        }
                    }
                }
                currentSet = nextSet;
                nextSet = new LinkedHashSet<>();
            }
            firstLoop = false;

            // move lateral
            nextSet.addAll(currentSet);
            for (LaneStructureRecord laneRecord : currentSet)
            {
                while (laneRecord.legalLeft() && !nextSet.contains(laneRecord.getLeft()))
                {
                    nextSet.add(laneRecord.getLeft());
                    laneRecord = laneRecord.getLeft();
                }
            }
            for (LaneStructureRecord laneRecord : currentSet)
            {
                while (laneRecord.legalRight() && !nextSet.contains(laneRecord.getRight()))
                {
                    nextSet.add(laneRecord.getRight());
                    laneRecord = laneRecord.getRight();
                }
            }

            // none of the next lanes was on the route
            if (nextSet.isEmpty())
            {
                // if at the end of the lane structure we are still not able to reach all lanes in a link, assume ok
                return allCutOff;
            }

            // reached a link on the route where all lanes can be reached?
            int nLanesOnNextLink = 0;
            LaneStructureRecord nextRecord = nextSet.iterator().next();
            for (Lane l : nextRecord.getLane().getParentLink().getLanes())
            {
                nLanesOnNextLink++;
            }
            if (nextSet.size() == nLanesOnNextLink)
            {
                // in this case we don't need to look further, anything is possible again
                return true;
            }

            currentSet = nextSet;
            nextSet = new LinkedHashSet<>();

        }

        // never reached our destination or a link with all lanes accessible
        return false;
    }

    /**
     * Returns whether continuing on this lane will allow the route to be followed, while the lane itself is not on the route.
     * @param route Route; the route to follow
     * @param gtuType GtuType; gtu type
     * @param original LaneStructureRecord; source record, should be {@code null} to prevent loop recognition on first iteration
     * @return whether continuing on this lane will allow the route to be followed
     * @throws NetworkException if no destination node
     */
    private boolean leadsToRoute(final Route route, final GtuType gtuType, final LaneStructureRecord original)
            throws NetworkException
    {
        if (original == this)
        {
            return false; // stop loop
        }
        if (original != null && allowsRoute(route, gtuType))
        {
            return true;
        }
        // move downstream until we are at the route
        for (LaneStructureRecord record : getNext())
        {
            boolean leadsTo =
                    ((RollingLaneStructureRecord) record).leadsToRoute(route, gtuType, original == null ? this : original);
            if (leadsTo)
            {
                return true;
            }
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public final RollingLaneStructureRecord getLeft()
    {
        return this.left;
    }

    /**
     * @param leftRecord RollingLaneStructureRecord; set the left LSR or null if not available. Left and right are relative to
     *            the &lt;b&gt;driving&lt;/b&gt; direction.
     * @param gtuType GtuType; GTU type
     */
    public final void setLeft(final RollingLaneStructureRecord leftRecord, final GtuType gtuType)
    {
        this.left = leftRecord;
        this.mayChangeLeft =
                getLane().accessibleAdjacentLanesLegal(LateralDirectionality.LEFT, gtuType).contains(leftRecord.getLane());
        if (getLane().getFullId().equals("1023.FORWARD3") && !this.mayChangeLeft)
        {
            System.out.println("Lane 1023.FORWARD3 allows left:" + this.mayChangeLeft);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final boolean legalLeft()
    {
        return this.mayChangeLeft;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean physicalLeft()
    {
        return this.left != null;
    }

    /** {@inheritDoc} */
    @Override
    public final RollingLaneStructureRecord getRight()
    {
        return this.right;
    }

    /**
     * @param rightRecord RollingLaneStructureRecord; set the right LSR or null if not available. Left and right are relative to
     *            the &lt;b&gt;driving&lt;/b&gt; direction
     * @param gtuType GtuType; GTU type
     */
    public final void setRight(final RollingLaneStructureRecord rightRecord, final GtuType gtuType)
    {
        this.right = rightRecord;
        this.mayChangeRight =
                getLane().accessibleAdjacentLanesLegal(LateralDirectionality.RIGHT, gtuType).contains(rightRecord.getLane());
    }

    /** {@inheritDoc} */
    @Override
    public final boolean legalRight()
    {
        return this.mayChangeRight;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean physicalRight()
    {
        return this.right != null;
    }

    /** {@inheritDoc} */
    @Override
    public final List<RollingLaneStructureRecord> getNext()
    {
        return this.nextList;
    }

    /**
     * Clears the next list.
     */
    final void clearNextList()
    {
        this.nextList.clear();
    }

    /**
     * @param next RollingLaneStructureRecord; a next LSRs to add. Next is relative to the driving direction, not to the design
     *            line direction.
     * @throws GtuException if the records is cut-off at the end
     */
    public final void addNext(final RollingLaneStructureRecord next) throws GtuException
    {
        Throw.when(this.cutOffEnd != null, GtuException.class,
                "Cannot add next records to a record that was cut-off at the end.");
        this.nextList.add(next);
    }

    /** {@inheritDoc} */
    @Override
    public final List<RollingLaneStructureRecord> getPrev()
    {
        return this.prevList;
    }

    /**
     * Clears the prev list.
     */
    final void clearPrevList()
    {
        this.prevList.clear();
    }

    /**
     * @param prev RollingLaneStructureRecord; a previous LSRs to add. Previous is relative to the driving direction, not to the
     *            design line direction.
     * @throws GtuException if the records is cut-off at the start
     */
    public final void addPrev(final RollingLaneStructureRecord prev) throws GtuException
    {
        Throw.when(this.cutOffStart != null, GtuException.class,
                "Cannot add previous records to a record that was cut-off at the start.");
        this.prevList.add(prev);
    }

    /**
     * Sets this record as being cut-off, i.e. there are no next records due to cut-off.
     * @param cutOffEnd Length; where this lane was cut-off (in the driving direction) resulting in no next lanes
     * @throws GtuException if there are next records
     */
    public final void setCutOffEnd(final Length cutOffEnd) throws GtuException
    {
        Throw.when(!this.nextList.isEmpty(), GtuException.class,
                "Setting lane record with cut-off end, but there are next records.");
        this.cutOffEnd = cutOffEnd;
    }

    /**
     * Sets this record as being cut-off, i.e. there are no previous records due to cut-off.
     * @param cutOffStart Length; where this lane was cut-off (in the driving direction) resulting in no prev lanes
     * @throws GtuException if there are previous records
     */
    public final void setCutOffStart(final Length cutOffStart) throws GtuException
    {
        Throw.when(!this.prevList.isEmpty(), GtuException.class,
                "Setting lane record with cut-off start, but there are previous records.");
        this.cutOffStart = cutOffStart;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isCutOffEnd()
    {
        return this.cutOffEnd != null;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isCutOffStart()
    {
        return this.cutOffStart != null;
    }

    /**
     * Returns distance where the structure was cut-off.
     * @return distance where the structure was cut-off
     */
    public final Length getCutOffEnd()
    {
        return this.cutOffEnd;
    }

    /**
     * Returns distance where the structure was cut-off.
     * @return distance where the structure was cut-off
     */
    public final Length getCutOffStart()
    {
        return this.cutOffStart;
    }

    /**
     * Clears the cut-off at the end.
     */
    public final void clearCutOffEnd()
    {
        this.cutOffEnd = null;
    }

    /**
     * Clears the cut-off at the start.
     */
    public final void clearCutOffStart()
    {
        this.cutOffStart = null;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isDeadEnd()
    {
        return this.cutOffEnd == null && this.nextList.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public final Lane getLane()
    {
        return this.lane;
    }

    /** {@inheritDoc} */
    @Override
    public final Length getStartDistance()
    {
        return this.startDistance;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isDownstreamBranch()
    {
        // DOWN, LATERAL_START and CROSS are part of the downstream branch
        return !RecordLink.UP.equals(this.sourceLink) && !RecordLink.LATERAL_END.equals(this.sourceLink);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        // left and right may cause stack overflow
        String s;
        if (this.source == null)
        {
            s = "o";
        }
        else if (this.source == this.left)
        {
            s = "^";
        }
        else if (this.source == this.right)
        {
            s = "v";
        }
        else if (this.prevList.contains(this.source))
        {
            s = "<";
        }
        else if (this.nextList.contains(this.source))
        {
            s = ">";
        }
        else
        {
            s = "?";
        }
        return "LaneStructureRecord [lane=" + this.lane + " (" + s + ")]";
    }

    /**
     * Link between records that defines the dependence of start position and hence how this is updated as the GTU moves.
     * <p>
     * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    public enum RecordLink
    {

        /** This record is upstream of the start distance source. */
        UP
        {
            /** {@inheritDoc} */
            @Override
            public Length calculateStartDistance(final RollingLaneStructureRecord startDistanceSource,
                    final RollingLaneStructureRecord self, final double fractionalPosition)
            {
                return startDistanceSource.getStartDistance().minus(self.getLane().getLength());
            }
        },

        /** This record is downstream of the start distance source. */
        DOWN
        {
            /** {@inheritDoc} */
            @Override
            public Length calculateStartDistance(final RollingLaneStructureRecord startDistanceSource,
                    final RollingLaneStructureRecord self, final double fractionalPosition)
            {
                return startDistanceSource.getStartDistance().plus(startDistanceSource.getLane().getLength());
            }
        },

        /** This record is laterally adjacent to the start distance source, and found in an upstream search. */
        LATERAL_END
        {
            /** {@inheritDoc} */
            @Override
            public Length calculateStartDistance(final RollingLaneStructureRecord startDistanceSource,
                    final RollingLaneStructureRecord self, final double fractionalPosition)
            {
                return startDistanceSource.getStartDistance().plus(startDistanceSource.getLane().getLength())
                        .minus(self.getLane().getLength());
            }
        },

        /** This record is laterally adjacent to the start distance source, and found in a downstream search. */
        LATERAL_START
        {
            /** {@inheritDoc} */
            @Override
            public Length calculateStartDistance(final RollingLaneStructureRecord startDistanceSource,
                    final RollingLaneStructureRecord self, final double fractionalPosition)
            {
                return startDistanceSource.getStartDistance();
            }
        },

        /** Part of the current cross-section. */
        CROSS
        {
            /** {@inheritDoc} */
            @Override
            public Length calculateStartDistance(final RollingLaneStructureRecord startDistanceSource,
                    final RollingLaneStructureRecord self, final double fractionalPosition)
            {
                return self.getLane().getLength().times(fractionalPosition).neg();
            }
        };

        /**
         * Calculate the start position of this record based on a neighboring source.
         * @param startDistanceSource RollingLaneStructureRecord; source record in the tree
         * @param self RollingLaneStructureRecord; own record
         * @param fractionalPosition double; fractional position on the cross-section
         * @return start position of this record based on a neighboring source
         */
        public abstract Length calculateStartDistance(RollingLaneStructureRecord startDistanceSource,
                RollingLaneStructureRecord self, double fractionalPosition);

    }

}
