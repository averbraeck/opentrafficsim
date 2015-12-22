package org.opentrafficsim.road.gtu.lane.perception;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.perception.Perception;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.Link;
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
 * recalculated when asked explicitly.<br>
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
public class LanePerception implements Perception
{
    /** */
    private static final long serialVersionUID = 20151128L;

    /** the time stamp of the last perception. */
    private Time.Abs timestamp = new Time.Abs(Double.NaN, TimeUnit.SI);

    /** the lane based GTU for which this perception module stores information. */
    private LaneBasedGTU gtu;

    /** the lanes ahead of us for the given headway; stored so it is only calculated once per perception round. */
    private List<Lane> laneListForward = new ArrayList<>();

    /** the forward headway and (leader) GTU. */
    private HeadwayGTU forwardHeadwayGTU;

    /** the backward headway and (follower) GTU. */
    private HeadwayGTU backwardHeadwayGTU;

    /** the minimum speed limit of all lanes where the GTU is registered. */
    private Speed speedLimit;

    /**
     * The adjacent lanes that are accessible for the GTU This information is cached, because it might be requested by different
     * functions in the tactical planner. The set of lanes is stored per LateralDirectionality (LEFT, RIGHT).
     */
    private final Map<Lane, EnumMap<LateralDirectionality, Set<Lane>>> accessibleAdjacentLanes = new HashMap<>();

    /** the GTUs parallel to us. */
    private final Map<Lane, Set<LaneBasedGTU>> parallelGTUs = new HashMap<>();

    /**
     * Create a new LanePerception module. Because the constructor is often called inside the constructor of a GTU, this
     * constructor does not ask for the pointer to the GTU, as it is often impossible to provide at the time of construction.
     * Use the setter of the GTU instead.
     */
    public LanePerception()
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
     * @return whether perception has ever taken place or not
     */
    public boolean isInitialized()
    {
        return !Double.isNaN(this.timestamp.si);
    }

    /** {@inheritDoc} */
    @Override
    public void perceive() throws GTUException, NetworkException
    {
        if (this.gtu == null)
        {
            throw new GTUException("gtu value has not been initialized for LanePerception.perceive() method");
        }

        this.timestamp = this.gtu.getSimulator().getSimulatorTime().getTime();

        // assess the speed limit where we are right now
        this.speedLimit = new Speed(Double.MAX_VALUE, SpeedUnit.SI);
        for (Lane lane : this.gtu.getLanes().keySet())
        {
            if (lane.getSpeedLimit(this.gtu.getGTUType()).lt(this.speedLimit))
            {
                this.speedLimit = lane.getSpeedLimit(this.gtu.getGTUType());
            }
        }

        // build a list of lanes forward, with a maximum headway.
        buildLaneListForward(this.gtu.getDrivingCharacteristics().getForwardHeadwayDistance());

        // determine who's in front of us and behind us
        this.forwardHeadwayGTU = headway(this.gtu.getDrivingCharacteristics().getForwardHeadwayDistance());
        this.backwardHeadwayGTU = headway(this.gtu.getDrivingCharacteristics().getBackwardHeadwayDistance());

        // determine where we might go
        buildAccessibleAdjacentLanes();

        // for the accessible lanes, see who is parallel with us
        this.parallelGTUs.clear();
        for (Lane lane : this.accessibleAdjacentLanes.keySet())
        {
            this.parallelGTUs.put(lane, parallel(lane, this.timestamp));
        }

        // for the accessible lanes, see who is ahead of us and in front of us
        // TODO see who is ahead of us and in front of us

        // look for traffic lights, blocking objects, lane ends, or other objects that can force us to stop
        // TODO other objects that can force us to stop
    }

    /**
     * Build a list of lanes forward, with a maximum headway relative to the reference point of the GTU.
     * @param maxHeadway the maximum length for which lanes should be returned
     * @throws NetworkException when the vehicle is not on one of the lanes on which it is registered
     */
    private void buildLaneListForward(final Length.Rel maxHeadway) throws NetworkException
    {
        this.laneListForward.clear();
        Lane lane = getReferenceLane();
        this.laneListForward.add(lane);
        Length.Rel lengthForward =
            this.gtu.getLanes().get(lane).equals(GTUDirectionality.DIR_PLUS) ? this.gtu.position(lane,
                this.gtu.getReference()) : lane.getLength().minus(this.gtu.position(lane, this.gtu.getReference()));
        while (lengthForward.lt(maxHeadway))
        {
            Map<Lane, GTUDirectionality> lanes = lane.nextLanes(this.gtu.getGTUType());
            if (lanes.size() == 0)
            {
                // dead end. return with the list as is.
                return;
            }
            if (lanes.size() == 1)
            {
                lane = lanes.keySet().iterator().next();
                this.laneListForward.add(lane);
                lengthForward = lengthForward.plus(lane.getLength());
            }
            else
            {
                // multiple next lanes; ask the strategical planner where to go
                LinkDirection ld =
                    this.gtu.getStrategicalPlanner().nextLinkDirection(lane.getParentLink(),
                        this.gtu.getLanes().get(lane));
                Link nextLink = ld.getLink();
                for (Lane nextLane : lanes.keySet())
                {
                    if (nextLane.getParentLink().equals(nextLink))
                    {
                        lane = nextLane;
                        this.laneListForward.add(lane);
                        lengthForward = lengthForward.plus(lane.getLength());
                        break;
                    }
                }
            }
        }
    }

