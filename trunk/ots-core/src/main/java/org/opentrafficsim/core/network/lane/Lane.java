package org.opentrafficsim.core.network.lane;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import nl.tudelft.simulation.dsol.SimRuntimeException;

import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.lane.AbstractLaneBasedGTU;
import org.opentrafficsim.core.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.unit.FrequencyUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.graphs.LaneBasedGTUSampler;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version ug 19, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @param <NODEID> the ID type of the Node, e.g., String.
 * @param <LINKID> the ID type of the Link, e.g., String.
 */
public class Lane<LINKID, NODEID> extends CrossSectionElement<LINKID, NODEID>
{
    /** type of lane to deduce compatibility with GTU types. */
    private final LaneType<?> laneType;

    /** in direction of geometry, reverse, or both. */
    private final LongitudinalDirectionality directionality;

    /** Lane capacity in vehicles per time unit. This is a mutable property (e.g., blockage); thus not final. */
    private DoubleScalar.Abs<FrequencyUnit> capacity;

    /** the speed limit of this lane. */
    private DoubleScalar.Abs<SpeedUnit> speedLimit;

    /** Sensors on the lane to trigger behavior of the GTU, sorted by longitudinal position. */
    private final SortedMap<Double, List<Sensor>> sensors = new TreeMap<>();

    /** GTUs ordered by increasing longitudinal position. */
    private final List<LaneBasedGTU<?>> gtuList = new ArrayList<LaneBasedGTU<?>>();

    /** Adjacent left lanes that some GTU types can change onto. */
    private Set<Lane<LINKID, NODEID>> leftNeighbors = new LinkedHashSet<Lane<LINKID, NODEID>>(1);

    /** Adjacent right lanes that some GTU types can change onto. */
    private Set<Lane<LINKID, NODEID>> rightNeighbors = new LinkedHashSet<Lane<LINKID, NODEID>>(1);

    /**
     * Next lane(s) following this lane. Initially null so we can calculate and cache the first time the method is called.
     */
    private Set<Lane<LINKID, NODEID>> nextLanes = null;

    /**
     * Next lane(s) following this lane. Initially null so we can calculate and cache the first time the method is called.
     */
    private Set<Lane<LINKID, NODEID>> prevLanes = null;

    // TODO write interface for samplers
    /** List of graphs that want to sample GTUs on this Lane. */
    private ArrayList<LaneBasedGTUSampler> samplers = new ArrayList<LaneBasedGTUSampler>();

    /**
     * @param parentLink Cross Section Link to which the element belongs.
     * @param lateralOffsetAtStart DoubleScalar.Rel&lt;LengthUnit&gt;; the lateral offset of the design line of the new
     *            CrossSectionLink with respect to the design line of the parent Link at the start of the parent Link
     * @param lateralOffsetAtEnd DoubleScalar.Rel&lt;LengthUnit&gt;; the lateral offset of the design line of the new
     *            CrossSectionLink with respect to the design line of the parent Link at the end of the parent Link
     * @param beginWidth DoubleScalar.Rel&lt;LengthUnit&gt;; start width, positioned <i>symmetrically around</i> the design line
     * @param endWidth DoubleScalar.Rel&lt;LengthUnit&gt;; end width, positioned <i>symmetrically around</i> the design line
     * @param laneType type of lane to deduce compatibility with GTU types
     * @param directionality in direction of geometry, reverse, or both
     * @param capacity Lane capacity in vehicles per time unit. This is a mutable property (e.g., blockage)
     * @param speedLimit speed limit on this lane
     * @throws OTSGeometryException when creation of the center line or contour geometry fails
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public Lane(final CrossSectionLink<LINKID, NODEID> parentLink, final DoubleScalar.Rel<LengthUnit> lateralOffsetAtStart,
        final DoubleScalar.Rel<LengthUnit> lateralOffsetAtEnd, final DoubleScalar.Rel<LengthUnit> beginWidth,
        final DoubleScalar.Rel<LengthUnit> endWidth, final LaneType<?> laneType,
        final LongitudinalDirectionality directionality, final DoubleScalar.Abs<FrequencyUnit> capacity,
        final DoubleScalar.Abs<SpeedUnit> speedLimit) throws OTSGeometryException
    {
        super(parentLink, lateralOffsetAtStart, lateralOffsetAtEnd, beginWidth, endWidth);
        this.laneType = laneType;
        this.directionality = directionality;
        this.capacity = capacity;
        this.speedLimit = speedLimit;
        // TODO Take care of directionality.
    }

    /**
     * Retrieve one of the sets of neighboring Lanes.
     * @param direction LateralDirectionality; either LEFT or RIGHT
     * @return Set&lt;Lane&gt;; the indicated set of neighboring Lanes
     */
    private Set<Lane<LINKID, NODEID>> neighbors(final LateralDirectionality direction)
    {
        return direction == LateralDirectionality.LEFT ? this.leftNeighbors : this.rightNeighbors;
    }

