package org.opentrafficsim.road.gtu.lane.perception;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.network.lane.Lane;

import nl.tudelft.simulation.language.Throw;

/**
 * A LaneStructureRecord contains information about the lanes that can be accessed from this lane by a GTUType. It tells whether
 * there is a left and/or right lane by pointing to other LaneStructureRecords, and which successor LaneStructureRecord(s) there
 * are at the end of the lane of this LaneStructureRecord. All information (left, right, next) is calculated relative to the
 * driving direction of the GTU that owns this structure.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Feb 21, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class LaneStructureRecord implements LaneRecord<LaneStructureRecord>, Serializable
{
    /** */
    private static final long serialVersionUID = 20160400L;

    /** Cache of allows route information. */
    // TODO clear on network change, with an event listener?
    private static Map<Lane, Map<Route, Map<GTUType, Map<Boolean, Boolean>>>> allowsRouteCache = new HashMap<>();

    /** The lane of the LSR. */
    private final Lane lane;

    /** The direction in which we process this lane. */
    private final GTUDirectionality gtuDirectionality;

    /** The left LSR or null if not available. Left and right are relative to the <b>driving</b> direction. */
    private LaneStructureRecord left;

    /** Legal left lane change possibility. */
    private boolean mayChangeLeft;

    /** The right LSR or null if not available. Left and right are relative to the <b>driving</b> direction. */
    private LaneStructureRecord right;

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
    private List<LaneStructureRecord> nextList = new ArrayList<>();

    /**
     * The previous LSRs. The list is empty if no LSRs are available. Previous is relative to the driving direction, not to the
     * design line direction.
     */
    private List<LaneStructureRecord> prevList = new ArrayList<>();

    /** Record who's start start distance is used to calculate the start distance of this record. */
    private LaneStructureRecord source;

    /** Start distance link between records. */
    private RecordLink sourceLink;

    /** Set of records who's starting position depends on this record. */
    private final Set<LaneStructureRecord> dependentRecords = new LinkedHashSet<>();

    /**
     * Constructor.
     * @param lane Lane; lane
     * @param direction GTUDirectionality; direction of travel for the GTU
     * @param startDistanceSource LaneStructureRecord; record on which the start distance is based
     * @param recordLink RecordLink; link type to source
     */
    public LaneStructureRecord(final Lane lane, final GTUDirectionality direction,
            final LaneStructureRecord startDistanceSource, final RecordLink recordLink)
    {
        this.lane = lane;
        this.gtuDirectionality = direction;
        this.source = startDistanceSource;
        this.sourceLink = recordLink;
        if (startDistanceSource != null)
        {
            startDistanceSource.dependentRecords.add(this);
        }
    }

    /**
     * @param lane the lane of the LSR
     * @param direction the direction on which we process this lane
     * @param startDistance distance to start of the record, negative for backwards
     */
    public LaneStructureRecord(final Lane lane, final GTUDirectionality direction, final Length startDistance)
    {
        this.lane = lane;
        this.gtuDirectionality = direction;
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
     * @param startDistanceSource LaneStructureRecord; record on which the start distance is based
     * @param recordLink RecordLink; link type to source
     */
    void changeStartDistanceSource(final LaneStructureRecord startDistanceSource, final RecordLink recordLink)
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
     * @param laneStructure LaneStructure; parent lane structure
     */
    final void updateStartDistance(final double fractionalPosition, final LaneStructure laneStructure)
    {
        this.startDistance = this.sourceLink.calculateStartDistance(this.source, this, fractionalPosition);
        if (this.startDistance.si < 0.0 && this.startDistance.si + this.lane.getLength().si > 0.0)
        {
            laneStructure.addToCrossSection(this);
        }
        for (LaneStructureRecord record : this.dependentRecords)
        {
            record.updateStartDistance(fractionalPosition, laneStructure);
        }
    }

    /**
     * Returns the source of the start distance.
     * @return LaneStructureRecord; source of the start distance
     */
    final LaneStructureRecord getStartDistanceSource()
    {
        return this.source;
    }

    /**
     * @return the 'from' node of the link belonging to this lane, in the driving direction.
     */
    public final Node getFromNode()
    {
        return this.gtuDirectionality.isPlus() ? this.lane.getParentLink().getStartNode()
                : this.lane.getParentLink().getEndNode();
    }

    /**
     * @return the 'to' node of the link belonging to this lane, in the driving direction.
     */
    public final Node getToNode()
    {
        return this.gtuDirectionality.isPlus() ? this.lane.getParentLink().getEndNode()
                : this.lane.getParentLink().getStartNode();
    }

    /**
     * @return whether the link to which this lane belongs splits, i.e. some of the parallel, connected lanes lead to a
     *         different destination than others
     */
    public final boolean isLinkSplit()
    {
        if (isCutOffEnd())
        {
            // if the end is a split, it's out of range
            return false;
        }
        Set<Node> toNodes = new HashSet<>();
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
        Set<Node> fromNodes = new HashSet<>();
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

    /**
     * Returns whether this lane allows the route to be followed.
     * @param route Route; the route to follow
     * @param gtuType GTUType; gtu type
     * @return whether this lane allows the route to be followed
     * @throws NetworkException if no destination node
     */
    public final boolean allowsRoute(final Route route, final GTUType gtuType) throws NetworkException
    {
        return allowsRoute(route, gtuType, false);
    }

    /**
     * Returns whether the end of this lane allows the route to be followed.
     * @param route Route; the route to follow
     * @param gtuType GTUType; gtu type
     * @return whether the end of this lane allows the route to be followed
     * @throws NetworkException if no destination node
     */
    public final boolean allowsRouteAtEnd(final Route route, final GTUType gtuType) throws NetworkException
    {
        return allowsRoute(route, gtuType, true);
    }

    /**
     * Returns whether (the end of) this lane allows the route to be followed, using caching.
     * @param route Route; the route to follow
     * @param gtuType GTUType; gtu type
     * @param end boolean; whether to consider the end (or otherwise the lane itself, i.e. allow lane change from this lane)
     * @return whether the end of this lane allows the route to be followed
     * @throws NetworkException if no destination node
     */
    private boolean allowsRoute(final Route route, final GTUType gtuType, final boolean end) throws NetworkException
    {
        Map<Route, Map<GTUType, Map<Boolean, Boolean>>> map1 = allowsRouteCache.get(this.lane);
        if (map1 == null)
        {
            map1 = new HashMap<>();
            allowsRouteCache.put(this.lane, map1);
        }
        Map<GTUType, Map<Boolean, Boolean>> map2 = map1.get(route);
        if (map2 == null)
        {
            map2 = new HashMap<>();
            map1.put(route, map2);
        }
        Map<Boolean, Boolean> map3 = map2.get(gtuType);
        if (map3 == null)
        {
            map3 = new HashMap<>();
            map2.put(gtuType, map3);
        }
        Boolean allows = map3.get(end);
        if (allows == null)
        {
            allows = allowsRoute0(route, gtuType, end);
            map3.put(end, allows);
        }
        return allows;
    }

    /**
     * Returns whether (the end of) this lane allows the route to be followed.
     * @param route Route; the route to follow
     * @param gtuType GTUType; gtu type
     * @param end boolean; whether to consider the end (or otherwise the lane itself, i.e. allow lane change from this lane)
     * @return whether the end of this lane allows the route to be followed
     * @throws NetworkException if no destination node
     */
    private boolean allowsRoute0(final Route route, final GTUType gtuType, final boolean end) throws NetworkException
    {

        // driving without route
        if (route == null)
        {
            return true;
        }

        // start with simple check
        int from = route.indexOf(this.getFromNode());
        int to = route.indexOf(this.getToNode());
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

            if (!firstLoop || end)
            {
                // move longitudinal
                for (LaneStructureRecord laneRecord : currentSet)
                {
                    to = route.indexOf(laneRecord.getToNode());
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
                return false;
            }

            // reached a link on the route where all lanes can be reached?
            int nLanesOnNextLink = 0;
            LaneStructureRecord nextRecord = nextSet.iterator().next();
            for (Lane l : nextRecord.getLane().getParentLink().getLanes())
            {
                if (l.getLaneType().getDirectionality(gtuType).getDirectionalities().contains(nextRecord.getDirection()))
                {
                    nLanesOnNextLink++;
                }
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
     * @param gtuType GTUType; gtu type
     * @param original LaneStructureRecord; source record, should be {@code null} to prevent loop recognition on first iteration
     * @return whether continuing on this lane will allow the route to be followed
     * @throws NetworkException if no destination node
     */
    private boolean leadsToRoute(final Route route, final GTUType gtuType, final LaneStructureRecord original)
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
            boolean leadsTo = record.leadsToRoute(route, gtuType, original == null ? this : original);
            if (leadsTo)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * @return the left LSR or null if not available. Left and right are relative to the <b>driving</b> direction.
     */
    public final LaneStructureRecord getLeft()
    {
        return this.left;
    }

    /**
     * @param left set the left LSR or null if not available. Left and right are relative to the <b>driving</b> direction.
     * @param gtuType GTU type
     */
    public final void setLeft(final LaneStructureRecord left, final GTUType gtuType)
    {
        this.left = left;
        this.mayChangeLeft = getLane().accessibleAdjacentLanesLegal(LateralDirectionality.LEFT, gtuType, this.gtuDirectionality)
                .contains(left.getLane());
    }

    /**
     * Returns whether a left lane change is legal.
     * @return whether a left lane change is legal
     */
    public final boolean legalLeft()
    {
        return this.mayChangeLeft;
    }

    /**
     * Returns whether a left lane change is physically possible.
     * @return whether a left lane change is physically possible
     */
    public final boolean physicalLeft()
    {
        return this.left != null;
    }

    /**
     * @return the right LSR or null if not available. Left and right are relative to the <b>driving</b> direction
     */
    public final LaneStructureRecord getRight()
    {
        return this.right;
    }

    /**
     * @param right set the right LSR or null if not available. Left and right are relative to the <b>driving</b> direction
     * @param gtuType GTU type
     */
    public final void setRight(final LaneStructureRecord right, final GTUType gtuType)
    {
        this.right = right;
        this.mayChangeRight =
                getLane().accessibleAdjacentLanesLegal(LateralDirectionality.RIGHT, gtuType, this.gtuDirectionality)
                        .contains(right.getLane());
    }

    /**
     * Returns whether a right lane change is legal.
     * @return whether a right lane change is legal
     */
    public final boolean legalRight()
    {
        return this.mayChangeRight;
    }

    /**
     * Returns whether a right lane change is physically possible.
     * @return whether a right lane change is physically possible
     */
    public final boolean physicalRight()
    {
        return this.right != null;
    }

    /** {@inheritDoc} */
    @Override
    public final List<LaneStructureRecord> getNext()
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
     * @param next a next LSRs to add. Next is relative to the driving direction, not to the design line direction.
     * @throws GTUException if the records is cut-off at the end
     */
    public final void addNext(final LaneStructureRecord next) throws GTUException
    {
        Throw.when(this.cutOffEnd != null, GTUException.class,
                "Cannot add next records to a record that was cut-off at the end.");
        this.nextList.add(next);
    }

    /** {@inheritDoc} */
    @Override
    public final List<LaneStructureRecord> getPrev()
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
     * @param prev a previous LSRs to add. Previous is relative to the driving direction, not to the design line direction.
     * @throws GTUException if the records is cut-off at the start
     */
    public final void addPrev(final LaneStructureRecord prev) throws GTUException
    {
        Throw.when(this.cutOffStart != null, GTUException.class,
                "Cannot add previous records to a record that was cut-off at the start.");
        this.prevList.add(prev);
    }

    /**
     * Sets this record as being cut-off, i.e. there are no next records due to cut-off.
     * @param cutOffEnd where this lane was cut-off (in the driving direction) resulting in no next lanes
     * @throws GTUException if there are next records
     */
    public final void setCutOffEnd(final Length cutOffEnd) throws GTUException
    {
        Throw.when(!this.nextList.isEmpty(), GTUException.class,
                "Setting lane record with cut-off end, but there are next records.");
        this.cutOffEnd = cutOffEnd;
    }

    /**
     * Sets this record as being cut-off, i.e. there are no previous records due to cut-off.
     * @param cutOffStart where this lane was cut-off (in the driving direction) resulting in no prev lanes
     * @throws GTUException if there are previous records
     */
    public final void setCutOffStart(final Length cutOffStart) throws GTUException
    {
        Throw.when(!this.prevList.isEmpty(), GTUException.class,
                "Setting lane record with cut-off start, but there are previous records.");
        this.cutOffStart = cutOffStart;
    }

    /**
     * Returns whether this lane has no next records as the lane structure was cut-off.
     * @return whether this lane has no next records as the lane structure was cut-off
     */
    public final boolean isCutOffEnd()
    {
        return this.cutOffEnd != null;
    }

    /**
     * Returns whether this lane has no previous records as the lane structure was cut-off.
     * @return whether this lane has no previous records as the lane structure was cut-off
     */
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

    /**
     * Returns whether the record forms a dead-end.
     * @return whether the record forms a dead-end
     */
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
    public final GTUDirectionality getDirection()
    {
        return this.gtuDirectionality;
    }

    /** {@inheritDoc} */
    @Override
    public final Length getStartDistance()
    {
        return this.startDistance;
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
        return "LaneStructureRecord [lane=" + this.lane + " (" + s + "), direction=" + this.gtuDirectionality + "]";
    }

    /**
     * Link between records that defines the dependence of start position and hence how this is updated as the GTU moves.
     * <p>
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 22 jan. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    public enum RecordLink
    {

        /** This record is upstream of the start distance source. */
        UP
        {
            /** {@inheritDoc} */
            @Override
            public Length calculateStartDistance(final LaneStructureRecord startDistanceSource, final LaneStructureRecord self,
                    final double fractionalPosition)
            {
                return startDistanceSource.getStartDistance().minus(self.getLane().getLength());
            }
        },

        /** This record is downstream of the start distance source. */
        DOWN
        {
            /** {@inheritDoc} */
            @Override
            public Length calculateStartDistance(final LaneStructureRecord startDistanceSource, final LaneStructureRecord self,
                    final double fractionalPosition)
            {
                return startDistanceSource.getStartDistance().plus(startDistanceSource.getLane().getLength());
            }
        },

        /** This record is laterally adjacent to the start distance source, and found in an upstream search. */
        LATERAL_END
        {
            /** {@inheritDoc} */
            @Override
            public Length calculateStartDistance(final LaneStructureRecord startDistanceSource, final LaneStructureRecord self,
                    final double fractionalPosition)
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
            public Length calculateStartDistance(final LaneStructureRecord startDistanceSource, final LaneStructureRecord self,
                    final double fractionalPosition)
            {
                return startDistanceSource.getStartDistance();
            }
        },

        /** Part of the current cross-section. */
        CROSS
        {
            /** {@inheritDoc} */
            @Override
            public Length calculateStartDistance(final LaneStructureRecord startDistanceSource, final LaneStructureRecord self,
                    final double fractionalPosition)
            {
                return self.getLane().getLength().multiplyBy(fractionalPosition).neg();
            }
        };

        /**
         * Calculate the start position of this record based on a neighboring source.
         * @param startDistanceSource LaneStructureRecord; source record in the tree
         * @param self LaneStructureRecord; own record
         * @param fractionalPosition double; fractional position on the cross-section
         * @return start position of this record based on a neighboring source
         */
        public abstract Length calculateStartDistance(LaneStructureRecord startDistanceSource, LaneStructureRecord self,
                double fractionalPosition);

    }

}
