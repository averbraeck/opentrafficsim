package org.opentrafficsim.road.network.lane;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.djunits.unit.LengthUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.network.lane.changing.OvertakingConditions;
import org.opentrafficsim.road.network.lane.object.AbstractLaneBasedObject;
import org.opentrafficsim.road.network.lane.object.LaneBasedObject;
import org.opentrafficsim.road.network.lane.object.sensor.AbstractSensor;
import org.opentrafficsim.road.network.lane.object.sensor.SingleSensor;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEvent;
import nl.tudelft.simulation.event.EventType;
import nl.tudelft.simulation.immutablecollections.Immutable;
import nl.tudelft.simulation.immutablecollections.ImmutableArrayList;
import nl.tudelft.simulation.immutablecollections.ImmutableList;
import nl.tudelft.simulation.language.Throw;

/**
 * The Lane is the CrossSectionElement of a CrossSectionLink on which GTUs can drive. The Lane stores several important
 * properties, such as the successor lane(s), predecessor lane(s), and adjacent lane(s), all separated per GTU type. It can, for
 * instance, be that a truck is not allowed to move into an adjacent lane, while a car is allowed to do so. Furthermore, the
 * lane contains sensors that can be triggered by passing GTUs. The Lane class also contains methods to determine to trigger the
 * sensors at exactly calculated and scheduled times, given the movement of the GTUs. <br>
 * Finally, the Lane stores the GTUs on the lane, and contains several access methods to determine successor and predecessor
 * GTUs, as well as methods to add a GTU to a lane (either at the start or in the middle when changing lanes), and remove a GTU
 * from the lane (either at the end, or in the middle when changing onto another lane).
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-09-24 14:17:07 +0200 (Thu, 24 Sep 2015) $, @version $Revision: 1407 $, by $Author: averbraeck $,
 * initial version Aug 19, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class Lane extends CrossSectionElement implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150826L;

    /** Type of lane to deduce compatibility with GTU types. */
    private final LaneType laneType;

    /**
     * SHOULD NOT BE IN Lane (but in LaneType). The directions in which vehicles can drive, i.e., in direction of geometry,
     * reverse, or both. This can differ per GTU type. In an overtake lane, cars might overtake and trucks not. It might be that
     * the lane (e.g., a street in a city) is FORWARD (from start node of the link to end node of the link) for the GTU type
     * CAR, but BOTH for the GTU type BICYCLE (i.e., bicycles can also go in the other direction, opposite to the drawing
     * direction of the Link). If the directionality for a GTUType is set to NONE, this means that the given GTUType cannot use
     * the Lane. If a Directionality is set for GTUType.ALL, the getDirectionality will default to these settings when there is
     * no specific entry for a given directionality. This means that the settings can be used additive, or restrictive. <br>
     * In <b>additive use</b>, set the directionality for GTUType.ALL to NONE, or do not set the directionality for GTUType.ALL.
     * Now, one by one, the allowed directionalities can be added. An example is a lane on a highway, which we only open for
     * CAR, TRUCK and BUS. <br>
     * In <b>restrictive use</b>, set the directionality for GTUType.ALL to BOTH, FORWARD, or BACKWARD. Override the
     * directionality for certain GTUTypes to a more restrictive access, e.g. to NONE. An example is a lane that is open for all
     * road users, except TRUCK.
     */
    // private final Map<GTUType, LongitudinalDirectionality> directionalityMap;

    /**
     * The speed limit of this lane, which can differ per GTU type. Cars might be allowed to drive 120 km/h and trucks 90 km/h.
     * If the speed limit is the same for all GTU types, GTUType.ALL will be used. This means that the settings can be used
     * additive, or subtractive. <br>
     * In <b>additive use</b>, do not set the speed limit for GTUType.ALL. Now, one by one, the allowed maximum speeds for each
     * of the GTU Types have be added. Do this when there are few GTU types or the speed limits per TU type are very different. <br>
     * In <b>subtractive use</b>, set the speed limit for GTUType.ALL to the most common one. Override the speed limit for
     * certain GTUTypes to a different value. An example is a lane on a highway where all vehicles, except truck (CAR, BUS,
     * MOTORCYCLE, etc.), can drive 120 km/h, but trucks are allowed only 90 km/h. In that case, set the speed limit for
     * GTUType.ALL to 120 km/h, and for TRUCK to 90 km/h.
     */
    // TODO allow for direction-dependent speed limit
    private Map<GTUType, Speed> speedLimitMap;

    /**
     * Sensors on the lane to trigger behavior of the GTU, sorted by longitudinal position. The triggering of sensors is done
     * per GTU type, so different GTUs can trigger different sensors.
     */
    // TODO allow for direction-dependent sensors
    private final SortedMap<Double, List<SingleSensor>> sensors = new TreeMap<>();

    /**
     * Objects on the lane can be observed by the GTU. Examples are signs, speed signs, blocks, and traffic lights. They are
     * sorted by longitudinal position.
     */
    // TODO allow for direction-dependent lane objects
    private final SortedMap<Double, List<LaneBasedObject>> laneBasedObjects = new TreeMap<>();

    /** GTUs ordered by increasing longitudinal position; increasing in the direction of the center line. */
    private final List<LaneBasedGTU> gtuList = new ArrayList<LaneBasedGTU>();

    /**
     * Adjacent left lanes that some GTU types can change onto. Left is defined relative to the direction of the design line of
     * the link (and the direction of the center line of the lane). In terms of offsets, 'left' lanes always have a more
     * positive offset than the current lane. Initially null so we can calculate and cache the first time the method is called.
     */
    private Map<GTUType, Set<Lane>> leftNeighbors = null;

    /**
     * Adjacent right lanes that some GTU types can change onto. Right is defined relative to the direction of the design line
     * of the link (and the direction of the center line of the lane). In terms of offsets, 'right' lanes always have a more
     * negative offset than the current lane. Initially null so we can calculate and cache the first time the method is called.
     */
    private Map<GTUType, Set<Lane>> rightNeighbors = null;

    /**
     * Next lane(s) following this lane that some GTU types can drive from or onto. Next is defined in the direction of the
     * design line. Initially null so we can calculate and cache the first time the method is called.
     */
    private Map<GTUType, Map<Lane, GTUDirectionality>> nextLanes = null;

    /**
     * Previous lane(s) preceding this lane that some GTU types can drive from or onto. Previous is defined relative to the
     * direction of the design line. Initially null so we can calculate and cache the first time the method is called.
     */
    private Map<GTUType, Map<Lane, GTUDirectionality>> prevLanes = null;

    /**
     * Downstream lane(s) following this lane that some GTU types can drive onto given the direction. Initially null so we can
     * calculate and cache the first time the method is called.
     */
    private Map<GTUType, Map<GTUDirectionality, Map<Lane, GTUDirectionality>>> downLanes = null;

    /**
     * Previous lane(s) preceding this lane that some GTU types can drive from given the direction. Initially null so we can
     * calculate and cache the first time the method is called.
     */
    private Map<GTUType, Map<GTUDirectionality, Map<Lane, GTUDirectionality>>> upLanes = null;

    /** The conditions for overtaking another GTU, viewed from this lane. */
    // TODO allow for direction-dependent overtaking conditions
    private final OvertakingConditions overtakingConditions;

    /**
     * The <b>timed</b> event type for pub/sub indicating the addition of a GTU to the lane. <br>
     * Payload: Object[] {String gtuId, LaneBasedGTU gtu, int count_after_addition}
     */
    public static final EventType GTU_ADD_EVENT = new EventType("GTU.ADD");

    /**
     * The <b>timed</b> event type for pub/sub indicating the removal of a GTU from the lane. <br>
     * Payload: Object[] {String gtuId, LaneBasedGTU gtu, int count_after_removal, Length position}
     */
    public static final EventType GTU_REMOVE_EVENT = new EventType("GTU.REMOVE");

    /**
     * The <b>timed</b> event type for pub/sub indicating the addition of a Sensor to the lane. <br>
     * Payload: Object[] {String sensorId, Sensor sensor}
     */
    public static final EventType SENSOR_ADD_EVENT = new EventType("SENSOR.ADD");

    /**
     * The <b>timed</b> event type for pub/sub indicating the removal of a Sensor from the lane. <br>
     * Payload: Object[] {String sensorId, Sensor sensor}
     */
    public static final EventType SENSOR_REMOVE_EVENT = new EventType("SENSOR.REMOVE");

    /**
     * The event type for pub/sub indicating the addition of a LaneBasedObject to the lane. <br>
     * Payload: Object[] {LaneBasedObject laneBasedObject}
     */
    public static final EventType OBJECT_ADD_EVENT = new EventType("OBJECT.ADD");

    /**
     * The event type for pub/sub indicating the removal of a LaneBasedObject from the lane. <br>
     * Payload: Object[] {LaneBasedObject laneBasedObject}
     */
    public static final EventType OBJECT_REMOVE_EVENT = new EventType("OBJECT.REMOVE");

    /**
     * Construct a new Lane.
     * @param parentLink CrossSectionLink; the link to which the new Lane will belong (must be constructed first)
     * @param id String; the id of this lane within the link; should be unique within the link.
     * @param lateralOffsetAtStart Length; the lateral offset of the design line of the new CrossSectionLink with respect to the
     *            design line of the parent Link at the start of the parent Link
     * @param lateralOffsetAtEnd Length; the lateral offset of the design line of the new CrossSectionLink with respect to the
     *            design line of the parent Link at the end of the parent Link
     * @param beginWidth Length; start width, positioned <i>symmetrically around</i> the design line
     * @param endWidth Length; end width, positioned <i>symmetrically around</i> the design line
     * @param laneType LaneType; the type of lane to deduce compatibility with GTU types
     * @param speedLimitMap Map&lt;GTUType, Speed&gt;; speed limit on this lane, specified per GTU Type
     * @param overtakingConditions OvertakingConditions; the conditions for overtaking another GTU, viewed from this lane
     * @throws OTSGeometryException when creation of the center line or contour geometry fails
     * @throws NetworkException when id equal to null or not unique
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public Lane(final CrossSectionLink parentLink, final String id, final Length lateralOffsetAtStart,
            final Length lateralOffsetAtEnd, final Length beginWidth, final Length endWidth, final LaneType laneType,
            final Map<GTUType, Speed> speedLimitMap, final OvertakingConditions overtakingConditions)
            throws OTSGeometryException, NetworkException
    {
        super(parentLink, id, lateralOffsetAtStart, lateralOffsetAtEnd, beginWidth, endWidth);
        this.laneType = laneType;
        checkDirectionality();
        this.speedLimitMap = speedLimitMap;
        this.overtakingConditions = overtakingConditions;
    }

    /**
     * Construct a new Lane.
     * @param parentLink CrossSectionLink; the link to which the element will belong (must be constructed first)
     * @param id String; the id of this lane within the link; should be unique within the link.
     * @param lateralOffsetAtStart Length; the lateral offset of the design line of the new CrossSectionLink with respect to the
     *            design line of the parent Link at the start of the parent Link
     * @param lateralOffsetAtEnd Length; the lateral offset of the design line of the new CrossSectionLink with respect to the
     *            design line of the parent Link at the end of the parent Link
     * @param beginWidth Length; start width, positioned <i>symmetrically around</i> the design line
     * @param endWidth Length; end width, positioned <i>symmetrically around</i> the design line
     * @param laneType LaneType; the type of lane to deduce compatibility with GTU types
     * @param speedLimit Speed; speed limit on this lane
     * @param overtakingConditions OvertakingConditions; the conditions for overtaking another GTU, viewed from this lane
     * @throws OTSGeometryException when creation of the center line or contour geometry fails
     * @throws NetworkException when id equal to null or not unique
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public Lane(final CrossSectionLink parentLink, final String id, final Length lateralOffsetAtStart,
            final Length lateralOffsetAtEnd, final Length beginWidth, final Length endWidth, final LaneType laneType,
            final Speed speedLimit, final OvertakingConditions overtakingConditions) throws OTSGeometryException,
            NetworkException
    {
        super(parentLink, id, lateralOffsetAtStart, lateralOffsetAtEnd, beginWidth, endWidth);
        this.laneType = laneType;
        checkDirectionality();
        this.speedLimitMap = new LinkedHashMap<>();
        this.speedLimitMap.put(GTUType.CAR, speedLimit);
        this.overtakingConditions = overtakingConditions;
    }

    /**
     * Construct a new Lane.
     * @param parentLink CrossSectionLink; the link to which the element will belong (must be constructed first)
     * @param id String; the id of this lane within the link; should be unique within the link.
     * @param lateralOffset Length; the lateral offset of the design line of the new CrossSectionLink with respect to the design
     *            line of the parent Link
     * @param width Length; width, positioned <i>symmetrically around</i> the design line
     * @param laneType type of lane to deduce compatibility with GTU types
     * @param speedLimitMap Map&lt;GTUType, Speed&gt;; the speed limit on this lane, specified per GTU Type
     * @param overtakingConditions OvertakingConditions; the conditions for overtaking another GTU, viewed from this lane
     * @throws OTSGeometryException when creation of the center line or contour geometry fails
     * @throws NetworkException when id equal to null or not unique
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public Lane(final CrossSectionLink parentLink, final String id, final Length lateralOffset, final Length width,
            final LaneType laneType, final Map<GTUType, Speed> speedLimitMap, final OvertakingConditions overtakingConditions)
            throws OTSGeometryException, NetworkException
    {
        super(parentLink, id, lateralOffset, width);
        this.laneType = laneType;
        checkDirectionality();
        this.speedLimitMap = speedLimitMap;
        this.overtakingConditions = overtakingConditions;
    }

    /**
     * Construct a new Lane.
     * @param parentLink CrossSectionLink; the link to which the element belongs (must be constructed first)
     * @param id String; the id of this lane within the link; should be unique within the link
     * @param lateralOffset Length; the lateral offset of the design line of the new CrossSectionLink with respect to the design
     *            line of the parent Link
     * @param width Length; width, positioned <i>symmetrically around</i> the design line
     * @param laneType LaneType; the type of lane to deduce compatibility with GTU types
     * @param speedLimit Speed; the speed limit on this lane
     * @param overtakingConditions OvertakingConditions; the conditions for overtaking another GTU, viewed from this lane
     * @throws OTSGeometryException when creation of the center line or contour geometry fails
     * @throws NetworkException when id equal to null or not unique
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public Lane(final CrossSectionLink parentLink, final String id, final Length lateralOffset, final Length width,
            final LaneType laneType, final Speed speedLimit, final OvertakingConditions overtakingConditions)
            throws OTSGeometryException, NetworkException
    {
        super(parentLink, id, lateralOffset, width);
        this.laneType = laneType;
        checkDirectionality();
        this.speedLimitMap = new LinkedHashMap<>();
        this.speedLimitMap.put(GTUType.CAR, speedLimit);
        this.overtakingConditions = overtakingConditions;
    }

    /**
     * Construct a new Lane.
     * @param parentLink CrossSectionLink; the link to which the element belongs (must be constructed first)
     * @param id String; the id of this lane within the link; should be unique within the link.
     * @param crossSectionSlices List&lt;CrossSectionSlice&gt;; the offsets and widths at positions along the line, relative to
     *            the design line of the parent link. If there is just one with and offset, there should just be one element in
     *            the list with Length = 0. If there are more slices, the last one should be at the length of the design line.
     *            If not, a NetworkException is thrown.
     * @param laneType LaneType; the type of lane to deduce compatibility with GTU types
     * @param speedLimitMap Map&lt;GTUType, Speed&gt;; the speed limit on this lane, specified per GTU Type
     * @param overtakingConditions OvertakingConditions; the conditions for overtaking another GTU, viewed from this lane
     * @throws OTSGeometryException when creation of the center line or contour geometry fails
     * @throws NetworkException when id equal to null or not unique
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public Lane(final CrossSectionLink parentLink, final String id, final List<CrossSectionSlice> crossSectionSlices,
            final LaneType laneType, final Map<GTUType, Speed> speedLimitMap, final OvertakingConditions overtakingConditions)
            throws OTSGeometryException, NetworkException
    {
        super(parentLink, id, crossSectionSlices);
        this.laneType = laneType;
        checkDirectionality();
        this.speedLimitMap = speedLimitMap;
        this.overtakingConditions = overtakingConditions;
    }

    /**
     * Construct a new Lane.
     * @param parentLink CrossSectionLink; the link to which the element belongs (must be constructed first)
     * @param id String; the id of this lane within the link; should be unique within the link.
     * @param crossSectionSlices List&lt;CrossSectionSlice&gt;; the offsets and widths at positions along the line, relative to
     *            the design line of the parent link. If there is just one with and offset, there should just be one element in
     *            the list with Length = 0. If there are more slices, the last one should be at the length of the design line.
     *            If not, a NetworkException is thrown.
     * @param laneType LaneType; the type of lane to deduce compatibility with GTU types
     * @param speedLimit Speed; the speed limit on this lane
     * @param overtakingConditions OvertakingCondition; the conditions for overtaking another GTU, viewed from this lane
     * @throws OTSGeometryException when creation of the center line or contour geometry fails
     * @throws NetworkException when id equal to null or not unique
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public Lane(final CrossSectionLink parentLink, final String id, final List<CrossSectionSlice> crossSectionSlices,
            final LaneType laneType, final Speed speedLimit, final OvertakingConditions overtakingConditions)
            throws OTSGeometryException, NetworkException
    {
        super(parentLink, id, crossSectionSlices);
        this.laneType = laneType;
        checkDirectionality();
        this.speedLimitMap = new LinkedHashMap<>();
        this.speedLimitMap.put(GTUType.CAR, speedLimit);
        this.overtakingConditions = overtakingConditions;
    }

    /**
     * Clone a Lane for a new network.
     * @param newParentLink the new link to which the clone belongs
     * @param newSimulator the new simulator for this network
     * @param animation whether to (re)create animation or not
     * @param cse the element to clone from
     * @throws NetworkException if link already exists in the network, if name of the link is not unique, or if the start node
     *             or the end node of the link are not registered in the network.
     */
    protected Lane(final CrossSectionLink newParentLink, final OTSSimulatorInterface newSimulator, final boolean animation,
            final Lane cse) throws NetworkException
    {
        super(newParentLink, newSimulator, animation, cse);
        this.laneType = cse.laneType;
        this.speedLimitMap = new HashMap<GTUType, Speed>(cse.speedLimitMap);
        this.overtakingConditions = cse.overtakingConditions;

        if (animation)
        {
            OTSNetwork.cloneAnimation(cse, this, cse.getParentLink().getSimulator(), newSimulator);
        }
    }

    // TODO constructor calls with this(...)

    /**
     * Retrieve one of the sets of neighboring Lanes that is accessible for the given type of GTU. A defensive copy of the
     * internal data structure is returned.
     * @param direction LateralDirectionality; either LEFT or RIGHT, relative to the DESIGN LINE of the link (and the direction
     *            of the center line of the lane). In terms of offsets, 'left' lanes always have a more positive offset than the
     *            current lane, and 'right' lanes a more negative offset.
     * @param gtuType GTUType; the GTU type to check the accessibility for
     * @param drivingDirection GTUDirectionality; driving direction of the GTU
     * @return Set&lt;Lane&gt;; the indicated set of neighboring Lanes
     */
    private Set<Lane> neighbors(final LateralDirectionality direction, final GTUType gtuType,
            final GTUDirectionality drivingDirection)
    {
        if (this.leftNeighbors == null || this.rightNeighbors == null)
        {
            this.leftNeighbors = new LinkedHashMap<>(1);
            this.rightNeighbors = new LinkedHashMap<>(1);
        }

        if (!this.leftNeighbors.containsKey(gtuType) || !this.rightNeighbors.containsKey(gtuType))
        {
            Set<Lane> leftSet = new LinkedHashSet<>(1);
            Set<Lane> rightSet = new LinkedHashSet<>(1);
            this.leftNeighbors.put(gtuType, leftSet);
            this.rightNeighbors.put(gtuType, rightSet);
            for (CrossSectionElement cse : this.parentLink.getCrossSectionElementList())
            {
                if (cse instanceof Lane && !cse.equals(this))
                {
                    Lane lane = (Lane) cse;
                    if (laterallyAdjacentAndAccessible(lane, LateralDirectionality.LEFT, gtuType, drivingDirection))
                    {
                        leftSet.add(lane);
                    }
                    if (laterallyAdjacentAndAccessible(lane, LateralDirectionality.RIGHT, gtuType, drivingDirection))
                    {
                        rightSet.add(lane);
                    }
                }
            }
        }

        Set<Lane> lanes = new LinkedHashSet<>();
        if (direction.equals(LateralDirectionality.LEFT))
        {
            lanes.addAll(this.leftNeighbors.get(gtuType));
        }
        else
        {
            lanes.addAll(this.rightNeighbors.get(gtuType));
        }
        return lanes;
    }

    /** Lateral alignment margin for longitudinally connected Lanes. */
    static final Length ADJACENT_MARGIN = new Length(0.2, LengthUnit.METER);

    /**
     * Determine whether another lane is adjacent to this lane (dependent on distance) and accessible (dependent on stripes) for
     * a certain GTU type (dependent on usability of the adjacent lane for that GTU type). This method assumes that when there
     * is NO stripe between two adjacent lanes that are accessible for the GTU type, the GTU can enter that lane. <br>
     * @param lane Lane; the other lane to evaluate
     * @param direction LateralDirectionality; the direction to look at, relative to the DESIGN LINE of the link. This is a very
     *            important aspect to note: all information is stored relative to the direction of the design line, and not in a
     *            driving direction, which can vary for lanes that can be driven in two directions (e.g. at overtaking).
     * @param gtuType GTUType; the GTU type to check the accessibility for
     * @param drivingDirection GTUDirectionality; driving direction of the GTU
     * @return boolean; true if the other lane is adjacent to this lane and accessible for the given GTU type; false otherwise
     */
    private boolean laterallyAdjacentAndAccessible(final Lane lane, final LateralDirectionality direction,
            final GTUType gtuType, final GTUDirectionality drivingDirection)
    {
        if (!lane.getLaneType().isCompatible(gtuType, drivingDirection))
        {
            // not accessible for the given GTU type
            return false;
        }

        if (direction.equals(LateralDirectionality.LEFT))
        {
            // TODO take the cross section slices into account...
            if (Math.abs((getDesignLineOffsetAtBegin().getSI() + getBeginWidth().getSI() / 2.0)
                    - (lane.getDesignLineOffsetAtBegin().getSI() - lane.getBeginWidth().getSI() / 2.0)) < ADJACENT_MARGIN
                        .getSI()
                    && Math.abs((getDesignLineOffsetAtEnd().getSI() + getEndWidth().getSI() / 2.0)
                            - (lane.getDesignLineOffsetAtEnd().getSI() - lane.getEndWidth().getSI() / 2.0)) < ADJACENT_MARGIN
                                .getSI())
            {
                // look at stripes between the two lanes
                for (CrossSectionElement cse : this.parentLink.getCrossSectionElementList())
                {
                    if (cse instanceof Stripe)
                    {
                        Stripe stripe = (Stripe) cse;
                        // TODO take the cross section slices into account...
                        if (Math.abs((getDesignLineOffsetAtBegin().getSI() + getBeginWidth().getSI() / 2.0)
                                - stripe.getDesignLineOffsetAtBegin().getSI()) < ADJACENT_MARGIN.getSI()
                                && Math.abs((getDesignLineOffsetAtEnd().getSI() + getEndWidth().getSI() / 2.0)
                                        - stripe.getDesignLineOffsetAtEnd().getSI()) < ADJACENT_MARGIN.getSI())
                        {
                            if (!stripe.isPermeable(gtuType, LateralDirectionality.LEFT))
                            {
                                // there is a stripe forbidding to cross to the adjacent lane
                                return false;
                            }
                        }
                    }
                }
                // the lanes are adjacent, and there is no stripe forbidding us to enter that lane
                // or there is no stripe at all
                return true;
            }
        }

        else
        // direction.equals(LateralDirectionality.RIGHT)
        {
            // TODO take the cross section slices into account...
            if (Math.abs((getDesignLineOffsetAtBegin().getSI() - getBeginWidth().getSI() / 2.0)
                    - (lane.getDesignLineOffsetAtBegin().getSI() + lane.getBeginWidth().getSI() / 2.0)) < ADJACENT_MARGIN
                        .getSI()
                    && Math.abs((getDesignLineOffsetAtEnd().getSI() - getEndWidth().getSI() / 2.0)
                            - (lane.getDesignLineOffsetAtEnd().getSI() + lane.getEndWidth().getSI() / 2.0)) < ADJACENT_MARGIN
                                .getSI())
            {
                // look at stripes between the two lanes
                for (CrossSectionElement cse : this.parentLink.getCrossSectionElementList())
                {
                    if (cse instanceof Stripe)
                    {
                        Stripe stripe = (Stripe) cse;
                        // TODO take the cross section slices into account...
                        if (Math.abs((getDesignLineOffsetAtBegin().getSI() - getBeginWidth().getSI() / 2.0)
                                - stripe.getDesignLineOffsetAtBegin().getSI()) < ADJACENT_MARGIN.getSI()
                                && Math.abs((getDesignLineOffsetAtEnd().getSI() - getEndWidth().getSI() / 2.0)
                                        - stripe.getDesignLineOffsetAtEnd().getSI()) < ADJACENT_MARGIN.getSI())
                        {
                            if (!stripe.isPermeable(gtuType, LateralDirectionality.RIGHT))
                            {
                                // there is a stripe forbidding to cross to the adjacent lane
                                return false;
                            }
                        }
                    }
                }
                // the lanes are adjacent, and there is no stripe forbidding us to enter that lane
                // or there is no stripe at all
                return true;
            }
        }

        // no lanes were found that are close enough laterally.
        return false;
    }

    /**
     * Insert a sensor at the right place in the sensor list of this Lane.
     * @param sensor Sensor; the sensor to add
     * @throws NetworkException when the position of the sensor is beyond (or before) the range of this Lane
     */
    public final void addSensor(final SingleSensor sensor) throws NetworkException
    {
        double position = sensor.getLongitudinalPosition().si;
        if (position < 0 || position > getLength().getSI())
        {
            throw new NetworkException("Illegal position for sensor " + position + " valid range is 0.."
                    + getLength().getSI());
        }
        if (this.parentLink.getNetwork().containsObject(sensor.getFullId()))
        {
            throw new NetworkException("Network already contains an object with the name " + sensor.getFullId());
        }
        List<SingleSensor> sensorList = this.sensors.get(position);
        if (null == sensorList)
        {
            sensorList = new ArrayList<SingleSensor>(1);
            this.sensors.put(position, sensorList);
        }
        sensorList.add(sensor);
        this.parentLink.getNetwork().addObject(sensor);
        fireTimedEvent(Lane.SENSOR_ADD_EVENT, new Object[] { sensor.getId(), sensor }, sensor.getSimulator()
                .getSimulatorTime());
    }

    /**
     * Remove a sensor from the sensor list of this Lane.
     * @param sensor Sensoe; the sensor to remove.
     * @throws NetworkException when the sensor was not found on this Lane
     */
    public final void removeSensor(final SingleSensor sensor) throws NetworkException
    {
        fireTimedEvent(Lane.SENSOR_REMOVE_EVENT, new Object[] { sensor.getId(), sensor }, sensor.getSimulator()
                .getSimulatorTime());
        List<SingleSensor> sensorList = this.sensors.get(sensor.getLongitudinalPosition().si);
        if (null == sensorList)
        {
            throw new NetworkException("No sensor at " + sensor.getLongitudinalPosition().si);
        }
        sensorList.remove(sensor);
        if (sensorList.size() == 0)
        {
            this.sensors.remove(sensor.getLongitudinalPosition().si);
        }
        this.parentLink.getNetwork().removeObject(sensor);
    }

    /**
     * Retrieve the list of Sensors of this Lane in the specified distance range for the given GTUType. The resulting list is a
     * defensive copy.
     * @param minimumPosition Length; the minimum distance on the Lane (inclusive)
     * @param maximumPosition Length; the maximum distance on the Lane (inclusive)
     * @param gtuType the GTU type to provide the sensors for
     * @param direction GTUDirectionality; direction of movement of the GTU
     * @return List&lt;Sensor&gt;; list of the sensor in the specified range. This is a defensive copy.
     */
    public final List<SingleSensor> getSensors(final Length minimumPosition, final Length maximumPosition,
            final GTUType gtuType, final GTUDirectionality direction)
    {
        List<SingleSensor> sensorList = new ArrayList<>(1);
        for (List<SingleSensor> sl : this.sensors.values())
        {
            for (SingleSensor sensor : sl)
            {
                if (sensor.isCompatible(gtuType, direction) && sensor.getLongitudinalPosition().ge(minimumPosition)
                        && sensor.getLongitudinalPosition().le(maximumPosition))
                {
                    sensorList.add(sensor);
                }
            }
        }
        return sensorList;
    }

    /**
     * Retrieve the list of Sensors of this Lane that are triggered by the given GTUType. The resulting list is a defensive
     * copy.
     * @param gtuType GTUType; the GTU type to provide the sensors for
     * @param direction GTUDirectionality; direction of movement of the GTU
     * @return List&lt;Sensor&gt;; list of the sensors, in ascending order for the location on the Lane
     */
    public final List<SingleSensor> getSensors(final GTUType gtuType, final GTUDirectionality direction)
    {
        List<SingleSensor> sensorList = new ArrayList<>(1);
        for (List<SingleSensor> sl : this.sensors.values())
        {
            for (SingleSensor sensor : sl)
            {
                if (sensor.isCompatible(gtuType, direction))
                {
                    sensorList.add(sensor);
                }
            }
        }
        return sensorList;
    }

    /**
     * Retrieve the list of all Sensors of this Lane. The resulting list is a defensive copy.
     * @return List&lt;Sensor&gt;; list of the sensors, in ascending order for the location on the Lane
     */
    public final List<SingleSensor> getSensors()
    {
        if (this.sensors == null)
        {
            return new ArrayList<>();
        }
        List<SingleSensor> sensorList = new ArrayList<>(1);
        for (List<SingleSensor> sl : this.sensors.values())
        {
            for (SingleSensor sensor : sl)
            {
                sensorList.add(sensor);
            }
        }
        return sensorList;
    }

    /**
     * Retrieve the list of Sensors of this Lane for the given GTUType. The resulting Map is a defensive copy.
     * @param gtuType GTUType; the GTU type to provide the sensors for
     * @param direction GTUDirectionality; direction of movement of the GTU
     * @return SortedMap&lt;Double, List&lt;Sensor&gt;&gt;; all sensors on this lane for the given GTUType as a map per distance
     */
    public final SortedMap<Double, List<SingleSensor>> getSensorMap(final GTUType gtuType, final GTUDirectionality direction)
    {
        SortedMap<Double, List<SingleSensor>> sensorMap = new TreeMap<>();
        for (double d : this.sensors.keySet())
        {
            List<SingleSensor> sensorList = new ArrayList<>(1);
            for (List<SingleSensor> sl : this.sensors.values())
            {
                for (SingleSensor sensor : sl)
                {
                    if (sensor.getLongitudinalPosition().si == d && sensor.isCompatible(gtuType, direction))
                    {
                        sensorList.add(sensor);
                    }
                }
            }
            if (sensorList.size() > 0)
            {
                sensorMap.put(d, sensorList);
            }
        }
        // System.out.println("getSensorMap returns");
        // for (Double key : sensorMap.keySet())
        // {
        // System.out.println("\t" + key + " -> " + (sensorMap.get(key).size()) + " sensors");
        // for (Sensor s : sensorMap.get(key))
        // {
        // System.out.println("\t\t" + s);
        // }
        // }
        return sensorMap;
    }

    /**
     * Schedule triggering of the sensors for a certain time step; from now until the nextEvaluationTime of the GTU.
     * @param gtu LaneBasedGTU; the lane based GTU for which to schedule triggering of the sensors.
     * @param referenceStartSI double; the SI distance of the GTU reference point on the lane at the current time
     * @param referenceMoveSI double; the SI distance traveled in the next time step.
     * @throws NetworkException when GTU not on this lane.
     * @throws SimRuntimeException when method cannot be scheduled.
     */
    public final void scheduleSensorTriggers(final LaneBasedGTU gtu, final double referenceStartSI,
            final double referenceMoveSI) throws NetworkException, SimRuntimeException
    {
        GTUDirectionality drivingDirection = referenceMoveSI >= 0 ? GTUDirectionality.DIR_PLUS : GTUDirectionality.DIR_MINUS;
        // System.out.println("Getting sensor map for " + gtu.getGTUType() + " driving in direction " + drivingDirection + ":");
        for (List<SingleSensor> sensorList : getSensorMap(gtu.getGTUType(), drivingDirection).values())
        {
            // System.out.println("Time=" + gtu.getSimulator().getSimulatorTime().toString() + " SensorList contains "
            // + (sensorList.size()) + " sensors:");
            // for (Sensor sensor : sensorList)
            // {
            // System.out.println("\t" + sensor);
            // }
            for (SingleSensor sensor : sensorList)
            {
                for (RelativePosition relativePosition : gtu.getRelativePositions().values())
                {
                    // System.out.println("GTU relative position " + relativePosition + " sensor relative position "
                    // + sensor.getPositionType());
                    if (sensor.getPositionType().equals(relativePosition.getType())
                            && referenceStartSI + relativePosition.getDx().getSI() <= sensor.getLongitudinalPosition().si
                            && referenceStartSI + referenceMoveSI + relativePosition.getDx().getSI() > sensor
                                    .getLongitudinalPosition().si)
                    {
                        // the exact time of triggering is based on the distance between the current position of the
                        // relative position on the GTU and the location of the sensor.
                        // TODO make sure triggering is done right when driving in DIR_MINUS direction
                        double d = sensor.getLongitudinalPosition().si - referenceStartSI - relativePosition.getDx().getSI();
                        if (d < 0)
                        {
                            throw new NetworkException("scheduleTriggers for gtu: " + gtu + ", d<0 d=" + d);
                        }

                        OperationalPlan oPlan = gtu.getOperationalPlan();
                        Time triggerTime = oPlan.timeAtDistance(new Length(d, LengthUnit.METER));
                        if (triggerTime.gt(oPlan.getEndTime()))
                        {
                            System.err.println("Time=" + gtu.getSimulator().getSimulatorTime().getTime().getSI()
                                    + " - Scheduling trigger at " + triggerTime.getSI() + "s. > "
                                    + oPlan.getEndTime().getSI() + "s. (nextEvalTime) for sensor " + sensor + " , gtu "
                                    + gtu);
                            System.err.println("  v=" + gtu.getSpeed() + ", a=" + gtu.getAcceleration() + ", lane="
                                    + toString() + ", refStartSI=" + referenceStartSI + ", moveSI=" + referenceMoveSI);
                            triggerTime =
                                    new Time(oPlan.getEndTime().getSI() - Math.ulp(oPlan.getEndTime().getSI()),
                                            TimeUnit.BASE);
                            // gtu.timeAtDistance(new Length(-d, METER));
                            // System.exit(-1);
                        }
                        // System.out.println("Scheduling a trigger for relativePosition " + relativePosition);
                        // System.out.println("Time=" + gtu.getSimulator().getSimulatorTime().toString()
                        // + " - Scheduling trigger at " + triggerTime + " for sensor " + sensor + " , gtu " + gtu);
                        SimEvent<OTSSimTimeDouble> event =
                                new SimEvent<OTSSimTimeDouble>(new OTSSimTimeDouble(triggerTime), this, sensor, "trigger",
                                        new Object[] { gtu });
                        gtu.getSimulator().scheduleEvent(event);
                        gtu.addTrigger(this, event);
                    }
                }
            }
        }
    }

    /**
     * Insert a laneBasedObject at the right place in the laneBasedObject list of this Lane. Register it in the network WITH the
     * Lane id.
     * @param laneBasedObject LaneBasedObject; the laneBasedObject to add
     * @throws NetworkException when the position of the laneBasedObject is beyond (or before) the range of this Lane
     */
    public final void addLaneBasedObject(final LaneBasedObject laneBasedObject) throws NetworkException
    {
        double position = laneBasedObject.getLongitudinalPosition().si;
        if (position < 0 || position > getLength().getSI())
        {
            throw new NetworkException("Illegal position for laneBasedObject " + position + " valid range is 0.."
                    + getLength().getSI());
        }
        if (this.parentLink.getNetwork().containsObject(laneBasedObject.getFullId()))
        {
            throw new NetworkException("Network already contains an object with the name " + laneBasedObject.getFullId());
        }
        List<LaneBasedObject> laneBasedObjectList = this.laneBasedObjects.get(position);
        if (null == laneBasedObjectList)
        {
            laneBasedObjectList = new ArrayList<LaneBasedObject>(1);
            this.laneBasedObjects.put(position, laneBasedObjectList);
        }
        laneBasedObjectList.add(laneBasedObject);
        this.parentLink.getNetwork().addObject(laneBasedObject);
        fireEvent(Lane.OBJECT_ADD_EVENT, new Object[] { laneBasedObject });
    }

    /**
     * Remove a laneBasedObject from the laneBasedObject list of this Lane.
     * @param laneBasedObject LaneBasedObject; the laneBasedObject to remove.
     * @throws NetworkException when the laneBasedObject was not found on this Lane
     */
    public final void removeLaneBasedObject(final LaneBasedObject laneBasedObject) throws NetworkException
    {
        fireEvent(Lane.OBJECT_REMOVE_EVENT, new Object[] { laneBasedObject });
        List<LaneBasedObject> laneBasedObjectList =
                this.laneBasedObjects.get(laneBasedObject.getLongitudinalPosition().getSI());
        if (null == laneBasedObjectList)
        {
            throw new NetworkException("No laneBasedObject at " + laneBasedObject.getLongitudinalPosition().si);
        }
        laneBasedObjectList.remove(laneBasedObject);
        if (laneBasedObjectList.isEmpty())
        {
            this.laneBasedObjects.remove(laneBasedObject.getLongitudinalPosition().doubleValue());
        }
        this.parentLink.getNetwork().removeObject(laneBasedObject);
    }

    /**
     * Retrieve the list of LaneBasedObjects of this Lane in the specified distance range. The resulting list is a defensive
     * copy.
     * @param minimumPosition Length; the minimum distance on the Lane (inclusive)
     * @param maximumPosition Length; the maximum distance on the Lane (inclusive)
     * @return List&lt;LaneBasedObject&gt;; list of the laneBasedObject in the specified range. This is a defensive copy.
     */
    public final List<LaneBasedObject> getLaneBasedObjects(final Length minimumPosition, final Length maximumPosition)
    {
        List<LaneBasedObject> laneBasedObjectList = new ArrayList<>(1);
        for (List<LaneBasedObject> lbol : this.laneBasedObjects.values())
        {
            for (LaneBasedObject lbo : lbol)
            {
                if (lbo.getLongitudinalPosition().ge(minimumPosition) && lbo.getLongitudinalPosition().le(maximumPosition))
                {
                    laneBasedObjectList.add(lbo);
                }
            }
        }
        return laneBasedObjectList;
    }

    /**
     * Retrieve the list of all LaneBasedObjects of this Lane. The resulting list is a defensive copy.
     * @return List&lt;LaneBasedObject&gt;; list of the laneBasedObjects, in ascending order for the location on the Lane
     */
    public final List<LaneBasedObject> getLaneBasedObjects()
    {
        if (this.laneBasedObjects == null)
        {
            return new ArrayList<>();
        }
        List<LaneBasedObject> laneBasedObjectList = new ArrayList<>(1);
        for (List<LaneBasedObject> lbol : this.laneBasedObjects.values())
        {
            for (LaneBasedObject lbo : lbol)
            {
                laneBasedObjectList.add(lbo);
            }
        }
        return laneBasedObjectList;
    }

    /**
     * Retrieve the list of LaneBasedObjects of this Lane. The resulting Map is a defensive copy.
     * @return SortedMap&lt;Double, List&lt;LaneBasedObject&gt;&gt;; all laneBasedObjects on this lane
     */
    public final SortedMap<Double, List<LaneBasedObject>> getLaneBasedObjectMap()
    {
        SortedMap<Double, List<LaneBasedObject>> laneBasedObjectMap = new TreeMap<>();
        for (double d : this.laneBasedObjects.keySet())
        {
            List<LaneBasedObject> laneBasedObjectList = new ArrayList<>(1);
            for (LaneBasedObject lbo : this.laneBasedObjects.get(d))
            {
                laneBasedObjectList.add(lbo);
            }
            laneBasedObjectMap.put(d, laneBasedObjectList);
        }
        return laneBasedObjectMap;
    }

    /**
     * Transform a fraction on the lane to a relative length (can be less than zero or larger than the lane length).
     * @param fraction double; fraction relative to the lane length.
     * @return Length; the longitudinal length corresponding to the fraction.
     */
    public final Length position(final double fraction)
    {
        if (this.length.getUnit().isBaseSIUnit())
        {
            return new Length(this.length.si * fraction, LengthUnit.SI);
        }
        return new Length(this.length.getInUnit() * fraction, this.length.getUnit());
    }

    /**
     * Transform a fraction on the lane to a relative length in SI units (can be less than zero or larger than the lane length).
     * @param fraction double; fraction relative to the lane length.
     * @return double; length corresponding to the fraction, in SI units.
     */
    public final double positionSI(final double fraction)
    {
        return this.length.si * fraction;
    }

    /**
     * Transform a position on the lane (can be less than zero or larger than the lane length) to a fraction.
     * @param position Length; relative length on the lane (may be less than zero or larger than the lane length).
     * @return fraction double; fraction relative to the lane length.
     */
    public final double fraction(final Length position)
    {
        return position.si / this.length.si;
    }

    /**
     * Transform a position on the lane in SI units (can be less than zero or larger than the lane length) to a fraction.
     * @param positionSI double; relative length on the lane in SI units (may be less than zero or larger than the lane length).
     * @return double; fraction relative to the lane length.
     */
    public final double fractionSI(final double positionSI)
    {
        return positionSI / this.length.si;
    }

    /**
     * Add a LaneBasedGTU to the list of this Lane.
     * @param gtu LaneBasedGTU; the GTU to add
     * @param fractionalPosition double; the fractional position that the newly added GTU will have on this Lane
     * @return int; the rank that the newly added GTU has on this Lane (should be 0, except when the GTU enters this Lane due to
     *         a lane change operation)
     * @throws GTUException when the fractionalPosition is outside the range 0..1, or the GTU is already registered on this Lane
     */
    public final int addGTU(final LaneBasedGTU gtu, final double fractionalPosition) throws GTUException
    {
        int index;
        // check if we are the first
        if (this.gtuList.size() == 0)
        {
            this.gtuList.add(gtu);
            index = 0;
        }
        else
        {
            // check if we can add at the end
            LaneBasedGTU lastGTU = this.gtuList.get(this.gtuList.size() - 1);
            if (fractionalPosition > lastGTU.fractionalPosition(this, lastGTU.getFront()))
            {
                this.gtuList.add(gtu);
                index = this.gtuList.size() - 1;
            }
            else
            {
                // figure out the rank for the new GTU
                for (index = 0; index < this.gtuList.size(); index++)
                {
                    LaneBasedGTU otherGTU = this.gtuList.get(index);
                    if (gtu == otherGTU)
                    {
                        throw new GTUException(gtu + " already registered on Lane " + this + " [registered lanes: "
                                + gtu.positions(gtu.getFront()).keySet() + "] locations: "
                                + gtu.positions(gtu.getFront()).values() + " time: "
                                + gtu.getSimulator().getSimulatorTime().getTime());
                    }
                    if (otherGTU.fractionalPosition(this, otherGTU.getFront()) >= fractionalPosition)
                    {
                        break;
                    }
                }
                this.gtuList.add(index, gtu);
            }
        }
        fireTimedEvent(Lane.GTU_ADD_EVENT, new Object[] { gtu.getId(), gtu, this.gtuList.size() }, gtu.getSimulator()
                .getSimulatorTime());
        getParentLink().addGTU(gtu);
        return index;
    }

    /**
     * Add a LaneBasedGTU to the list of this Lane.
     * @param gtu LaneBasedGTU; the GTU to add
     * @param longitudinalPosition Length; the longitudinal position that the newly added GTU will have on this Lane
     * @return int; the rank that the newly added GTU has on this Lane (should be 0, except when the GTU enters this Lane due to
     *         a lane change operation)
     * @throws GTUException when longitudinalPosition is negative or exceeds the length of this Lane
     */
    public final int addGTU(final LaneBasedGTU gtu, final Length longitudinalPosition) throws GTUException
    {
        return addGTU(gtu, longitudinalPosition.getSI() / getLength().getSI());
    }

    /**
     * Remove a GTU from the GTU list of this lane.
     * @param gtu the GTU to remove.
     * @param removeFromParentLink when the GTU leaves the last lane of the parentLink of this Lane
     * @param position Length; last position of the GTU
     */
    public final void removeGTU(final LaneBasedGTU gtu, final boolean removeFromParentLink, final Length position)
    {
        this.gtuList.remove(gtu);
        fireTimedEvent(Lane.GTU_REMOVE_EVENT, new Object[] { gtu.getId(), gtu, this.gtuList.size(), position }, gtu
                .getSimulator().getSimulatorTime());
        if (removeFromParentLink)
        {
            this.parentLink.removeGTU(gtu);
        }
    }

    /**
     * Get the last GTU on the lane, relative to a driving direction on this lane.
     * @param direction GTUDirectionality; whether we are looking in the the design line direction or against the center line
     *            direction.
     * @return LaneBasedGTU; the last GTU on this lane in the given direction, or null if no GTU could be found.
     * @throws GTUException when there is a problem with the position of the GTUs on the lane.
     */
    public final LaneBasedGTU getLastGtu(final GTUDirectionality direction) throws GTUException
    {
        if (this.gtuList.size() == 0)
        {
            return null;
        }
        if (direction.equals(GTUDirectionality.DIR_PLUS))
        {
            return this.gtuList.get(this.gtuList.size() - 1);
        }
        else
        {
            return this.gtuList.get(0);
        }
    }

    /**
     * Get the first GTU on the lane, relative to a driving direction on this lane.
     * @param direction GTUDirectionality; whether we are looking in the the design line direction or against the center line
     *            direction.
     * @return LaneBasedGTU; the first GTU on this lane in the given direction, or null if no GTU could be found.
     * @throws GTUException when there is a problem with the position of the GTUs on the lane.
     */
    public final LaneBasedGTU getFirstGtu(final GTUDirectionality direction) throws GTUException
    {
        if (this.gtuList.size() == 0)
        {
            return null;
        }
        if (direction.equals(GTUDirectionality.DIR_PLUS))
        {
            return this.gtuList.get(0);
        }
        else
        {
            return this.gtuList.get(this.gtuList.size() - 1);
        }
    }

    /**
     * Get the first GTU where the relativePosition is in front of another GTU on the lane, in a driving direction on this lane,
     * compared to the DESIGN LINE.
     * @param position Length; the position before which the relative position of a GTU will be searched.
     * @param direction GTUDirectionality; whether we are looking in the the center line direction or against the center line
     *            direction.
     * @param relativePosition RelativePosition.TYPE; the relative position we want to compare against
     * @param when Time; the time for which to evaluate the positions.
     * @return LaneBasedGTU; the first GTU before a position on this lane in the given direction, or null if no GTU could be
     *         found.
     * @throws GTUException when there is a problem with the position of the GTUs on the lane.
     */
    public final LaneBasedGTU getGtuAhead(final Length position, final GTUDirectionality direction,
            final RelativePosition.TYPE relativePosition, final Time when) throws GTUException
    {
        if (direction.equals(GTUDirectionality.DIR_PLUS))
        {
            for (LaneBasedGTU gtu : this.gtuList)
            {
                Length gtuPos = gtu.position(this, gtu.getRelativePositions().get(relativePosition), when);
                if (gtuPos.gt(position))
                {
                    return gtu;
                }
            }
        }
        else
        {
            for (int i = this.gtuList.size() - 1; i >= 0; i--)
            {
                LaneBasedGTU gtu = this.gtuList.get(i);
                if (gtu.position(this, gtu.getRelativePositions().get(relativePosition), when).lt(position))
                {
                    return gtu;
                }
            }
        }
        return null;
    }

    /**
     * Get the first object where the relativePosition is in front of a certain position on the lane, in a driving direction on
     * this lane, compared to the DESIGN LINE. Perception should iterate over results from this method to see what is most
     * limiting.
     * @param position Length; the position after which the relative position of an object will be searched.
     * @param direction GTUDirectionality; whether we are looking in the the center line direction or against the center line
     *            direction.
     * @return List&lt;LaneBasedObject&gt;; the first object(s) before a position on this lane in the given direction, or null
     *         if no object could be found.
     */
    public final List<LaneBasedObject> getObjectAhead(final Length position, final GTUDirectionality direction)
    {
        if (direction.equals(GTUDirectionality.DIR_PLUS))
        {
            for (double distance : this.laneBasedObjects.keySet())
            {
                if (distance > position.si)
                {
                    return new ArrayList<>(this.laneBasedObjects.get(distance));
                }
            }
        }
        else
        {
            NavigableMap<Double, List<LaneBasedObject>> reverseLBO =
                    (NavigableMap<Double, List<LaneBasedObject>>) this.laneBasedObjects;
            for (double distance : reverseLBO.descendingKeySet())
            {
                if (distance < position.si)
                {
                    return new ArrayList<>(this.laneBasedObjects.get(distance));
                }
            }
        }
        return null;
    }

    /**
     * Get the first object where the relativePosition is behind of a certain position on the lane, in a driving direction on
     * this lane, compared to the DESIGN LINE. Perception should iterate over results from this method to see what is most
     * limiting.
     * @param position Length; the position after which the relative position of an object will be searched.
     * @param direction GTUDirectionality; whether we are looking in the the center line direction or against the center line
     *            direction.
     * @return List&lt;LaneBasedObject&gt;; the first object(s) after a position on this lane in the given direction, or null if
     *         no object could be found.
     */
    public final List<LaneBasedObject> getObjectBehind(final Length position, final GTUDirectionality direction)
    {
        if (direction.equals(GTUDirectionality.DIR_PLUS))
        {
            return getObjectAhead(position, GTUDirectionality.DIR_MINUS);
        }
        return getObjectAhead(position, GTUDirectionality.DIR_PLUS);
    }

    /**
     * Get the first GTU where the relativePosition is behind a certain position on the lane, in a driving direction on this
     * lane, compared to the DESIGN LINE.
     * @param position Length; the position before which the relative position of a GTU will be searched.
     * @param direction GTUDirectionality; whether we are looking in the the center line direction or against the center line
     *            direction.
     * @param relativePosition RelativePosition.TYPE; the relative position of the GTU we are looking for.
     * @param when Time; the time for which to evaluate the positions.
     * @return LaneBasedGTU; the first GTU after a position on this lane in the given direction, or null if no GTU could be
     *         found.
     * @throws GTUException when there is a problem with the position of the GTUs on the lane.
     */
    public final LaneBasedGTU getGtuBehind(final Length position, final GTUDirectionality direction,
            final RelativePosition.TYPE relativePosition, final Time when) throws GTUException
    {
        if (direction.equals(GTUDirectionality.DIR_PLUS))
        {
            return getGtuAhead(position, GTUDirectionality.DIR_MINUS, relativePosition, when);
        }
        return getGtuAhead(position, GTUDirectionality.DIR_PLUS, relativePosition, when);
    }

    /*
     * TODO only center position? Or also width? What is a good cutoff? Base on average width of the GTU type that can drive on
     * this Lane? E.g., for a Tram or Train, a 5 cm deviation is a problem; for a Car or a Bicycle, more deviation is
     * acceptable.
     */
    /** Lateral alignment margin for longitudinally connected Lanes. */
    public static final Length MARGIN = new Length(0.5, LengthUnit.METER);

    /**
     * NextLanes returns the successor lane(s) in the design line direction, if any exist.<br>
     * The next lane(s) are cached, as it is too expensive to make the calculation every time. There are several possibilities:
     * (1) Returning an empty set when there is no successor lane in the design direction or there is no longitudinal transfer
     * possible to a successor lane in the design direction. (2) Returning a set with just one lane if the lateral position of
     * the successor lane matches the lateral position of this lane (based on an overlap of the lateral positions of the two
     * joining lanes of more than a certain percentage). (3) Multiple lanes in case the Node where the underlying Link for this
     * Lane has multiple "outgoing" Links, and there are multiple lanes that match the lateral position of this lane.<br>
     * The next lanes can differ per GTU type. For instance, a lane where cars and buses are allowed can have a next lane where
     * only buses are allowed, forcing the cars to leave that lane.
     * @param gtuType the GTU type for which we return the next lanes.
     * @return set of Lanes following this lane for the given GTU type.
     */
    public final Map<Lane, GTUDirectionality> nextLanes(final GTUType gtuType)
    {
        if (this.nextLanes == null)
        {
            this.nextLanes = new LinkedHashMap<>(1);
        }
        if (!this.nextLanes.containsKey(gtuType))
        {
            // TODO determine if this should synchronize on this.nextLanes
            Map<Lane, GTUDirectionality> laneMap = new LinkedHashMap<>(1);
            this.nextLanes.put(gtuType, laneMap);
            // Construct (and cache) the result.
            for (Link link : getParentLink().getEndNode().getLinks())
            {
                if (!(link.equals(this.getParentLink())) && link instanceof CrossSectionLink)
                {
                    for (CrossSectionElement cse : ((CrossSectionLink) link).getCrossSectionElementList())
                    {
                        if (cse instanceof Lane)
                        {
                            Lane lane = (Lane) cse;
                            Length jumpToStart = this.getCenterLine().getLast().distance(lane.getCenterLine().getFirst());
                            Length jumpToEnd = this.getCenterLine().getLast().distance(lane.getCenterLine().getLast());
                            // this, parentLink ---> O ---> lane, link
                            if (jumpToStart.lt(MARGIN) && jumpToStart.lt(jumpToEnd)
                                    && link.getStartNode().equals(getParentLink().getEndNode()))
                            {
                                // Would the GTU move in the design line direction or against it?
                                // TODO And is it aligned with its next lane?
                                if (lane.getLaneType().isCompatible(gtuType, GTUDirectionality.DIR_PLUS))
                                {
                                    laneMap.put(lane, GTUDirectionality.DIR_PLUS);
                                }
                                else if (lane.getLaneType().isCompatible(gtuType, GTUDirectionality.DIR_MINUS))// getDirectionality(gtuType).isBackwardOrBoth())
                                {
                                    laneMap.put(lane, GTUDirectionality.DIR_MINUS);
                                }
                            }
                            // this, parentLink ---> O <--- lane, link
                            else if (jumpToEnd.lt(MARGIN) && jumpToEnd.lt(jumpToStart)
                                    && link.getEndNode().equals(getParentLink().getEndNode()))
                            {
                                // Would the GTU move in the design line direction or against it?
                                // TODO And is it aligned with its next lane?
                                if (lane.getLaneType().isCompatible(gtuType, GTUDirectionality.DIR_PLUS))// getDirectionality(gtuType).isForwardOrBoth())
                                {
                                    laneMap.put(lane, GTUDirectionality.DIR_PLUS);
                                }
                                else if (lane.getLaneType().isCompatible(gtuType, GTUDirectionality.DIR_MINUS))// getDirectionality(gtuType).isBackwardOrBoth())
                                {
                                    laneMap.put(lane, GTUDirectionality.DIR_MINUS);
                                }
                            }
                            // else: not a "connected" lane
                        }
                    }
                }
            }
        }
        return this.nextLanes.get(gtuType);
    }

    /**
     * PrevLanes returns the predecessor lane(s) relative to the design line direction, if any exist.<br>
     * The previous lane(s) are cached, as it is too expensive to make the calculation every time. There are several
     * possibilities: (1) Returning an empty set when there is no predecessor lane relative to the design direction or there is
     * no longitudinal transfer possible to a predecessor lane relative to the design direction. (2) Returning a set with just
     * one lane if the lateral position of the predecessor lane matches the lateral position of this lane (based on an overlap
     * of the lateral positions of the two joining lanes of more than a certain percentage). (3) Multiple lanes in case the Node
     * where the underlying Link for this Lane has multiple "incoming" Links, and there are multiple lanes that match the
     * lateral position of this lane.<br>
     * The previous lanes can differ per GTU type. For instance, a lane where cars and buses are allowed can be preceded by a
     * lane where only buses are allowed.
     * @param gtuType the GTU type for which we return the next lanes.
     * @return set of Lanes following this lane for the given GTU type.
     */
    public final Map<Lane, GTUDirectionality> prevLanes(final GTUType gtuType)
    {
        if (this.prevLanes == null)
        {
            this.prevLanes = new LinkedHashMap<>(1);
        }
        if (!this.prevLanes.containsKey(gtuType))
        {
            Map<Lane, GTUDirectionality> laneMap = new LinkedHashMap<>(1);
            this.prevLanes.put(gtuType, laneMap);
            // Construct (and cache) the result.
            for (Link link : getParentLink().getStartNode().getLinks())
            {
                if (!(link.equals(this.getParentLink())) && link instanceof CrossSectionLink)
                {
                    for (CrossSectionElement cse : ((CrossSectionLink) link).getCrossSectionElementList())
                    {
                        if (cse instanceof Lane)
                        {
                            Lane lane = (Lane) cse;
                            Length jumpToStart = this.getCenterLine().getFirst().distance(lane.getCenterLine().getFirst());
                            Length jumpToEnd = this.getCenterLine().getFirst().distance(lane.getCenterLine().getLast());
                            // this, parentLink <--- O ---> lane, link
                            if (jumpToStart.lt(MARGIN) && jumpToStart.lt(jumpToEnd)
                                    && link.getStartNode().equals(getParentLink().getStartNode()))
                            {
                                // does the GTU move in the design line direction or against it?
                                // TODO And is it aligned with its next lane?
                                if (lane.getLaneType().isCompatible(gtuType, GTUDirectionality.DIR_PLUS))// getDirectionality(gtuType).isForwardOrBoth())
                                {
                                    laneMap.put(lane, GTUDirectionality.DIR_PLUS);
                                }
                                else if (lane.getLaneType().isCompatible(gtuType, GTUDirectionality.DIR_MINUS))// getDirectionality(gtuType).isBackwardOrBoth())
                                {
                                    laneMap.put(lane, GTUDirectionality.DIR_MINUS);
                                }
                            }
                            // this, parentLink <--- O <--- lane, link
                            else if (jumpToEnd.lt(MARGIN) && jumpToEnd.lt(jumpToStart)
                                    && link.getEndNode().equals(getParentLink().getStartNode()))
                            {
                                // does the GTU move in the design line direction or against it?
                                // TODO And is it aligned with its next lane?
                                if (lane.getLaneType().isCompatible(gtuType, GTUDirectionality.DIR_PLUS))// getDirectionality(gtuType).isForwardOrBoth())
                                {
                                    laneMap.put(lane, GTUDirectionality.DIR_PLUS);
                                }
                                else if (lane.getLaneType().isCompatible(gtuType, GTUDirectionality.DIR_MINUS))// getDirectionality(gtuType).isBackwardOrBoth())
                                {
                                    laneMap.put(lane, GTUDirectionality.DIR_MINUS);
                                }
                            }
                            // else: not a "connected" lane
                        }
                    }
                }
            }
        }
        return this.prevLanes.get(gtuType);
    }

    /**
     * Returns the lanes that could be followed in a given direction and for the given GTU type.
     * @param direction GTUDirectionality; gtu direction
     * @param gtuType GTUType; gtu type
     * @return lanes that can be followed in a given direction and for the given GTU type
     */
    public final Map<Lane, GTUDirectionality> downstreamLanes(final GTUDirectionality direction, final GTUType gtuType)
    {
        if (this.downLanes == null)
        {
            this.downLanes = new LinkedHashMap<>(1);
        }
        if (!this.downLanes.containsKey(gtuType))
        {
            this.downLanes.put(gtuType, new HashMap<>());
        }
        if (!this.downLanes.get(gtuType).containsKey(direction))
        {
            Map<Lane, GTUDirectionality> down = direction.isPlus() ? nextLanes(gtuType) : prevLanes(gtuType);
            down = new LinkedHashMap<>(down); // safe copy
            Node downNode = direction.isPlus() ? getParentLink().getEndNode() : getParentLink().getStartNode();
            Iterator<Entry<Lane, GTUDirectionality>> iterator = down.entrySet().iterator();
            while (iterator.hasNext())
            {
                Entry<Lane, GTUDirectionality> entry = iterator.next();
                if ((entry.getValue().isPlus() && !entry.getKey().getParentLink().getStartNode().equals(downNode))
                        || (entry.getValue().isMinus() && !entry.getKey().getParentLink().getEndNode().equals(downNode)))
                {
                    // cannot move onto this lane
                    iterator.remove();
                }
            }
            this.downLanes.get(gtuType).put(direction, down);
        }
        return this.downLanes.get(gtuType).get(direction);
    }

    /**
     * Returns the lanes that could precede in a given direction and for the given GTU type.
     * @param direction GTUDirectionality; gtu direction
     * @param gtuType GTUType; gtu type
     * @return lanes that can be followed in a given direction and for the given GTU type
     */
    public final Map<Lane, GTUDirectionality> upstreamLanes(final GTUDirectionality direction, final GTUType gtuType)
    {
        if (this.upLanes == null)
        {
            this.upLanes = new LinkedHashMap<>(1);
        }
        if (!this.upLanes.containsKey(gtuType))
        {
            this.upLanes.put(gtuType, new HashMap<>());
        }
        if (!this.upLanes.get(gtuType).containsKey(direction))
        {
            Map<Lane, GTUDirectionality> up = direction.isPlus() ? prevLanes(gtuType) : nextLanes(gtuType);
            up = new LinkedHashMap<>(up); // safe copy
            Node upNode = direction.isPlus() ? getParentLink().getStartNode() : getParentLink().getEndNode();
            Iterator<Entry<Lane, GTUDirectionality>> iterator = up.entrySet().iterator();
            while (iterator.hasNext())
            {
                Entry<Lane, GTUDirectionality> entry = iterator.next();
                if ((entry.getValue().isPlus() && !entry.getKey().getParentLink().getEndNode().equals(upNode))
                        || (entry.getValue().isMinus() && !entry.getKey().getParentLink().getStartNode().equals(upNode)))
                {
                    // cannot have come from this lane
                    iterator.remove();
                }
            }
            this.upLanes.get(gtuType).put(direction, up);
        }
        return this.upLanes.get(gtuType).get(direction);
    }

    /**
     * Determine the set of lanes to the left or to the right of this lane, which are accessible from this lane, or an empty set
     * if no lane could be found. The method takes the LongitidinalDirectionality of the lane into account. In other words, if
     * we drive in the DIR_PLUS direction and look for a lane on the LEFT, and there is a lane but the Directionality of that
     * lane is not DIR_PLUS or DIR_BOTH, it will not be included.<br>
     * A lane is called adjacent to another lane if the lateral edges are not more than a delta distance apart. This means that
     * a lane that <i>overlaps</i> with another lane is <b>not</b> returned as an adjacent lane. <br>
     * <b>Note:</b> LEFT and RIGHT are seen from the direction of the GTU, in its forward driving direction. <br>
     * @param lateralDirection LEFT or RIGHT.
     * @param gtuType the type of GTU for which to return the adjacent lanes.
     * @param drivingDirection GTUDirectinality; the driving direction of the GTU on <code>this</code> Lane
     * @return the set of lanes that are accessible, or null if there is no lane that is accessible with a matching driving
     *         direction.
     */
    public final Set<Lane> accessibleAdjacentLanes(final LateralDirectionality lateralDirection, final GTUType gtuType,
            final GTUDirectionality drivingDirection)
    {
        Set<Lane> candidates = new LinkedHashSet<>(1);
        LateralDirectionality dir =
                drivingDirection.equals(GTUDirectionality.DIR_PLUS) ? lateralDirection : lateralDirection.isLeft()
                        ? LateralDirectionality.RIGHT : LateralDirectionality.LEFT;
        // this.getDirectionality(gtuType).isForwardOrBoth() ? lateralDirection : lateralDirection.isLeft()
        // ? LateralDirectionality.RIGHT : LateralDirectionality.LEFT;
        for (Lane lane : neighbors(dir, gtuType, drivingDirection))
        {
            if (lane.getLaneType().isCompatible(gtuType, drivingDirection))
            // getDirectionality(gtuType).equals(LongitudinalDirectionality.DIR_BOTH)
            // || lane.getDirectionality(gtuType).equals(this.getDirectionality(gtuType)))
            {
                candidates.add(lane);
            }
        }
        return candidates;
    }

    /**
     * Get the speed limit of this lane, which can differ per GTU type. E.g., cars might be allowed to drive 120 km/h and trucks
     * 90 km/h.
     * @param gtuType the GTU type to provide the speed limit for
     * @return the speedLimit.
     * @throws NetworkException on network inconsistency
     */
    public final Speed getSpeedLimit(final GTUType gtuType) throws NetworkException
    {
        if (this.speedLimitMap.containsKey(gtuType))
        {
            return this.speedLimitMap.get(gtuType);
        }
        if (gtuType.getParent() != null)
        {
            return getSpeedLimit(gtuType.getParent());
        }
        throw new NetworkException("No speed limit set for GTUType " + gtuType + " on lane " + toString());
    }

    /**
     * Get the highest speed limit of this lane.
     * @return the highest speedLimit.
     * @throws NetworkException on network inconsistency
     */
    public final Speed getHighestSpeedLimit() throws NetworkException
    {
        Throw.when(this.speedLimitMap.isEmpty(), NetworkException.class, "Lane %s has no speed limits set.", toString());
        Speed out = Speed.ZERO;
        for (GTUType gtuType : this.speedLimitMap.keySet())
        {
            out = Speed.max(out, this.speedLimitMap.get(gtuType));
        }
        return out;
    }

    /**
     * Set the speed limit of this lane, which can differ per GTU type. Cars might be allowed to drive 120 km/h and trucks 90
     * km/h. If the speed limit is the same for all GTU types, GTUType.ALL will be used. This means that the settings can be
     * used additive, or subtractive. <br>
     * In <b>additive use</b>, do not set the speed limit for GTUType.ALL. Now, one by one, the allowed maximum speeds for each
     * of the GTU Types have be added. Do this when there are few GTU types or the speed limits per TU type are very different. <br>
     * In <b>subtractive use</b>, set the speed limit for GTUType.ALL to the most common one. Override the speed limit for
     * certain GTUTypes to a different value. An example is a lane on a highway where all vehicles, except truck (CAR, BUS,
     * MOTORCYCLE, etc.), can drive 120 km/h, but trucks are allowed only 90 km/h. In that case, set the speed limit for
     * GTUType.ALL to 120 km/h, and for TRUCK to 90 km/h.
     * @param gtuType the GTU type to provide the speed limit for
     * @param speedLimit the speed limit for this gtu type
     */
    public final void setSpeedLimit(final GTUType gtuType, final Speed speedLimit)
    {
        this.speedLimitMap.put(gtuType, speedLimit);
    }

    /**
     * Remove the set speed limit for a GTUType. If the speed limit for GTUType.ALL will be removed, there will not be a
     * 'default' speed limit anymore. If the speed limit for a certain GTUType is removed, its speed limit will default to the
     * speed limit of GTUType.ALL. <br>
     * <b>Note</b>: if no speed limit is known for a GTUType, getSpeedLimit will throw a NetworkException when the speed limit
     * is retrieved for that GTUType.
     * @param gtuType the GTU type to provide the speed limit for
     */
    public final void removeSpeedLimit(final GTUType gtuType)
    {
        this.speedLimitMap.remove(gtuType);
    }

    /**
     * @return laneType.
     */
    public final LaneType getLaneType()
    {
        return this.laneType;
    }

    /**
     * This method sets the directionality of the lane for a GTU type. It might be that the driving direction in the lane is
     * FORWARD (from start node of the link to end node of the link) for the GTU type CAR, but BOTH for the GTU type BICYCLE
     * (i.e., bicycles can also go in the other direction; we see this on some city streets). If the directionality for a
     * GTUType is set to NONE, this means that the given GTUType cannot use the Lane. If a Directionality is set for
     * GTUType.ALL, the getDirectionality will default to these settings when there is no specific entry for a given
     * directionality. This means that the settings can be used additive, or restrictive. <br>
     * In <b>additive use</b>, set the directionality for GTUType.ALL to NONE, or do not set the directionality for GTUType.ALL.
     * Now, one by one, the allowed directionalities can be added. An example is a lane on a highway, which we only open for
     * CAR, TRUCK and BUS. <br>
     * In <b>restrictive use</b>, set the directionality for GTUType.ALL to BOTH, FORWARD, or BACKWARD. Override the
     * directionality for certain GTUTypes to a more restrictive access, e.g. to NONE. An example is a lane that is open for all
     * road users, except TRUCK.
     * @param gtuType the GTU type to set the directionality for.
     * @param directionality the longitudinal directionality of the link (FORWARD, BACKWARD, BOTH or NONE) for the given GTU
     *            type.
     * @throws NetworkException when the lane directionality for the given GTUType is inconsistent with the Link directionality
     *             to which the lane belongs.
     */