    /**
     * Indicate that a Lane is adjacent to this Lane.
     * @param adjacentLane Lane; the adjacent Lane
     * @param direction LateralDirectionality; the direction in which the Lane is adjacent to this Lane
     */
    public final void addAccessibleAdjacentLane(final Lane<LINKID, NODEID> adjacentLane,
        final LateralDirectionality direction)
    {
        neighbors(direction).add(adjacentLane);
    }

    /**
     * Indicate that a Lane is no longer adjacent to this Lane (may be useful for lanes that are sometimes closed, e.g. tidal
     * flow lanes).
     * @param adjacentLane Lane; the adjacent Lane that must be unregistered
     * @param direction LateralDirectionality; the direction in which the Lane was adjacent to this Lane
     * @throws NetworkException when the adjacentLane was not registered as adjacent in the indicated direction
     */
    public final void removeAccessibleAdjacentLane(final Lane<LINKID, NODEID> adjacentLane,
        final LateralDirectionality direction) throws NetworkException
    {
        Set<Lane<LINKID, NODEID>> neighbors = neighbors(direction);
        if (!neighbors.contains(adjacentLane))
        {
            throw new NetworkException("Lane " + adjacentLane + " is not among the " + direction + " neighbors of this Lane");
        }
        neighbors.remove(adjacentLane);
    }

    /**
     * Insert the sensor at the right place in the sensor list of this lane.
     * @param sensor the sensor to add
     * @throws NetworkException when the position of the sensor is beyond (or before) the range of this Lane
     */
    public final void addSensor(final Sensor sensor) throws NetworkException
    {
        double position = sensor.getLongitudinalPositionSI();
        if (position < 0 || position > getLength().getSI())
        {
            throw new NetworkException("Illegal position for sensor " + position + " valid range is 0.."
                + getLength().getSI());
        }
        List<Sensor> sensorList = this.sensors.get(position);
        if (null == sensorList)
        {
            sensorList = new ArrayList<Sensor>(1);
            this.sensors.put(position, sensorList);
        }
        sensorList.add(sensor);
    }

    /**
     * Remove a sensor from the sensor list of this lane.
     * @param sensor the sensor to remove.
     * @throws NetworkException when the sensor was not found on this Lane
     */
    public final void removeSensor(final Sensor sensor) throws NetworkException
    {
        List<Sensor> sensorList = this.sensors.get(sensor.getLongitudinalPosition().getSI());
        if (null == sensorList)
        {
            throw new NetworkException("No sensor at " + sensor.getLongitudinalPositionSI());
        }
        sensorList.remove(sensor);
        if (sensorList.size() == 0)
        {
            this.sensors.remove(sensor.getLongitudinalPosition().getSI());
        }
    }

    /**
     * Retrieve the list of Sensors of this Lane in the specified distance range.
     * @param minimumPosition DoubleScalar.Rel&lt;LengthUnit&gt;; the minimum distance on the Lane
     * @param maximumPosition DoubleScalar.Rel&lt;LengthUnit&gt;; the maximum distance on the Lane
     * @return List&lt;Sensor&gt;; list of the sensor in the specified range
     */
    public final List<Sensor> getSensors(final DoubleScalar.Rel<LengthUnit> minimumPosition,
        final DoubleScalar.Rel<LengthUnit> maximumPosition)
    {
        ArrayList<Sensor> result = new ArrayList<Sensor>();
        for (List<Sensor> sensorList : this.sensors.subMap(minimumPosition.getSI(), maximumPosition.getSI()).values())
        {
            result.addAll(sensorList);
        }
        return result;
    }

