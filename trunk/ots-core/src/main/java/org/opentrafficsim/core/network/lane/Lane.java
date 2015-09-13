package org.opentrafficsim.core.network.lane;

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

import org.djunits.unit.TimeUnit;
import org.opentrafficsim.core.OTS_SCALAR;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.lane.AbstractLaneBasedGTU;
import org.opentrafficsim.core.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.graphs.LaneBasedGTUSampler;

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
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Aug 19, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public class Lane extends CrossSectionElement implements Serializable, OTS_SCALAR
{
    /** */
    private static final long serialVersionUID = 20150826L;

    /** type of lane to deduce compatibility with GTU types. */
    private final LaneType laneType;

    /**
     * The direction in which vehicles can drive, i.e., in direction of geometry, reverse, or both. This can differ per GTU
     * type. In an overtake lane, cars might overtake and trucks not
     */
    private final Map<GTUType, LongitudinalDirectionality> directionalityMap;

    /**
     * the speed limit of this lane. This can differ per GTU type. Cars might be allowed to drive 120 km/h and trucks 90 km/h.
     * If the speed limit is the same for all GTU types, GTUType.ALL will be used.
     */
    private Map<GTUType, Speed.Abs> speedLimitMap;

    /**
     * Sensors on the lane to trigger behavior of the GTU, sorted by longitudinal position. The triggering of sensors is done
     * per GTU type, so different GTUs can trigger different sensors.
     */
    private final SortedMap<Double, List<GTUTypeSensor>> sensors = new TreeMap<>();

    /** GTUs ordered by increasing longitudinal position. */
    private final List<LaneBasedGTU> gtuList = new ArrayList<LaneBasedGTU>();

    /**
     * Adjacent left lanes that some GTU types can change onto. Initially null so we can calculate and cache the first time the
     * method is called.
     */
    private Map<GTUType, Set<Lane>> leftNeighbors = null;

    /**
     * Adjacent right lanes that some GTU types can change onto. Initially null so we can calculate and cache the first time the
     * method is called.
     */
    private Map<GTUType, Set<Lane>> rightNeighbors = null;

    /**
     * Next lane(s) following this lane that some GTU types can drive onto. Initially null so we can calculate and cache the
     * first time the method is called.
     */
    private Map<GTUType, Set<Lane>> nextLanes = null;

    /**
     * Previous lane(s) preceding this lane that some GTU types can drive onto. Initially null so we can calculate and cache the
     * first time the method is called.
     */
    private Map<GTUType, Set<Lane>> prevLanes = null;

    /** List of graphs that want to sample GTUs on this Lane. */
    private ArrayList<LaneBasedGTUSampler> samplers = new ArrayList<LaneBasedGTUSampler>();

    /**
     * @param parentLink Cross Section Link to which the element belongs.
     * @param id the id of this lane within the link; should be unique within the link.
     * @param lateralOffsetAtStart DoubleScalar.Rel&lt;LengthUnit&gt;; the lateral offset of the design line of the new
     *            CrossSectionLink with respect to the design line of the parent Link at the start of the parent Link
     * @param lateralOffsetAtEnd DoubleScalar.Rel&lt;LengthUnit&gt;; the lateral offset of the design line of the new
     *            CrossSectionLink with respect to the design line of the parent Link at the end of the parent Link
     * @param beginWidth DoubleScalar.Rel&lt;LengthUnit&gt;; start width, positioned <i>symmetrically around</i> the design line
     * @param endWidth DoubleScalar.Rel&lt;LengthUnit&gt;; end width, positioned <i>symmetrically around</i> the design line
     * @param laneType type of lane to deduce compatibility with GTU types
     * @param directionalityMap in direction of geometry, reverse, or both, specified per GTU Type
     * @param speedLimitMap speed limit on this lane, specified per GTU Type
     * @throws OTSGeometryException when creation of the center line or contour geometry fails
     * @throws NetworkException when id equal to null or not unique
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public Lane(final CrossSectionLink parentLink, final String id, final Length.Rel lateralOffsetAtStart,
        final Length.Rel lateralOffsetAtEnd, final Length.Rel beginWidth, final Length.Rel endWidth,
        final LaneType laneType, final Map<GTUType, LongitudinalDirectionality> directionalityMap,
        final Map<GTUType, Speed.Abs> speedLimitMap) throws OTSGeometryException, NetworkException
    {
        super(parentLink, id, lateralOffsetAtStart, lateralOffsetAtEnd, beginWidth, endWidth);
        this.laneType = laneType;
        this.directionalityMap = directionalityMap;
        this.speedLimitMap = speedLimitMap;
    }

    /**
     * @param parentLink Cross Section Link to which the element belongs.
     * @param id the id of this lane within the link; should be unique within the link.
     * @param lateralOffsetAtStart DoubleScalar.Rel&lt;LengthUnit&gt;; the lateral offset of the design line of the new
     *            CrossSectionLink with respect to the design line of the parent Link at the start of the parent Link
     * @param lateralOffsetAtEnd DoubleScalar.Rel&lt;LengthUnit&gt;; the lateral offset of the design line of the new
     *            CrossSectionLink with respect to the design line of the parent Link at the end of the parent Link
     * @param beginWidth DoubleScalar.Rel&lt;LengthUnit&gt;; start width, positioned <i>symmetrically around</i> the design line
     * @param endWidth DoubleScalar.Rel&lt;LengthUnit&gt;; end width, positioned <i>symmetrically around</i> the design line
     * @param laneType type of lane to deduce compatibility with GTU types
     * @param directionality in direction of geometry, reverse, or both
     * @param speedLimit speed limit on this lane
     * @throws OTSGeometryException when creation of the center line or contour geometry fails
     * @throws NetworkException when id equal to null or not unique
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public Lane(final CrossSectionLink parentLink, final String id, final Length.Rel lateralOffsetAtStart,
        final Length.Rel lateralOffsetAtEnd, final Length.Rel beginWidth, final Length.Rel endWidth,
        final LaneType laneType, final LongitudinalDirectionality directionality, final Speed.Abs speedLimit)
        throws OTSGeometryException, NetworkException
    {
        super(parentLink, id, lateralOffsetAtStart, lateralOffsetAtEnd, beginWidth, endWidth);
        this.laneType = laneType;
        this.directionalityMap = new LinkedHashMap<>(1);
        this.directionalityMap.put(GTUType.ALL, directionality);
        this.speedLimitMap = new LinkedHashMap<>();
        this.speedLimitMap.put(GTUType.ALL, speedLimit);
    }

    /**
     * Retrieve one of the sets of neighboring Lanes that is accessible for the given type of GTU. A defensive copy of the
     * internal data structure is returned.
     * @param direction LateralDirectionality; either LEFT or RIGHT, relative to the DESIGN LINE of the link
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
    static final Length.Rel ADJACENT_MARGIN = new Length.Rel(0.2, METER);

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
    private boolean laterallyAdjacentAndAccessible(final Lane lane, final LateralDirectionality direction,
        final GTUType gtuType)
    {
        if (!lane.getLaneType().isCompatible(gtuType) || gtuType.equals(GTUType.ALL) || gtuType.equals(GTUType.NONE))
        {
            // not accessible for the given GTU type
            return false;
        }

        if (direction.equals(LateralDirectionality.LEFT))
        {
            if (Math.abs((this.designLineOffsetAtBegin.getSI() + this.beginWidth.getSI() / 2.0)
                - (lane.designLineOffsetAtBegin.getSI() - lane.beginWidth.getSI() / 2.0)) < ADJACENT_MARGIN.getSI()
                && Math.abs((this.designLineOffsetAtEnd.getSI() + this.endWidth.getSI() / 2.0)
                    - (lane.designLineOffsetAtEnd.getSI() - lane.endWidth.getSI() / 2.0)) < ADJACENT_MARGIN.getSI())
            {
                // look at stripes between the two lanes
                for (CrossSectionElement cse : this.parentLink.getCrossSectionElementList())
                {
                    if (cse instanceof Stripe)
                    {
                        Stripe stripe = (Stripe) cse;
                        if (Math.abs((this.designLineOffsetAtBegin.getSI() + this.beginWidth.getSI() / 2.0)
                            - stripe.designLineOffsetAtBegin.getSI()) < ADJACENT_MARGIN.getSI()
                            && Math.abs((this.designLineOffsetAtEnd.getSI() + this.endWidth.getSI() / 2.0)
                                - stripe.designLineOffsetAtEnd.getSI()) < ADJACENT_MARGIN.getSI())
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
            if (Math.abs((this.designLineOffsetAtBegin.getSI() - this.beginWidth.getSI() / 2.0)
                - (lane.designLineOffsetAtBegin.getSI() + lane.beginWidth.getSI() / 2.0)) < ADJACENT_MARGIN.getSI()
                && Math.abs((this.designLineOffsetAtEnd.getSI() - this.endWidth.getSI() / 2.0)
                    - (lane.designLineOffsetAtEnd.getSI() + lane.endWidth.getSI() / 2.0)) < ADJACENT_MARGIN.getSI())
            {
                // look at stripes between the two lanes
                for (CrossSectionElement cse : this.parentLink.getCrossSectionElementList())
                {
                    if (cse instanceof Stripe)
                    {
                        Stripe stripe = (Stripe) cse;
                        if (Math.abs((this.designLineOffsetAtBegin.getSI() - this.beginWidth.getSI() / 2.0)
                            - stripe.designLineOffsetAtBegin.getSI()) < ADJACENT_MARGIN.getSI()
                            && Math.abs((this.designLineOffsetAtEnd.getSI() - this.endWidth.getSI() / 2.0)
                                - stripe.designLineOffsetAtEnd.getSI()) < ADJACENT_MARGIN.getSI())
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
            throw new NetworkException("Illegal position for sensor " + position + " valid range is 0.."
                + getLength().getSI());
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
     * @param minimumPosition DoubleScalar.Rel&lt;LengthUnit&gt;; the minimum distance on the Lane
     * @param maximumPosition DoubleScalar.Rel&lt;LengthUnit&gt;; the maximum distance on the Lane
     * @param gtuType the GTU type to provide the sensors for
     * @return List&lt;Sensor&gt;; list of the sensor in the specified range
     */
    public final List<Sensor> getSensors(final Length.Rel minimumPosition, final Length.Rel maximumPosition,
        final GTUType gtuType)
    {
        ArrayList<Sensor> result = new ArrayList<Sensor>();
        for (List<Sensor> sensorList : this.getSensors(gtuType).subMap(minimumPosition.doubleValue(),
            maximumPosition.doubleValue()).values())
        {
            result.addAll(sensorList);
        }
        for (List<Sensor> sensorList : this.getSensors(GTUType.ALL).subMap(minimumPosition.doubleValue(),
            maximumPosition.doubleValue()).values())
        {
            result.addAll(sensorList);
        }
        return result;
    }

    /**
     * Retrieve the list of Sensors of this Lane for the given GTUType. The sensors that are triggered by GTUTypes.ALL are added
     * as well. The resulting Map is a defensive copy.
     * @param gtuType the GTU type to provide the sensors for
     * @return all sensors on this lane for the given GTUType.
     */
    public final SortedMap<Double, List<Sensor>> getSensors(final GTUType gtuType)
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
        for (List<Sensor> sensorList : getSensors(gtu.getGTUType()).values())
        {
            for (Sensor sensor : sensorList)
            {
                for (RelativePosition relativePosition : gtu.getRelativePositions().values())
                {
                    if (sensor.getPositionType().equals(relativePosition.getType())
                        && referenceStartSI + relativePosition.getDx().getSI() <= sensor.getLongitudinalPositionSI()
                        && referenceStartSI + referenceMoveSI + relativePosition.getDx().getSI() > sensor
                            .getLongitudinalPositionSI())
                    {
                        // the exact time of triggering is based on the distance between the current position of the
                        // relative position on the GTU and the location of the sensor.
                        double d = sensor.getLongitudinalPositionSI() - referenceStartSI - relativePosition.getDx().getSI();
                        if (d < 0)
                        {
                            throw new NetworkException("scheduleTriggers for gtu: " + gtu + ", d<0 d=" + d);
                        }

                        Time.Abs triggerTime = gtu.timeAtDistance(new Length.Rel(d, METER));
                        if (triggerTime.gt(gtu.getNextEvaluationTime()))
                        {
                            System.err.println("Time=" + gtu.getSimulator().getSimulatorTime().getTime().getSI()
                                + " - Scheduling trigger at " + triggerTime.getSI() + "s. > "
                                + gtu.getNextEvaluationTime().getSI() + "s. (nextEvalTime) for sensor " + sensor + " , gtu "
                                + gtu);
                            System.err.println("  v=" + gtu.getVelocity() + ", a=" + gtu.getAcceleration() + ", lane="
                                + toString() + ", refStartSI=" + referenceStartSI + ", moveSI=" + referenceMoveSI);
                            triggerTime =
                                new Time.Abs(gtu.getNextEvaluationTime().getSI()
                                    - Math.ulp(gtu.getNextEvaluationTime().getSI()), TimeUnit.SI);
                            // gtu.timeAtDistance(new Length.Rel(-d, METER));
                            // System.exit(-1);
                        }
                        // System.out.println("Time=" + gtu.getSimulator().getSimulatorTime().toString()
                        // + " - Scheduling trigger at " + triggerTime + " for sensor " + sensor + " , gtu " + gtu);
                        gtu.getSimulator().scheduleEventAbs(triggerTime, this, sensor, "trigger", new Object[]{gtu});
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
    public final Length.Rel position(final double fraction)
    {
        return new Length.Rel(this.getLength().getInUnit() * fraction, this.getLength().getUnit());
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
    public final double fraction(final Length.Rel position)
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
     * @throws NetworkException when the fractionalPosition is outside the range 0..1, or the GTU is already registered on this
     *             Lane
     */
    public final int addGTU(final LaneBasedGTU gtu, final double fractionalPosition) throws
        NetworkException
    {
        // figure out the rank for the new GTU
        int index;
        for (index = 0; index < this.gtuList.size(); index++)
        {
            LaneBasedGTU otherGTU = this.gtuList.get(index);
            if (gtu == otherGTU)
            {
                throw new NetworkException("GTU " + gtu + " already registered on Lane " + this + " [registered lanes: "
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
     * @param longitudinalPosition DoubleScalar.Rel&lt;LengthUnit&gt;; the longitudinal position that the newly added GTU will
     *            have on this Lane
     * @return int; the rank that the newly added GTU has on this Lane (should be 0, except when the GTU enters this Lane due to
     *         a lane change operation)
     * @throws NetworkException when longitudinalPosition is negative or exceeds the length of this Lane
     */
    public final int addGTU(final LaneBasedGTU gtu, final Length.Rel longitudinalPosition) throws
        NetworkException
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
     * @param position the front position after which the relative position of a GTU will be searched.
     * @param relativePosition the relative position of the GTU we are looking for.
     * @param when the time for which to evaluate the positions.
     * @return the first GTU after a position on this lane, or null if no GTU could be found.
     * @throws NetworkException when there is a problem with the position of the GTUs on the lane.
     */
    public final LaneBasedGTU getGtuAfter(final Length.Rel position, final RelativePosition.TYPE relativePosition,
        final Time.Abs when) throws NetworkException
    {
        for (LaneBasedGTU gtu : this.gtuList)
        {
            if (relativePosition.equals(RelativePosition.FRONT))
            {
                if (gtu.position(this, gtu.getFront(), when).gt(position))
                {
                    return gtu;
                }
            }
            else if (relativePosition.equals(RelativePosition.REAR))
            {
                if (gtu.position(this, gtu.getRear(), when).ge(position))// PK was >; not >=
                {
                    return gtu;
                }
            }
            else
            {
                throw new NetworkException("Can only use Lane.getGtuAfter(...) method with FRONT and REAR positions");
            }
        }
        return null;
    }

    /**
     * @param position the front position before which the relative position of a GTU will be searched.
     * @param relativePosition the relative position of the GTU we are looking for.
     * @param when the time for which to evaluate the positions.
     * @return the first GTU before a position on this lane, or null if no GTU could be found.
     * @throws NetworkException when there is a problem with the position of the GTUs on the lane.
     */
    public final LaneBasedGTU getGtuBefore(final Length.Rel position, final RelativePosition.TYPE relativePosition,
        final Time.Abs when) throws NetworkException
    {
        for (int i = this.gtuList.size() - 1; i >= 0; i--)
        {
            LaneBasedGTU gtu = this.gtuList.get(i);
            if (relativePosition.equals(RelativePosition.FRONT))
            {
                if (gtu.position(this, gtu.getFront(), when).getSI() < position.getSI())
                {
                    return gtu;
                }
            }
            else if (relativePosition.equals(RelativePosition.REAR))
            {
                if (gtu.position(this, gtu.getRear(), when).getSI() < position.getSI())
                {
                    return gtu;
                }
            }
            else
            {
                throw new NetworkException("Can only use Lane.getGtuBefore(...) method with FRONT and REAR positions");
            }
        }
        return null;
    }

    /**
     * Are two cross section elements laterally well enough aligned to be longitudinally connected?
     * @param incomingCSE CrossSectionElement; the cross section element where the end position is considered
     * @param outgoingCSE CrossSectionElement; the cross section element where the begin position is considered
     * @param margin DoubleScalar.Rel&lt;LengthUnit&gt;; the maximum accepted alignment error
     * @return boolean; true if the two cross section elements are well enough aligned to be connected
     */
    private boolean laterallyCloseEnough(final CrossSectionElement incomingCSE, final CrossSectionElement outgoingCSE,
        final Length.Rel margin)
    {
        return Math.abs(incomingCSE.getDesignLineOffsetAtEnd().getSI() //
            - outgoingCSE.getDesignLineOffsetAtBegin().getSI()) <= margin.getSI();
    }

    /*
     * TODO only center position? Or also width? What is a good cutoff? Base on average width of the GTU type that can drive on
     * this Lane? E.g., for a Tram or Train, a 5 cm deviation is a problem; for a Car or a Bicycle, more deviation is
     * acceptable.
     */
    /** Lateral alignment margin for longitudinally connected Lanes. */
    static final Length.Rel LATERAL_MARGIN = new Length.Rel(0.5, METER);

    /**
     * The next lane(s) are cached, as it is too expensive to make the calculation every time. There are several possibilities:
     * returning an empty set when the lane stops and there is no longitudinal transfer method to a next lane. Returning a set
     * with just one lane if the lateral position of the next lane matches the lateral position of this lane (based on an
     * overlap of the lateral positions of the two joining lanes of more than a certain percentage). Multiple lanes in case the
     * Node where the underlying Link for this Lane has multiple outgoing Links, and there are multiple lanes that match the
     * lateral position of this lane.<br>
     * The next lanes can differ per GTU type. For instance, a lane where cars and buses are allowed can have a next lane where
     * only buses are allowed, forcing the cars to leave that lane.
     * @param gtuType the GTU type for which we return the next lanes.
     * @return set of Lanes following this lane for the given GTU type.
     */
    public final Set<Lane> nextLanes(final GTUType gtuType)
    {
        if (this.nextLanes == null)
        {
            this.nextLanes = new LinkedHashMap<>(1);
        }
        if (!this.nextLanes.containsKey(gtuType))
        {
            Set<Lane> laneSet = new LinkedHashSet<>(1);
            this.nextLanes.put(gtuType, laneSet);
            // Construct (and cache) the result.
            for (Link link : getParentLink().getEndNode().getLinksOut())
            {
                if (link instanceof CrossSectionLink)
                {
                    for (CrossSectionElement cse : ((CrossSectionLink) link).getCrossSectionElementList())
                    {
                        if (cse instanceof Lane && laterallyCloseEnough(this, cse, LATERAL_MARGIN))
                        {
                            laneSet.add((Lane) cse);
                        }
                    }
                }
            }
        }
        return this.nextLanes.get(gtuType);
    }

    /**
     * The previous lane(s) are cached, as it is too expensive to make the calculation every time. There are several
     * possibilities: returning an empty set when the lane starts and there is no longitudinal transfer method from a previous
     * lane. Returning a set with just one lane if the lateral position of the previous lane matches the lateral position of
     * this lane (based on an overlap of the lateral positions of the two joining lanes of more than a certain percentage).
     * Multiple lanes in case the Node where the underlying Link for this Lane has multiple incoming Links, and there are
     * multiple lanes that match the lateral position of this lane. <br>
     * The previous lanes can differ per GTU type. For instance, a lane where cars and buses are allowed can be preceded by a
     * lane where only buses are allowed.
     * @param gtuType the GTU type for which we return the next lanes.
     * @return set of Lanes following this lane for the given GTU type.
     */
    public final Set<Lane> prevLanes(final GTUType gtuType)
    {
        if (this.prevLanes == null)
        {
            this.prevLanes = new LinkedHashMap<>(1);
        }
        if (!this.prevLanes.containsKey(gtuType))
        {
            Set<Lane> laneSet = new LinkedHashSet<>(1);
            this.prevLanes.put(gtuType, laneSet);
            // Construct (and cache) the result.
            for (Link link : getParentLink().getStartNode().getLinksIn())
            {
                if (link instanceof CrossSectionLink)
                {
                    for (CrossSectionElement cse : ((CrossSectionLink) link).getCrossSectionElementList())
                    {
                        if (cse instanceof Lane && laterallyCloseEnough(cse, this, LATERAL_MARGIN))
                        {
                            laneSet.add((Lane) cse);
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
     * we drive FORWARD and look for a lane on the LEFT, and there is a lane but the Directionality of that lane is not FORWARD
     * or BOTH, it will not be included.<br>
     * A lane is called adjacent to another lane if the lateral edges are not more than a delta distance apart. This means that
     * a lane that <i>overlaps</i> with another lane is <b>not</b> returned as an adjacent lane. <br>
     * <b>Note:</b> LEFT is seen as a positive lateral direction, RIGHT as a negative lateral direction. <br>
     * @param lateralDirection LEFT or RIGHT.
     * @param gtuType the type of GTU for which to return the adjacent lanes.
     * @return the set of lanes that are accessible, or null if there is no lane that is accessible with a matching driving
     *         direction.
     */
    public final Set<Lane> accessibleAdjacentLanes(final LateralDirectionality lateralDirection, final GTUType gtuType)
    {
        Set<Lane> candidates = new LinkedHashSet<>(1);
        for (Lane lane : neighbors(lateralDirection, gtuType))
        {
            if (lane.getDirectionality(gtuType).equals(LongitudinalDirectionality.BOTH)
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
     */
    public final void sample(final AbstractLaneBasedGTU gtu) throws NetworkException
    {
        // FIXME: Hack; do not sample dummy vehicle at lane drop
        if (gtu.getNextEvaluationTime().getSI() == Double.MAX_VALUE)
        {
            return;
        }
        for (LaneBasedGTUSampler sampler : this.samplers)
        {
            sampler.addData(gtu, this);
        }
    }

    /**
     * @param gtuType the GTU type to provide the speed limit for
     * @return speedLimit.
     */
    public final Speed.Abs getSpeedLimit(final GTUType gtuType)
    {
        if (this.speedLimitMap.containsKey(gtuType))
        {
            return this.speedLimitMap.get(gtuType);
        }
        if (this.speedLimitMap.containsKey(GTUType.ALL))
        {
            return this.speedLimitMap.get(GTUType.ALL);
        }
        return new Speed.Abs(0.0, METER_PER_SECOND); // XXX is this what we want, or should we throw exception?
    }

    /**
     * @param gtuType the GTU type to provide the speed limit for
     * @param speedLimit the speed limit for this gtu type
     */
    public final void setSpeedLimit(final GTUType gtuType, final Speed.Abs speedLimit)
    {
        this.speedLimitMap.put(gtuType, speedLimit);
    }

    /**
     * @return laneType.
     */
    public final LaneType getLaneType()
    {
        return this.laneType;
    }

    /**
     * @param gtuType the GTU type to provide the directionality for
     * @return directionality.
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
        return LongitudinalDirectionality.NONE;
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

    /** {@inheritDoc} */
    public final String toString()
    {
        CrossSectionLink link = getParentLink();
        return String.format("Lane %s of %s", getId(), link.toString());
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
    @SuppressWarnings({"checkstyle:designforextension", "checkstyle:needbraces"})
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
     * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * $LastChangedDate$, @version $Revision$, by $Author$,
     * initial version Aug 28, 2015 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    private class GTUTypeSensor implements Serializable
    {
        /** */
        private static final long serialVersionUID = 20150828L;

        /** the GTU type that triggers this sensor; GTUType.ALL if all GTU types trigger the sensor. */
        private final GTUType gtuType;

        /** the sensor that is triggers by the gtuType. */
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
    }
}
