package org.opentrafficsim.road.gtu.lane.perception;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypes;
import org.opentrafficsim.core.gtu.perception.AbstractPerception;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlanner;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * The perception module of a GTU based on lanes. It is responsible for perceiving (sensing) the environment of the GTU, which
 * includes the locations of other GTUs. Perception is done at a certain time, and the perceived information might have a
 * limited validity. In that sense, Perception is stateful. Information can be requested as often as needed, but will only be
 * recalculated when asked explicitly. This abstract class provides the building blocks for lane-based perception. <br>
 * Perception for lane-based GTUs involves information about GTUs in front of the owner GTU on the same lane (the 'leader' GTU),
 * parallel vehicles (important if we want to change lanes), distance to other vehicles on parallel lanes, as well in front as
 * to the back (important if we want to change lanes), and information about obstacles, traffic lights, speed signs, and ending
 * lanes.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 15, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public abstract class AbstractLanePerception extends AbstractPerception implements LanePerception
{

    /** */
    private static final long serialVersionUID = 20151128L;

    /** Lane structure to perform the perception with. */
    private LaneStructure laneStructure = null;

    /** Most recent update time of lane structure. */
    private Time updateTime = null;

    /**
     * Create a new LanePerception module. Because the constructor is often called inside the constructor of a GTU, this
     * constructor does not ask for the pointer to the GTU, as it is often impossible to provide at the time of construction.
     * Use the setter of the GTU instead.
     * @param gtu GTU
     */
    public AbstractLanePerception(final LaneBasedGTU gtu)
    {
        super(gtu);
    }

    /** {@inheritDoc} */
    @Override
    public final LaneBasedGTU getGtu()
    {
        return (LaneBasedGTU) super.getGtu();
    }

    /** {@inheritDoc} */
    @Override
    public final LaneStructure getLaneStructure() throws ParameterException
    {
        if (this.laneStructure == null || this.updateTime.lt(getGtu().getSimulator().getSimulatorTime().getTime()))
        {
            // downstream structure length
            Length down = getGtu().getBehavioralCharacteristics().getParameter(ParameterTypes.PERCEPTION);
            // upstream structure length
            Length up = getGtu().getBehavioralCharacteristics().getParameter(ParameterTypes.LOOKBACK);
            // structure length downstream of split on link not on route
            Length downSplit = getGtu().getBehavioralCharacteristics().getParameter(ParameterTypes.LOOKAHEAD);
            // structure length upstream of merge on link not on route
            Length upMerge = Length.max(up, downSplit);
            // negative values for upstream
            up = up.multiplyBy(-1.0);
            upMerge = upMerge.multiplyBy(-1.0);
            // Create Lane Structure
            DirectedLanePosition dlp;
            try
            {
                dlp = getGtu().getReferencePosition();
            }
            catch (GTUException exception)
            {
                // Should not happen, we get the lane from the GTU
                throw new RuntimeException("Could not get fraction on root lane.", exception);
            }
            Lane rootLane = dlp.getLane();
            GTUDirectionality direction = dlp.getGtuDirection();
            double fraction = dlp.getPosition().si / rootLane.getLength().si;
            LaneStructureRecord rootLSR =
                    new LaneStructureRecord(rootLane, direction, rootLane.getLength().multiplyBy(-fraction));
            this.laneStructure = new LaneStructure(rootLSR, downSplit);
            this.laneStructure.addLaneStructureRecord(rootLSR, RelativeLane.CURRENT);
            this.relativeLaneMap.clear();
            this.relativeLaneMap.put(rootLSR, RelativeLane.CURRENT);
            startBuild(rootLSR, fraction, getGtu().getGTUType(), down, downSplit, up, upMerge);

            // TODO possibly optimize by using a 'singleton' lane structure source, per GTUType
            // TODO possibly build and destroy at edges only
            this.updateTime = getGtu().getSimulator().getSimulatorTime().getTime(); 
        }
        return this.laneStructure;
    }

    /**
     * Local map where relative lanes are store per record, such that other records can be linked to the correct relative lane.
     */
    private final Map<LaneStructureRecord, RelativeLane> relativeLaneMap = new HashMap<>();

    /** Set of lanes that can be ignored as they are beyond build bounds. */
    private final Set<Lane> ignoreSet = new HashSet<>();

    /**
     * Starts the build from the current lane and creates an initial lateral set with correct start distances based on the
     * fraction.
     * 
     * <pre>
     *  ---------
     * |  /|\    |
     *  ---|-----
     * |  /|\    |
     *  ---|-----
     * |   o     | rootLSR
     *  ---|-----
     * |  \|/    |
     *  ---------
     *  
     * (---) fraction
     * </pre>
     * 
     * @param rootLSR record where the GTU is currently
     * @param fraction fractional position where the gtu is
     * @param gtuType GTU type
     * @param down maximum downstream distance to build structure
     * @param downSplit maximum downstream distance past split not following the route to build structure
     * @param up maximum upstream distance to build structure
     * @param upMerge maximum upstream distance upstream of downstream merges to build structure
     */
    private void startBuild(final LaneStructureRecord rootLSR, final double fraction, final GTUType gtuType, final Length down,
            final Length downSplit, final Length up, final Length upMerge)
    {
        // Build initial lateral set
        Set<LaneStructureRecord> recordSet = new HashSet<>();
        Set<Lane> laneSet = new HashSet<>();
        recordSet.add(rootLSR);
        laneSet.add(rootLSR.getLane());
        for (LateralDirectionality latDirection : new LateralDirectionality[] { LateralDirectionality.LEFT,
                LateralDirectionality.RIGHT })
        {
            LaneStructureRecord current = rootLSR;
            RelativeLane relativeLane = RelativeLane.CURRENT;
            while (!current.getLane().accessibleAdjacentLanes(latDirection, gtuType).isEmpty())
            {
                relativeLane = latDirection.isLeft() ? relativeLane.getLeft() : relativeLane.getRight();
                Lane lane = current.getLane().accessibleAdjacentLanes(latDirection, gtuType).iterator().next();
                LaneStructureRecord adjacentRecord =
                        constructRecord(lane, current.getDirection(), lane.getLength().multiplyBy(-fraction), relativeLane);
                if (latDirection.isLeft())
                {
                    if (lane.accessibleAdjacentLanes(LateralDirectionality.RIGHT, gtuType).contains(current.getLane()))
                    {
                        adjacentRecord.setRight(current);
                    }
                    current.setLeft(adjacentRecord);
                }
                else
                {
                    if (lane.accessibleAdjacentLanes(LateralDirectionality.LEFT, gtuType).contains(current.getLane()))
                    {
                        adjacentRecord.setLeft(current);
                    }
                    current.setRight(adjacentRecord);
                }
                recordSet.add(adjacentRecord);
                laneSet.add(lane);
                current = adjacentRecord;
            }
        }
        try
        {
            // System.out.println("START: downstream");
            this.ignoreSet.clear();
            buildDownstreamRecursive(recordSet, gtuType, down, up, downSplit, upMerge);
            // System.out.println("START: upstream");
            this.ignoreSet.clear();
            buildUpstreamRecursive(recordSet, gtuType, down, up, upMerge);
        }
        catch (GTUException | NetworkException exception)
        {
            throw new RuntimeException("Exception while building lane map.", exception);
        }
    }

    /**
     * Extends the lane structure with the downstream lanes of the current set. Per downstream link, a new set results, which
     * are expanded laterally before performing the next downstream step. If the lateral expansion finds new lanes, the
     * structure is expanded upstream from those over a limited distance.
     * 
     * <pre>
     *  --------- ---------
     * |       --|-)     --|-?A       ?: possible next steps
     *  --------- ---------
     * |       --|-)     --|-?A
     *  --------- =============       A, B: two separate downstream links
     * |       --|-)         --|-?B
     *  ----===== ------|------
     *     | C?(-|--   \|/   --|-?B   C: expand upstream if merge
     *      ----- -------------
     * </pre>
     * 
     * @param recordSet current lateral set of records
     * @param gtuType GTU type
     * @param down maximum downstream distance to build structure
     * @param up maximum upstream distance to build structure
     * @param downSplit maximum downstream distance past split not following the route to build structure
     * @param upMerge maximum upstream distance upstream of downstream merges to build structure
     * @throws GTUException if an inconsistency in the lane map is encountered
     * @throws NetworkException exception during movement over the network
     */
    private void buildDownstreamRecursive(final Set<LaneStructureRecord> recordSet, final GTUType gtuType, final Length down,
            final Length up, final Length downSplit, final Length upMerge) throws GTUException, NetworkException
    {
        // Loop lanes and put downstream lanes in sets per downstream link
        Map<Link, Set<Lane>> laneSets = new HashMap<>();
        Map<Link, TreeMap<RelativeLane, LaneStructureRecord>> recordSets = new HashMap<>();
        Map<Link, Length> maxStart = new HashMap<>();
        for (LaneStructureRecord laneRecord : recordSet)
        {
            if (!laneRecord.isCutOffEnd())
            {
                for (Lane nextLane : laneRecord.getLane().nextLanes(gtuType).keySet())
                {
                    Link nextLink = nextLane.getParentLink();
                    if (!laneSets.containsKey(nextLink))
                    {
                        laneSets.put(nextLink, new HashSet<>());
                        recordSets.put(nextLink, new TreeMap<>());
                        maxStart.put(nextLink, new Length(Double.MIN_VALUE, LengthUnit.SI));
                    }
                    laneSets.get(nextLink).add(nextLane);
                    RelativeLane relativeLane = this.relativeLaneMap.get(laneRecord);
                    Length start = laneRecord.getStartDistance().plus(laneRecord.getLane().getLength());
                    maxStart.put(nextLink, Length.max(maxStart.get(nextLink), start));
                    LaneStructureRecord nextRecord = constructRecord(nextLane,
                            laneRecord.getLane().nextLanes(gtuType).get(nextLane), start, relativeLane);
                    if (start.plus(nextLane.getLength()).ge(down))
                    {
                        nextRecord.setCutOffEnd(down.minus(start));
                        // System.out.println("  end cutoff at " + down.minus(start));
                    }
                    recordSets.get(nextLink).put(relativeLane, nextRecord);
                    laneRecord.addNext(nextRecord);
                    nextRecord.addPrev(laneRecord);
                }
            }
            else
            {
                for (Lane nextLane : laneRecord.getLane().nextLanes(gtuType).keySet())
                {
                    this.ignoreSet.add(nextLane); // beyond 'down', do not add in lateral step
                }
            }
        }
        // loop links to connect the lanes laterally and continue the build
        Link currentLink = recordSet.iterator().next().getLane().getParentLink();
        GTUDirectionality direction = recordSet.iterator().next().getDirection();
        for (Link link : laneSets.keySet())
        {
            connectLaterally(recordSets.get(link), gtuType);
            Set<LaneStructureRecord> set = new HashSet<>(recordSets.get(link).values()); // collection to set
            // System.out.println(">> LATERAL");
            set = extendLateral(set, gtuType, down, up, upMerge, true);
            // reduce remaining downstream length if not on route, to at most 'downSplit'
            Node nextNode = direction.isPlus() ? currentLink.getEndNode() : currentLink.getStartNode();
            Length downLimit = down;
            Route route = getGtu().getStrategicalPlanner().getRoute();
            if (route != null && (!route.contains(nextNode) // if no route, do not limit
                    || !((LaneBasedStrategicalRoutePlanner) getGtu().getStrategicalPlanner())
                            .nextLinkDirection(nextNode, currentLink, gtuType).equals(link)))
            {
                // as each lane has a separate start distance, use the maximum value from maxStart
                downLimit = Length.min(downLimit, maxStart.get(link).plus(downSplit));
            }
            // System.out.println(">> DOWNSTREAM");
            buildDownstreamRecursive(set, gtuType, downLimit, up, downSplit, upMerge);
        }
    }

    /**
     * Extends the lane structure with (multiple) left and right lanes of the current set. The extended lateral set is returned
     * for the downstream or upstream build to continue.
     * 
     * <pre>
     *  ---- ---------
     * | ?(-|-- /|\   |
     *  ---- ----|----   ?: extend upstream of merge if doMerge = true
     * | ?(-|-- /|\   |
     *  ---- ----|----  
     *      |         | } 
     *       ---------   } recordSet
     *      |         | }
     *       ----|---- 
     *      |   \|/   |
     *       ---------
     * </pre>
     * 
     * @param recordSet current lateral set of records
     * @param gtuType GTU type
     * @param down maximum downstream distance to build structure
     * @param up maximum upstream distance to build structure
     * @param upMerge maximum upstream distance upstream of downstream merges to build structure
     * @param downstreamBuild whether building downstream
     * @return laterally extended set
     * @throws GTUException if an inconsistency in the lane map is encountered
     * @throws NetworkException exception during movement over the network
     */
    private Set<LaneStructureRecord> extendLateral(final Set<LaneStructureRecord> recordSet, final GTUType gtuType,
            final Length down, final Length up, final Length upMerge, final boolean downstreamBuild)
            throws GTUException, NetworkException
    {
        Set<Lane> laneSet = new HashSet<>();
        for (LaneStructureRecord laneStructureRecord : recordSet)
        {
            laneSet.add(laneStructureRecord.getLane());
        }
        for (LateralDirectionality latDirection : new LateralDirectionality[] { LateralDirectionality.LEFT,
                LateralDirectionality.RIGHT })
        {
            Set<LaneStructureRecord> expandSet = new HashSet<>();
            Length startDistance = null;
            Length endDistance = null;
            for (LaneStructureRecord laneRecord : recordSet)
            {
                LaneStructureRecord current = laneRecord;
                startDistance = current.getStartDistance();
                endDistance = current.getStartDistance().plus(current.getLane().getLength());
                RelativeLane relativeLane = this.relativeLaneMap.get(laneRecord);
                while (!current.getLane().accessibleAdjacentLanes(latDirection, gtuType).isEmpty())
                {
                    Lane laneAdjacent = current.getLane().accessibleAdjacentLanes(latDirection, gtuType).iterator().next();
                    Length adjacentStart = downstreamBuild ? startDistance : endDistance.minus(laneAdjacent.getLength());
                    // skip is lane already in set, no effective length in structure, or in ignore list
                    if (!laneSet.contains(laneAdjacent) && !adjacentStart.plus(laneAdjacent.getLength()).le(up)
                            && !adjacentStart.ge(down) && !this.ignoreSet.contains(laneAdjacent))
                    {
                        laneSet.add(laneAdjacent);
                        relativeLane = latDirection.isLeft() ? relativeLane.getLeft() : relativeLane.getRight();
                        LaneStructureRecord recordAdjacent =
                                constructRecord(laneAdjacent, laneRecord.getDirection(), adjacentStart, relativeLane);
                        expandSet.add(recordAdjacent);
                        if (latDirection.isLeft())
                        {
                            if (laneAdjacent.accessibleAdjacentLanes(LateralDirectionality.RIGHT, gtuType)
                                    .contains(current.getLane()))
                            {
                                recordAdjacent.setRight(current);
                            }
                            current.setLeft(recordAdjacent);
                        }
                        else
                        {
                            if (laneAdjacent.accessibleAdjacentLanes(LateralDirectionality.LEFT, gtuType)
                                    .contains(current.getLane()))
                            {
                                recordAdjacent.setLeft(current);
                            }
                            current.setRight(recordAdjacent);
                        }
                        if (adjacentStart.plus(laneAdjacent.getLength()).ge(down))
                        {
                            recordAdjacent.setCutOffEnd(down.minus(adjacentStart));
                            // System.out.println("  end cutoff at " + down.minus(adjacentStart));
                        }
                        if (adjacentStart.le(up))
                        {
                            recordAdjacent.setCutOffStart(up.minus(adjacentStart));
                            // System.out.println("  start cutoff at " + up.minus(adjacentStart));
                        }
                        current = recordAdjacent;
                    }
                    else
                    {
                        break;
                    }
                }
            }
            if (downstreamBuild & !expandSet.isEmpty())
            {
                // limit search range and search upstream of merge
                // System.out.println(">> MERGE");
                buildUpstreamRecursive(expandSet, gtuType, down, startDistance.plus(upMerge), upMerge);
                // System.out.println("<< MERGE");
            }
            recordSet.addAll(expandSet);
        }
        return recordSet;
    }

    /**
     * Extends the lane structure with the upstream lanes of the current set. Per upstream link, a new set results, which are
     * expanded laterally before performing the next upstream step.
     * @param recordSet current lateral set of records
     * @param gtuType GTU type
     * @param down maximum downstream distance to build structure
     * @param up maximum upstream distance to build structure
     * @param upMerge maximum upstream distance upstream of downstream merges to build structure
     * @throws GTUException if an inconsistency in the lane map is encountered
     * @throws NetworkException exception during movement over the network
     */
    private void buildUpstreamRecursive(final Set<LaneStructureRecord> recordSet, final GTUType gtuType, final Length down,
            final Length up, final Length upMerge) throws GTUException, NetworkException
    {
        // Loop lanes and put upstream lanes in sets per upstream link
        Map<Link, Set<Lane>> laneSets = new HashMap<>();
        Map<Link, TreeMap<RelativeLane, LaneStructureRecord>> recordSets = new HashMap<>();
        Map<Link, Length> minStart = new HashMap<>();
        for (LaneStructureRecord laneRecord : recordSet)
        {
            if (!laneRecord.isCutOffStart())
            {
                for (Lane prevLane : laneRecord.getLane().prevLanes(gtuType).keySet())
                {
                    Link prevLink = prevLane.getParentLink();
                    if (!laneSets.containsKey(prevLink))
                    {
                        laneSets.put(prevLink, new HashSet<>());
                        recordSets.put(prevLink, new TreeMap<>());
                        minStart.put(prevLink, new Length(Double.MAX_VALUE, LengthUnit.SI));
                    }
                    laneSets.get(prevLink).add(prevLane);
                    RelativeLane relativeLane = this.relativeLaneMap.get(laneRecord);
                    Length start = laneRecord.getStartDistance().minus(prevLane.getLength());
                    minStart.put(prevLink, Length.min(minStart.get(prevLink), start));
                    LaneStructureRecord prevRecord = constructRecord(prevLane,
                            laneRecord.getLane().prevLanes(gtuType).get(prevLane), start, relativeLane);
                    if (start.le(up))
                    {
                        prevRecord.setCutOffStart(up.minus(start));
                        // System.out.println("  start cutoff at " + up.minus(start));
                    }
                    recordSets.get(prevLink).put(relativeLane, prevRecord);
                    laneRecord.addPrev(prevRecord);
                    prevRecord.addNext(laneRecord);
                }
            }
            else
            {
                for (Lane prevLane : laneRecord.getLane().prevLanes(gtuType).keySet())
                {
                    this.ignoreSet.add(prevLane); // beyond 'up', do not add in lateral step
                }
            }
        }
        // loop links to connect the lanes laterally and continue the build
        for (Link link : laneSets.keySet())
        {
            connectLaterally(recordSets.get(link), gtuType);
            Set<LaneStructureRecord> set = new HashSet<>(recordSets.get(link).values()); // collection to set
            // System.out.println(">> LATERAL");
            set = extendLateral(set, gtuType, down, up, upMerge, false);
            // System.out.println(">> UPSTREAM");
            buildUpstreamRecursive(set, gtuType, down, up, upMerge);
        }
    }

    /**
     * Creates a lane structure record and adds it to relevant maps.
     * @param lane lane
     * @param direction direction
     * @param startDistance distance at start of record
     * @param relativeLane relative lane
     * @return created lane structure record
     */
    private LaneStructureRecord constructRecord(final Lane lane, final GTUDirectionality direction, final Length startDistance,
            final RelativeLane relativeLane)
    {
        // System.out.println("Record from " + startDistance + " till " + startDistance.plus(lane.getLength()));
        LaneStructureRecord record = new LaneStructureRecord(lane, direction, startDistance);
        this.laneStructure.addLaneStructureRecord(record, relativeLane);
        this.relativeLaneMap.put(record, relativeLane);
        return record;
    }

    /**
     * Connects the lane structure records laterally if appropriate.
     * @param map Map<RelativeLane, LaneStructureRecord>; map
     * @param gtuType gtu type
     */
    private void connectLaterally(final Map<RelativeLane, LaneStructureRecord> map, final GTUType gtuType)
    {
        for (RelativeLane relativeLane : map.keySet())
        {
            if (map.containsKey(relativeLane.getRight()))
            {
                Lane thisLane = map.get(relativeLane).getLane();
                Lane rightLane = map.get(relativeLane.getRight()).getLane();
                if (thisLane.accessibleAdjacentLanes(LateralDirectionality.RIGHT, gtuType).contains(rightLane))
                {
                    map.get(relativeLane).setRight(map.get(relativeLane.getRight()));
                }
                if (rightLane.accessibleAdjacentLanes(LateralDirectionality.LEFT, gtuType).contains(thisLane))
                {
                    map.get(relativeLane.getRight()).setLeft(map.get(relativeLane));
                }
            }
        }
    }

    /** {@inheritDoc} */
    public final EnvironmentState getEnvironmentState()
    {
        return this.laneStructure;
    }

}
