package org.opentrafficsim.road.gtu.lane.perception;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.perception.Perception;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.LinkDirection;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.perception.PerceivedObject;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.tactical.following.HeadwayGTU;
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
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 15, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public abstract class AbstractLanePerception implements Perception
{
    /** */
    private static final long serialVersionUID = 20151128L;

    /** The lane based GTU for which this perception module stores information. */
    protected LaneBasedGTU gtu;

    /** The forward headway and (leader) GTU. */
    private TimeStampedObject<HeadwayGTU> forwardHeadwayGTU;

    /** The backward headway and (follower) GTU. */
    private TimeStampedObject<HeadwayGTU> backwardHeadwayGTU;

    /** The minimum speed limit of all lanes where the GTU is registered. */
    private TimeStampedObject<Speed> speedLimit;

    /** The adjacent lanes that are accessible for the GTU at the left side. */
    private TimeStampedObject<Map<Lane, Set<Lane>>> accessibleAdjacentLanesLeft;

    /** The adjacent lanes that are accessible for the GTU at the right side. */
    private TimeStampedObject<Map<Lane, Set<Lane>>> accessibleAdjacentLanesRight;

    /** The GTUs parallel to us on the left side. */
    private TimeStampedObject<Set<LaneBasedGTU>> parallelGTUsLeft;

    /** The GTUs parallel to us on the right side. */
    private TimeStampedObject<Set<LaneBasedGTU>> parallelGTUsRight;

    /** The GTUs on the left side. */
    private TimeStampedObject<Collection<HeadwayGTU>> neighboringGTUsLeft;

    /** The GTUs on the right side. */
    private TimeStampedObject<Collection<HeadwayGTU>> neighboringGTUsRight;

    /**
     * Create a new LanePerception module. Because the constructor is often called inside the constructor of a GTU, this
     * constructor does not ask for the pointer to the GTU, as it is often impossible to provide at the time of construction.
     * Use the setter of the GTU instead.
     */
    public AbstractLanePerception()
    {
        super();
    }

    /**
     * sets the GTU -- call this method before any call to the perceive() method!
     * @param gtu the GTU for which this is the perception module
     */
    public final void setGtu(final LaneBasedGTU gtu)
    {
        this.gtu = gtu;
    }

    /**
     * Check whether the GTU has been initialized, and returns the current time.
     * @return the current time according to the simulator.
     * @throws GTUException when the GTU was not initialized yet.
     */
    private Time.Abs getTimestamp() throws GTUException
    {
        if (this.gtu == null)
        {
            throw new GTUException("gtu value has not been initialized for LanePerception when perceiving.");
        }
        return this.gtu.getSimulator().getSimulatorTime().getTime();
    }

    /**
     * Update the perceived speed limit.
     * @throws NetworkException when the speed limit for a GTU type cannot be retreived from the network.
     * @throws GTUException when the GTU was not initialized yet.
     */
    protected void updateSpeedLimit() throws GTUException, NetworkException
    {
        Time.Abs timestamp = getTimestamp();
        // assess the speed limit where we are right now
        this.speedLimit = new TimeStampedObject<>(new Speed(Double.MAX_VALUE, SpeedUnit.SI), timestamp);
        for (Lane lane : this.gtu.getLanes().keySet())
        {
            if (lane.getSpeedLimit(this.gtu.getGTUType()).lt(this.speedLimit.getObject()))
            {
                this.speedLimit = new TimeStampedObject<>(lane.getSpeedLimit(this.gtu.getGTUType()), timestamp);
            }
        }
    }

    /**
     * Update who's in front of us and how far away the nearest GTU is.
     * @throws GTUException when the GTU was not initialized yet.
     * @throws NetworkException when the headway cannot be determined for this GTU, usually due to routing problems.
     */
    protected void updateForwardHeadwayGTU() throws GTUException, NetworkException
    {
        Time.Abs timestamp = getTimestamp();
        Length.Rel maximumForwardHeadway = this.gtu.getDrivingCharacteristics().getForwardHeadwayDistance();
        this.forwardHeadwayGTU = new TimeStampedObject<>(headway(maximumForwardHeadway), timestamp);
    }

    /**
     * Update who's behind us and how far away the nearest GTU is.
     * @throws GTUException when the GTU was not initialized yet.
     * @throws NetworkException when the headway cannot be determined for this GTU, usually due to routing problems.
     */
    protected void updateBackwardHeadwayGTU() throws GTUException, NetworkException
    {
        Time.Abs timestamp = getTimestamp();
        Length.Rel maximumReverseHeadway = this.gtu.getDrivingCharacteristics().getBackwardHeadwayDistance();
        this.backwardHeadwayGTU = new TimeStampedObject<>(headway(maximumReverseHeadway), timestamp);
    }

    /**
     * Build a set of Lanes that is adjacent to the given lane that this GTU can enter, for the left lateral direction.
     * @throws GTUException when the GTU was not initialized yet.
     */
    protected void updateAccessibleAdjacentLanesLeft() throws GTUException
    {
        Time.Abs timestamp = getTimestamp();
        Map<Lane, Set<Lane>> accessibleAdjacentLanesMap = new HashMap<>();
        for (Lane lane : this.gtu.getLanes().keySet())
        {
            Set<Lane> adjacentLanes = new HashSet<Lane>(1);
            adjacentLanes.addAll(lane.accessibleAdjacentLanes(LateralDirectionality.LEFT, this.gtu.getGTUType()));
            accessibleAdjacentLanesMap.put(lane, adjacentLanes);
        }
        this.accessibleAdjacentLanesLeft = new TimeStampedObject<>(accessibleAdjacentLanesMap, timestamp);
    }

    /**
     * Build a set of Lanes that is adjacent to the given lane that this GTU can enter, for the left lateral direction.
     * @throws GTUException when the GTU was not initialized yet.
     */
    protected void updateAccessibleAdjacentLanesRight() throws GTUException
    {
        Time.Abs timestamp = getTimestamp();
        Map<Lane, Set<Lane>> accessibleAdjacentLanesMap = new HashMap<>();
        for (Lane lane : this.gtu.getLanes().keySet())
        {
            Set<Lane> adjacentLanes = new HashSet<Lane>(1);
            adjacentLanes.addAll(lane.accessibleAdjacentLanes(LateralDirectionality.RIGHT, this.gtu.getGTUType()));
            accessibleAdjacentLanesMap.put(lane, adjacentLanes);
        }
        this.accessibleAdjacentLanesRight = new TimeStampedObject<>(accessibleAdjacentLanesMap, timestamp);
    }

    /**
     * Update the information about the GTUs parallel to our GTU on the left side.
     * @throws GTUException when the GTU was not initialized yet.
     */
    protected void updateParallelGTUsLeft() throws GTUException
    {
        Time.Abs timestamp = getTimestamp();
        if (this.accessibleAdjacentLanesLeft == null
            || !timestamp.equals(this.accessibleAdjacentLanesLeft.getTimestamp()))
        {
            updateAccessibleAdjacentLanesLeft();
        }
        Set<LaneBasedGTU> parallelGTUSet = new HashSet<>();
        for (Lane lane : this.accessibleAdjacentLanesLeft.getObject().keySet())
        {
            parallelGTUSet.addAll(parallel(lane, timestamp));
        }
        this.parallelGTUsLeft = new TimeStampedObject<>(parallelGTUSet, timestamp);
    }

    /**
     * Update the information about the GTUs parallel to our GTU on the right side.
     * @throws GTUException when the GTU was not initialized yet.
     */
    protected void updateParallelGTUsRight() throws GTUException
    {
        Time.Abs timestamp = getTimestamp();
        if (this.accessibleAdjacentLanesRight == null
            || !timestamp.equals(this.accessibleAdjacentLanesRight.getTimestamp()))
        {
            updateAccessibleAdjacentLanesLeft();
        }
        Set<LaneBasedGTU> parallelGTUSet = new HashSet<>();
        for (Lane lane : this.accessibleAdjacentLanesRight.getObject().keySet())
        {
            parallelGTUSet.addAll(parallel(lane, timestamp));
        }
        this.parallelGTUsRight = new TimeStampedObject<>(parallelGTUSet, timestamp);
    }

    /**
     * Update the information about the GTUs left of our GTU, and behind us or ahead on the left hand side.
     * @throws GTUException when the GTU was not initialized yet.
     * @throws NetworkException
     */
    protected void updateLaneTrafficLeft() throws GTUException, NetworkException
    {
        Time.Abs timestamp = getTimestamp();
        if (this.accessibleAdjacentLanesLeft == null
            || !timestamp.equals(this.accessibleAdjacentLanesLeft.getTimestamp()))
        {
            updateAccessibleAdjacentLanesLeft();
        }

        if (this.parallelGTUsLeft == null || !timestamp.equals(this.parallelGTUsLeft.getTimestamp()))
        {
            updateParallelGTUsLeft();
        }

        // for the accessible lanes, see who is ahead of us and in front of us
        Length.Rel maximumForwardHeadway = this.gtu.getDrivingCharacteristics().getForwardHeadwayDistance();
        Length.Rel maximumReverseHeadway = this.gtu.getDrivingCharacteristics().getBackwardHeadwayDistance();
        this.neighboringGTUsLeft =
            new TimeStampedObject<>(collectNeighborLaneTraffic(LateralDirectionality.LEFT, timestamp,
                maximumForwardHeadway, maximumReverseHeadway), timestamp);
    }

    /**
     * Update the information about the GTUs right of our GTU, and behind us or ahead on the left hand side.
     * @throws GTUException when the GTU was not initialized yet.
     * @throws NetworkException
     */
    protected void updateLaneTrafficRight() throws GTUException, NetworkException
    {
        Time.Abs timestamp = getTimestamp();
        if (this.accessibleAdjacentLanesRight == null
            || !timestamp.equals(this.accessibleAdjacentLanesRight.getTimestamp()))
        {
            updateAccessibleAdjacentLanesRight();
        }

        if (this.parallelGTUsRight == null || !timestamp.equals(this.parallelGTUsRight.getTimestamp()))
        {
            updateParallelGTUsRight();
        }

        // for the accessible lanes, see who is ahead of us and in front of us
        Length.Rel maximumForwardHeadway = this.gtu.getDrivingCharacteristics().getForwardHeadwayDistance();
        Length.Rel maximumReverseHeadway = this.gtu.getDrivingCharacteristics().getBackwardHeadwayDistance();
        this.neighboringGTUsRight =
            new TimeStampedObject<>(collectNeighborLaneTraffic(LateralDirectionality.RIGHT, timestamp,
                maximumForwardHeadway, maximumReverseHeadway), timestamp);
    }

    /**
     * Provide a list of lanes that we can follow to a point where we have to stop. This includes objects that can force us to
     * stop, such as traffic lights. <br>
     * TODO: the exact formulation of a lane plan has not been fully determined yet.
     * @throws GTUException when the GTU has not been set
     */
    protected void updateLanePlan() throws GTUException
    {
        Time.Abs timestamp = getTimestamp();

    }

    /**
     * @param lateralDirection the direction to return the accessible adjacent lane map for
     * @return the accessible adjacent lane map for the given direction
     */
    private Map<Lane, Set<Lane>> accessibleAdjacentLaneMap(final LateralDirectionality lateralDirection)
    {
        return lateralDirection.equals(LateralDirectionality.LEFT) ? this.accessibleAdjacentLanesLeft.getObject()
            : this.accessibleAdjacentLanesRight.getObject();
    }

    /**
     * @param lateralDirection the direction to return the parallel GTU map for
     * @return the parallel GTU map for the given direction
     */
    private Set<LaneBasedGTU> parallelGTUMap(final LateralDirectionality lateralDirection)
    {
        return lateralDirection.equals(LateralDirectionality.LEFT) ? this.parallelGTUsLeft.getObject()
            : this.parallelGTUsRight.getObject();
    }

    /**
     * @param lateralDirection the direction to return the neighboring GTU collection for
     * @return the neighboring GTU collection for the given direction
     */
    private Collection<HeadwayGTU> neighboringGTUCollection(final LateralDirectionality lateralDirection)
    {
        return lateralDirection.equals(LateralDirectionality.LEFT) ? this.neighboringGTUsLeft.getObject()
            : this.neighboringGTUsRight.getObject();
    }

    /**
     * Determine which GTU is in front of this GTU, or behind this GTU. This method looks in all lanes where this GTU is
     * registered, and not further than the absolute value of the given maxDistance. The minimum headway is returned of all
     * Lanes where the GTU is registered. When no GTU is found within the given maxDistance, <b>null</b> is returned. The search
     * will extend into successive lanes if the maxDistance is larger than the remaining length on the lane. When Lanes (or
     * underlying CrossSectionLinks) diverge, the headway algorithms have to look at multiple Lanes and return the minimum
     * headway in each of the Lanes. When the Lanes (or underlying CrossSectionLinks) converge, "parallel" traffic is not taken
     * into account in the headway calculation. Instead, gap acceptance algorithms or their equivalent should guide the merging
     * behavior.<br>
     * <b>Note:</b> Headway is the net headway and calculated on a front-to-back basis.
     * @param maxDistance the maximum distance to look for the nearest GTU; positive values search forwards; negative values
     *            search backwards
     * @return HeadwayGTU; the headway and the GTU
     * @throws GTUException when there is an error with the next lanes in the network.
     * @throws NetworkException when there is a problem with the route planner
     */
    private final HeadwayGTU headway(Length.Rel maxDistance) throws GTUException, NetworkException
    {
        return headwayGTUSI(maxDistance.getSI());
    }

    /**
     * Determine by what distance the front of this GTU is behind the rear an other GTU, or the rear of this GTU is ahead of the
     * front of an other GTU. Only positive values are returned. This method only looks in the given lane, and not further than
     * the given maxDistance. When no vehicle is found within the given maxDistance, maxDictance is returned. The search will
     * extend into successive lanes if the maxDistance is larger than the length of the lane.<br>
     * <b>Note:</b> Headway is the net headway and calculated on a front-to-back basis.
     * @param lane the lane to look for another GTU
     * @param direction the direction of the GTU on the lane
     * @param maxDistance the maximum distance to look for; if positive, the search is forwards; if negative, the search is
     *            backwards
     * @return HeadwayGTU; the headway and the GTU
     * @throws GTUException when the vehicle's route is inconclusive or vehicles are not registered correctly on their lanes
     * @throws NetworkException when there is a problem with the route planner
     */
    private final HeadwayGTU headway(final Lane lane, final GTUDirectionality direction, final Length.Rel maxDistance)
        throws GTUException, NetworkException
    {
        Time.Abs when = this.gtu.getSimulator().getSimulatorTime().getTime();
        if (maxDistance.getSI() > 0.0)
        {
            return headwayRecursiveForwardSI(lane, direction,
                this.gtu.projectedPosition(lane, this.gtu.getFront(), when).getSI(), 0.0, maxDistance.getSI(), when);
        }
        else
        {
            return headwayRecursiveBackwardSI(lane, direction,
                this.gtu.projectedPosition(lane, this.gtu.getRear(), when).getSI(), 0.0, -maxDistance.getSI(), when);
        }
    }

    /**
     * Determine which GTUs are parallel with us on another lane, based on fractional positions. <br>
     * Note: When the GTU that calls the method is also registered on the given lane, it is excluded from the return set.
     * @param lane the lane to look for parallel (partial or full overlapping) GTUs.
     * @param when the future time for which to calculate the headway
     * @return the set of GTUs parallel to us on the other lane (partial overlap counts as parallel), based on fractional
     *         positions, or an empty set when no GTUs were found.
     * @throws GTUException when the vehicle's route is inconclusive, when vehicles are not registered correctly on their lanes,
     *             or when the given lane is not parallel to one of the lanes where we are registered.
     */
    private final Set<LaneBasedGTU> parallel(final Lane lane, final Time.Abs when) throws GTUException
    {
        Set<LaneBasedGTU> gtuSet = new LinkedHashSet<LaneBasedGTU>();
        for (Lane l : this.gtu.getLanes().keySet())
        {
            // only take lanes that we can compare based on a shared design line
            if (l.getParentLink().equals(lane.getParentLink()))
            {
                // compare based on fractional positions.
                double posFractionFront = Math.max(0.0, this.gtu.fractionalPosition(l, this.gtu.getFront(), when));
                double posFractionRear = Math.min(1.0, this.gtu.fractionalPosition(l, this.gtu.getRear(), when));
                double posMin = Math.min(posFractionFront, posFractionRear);
                double posMax = Math.max(posFractionFront, posFractionRear);
                for (LaneBasedGTU otherGTU : lane.getGtuList())
                {
                    if (!otherGTU.equals(this))
                    {
                        /*- cater for: *-----*         *-----*       *-----*       *----------*
                         *                *-----*    *----*      *------------*       *-----*
                         * where the GTUs can each drive in two directions (!)
                         */
                        double gtuFractionFront =
                            Math.max(0.0, otherGTU.fractionalPosition(lane, otherGTU.getFront(), when));
                        double gtuFractionRear =
                            Math.min(1.0, otherGTU.fractionalPosition(lane, otherGTU.getRear(), when));
                        double gtuMin = Math.min(gtuFractionFront, gtuFractionRear);
                        double gtuMax = Math.max(gtuFractionFront, gtuFractionRear);
                        if ((gtuMin >= posMin && gtuMin <= posMax) || (gtuMax >= posMin && gtuMax <= posMax)
                            || (posMin >= gtuMin && posMin <= gtuMax) || (posMax >= gtuMin && posMax <= gtuMax))
                        {
                            gtuSet.add(otherGTU);
                        }
                    }
                }
            }
        }
        return gtuSet;
    }

    /**
     * Determine which GTUs are parallel with us in a certain lateral direction, based on fractional positions. <br>
     * Note 1: This method will look to the adjacent lanes of all lanes where the vehicle has been registered.<br>
     * Note 2: When the GTU that calls the method is also registered on the given lane, it is excluded from the return set.
     * @param lateralDirection the direction of the adjacent lane(s) to look for parallel (partial or full overlapping) GTUs.
     * @param when the future time for which to calculate the headway
     * @return the set of GTUs parallel to us on other lane(s) in the given direction (partial overlap counts as parallel),
     *         based on fractional positions, or an empty set when no GTUs were found.
     * @throws GTUException when the vehicle's route is inconclusive, when vehicles are not registered correctly on their lanes,
     *             or when there are no lanes parallel to one of the lanes where we are registered in the given direction.
     */
    private final Set<LaneBasedGTU> parallel(final LateralDirectionality lateralDirection, final Time.Abs when)
        throws GTUException
    {
        Set<LaneBasedGTU> gtuSet = new LinkedHashSet<LaneBasedGTU>();
        for (Lane lane : this.gtu.getLanes().keySet())
        {
            for (Lane adjacentLane : accessibleAdjacentLaneMap(lateralDirection).get(lane))
            {
                gtuSet.addAll(parallel(adjacentLane, when));
            }
        }
        return gtuSet;
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
     * <b>Note:</b> LEFT is seen as a negative lateral direction, RIGHT as a positive lateral direction. <br>
     * @param currentLane the lane to look for the best accessible adjacent lane
     * @param lateralDirection the direction (LEFT, RIGHT) to look at
     * @param longitudinalPosition Length.Rel; the position of the GTU along this Lane
     * @return the lane if it is accessible, or null if there is no lane, it is not accessible, or the driving direction does
     *         not match.
     */
    public final Lane bestAccessibleAdjacentLane(final Lane currentLane, final LateralDirectionality lateralDirection,
        final Length.Rel longitudinalPosition)
    {
        Set<Lane> candidates = accessibleAdjacentLaneMap(lateralDirection).get(currentLane);
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
            if (lane.getWidth(longitudinalPosition).getSI() > widthM)
            {
                widthM = lane.getWidth(longitudinalPosition).getSI();
                bestLane = lane;
            }
        }
        return bestLane;
    }

    /**
     * Collect relevant traffic in adjacent lanes. Parallel traffic is included with headway equal to Double.NaN.
     * @param directionality LateralDirectionality; either <cite>LateralDirectionality.LEFT</cite>, or
     *            <cite>LateralDirectionality.RIGHT</cite>
     * @param when DoubleScalar.Abs&lt;TimeUnit&gt;; the (current) time
     * @param maximumForwardHeadway DoubleScalar.Rel&lt;LengthUnit&gt;; the maximum forward search distance
     * @param maximumReverseHeadway DoubleScalar.Rel&lt;LengthUnit&gt;; the maximum reverse search distance
     * @return Collection&lt;LaneBasedGTU&gt;;
     * @throws NetworkException on network inconsistency
     * @throws GTUException on problems with the GTU state (e.g., position)
     */
    private Collection<HeadwayGTU> collectNeighborLaneTraffic(final LateralDirectionality directionality,
        final Time.Abs when, final Length.Rel maximumForwardHeadway, final Length.Rel maximumReverseHeadway)
        throws NetworkException, GTUException
    {
        Collection<HeadwayGTU> result = new HashSet<HeadwayGTU>();
        for (LaneBasedGTU p : parallel(directionality, when))
        {
            result.add(new HeadwayGTU(p, Double.NaN));
        }
        for (Lane lane : this.gtu.getLanes().keySet())
        {
            for (Lane adjacentLane : accessibleAdjacentLaneMap(directionality).get(lane))
            {
                HeadwayGTU leader = headway(adjacentLane, this.gtu.getLanes().get(lane), maximumForwardHeadway);
                if (null != leader.getGTU() && !result.contains(leader))
                {
                    result.add(leader);
                }
                HeadwayGTU follower = headway(adjacentLane, this.gtu.getLanes().get(lane), maximumReverseHeadway);
                if (null != follower.getGTU() && !result.contains(follower))
                {
                    result.add(new HeadwayGTU(follower.getGTU(), -follower.getDistanceSI()));
                }
            }
        }
        return result;
    }

    /**
     * @param maxDistanceSI the maximum distance to look for in SI units
     * @return the nearest GTU and the net headway to this GTU in SI units when we have found the GTU, or a null GTU with a
     *         distance of Double.MAX_VALUE meters when no other GTU could not be found within maxDistanceSI meters
     * @throws GTUException when there is a problem with the geometry of the network
     * @throws NetworkException when there is a problem with the route planner
     */
    private HeadwayGTU headwayGTUSI(final double maxDistanceSI) throws GTUException, NetworkException
    {
        Time.Abs time = this.gtu.getSimulator().getSimulatorTime().getTime();
        HeadwayGTU foundMaxGTUDistanceSI = new HeadwayGTU(null, Double.MAX_VALUE);
        // search for the closest GTU on all current lanes we are registered on.
        if (maxDistanceSI > 0.0)
        {
            // look forward.
            for (Lane lane : this.gtu.positions(this.gtu.getFront()).keySet())
            {
                HeadwayGTU closest =
                    headwayRecursiveForwardSI(lane, this.gtu.getLanes().get(lane),
                        this.gtu.position(lane, this.gtu.getFront(), time).getSI(), 0.0, maxDistanceSI, time);
                if (closest.getDistanceSI() < maxDistanceSI
                    && closest.getDistanceSI() < foundMaxGTUDistanceSI.getDistanceSI())
                {
                    foundMaxGTUDistanceSI = closest;
                }
            }
        }
        else
        {
            // look backward.
            for (Lane lane : this.gtu.positions(this.gtu.getRear()).keySet())
            {
                HeadwayGTU closest =
                    headwayRecursiveBackwardSI(lane, this.gtu.getLanes().get(lane),
                        this.gtu.position(lane, this.gtu.getRear(), time).getSI(), 0.0, -maxDistanceSI, time);
                if (closest.getDistanceSI() < -maxDistanceSI
                    && closest.getDistanceSI() < foundMaxGTUDistanceSI.getDistanceSI())
                {
                    foundMaxGTUDistanceSI = closest;
                }
            }
        }
        return foundMaxGTUDistanceSI;
    }

    /**
     * Calculate the minimum net headway, possibly on subsequent lanes, in forward direction. It returns the headway in SI units
     * when we have found a GTU on our path within 'maxDistanceSI' meters. When no other GTU could not be found within
     * maxDistanceSI meters it returns a null GTU with a distance of maxDistanceSI meters. When there is a lane drop, or our
     * successor lanes do not continue on our route, a null GTU and the distance to the lane drop or last point on our route is
     * returned. Typically, this method is called with the FRONT of the GTU that needs the information, where the minimum
     * distance to the REAR of the next GTU is returned.
     * @param lane the lane where we are looking right now
     * @param direction the direction we are driving on that lane
     * @param lanePositionSI from which position on this lane do we start measuring? This is the current position of the GTU
     *            when we measure in the lane where the original GTU is positioned, and 0.0 for each subsequent lane
     * @param cumDistanceSI the distance we have already covered searching on previous lanes
     * @param maxDistanceSI the maximum distance to look for in SI units; stays the same in subsequent calls
     * @param when the current or future time for which to calculate the headway
     * @return the headway in SI units when we have found the GTU, or a null GTU with a distance of maxDistanceSI meters when no
     *         other GTU could not be found within maxDistanceSI meters, or a null GTU and the distance to a lane drop or
     *         impossibility to drive further when the successor lane does not continue on our route.
     * @throws GTUException when there is a problem with the geometry of the network
     * @throws NetworkException when the
     */
    private HeadwayGTU headwayRecursiveForwardSI(final Lane lane, final GTUDirectionality direction,
        final double lanePositionSI, final double cumDistanceSI, final double maxDistanceSI, final Time.Abs when)
        throws GTUException, NetworkException
    {
        LaneBasedGTU otherGTU =
            lane.getGtuAhead(new Length.Rel(lanePositionSI, LengthUnit.SI), direction, RelativePosition.REAR, when);
        if (otherGTU != null)
        {
            double distanceSI =
                direction.equals(GTUDirectionality.DIR_PLUS) ? cumDistanceSI
                    + otherGTU.position(lane, otherGTU.getRear(), when).getSI() - lanePositionSI : cumDistanceSI
                    + lanePositionSI;
            if (distanceSI > 0 && distanceSI <= maxDistanceSI)
            {
                return new HeadwayGTU(otherGTU, distanceSI);
            }
            return new HeadwayGTU(null, maxDistanceSI);
        }

        double distanceSI =
            direction.equals(GTUDirectionality.DIR_PLUS) ? cumDistanceSI + lane.getLength().getSI() - lanePositionSI
                : cumDistanceSI + lanePositionSI;
        if (distanceSI > maxDistanceSI)
        {
            // No other GTU was found on one of the current lanes or their successors.
            return new HeadwayGTU(null, maxDistanceSI);
        }

        // Continue search on successor lanes (if they exist). If not, STOP AT THE LANE DROP.
        // is there a successor link?
        GTUType gtuType = this.gtu.getGTUType();
        if (lane.nextLanes(gtuType).size() == 0)
        {
            return new HeadwayGTU(null, distanceSI);
        }

        // is there a next lane that is on our path?
        Lane nextLane = null;
        GTUDirectionality nextDir = null;
        if (lane.nextLanes(gtuType).size() == 1)
        {
            nextLane = lane.nextLanes(gtuType).keySet().iterator().next();
            nextDir = lane.nextLanes(gtuType).get(nextLane);
        }
        else
        {
            LinkDirection ld =
                this.gtu.getStrategicalPlanner().nextLinkDirection(lane.getParentLink(), direction, gtuType);
            for (Lane l : lane.nextLanes(gtuType).keySet())
            {
                if (l.getParentLink().equals(ld.getLink()))
                {
                    nextLane = l;
                    nextDir = lane.nextLanes(gtuType).get(nextLane);
                }
            }
            if (nextLane == null)
            {
                // none of the next lanes keep us on the route
                return new HeadwayGTU(null, distanceSI);
            }
        }
        return headwayRecursiveForwardSI(nextLane, nextDir, 0.0, distanceSI, maxDistanceSI, when);
    }

    /**
     * Calculate the minimum headway, possibly on subsequent lanes, in backward direction (so between our back, and the other
     * GTU's front). Note: this method returns a POSITIVE number.
     * @param lane the lane where we are looking right now
     * @param direction the direction we are driving on that lane
     * @param lanePositionSI from which position on this lane do we start measuring? This is the current position of the rear of
     *            the GTU when we measure in the lane where the original GTU is positioned, and lane.getLength() for each
     *            subsequent lane.
     * @param cumDistanceSI the distance we have already covered searching on previous lanes. Note: This is a POSITIVE number.
     * @param maxDistanceSI the maximum distance to look for in SI units; stays the same in subsequent calls. Note: this is a
     *            POSITIVE number.
     * @param when the current or future time for which to calculate the headway
     * @return the headway in SI units when we have found the GTU, or a null GTU with a distance of Double.MAX_VALUE meters when
     *         no other GTU could not be found within maxDistanceSI meters
     * @throws GTUException when there is a problem with the geometry of the network
     */
    private HeadwayGTU headwayRecursiveBackwardSI(final Lane lane, final GTUDirectionality direction,
        final double lanePositionSI, final double cumDistanceSI, final double maxDistanceSI, final Time.Abs when)
        throws GTUException
    {
        // WRONG - adapt method to forward perception method!
        LaneBasedGTU otherGTU =
            lane.getGtuBehind(new Length.Rel(lanePositionSI, LengthUnit.SI), direction, RelativePosition.FRONT, when);
        if (otherGTU != null)
        {
            double distanceM =
                cumDistanceSI + lanePositionSI - otherGTU.position(lane, otherGTU.getFront(), when).getSI();
            if (distanceM > 0 && distanceM <= maxDistanceSI)
            {
                return new HeadwayGTU(otherGTU, distanceM);
            }
            return new HeadwayGTU(null, Double.MAX_VALUE);
        }

        // Continue search on predecessor lanes.
        if (cumDistanceSI + lanePositionSI < maxDistanceSI)
        {
            // is there a predecessor link?
            if (lane.prevLanes(this.gtu.getGTUType()).size() > 0)
            {
                HeadwayGTU foundMaxGTUDistanceSI = new HeadwayGTU(null, Double.MAX_VALUE);
                for (Lane prevLane : lane.prevLanes(this.gtu.getGTUType()).keySet())
                {
                    // What is behind us is INDEPENDENT of the followed route!
                    double traveledDistanceSI = cumDistanceSI + lanePositionSI;
                    // WRONG - adapt method to forward perception method!
                    HeadwayGTU closest =
                        headwayRecursiveBackwardSI(prevLane, direction, prevLane.getLength().getSI(),
                            traveledDistanceSI, maxDistanceSI, when);
                    if (closest.getDistanceSI() < maxDistanceSI
                        && closest.getDistanceSI() < foundMaxGTUDistanceSI.getDistanceSI())
                    {
                        foundMaxGTUDistanceSI = closest;
                    }
                }
                return foundMaxGTUDistanceSI;
            }
        }

        // No other GTU was not on one of the current lanes or their successors.
        return new HeadwayGTU(null, Double.MAX_VALUE);
    }

    /**
     * Calculate the headway to a GTU, possibly on subsequent lanes, in forward direction.
     * @param lane the lane where we are looking right now
     * @param lanePositionSI from which position on this lane do we start measuring? This is the current position of the (front
     *            of the) GTU when we measure in the lane where the original GTU is positioned, and 0.0 for each subsequent lane
     * @param otherGTU the GTU to which the headway must be returned
     * @param cumDistanceSI the distance we have already covered searching on previous lanes
     * @param maxDistanceSI the maximum distance to look for; stays the same in subsequent calls
     * @param when the future time for which to calculate the headway
     * @return the headway in SI units when we have found the GTU, or Double.MAX_VALUE when the otherGTU could not be found
     *         within maxDistanceSI
     * @throws GTUException when there is a problem with the geometry of the network
     */
    private double headwayRecursiveForwardSI(final Lane lane, final double lanePositionSI, final LaneBasedGTU otherGTU,
        final double cumDistanceSI, final double maxDistanceSI, final Time.Abs when) throws GTUException
    {
        if (lane.getGtuList().contains(otherGTU))
        {
            double distanceM =
                cumDistanceSI + otherGTU.position(lane, otherGTU.getRear(), when).getSI() - lanePositionSI;
            if (distanceM > 0 && distanceM <= maxDistanceSI)
            {
                return distanceM;
            }
            return Double.MAX_VALUE;
        }

        // Continue search on successor lanes.
        if (cumDistanceSI + lane.getLength().getSI() - lanePositionSI < maxDistanceSI)
        {
            // is there a successor link?
            for (Lane nextLane : lane.nextLanes(this.gtu.getGTUType()).keySet())
            {
                // TODO Only follow links on the Route if there is a Route
                // if (this.getRoute() == null || this.getRoute().size() == 0) /* XXX STUB dummy route */
                // || this.routeNavigator.getRoute().containsLink((Link) lane.getParentLink()))
                {
                    double traveledDistanceSI = cumDistanceSI + lane.getLength().getSI() - lanePositionSI;
                    double headwaySuccessor =
                        headwayRecursiveForwardSI(nextLane, 0.0, otherGTU, traveledDistanceSI, maxDistanceSI, when);
                    if (headwaySuccessor < maxDistanceSI)
                    {
                        return headwaySuccessor;
                    }
                }
            }
        }

        // The otherGTU was not on one of the current lanes or their successors.
        return Double.MAX_VALUE;
    }

    /**
     * Calculate the headway to a GTU, possibly on subsequent lanes, in backward direction.
     * @param lane the lane where we are looking right now
     * @param lanePositionSI from which position on this lane do we start measuring? This is the current position of the (back
     *            of) the GTU when we measure in the lane where the original GTU is positioned, and the length of the lane for
     *            each subsequent lane
     * @param otherGTU the GTU to which the headway must be returned
     * @param cumDistanceSI the distance we have already covered searching on previous lanes, as a POSITIVE number
     * @param maxDistanceSI the maximum distance to look for; stays the same in subsequent calls, as a POSITIVE number
     * @param when the future time for which to calculate the headway
     * @return the headway in SI units when we have found the GTU, or Double.MAX_VALUE when the otherGTU could not be found
     *         within maxDistanceSI
     * @throws GTUException when there is a problem with the geometry of the network
     */
    private double headwayRecursiveBackwardSI(final Lane lane, final double lanePositionSI,
        final LaneBasedGTU otherGTU, final double cumDistanceSI, final double maxDistanceSI, final Time.Abs when)
        throws GTUException
    {
        if (lane.getGtuList().contains(otherGTU))
        {
            double distanceM =
                cumDistanceSI + lanePositionSI - otherGTU.position(lane, otherGTU.getFront(), when).getSI();
            if (distanceM > 0 && distanceM <= maxDistanceSI)
            {
                return distanceM;
            }
            return Double.MAX_VALUE;
        }

        // Continue search on predecessor lanes.
        if (cumDistanceSI + lanePositionSI < maxDistanceSI)
        {
            // is there a predecessor link?
            for (Lane prevLane : lane.prevLanes(this.gtu.getGTUType()).keySet())
            {
                // Routes are NOT IMPORTANT when we look backward.
                double traveledDistanceSI = cumDistanceSI + lanePositionSI;
                // PK: This looks like a bug; replacement code below this comment.
                // double headwayPredecessor =
                // headwayRecursiveForwardSI(prevLane, prevLane.getLength().getSI(), otherGTU,
                // traveledDistanceSI, maxDistanceSI, when);
                double headwayPredecessor =
                    headwayRecursiveBackwardSI(prevLane, prevLane.getLength().getSI(), otherGTU, traveledDistanceSI,
                        maxDistanceSI, when);
                if (headwayPredecessor < maxDistanceSI)
                {
                    return headwayPredecessor;
                }
            }
        }

        // The otherGTU was not on one of the current lanes or their successors.
        return Double.MAX_VALUE;
    }

    /**
     * @return gtu
     */
    public final LaneBasedGTU getGtu()
    {
        return this.gtu;
    }

    /**
     * @return forwardHeadwayGTU
     */
    public final HeadwayGTU getForwardHeadwayGTU()
    {
        return this.forwardHeadwayGTU.getObject();
    }

    /**
     * @return backwardHeadwayGTU
     */
    public final HeadwayGTU getBackwardHeadwayGTU()
    {
        return this.backwardHeadwayGTU.getObject();
    }

    /**
     * @return accessibleAdjacentLanesLeft
     */
    public final Map<Lane, Set<Lane>> getAccessibleAdjacentLanesLeft()
    {
        return this.accessibleAdjacentLanesLeft.getObject();
    }

    /**
     * @return accessibleAdjacentLanesRight
     */
    public final Map<Lane, Set<Lane>> getAccessibleAdjacentLanesRight()
    {
        return this.accessibleAdjacentLanesRight.getObject();
    }

    /**
     * @return neighboringGTUsLeft
     */
    public final Collection<HeadwayGTU> getNeighboringGTUsLeft()
    {
        return this.neighboringGTUsLeft.getObject();
    }

    /**
     * @return neighboringGTUsRight
     */
    public final Collection<HeadwayGTU> getNeighboringGTUsRight()
    {
        return this.neighboringGTUsRight.getObject();
    }

    /**
     * @return parallelGTUsLeft
     */
    public final Set<LaneBasedGTU> getParallelGTUsLeft()
    {
        return this.parallelGTUsLeft.getObject();
    }

    /**
     * @return parallelGTUsRight
     */
    public final Set<LaneBasedGTU> getParallelGTUsRight()
    {
        return this.parallelGTUsRight.getObject();
    }

    /**
     * @return speedLimit
     */
    public final Speed getSpeedLimit()
    {
        return this.speedLimit.getObject();
    }

    /** {@inheritDoc} */
    @Override
    public Set<PerceivedObject> getPerceivedObjects()
    {
        // TODO getPerceivedObjects() in LanePerception
        return new HashSet<PerceivedObject>();
    }

    /**
     * @return TimeStamped forwardHeadwayGTU
     */
    public final TimeStampedObject<HeadwayGTU> getTimeStampedForwardHeadwayGTU()
    {
        return this.forwardHeadwayGTU;
    }

    /**
     * @return TimeStamped backwardHeadwayGTU
     */
    public final TimeStampedObject<HeadwayGTU> getTimeStampedBackwardHeadwayGTU()
    {
        return this.backwardHeadwayGTU;
    }

    /**
     * @return TimeStamped accessibleAdjacentLanesLeft
     */
    public final TimeStampedObject<Map<Lane, Set<Lane>>> getTimeStampedAccessibleAdjacentLanesLeft()
    {
        return this.accessibleAdjacentLanesLeft;
    }

    /**
     * @return TimeStamped accessibleAdjacentLanesRight
     */
    public final TimeStampedObject<Map<Lane, Set<Lane>>> getTimeStampedAccessibleAdjacentLanesRight()
    {
        return this.accessibleAdjacentLanesRight;
    }

    /**
     * @return TimeStamped neighboringGTUsLeft
     */
    public final TimeStampedObject<Collection<HeadwayGTU>> getTimeStampedNeighboringGTUsLeft()
    {
        return this.neighboringGTUsLeft;
    }

    /**
     * @return TimeStamped neighboringGTUsRight
     */
    public final TimeStampedObject<Collection<HeadwayGTU>> getTimeStampedNeighboringGTUsRight()
    {
        return this.neighboringGTUsRight;
    }

    /**
     * @return TimeStamped parallelGTUsLeft
     */
    public final TimeStampedObject<Set<LaneBasedGTU>> getTimeStampedParallelGTUsLeft()
    {
        return this.parallelGTUsLeft;
    }

    /**
     * @return TimeStamped parallelGTUsRight
     */
    public final TimeStampedObject<Set<LaneBasedGTU>> getTimeStampedParallelGTUsRight()
    {
        return this.parallelGTUsRight;
    }

    /**
     * @return TimeStamped speedLimit
     */
    public final TimeStampedObject<Speed> getTimeStampedSpeedLimit()
    {
        return this.speedLimit;
    }

    /**
     * @return TimeStamped perceived objects
     * @throws GTUException when GTU was not initialized
     */
    public TimeStampedObject<Set<PerceivedObject>> getTimeStampedPerceivedObjects() throws GTUException
    {
        // TODO getPerceivedObjects() in LanePerception
        return new TimeStampedObject<Set<PerceivedObject>>(new HashSet<PerceivedObject>(), getTimestamp());
    }

}