    /**
     * @return sensors.
     */
    protected final SortedMap<Double, List<Sensor>> getSensors()
    {
        return this.sensors;
    }

    /**
     * Trigger the sensors for a certain time step; from now until the nextEvaluationTime of the GTU.
     * @param gtu the LaneBasedGTU for which to trigger the sensors.
     * @param referenceStartSI the SI distance of the GTU reference point on the lane at the current time
     * @param referenceMoveSI the SI distance traveled in the next time step.
     * @throws RemoteException when simulation time cannot be retrieved.
     * @throws NetworkException when GTU not on this lane.
     * @throws SimRuntimeException when method cannot be scheduled.
     */
    public final void
        scheduleTriggers(final LaneBasedGTU<?> gtu, final double referenceStartSI, final double referenceMoveSI)
            throws RemoteException, NetworkException, SimRuntimeException
    {
        if (toString().contains("endLink"))
        {
            System.out.println("gtu=" + gtu.toString() + " - start=" + referenceStartSI + ", move=" + referenceMoveSI);
        }
        for (List<Sensor> sensorList : this.sensors.values())
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
                        // FIXME: PK does not understand the use of Math.max here.
                        /*-
                        double d =
                            Math.max(0.0, sensor.getLongitudinalPositionSI() - referenceStartSI
                                - relativePosition.getDx().getSI());
                         */
                        double d = sensor.getLongitudinalPositionSI() - referenceStartSI - relativePosition.getDx().getSI();
                        if (d < 0)
                        {
                            throw new NetworkException("scheduleTriggers for gtu: " + gtu + ", d<0 d=" + d);
                        }
                        DoubleScalar.Abs<TimeUnit> triggerTime =
                            gtu.timeAtDistance(new DoubleScalar.Rel<LengthUnit>(d, LengthUnit.METER));
                        if (triggerTime.gt(gtu.getNextEvaluationTime()))
                        {
                            System.out.println("Time=" + gtu.getSimulator().getSimulatorTime().toString()
                                + " - Scheduling trigger at " + triggerTime + " > " + gtu.getNextEvaluationTime()
                                + " (nextEvalTime) for sensor " + sensor + " , gtu " + gtu);
                            // gtu.timeAtDistance(new DoubleScalar.Rel<LengthUnit>(-d, LengthUnit.METER));
                            // System.exit(-1);
                        }
                        System.out.println("Time=" + gtu.getSimulator().getSimulatorTime().toString()
                            + " - Scheduling trigger at " + triggerTime + " for sensor " + sensor + " , gtu " + gtu);
                        gtu.getSimulator().scheduleEventAbs(triggerTime, this, sensor, "trigger", new Object[] {gtu});
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
    public final DoubleScalar.Rel<LengthUnit> position(final double fraction)
    {
        return new DoubleScalar.Rel<LengthUnit>(this.getLength().getInUnit() * fraction, this.getLength().getUnit());
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
    public final double fraction(final DoubleScalar.Rel<LengthUnit> position)
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
     * Add a LaneBasedGTU&lt;?&gt; to the list of this Lane.
     * @param gtu LaneBasedGTU&lt;?&gt;; the GTU to add
     * @param fractionalPosition double; the fractional position that the newly added GTU will have on this Lane
     * @return int; the rank that the newly added GTU has on this Lane (should be 0, except when the GTU enters this Lane due to
     *         a lane change operation)
     * @throws RemoteException on communication failure
     * @throws NetworkException when the fractionalPosition is outside the range 0..1, or the GTU is already registered on this
     *             Lane
     */
    public final int addGTU(final LaneBasedGTU<?> gtu, final double fractionalPosition) throws RemoteException,
        NetworkException
    {
        // figure out the rank for the new GTU
        int index;
        for (index = 0; index < this.gtuList.size(); index++)
        {
            LaneBasedGTU<?> otherGTU = this.gtuList.get(index);
            if (gtu == otherGTU)
            {
                System.err.println("GTU " + gtu + " already registered on Lane " + this + " [registered lanes: "
                    + gtu.positions(gtu.getFront()).keySet() + "] locations: " + gtu.positions(gtu.getFront()).values()
                    + " time: " + gtu.getSimulator().getSimulatorTime().get());
                new Exception().printStackTrace();
                return index;
                /*-
                throw new NetworkException("GTU " + gtu + " already registered on Lane " + this
                        + " [registered lanes: " + gtu.positions(gtu.getFront()).keySet() + "] locations: "
                        + gtu.positions(gtu.getFront()).values() + " time: "
                        + gtu.getSimulator().getSimulatorTime().get());
                 */
            }
            try
            {
                if (otherGTU.fractionalPosition(this, otherGTU.getFront()) >= fractionalPosition)
                {
                    break;
                }
            }
            catch (NetworkException exception)
            {
                // Should never happen; implies that there is a GTU on this Lane that does not think it is on this Lane
                exception.printStackTrace();
            }
        }
        this.gtuList.add(index, gtu);
        // System.out.println("Added gtu " + gtu.getId() + " to lane " + this + " at index " + index);
        return index;
    }

    /**
     * Add a LaneBasedGTU&lt;?&gt; to the list of this Lane.
     * @param gtu LaneBasedGTU&lt;?&gt;; the GTU to add
     * @param longitudinalPosition DoubleScalar.Rel&lt;LengthUnit&gt;; the longitudinal position that the newly added GTU will
     *            have on this Lane
     * @return int; the rank that the newly added GTU has on this Lane (should be 0, except when the GTU enters this Lane due to
     *         a lane change operation)
     * @throws RemoteException on communication failure
     * @throws NetworkException when longitudinalPosition is negative or exceeds the length of this Lane
     */
    public final int addGTU(final LaneBasedGTU<?> gtu, final DoubleScalar.Rel<LengthUnit> longitudinalPosition)
        throws RemoteException, NetworkException
    {
        return addGTU(gtu, longitudinalPosition.getSI() / getLength().getSI());
    }

    /**
     * Remove a GTU from the GTU list of this lane.
     * @param gtu the GTU to remove.
     */
    public final void removeGTU(final LaneBasedGTU<?> gtu)
    {
        this.gtuList.remove(gtu);
    }

    /**
     * @param position the front position after which the relative position of a GTU will be searched.
     * @param relativePosition the relative position of the GTU we are looking for.
     * @param when the time for which to evaluate the positions.
     * @return the first GTU after a position on this lane, or null if no GTU could be found.
     * @throws NetworkException when there is a problem with the position of the GTUs on the lane.
     * @throws RemoteException on communications failure
     */
    public final LaneBasedGTU<?> getGtuAfter(final DoubleScalar.Rel<LengthUnit> position,
        final RelativePosition.TYPE relativePosition, final DoubleScalar.Abs<TimeUnit> when) throws NetworkException,
        RemoteException
    {
        for (LaneBasedGTU<?> gtu : this.gtuList)
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
     * @throws RemoteException on communications failure
     */
    public final LaneBasedGTU<?> getGtuBefore(final DoubleScalar.Rel<LengthUnit> position,
        final RelativePosition.TYPE relativePosition, final DoubleScalar.Abs<TimeUnit> when) throws NetworkException,
        RemoteException
    {
        for (int i = this.gtuList.size() - 1; i >= 0; i--)
        {
            LaneBasedGTU<?> gtu = this.gtuList.get(i);
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
    private boolean laterallyCloseEnough(final CrossSectionElement<LINKID, NODEID> incomingCSE,
        final CrossSectionElement<LINKID, NODEID> outgoingCSE, final DoubleScalar.Rel<LengthUnit> margin)
    {
        return Math.abs(DoubleScalar.minus(incomingCSE.getLateralCenterPosition(1), outgoingCSE.getLateralCenterPosition(0))
            .getSI()) <= margin.getSI();
    }

    /*
     * TODO only center position? Or also width? What is a good cutoff? Base on average width of the GTU type that can drive on
     * this Lane? E.g., for a Tram or Train, a 5 cm deviation is a problem; for a Car or a Bicycle, more deviation is
     * acceptable.
     */
    /** Lateral alignment margin for longitudinally connected Lanes. */
    static final DoubleScalar.Rel<LengthUnit> LATERAL_MARGIN = new DoubleScalar.Rel<LengthUnit>(0.5, LengthUnit.METER);

    /**
     * The next lane(s) are cached, as it is too expensive to make the calculation every time. There are several possibilities:
     * returning an empty set when the lane stops and there is no longitudinal transfer method to a next lane. Returning a set
     * with just one lane if the lateral position of the next lane matches the lateral position of this lane (based on an
     * overlap of the lateral positions of the two joining lanes of more than a certain percentage). Multiple lanes in case the
     * Node where the underlying Link for this Lane has multiple outgoing Links, and there are multiple lanes that match the
     * lateral position of this lane.
     * @return set of Lanes following this lane.
     */
    public final Set<Lane<LINKID, NODEID>> nextLanes()
    {
        if (this.nextLanes == null)
        {
            // Construct (and cache) the result.
            this.nextLanes = new LinkedHashSet<Lane<LINKID, NODEID>>(1);
            for (Link<?, NODEID> link : getParentLink().getEndNode().getLinksOut())
            {
                if (link instanceof CrossSectionLink<?, ?>)
                {
                    for (CrossSectionElement<LINKID, NODEID> cse : ((CrossSectionLink<LINKID, NODEID>) link)
                        .getCrossSectionElementList())
                    {
                        if (cse instanceof Lane && laterallyCloseEnough(this, cse, LATERAL_MARGIN))
                        {
                            this.nextLanes.add((Lane<LINKID, NODEID>) cse);
                        }
                    }
                }
            }
        }
        return this.nextLanes;
    }

    /**
     * The previous lane(s) are cached, as it is too expensive to make the calculation every time. There are several
     * possibilities: returning an empty set when the lane starts and there is no longitudinal transfer method from a previous
     * lane. Returning a set with just one lane if the lateral position of the previous lane matches the lateral position of
     * this lane (based on an overlap of the lateral positions of the two joining lanes of more than a certain percentage).
     * Multiple lanes in case the Node where the underlying Link for this Lane has multiple incoming Links, and there are
     * multiple lanes that match the lateral position of this lane.
     * @return set of Lanes preceding this lane.
     */
    public final Set<Lane<LINKID, NODEID>> prevLanes()
    {
        if (this.prevLanes == null)
        {
            // Construct (and cache) the result.
            this.prevLanes = new LinkedHashSet<Lane<LINKID, NODEID>>(1);
            for (Link<?, NODEID> link : getParentLink().getStartNode().getLinksIn())
            {
                if (link instanceof CrossSectionLink<?, ?>)
                {
                    for (CrossSectionElement<LINKID, NODEID> cse : ((CrossSectionLink<LINKID, NODEID>) link)
                        .getCrossSectionElementList())
                    {
                        if (cse instanceof Lane && laterallyCloseEnough(cse, this, LATERAL_MARGIN))
                        {
                            this.prevLanes.add((Lane<LINKID, NODEID>) cse);
                        }
                    }
                }
            }
        }
        return this.prevLanes;
    }

    /**
     * Determine the set of lanes to the left or to the right of this lane, which are accessible from this lane, or an empty set
     * if no lane could be found. The method takes the LongitidinalDirectionality of the lane into account. In other words, if
     * we drive FORWARD and look for a lane on the LEFT, and there is a lane but the Directionality of that lane is not FORWARD
     * or BOTH, it will not be included.<br>
     * A lane is called adjacent to another lane if the lateral edges are not more than a delta distance apart. This means that
     * a lane that <i>overlaps</i> with another lane is <b>not</b> returned as an adjacent lane. <br>
     * The algorithm also looks for RoadMarkerAcross elements between the lanes to determine the lateral permeability for a GTU.
     * A RoadMarkerAcross is seen as being between two lanes if its center line is not more than delta distance from the
     * relevant lateral edges of the two adjacent lanes. <br>
     * <b>Note:</b> LEFT is seen as a positive lateral direction, RIGHT as a negative lateral direction. <br>
     * @param lateralDirection LEFT or RIGHT.
     * @param gtuType the type of GTU for which this an adjacent lane.
     * @return the set of lanes that are accessible, or null if there is no lane that is accessible with a matching driving
     *         direction.
     */
    public final Set<Lane<?, ?>> accessibleAdjacentLanes(final LateralDirectionality lateralDirection,
        final GTUType<?> gtuType)
    {
        Set<Lane<?, ?>> candidates = new LinkedHashSet<>();
        for (Lane<LINKID, NODEID> l : neighbors(lateralDirection))
        {
            if (l.getLaneType().isCompatible(gtuType)
                && (l.getDirectionality().equals(LongitudinalDirectionality.BOTH) || l.getDirectionality().equals(
                    this.getDirectionality())))
            {
                candidates.add(l);
            }
        }
        return candidates;
    }

    /**
     * Determine whether there is a lane to the left or to the right of this lane, which is accessible from this lane, or null
     * if no lane could be found. The method takes the LongitidinalDirectionality of the lane into account. In other words, if
     * we drive FORWARD and look for a lane on the LEFT, and there is a lane but the Directionality of that lane is not FORWARD
     * or BOTH, null will be returned.<br>
     * A lane is called adjacent to another lane if the lateral edges are not more than a delta distance apart. This means that
     * a lane that <i>overlaps</i> with another lane is <b>not</b> returned as an adjacent lane. <br>
     * The algorithm also looks for RoadMarkerAcross elements between the lanes to determine the lateral permeability for a GTU.
     * A RoadMarkerAcross is seen as being between two lanes if its center line is not more than delta distance from the
     * relevant lateral edges of the two adjacent lanes. <br>
     * When there are multiple lanes that are adjacent, which could e.g. be the case if an overlapping tram lane and a car lane
     * are adjacent to the current lane, the widest lane that best matches the GTU accessibility of the provided GTUType is
     * returned. <br>
     * <b>Note:</b> LEFT is seen as a positive lateral direction, RIGHT as a negative lateral direction. <br>
     * @param lateralDirection LEFT or RIGHT.
     * @param longitudinalPosition DoubleScalar.Rel&lt;LengthUnit&gt;; the position of the GTU along this Lane
     * @param gtuType the type of GTU for which this an adjacent lane.
     * @return the lane if it is accessible, or null if there is no lane, it is not accessible, or the driving direction does
     *         not match.
     */
    public final Lane<?, ?> bestAccessibleAdjacentLane(final LateralDirectionality lateralDirection,
        final DoubleScalar.Rel<LengthUnit> longitudinalPosition, final GTUType<?> gtuType)
    {
        Set<Lane<?, ?>> candidates = accessibleAdjacentLanes(lateralDirection, gtuType);

        if (candidates.isEmpty())
        {
            return null; // There is no adjacent Lane that this GTU type can cross into
        }
        if (candidates.size() == 1)
        {
            return candidates.iterator().next(); // There is exactly one adjacent Lane that this GTU type can cross into
        }
        // There are several candidates; find the one that is widest at the beginning.
        Lane<?, ?> bestLane = null;
        double widthM = -1.0;
        for (Lane<?, ?> lane : candidates)
        {
            if (lane.getWidth(longitudinalPosition).getSI() > widthM)
            {
                widthM = lane.getWidth(longitudinalPosition).getSI();
                bestLane = lane;
            }
        }
        return bestLane;
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
     * @param gtu AbstractLaneBasedGTU&lt;?&gt;; the GTU to sample
     * @throws NetworkException on network inconsistency
     * @throws RemoteException on communications failure
     */
    public final void sample(final AbstractLaneBasedGTU<?> gtu) throws RemoteException, NetworkException
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
     * @return capacity.
     */
    public final DoubleScalar.Abs<FrequencyUnit> getCapacity()
    {
        return this.capacity;
    }

    /**
     * @param capacity set capacity.
     */
    public final void setCapacity(final DoubleScalar.Abs<FrequencyUnit> capacity)
    {
        this.capacity = capacity;
    }

    /**
     * @return speedLimit.
     */
    public final DoubleScalar.Abs<SpeedUnit> getSpeedLimit()
    {
        return this.speedLimit;
    }

    /**
     * @return laneType.
     */
    public final LaneType<?> getLaneType()
    {
        return this.laneType;
    }

    /**
     * @return directionality.
     */
    public final LongitudinalDirectionality getDirectionality()
    {
        return this.directionality;
    }

    /**
     * @return gtuList.
     */
    public final List<LaneBasedGTU<?>> getGtuList()
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
        CrossSectionLink<LINKID, NODEID> link = getParentLink();
        // FIXME indexOf may not be the correct way to determine the rank of a Lane (counts stripes as well)
        return String.format("Lane %d of %s", link.getCrossSectionElementList().indexOf(this), link.toString());
    }

}