//    public final void addDirectionality(final GTUType gtuType, final LongitudinalDirectionality directionality)
//            throws NetworkException
//    {
//        this.directionalityMap.put(gtuType, directionality);
//        checkDirectionality();
//    }

    /**
     * This method removes an earlier provided directionality of the lane for a given GTU type, e.g. for maintenance of the
     * lane. After removing, the directionality for the GTU will fall back to the provided directionality for GTUType.ALL (if
     * present). Thereby removing a directionality is different from setting the directionality to NONE.
     * @param gtuType the GTU type to remove the directionality for on this lane.
     */
//    public final void removeDirectionality(final GTUType gtuType)
//    {
//        this.directionalityMap.remove(gtuType);
//    }

    /**
     * Check whether the directionalities for the GTU types for this lane are consistent with the directionalities of the
     * overarching Link.
     * @throws NetworkException when the lane directionality for a given GTUType is inconsistent with the Link directionality to
     *             which the lane belongs.
     */
    private void checkDirectionality() throws NetworkException
    {
        // TODO check that the directionality of this Lane does not conflict with that of the parent the OTSLink 
//        for (GTUType gtuType : this.directionalityMap.keySet())
//        {
//            LongitudinalDirectionality directionality = this.directionalityMap.get(gtuType);
//            if (!getParentLink().getDirectionality(gtuType).contains(directionality))
//            {
//                throw new NetworkException("Lane " + toString() + " allows " + gtuType + " a directionality of "
//                        + directionality + " which is not present in the overarching link " + getParentLink().toString());
//            }
//        }
    }

    /**
     * @return gtuList.
     */
    public final ImmutableList<LaneBasedGTU> getGtuList()
    {
        return this.gtuList == null ? new ImmutableArrayList<>(new ArrayList<>()) : new ImmutableArrayList<>(this.gtuList,
                Immutable.WRAP);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    protected double getZ()
    {
        return 0.0;
    }

    /**
     * @return overtakingConditions
     */
    public final OvertakingConditions getOvertakingConditions()
    {
        return this.overtakingConditions;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        CrossSectionLink link = getParentLink();
        return String.format("Lane %s of %s", getId(), link.getId());
    }

    /** Cache of the hashCode. */
    private Integer cachedHashCode = null;

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public int hashCode()
    {
        if (this.cachedHashCode == null)
        {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + ((this.laneType == null) ? 0 : this.laneType.hashCode());
            this.cachedHashCode = result;
        }
        return this.cachedHashCode;
    }

    /** {@inheritDoc} */
    @SuppressWarnings({ "checkstyle:designforextension", "checkstyle:needbraces" })
    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        Lane other = (Lane) obj;
        if (this.laneType == null)
        {
            if (other.laneType != null)
                return false;
        }
        else if (!this.laneType.equals(other.laneType))
            return false;
        return true;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public Lane clone(final CrossSectionLink newParentLink, final OTSSimulatorInterface newSimulator, final boolean animation)
            throws NetworkException
    {
        Lane newLane = new Lane(newParentLink, newSimulator, animation, this);
        // nextLanes, prevLanes, nextNeighbors, rightNeighbors are filled at first request

        SortedMap<Double, List<SingleSensor>> newSensorMap = new TreeMap<>();
        for (double distance : this.sensors.keySet())
        {
            List<SingleSensor> newSensorList = new ArrayList<>();
            for (SingleSensor sensor : this.sensors.get(distance))
            {
                SingleSensor newSensor = ((AbstractSensor) sensor).clone(newLane, newSimulator, animation);
                newSensorList.add(newSensor);
            }
            newSensorMap.put(distance, newSensorList);
        }
        newLane.sensors.clear();
        newLane.sensors.putAll(newSensorMap);

        SortedMap<Double, List<LaneBasedObject>> newLaneBasedObjectMap = new TreeMap<>();
        for (double distance : this.laneBasedObjects.keySet())
        {
            List<LaneBasedObject> newLaneBasedObjectList = new ArrayList<>();
            for (LaneBasedObject lbo : this.laneBasedObjects.get(distance))
            {
                AbstractLaneBasedObject laneBasedObject = (AbstractLaneBasedObject) lbo;
                LaneBasedObject newLbo = laneBasedObject.clone(newLane, newSimulator, animation);
                newLaneBasedObjectList.add(newLbo);
            }
            newLaneBasedObjectMap.put(distance, newLaneBasedObjectList);
        }
        newLane.laneBasedObjects.clear();
        newLane.laneBasedObjects.putAll(newLaneBasedObjectMap);

        return newLane;
    }
}