    /**
     * @return a lane on which the reference point is between start and end.
     * @throws NetworkException when the reference point of the GTU is not on any of the lanes on which it is registered
     */
    private Lane getReferenceLane() throws NetworkException
    {
        Map<Lane, Length.Rel> positions = this.gtu.positions(this.gtu.getReference());
        for (Lane lane : positions.keySet())
        {
            double posSI = positions.get(lane).si;
            if (posSI >= 0.0 && posSI <= lane.getLength().si)
            {
                return lane;
            }
        }
        throw new NetworkException("The reference point of GTU " + this.gtu
            + " is not on any of the lanes on which it is registered");
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
     * @throws NetworkException when there is an error with the next lanes in the network.
     */
    private HeadwayGTU headway(Length.Rel maxDistance) throws NetworkException
    {
        return headwayGTUSI(maxDistance.getSI());
    }

    /**
     * Determine by what distance the front of this GTU is behind the rear an other GTU, or the rear of this GTU is ahead of the
     * front of an other GTU. Only positive values are returned. This method only looks in the given lane, and not further than
     * the given maxDistance. When no vehicle is found within the given maxDistance,
     * <code>new Length.Rel(Double.MAX_VALUE, METER)</code> is returned. The search will extend into successive lanes if the
     * maxDistance is larger than the length of the lane.<br>
     * <b>Note:</b> Headway is the net headway and calculated on a front-to-back basis.
     * @param lane the lane to look for another GTU
     * @param maxDistance the maximum distance to look for; if positive, the search is forwards; if negative, the search is
     *            backwards
     * @return HeadwayGTU; the headway and the GTU
     * @throws NetworkException when the vehicle's route is inconclusive or vehicles are not registered correctly on their lanes
     */
    private final HeadwayGTU headway(final Lane lane, final Length.Rel maxDistance) throws NetworkException
    {
        Time.Abs when = this.gtu.getSimulator().getSimulatorTime().getTime();
        if (maxDistance.getSI() > 0.0)
        {
            return headwayRecursiveForwardSI(lane, this.gtu.projectedPosition(lane, this.gtu.getFront(), when).getSI(),
                0.0, maxDistance.getSI(), when);
        }
        else
        {
            return headwayRecursiveBackwardSI(lane, this.gtu.projectedPosition(lane, this.gtu.getRear(), when).getSI(),
                0.0, -maxDistance.getSI(), when);
        }
    }

    /**
     * Determine which GTUs are parallel with us on another lane, based on fractional positions. <br>
     * Note: When the GTU that calls the method is also registered on the given lane, it is excluded from the return set.
     * @param lane the lane to look for parallel (partial or full overlapping) GTUs.
     * @param when the future time for which to calculate the headway
     * @return the set of GTUs parallel to us on the other lane (partial overlap counts as parallel), based on fractional
     *         positions, or an empty set when no GTUs were found.
     * @throws NetworkException when the vehicle's route is inconclusive, when vehicles are not registered correctly on their
     *             lanes, or when the given lane is not parallel to one of the lanes where we are registered.
     */
    private final Set<LaneBasedGTU> parallel(final Lane lane, final Time.Abs when) throws NetworkException
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
                for (LaneBasedGTU otherGTU : lane.getGtuList())
                {
                    if (!otherGTU.equals(this))
                    {
                        double gtuFractionFront =
                            Math.max(0.0, otherGTU.fractionalPosition(lane, otherGTU.getFront(), when));
                        double gtuFractionRear =
                            Math.min(1.0, otherGTU.fractionalPosition(lane, otherGTU.getRear(), when));
                        // TODO is this formula for parallel() okay?
                        // TODO should it not be extended with several || clauses?
                        if (gtuFractionFront >= posFractionRear && gtuFractionRear <= posFractionFront)
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
     * @throws NetworkException when the vehicle's route is inconclusive, when vehicles are not registered correctly on their
     *             lanes, or when there are no lanes parallel to one of the lanes where we are registered in the given
     *             direction.
     */
    private final Set<LaneBasedGTU> parallel(final LateralDirectionality lateralDirection, final Time.Abs when)
        throws NetworkException
    {
        Set<LaneBasedGTU> gtuSet = new LinkedHashSet<LaneBasedGTU>();
        for (Lane lane : this.gtu.getLanes().keySet())
        {
            for (Lane adjacentLane : this.accessibleAdjacentLanes.get(lane).get(lateralDirection))
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
        Set<Lane> candidates = this.accessibleAdjacentLanes.get(currentLane).get(lateralDirection);
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
     * @param maxDistanceSI the maximum distance to look for in SI units
     * @return the nearest GTU and the net headway to this GTU in SI units when we have found the GTU, or a null GTU with a
     *         distance of Double.MAX_VALUE meters when no other GTU could not be found within maxDistanceSI meters
     * @throws NetworkException when there is a problem with the geometry of the network
     */
    private HeadwayGTU headwayGTUSI(final double maxDistanceSI) throws NetworkException
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
                    headwayRecursiveForwardSI(lane, this.gtu.position(lane, this.gtu.getFront(), time).getSI(), 0.0,
                        maxDistanceSI, time);
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
                    headwayRecursiveBackwardSI(lane, this.gtu.position(lane, this.gtu.getRear(), time).getSI(), 0.0,
                        -maxDistanceSI, time);
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
     * Calculate the minimum headway, possibly on subsequent lanes, in forward direction.
     * @param lane the lane where we are looking right now
     * @param lanePositionSI from which position on this lane do we start measuring? This is the current position of the GTU
     *            when we measure in the lane where the original GTU is positioned, and 0.0 for each subsequent lane
     * @param cumDistanceSI the distance we have already covered searching on previous lanes
     * @param maxDistanceSI the maximum distance to look for in SI units; stays the same in subsequent calls
     * @param when the current or future time for which to calculate the headway
     * @return the headway in SI units when we have found the GTU, or a null GTU with a distance of Double.MAX_VALUE meters when
     *         no other GTU could not be found within maxDistanceSI meters
     * @throws NetworkException when there is a problem with the geometry of the network
     */
    private HeadwayGTU headwayRecursiveForwardSI(final Lane lane, final double lanePositionSI,
        final double cumDistanceSI, final double maxDistanceSI, final Time.Abs when) throws NetworkException
    {
        LaneBasedGTU otherGTU =
            lane.getGtuAfter(new Length.Rel(lanePositionSI, LengthUnit.SI), RelativePosition.REAR, when);
        if (otherGTU != null)
        {
            double distanceM =
                cumDistanceSI + otherGTU.position(lane, otherGTU.getRear(), when).getSI() - lanePositionSI;
            if (distanceM > 0 && distanceM <= maxDistanceSI)
            {
                return new HeadwayGTU(otherGTU, distanceM);
            }
            return new HeadwayGTU(null, Double.MAX_VALUE);
        }

        // Continue search on successor lanes.
        if (cumDistanceSI + lane.getLength().getSI() - lanePositionSI < maxDistanceSI)
        {
            // is there a successor link?
            if (lane.nextLanes(this.gtu.getGTUType()).size() > 0)
            {
                HeadwayGTU foundMaxGTUDistanceSI = new HeadwayGTU(null, Double.MAX_VALUE);
                for (Lane nextLane : lane.nextLanes(this.gtu.getGTUType()).keySet())
                {
                    // TODO Only follow links on the Route if there is a "real" Route
                    // TODO use new functions of the Navigator
                    // if (this.getRoute() == null || this.getRoute().size() == 0 /* XXX STUB dummy route */
                    // || (this.routeNavigator.getRoute().containsLink((Link) lane.getParentLink())))
                    {
                        double traveledDistanceSI = cumDistanceSI + lane.getLength().getSI() - lanePositionSI;
                        HeadwayGTU closest =
                            headwayRecursiveForwardSI(nextLane, 0.0, traveledDistanceSI, maxDistanceSI, when);
                        if (closest.getDistanceSI() < maxDistanceSI
                            && closest.getDistanceSI() < foundMaxGTUDistanceSI.getDistanceSI())
                        {
                            foundMaxGTUDistanceSI = closest;
                        }
                    }
                }
                return foundMaxGTUDistanceSI;
            }
        }

        // No other GTU was not on one of the current lanes or their successors.
        return new HeadwayGTU(null, Double.MAX_VALUE);
    }

    /**
     * Calculate the minimum headway, possibly on subsequent lanes, in backward direction (so between our back, and the other
     * GTU's front). Note: this method returns a POSITIVE number.
     * @param lane the lane where we are looking right now
     * @param lanePositionSI from which position on this lane do we start measuring? This is the current position of the rear of
     *            the GTU when we measure in the lane where the original GTU is positioned, and lane.getLength() for each
     *            subsequent lane.
     * @param cumDistanceSI the distance we have already covered searching on previous lanes. Note: This is a POSITIVE number.
     * @param maxDistanceSI the maximum distance to look for in SI units; stays the same in subsequent calls. Note: this is a
     *            POSITIVE number.
     * @param when the current or future time for which to calculate the headway
     * @return the headway in SI units when we have found the GTU, or a null GTU with a distance of Double.MAX_VALUE meters when
     *         no other GTU could not be found within maxDistanceSI meters
     * @throws NetworkException when there is a problem with the geometry of the network
     */
    private HeadwayGTU headwayRecursiveBackwardSI(final Lane lane, final double lanePositionSI,
        final double cumDistanceSI, final double maxDistanceSI, final Time.Abs when) throws NetworkException
    {
        LaneBasedGTU otherGTU =
            lane.getGtuBefore(new Length.Rel(lanePositionSI, LengthUnit.SI), RelativePosition.FRONT, when);
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
                    HeadwayGTU closest =
                        headwayRecursiveBackwardSI(prevLane, prevLane.getLength().getSI(), traveledDistanceSI,
                            maxDistanceSI, when);
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
     * @throws NetworkException when there is a problem with the geometry of the network
     */
    private double headwayRecursiveForwardSI(final Lane lane, final double lanePositionSI, final LaneBasedGTU otherGTU,
        final double cumDistanceSI, final double maxDistanceSI, final Time.Abs when) throws NetworkException
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
     * @throws NetworkException when there is a problem with the geometry of the network
     */
    private double headwayRecursiveBackwardSI(final Lane lane, final double lanePositionSI,
        final LaneBasedGTU otherGTU, final double cumDistanceSI, final double maxDistanceSI, final Time.Abs when)
        throws NetworkException
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
     * Build a set of Lanes that is adjacent to the given lane that this GTU can enter, for both lateral directions.
     */
    private void buildAccessibleAdjacentLanes()
    {
        this.accessibleAdjacentLanes.clear();
        for (Lane lane : this.gtu.getLanes().keySet())
        {
            EnumMap<LateralDirectionality, Set<Lane>> adjacentMap = new EnumMap<>(LateralDirectionality.class);
            for (LateralDirectionality lateralDirection : LateralDirectionality.values())
            {
                Set<Lane> adjacentLanes = new HashSet<Lane>(1);
                adjacentLanes.addAll(lane.accessibleAdjacentLanes(lateralDirection, this.gtu.getGTUType()));
                adjacentMap.put(lateralDirection, adjacentLanes);
            }
            this.accessibleAdjacentLanes.put(lane, adjacentMap);
        }
    }

    /**
     * @return timestamp
     */
    public final Time.Abs getTimestamp()
    {
        return this.timestamp;
    }

    /**
     * @return gtu
     */
    public final LaneBasedGTU getGtu()
    {
        return this.gtu;
    }

    /**
     * @return laneListForward
     */
    public final List<Lane> getLaneListForward()
    {
        return this.laneListForward;
    }

    /**
     * @return forwardHeadwayGTU
     */
    public final HeadwayGTU getForwardHeadwayGTU()
    {
        return this.forwardHeadwayGTU;
    }

    /**
     * @return backwardHeadwayGTU
     */
    public final HeadwayGTU getBackwardHeadwayGTU()
    {
        return this.backwardHeadwayGTU;
    }

    /**
     * @return accessibleAdjacentLanes
     */
    public final Map<Lane, EnumMap<LateralDirectionality, Set<Lane>>> getAccessibleAdjacentLanes()
    {
        return this.accessibleAdjacentLanes;
    }

    /**
     * @return parallelGTUs
     */
    public final Map<Lane, Set<LaneBasedGTU>> getParallelGTUs()
    {
        return this.parallelGTUs;
    }

    /**
     * @return speedLimit
     */
    public final Speed getSpeedLimit()
    {
        return this.speedLimit;
    }

    /** {@inheritDoc} */
    @Override
    public Set<PerceivedObject> getPerceivedObjects()
    {
        // TODO getPerceivedObjects() in LanePerception
        return new HashSet<PerceivedObject>();
    }

}
