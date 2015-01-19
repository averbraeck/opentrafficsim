package org.opentrafficsim.core.network.lane;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import nl.tudelft.simulation.dsol.SimRuntimeException;

import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.LaneBasedGTU;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.unit.FrequencyUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Aug 19, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public class Lane extends CrossSectionElement
{
    /** type of lane to deduce compatibility with GTU types. */
    private final LaneType<?> laneType;

    /** in direction of geometry, reverse, or both. */
    private final LongitudinalDirectionality directionality;

    /** Lane capacity in vehicles per time unit. This is a mutable property (e.g., blockage). */
    private DoubleScalar.Abs<FrequencyUnit> capacity;

    /** Sensors on the lane to trigger behavior of the GTU, sorted by longitudinal position. */
    private SortedMap<Double, Sensor> sensors = new TreeMap<>();

    /** GTUs on the lane at the last evaluation step of the traffic model. */
    private final List<LaneBasedGTU<?>> gtuList;

    /**
     * @param parentLink Cross Section Link to which the element belongs.
     * @param lateralStartPosition the lateral start position compared to the linear geometry of the Cross Section Link.
     * @param beginWidth start width, positioned <i>symmetrically around</i> the lateral start position.
     * @param endWidth end width, positioned <i>symmetrically around</i> the lateral end position.
     * @param laneType type of lane to deduce compatibility with GTU types.
     * @param directionality in direction of geometry, reverse, or both.
     * @param capacity Lane capacity in vehicles per time unit. This is a mutable property (e.g., blockage).
     */
    public Lane(final CrossSectionLink<?, ?> parentLink, final DoubleScalar.Rel<LengthUnit> lateralStartPosition,
            final DoubleScalar.Rel<LengthUnit> beginWidth, final DoubleScalar.Rel<LengthUnit> endWidth,
            final LaneType<?> laneType, final LongitudinalDirectionality directionality,
            final DoubleScalar.Abs<FrequencyUnit> capacity)
    {
        super(parentLink, lateralStartPosition, beginWidth, endWidth);
        this.laneType = laneType;
        this.directionality = directionality;
        this.capacity = capacity;
        this.gtuList = new ArrayList<LaneBasedGTU<?>>();
        // TODO Take care of directionality.
        addSensor(new SensorLaneStart(this));
        addSensor(new SensorLaneEnd(this));
    }

    /**
     * Insert the sensor at the right place in the sensor list of this lane.
     * @param sensor the sensor to add.
     */
    public final void addSensor(final Sensor sensor)
    {
        // XXX PK asks: Could this place a SensorLaneEnd BEYOND the end of the Lane? Would that be bad?
        // If true, is there another work-around? Like, arrange sensors at the same position in a linked list?
        double position = sensor.getLongitudinalPositionSI();
        while (this.sensors.containsKey(position))
        {
            position += Math.ulp(position);
        }
        this.sensors.put(position, sensor);
    }

    /**
     * Remove a sensor from the sensor list of this lane.
     * @param sensor the sensor to remove.
     */
    public final void removeSensor(final Sensor sensor)
    {
        this.sensors.remove(sensor);
    }

    /**
     * Retrieve the list of Sensors of this Lane in the specified distance range.
     * @param minimumPosition DoubleScalar.Rel&lt;LengthUnit&gt;; the minimum distance on the Lane
     * @param maximumPosition DoubleScalar.Rel&lt;LengthUnit&gt;; the maximum distance on the Lane
     * @return List&lt;Sensor&gt;; list of the sensor in the specified range
     */
    public List<Sensor> getSensors(DoubleScalar.Rel<LengthUnit> minimumPosition,
            DoubleScalar.Rel<LengthUnit> maximumPosition)
    {
        return new ArrayList<Sensor>(this.sensors.subMap(minimumPosition.getSI(), maximumPosition.getSI()).values());
    }

    /**
     * Trigger the sensors for a certain timestep; from now until the nextEvaluationTime of the GTU.
     * @param gtu the LaneBasedGTU for which to trigger the sensors.
     * @throws RemoteException when simulation time cannot be retrieved.
     * @throws NetworkException when GTU not on this lane.
     * @throws SimRuntimeException when method cannot be scheduled.
     */
    public final void scheduleTriggers(final LaneBasedGTU<?> gtu) throws RemoteException, NetworkException,
            SimRuntimeException
    {
        double mStart = gtu.position(this, gtu.getFront()).getSI();
        double mEnd = gtu.position(this, gtu.getFront(), gtu.getNextEvaluationTime()).getSI();
        //System.out.println("mstart: " + mStart + ", mEnd: " + mEnd);
        List<Sensor> triggerSensors = new ArrayList<Sensor>(this.sensors.subMap(mStart, mEnd).values());
        for (Sensor sensor : triggerSensors)
        {
            // the exact time of triggering is based on the distance between the current position of the GTU and the
            // location of the sensor.
            double d = Math.max(0.0, sensor.getLongitudinalPositionSI() - mStart);
            // how much time to travel d meters? 0.5*a*t^2 + v0*t - d = 0
            // => t = (-v0 +/- sqrt(v0^2 - 4*0.5*a*(-d))) / 2*0.5*a = (-v0 +/- sqrt(v0^2 + 2*a*d)) / a
            double v0 = gtu.getLongitudinalVelocity().getSI();
            double a = gtu.getAcceleration().getSI();
            double sq = Math.sqrt(v0 * v0 + 2.0 * a * d);
            double t1 = (-v0 + sq) / a;
            double t2 = (-v0 - sq) / a;
            // PK: Wrong: double t = t1 < 0 ? t2 : t1 < t2 ? t1 : t2;
            // Figure out which one comes first (but not in the past)
            double now = gtu.getSimulator().getSimulatorTime().get().getSI();
            double t;
            if (t1 < now)
            {
                if (t2 < now)
                {
                    System.err.println("Oops, both possible trigger times are in the past");
                    t = Double.NEGATIVE_INFINITY; // This will cause an exception when scheduling the event
                }
                else
                {
                    t = t2;
                }
            }
            else
            {
                if (t2 < now)
                {
                    t = t1;
                }
                // Both solutions are > now; take the earliest
                else if (t1 < t2)
                {
                    t = t1;
                }
                else
                {
                    t = t2;
                }
            }
            // System.out.println(String.format("t1=%.3fs, t2=%.3fs, now=%.3fs, scheduling %.3fs", t1, t2, gtu
            // .getSimulator().getSimulatorTime().get().getSI(), t));
            gtu.getSimulator().scheduleEventRel(new DoubleScalar.Rel<TimeUnit>(t, TimeUnit.SECOND), this, sensor,
                    "trigger", new Object[]{gtu});
        }
    }

    /**
     * Add a GTU to the GTU list of this lane.
     * @param gtu the GTU to add.
     */
    public final void addGTU(final LaneBasedGTU<?> gtu)
    {
        addGTU(gtu, 0);
    }

    /**
     * Register the GTU on a lane on a given position. This method is used when a GTU is changing lanes and inserts
     * itself between / before / after other GTUs.
     * @param gtu the GTU to insert into the list.
     * @param index the index in the GTU list of the lane that this GTU will get.
     * @throws IndexOutOfBoundsException when index < 0 or > gtuList.size()-1.
     */
    @SuppressWarnings("checkstyle:redundantthrows")
    public final void addGTU(final LaneBasedGTU<?> gtu, final int index) throws IndexOutOfBoundsException
    {
        if (!this.gtuList.contains(gtu))
        {
            this.gtuList.add(index, gtu);
            // schedule the triggers for the remainder of this timestep

        }
    }

    /**
     * Register the GTU on a lane after another GTU. This method is used when a GTU is changing lanes and inserts itself
     * after another GTU.
     * @param gtu the GTU to insert into the list.
     * @param otherGTU the other GTU of the lane after which this GTU will be entered in the GTU list.
     * @throws NetworkException when other GTU not on the given lane.
     */
    public final void addGTUAfter(final LaneBasedGTU<?> gtu, final LaneBasedGTU<?> otherGTU) throws NetworkException
    {
        if (!this.gtuList.contains(otherGTU))
        {
            throw new NetworkException("Tried to insert GTU " + gtu + " after GTU " + otherGTU + " on lane " + this
                    + ", but 2nd GTU not on this lane.");
        }
        addGTU(gtu, this.gtuList.indexOf(otherGTU) + 1);
    }

    /**
     * Register the GTU on a lane before another GTU. This method is used when a GTU is changing lanes and inserts
     * itself before another GTU.
     * @param gtu the GTU to insert into the list.
     * @param otherGTU the other GTU of the lane before which this GTU will be entered in the GTU list.
     * @throws NetworkException when other GTU not on the given lane.
     */
    public final void addGTUBefore(final LaneBasedGTU<?> gtu, final LaneBasedGTU<?> otherGTU) throws NetworkException
    {
        if (!this.gtuList.contains(otherGTU))
        {
            throw new NetworkException("Tried to insert GTU " + gtu + " before GTU " + otherGTU + " on lane " + this
                    + ", but 2nd GTU not on this lane.");
        }
        addGTU(gtu, this.gtuList.indexOf(otherGTU));
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

    /**
     * TODO this should be done once in the constructor and then cached: too expensive to do every time...
     * @return set of Lanes following this lane. There are several possibilities: returning an empty set when the lane
     *         stops and there is no longitudinal transfer method to a next lane. Returning a set with just one lane if
     *         the lateral position of the next lane matches the lateral position of this lane (based on an overlap of
     *         the lateral positions of the two joining lanes of more than a certain percentage). Multiple lanes in case
     *         the Node where the underlying Link for this Lane has multiple outgoing Links, and there are multiple
     *         lanes that match the lateral position of this lane.
     */
    public final Set<Lane> nextLanes()
    {
        Set<Lane> lanes = new HashSet<Lane>();
        for (Link<?, ?> link : getParentLink().getEndNode().getLinksOut())
        {
            if (link instanceof CrossSectionLink<?, ?>)
            {
                for (CrossSectionElement cse : ((CrossSectionLink<?, ?>) link).getCrossSectionElementList())
                {
                    if (cse instanceof Lane)
                    {
                        /*
                         * TODO only center position? Or also width? What is a good cutoff? Base on average width of the
                         * GTU type that can drive on this Lane? E.g., for a Tram or Train, a 5 cm deviation is a
                         * problem; for a Car or a Bicycle, more deviation is acceptable.
                         */
                        if (Math.abs(cse.getLateralCenterPosition().getSI() - this.getLateralCenterPosition().getSI()) < 0.5)
                        {
                            lanes.add((Lane) cse);
                        }
                    }
                }
            }
        }
        return lanes;
    }

    /**
     * TODO this should be done once in the constructor and then cached: too expensive to do every time...
     * @return set of Lanes preceding this lane. There are several possibilities: returning an empty set when the lane
     *         starts and there is no longitudinal transfer method from a previous lane. Returning a set with just one
     *         lane if the lateral position of the previous lane matches the lateral position of this lane (based on an
     *         overlap of the lateral positions of the two joining lanes of more than a certain percentage). Multiple
     *         lanes in case the Node where the underlying Link for this Lane has multiple incoming Links, and there are
     *         multiple lanes that match the lateral position of this lane.
     */
    public final Set<Lane> prevLanes()
    {
        Set<Lane> lanes = new HashSet<Lane>();
        for (Link<?, ?> link : getParentLink().getEndNode().getLinksIn())
        {
            if (link instanceof CrossSectionLink<?, ?>)
            {
                for (CrossSectionElement cse : ((CrossSectionLink<?, ?>) link).getCrossSectionElementList())
                {
                    if (cse instanceof Lane)
                    {
                        /*
                         * TODO only center position? Or also width? What is a good cutoff? Base on average width of the
                         * GTU type that can drive on this Lane? E.g., for a Tram or Train, a 5 cm deviation is a
                         * problem; for a Car or a Bicycle, more deviation is acceptable.
                         */
                        if (Math.abs(cse.getLateralCenterPosition().getSI() - this.getLateralCenterPosition().getSI()) < 0.5)
                        {
                            lanes.add((Lane) cse);
                        }
                    }
                }
            }
        }
        return lanes;
    }

    /**
     * Determine whether there is a lane to the left or to the right of this lane, which is accessible from this lane,
     * or null if no lane could be found. The method takes the LongitidinalDirectionality of the lane into account. In
     * other words, if we drive FORWARD and look for a lane on the LEFT, and there is a lane but the Directionality of
     * that lane is not FORWARD or BOTH, null will be returned.<br>
     * A lane is called adjacent to another lane if the lateral edges are not more than a delta distance apart. This
     * means that a lane that <i>overlaps</i> with another lane is <b>not</b> returned as an adjacent lane. <br>
     * The algorithm also looks for RoadMarkerAcross elements between the lanes to determine the lateral permeability
     * for a GTU. A RoadMarkerAcross is seen as being between two lanes if its center line is not more than delta
     * distance from the relevant lateral edges of the two adjacent lanes. <br>
     * When there are multiple lanes that are adjacent, which could e.g. be the case if an overlapping tram lane and a
     * car lane are adjacent to the current lane, the widest lane that best matches the GTU accessibility of the
     * provided GTUType is returned. <br>
     * <b>Note:</b> LEFT is seen as a positive lateral direction, RIGHT as a negative lateral direction. <br>
     * TODO this should be done once in the constructor and then cached: too expensive to do every time...
     * @param lateralDirection LEFT or RIGHT.
     * @param gtuType the type of GTU for which this an adjacent lane.
     * @return the lane if it is accessible, or null if there is no lane, it is not accessible, or the driving direction
     *         does not match.
     */
    public final Lane accessibleAdjacentLane(final LateralDirectionality lateralDirection, final GTUType<?> gtuType)
    {
        final double delta = 0.1; // in meters
        Set<Lane> candidates = new HashSet<>();
        LateralDirectionality reverseDirection =
                lateralDirection == LateralDirectionality.LEFT ? LateralDirectionality.RIGHT
                        : LateralDirectionality.LEFT;
        for (CrossSectionElement cse : getParentLink().getCrossSectionElementList())
        {
            if (!(cse instanceof Lane))
            {
                continue; // Not a Lane (probably a road marker)
            }
            Lane cseLane = (Lane) cse;
            if (!cseLane.getLaneType().isCompatible(gtuType))
            {
                continue; // Not compatible with our GTU type
            }
            if (cseLane.getDirectionality().equals(LongitudinalDirectionality.BOTH)
                    || cseLane.getDirectionality().equals(this.getDirectionality()))
            {
                // The driving direction of our GTU is compatible with cseLane
                // System.out.println("this: " + toString());
                // System.out.println(" cse: " + cse.toString());
                if (this == cse)
                {
                    // System.out.println("Current lane is not adjacent to itself");
                    continue; // Current lane is not adjacent to itself
                }
                // System.out.println("checking for " + lateralDirection + " adjacency");
                if (Math.abs(DoubleScalar.minus(this.getLateralBeginPosition(lateralDirection),
                        cse.getLateralBeginPosition(reverseDirection)).getSI()) > delta)
                {
                    continue; // Not adjacent (within error margin delta)
                }
                // System.out.println("Lanes are adjacent!");
                // Lanes are adjacent. Can we cross?
                if (canCrossTo(gtuType, this.getLateralBeginPosition(lateralDirection).getSI(), delta, lateralDirection))
                {
                    candidates.add(cseLane); // Yes! Our GTU type can cross into this adjacent Lane
                }
            }
        }
        if (candidates.isEmpty())
        {
            return null; // There is no adjacent Lane that this GTU type can cross into
        }
        if (candidates.size() == 1)
        {
            return candidates.iterator().next(); // There is exactly one adjacent Lane that this GTU type can cross into
        }
        // There are several candidates; find the one that is widest at the beginning.
        Lane bestLane = null;
        double widthM = -1.0;
        for (Lane lane : candidates)
        {
            if (lane.getBeginWidth().getSI() > widthM)
            {
                widthM = lane.getBeginWidth().getSI();
                bestLane = lane;
            }
        }
        return bestLane;
    }

    /**
     * Find all longitudinal road markers along between the lanes and test if we can cross.
     * @param gtuType the GTU type for which we have to check permeability.
     * @param edgeOffset the offset
     * @param delta the margin to use to find the road markers.
     * @param lateralDirection direction to cross to.
     * @return whether we can cross or not.
     */
    private boolean canCrossTo(final GTUType<?> gtuType, final double edgeOffset, final double delta,
            final LateralDirectionality lateralDirection)
    {
        for (CrossSectionElement cse : getParentLink().getCrossSectionElementList())
        {
            if (cse instanceof RoadMarkerAlong)
            {
                RoadMarkerAlong marker = (RoadMarkerAlong) cse;
                if (Math.abs(marker.getLateralCenterPosition().getSI() - edgeOffset) < delta)
                {
                    if (!marker.isPermeable(gtuType, lateralDirection))
                    {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /** {@inheritDoc} */
    public final String toString()
    {
        CrossSectionLink<?, ?> link = getParentLink();
        // FIXME indexOf is the the correct way to determine the rank of a Lane
        return String.format("Lane %d of %s, %s", link.getCrossSectionElementList().indexOf(this), link.toString(),
                super.toString());
    }

}
