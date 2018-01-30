package org.opentrafficsim.road.gtu.lane.perception;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.WeakHashMap;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.LaneStructureRecord.RecordLink;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.object.LaneBasedObject;

import nl.tudelft.simulation.language.Throw;

/**
 * This data structure can clearly indicate the lane structure ahead of us, e.g. in the following situation:
 * 
 * <pre>
 *     (---- a ----)(---- b ----)(---- c ----)(---- d ----)(---- e ----)(---- f ----)(---- g ----)  
 *                                             __________                             __________
 *                                            / _________ 1                          / _________ 2
 *                                           / /                                    / /
 *                                __________/ /             _______________________/ /
 *  1  ____________ ____________ /_ _ _ _ _ _/____________ /_ _ _ _ _ _ _ _ _ _ _ _ /      
 *  0 |_ _X_ _ _ _ |_ _ _ _ _ _ |_ _ _ _ _ _ |_ _ _ _ _ _ |_ _ _ _ _ _ |_ _ _ _ _ _ \____________
 * -1 |____________|_ _ _ _ _ _ |____________|____________|  __________|____________|____________| 3
 * -2              / __________/                           \ \  
 *        ________/ /                                       \ \___________  
 *      5 _________/                                         \____________  4
 * </pre>
 * 
 * When the GTU is looking ahead, it needs to know that when it continues to destination 3, it needs to shift one lane to the
 * right at some point, but <b>not</b> two lanes to the right in link b, and not later than at the end of link f. When it needs
 * to go to destination 1, it needs to shift to the left in link c. When it has to go to destination 2, it has to shift to the
 * left, but not earlier than at link e. At node [de], it is possible to leave the rightmost lane of link e, and go to
 * destination 4. The rightmost lane just splits into two lanes at the end of link d, and the GTU can either continue driving to
 * destination 3, turn right to destination 4. This means that the right lane of link d has <b>two</b> successor lanes.
 * <p>
 * In the data structures, lanes are numbered laterally. Suppose that the lane where vehicle X resides would be number 0.
 * Consistent with "left is positive" for angles, the lane right of X would have number -1, and entry 5 would have number -2.
 * <p>
 * In the data structure, this can be indicated as follows (N = next, P = previous, L = left, R = right, D = lane drop, . =
 * continued but not in this structure). The merge lane in b is considered "off limits" for the GTUs on the "main" lane -1; the
 * "main" lane 0 is considered off limits from the exit lanes on c, e, and f. Still, we need to maintain pointers to these
 * lanes, as we are interested in the GTUs potentially driving next to us, feeding into our lane, etc.
 * 
 * <pre>
 *       1                0               -1               -2
 *       
 *                       ROOT 
 *                   _____|_____      ___________      ___________            
 *                  |_-_|_._|_R_|----|_L_|_._|_-_|    |_-_|_._|_-_|  a           
 *                        |                |                |
 *                   _____V_____      _____V_____      _____V_____            
 *                  |_-_|_N_|_R_|----|_L_|_N_|_R_|&lt;---|_L_|_D_|_-_|  b           
 *                        |                |                 
 *  ___________      _____V_____      _____V_____                 
 * |_-_|_N_|_R_|&lt;---|_L_|_N_|_R_|----|_L_|_N_|_-_|                   c
 *       |                |                |                 
 *  _____V_____      _____V_____      _____V_____                 
 * |_-_|_._|_-_|    |_-_|_N_|_R_|----|_L_|_NN|_-_|                   d          
 *                        |                ||_______________ 
 *  ___________      _____V_____      _____V_____      _____V_____            
 * |_-_|_N_|_R_|&lt;---|_L_|_N_|_R_|----|_L_|_N_|_-_|    |_-_|_N_|_-_|  e          
 *       |                |                |                |
 *  _____V_____      _____V_____      _____V_____      _____V_____            
 * |_-_|_N_|_R_|&lt;---|_L_|_D_|_R_|----|_L_|_N_|_-_|    |_-_|_._|_-_|  f          
 *       |                                 |                 
 *  _____V_____                       _____V_____                             
 * |_-_|_._|_-_|                     |_-_|_._|_-_|                   g
 * </pre>
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Feb 20, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class LaneStructure implements Serializable
{
    /** */
    private static final long serialVersionUID = 20160400L;

    /** The lanes from which we observe the situation. */
    private LaneStructureRecord root;

    /** Look ahead distance. */
    private Length lookAhead;

    /** Route the structure is based on. */
    private Route previousRoute;

    /** Lane structure records of the cross section. */
    private TreeMap<RelativeLane, LaneStructureRecord> crossSectionRecords = new TreeMap<>();

    /** Lane structure records grouped per relative lane. */
    private Map<RelativeLane, Set<LaneStructureRecord>> relativeLaneMap = new HashMap<>();

    /** Relative lanes storage per record, such that other records can be linked to the correct relative lane. */
    private Map<LaneStructureRecord, RelativeLane> relativeLanes = new HashMap<>();

    /** Set of lanes that can be ignored as they are beyond build bounds. */
    private final Set<Lane> ignoreSet = new HashSet<>();

    /** Upstream edges. */
    private Set<LaneStructureRecord> upstreamEdge = new LinkedHashSet<>();

    /** Downstream edges. */
    private Set<LaneStructureRecord> downstreamEdge = new LinkedHashSet<>();

    /** Downstream distance over which the structure is made. */
    private final Length down;

    /** Upstream distance over which the structure is made. */
    private final Length up;

    /** Downstream distance at splits (links not on route) included in the structure. */
    private final Length downSplit;

    /** Upstream distance at downstream merges (links not on route) included in the structure. */
    private final Length upMerge;

    /**
     * @param rootLSR the root record.
     * @param lookAhead look ahead distance
     */
    @Deprecated
    public LaneStructure(final LaneStructureRecord rootLSR, final Length lookAhead)
    {
        this.root = rootLSR;
        this.lookAhead = lookAhead;
        this.down = null;
        this.up = null;
        this.downSplit = null;
        this.upMerge = null;
    }

    /**
     * Constructor.
     * @param lookAhead Length; distance over which visual objects are included
     * @param down Length; downstream distance over which the structure is made
     * @param up Length; upstream distance over which the structure is made
     * @param downSplit Length; downstream distance at splits (links not on route) included in the structure
     * @param upMerge Length; upstream distance at downstream merges (links not on route) included in the structure
     */
    public LaneStructure(Length lookAhead, Length down, Length up, Length downSplit, Length upMerge)
    {
        this.lookAhead = lookAhead;
        this.down = down;
        this.up = up;
        this.downSplit = downSplit;
        this.upMerge = upMerge;
    }

    /**
     * Updates the underlying structure shifting the root position to the input.
     * @param pos DirectedLanePosition; current position of the GTU
     * @param route Route; current route of the GTU
     * @param gtuType GTUType; GTU type
     * @throws GTUException on a problem while updating the structure
     */
    public final void update(final DirectedLanePosition pos, final Route route, final GTUType gtuType) throws GTUException
    {
        /*
         * Implementation note: the LaneStructure was previously generated by AbstractLanePerception every time step. This
         * functionality has been moved to LaneStructure itself, in a manner that can update the LaneStructure. Start distances
         * of individual records are therefore made dynamic, calculated relative to a neighboring source record. For many time
         * steps this means that only these distances have to be updated. In other cases, the sources for start distances are
         * changed concerning the records that were involved in the previous time step. The LaneStructure now also maintains an
         * upstream and a downstream edge, i.e. set of records. These are moved forward as the GTU moves.
         */

        // fractional position
        Lane lane = pos.getLane();
        GTUDirectionality direction = pos.getGtuDirection();
        Length position = pos.getPosition();
        double fracPos = direction.isPlus() ? position.si / lane.getLength().si : 1.0 - position.si / lane.getLength().si;

        if (this.previousRoute != route || this.root == null)
        {
            // create new LaneStructure
            this.previousRoute = route;

            // clear
            this.upstreamEdge.clear();
            this.downstreamEdge.clear();
            this.crossSectionRecords.clear();
            this.relativeLanes.clear();

            // build cross-section
            this.root = constructRecord(lane, direction, null, RecordLink.CROSS, RelativeLane.CURRENT);
            this.crossSectionRecords.put(RelativeLane.CURRENT, this.root);
            Set<LaneStructureRecord> set = new LinkedHashSet<>();
            set.add(this.root);
            for (LateralDirectionality latDirection : new LateralDirectionality[] { LateralDirectionality.LEFT,
                    LateralDirectionality.RIGHT })
            {
                LaneStructureRecord current = this.root;
                RelativeLane relativeLane = RelativeLane.CURRENT;
                Set<Lane> adjacentLanes =
                        current.getLane().accessibleAdjacentLanes(latDirection, gtuType, current.getDirection());
                while (!adjacentLanes.isEmpty())
                {
                    Throw.when(adjacentLanes.size() > 1, RuntimeException.class,
                            "Multiple adjacent lanes encountered during construction of lane map.");
                    relativeLane = latDirection.isLeft() ? relativeLane.getLeft() : relativeLane.getRight();
                    Lane adjacentLane = adjacentLanes.iterator().next();
                    LaneStructureRecord adjacentRecord =
                            constructRecord(adjacentLane, direction, current, RecordLink.CROSS, relativeLane);
                    this.crossSectionRecords.put(relativeLane, adjacentRecord);
                    set.add(adjacentRecord);
                    if (latDirection.isLeft())
                    {
                        if (adjacentLane.accessibleAdjacentLanes(LateralDirectionality.RIGHT, gtuType, current.getDirection())
                                .contains(current.getLane()))
                        {
                            adjacentRecord.setRight(current);
                        }
                        current.setLeft(adjacentRecord);
                    }
                    else
                    {
                        if (adjacentLane.accessibleAdjacentLanes(LateralDirectionality.LEFT, gtuType, current.getDirection())
                                .contains(current.getLane()))
                        {
                            adjacentRecord.setLeft(current);
                        }
                        current.setRight(adjacentRecord);
                    }
                    current = adjacentRecord;
                    adjacentLanes = current.getLane().accessibleAdjacentLanes(latDirection, gtuType, current.getDirection());
                }
            }
            this.upstreamEdge.addAll(set);
            this.downstreamEdge.addAll(set);

            // set the start distances so the upstream expand can work
            this.root.updateStartDistance(fracPos, this);

            // expand upstream edge
            expandUpstreamEdge(gtuType, fracPos);
        }
        else
        {
            // update LaneStructure
            if (!lane.equals(this.root.getLane()))
            {
                // find the root, and possible lateral shift if changed lane
                LaneStructureRecord newRoot = null;
                RelativeLane lateralMove = null;
                for (RelativeLane relativeLane : this.relativeLaneMap.keySet())
                {
                    if (newRoot != null)
                    {
                        break;
                    }
                    for (LaneStructureRecord record : this.relativeLaneMap.get(relativeLane))
                    {
                        if (record.getLane().equals(lane))
                        {
                            newRoot = record;
                            lateralMove = relativeLane;
                            break;
                        }
                    }
                }
                this.root = newRoot;

                // update start distance sources
                updateStartDistanceSources();

                // shift if changed lane
                if (!lateralMove.isCurrent())
                {
                    LateralDirectionality dir = lateralMove.getLateralDirectionality().isRight() ? LateralDirectionality.LEFT
                            : LateralDirectionality.RIGHT;
                    RelativeLane delta = new RelativeLane(dir, lateralMove.getNumLanes()); // flipped

                    TreeMap<RelativeLane, LaneStructureRecord> newCrossSectionRecords = new TreeMap<>();
                    for (RelativeLane relativeLane : this.crossSectionRecords.keySet())
                    {
                        RelativeLane newRelativeLane = relativeLane.add(delta);
                        newCrossSectionRecords.put(newRelativeLane, this.crossSectionRecords.get(relativeLane));
                    }
                    this.crossSectionRecords = newCrossSectionRecords;

                    Map<RelativeLane, Set<LaneStructureRecord>> newRelativeLaneMap = new HashMap<>();
                    for (RelativeLane relativeLane : this.relativeLaneMap.keySet())
                    {
                        RelativeLane newRelativeLane = relativeLane.add(delta);
                        newRelativeLaneMap.put(newRelativeLane, this.relativeLaneMap.get(relativeLane));
                    }
                    this.relativeLaneMap = newRelativeLaneMap;

                    Map<LaneStructureRecord, RelativeLane> newRelativeLanes = new HashMap<>();
                    for (LaneStructureRecord record : this.relativeLanes.keySet())
                    {
                        newRelativeLanes.put(record, this.relativeLanes.get(record).add(delta));
                    }
                    this.relativeLanes = newRelativeLanes;
                }
            }

            // set the start distances so the edge updates can work, first remove cross-section records not on the current link
            Iterator<RelativeLane> iterator = this.crossSectionRecords.keySet().iterator();
            while (iterator.hasNext())
            {
                RelativeLane relativeLane = iterator.next();
                LaneStructureRecord record = this.crossSectionRecords.get(relativeLane);
                if (!record.getLane().getParentLink().equals(this.root.getLane().getParentLink()))
                {
                    iterator.remove();
                }
            }
            this.root.updateStartDistance(fracPos, this);

            // update upstream edges
            retreatUpstreamEdge();

        }

        // update downstream edges
        expandDownstreamEdge(gtuType, fracPos, route);
    }

    /**
     * Upstream algorithm from the new source, where records along the way are assigned a new start distance source. These
     * records were downstream in the previous time step, but are now upstream. Hence, their start distance source should now
     * become their downstream record. This algorithm acts like an upstream tree, where each branch is stopped if there are no
     * upstream records, or the upstream records already have their downstream record as source (i.e. the record was already
     * upstream in the previous time step.
     */
    private void updateStartDistanceSources()
    {
        // initial cross section
        Set<LaneStructureRecord> set = new HashSet<>();
        set.add(this.root);
        this.root.changeStartDistanceSource(null, RecordLink.CROSS);
        LaneStructureRecord prev = this.root;
        LaneStructureRecord next = prev.getLeft();
        while (next != null)
        {
            set.add(next);
            next.changeStartDistanceSource(prev, RecordLink.CROSS);
            prev = next;
            next = next.getLeft();
        }
        prev = this.root;
        next = prev.getRight();
        while (next != null)
        {
            set.add(next);
            next.changeStartDistanceSource(prev, RecordLink.CROSS);
            prev = next;
            next = next.getRight();
        }
        // tree algorithm (branches flattened to a single set)
        while (!set.isEmpty())
        {
            // lateral
            Set<LaneStructureRecord> newSet = new HashSet<>();
            for (LaneStructureRecord record : set)
            {
                for (LateralDirectionality latDirection : new LateralDirectionality[] { LateralDirectionality.LEFT,
                        LateralDirectionality.RIGHT })
                {
                    prev = record;
                    next = latDirection.isLeft() ? record.getLeft() : record.getRight();
                    while (next != null && !set.contains(next))
                    {
                        next.changeStartDistanceSource(prev, RecordLink.LATERAL_END);
                        removeDownstream(next); // split not taken can be thrown away
                        newSet.add(next);
                        prev = next;
                        next = latDirection.isLeft() ? next.getLeft() : next.getRight();
                    }
                }
            }
            set.addAll(newSet);

            // longitudinal
            newSet.clear();
            for (LaneStructureRecord record : set)
            {
                for (LaneStructureRecord prevRecord : record.getPrev())
                {
                    LaneStructureRecord source = prevRecord.getStartDistanceSource();
                    if (source == null || (source != null && !source.equals(record)))
                    {
                        prevRecord.changeStartDistanceSource(record, RecordLink.UP);
                        newSet.add(prevRecord);
                    }
                }
            }

            // next loop
            set = newSet;
        }
    }

    /**
     * Removes all records downstream of the given record from underlying data structures.
     * @param record LaneStructureRecord; record, downstream of which to remove all records
     */
    private void removeDownstream(final LaneStructureRecord record)
    {
        for (LaneStructureRecord next : record.getNext())
        {
            next.changeStartDistanceSource(null, null);
            removeDownstream(next);
            removeRecord(next);
        }
        record.clearNextList();
    }

    /**
     * Removes the record from underlying data structures.
     * @param record LaneStructureRecord; record to remove
     */
    private void removeRecord(final LaneStructureRecord record)
    {
        this.relativeLaneMap.get(this.relativeLanes.get(record)).remove(record);
        this.relativeLanes.remove(record);
    }

    /**
     * On a new build, this method is used to create the upstream map.
     * @param gtuType GTUType; GTU type
     * @param fractionalPosition double; fractional position on reference link
     * @throws GTUException on exception
     */
    private void expandUpstreamEdge(final GTUType gtuType, final double fractionalPosition) throws GTUException
    {
        this.ignoreSet.clear();
        Set<LaneStructureRecord> nextSet = new LinkedHashSet<>();
        boolean expand = true;
        while (expand)
        {
            expand = false;

            // longitudinal
            Iterator<LaneStructureRecord> iterator = this.upstreamEdge.iterator();
            while (iterator.hasNext())
            {
                LaneStructureRecord prev = iterator.next();
                Map<Lane, GTUDirectionality> nexts = prev.getLane().upstreamLanes(prev.getDirection(), gtuType);
                if (prev.getStartDistance().si < this.up.si)
                {
                    // upstream search ends on this lane
                    prev.setCutOffStart(this.up.minus(prev.getStartDistance()));
                    for (Lane prevLane : nexts.keySet())
                    {
                        this.ignoreSet.add(prevLane); // exclude in lateral search
                    }
                }
                else
                {
                    // upstream search goes further upstream
                    prev.clearCutOffStart();
                    if (!nexts.isEmpty())
                    {
                        iterator.remove();
                        for (Lane prevLane : nexts.keySet())
                        {
                            RelativeLane relativeLane = this.relativeLanes.get(prev);
                            LaneStructureRecord next =
                                    constructRecord(prevLane, nexts.get(prevLane), prev, RecordLink.UP, relativeLane);
                            next.updateStartDistance(fractionalPosition, this);
                            connectLaterally(next, gtuType);
                            next.addNext(prev);
                            prev.addPrev(next);
                            nextSet.add(next);
                        }
                    }
                }
            }
            this.upstreamEdge.addAll(nextSet);
            expand |= !nextSet.isEmpty();
            nextSet.clear();

            // lateral
            Set<LaneStructureRecord> lateralSet =
                    expandLateral(this.upstreamEdge, RecordLink.LATERAL_END, gtuType, fractionalPosition);
            nextSet.addAll(lateralSet);

            // next iteration
            this.upstreamEdge.addAll(nextSet);
            expand |= !nextSet.isEmpty();
            nextSet.clear();
        }
    }

    /**
     * Helper method for upstream and downstream expansion. This method returns all lanes that can be laterally found from the
     * input set.
     * @param edge Set&lt;LaneStructureRecord&gt;; input set
     * @param recordLink RecordLink; link to add between lateral records, depends on upstream or downstream search
     * @param gtuType GTUType; GTU type
     * @param fractionalPosition double; fractional position on reference link
     * @return Set&lt;LaneStructureRecord&gt;; output set with all laterally found lanes
     */
    private Set<LaneStructureRecord> expandLateral(final Set<LaneStructureRecord> edge, final RecordLink recordLink,
            final GTUType gtuType, final double fractionalPosition)
    {
        Set<LaneStructureRecord> nextSet = new LinkedHashSet<>();
        Set<Lane> laneSet = new HashSet<>(); // set to check that an adjacent lane is not another lane already in the set
        for (LaneStructureRecord record : edge)
        {
            laneSet.add(record.getLane());
        }
        Iterator<LaneStructureRecord> iterator = edge.iterator();
        while (iterator.hasNext())
        {
            LaneStructureRecord record = iterator.next();
            for (LateralDirectionality latDirection : new LateralDirectionality[] { LateralDirectionality.LEFT,
                    LateralDirectionality.RIGHT })
            {
                RelativeLane relativeLane = this.relativeLanes.get(record);
                LaneStructureRecord prev = record;
                Set<Lane> adjacentLanes = prev.getLane().accessibleAdjacentLanes(latDirection, gtuType, prev.getDirection());
                while (!adjacentLanes.isEmpty())
                {
                    Throw.when(adjacentLanes.size() > 1, RuntimeException.class,
                            "Multiple adjacent lanes encountered during construction of lane map.");
                    relativeLane = latDirection.isLeft() ? relativeLane.getLeft() : relativeLane.getRight();
                    Lane nextLane = adjacentLanes.iterator().next();
                    if (!laneSet.contains(nextLane) && !this.ignoreSet.contains(nextLane))
                    {
                        LaneStructureRecord next =
                                constructRecord(nextLane, record.getDirection(), prev, recordLink, relativeLane);
                        next.updateStartDistance(fractionalPosition, this);
                        nextSet.add(next);
                        laneSet.add(nextLane);
                        if (latDirection.isLeft())
                        {
                            prev.setLeft(next);
                            if (nextLane.accessibleAdjacentLanes(LateralDirectionality.RIGHT, gtuType, prev.getDirection())
                                    .contains(prev.getLane()))
                            {
                                next.setRight(prev);
                            }
                        }
                        else
                        {
                            prev.setRight(next);
                            if (nextLane.accessibleAdjacentLanes(LateralDirectionality.LEFT, gtuType, prev.getDirection())
                                    .contains(prev.getLane()))
                            {
                                next.setLeft(prev);
                            }
                        }
                        prev = next;
                        adjacentLanes = prev.getLane().accessibleAdjacentLanes(latDirection, gtuType, prev.getDirection());
                    }
                    else
                    {
                        break;
                    }
                }
            }
        }
        return nextSet;
    }

    /**
     * This method makes sure that not all history is maintained forever, and the upstream edge moves with the GTU.
     * @throws GTUException on exception
     */
    private void retreatUpstreamEdge() throws GTUException
    {
        boolean moved = true;
        Set<LaneStructureRecord> nexts = new LinkedHashSet<>();
        while (moved)
        {
            moved = false;
            nexts.clear();
            Iterator<LaneStructureRecord> iterator = this.upstreamEdge.iterator();
            while (iterator.hasNext())
            {
                LaneStructureRecord prev = iterator.next();
                if (prev.getStartDistance().si + prev.getLane().getLength().si < this.up.si)
                {
                    for (LaneStructureRecord next : prev.getNext())
                    {
                        next.clearPrevList();
                        next.setCutOffStart(this.up.minus(next.getStartDistance()));
                        moved = true;
                        nexts.add(next);
                    }
                    prev.clearNextList();
                    removeRecord(prev);
                    iterator.remove();
                }
            }
            this.upstreamEdge.addAll(nexts);

            // check adjacent lanes
            for (LaneStructureRecord record : nexts)
            {
                LaneStructureRecord prev = record;
                for (LateralDirectionality latDirection : new LateralDirectionality[] { LateralDirectionality.LEFT,
                        LateralDirectionality.RIGHT })
                {
                    while (prev != null)
                    {
                        LaneStructureRecord next = latDirection.isLeft() ? prev.getLeft() : prev.getRight();
                        if (next != null && !this.upstreamEdge.contains(next))
                        {
                            moved |= findUpstreamEdge(next);
                        }
                        prev = next;
                    }
                }
            }
        }
    }

    /**
     * Recursive method to find downstream record(s) on the upstream edge, as the edge was moved downstream and a laterally
     * connected lane was not yet in the upstream edge. All edge records are added to the edge set.
     * @param record LaneStructureRecord; newly found adjacent record after moving the upstream edge downstream
     * @return boolean; whether a record was added to the edge, note that no record is added of the record is fully downstream
     *         of the upstream view distance
     * @throws GTUException on exception
     */
    private final boolean findUpstreamEdge(final LaneStructureRecord record) throws GTUException
    {
        Length cutOff = this.up.minus(record.getStartDistance());
        boolean moved = false;
        if (cutOff.gt0())
        {
            if (cutOff.lt(record.getLane().getLength()))
            {
                record.clearPrevList();
                record.setCutOffStart(cutOff);
                this.upstreamEdge.add(record);
                moved = true;
            }
            else
            {
                if (this.relativeLanes.containsKey(record))
                {
                    // could have been removed from upstream already
                    removeRecord(record);
                }
                for (LaneStructureRecord next : record.getNext())
                {
                    moved |= findUpstreamEdge(next);
                }
            }
        }
        return moved;
    }

    /**
     * Main downstream search for the map. Can be used at initial build and to update.
     * @param gtuType GTUType; GTU type
     * @param fractionalPosition double; fractional position on reference link
     * @param route Route; route of the GTU
     * @throws GTUException on exception
     */
    private void expandDownstreamEdge(final GTUType gtuType, final double fractionalPosition, final Route route)
            throws GTUException
    {
        this.ignoreSet.clear();
        Set<LaneStructureRecord> nextSet = new LinkedHashSet<>();
        Set<LaneStructureRecord> splitSet = new LinkedHashSet<>();
        boolean expand = true;
        while (expand)
        {
            expand = false;

            // longitudinal
            Iterator<LaneStructureRecord> iterator = this.downstreamEdge.iterator();
            while (iterator.hasNext())
            {
                LaneStructureRecord record = iterator.next();
                Map<Lane, GTUDirectionality> nexts = record.getLane().downstreamLanes(record.getDirection(), gtuType);
                if (record.getStartDistance().si + record.getLane().getLength().si > this.down.si)
                {
                    // downstream search ends on this lane
                    record.setCutOffEnd(this.down.minus(record.getStartDistance()));
                    for (Lane nextLane : nexts.keySet())
                    {
                        this.ignoreSet.add(nextLane); // exclude in lateral search
                    }
                }
                else
                {
                    // downstream search goes further upstream
                    record.clearCutOffEnd();
                    iterator.remove(); // can remove from edge, no algorithm needs it anymore in the downstream edge
                    for (Lane nextLane : nexts.keySet())
                    {
                        RelativeLane relativeLane = this.relativeLanes.get(record);
                        LaneStructureRecord next =
                                constructRecord(nextLane, nexts.get(nextLane), record, RecordLink.DOWN, relativeLane);
                        next.updateStartDistance(fractionalPosition, this);
                        record.addNext(next);
                        next.addPrev(record);
                        connectLaterally(next, gtuType);
                        // check route
                        int from = route.indexOf(next.getFromNode());
                        int to = route.indexOf(next.getToNode());
                        if (to < 0 || to - from != 1)
                        {
                            // not on our route, add some distance and stop
                            splitSet.add(next);
                        }
                        else
                        {
                            // regular edge
                            nextSet.add(next);
                        }
                    }
                }
            }
            this.downstreamEdge.addAll(nextSet);
            expand |= !nextSet.isEmpty();
            nextSet.clear();

            // split
            expandDownstreamSplit(splitSet, gtuType, fractionalPosition);
            splitSet.clear();

            // lateral
            Set<LaneStructureRecord> lateralSet =
                    expandLateral(this.downstreamEdge, RecordLink.LATERAL_END, gtuType, fractionalPosition);
            nextSet.addAll(lateralSet);
            expandUpstreamMerge(lateralSet, gtuType, fractionalPosition);

            // next iteration
            this.downstreamEdge.addAll(nextSet);
            expand |= !nextSet.isEmpty();
            nextSet.clear();
        }
    }

    /**
     * Expand the map to include a limited section downstream of a split, regarding links not on the route.
     * @param set Set&lt;LaneStructureRecord&gt;; set of lanes that have been laterally found
     * @param gtuType GTUType; GTU type
     * @param fractionalPosition double; fractional position on reference link
     * @throws GTUException on exception
     */
    private void expandDownstreamSplit(final Set<LaneStructureRecord> set, final GTUType gtuType,
            final double fractionalPosition) throws GTUException
    {
        Map<LaneStructureRecord, Length> prevs = new LinkedHashMap<>();
        Map<LaneStructureRecord, Length> nexts = new LinkedHashMap<>();
        for (LaneStructureRecord record : set)
        {
            prevs.put(record, record.getStartDistance().plus(this.downSplit));
        }
        while (!prevs.isEmpty())
        {
            for (LaneStructureRecord prev : prevs.keySet())
            {
                Map<Lane, GTUDirectionality> nextLanes = prev.getLane().downstreamLanes(prev.getDirection(), gtuType);
                for (Lane nextLane : nextLanes.keySet())
                {
                    RelativeLane relativeLane = this.relativeLanes.get(prev);
                    LaneStructureRecord next =
                            constructRecord(nextLane, nextLanes.get(nextLane), prev, RecordLink.DOWN, relativeLane);
                    next.updateStartDistance(fractionalPosition, this);
                    next.addPrev(prev);
                    prev.addNext(next);
                    connectLaterally(next, gtuType);
                    Length downHere = prevs.get(prev);
                    if (next.getStartDistance().si > downHere.si)
                    {
                        next.setCutOffEnd(downHere.minus(next.getStartDistance()));
                        this.downstreamEdge.add(next);
                    }
                    else
                    {
                        nexts.put(next, downHere);
                    }
                }
            }
            prevs = nexts;
            nexts = new LinkedHashMap<>();
        }
    }

    /**
     * Expand the map to include a limited section upstream of a merge that is downstream, regarding links not on the route.
     * @param set Set&lt;LaneStructureRecord&gt;; set of lanes that have been laterally found
     * @param gtuType GTUType; GTU type
     * @param fractionalPosition double; fractional position on reference link
     * @throws GTUException on exception
     */
    private void expandUpstreamMerge(final Set<LaneStructureRecord> set, final GTUType gtuType, final double fractionalPosition)
            throws GTUException
    {
        Map<LaneStructureRecord, Length> prevs = new LinkedHashMap<>();
        Map<LaneStructureRecord, Length> nexts = new LinkedHashMap<>();
        for (LaneStructureRecord record : set)
        {
            prevs.put(record, record.getStartDistance().minus(this.upMerge));
        }
        while (!prevs.isEmpty())
        {
            for (LaneStructureRecord prev : prevs.keySet())
            {
                Map<Lane, GTUDirectionality> nextLanes = prev.getLane().upstreamLanes(prev.getDirection(), gtuType);
                for (Lane nextLane : nextLanes.keySet())
                {
                    RelativeLane relativeLane = this.relativeLanes.get(prev);
                    LaneStructureRecord next =
                            constructRecord(nextLane, nextLanes.get(nextLane), prev, RecordLink.UP, relativeLane);
                    next.updateStartDistance(fractionalPosition, this);
                    next.addNext(prev);
                    prev.addPrev(next);
                    connectLaterally(next, gtuType);
                    Length upHere = prevs.get(prev);
                    if (next.getStartDistance().si < upHere.si)
                    {
                        next.setCutOffStart(upHere.minus(next.getStartDistance()));
                        this.upstreamEdge.add(next);
                    }
                    else
                    {
                        nexts.put(next, upHere);
                    }
                }
            }
            prevs = nexts;
            nexts = new LinkedHashMap<>();
        }
    }

    /**
     * Helper method of various other methods that laterally couples lanes that have been longitudinally found.
     * @param record LaneStructureRecord; longitudinally found lane
     * @param gtuType GTUType; GTU type
     */
    private void connectLaterally(final LaneStructureRecord record, final GTUType gtuType)
    {
        for (GTUDirectionality longDirection : new GTUDirectionality[] { GTUDirectionality.DIR_PLUS,
                GTUDirectionality.DIR_MINUS })
        {
            List<LaneStructureRecord> nexts = longDirection.isPlus() ? record.getNext() : record.getPrev();
            for (LaneStructureRecord next : nexts)
            {
                for (LateralDirectionality latDirection : new LateralDirectionality[] { LateralDirectionality.LEFT,
                        LateralDirectionality.RIGHT })
                {
                    LaneStructureRecord nextAdj = latDirection.isLeft() ? next.getLeft() : next.getRight();
                    if (nextAdj != null)
                    {
                        List<LaneStructureRecord> prevs = longDirection.isPlus() ? nextAdj.getPrev() : nextAdj.getNext();
                        for (LaneStructureRecord nextAdjPrev : prevs)
                        {
                            if (record.getLane().getParentLink().equals(nextAdjPrev.getLane().getParentLink()))
                            {
                                if (record.getLane().accessibleAdjacentLanes(latDirection, gtuType, record.getDirection())
                                        .contains(nextAdjPrev.getLane()))
                                {
                                    if (latDirection.isLeft())
                                    {
                                        record.setLeft(nextAdjPrev);
                                    }
                                    else
                                    {
                                        record.setRight(nextAdjPrev);
                                    }
                                }
                                if (nextAdjPrev.getLane()
                                        .accessibleAdjacentLanes(latDirection.flip(), gtuType, nextAdjPrev.getDirection())
                                        .contains(record.getLane()))
                                {
                                    if (latDirection.isLeft())
                                    {
                                        nextAdjPrev.setRight(record);
                                    }
                                    else
                                    {
                                        nextAdjPrev.setLeft(record);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Creates a lane structure record and adds it to relevant maps.
     * @param lane lane
     * @param direction direction
     * @param startDistanceSource source of the start distance
     * @param recordLink record link
     * @param relativeLane relative lane
     * @return created lane structure record
     */
    private LaneStructureRecord constructRecord(final Lane lane, final GTUDirectionality direction,
            final LaneStructureRecord startDistanceSource, final RecordLink recordLink, final RelativeLane relativeLane)
    {
        LaneStructureRecord record = new LaneStructureRecord(lane, direction, startDistanceSource, recordLink);
        if (!this.relativeLaneMap.containsKey(relativeLane))
        {
            this.relativeLaneMap.put(relativeLane, new LinkedHashSet<>());
        }
        this.relativeLaneMap.get(relativeLane).add(record);
        this.relativeLanes.put(record, relativeLane);
        return record;
    }

    /**
     * Adds a record to the current cross section. This method is called by {@code LaneStructureRecord.updateStartDistance()}
     * when that recognizes that distance = 0.0 is on a particular record. This method will only add the record if there is no
     * record in the cross section for the {@code RelativeLane} on which the record is registered.
     * @param record LaneStructureRecord; record
     */
    final void addToCrossSection(final LaneStructureRecord record)
    {
        RelativeLane lane = this.relativeLanes.get(record);
        if (!this.crossSectionRecords.containsKey(lane))
        {
            this.crossSectionRecords.put(lane, record);
        }
    }

    /**
     * @return rootLSR
     */
    public final LaneStructureRecord getRootLSR()
    {
        return this.root;
    }

    /**
     * Returns the cross section.
     * @return cross section
     */
    public final SortedSet<RelativeLane> getCrossSection()
    {
        return this.crossSectionRecords.navigableKeySet();
    }

    /**
     * @param lane lane to check
     * @return record at given lane
     * @throws GTUException if the lane is not in the cross section
     */
    public final LaneStructureRecord getLaneLSR(final RelativeLane lane) throws GTUException
    {
        LaneStructureRecord record = this.crossSectionRecords.get(lane);
        Throw.when(record == null, GTUException.class, "The requested lane %s is not in the most recent cross section.", lane);
        return record;
    }

    /**
     * Removes all mappings to relative lanes that are not in the most recent cross section.
     * @param map map to clear mappings from
     */
    public final void removeInvalidMappings(final Map<RelativeLane, ?> map)
    {
        Iterator<RelativeLane> iterator = map.keySet().iterator();
        while (iterator.hasNext())
        {
            RelativeLane lane = iterator.next();
            if (!this.crossSectionRecords.containsKey(lane))
            {
                iterator.remove();
            }
        }
    }

    /**
     * Adds a lane structure record in a mapping from relative lanes. The record is also added to the current cross section if
     * the start distance is negative, and the start distance plus length is positive. If the relative lane is already in the
     * current cross section, it is <b>not</b> overwritten.
     * @param lsr lane structure record
     * @param relativeLane relative lane
     */
    @Deprecated
    final void addLaneStructureRecord(final LaneStructureRecord lsr, final RelativeLane relativeLane)
    {
        if (!this.relativeLaneMap.containsKey(relativeLane))
        {
            this.relativeLaneMap.put(relativeLane, new LinkedHashSet<>());
        }
        this.relativeLaneMap.get(relativeLane).add(lsr);
        if (lsr.getStartDistance().le0() && lsr.getStartDistance().plus(lsr.getLane().getLength()).gt0()
                && (!this.crossSectionRecords.containsKey(relativeLane)
                        || this.crossSectionRecords.get(relativeLane).getStartDistance().gt0() && lsr.getStartDistance().le0()))
        {
            this.crossSectionRecords.put(relativeLane, lsr);
        }
    }

    /**
     * Retrieve objects on a lane of a specific type. Returns objects over a maximum length of the look ahead distance
     * downstream from the relative position, or as far as the lane map goes.
     * @param lane lane
     * @param clazz class of objects to find
     * @param gtu gtu
     * @param pos relative position to start search from
     * @param <T> type of objects to find
     * @return Sorted set of objects of requested type
     * @throws GTUException if lane is not in current set
     */
    @SuppressWarnings("unchecked")
    public final <T extends LaneBasedObject> SortedSet<Entry<T>> getDownstreamObjects(final RelativeLane lane,
            final Class<T> clazz, final LaneBasedGTU gtu, final RelativePosition.TYPE pos) throws GTUException
    {
        LaneStructureRecord record = this.crossSectionRecords.get(lane);
        double minLength;
        if (record == null)
        {
            minLength = Double.MAX_VALUE;
            // not in current cross section, get first downstream
            for (LaneStructureRecord rec : this.relativeLaneMap.get(lane))
            {
                double startDistance = rec.getStartDistance().si;
                if (startDistance >= 0 && startDistance < minLength)
                {
                    record = rec;
                    minLength = rec.getStartDistance().si;
                }
            }
        }
        else
        {
            minLength = record.getStartDistance().si;
        }
        Throw.when(record == null, GTUException.class, "Trying to get objects on %s, but that lane is not in the structure.",
                lane);
        Length ds = Length.createSI(gtu.getRelativePositions().get(pos).getDx().si - gtu.getReference().getDx().si);
        // the list is ordered, but only for DIR_PLUS, need to do our own ordering
        Length minimumPosition;
        Length maximumPosition;
        if (record.getDirection().isPlus())
        {
            minimumPosition = Length.createSI(ds.si - minLength);
            maximumPosition = Length.createSI(record.getLane().getLength().si);
        }
        else
        {
            minimumPosition = Length.ZERO;
            maximumPosition = Length.createSI(record.getLane().getLength().si + minLength - ds.si);
        }
        SortedSet<Entry<T>> set = new TreeSet<>();
        for (LaneBasedObject object : record.getLane().getLaneBasedObjects(minimumPosition, maximumPosition))
        {

            if (clazz.isAssignableFrom(object.getClass())
                    && ((record.getDirection().isPlus() && object.getDirection().isForwardOrBoth())
                            || (record.getDirection().isMinus() && object.getDirection().isBackwardOrBoth())))
            {
                // unchecked, but the above isAssignableFrom assures correctness
                Length distance = record.getDistanceToPosition(object.getLongitudinalPosition()).minus(ds);
                if (distance.le(this.lookAhead))
                {
                    set.add(new Entry<>(distance, (T) object));
                }
            }
        }
        getDownstreamObjectsRecursive(set, record, clazz, ds);
        return set;
    }

    /**
     * Retrieve objects on a lane of a specific type. Returns objects over a maximum length of the look ahead distance
     * downstream from the relative position, or as far as the lane map goes. Objects on links not on the route are ignored.
     * @param lane lane
     * @param clazz class of objects to find
     * @param gtu gtu
     * @param pos relative position to start search from
     * @param <T> type of objects to find
     * @param route the route
     * @return Sorted set of objects of requested type
     * @throws GTUException if lane is not in current set
     */
    public final <T extends LaneBasedObject> SortedSet<Entry<T>> getDownstreamObjectsOnRoute(final RelativeLane lane,
            final Class<T> clazz, final LaneBasedGTU gtu, final RelativePosition.TYPE pos, final Route route)
            throws GTUException
    {
        SortedSet<Entry<T>> set = getDownstreamObjects(lane, clazz, gtu, pos);
        if (route != null)
        {
            Iterator<Entry<T>> iterator = set.iterator();
            while (iterator.hasNext())
            {
                Entry<T> entry = iterator.next();
                CrossSectionLink link = entry.getLaneBasedObject().getLane().getParentLink();
                if (!route.contains(link.getStartNode()) || !route.contains(link.getEndNode())
                        || Math.abs(route.indexOf(link.getStartNode()) - route.indexOf(link.getEndNode())) != 1)
                {
                    iterator.remove();
                }
            }
        }
        return set;
    }

    /**
     * Recursive search for lane based objects downstream.
     * @param set set to store entries into
     * @param record current record
     * @param clazz class of objects to find
     * @param ds distance from reference to chosen relative position
     * @param <T> type of objects to find
     */
    @SuppressWarnings("unchecked")
    private <T extends LaneBasedObject> void getDownstreamObjectsRecursive(final SortedSet<Entry<T>> set,
            final LaneStructureRecord record, final Class<T> clazz, final Length ds)
    {
        if (record.getNext().isEmpty() || record.getNext().get(0).getStartDistance().gt(this.lookAhead))
        {
            return;
        }
        for (LaneStructureRecord next : record.getNext())
        {
            List<LaneBasedObject> list = next.getLane().getLaneBasedObjects();
            int iStart, di;
            if (record.getDirection().isPlus())
            {
                iStart = 0;
                di = 1;
            }
            else
            {
                iStart = list.size() - 1;
                di = -1;
            }
            for (int i = iStart; i >= 0 & i < list.size(); i += di)
            {
                LaneBasedObject object = list.get(i);
                if (clazz.isAssignableFrom(object.getClass())
                        && ((record.getDirection().isPlus() && object.getDirection().isForwardOrBoth())
                                || (record.getDirection().isMinus() && object.getDirection().isBackwardOrBoth())))
                {
                    // unchecked, but the above isAssignableFrom assures correctness
                    Length distance = next.getDistanceToPosition(object.getLongitudinalPosition()).minus(ds);
                    if (distance.le(this.lookAhead))
                    {
                        set.add(new Entry<>(distance, (T) object));
                    }
                    else
                    {
                        return;
                    }
                }
            }
            getDownstreamObjectsRecursive(set, next, clazz, ds);
        }
    }

    /**
     * Retrieve objects of a specific type. Returns objects over a maximum length of the look ahead distance downstream from the
     * relative position, or as far as the lane map goes. Objects on links not on the route are ignored.
     * @param clazz class of objects to find
     * @param gtu gtu
     * @param pos relative position to start search from
     * @param <T> type of objects to find
     * @param route the route
     * @return Sorted set of objects of requested type
     * @throws GTUException if lane is not in current set
     */
    public final <T extends LaneBasedObject> Map<RelativeLane, SortedSet<Entry<T>>> getDownstreamObjectsOnRoute(
            final Class<T> clazz, final LaneBasedGTU gtu, final RelativePosition.TYPE pos, final Route route)
            throws GTUException
    {
        Map<RelativeLane, SortedSet<Entry<T>>> out = new HashMap<>();
        for (RelativeLane relativeLane : this.relativeLaneMap.keySet())
        {
            out.put(relativeLane, getDownstreamObjectsOnRoute(relativeLane, clazz, gtu, pos, route));
        }
        return out;
    }

    /**
     * Retrieve objects on a lane of a specific type. Returns upstream objects from the relative position for as far as the lane
     * map goes. Distances to upstream objects are given as positive values.
     * @param lane lane
     * @param clazz class of objects to find
     * @param gtu gtu
     * @param pos relative position to start search from
     * @param <T> type of objects to find
     * @return Sorted set of objects of requested type
     * @throws GTUException if lane is not in current set
     */
    @SuppressWarnings("unchecked")
    public final <T extends LaneBasedObject> SortedSet<Entry<T>> getUpstreamObjects(final RelativeLane lane,
            final Class<T> clazz, final LaneBasedGTU gtu, final RelativePosition.TYPE pos) throws GTUException
    {
        LaneStructureRecord record = this.getLaneLSR(lane);
        Length ds = gtu.getReference().getDx().minus(gtu.getRelativePositions().get(pos).getDx());
        // the list is ordered, but only for DIR_PLUS, need to do our own ordering
        Length minimumPosition;
        Length maximumPosition;
        if (record.getDirection().isPlus())
        {
            minimumPosition = Length.ZERO;
            maximumPosition = record.getStartDistance().neg().minus(ds);
        }
        else
        {
            minimumPosition = record.getLane().getLength().plus(record.getStartDistance()).plus(ds);
            maximumPosition = record.getLane().getLength();
        }
        SortedSet<Entry<T>> set = new TreeSet<>();
        Length distance;
        for (LaneBasedObject object : record.getLane().getLaneBasedObjects(minimumPosition, maximumPosition))
        {
            if (clazz.isAssignableFrom(object.getClass())
                    && ((record.getDirection().isPlus() && object.getDirection().isForwardOrBoth())
                            || (record.getDirection().isMinus() && object.getDirection().isBackwardOrBoth())))
            {
                distance = record.getDistanceToPosition(object.getLongitudinalPosition()).neg().minus(ds);
                // unchecked, but the above isAssignableFrom assures correctness
                set.add(new Entry<>(distance, (T) object));
            }
        }
        getUpstreamObjectsRecursive(set, record, clazz, ds);
        return set;
    }

    /**
     * Recursive search for lane based objects upstream.
     * @param set set to store entries into
     * @param record current record
     * @param clazz class of objects to find
     * @param ds distance from reference to chosen relative position
     * @param <T> type of objects to find
     */
    @SuppressWarnings("unchecked")
    private <T extends LaneBasedObject> void getUpstreamObjectsRecursive(final SortedSet<Entry<T>> set,
            final LaneStructureRecord record, final Class<T> clazz, final Length ds)
    {
        for (LaneStructureRecord prev : record.getPrev())
        {
            Length distance;
            for (LaneBasedObject object : prev.getLane().getLaneBasedObjects())
            {
                if (clazz.isAssignableFrom(object.getClass())
                        && ((record.getDirection().isPlus() && object.getDirection().isForwardOrBoth())
                                || (record.getDirection().isMinus() && object.getDirection().isBackwardOrBoth())))
                {
                    distance = prev.getDistanceToPosition(object.getLongitudinalPosition()).neg().minus(ds);
                    // unchecked, but the above isAssignableFrom assures correctness
                    set.add(new Entry<>(distance, (T) object));
                }
            }
            getUpstreamObjectsRecursive(set, prev, clazz, ds);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "LaneStructure [rootLSR=" + this.root + "]";
    }

    /**
     * Wrapper to hold lane-based object and it's distance.
     * <p>
     * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version Sep 15, 2016 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     * @param <T> class of lane based object contained
     */
    public static class Entry<T extends LaneBasedObject> implements Comparable<Entry<T>>
    {

        /** Distance to lane based object. */
        private final Length distance;

        /** Lane based object. */
        private final T laneBasedObject;

        /**
         * @param distance distance to lane based object
         * @param laneBasedObject lane based object
         */
        public Entry(final Length distance, final T laneBasedObject)
        {
            this.distance = distance;
            this.laneBasedObject = laneBasedObject;
        }

        /**
         * @return distance.
         */
        public final Length getDistance()
        {
            return this.distance;
        }

        /**
         * @return laneBasedObject.
         */
        public final T getLaneBasedObject()
        {
            return this.laneBasedObject;
        }

        /** {@inheritDoc} */
        @Override
        public final int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((this.distance == null) ? 0 : this.distance.hashCode());
            result = prime * result + ((this.laneBasedObject == null) ? 0 : this.laneBasedObject.hashCode());
            return result;
        }

        /** {@inheritDoc} */
        @Override
        public final boolean equals(final Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            Entry<?> other = (Entry<?>) obj;
            if (this.distance == null)
            {
                if (other.distance != null)
                {
                    return false;
                }
            }
            else if (!this.distance.equals(other.distance))
            {
                return false;
            }
            if (this.laneBasedObject == null)
            {
                if (other.laneBasedObject != null)
                {
                    return false;
                }
            }
            // laneBasedObject does not implement equals...
            else if (!this.laneBasedObject.equals(other.laneBasedObject))
            {
                return false;
            }
            return true;
        }

        /** {@inheritDoc} */
        @Override
        public final int compareTo(final Entry<T> arg)
        {
            int d = this.distance.compareTo(arg.distance);
            if (d != 0 || this.laneBasedObject.equals(arg.laneBasedObject))
            {
                return d; // different distance (-1 or 1), or same distance but also equal lane based object (0)
            }
            return 1; // same distance, unequal lane based object (1)
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "LaneStructure.Entry [distance=" + this.distance + ", laneBasedObject=" + this.laneBasedObject + "]";
        }

    }

}
