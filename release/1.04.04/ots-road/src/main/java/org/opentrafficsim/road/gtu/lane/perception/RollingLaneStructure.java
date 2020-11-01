package org.opentrafficsim.road.gtu.lane.perception;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.event.EventInterface;
import org.djutils.event.EventListenerInterface;
import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
import org.djutils.immutablecollections.ImmutableMap;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.perception.Historical;
import org.opentrafficsim.core.perception.HistoricalValue;
import org.opentrafficsim.core.perception.HistoryManager;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.RollingLaneStructureRecord.RecordLink;
import org.opentrafficsim.road.gtu.lane.plan.operational.LaneBasedOperationalPlan;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneDirection;
import org.opentrafficsim.road.network.lane.object.LaneBasedObject;

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
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Feb 20, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class RollingLaneStructure implements LaneStructure, Serializable, EventListenerInterface
{
    /** */
    private static final long serialVersionUID = 20160400L;

    /** The lanes from which we observe the situation. */
    private final Historical<RollingLaneStructureRecord> root;

    /** Look ahead distance. */
    private Length lookAhead;

    /** Route the structure is based on. */
    private Route previousRoute;

    /** Whether the previous plan was deviative. */
    private boolean previouslyDeviative = false;

    /** Lane structure records of the cross section. */
    private TreeMap<RelativeLane, RollingLaneStructureRecord> crossSectionRecords = new TreeMap<>();

    /** First lane structure records. */
    private TreeMap<RelativeLane, RollingLaneStructureRecord> firstRecords = new TreeMap<>();

    /** Lane structure records grouped per relative lane. */
    private Map<RelativeLane, Set<RollingLaneStructureRecord>> relativeLaneMap = new LinkedHashMap<>();

    /** Relative lanes storage per record, such that other records can be linked to the correct relative lane. */
    private Map<LaneStructureRecord, RelativeLane> relativeLanes = new LinkedHashMap<>();

    /** Set of lanes that can be ignored as they are beyond build bounds. */
    private final Set<Lane> ignoreSet = new LinkedHashSet<>();

    /** Upstream edges. */
    private final Set<RollingLaneStructureRecord> upstreamEdge = new LinkedHashSet<>();

    /** Downstream edges. */
    private final Set<RollingLaneStructureRecord> downstreamEdge = new LinkedHashSet<>();

    /** Downstream distance over which the structure is made. */
    private final Length down;

    /** Upstream distance over which the structure is made. */
    private final Length up;

    /** Downstream distance at splits (links not on route) included in the structure. */
    private final Length downSplit;

    /** Upstream distance at downstream merges (links not on route) included in the structure. */
    private final Length upMerge;

    /** GTU. */
    private final LaneBasedGTU containingGtu;

    /** the animation access. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    public AnimationAccess animationAccess = new AnimationAccess();

    /**
     * Constructor.
     * @param lookAhead Length; distance over which visual objects are included
     * @param down Length; downstream distance over which the structure is made
     * @param up Length; upstream distance over which the structure is made, should include a margin for reaction time
     * @param downSplit Length; downstream distance at splits (links not on route) included in the structure
     * @param upMerge Length; upstream distance at downstream merges (links not on route) included in the structure
     * @param gtu LaneBasedGTU; GTU
     */
    public RollingLaneStructure(final Length lookAhead, final Length down, final Length up, final Length downSplit,
        final Length upMerge, final LaneBasedGTU gtu)
    {
        HistoryManager historyManager = gtu.getSimulator().getReplication().getHistoryManager(gtu.getSimulator());
        this.root = new HistoricalValue<>(historyManager);
        this.lookAhead = lookAhead;
        this.down = down;
        this.up = up;
        this.downSplit = downSplit;
        this.upMerge = upMerge;
        this.containingGtu = gtu;
        try
        {
            gtu.addListener(this, LaneBasedGTU.LANE_CHANGE_EVENT);
        }
        catch (RemoteException exception)
        {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Updates the underlying structure shifting the root position to the input.
     * @param pos DirectedLanePosition; current position of the GTU
     * @param route Route; current route of the GTU
     * @param gtuType GTUType; GTU type
     * @throws GTUException on a problem while updating the structure
     */
    @Override
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
        boolean deviative = this.containingGtu.getOperationalPlan() instanceof LaneBasedOperationalPlan
            && ((LaneBasedOperationalPlan) this.containingGtu.getOperationalPlan()).isDeviative();

        // TODO on complex networks, e.g. with sections connectors where lane changes are not possible, the update may fail
        if (this.previousRoute != route || this.root.get() == null || deviative != this.previouslyDeviative)
        {
            // create new LaneStructure
            this.previousRoute = route;

            // clear
            this.upstreamEdge.clear();
            this.downstreamEdge.clear();
            this.crossSectionRecords.clear();
            this.relativeLanes.clear();
            this.relativeLaneMap.clear();
            this.firstRecords.clear();

            // build cross-section
            RollingLaneStructureRecord newRoot = constructRecord(lane, direction, null, RecordLink.CROSS,
                RelativeLane.CURRENT);
            this.root.set(newRoot);
            this.crossSectionRecords.put(RelativeLane.CURRENT, newRoot);
            for (LateralDirectionality latDirection : new LateralDirectionality[] {LateralDirectionality.LEFT,
                LateralDirectionality.RIGHT})
            {
                RollingLaneStructureRecord current = newRoot;
                RelativeLane relativeLane = RelativeLane.CURRENT;
                Set<Lane> adjacentLanes = current.getLane().accessibleAdjacentLanesPhysical(latDirection, gtuType, current
                    .getDirection());
                while (!adjacentLanes.isEmpty())
                {
                    Throw.when(adjacentLanes.size() > 1, RuntimeException.class,
                        "Multiple adjacent lanes encountered during construction of lane map.");
                    relativeLane = latDirection.isLeft() ? relativeLane.getLeft() : relativeLane.getRight();
                    Lane adjacentLane = adjacentLanes.iterator().next();
                    RollingLaneStructureRecord adjacentRecord = constructRecord(adjacentLane, direction, current,
                        RecordLink.CROSS, relativeLane);
                    this.crossSectionRecords.put(relativeLane, adjacentRecord);
                    if (latDirection.isLeft())
                    {
                        if (adjacentLane.accessibleAdjacentLanesPhysical(LateralDirectionality.RIGHT, gtuType, current
                            .getDirection()).contains(current.getLane()))
                        {
                            adjacentRecord.setRight(current, gtuType);
                        }
                        current.setLeft(adjacentRecord, gtuType);
                    }
                    else
                    {
                        if (adjacentLane.accessibleAdjacentLanesPhysical(LateralDirectionality.LEFT, gtuType, current
                            .getDirection()).contains(current.getLane()))
                        {
                            adjacentRecord.setLeft(current, gtuType);
                        }
                        current.setRight(adjacentRecord, gtuType);
                    }
                    current = adjacentRecord;
                    adjacentLanes = current.getLane().accessibleAdjacentLanesPhysical(latDirection, gtuType, current
                        .getDirection());
                }
            }
            this.upstreamEdge.addAll(this.crossSectionRecords.values());
            this.downstreamEdge.addAll(this.crossSectionRecords.values());
            this.firstRecords.putAll(this.crossSectionRecords);

            // set the start distances so the upstream expand can work
            newRoot.updateStartDistance(fracPos, this);

            // expand upstream edge
            expandUpstreamEdge(gtuType, fracPos);

            // derive first records
            deriveFirstRecords();
        }
        else
        {

            // update LaneStructure
            RollingLaneStructureRecord newRoot = this.root.get();
            if (!lane.equals(newRoot.getLane()))
            {
                // find the root, and possible lateral shift if changed lane
                newRoot = null;
                RelativeLane lateralMove = null;
                double closest = Double.POSITIVE_INFINITY;
                for (RelativeLane relativeLane : this.relativeLaneMap.keySet())
                {
                    for (RollingLaneStructureRecord record : this.relativeLaneMap.get(relativeLane))
                    {
                        if (record.getLane().equals(lane) && record.getStartDistance().si < closest && record
                            .getStartDistance().si + record.getLength().si > 0.0)
                        {
                            newRoot = record;
                            lateralMove = relativeLane;
                            // multiple records may be present for the current lane due to a loop
                            closest = record.getStartDistance().si;
                        }
                    }
                    if (newRoot != null)
                    {
                        break;
                    }
                }
                // newRoot.getPrev().contains(newRoot.getStartDistanceSource())
                this.root.set(newRoot);

                // update start distance sources
                updateStartDistanceSources();

                // shift if changed lane
                if (!lateralMove.isCurrent())
                {
                    RelativeLane delta = new RelativeLane(lateralMove.getLateralDirectionality().flip(), lateralMove
                        .getNumLanes());

                    TreeMap<RelativeLane, Set<RollingLaneStructureRecord>> newRelativeLaneMap = new TreeMap<>();
                    for (RelativeLane relativeLane : this.relativeLaneMap.keySet())
                    {
                        RelativeLane newRelativeLane = relativeLane.add(delta);
                        newRelativeLaneMap.put(newRelativeLane, this.relativeLaneMap.get(relativeLane));
                    }
                    this.relativeLaneMap = newRelativeLaneMap;

                    Map<LaneStructureRecord, RelativeLane> newRelativeLanes = new LinkedHashMap<>();
                    for (LaneStructureRecord record : this.relativeLanes.keySet())
                    {
                        newRelativeLanes.put(record, this.relativeLanes.get(record).add(delta));
                    }
                    this.relativeLanes = newRelativeLanes;
                }

                this.crossSectionRecords.clear();
                this.crossSectionRecords.put(RelativeLane.CURRENT, newRoot);
                for (LateralDirectionality latDirection : new LateralDirectionality[] {LateralDirectionality.LEFT,
                    LateralDirectionality.RIGHT})
                {
                    RollingLaneStructureRecord record = newRoot;
                    RollingLaneStructureRecord next = newRoot;
                    RelativeLane delta = new RelativeLane(latDirection, 1);
                    RelativeLane relLane = RelativeLane.CURRENT;
                    while (next != null)
                    {
                        next = latDirection.isLeft() ? record.getLeft() : record.getRight();
                        if (next != null)
                        {
                            next.changeStartDistanceSource(record, RecordLink.CROSS);
                            relLane = relLane.add(delta);
                            this.crossSectionRecords.put(relLane, next);
                            record = next;
                        }
                    }
                }

            }
            newRoot.updateStartDistance(fracPos, this);

            // update upstream edges
            retreatUpstreamEdge();

            // derive first records
            deriveFirstRecords();

        }

        this.previouslyDeviative = deviative;

        // update downstream edges
        expandDownstreamEdge(gtuType, fracPos, route);

    }

    /**
     * Derives the first downstream records so the extended cross-section can be returned.
     */
    private void deriveFirstRecords()
    {
        this.firstRecords.clear();
        this.firstRecords.putAll(this.crossSectionRecords);
        for (RelativeLane lane : this.relativeLaneMap.keySet())
        {
            getFirstRecord(lane); // store non-null values internally
        }
    }

    /**
     * Upstream algorithm from the new source, where records along the way are assigned a new start distance source. These
     * records were downstream in the previous time step, but are now upstream. Hence, their start distance source should now
     * become their downstream record. This algorithm acts like an upstream tree, where each branch is stopped if there are no
     * upstream records, or the upstream records already have their downstream record as source (i.e. the record was already
     * upstream in the previous time step).
     */
    private void updateStartDistanceSources()
    {

        // initial cross section
        Set<RollingLaneStructureRecord> set = new LinkedHashSet<>();
        RollingLaneStructureRecord rootRecord = this.root.get();
        set.add(rootRecord);
        rootRecord.changeStartDistanceSource(null, RecordLink.CROSS);
        RollingLaneStructureRecord prev = rootRecord;
        RollingLaneStructureRecord next = prev.getLeft();
        while (next != null)
        {
            set.add(next);
            next.changeStartDistanceSource(prev, RecordLink.CROSS);
            prev = next;
            next = next.getLeft();
        }
        prev = rootRecord;
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
            Set<RollingLaneStructureRecord> newSet = new LinkedHashSet<>();
            for (RollingLaneStructureRecord record : set)
            {
                for (LateralDirectionality latDirection : new LateralDirectionality[] {LateralDirectionality.LEFT,
                    LateralDirectionality.RIGHT})
                {
                    prev = record;
                    next = latDirection.isLeft() ? record.getLeft() : record.getRight();
                    while (next != null && !set.contains(next))
                    {
                        next.changeStartDistanceSource(prev, RecordLink.LATERAL_END);
                        removeDownstream(next, latDirection.flip()); // split not taken can be thrown away
                        newSet.add(next);
                        prev = next;
                        next = latDirection.isLeft() ? next.getLeft() : next.getRight();
                    }
                }
            }
            set.addAll(newSet);

            // longitudinal
            newSet.clear();
            for (RollingLaneStructureRecord record : set)
            {
                for (RollingLaneStructureRecord prevRecord : record.getPrev())
                {
                    Iterator<RollingLaneStructureRecord> it = prevRecord.getNext().iterator();
                    while (it.hasNext())
                    {
                        RollingLaneStructureRecord otherDown = it.next();
                        if (!otherDown.getLane().getParentLink().equals(record.getLane().getParentLink()))
                        {
                            // split not taken can be thrown away
                            otherDown.changeStartDistanceSource(null, null);
                            // this can throw away records that are laterally connected as they later merge... ??
                            removeDownstream(otherDown, LateralDirectionality.NONE);
                            removeRecord(otherDown);
                            it.remove();
                        }
                    }
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
     * @param record RollingLaneStructureRecord; record, downstream of which to remove all records
     * @param lat LateralDirectionality; records with an adjacent record at this side are not deleted
     */
    private void removeDownstream(final RollingLaneStructureRecord record, final LateralDirectionality lat)
    {
        for (RollingLaneStructureRecord next : record.getNext())
        {
            RollingLaneStructureRecord adj = lat.isLeft() ? next.getLeft() : lat.isRight() ? next.getRight() : null;
            if (adj == null)
            {
                next.changeStartDistanceSource(null, null);
                removeDownstream(next, lat);
                removeRecord(next);
            }
        }
        record.clearNextList();
    }

    /**
     * Removes the record from underlying data structures.
     * @param record RollingLaneStructureRecord; record to remove
     */
    private void removeRecord(final RollingLaneStructureRecord record)
    {
        RelativeLane lane = this.relativeLanes.get(record);
        if (lane != null)
        {
            this.relativeLaneMap.get(lane).remove(record);
            this.relativeLanes.remove(record);
        }
        record.changeStartDistanceSource(null, null);

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
        for (LaneStructureRecord record : this.upstreamEdge)
        {
            this.ignoreSet.add(record.getLane());
        }
        Set<RollingLaneStructureRecord> nextSet = new LinkedHashSet<>();
        boolean expand = true;
        while (expand)
        {
            expand = false;

            // longitudinal
            Iterator<RollingLaneStructureRecord> iterator = this.upstreamEdge.iterator();
            Set<RollingLaneStructureRecord> modifiedEdge = new LinkedHashSet<>(this.upstreamEdge);
            while (iterator.hasNext())
            {
                RollingLaneStructureRecord prev = iterator.next();
                ImmutableMap<Lane, GTUDirectionality> nexts = prev.getLane().upstreamLanes(prev.getDirection(), gtuType);
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
                    iterator.remove();
                    if (!nexts.isEmpty())
                    {
                        for (Lane prevLane : nexts.keySet())
                        {
                            RelativeLane relativeLane = this.relativeLanes.get(prev);
                            RollingLaneStructureRecord next = constructRecord(prevLane, nexts.get(prevLane), prev,
                                RecordLink.UP, relativeLane);
                            this.ignoreSet.add(prevLane);
                            next.updateStartDistance(fractionalPosition, this);
                            connectLaterally(next, gtuType, modifiedEdge);
                            next.addNext(prev);
                            prev.addPrev(next);
                            nextSet.add(next);
                            modifiedEdge.add(next);
                        }
                    }
                }
            }
            this.upstreamEdge.addAll(nextSet);
            expand |= !nextSet.isEmpty();
            nextSet.clear();

            // lateral
            Set<RollingLaneStructureRecord> lateralSet = expandLateral(this.upstreamEdge, RecordLink.LATERAL_END, gtuType,
                fractionalPosition);
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
     * @param edge Set&lt;RollingLaneStructureRecord&gt;; input set
     * @param recordLink RecordLink; link to add between lateral records, depends on upstream or downstream search
     * @param gtuType GTUType; GTU type
     * @param fractionalPosition double; fractional position on reference link
     * @return Set&lt;LaneStructureRecord&gt;; output set with all laterally found lanes
     */
    private Set<RollingLaneStructureRecord> expandLateral(final Set<RollingLaneStructureRecord> edge,
        final RecordLink recordLink, final GTUType gtuType, final double fractionalPosition)
    {
        Set<RollingLaneStructureRecord> nextSet = new LinkedHashSet<>();
        Set<Lane> laneSet = new LinkedHashSet<>(); // set to check that an adjacent lane is not another lane already in the set
        for (LaneStructureRecord record : edge)
        {
            laneSet.add(record.getLane());
        }
        Iterator<RollingLaneStructureRecord> iterator = edge.iterator();
        while (iterator.hasNext())
        {
            RollingLaneStructureRecord record = iterator.next();
            for (LateralDirectionality latDirection : new LateralDirectionality[] {LateralDirectionality.LEFT,
                LateralDirectionality.RIGHT})
            {
                if (record.getRight() != null && latDirection.isRight() || record.getLeft() != null && latDirection.isLeft())
                {
                    // skip if there already is a record on that side
                    continue;
                }
                RelativeLane relativeLane = this.relativeLanes.get(record);
                RollingLaneStructureRecord prev = record;
                Set<Lane> adjacentLanes = prev.getLane().accessibleAdjacentLanesPhysical(latDirection, gtuType, prev
                    .getDirection());
                while (!adjacentLanes.isEmpty())
                {
                    Throw.when(adjacentLanes.size() > 1, RuntimeException.class,
                        "Multiple adjacent lanes encountered during construction of lane map.");
                    relativeLane = latDirection.isLeft() ? relativeLane.getLeft() : relativeLane.getRight();
                    Lane nextLane = adjacentLanes.iterator().next();
                    if (!laneSet.contains(nextLane) && !this.ignoreSet.contains(nextLane))
                    {
                        RollingLaneStructureRecord next = constructRecord(nextLane, record.getDirection(), prev, recordLink,
                            relativeLane);
                        this.ignoreSet.add(nextLane);
                        next.updateStartDistance(fractionalPosition, this);
                        nextSet.add(next);
                        laneSet.add(nextLane);
                        if (latDirection.isLeft())
                        {
                            prev.setLeft(next, gtuType);
                            if (nextLane.accessibleAdjacentLanesPhysical(LateralDirectionality.RIGHT, gtuType, prev
                                .getDirection()).contains(prev.getLane()))
                            {
                                next.setRight(prev, gtuType);
                            }
                            for (RollingLaneStructureRecord edgeRecord : edge)
                            {
                                if (!edgeRecord.equals(prev) && edgeRecord.getLane().getParentLink().equals(next.getLane()
                                    .getParentLink()))
                                {
                                    for (Lane adjLane : edgeRecord.getLane().accessibleAdjacentLanesPhysical(
                                        LateralDirectionality.RIGHT, gtuType, edgeRecord.getDirection()))
                                    {
                                        if (adjLane.equals(next.getLane()))
                                        {
                                            edgeRecord.setRight(next, gtuType);
                                            next.setLeft(edgeRecord, gtuType);
                                        }
                                    }
                                }
                            }
                        }
                        else
                        {
                            prev.setRight(next, gtuType);
                            if (nextLane.accessibleAdjacentLanesPhysical(LateralDirectionality.LEFT, gtuType, prev
                                .getDirection()).contains(prev.getLane()))
                            {
                                next.setLeft(prev, gtuType);
                            }
                            for (RollingLaneStructureRecord edgeRecord : edge)
                            {
                                if (!edgeRecord.equals(prev) && edgeRecord.getLane().getParentLink().equals(next.getLane()
                                    .getParentLink()))
                                {
                                    for (Lane adjLane : edgeRecord.getLane().accessibleAdjacentLanesPhysical(
                                        LateralDirectionality.LEFT, gtuType, edgeRecord.getDirection()))
                                    {
                                        if (adjLane.equals(next.getLane()))
                                        {
                                            edgeRecord.setLeft(next, gtuType);
                                            next.setRight(edgeRecord, gtuType);
                                        }
                                    }
                                }
                            }
                        }
                        // connect longitudinally due to merge or split
                        Set<RollingLaneStructureRecord> adjs = new LinkedHashSet<>();
                        if (next.getLeft() != null)
                        {
                            adjs.add(next.getLeft());
                        }
                        if (next.getRight() != null)
                        {
                            adjs.add(next.getRight());
                        }
                        for (RollingLaneStructureRecord adj : adjs)
                        {
                            for (Lane lane : next.getLane().upstreamLanes(next.getDirection(), gtuType).keySet())
                            {
                                for (RollingLaneStructureRecord adjPrev : adj.getPrev())
                                {
                                    if (lane.equals(adjPrev.getLane()))
                                    {
                                        Try.execute(() -> next.addPrev(adjPrev), "Cut-off record added as prev.");
                                    }
                                }
                            }
                            for (Lane lane : next.getLane().downstreamLanes(next.getDirection(), gtuType).keySet())
                            {
                                for (RollingLaneStructureRecord adjNext : adj.getNext())
                                {
                                    if (lane.equals(adjNext.getLane()))
                                    {
                                        Try.execute(() -> next.addNext(adjNext), "Cut-off record added as next.");
                                    }
                                }
                            }
                        }

                        prev = next;
                        adjacentLanes = prev.getLane().accessibleAdjacentLanesPhysical(latDirection, gtuType, prev
                            .getDirection());
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
        Set<RollingLaneStructureRecord> nexts = new LinkedHashSet<>();
        while (moved)
        {
            moved = false;
            nexts.clear();
            Iterator<RollingLaneStructureRecord> iterator = this.upstreamEdge.iterator();
            while (iterator.hasNext())
            {
                RollingLaneStructureRecord prev = iterator.next();
                // nexts may contain 'prev' as two lanes are removed from the upstream edge that merge in to 1 downstream lane
                if (!nexts.contains(prev) && prev.getStartDistance().si + prev.getLane().getLength().si < this.up.si)
                {
                    for (RollingLaneStructureRecord next : prev.getNext())
                    {
                        next.clearPrevList();
                        next.setCutOffStart(this.up.minus(next.getStartDistance()));
                        moved = true;
                        nexts.add(next);
                        RollingLaneStructureRecord lat = next.getLeft();
                        while (lat != null && lat.getPrev().isEmpty())
                        {
                            nexts.add(lat);
                            lat = lat.getLeft();
                        }
                        lat = next.getRight();
                        while (lat != null && lat.getPrev().isEmpty())
                        {
                            nexts.add(lat);
                            lat = lat.getRight();
                        }
                    }
                    prev.clearNextList();
                    removeRecord(prev);
                    iterator.remove();
                }
                else
                {
                    Length cutOff = this.up.minus(prev.getStartDistance());
                    if (cutOff.si > 0)
                    {
                        prev.setCutOffStart(cutOff);
                    }
                }
            }
            this.upstreamEdge.addAll(nexts);

            // check adjacent lanes
            for (RollingLaneStructureRecord record : nexts)
            {
                RollingLaneStructureRecord prev = record;
                for (LateralDirectionality latDirection : new LateralDirectionality[] {LateralDirectionality.LEFT,
                    LateralDirectionality.RIGHT})
                {
                    while (prev != null)
                    {
                        RollingLaneStructureRecord next = latDirection.isLeft() ? prev.getLeft() : prev.getRight();
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
     * @param record RollingLaneStructureRecord; newly found adjacent record after moving the upstream edge downstream
     * @return boolean; whether a record was added to the edge, note that no record is added of the record is fully downstream
     *         of the upstream view distance
     * @throws GTUException on exception
     */
    private boolean findUpstreamEdge(final RollingLaneStructureRecord record) throws GTUException
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
                for (RollingLaneStructureRecord next : record.getNext())
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
        for (LaneStructureRecord record : this.downstreamEdge)
        {
            this.ignoreSet.add(record.getLane());
        }
        Set<RollingLaneStructureRecord> nextSet = new LinkedHashSet<>();
        Set<RollingLaneStructureRecord> splitSet = new LinkedHashSet<>();
        boolean expand = true;
        while (expand)
        {
            expand = false;

            // longitudinal
            // find links to extend from so we can add lanes if -any- of the next lanes comes within the perception distance
            Set<Link> linksToExpandFrom = new LinkedHashSet<>();
            Iterator<RollingLaneStructureRecord> iterator = this.downstreamEdge.iterator();
            while (iterator.hasNext())
            {
                RollingLaneStructureRecord record = iterator.next();
                if (record.getStartDistance().si + record.getLane().getLength().si < this.down.si)
                {
                    linksToExpandFrom.add(record.getLane().getParentLink());
                }
            }
            Set<RollingLaneStructureRecord> modifiedEdge = new LinkedHashSet<>(this.downstreamEdge);
            iterator = this.downstreamEdge.iterator();
            while (iterator.hasNext())
            {
                RollingLaneStructureRecord record = iterator.next();
                ImmutableMap<Lane, GTUDirectionality> nexts = record.getLane().downstreamLanes(record.getDirection(),
                    gtuType);
                if (!linksToExpandFrom.contains(record.getLane().getParentLink()))
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
                    // downstream search goes further downstream

                    // in case there are multiple lanes on the same link after a lane split, we need to choose one
                    LaneDirection nextLaneDirection = new LaneDirection(record.getLane(), record.getDirection())
                        .getNextLaneDirection(this.containingGtu);

                    record.clearCutOffEnd();
                    iterator.remove(); // can remove from edge, no algorithm needs it anymore in the downstream edge
                    for (Lane nextLane : nexts.keySet())
                    {
                        if (nextLaneDirection != null && nextLane.getParentLink().equals(nextLaneDirection.getLane()
                            .getParentLink()) && !nextLane.equals(nextLaneDirection.getLane()))
                        {
                            // skip this lane as its a not chosen lane on the next link after a lane split
                            continue;
                        }
                        RelativeLane relativeLane = this.relativeLanes.get(record);
                        GTUDirectionality dir = nexts.get(nextLane);
                        RollingLaneStructureRecord next = constructRecord(nextLane, dir, record, RecordLink.DOWN,
                            relativeLane);
                        this.ignoreSet.add(nextLane);
                        next.updateStartDistance(fractionalPosition, this);
                        record.addNext(next);
                        next.addPrev(record);
                        connectLaterally(next, gtuType, modifiedEdge);
                        // check route
                        int from = route == null ? 0 : route.indexOf(next.getFromNode());
                        int to = route == null ? 1 : route.indexOf(next.getToNode());
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
                        modifiedEdge.add(next);
                        // expand upstream over any possible other lane that merges in to this
                        Set<RollingLaneStructureRecord> set = new LinkedHashSet<>();
                        set.add(next);
                        expandUpstreamMerge(set, gtuType, fractionalPosition, route);
                    }
                }
            }
            this.downstreamEdge.addAll(nextSet);
            expand |= !nextSet.isEmpty();
            nextSet.clear();

            // split
            expandDownstreamSplit(splitSet, gtuType, fractionalPosition, route);
            splitSet.clear();

            // lateral
            Set<RollingLaneStructureRecord> lateralSet = expandLateral(this.downstreamEdge, RecordLink.LATERAL_END, gtuType,
                fractionalPosition);
            nextSet.addAll(lateralSet);
            expandUpstreamMerge(lateralSet, gtuType, fractionalPosition, route);

            // next iteration
            this.downstreamEdge.addAll(nextSet);
            expand |= !nextSet.isEmpty();
            nextSet.clear();
        }
    }

    /**
     * Expand the map to include a limited section downstream of a split, regarding links not on the route.
     * @param set Set&lt;RollingLaneStructureRecord&gt;; set of lanes that have been laterally found
     * @param gtuType GTUType; GTU type
     * @param fractionalPosition double; fractional position on reference link
     * @param route Route; route
     * @throws GTUException on exception
     */
    private void expandDownstreamSplit(final Set<RollingLaneStructureRecord> set, final GTUType gtuType,
        final double fractionalPosition, final Route route) throws GTUException
    {
        Map<RollingLaneStructureRecord, Length> prevs = new LinkedHashMap<>();
        Map<RollingLaneStructureRecord, Length> nexts = new LinkedHashMap<>();
        for (RollingLaneStructureRecord record : set)
        {
            prevs.put(record, record.getStartDistance().plus(this.downSplit));
        }
        while (!prevs.isEmpty())
        {
            for (RollingLaneStructureRecord prev : prevs.keySet())
            {
                ImmutableMap<Lane, GTUDirectionality> nextLanes = prev.getLane().downstreamLanes(prev.getDirection(),
                    gtuType);
                RelativeLane relativeLane = this.relativeLanes.get(prev);
                for (Lane nextLane : nextLanes.keySet())
                {
                    GTUDirectionality dir = nextLanes.get(nextLane);
                    Node fromNode = dir.isPlus() ? nextLane.getParentLink().getStartNode() : nextLane.getParentLink()
                        .getEndNode();
                    Node toNode = dir.isPlus() ? nextLane.getParentLink().getEndNode() : nextLane.getParentLink()
                        .getStartNode();
                    int from = route.indexOf(fromNode);
                    int to = route.indexOf(toNode);
                    if (from == -1 || to == -1 || to - from != 1)
                    {
                        RollingLaneStructureRecord next = constructRecord(nextLane, dir, prev, RecordLink.DOWN,
                            relativeLane);
                        next.updateStartDistance(fractionalPosition, this);
                        next.addPrev(prev);
                        prev.addNext(next);
                        connectLaterally(next, gtuType, nexts.keySet());
                        Length downHere = prevs.get(prev);
                        if (next.getStartDistance().si > downHere.si)
                        {
                            next.setCutOffEnd(downHere.minus(next.getStartDistance()));
                        }
                        else
                        {
                            nexts.put(next, downHere);
                        }
                    }
                }
            }
            prevs = nexts;
            nexts = new LinkedHashMap<>();
        }
    }

    /**
     * Expand the map to include a limited section upstream of a merge that is downstream, regarding links not on the route.
     * @param set Set&lt;RollingLaneStructureRecord&gt;; set of lanes that have been laterally found
     * @param gtuType GTUType; GTU type
     * @param fractionalPosition double; fractional position on reference link
     * @param route Route; route of the GTU
     * @throws GTUException on exception
     */
    private void expandUpstreamMerge(final Set<RollingLaneStructureRecord> set, final GTUType gtuType,
        final double fractionalPosition, final Route route) throws GTUException
    {
        Map<RollingLaneStructureRecord, Length> prevs = new LinkedHashMap<>();
        Map<RollingLaneStructureRecord, Length> nexts = new LinkedHashMap<>();
        for (RollingLaneStructureRecord record : set)
        {
            prevs.put(record, record.getStartDistance().plus(this.upMerge)); // upMerge is negative
        }
        while (!prevs.isEmpty())
        {
            for (RollingLaneStructureRecord prev : prevs.keySet())
            {
                ImmutableMap<Lane, GTUDirectionality> nextLanes = prev.getLane().upstreamLanes(prev.getDirection(), gtuType);
                boolean anyAdded = false;
                for (Lane nextLane : nextLanes.keySet())
                {
                    GTUDirectionality dir = nextLanes.get(nextLane);
                    Node fromNode = dir.isPlus() ? nextLane.getParentLink().getStartNode() : nextLane.getParentLink()
                        .getEndNode();
                    Node toNode = dir.isPlus() ? nextLane.getParentLink().getEndNode() : nextLane.getParentLink()
                        .getStartNode();
                    int from = route == null ? 0 : route.indexOf(fromNode);
                    int to = route == null ? 1 : route.indexOf(toNode);
                    // TODO we now assume everything is on the route, but merges could be ok without route
                    // so, without a route we should be able to recognize which upstream 'nextLane' is on the other link
                    if (from == -1 || to == -1 || to - from != 1)
                    {
                        anyAdded = true;
                        RelativeLane relativeLane = this.relativeLanes.get(prev);
                        RollingLaneStructureRecord next = constructRecord(nextLane, nextLanes.get(nextLane), prev,
                            RecordLink.UP, relativeLane);
                        next.updateStartDistance(fractionalPosition, this);
                        next.addNext(prev);
                        prev.addPrev(next);
                        connectLaterally(next, gtuType, nexts.keySet());
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
                if (!anyAdded && !set.contains(prev))
                {
                    this.upstreamEdge.add(prev);
                }
            }
            prevs = nexts;
            nexts = new LinkedHashMap<>();
        }
    }

    /**
     * Helper method of various other methods that laterally couples lanes that have been longitudinally found.
     * @param record RollingLaneStructureRecord; longitudinally found lane
     * @param gtuType GTUType; GTU type
     * @param nextSet Set&lt;RollingLaneStructureRecord&gt;; set of records on current build edge
     */
    private void connectLaterally(final RollingLaneStructureRecord record, final GTUType gtuType,
        final Set<RollingLaneStructureRecord> nextSet)
    {
        for (RollingLaneStructureRecord other : nextSet)
        {
            for (LateralDirectionality latDirection : new LateralDirectionality[] {LateralDirectionality.LEFT,
                LateralDirectionality.RIGHT})
            {
                if ((latDirection.isLeft() ? other.getLeft() : other.getRight()) == null)
                {
                    for (Lane otherLane : other.getLane().accessibleAdjacentLanesPhysical(latDirection, gtuType, other
                        .getDirection()))
                    {
                        if (otherLane.equals(record.getLane()))
                        {
                            if (latDirection.isLeft())
                            {
                                other.setLeft(record, gtuType);
                                record.setRight(other, gtuType);
                            }
                            else
                            {
                                other.setRight(record, gtuType);
                                record.setLeft(other, gtuType);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Creates a lane structure record and adds it to relevant maps.
     * @param lane Lane; lane
     * @param direction GTUDirectionality; direction
     * @param startDistanceSource RollingLaneStructureRecord; source of the start distance
     * @param recordLink RecordLink; record link
     * @param relativeLane RelativeLane; relative lane
     * @return created lane structure record
     */
    private RollingLaneStructureRecord constructRecord(final Lane lane, final GTUDirectionality direction,
        final RollingLaneStructureRecord startDistanceSource, final RecordLink recordLink, final RelativeLane relativeLane)
    {
        RollingLaneStructureRecord record = new RollingLaneStructureRecord(lane, direction, startDistanceSource, recordLink);
        if (!this.relativeLaneMap.containsKey(relativeLane))
        {
            this.relativeLaneMap.put(relativeLane, new LinkedHashSet<>());
        }
        this.relativeLaneMap.get(relativeLane).add(record);
        this.relativeLanes.put(record, relativeLane);
        return record;
    }

    /** {@inheritDoc} */
    @Override
    public final LaneStructureRecord getRootRecord()
    {
        return this.root.get();
    }

    /**
     * @param time Time; time to obtain the root at
     * @return rootRecord
     */
    public final LaneStructureRecord getRootRecord(final Time time)
    {
        return this.root.get(time);
    }

    /** {@inheritDoc} */
    @Override
    public final SortedSet<RelativeLane> getExtendedCrossSection()
    {
        return this.firstRecords.navigableKeySet();
    }

    /**
     * Returns the first record on the given lane. This is often a record in the current cross section, but it may be one
     * downstream for a lane that starts further downstream.
     * @param lane RelativeLane; lane
     * @return first record on the given lane, or {@code null} if no such record
     */
    @Override
    public final RollingLaneStructureRecord getFirstRecord(final RelativeLane lane)
    {
        if (this.firstRecords.containsKey(lane))
        {
            return this.firstRecords.get(lane);
        }
        // not in current cross section, get first via downstream
        RelativeLane rel = RelativeLane.CURRENT;
        int dMin = Integer.MAX_VALUE;
        for (RelativeLane relLane : this.crossSectionRecords.keySet())
        {
            if (relLane.getLateralDirectionality().equals(lane.getLateralDirectionality()))
            {
                int d = lane.getNumLanes() - relLane.getNumLanes();
                if (d < dMin)
                {
                    rel = relLane;
                    d = dMin;
                }
            }
        }
        RollingLaneStructureRecord record = this.crossSectionRecords.get(rel);
        // move downstream until a lateral move is made to the right relative lane
        while (rel.getNumLanes() < lane.getNumLanes())
        {
            RollingLaneStructureRecord adj = lane.getLateralDirectionality().isLeft() ? record.getLeft() : record.getRight();
            if (adj != null)
            {
                rel = lane.getLateralDirectionality().isLeft() ? rel.getLeft() : rel.getRight();
                record = adj;
            }
            else if (!record.getNext().isEmpty())
            {
                LaneDirection laneDir = new LaneDirection(record.getLane(), record.getDirection()).getNextLaneDirection(
                    this.containingGtu);
                if (laneDir == null)
                {
                    record = null;
                    break;
                }
                RollingLaneStructureRecord chosenNext = null;
                for (RollingLaneStructureRecord next : record.getNext())
                {
                    if (next.getLane().equals(laneDir.getLane()))
                    {
                        chosenNext = next;
                        break;
                    }
                }
                // Throw.when(chosenNext == null, RuntimeException.class,
                // "Unexpected exception while deriving first record not on the cross-section.");
                record = chosenNext;
                if (record == null)
                {
                    // TODO: Temporary fix for Aimsun demo
                    break;
                }
            }
            else
            {
                // reached a dead-end
                record = null;
                break;
            }
        }
        if (record != null)
        {
            // now move upstream until we are at x = 0
            while (record.getPrev().size() == 1 && record.getStartDistance().gt0())
            {
                record = record.getPrev().get(0);
            }
            this.firstRecords.put(lane, record);
        }
        return record;
    }

    /**
     * Retrieve objects of a specific type. Returns objects over a maximum length of the look ahead distance downstream from the
     * relative position, or as far as the lane map goes.
     * @param clazz Class&lt;T&gt;; class of objects to find
     * @param gtu LaneBasedGTU; gtu
     * @param pos RelativePosition.TYPE; relative position to start search from
     * @param <T> type of objects to find
     * @return Sorted set of objects of requested type per lane
     * @throws GTUException if lane is not in current set
     */
    @Override
    public final <T extends LaneBasedObject> Map<RelativeLane, SortedSet<Entry<T>>> getDownstreamObjects(
        final Class<T> clazz, final LaneBasedGTU gtu, final RelativePosition.TYPE pos) throws GTUException
    {
        Map<RelativeLane, SortedSet<Entry<T>>> out = new LinkedHashMap<>();
        for (RelativeLane relativeLane : this.relativeLaneMap.keySet())
        {
            out.put(relativeLane, getDownstreamObjects(relativeLane, clazz, gtu, pos));
        }
        return out;
    }

    /**
     * Retrieve objects on a lane of a specific type. Returns objects over a maximum length of the look ahead distance
     * downstream from the relative position, or as far as the lane map goes.
     * @param lane RelativeLane; lane
     * @param clazz Class&lt;T&gt;; class of objects to find
     * @param gtu LaneBasedGTU; gtu
     * @param pos RelativePosition.TYPE; relative position to start search from
     * @param <T> type of objects to find
     * @return Sorted set of objects of requested type
     * @throws GTUException if lane is not in current set
     */
    @Override
    @SuppressWarnings("unchecked")
    public final <T extends LaneBasedObject> SortedSet<Entry<T>> getDownstreamObjects(final RelativeLane lane,
        final Class<T> clazz, final LaneBasedGTU gtu, final RelativePosition.TYPE pos) throws GTUException
    {
        LaneStructureRecord record = getFirstRecord(lane);
        SortedSet<Entry<T>> set = new TreeSet<>();
        if (record != null)
        {
            double ds = gtu.getRelativePositions().get(pos).getDx().si - gtu.getReference().getDx().si;
            if (record.isDownstreamBranch())
            {
                // the list is ordered, but only for DIR_PLUS, need to do our own ordering
                Length minimumPosition;
                Length maximumPosition;
                if (record.getDirection().isPlus())
                {
                    minimumPosition = Length.instantiateSI(ds - record.getStartDistance().si);
                    maximumPosition = Length.instantiateSI(record.getLane().getLength().si);
                }
                else
                {
                    minimumPosition = Length.ZERO;
                    maximumPosition = Length.instantiateSI(record.getLane().getLength().si + record.getStartDistance().si
                        - ds);
                }

                for (LaneBasedObject object : record.getLane().getLaneBasedObjects(minimumPosition, maximumPosition))
                {
                    if (clazz.isAssignableFrom(object.getClass()) && ((record.getDirection().isPlus() && object
                        .getDirection().isForwardOrBoth()) || (record.getDirection().isMinus() && object.getDirection()
                            .isBackwardOrBoth())))
                    {
                        // unchecked, but the above isAssignableFrom assures correctness
                        double distance = record.getDistanceToPosition(object.getLongitudinalPosition()).si - ds;
                        if (distance <= this.lookAhead.si)
                        {
                            set.add(new Entry<>(Length.instantiateSI(distance), (T) object));
                        }
                    }
                }
            }
            getDownstreamObjectsRecursive(set, record, clazz, ds);
        }
        return set;
    }

    /**
     * Retrieve objects on a lane of a specific type. Returns objects over a maximum length of the look ahead distance
     * downstream from the relative position, or as far as the lane map goes. Objects on links not on the route are ignored.
     * @param lane RelativeLane; lane
     * @param clazz Class&lt;T&gt;; class of objects to find
     * @param gtu LaneBasedGTU; gtu
     * @param pos RelativePosition.TYPE; relative position to start search from
     * @param <T> type of objects to find
     * @param route Route; the route
     * @return Sorted set of objects of requested type
     * @throws GTUException if lane is not in current set
     */
    @Override
    public final <T extends LaneBasedObject> SortedSet<Entry<T>> getDownstreamObjectsOnRoute(final RelativeLane lane,
        final Class<T> clazz, final LaneBasedGTU gtu, final RelativePosition.TYPE pos, final Route route) throws GTUException
    {
        SortedSet<Entry<T>> set = getDownstreamObjects(lane, clazz, gtu, pos);
        if (route != null)
        {
            Iterator<Entry<T>> iterator = set.iterator();
            while (iterator.hasNext())
            {
                Entry<T> entry = iterator.next();
                CrossSectionLink link = entry.getLaneBasedObject().getLane().getParentLink();
                if (!route.contains(link.getStartNode()) || !route.contains(link.getEndNode()) || Math.abs(route.indexOf(link
                    .getStartNode()) - route.indexOf(link.getEndNode())) != 1)
                {
                    iterator.remove();
                }
            }
        }
        return set;
    }

    /**
     * Recursive search for lane based objects downstream.
     * @param set SortedSet&lt;Entry&lt;T&gt;&gt;; set to store entries into
     * @param record LaneStructureRecord; current record
     * @param clazz Class&lt;T&gt;; class of objects to find
     * @param ds double; distance from reference to chosen relative position
     * @param <T> type of objects to find
     */
    @SuppressWarnings("unchecked")
    private <T extends LaneBasedObject> void getDownstreamObjectsRecursive(final SortedSet<Entry<T>> set,
        final LaneStructureRecord record, final Class<T> clazz, final double ds)
    {
        if (record.getNext().isEmpty() || record.getNext().get(0).getStartDistance().gt(this.lookAhead))
        {
            return;
        }
        for (LaneStructureRecord next : record.getNext())
        {
            if (next.isDownstreamBranch())
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
                    if (clazz.isAssignableFrom(object.getClass()) && ((record.getDirection().isPlus() && object
                        .getDirection().isForwardOrBoth()) || (record.getDirection().isMinus() && object.getDirection()
                            .isBackwardOrBoth())))
                    {
                        // unchecked, but the above isAssignableFrom assures correctness
                        double distance = next.getDistanceToPosition(object.getLongitudinalPosition()).si - ds;
                        if (distance <= this.lookAhead.si)
                        {
                            set.add(new Entry<>(Length.instantiateSI(distance), (T) object));
                        }
                        else
                        {
                            return;
                        }
                    }
                }
            }
            getDownstreamObjectsRecursive(set, next, clazz, ds);
        }
    }

    /**
     * Retrieve objects of a specific type. Returns objects over a maximum length of the look ahead distance downstream from the
     * relative position, or as far as the lane map goes. Objects on links not on the route are ignored.
     * @param clazz Class&lt;T&gt;; class of objects to find
     * @param gtu LaneBasedGTU; gtu
     * @param pos RelativePosition.TYPE; relative position to start search from
     * @param <T> type of objects to find
     * @param route Route; the route
     * @return Sorted set of objects of requested type per lane
     * @throws GTUException if lane is not in current set
     */
    @Override
    public final <T extends LaneBasedObject> Map<RelativeLane, SortedSet<Entry<T>>> getDownstreamObjectsOnRoute(
        final Class<T> clazz, final LaneBasedGTU gtu, final RelativePosition.TYPE pos, final Route route) throws GTUException
    {
        Map<RelativeLane, SortedSet<Entry<T>>> out = new LinkedHashMap<>();
        for (RelativeLane relativeLane : this.relativeLaneMap.keySet())
        {
            out.put(relativeLane, getDownstreamObjectsOnRoute(relativeLane, clazz, gtu, pos, route));
        }
        return out;
    }

    /**
     * Retrieve objects on a lane of a specific type. Returns upstream objects from the relative position for as far as the lane
     * map goes. Distances to upstream objects are given as positive values.
     * @param lane RelativeLane; lane
     * @param clazz Class&lt;T&gt;; class of objects to find
     * @param gtu LaneBasedGTU; gtu
     * @param pos RelativePosition.TYPE; relative position to start search from
     * @param <T> type of objects to find
     * @return Sorted set of objects of requested type
     * @throws GTUException if lane is not in current set
     */
    @Override
    @SuppressWarnings("unchecked")
    public final <T extends LaneBasedObject> SortedSet<Entry<T>> getUpstreamObjects(final RelativeLane lane,
        final Class<T> clazz, final LaneBasedGTU gtu, final RelativePosition.TYPE pos) throws GTUException
    {
        SortedSet<Entry<T>> set = new TreeSet<>();
        LaneStructureRecord record = this.getFirstRecord(lane);
        if (record.getStartDistance().gt0())
        {
            return set; // this lane is only downstream
        }
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
        Length distance;
        for (LaneBasedObject object : record.getLane().getLaneBasedObjects(minimumPosition, maximumPosition))
        {
            if (clazz.isAssignableFrom(object.getClass()) && ((record.getDirection().isPlus() && object.getDirection()
                .isForwardOrBoth()) || (record.getDirection().isMinus() && object.getDirection().isBackwardOrBoth())))
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
     * @param set SortedSet&lt;Entry&lt;T&gt;&gt;; set to store entries into
     * @param record LaneStructureRecord; current record
     * @param clazz Class&lt;T&gt;; class of objects to find
     * @param ds Length; distance from reference to chosen relative position
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
                if (clazz.isAssignableFrom(object.getClass()) && ((record.getDirection().isPlus() && object.getDirection()
                    .isForwardOrBoth()) || (record.getDirection().isMinus() && object.getDirection().isBackwardOrBoth())))
                {
                    distance = prev.getDistanceToPosition(object.getLongitudinalPosition()).neg().minus(ds);
                    // unchecked, but the above isAssignableFrom assures correctness
                    set.add(new Entry<>(distance, (T) object));
                }
            }
            getUpstreamObjectsRecursive(set, prev, clazz, ds);
        }
    }

    /**
     * Print the lane structure as a number of lines in a String.
     * @param ls RollingLaneStructure; the lane structure to print
     * @param gtu LaneBasedGTU; the GTTU for which the lane structure is printed
     * @return a String with information about the RollingLaneStructire
     */
    public static String print(final RollingLaneStructure ls, final LaneBasedGTU gtu)
    {
        StringBuffer s = new StringBuffer();
        s.append(gtu.getSimulator().getSimulatorTime() + " " + gtu.getId() + " LANESTRUCTURE: ");
        for (LaneStructureRecord lsr : ls.relativeLanes.keySet())
        {
            s.append(lsr.toString() + "  ");
        }
        int totSize = 0;
        for (Set<RollingLaneStructureRecord> set : ls.relativeLaneMap.values())
        {
            totSize += set.size();
        }
        s.append("\n  relativeLanes.size()=" + ls.relativeLanes.size() + "  relativeLaneMap.totalSize()=" + totSize);
        return s.toString();
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "LaneStructure [rootLSR=" + this.root + "]";
    }

    /**
     * AnimationAccess provides access to a number of private fields in the structure, which should only be used read-only! <br>
     * <br>
     * Copyright (c) 2003-2020 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved.
     * See for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>.
     * The source code and binary code of this software is proprietary information of Delft University of Technology.
     * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
     */
    public class AnimationAccess
    {
        /**
         * @return the lane structure records of the cross section
         */
        @SuppressWarnings("synthetic-access")
        public TreeMap<RelativeLane, RollingLaneStructureRecord> getCrossSectionRecords()
        {
            return RollingLaneStructure.this.crossSectionRecords;
        }

        /**
         * @return the upstream edge
         */
        @SuppressWarnings("synthetic-access")
        public Set<RollingLaneStructureRecord> getUpstreamEdge()
        {
            return RollingLaneStructure.this.upstreamEdge;
        }

        /**
         * @return the downstream edge
         */
        @SuppressWarnings("synthetic-access")
        public Set<RollingLaneStructureRecord> getDownstreamEdge()
        {
            return RollingLaneStructure.this.downstreamEdge;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void notify(final EventInterface event) throws RemoteException
    {
        // triggers an update of the lane structure at the end of the final plan during the lane change, which is deviative
        this.previouslyDeviative = false;
    }
}
