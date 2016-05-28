package org.opentrafficsim.road.network.lane;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEvent;

import org.djunits.unit.LengthUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
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
import org.opentrafficsim.graphs.LaneBasedGTUSampler;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.network.lane.changing.OvertakingConditions;

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
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
     * The direction in which vehicles can drive, i.e., in direction of geometry, reverse, or both. This can differ per GTU
     * type. In an overtake lane, cars might overtake and trucks not. It might be that the lane (e.g., a street in a city) is
     * FORWARD (from start node of the link to end node of the link) for the GTU type CAR, but BOTH for the GTU type BICYCLE
     * (i.e., bicycles can also go in the other direction, opposite to the drawing direction of the Link). If the directionality
     * for a GTUType is set to NONE, this means that the given GTUType cannot use the Lane. If a Directionality is set for
     * GTUType.ALL, the getDirectionality will default to these settings when there is no specific entry for a given
     * directionality. This means that the settings can be used additive, or restrictive. <br>
     * In <b>additive use</b>, set the directionality for GTUType.ALL to NONE, or do not set the directionality for GTUType.ALL.
     * Now, one by one, the allowed directionalities can be added. An example is a lane on a highway, which we only open for
     * CAR, TRUCK and BUS. <br>
     * In <b>restrictive use</b>, set the directionality for GTUType.ALL to BOTH, FORWARD, or BACKWARD. Override the
     * directionality for certain GTUTypes to a more restrictive access, e.g. to NONE. An example is a lane that is open for all
     * road users, except TRUCK.
     */
    private final Map<GTUType, LongitudinalDirectionality> directionalityMap;

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
    private final SortedMap<Double, List<GTUTypeSensor>> sensors = new TreeMap<>();

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

    /** List of graphs that want to sample GTUs on this Lane. */
    private ArrayList<LaneBasedGTUSampler> samplers = new ArrayList<LaneBasedGTUSampler>();

    /** The conditions for overtaking another GTU, viewed from this lane. */
    // TODO allow for direction-dependent overtaking conditions
    private final OvertakingConditions overtakingConditions;

    /**
     * @param parentLink Cross Section Link to which the element belongs.
     * @param id the id of this lane within the link; should be unique within the link.
     * @param lateralOffsetAtStart Length; the lateral offset of the design line of the new CrossSectionLink with respect to the
     *            design line of the parent Link at the start of the parent Link
     * @param lateralOffsetAtEnd Length; the lateral offset of the design line of the new CrossSectionLink with respect to the
     *            design line of the parent Link at the end of the parent Link
     * @param beginWidth Length; start width, positioned <i>symmetrically around</i> the design line
     * @param endWidth Length; end width, positioned <i>symmetrically around</i> the design line
     * @param laneType type of lane to deduce compatibility with GTU types
     * @param directionalityMap in direction of geometry, reverse, or both, specified per GTU Type
     * @param speedLimitMap speed limit on this lane, specified per GTU Type
     * @param overtakingConditions the conditions for overtaking another GTU, viewed from this lane
     * @throws OTSGeometryException when creation of the center line or contour geometry fails
     * @throws NetworkException when id equal to null or not unique
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public Lane(final CrossSectionLink parentLink, final String id, final Length lateralOffsetAtStart,
            final Length lateralOffsetAtEnd, final Length beginWidth, final Length endWidth, final LaneType laneType,
            final Map<GTUType, LongitudinalDirectionality> directionalityMap, final Map<GTUType, Speed> speedLimitMap,
            final OvertakingConditions overtakingConditions) throws OTSGeometryException, NetworkException
    {
        super(parentLink, id, lateralOffsetAtStart, lateralOffsetAtEnd, beginWidth, endWidth);
        this.laneType = laneType;
        this.directionalityMap = directionalityMap;
        checkDirectionality();
        this.speedLimitMap = speedLimitMap;
        this.overtakingConditions = overtakingConditions;
    }

    /**
     * @param parentLink Cross Section Link to which the element belongs.
     * @param id the id of this lane within the link; should be unique within the link.
     * @param lateralOffsetAtStart Length; the lateral offset of the design line of the new CrossSectionLink with respect to the
     *            design line of the parent Link at the start of the parent Link
     * @param lateralOffsetAtEnd Length; the lateral offset of the design line of the new CrossSectionLink with respect to the
     *            design line of the parent Link at the end of the parent Link
     * @param beginWidth Length; start width, positioned <i>symmetrically around</i> the design line
     * @param endWidth Length; end width, positioned <i>symmetrically around</i> the design line
     * @param laneType type of lane to deduce compatibility with GTU types
     * @param directionality in direction of geometry, reverse, or both
     * @param speedLimit speed limit on this lane
     * @param overtakingConditions the conditions for overtaking another GTU, viewed from this lane
     * @throws OTSGeometryException when creation of the center line or contour geometry fails
     * @throws NetworkException when id equal to null or not unique
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public Lane(final CrossSectionLink parentLink, final String id, final Length lateralOffsetAtStart,
            final Length lateralOffsetAtEnd, final Length beginWidth, final Length endWidth, final LaneType laneType,
            final LongitudinalDirectionality directionality, final Speed speedLimit,
            final OvertakingConditions overtakingConditions) throws OTSGeometryException, NetworkException
    {
        super(parentLink, id, lateralOffsetAtStart, lateralOffsetAtEnd, beginWidth, endWidth);
        this.laneType = laneType;
        this.directionalityMap = new LinkedHashMap<>(1);
        this.directionalityMap.put(GTUType.ALL, directionality);
        checkDirectionality();
        this.speedLimitMap = new LinkedHashMap<>();
        this.speedLimitMap.put(GTUType.ALL, speedLimit);
        this.overtakingConditions = overtakingConditions;
    }

    /**
     * @param parentLink Cross Section Link to which the element belongs.
     * @param id the id of this lane within the link; should be unique within the link.
     * @param lateralOffset Length; the lateral offset of the design line of the new CrossSectionLink with respect to the design
     *            line of the parent Link
     * @param width Length; width, positioned <i>symmetrically around</i> the design line
     * @param laneType type of lane to deduce compatibility with GTU types
     * @param directionalityMap in direction of geometry, reverse, or both, specified per GTU Type
     * @param speedLimitMap speed limit on this lane, specified per GTU Type
     * @param overtakingConditions the conditions for overtaking another GTU, viewed from this lane
     * @throws OTSGeometryException when creation of the center line or contour geometry fails
     * @throws NetworkException when id equal to null or not unique
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public Lane(final CrossSectionLink parentLink, final String id, final Length lateralOffset, final Length width,
            final LaneType laneType, final Map<GTUType, LongitudinalDirectionality> directionalityMap,
            final Map<GTUType, Speed> speedLimitMap, final OvertakingConditions overtakingConditions)
            throws OTSGeometryException, NetworkException
    {
        super(parentLink, id, lateralOffset, width);
        this.laneType = laneType;
        this.directionalityMap = directionalityMap;
        checkDirectionality();
        this.speedLimitMap = speedLimitMap;
        this.overtakingConditions = overtakingConditions;
    }

    /**
     * @param parentLink Cross Section Link to which the element belongs.
     * @param id the id of this lane within the link; should be unique within the link.
     * @param lateralOffset Length; the lateral offset of the design line of the new CrossSectionLink with respect to the design
     *            line of the parent Link
     * @param width Length; width, positioned <i>symmetrically around</i> the design line
     * @param laneType type of lane to deduce compatibility with GTU types
     * @param directionality in direction of geometry, reverse, or both
     * @param speedLimit speed limit on this lane
     * @param overtakingConditions the conditions for overtaking another GTU, viewed from this lane
     * @throws OTSGeometryException when creation of the center line or contour geometry fails
     * @throws NetworkException when id equal to null or not unique
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public Lane(final CrossSectionLink parentLink, final String id, final Length lateralOffset, final Length width,
            final LaneType laneType, final LongitudinalDirectionality directionality, final Speed speedLimit,
            final OvertakingConditions overtakingConditions) throws OTSGeometryException, NetworkException
    {
        super(parentLink, id, lateralOffset, width);
        this.laneType = laneType;
        this.directionalityMap = new LinkedHashMap<>(1);
        this.directionalityMap.put(GTUType.ALL, directionality);
        checkDirectionality();
        this.speedLimitMap = new LinkedHashMap<>();
        this.speedLimitMap.put(GTUType.ALL, speedLimit);
        this.overtakingConditions = overtakingConditions;
    }

    /**
     * @param parentLink Cross Section Link to which the element belongs.
     * @param id the id of this lane within the link; should be unique within the link.
     * @param crossSectionSlices The offsets and widths at positions along the line, relative to the design line of the parent
     *            link. If there is just one with and offset, there should just be one element in the list with Length = 0. If
     *            there are more slices, the last one should be at the length of the design line. If not, a NetworkException is
     *            thrown.
     * @param laneType type of lane to deduce compatibility with GTU types
     * @param directionalityMap in direction of geometry, reverse, or both, specified per GTU Type
     * @param speedLimitMap speed limit on this lane, specified per GTU Type
     * @param overtakingConditions the conditions for overtaking another GTU, viewed from this lane
     * @throws OTSGeometryException when creation of the center line or contour geometry fails
     * @throws NetworkException when id equal to null or not unique
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public Lane(final CrossSectionLink parentLink, final String id, final List<CrossSectionSlice> crossSectionSlices,
            final LaneType laneType, final Map<GTUType, LongitudinalDirectionality> directionalityMap,
            final Map<GTUType, Speed> speedLimitMap, final OvertakingConditions overtakingConditions)
            throws OTSGeometryException, NetworkException
    {
        super(parentLink, id, crossSectionSlices);
        this.laneType = laneType;
        this.directionalityMap = directionalityMap;
        checkDirectionality();
        this.speedLimitMap = speedLimitMap;
        this.overtakingConditions = overtakingConditions;
    }

    /**
     * @param parentLink Cross Section Link to which the element belongs.
     * @param id the id of this lane within the link; should be unique within the link.
     * @param crossSectionSlices The offsets and widths at positions along the line, relative to the design line of the parent
     *            link. If there is just one with and offset, there should just be one element in the list with Length = 0. If
     *            there are more slices, the last one should be at the length of the design line. If not, a NetworkException is
     *            thrown.
     * @param laneType type of lane to deduce compatibility with GTU types
     * @param directionality in direction of geometry, reverse, or both
     * @param speedLimit speed limit on this lane
     * @param overtakingConditions the conditions for overtaking another GTU, viewed from this lane
     * @throws OTSGeometryException when creation of the center line or contour geometry fails
     * @throws NetworkException when id equal to null or not unique
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public Lane(final CrossSectionLink parentLink, final String id, final List<CrossSectionSlice> crossSectionSlices,
            final LaneType laneType, final LongitudinalDirectionality directionality, final Speed speedLimit,
            final OvertakingConditions overtakingConditions) throws OTSGeometryException, NetworkException
    {
        super(parentLink, id, crossSectionSlices);
        this.laneType = laneType;
        this.directionalityMap = new LinkedHashMap<>(1);
        this.directionalityMap.put(GTUType.ALL, directionality);
        checkDirectionality();
        this.speedLimitMap = new LinkedHashMap<>();
        this.speedLimitMap.put(GTUType.ALL, speedLimit);
        this.overtakingConditions = overtakingConditions;
    }

    // TODO constructor calls with this(...)

    /**
     * Retrieve one of the sets of neighboring Lanes that is accessible for the given type of GTU. A defensive copy of the
     * internal data structure is returned.
     * @param direction LateralDirectionality; either LEFT or RIGHT, relative to the DESIGN LINE of the link (and the direction
     *            of the center line of the lane). In terms of offsets, 'left' lanes always have a more positive offset than the
     *            current lane, and 'right' lanes a more negative offset.
     * @param gtuType the GTU type to check the accessibility for
     * @return Set&lt;Lane&gt;; the indicated set of neighboring Lanes
     */
    private Set<Lane> neighbors(final LateralDirectionality direction, final GTUType gtuType)
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
                    if (laterallyAdjacentAndAccessible(lane, LateralDirectionality.LEFT, gtuType))
                    {
                        leftSet.add(lane);
                    }
                    if (laterallyAdjacentAndAccessible(lane, LateralDirectionality.RIGHT, gtuType))
                    {
                        rightSet.add(lane);
                    }
                }
            }
        }

        Set<Lane> lanes = new LinkedHashSet<>();
        if (direction == LateralDirectionality.LEFT)
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
     * @param lane the other lane to evaluate
     * @param direction the direction to look at, relative to the DESIGN LINE of the link. This is a very important aspect to
     *            note: all information is stored relative to the direction of the design line, and not in a driving direction,
     *            which can vary for lanes that can be driven in two directions (e.g. at overtaking).
     * @param gtuType the GTU type to check the accessibility for
     * @return whether another lane is adjacent to this lane and accessible for the given GTU type
     */
    private boolean laterallyAdjacentAndAccessible(final Lane lane, final LateralDirectionality direction, final GTUType gtuType)
    {
        if (!lane.getLaneType().isCompatible(gtuType) || gtuType.equals(GTUType.ALL) || gtuType.equals(GTUType.NONE))
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
     * Insert the sensor at the right place in the sensor list of this lane.
     * @param sensor the sensor to add
     * @param gtuType the GTU type that triggers this sensor; use GTUType.ALL is all GTUs trigger it
     * @throws NetworkException when the position of the sensor is beyond (or before) the range of this Lane
     */
    public final void addSensor(final Sensor sensor, final GTUType gtuType) throws NetworkException
    {
        double position = sensor.getLongitudinalPositionSI();
        if (position < 0 || position > getLength().getSI())
        {
            throw new NetworkException("Illegal position for sensor " + position + " valid range is 0.." + getLength().getSI());
        }
        List<GTUTypeSensor> sensorList = this.sensors.get(position);
        if (null == sensorList)
        {
            sensorList = new ArrayList<GTUTypeSensor>(1);
            this.sensors.put(position, sensorList);
        }
        sensorList.add(new GTUTypeSensor(gtuType, sensor));
    }

    /**
     * Remove a sensor from the sensor list of this lane.
     * @param sensor the sensor to remove.
     * @throws NetworkException when the sensor was not found on this Lane
     */
    public final void removeSensor(final Sensor sensor) throws NetworkException
    {
        List<GTUTypeSensor> sensorList = this.sensors.get(sensor.getLongitudinalPosition().getSI());
        if (null == sensorList)
        {
            throw new NetworkException("No sensor at " + sensor.getLongitudinalPositionSI());
        }
        List<GTUTypeSensor> sensorList2 = new ArrayList<GTUTypeSensor>(1);
        for (GTUTypeSensor gs : sensorList)
        {
            if (!gs.getSensor().equals(sensor))
            {
                sensorList2.add(gs);
            }
        }
        if (sensorList2.size() == 0)
        {
            this.sensors.remove(sensor.getLongitudinalPosition().doubleValue());
        }
        else
        {
            this.sensors.put(sensor.getLongitudinalPosition().doubleValue(), sensorList2);
        }
    }

    /**
     * Retrieve the list of Sensors of this Lane in the specified distance range for the given GTUType. The sensors that are
     * triggered by GTUTypes.ALL are added as well. The resulting list is a defensive copy.
     * @param minimumPosition Length; the minimum distance on the Lane (exclusive)
     * @param maximumPosition Length; the maximum distance on the Lane (inclusive)
     * @param gtuType the GTU type to provide the sensors for
     * @return List&lt;Sensor&gt;; list of the sensor in the specified range
     */
    public final List<Sensor> getSensors(final Length minimumPosition, final Length maximumPosition, final GTUType gtuType)
    {
        List<Sensor> sensorList = new ArrayList<>(1);
        for (List<GTUTypeSensor> gtsl : this.sensors.values())
        {
            for (GTUTypeSensor gs : gtsl)
            {
                if ((gs.getGtuType().equals(gtuType) || gs.getGtuType().equals(GTUType.ALL))
                        && gs.getSensor().getLongitudinalPosition().gt(minimumPosition)
                        && gs.getSensor().getLongitudinalPosition().le(maximumPosition))
                {
                    sensorList.add(gs.getSensor());
                }
            }
        }
        return sensorList;
    }

    /**
     * Retrieve the list of Sensors of this Lane that are triggered by the given GTUType. The sensors that are triggered by
     * GTUTypes.ALL are added as well. The resulting list is a defensive copy.
     * @param gtuType the GTU type to provide the sensors for
     * @return List&lt;Sensor&gt;; list of the sensors, in ascending order for the location on the Lane
     */
    public final List<Sensor> getSensors(final GTUType gtuType)
    {
        List<Sensor> sensorList = new ArrayList<>(1);
        for (List<GTUTypeSensor> gtsl : this.sensors.values())
        {
            for (GTUTypeSensor gs : gtsl)
            {
                if ((gs.getGtuType().equals(gtuType) || gs.getGtuType().equals(GTUType.ALL)))
                {
                    sensorList.add(gs.getSensor());
                }
            }
        }
        return sensorList;
    }

    /**
     * Retrieve the list of all Sensors of this Lane. The resulting list is a defensive copy.
     * @return List&lt;Sensor&gt;; list of the sensors, in ascending order for the location on the Lane
     */
    public final List<Sensor> getSensors()
    {
        List<Sensor> sensorList = new ArrayList<>(1);
        for (List<GTUTypeSensor> gtsl : this.sensors.values())
        {
            for (GTUTypeSensor gs : gtsl)
            {
                sensorList.add(gs.getSensor());
            }
        }
        return sensorList;
    }

    /**
     * Retrieve the list of Sensors of this Lane for the given GTUType. The sensors that are triggered by GTUTypes.ALL are added
     * as well. The resulting Map is a defensive copy.
     * @param gtuType the GTU type to provide the sensors for
     * @return all sensors on this lane for the given GTUType as a map per distance
     */
    public final SortedMap<Double, List<Sensor>> getSensorMap(final GTUType gtuType)
    {
        SortedMap<Double, List<Sensor>> sensorMap = new TreeMap<>();
        for (double d : this.sensors.keySet())
        {
            List<Sensor> sensorList = new ArrayList<>(1);
            for (GTUTypeSensor gs : this.sensors.get(d))
            {
                if (gs.getGtuType().equals(gtuType) || gs.getGtuType().equals(GTUType.ALL))
                {
                    sensorList.add(gs.getSensor());
                }
            }
            if (sensorList.size() > 0)
            {
                sensorMap.put(d, sensorList);
            }
        }
        return sensorMap;
    }

    /**
     * Trigger the sensors for a certain time step; from now until the nextEvaluationTime of the GTU.
     * @param gtu the LaneBasedGTU for which to trigger the sensors.
     * @param referenceStartSI the SI distance of the GTU reference point on the lane at the current time
     * @param referenceMoveSI the SI distance traveled in the next time step.
     * @throws NetworkException when GTU not on this lane.
     * @throws SimRuntimeException when method cannot be scheduled.
     */
    public final void scheduleTriggers(final LaneBasedGTU gtu, final double referenceStartSI, final double referenceMoveSI)
            throws NetworkException, SimRuntimeException
    {
        for (List<Sensor> sensorList : getSensorMap(gtu.getGTUType()).values())
        {
            for (Sensor sensor : sensorList)
            {
                for (RelativePosition relativePosition : gtu.getRelativePositions().values())
                {
                    // System.out.println("GTU relative position " + relativePosition + " sensor relative position " + sensor.getPositionType());
                    if (sensor.getPositionType().equals(relativePosition.getType())
                            && referenceStartSI + relativePosition.getDx().getSI() <= sensor.getLongitudinalPositionSI()
                            && referenceStartSI + referenceMoveSI + relativePosition.getDx().getSI() > sensor
                                    .getLongitudinalPositionSI())
                    {
                        // the exact time of triggering is based on the distance between the current position of the
                        // relative position on the GTU and the location of the sensor.
                        // TODO make sure triggering is done right when driving in DIR_MINUS direction
                        double d = sensor.getLongitudinalPositionSI() - referenceStartSI - relativePosition.getDx().getSI();
                        if (d < 0)
                        {
                            throw new NetworkException("scheduleTriggers for gtu: " + gtu + ", d<0 d=" + d);
                        }

                        OperationalPlan oPlan = gtu.getOperationalPlan();
                        Time triggerTime = oPlan.timeAtDistance(new Length(d, LengthUnit.METER));
                        if (triggerTime.gt(oPlan.getEndTime()))
                        {
                            System.err.println("Time=" + gtu.getSimulator().getSimulatorTime().getTime().getSI()
                                    + " - Scheduling trigger at " + triggerTime.getSI() + "s. > " + oPlan.getEndTime().getSI()
                                    + "s. (nextEvalTime) for sensor " + sensor + " , gtu " + gtu);
                            System.err.println("  v=" + gtu.getSpeed() + ", a=" + gtu.getAcceleration() + ", lane="
                                    + toString() + ", refStartSI=" + referenceStartSI + ", moveSI=" + referenceMoveSI);
                            triggerTime =
                                    new Time(oPlan.getEndTime().getSI() - Math.ulp(oPlan.getEndTime().getSI()), TimeUnit.SI);
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
     * Transform a fraction on the lane to a relative length (can be less than zero or larger than the lane length).
     * @param fraction fraction relative to the lane length.
     * @return relative length corresponding to the fraction.
     */
    public final Length position(final double fraction)
    {
        return new Length(this.getLength().getInUnit() * fraction, this.getLength().getUnit());
    }

    /**
     * Transform a fraction on the lane to a relative length in SI units (can be less than zero or larger than the lane length).
     * @param fraction fraction relative to the lane length.
     * @return relative length corresponding to the fraction, in SI units.
     */
    public final double positionSI(final double fraction)
    {
        return this.getLength().getSI() * fraction;
    }

    /**
     * Transform a position on the lane (can be less than zero or larger than the lane length) to a fraction.
     * @param position relative length on the lane (may be less than zero or larger than the lane length).
     * @return fraction fraction relative to the lane length.
     */
    public final double fraction(final Length position)
    {
        return position.getSI() / this.getLength().getSI();
    }

    /**
     * Transform a position on the lane in SI units (can be less than zero or larger than the lane length) to a fraction.
     * @param positionSI relative length on the lane in SI units (may be less than zero or larger than the lane length).
     * @return fraction fraction relative to the lane length.
     */
    public final double fractionSI(final double positionSI)
    {
        return positionSI / this.getLength().getSI();
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
        // figure out the rank for the new GTU
        int index;
        for (index = 0; index < this.gtuList.size(); index++)
        {
            LaneBasedGTU otherGTU = this.gtuList.get(index);
            if (gtu == otherGTU)
            {
                throw new GTUException(gtu + " already registered on Lane " + this + " [registered lanes: "
                        + gtu.positions(gtu.getFront()).keySet() + "] locations: " + gtu.positions(gtu.getFront()).values()
                        + " time: " + gtu.getSimulator().getSimulatorTime().getTime());
            }
            if (otherGTU.fractionalPosition(this, otherGTU.getFront()) >= fractionalPosition)
            {
                break;
            }
        }
        this.gtuList.add(index, gtu);
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
     */
    public final void removeGTU(final LaneBasedGTU gtu)
    {
        this.gtuList.remove(gtu);
    }

    /**
     * Get the first GTU where the relativePosition is in front of a certain position on the lane, in a driving direction on
     * this lane, compared to the DESIGN LINE.
     * @param position the position after which the relative position of a GTU will be searched.
     * @param direction whether we are looking in the the center line direction or against the center line direction.
     * @param relativePosition the relative position we want to compare against
     * @param when the time for which to evaluate the positions.
     * @return the first GTU after a position on this lane in the given direction, or null if no GTU could be found.
     * @throws GTUException when there is a problem with the position of the GTUs on the lane.
     */
    public final LaneBasedGTU getGtuAhead(final Length position, final GTUDirectionality direction,
            final RelativePosition.TYPE relativePosition, final Time when) throws GTUException
    {
        if (direction.equals(GTUDirectionality.DIR_PLUS))
        {
            for (LaneBasedGTU gtu : this.gtuList)
            {
                if (gtu.position(this, gtu.getRelativePositions().get(relativePosition), when).gt(position))
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
     * Get the first GTU where the relativePosition is behind a certain position on the lane, in a driving direction on this
     * lane, compared to the DESIGN LINE.
     * @param position the position before which the relative position of a GTU will be searched.
     * @param direction whether we are looking in the the center line direction or against the center line direction.
     * @param relativePosition the relative position of the GTU we are looking for.
     * @param when the time for which to evaluate the positions.
     * @return the first GTU before a position on this lane in the given direction, or null if no GTU could be found.
     * @throws GTUException when there is a problem with the position of the GTUs on the lane.
     */
    public final LaneBasedGTU getGtuBehind(final Length position, final GTUDirectionality direction,
            final RelativePosition.TYPE relativePosition, final Time when) throws GTUException
    {
        if (direction.equals(GTUDirectionality.DIR_PLUS))
        {
            return getGtuAhead(position, GTUDirectionality.DIR_MINUS, relativePosition, when);
        }
        return getGtuAhead(position, direction, relativePosition, when);
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
                            Length df = this.getCenterLine().getLast().distance(lane.getCenterLine().getFirst());
                            Length dl = this.getCenterLine().getLast().distance(lane.getCenterLine().getLast());
                            // this, parentLink ---> O ---> lane, link
                            if (df.lt(MARGIN) && df.lt(dl) && link.getStartNode().equals(getParentLink().getEndNode()))
                            {
                                // does the GTU move in the design line direction or against it?
                                // TODO And is it aligned with its next lane?
                                if (lane.getDirectionality(gtuType).isForwardOrBoth())
                                {
                                    laneMap.put(lane, GTUDirectionality.DIR_PLUS);
                                }
                                else if (lane.getDirectionality(gtuType).isBackwardOrBoth())
                                {
                                    laneMap.put(lane, GTUDirectionality.DIR_MINUS);
                                }
                            }
                            // this, parentLink ---> O <--- lane, link
                            else if (dl.lt(MARGIN) && dl.lt(df) && link.getEndNode().equals(getParentLink().getEndNode()))
                            {
                                // does the GTU move in the design line direction or against it?
                                // TODO And is it aligned with its next lane?
                                if (lane.getDirectionality(gtuType).isForwardOrBoth())
                                {
                                    laneMap.put(lane, GTUDirectionality.DIR_PLUS);
                                }
                                else if (lane.getDirectionality(gtuType).isBackwardOrBoth())
                                {
                                    laneMap.put(lane, GTUDirectionality.DIR_MINUS);
                                }
                            }
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
                            Length df = this.getCenterLine().getFirst().distance(lane.getCenterLine().getFirst());
                            Length dl = this.getCenterLine().getFirst().distance(lane.getCenterLine().getLast());
                            // this, parentLink <--- O ---> lane, link
                            if (df.lt(MARGIN) && df.lt(dl) && link.getStartNode().equals(getParentLink().getStartNode()))
                            {
                                // does the GTU move in the design line direction or against it?
                                // TODO And is it aligned with its next lane?
                                if (lane.getDirectionality(gtuType).isForwardOrBoth())
                                {
                                    laneMap.put(lane, GTUDirectionality.DIR_PLUS);
                                }
                                else if (lane.getDirectionality(gtuType).isBackwardOrBoth())
                                {
                                    laneMap.put(lane, GTUDirectionality.DIR_MINUS);
                                }
                            }
                            // this, parentLink <--- O <--- lane, link
                            else if (dl.lt(MARGIN) && dl.lt(df) && link.getEndNode().equals(getParentLink().getStartNode()))
                            {
                                // does the GTU move in the design line direction or against it?
                                // TODO And is it aligned with its next lane?
                                if (lane.getDirectionality(gtuType).isForwardOrBoth())
                                {
                                    laneMap.put(lane, GTUDirectionality.DIR_PLUS);
                                }
                                else if (lane.getDirectionality(gtuType).isBackwardOrBoth())
                                {
                                    laneMap.put(lane, GTUDirectionality.DIR_MINUS);
                                }
                            }
                        }
                    }
                }
            }
        }
        return this.prevLanes.get(gtuType);
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
     * @return the set of lanes that are accessible, or null if there is no lane that is accessible with a matching driving
     *         direction.
     */
    public final Set<Lane> accessibleAdjacentLanes(final LateralDirectionality lateralDirection, final GTUType gtuType)
    {
        Set<Lane> candidates = new LinkedHashSet<>(1);
        LateralDirectionality dir =
                this.getDirectionality(gtuType).isForwardOrBoth() ? lateralDirection : lateralDirection.isLeft()
                        ? LateralDirectionality.RIGHT : LateralDirectionality.LEFT;
        for (Lane lane : neighbors(dir, gtuType))
        {
            if (lane.getDirectionality(gtuType).equals(LongitudinalDirectionality.DIR_BOTH)
                    || lane.getDirectionality(gtuType).equals(this.getDirectionality(gtuType)))
            {
                candidates.add(lane);
            }
        }
        return candidates;
    }

    /**
     * Register a LaneBasedGTUSampler on this Lane.
     * @param sampler LaneBasedGTUSampler; the sampler to register
     */
    public final void addSampler(final LaneBasedGTUSampler sampler)
    {
        this.samplers.add(sampler);
    }

    /**
     * Unregister a LaneBasedGTUSampler from this Lane.
     * @param sampler LaneBasedGTUSampler; the sampler to unregister
     */
    public final void removeSampler(final LaneBasedGTUSampler sampler)
    {
        this.samplers.remove(sampler);
    }

    /**
     * Add the movement of a GTU to all graphs that sample this Lane.
     * @param gtu AbstractLaneBasedGTU; the GTU to sample
     * @throws NetworkException on network inconsistency
     * @throws GTUException on problems obtaining data from the GTU for the graph
     */
    public final void sample(final LaneBasedGTU gtu) throws NetworkException, GTUException
    {
        for (LaneBasedGTUSampler sampler : this.samplers)
        {
            sampler.addData(gtu, this);
        }
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
        if (this.speedLimitMap.containsKey(GTUType.ALL))
        {
            return this.speedLimitMap.get(GTUType.ALL);
        }
        throw new NetworkException("No speed limit set for GTUType " + gtuType + " on lane " + toString());
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
     * The direction in which vehicles can drive, i.e., in direction of geometry, reverse, or both. This can differ per GTU
     * type. In an overtake lane, cars might overtake and trucks not. It might be that the lane (e.g., a street in a city) is
     * FORWARD (from start node of the link to end node of the link) for the GTU type CAR, but BOTH for the GTU type BICYCLE
     * (i.e., bicycles can also go in the other direction, opposite to the drawing direction of the Link). If the directionality
     * for a GTUType is set to NONE, this means that the given GTUType cannot use the Lane. If a Directionality is set for
     * GTUType.ALL, the getDirectionality will default to these settings when there is no specific entry for a given
     * directionality. This means that the settings can be used additive, or restrictive. <br>
     * In <b>additive use</b>, set the directionality for GTUType.ALL to NONE, or do not set the directionality for GTUType.ALL.
     * Now, one by one, the allowed directionalities can be added. An example is a lane on a highway, which we only open for
     * CAR, TRUCK and BUS. <br>
     * In <b>restrictive use</b>, set the directionality for GTUType.ALL to BOTH, FORWARD, or BACKWARD. Override the
     * directionality for certain GTUTypes to a more restrictive access, e.g. to NONE. An example is a lane that is open for all
     * road users, except TRUCK.
     * @param gtuType the GTU type to provide the directionality for
     * @return the directionality.
     */
    public final LongitudinalDirectionality getDirectionality(final GTUType gtuType)
    {
        if (this.directionalityMap.containsKey(gtuType))
        {
            return this.directionalityMap.get(gtuType);
        }
        if (this.directionalityMap.containsKey(GTUType.ALL))
        {
            return this.directionalityMap.get(GTUType.ALL);
        }
        return LongitudinalDirectionality.DIR_NONE;
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
    public void addDirectionality(final GTUType gtuType, final LongitudinalDirectionality directionality)
            throws NetworkException
    {
        this.directionalityMap.put(gtuType, directionality);
        checkDirectionality();
    }

    /**
     * This method removes an earlier provided directionality of the lane for a given GTU type, e.g. for maintenance of the
     * lane. After removing, the directionality for the GTU will fall back to the provided directionality for GTUType.ALL (if
     * present). Thereby removing a directionality is different from setting the directionality to NONE.
     * @param gtuType the GTU type to remove the directionality for on this lane.
     */
    public void removeDirectionality(final GTUType gtuType)
    {
        this.directionalityMap.remove(gtuType);
    }

    /**
     * Check whether the directionalities for the GTU types for this lane are consistent with the directionalities of the
     * overarching Link.
     * @throws NetworkException when the lane directionality for a given GTUType is inconsistent with the Link directionality to
     *             which the lane belongs.
     */
    private void checkDirectionality() throws NetworkException
    {
        for (GTUType gtuType : this.directionalityMap.keySet())
        {
            LongitudinalDirectionality directionality = this.directionalityMap.get(gtuType);
            if (!getParentLink().getDirectionality(gtuType).contains(directionality))
            {
                throw new NetworkException("Lane " + toString() + " allows " + gtuType + " a directionality of "
                        + directionality + " which is not present in the overarching link " + getParentLink().toString());
            }
        }
    }

    /**
     * @return gtuList.
     */
    public final List<LaneBasedGTU> getGtuList()
    {
        return this.gtuList;
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
    public final String toString()
    {
        CrossSectionLink link = getParentLink();
        return String.format("Lane %s of %s", getId(), link.getId());
    }

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.laneType == null) ? 0 : this.laneType.hashCode());
        return result;
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

    /**
     * The combination of GTUType and Sensor in one record.
     * <p>
     * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * $LastChangedDate: 2015-09-24 14:17:07 +0200 (Thu, 24 Sep 2015) $, @version $Revision: 1407 $, by $Author: averbraeck $,
     * initial version Aug 28, 2015 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    private class GTUTypeSensor implements Serializable
    {
        /** */
        private static final long serialVersionUID = 20150828L;

        /** The GTU type that triggers this sensor; GTUType.ALL if all GTU types trigger the sensor. */
        private final GTUType gtuType;

        /** The sensor that is triggers by the gtuType. */
        private final Sensor sensor;

        /**
         * @param gtuType the GTU type that triggers this sensor; GTUType.ALL if all GTU types trigger the sensor
         * @param sensor the sensor that is triggers by the gtuType
         */
        public GTUTypeSensor(final GTUType gtuType, final Sensor sensor)
        {
            this.gtuType = gtuType;
            this.sensor = sensor;
        }

        /**
         * @return gtuType
         */
        public final GTUType getGtuType()
        {
            return this.gtuType;
        }

        /**
         * @return sensor
         */
        public final Sensor getSensor()
        {
            return this.sensor;
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "GTUTypeSensor [gtuType=" + this.gtuType + ", sensor=" + this.sensor + "]";
        }

    }

}
