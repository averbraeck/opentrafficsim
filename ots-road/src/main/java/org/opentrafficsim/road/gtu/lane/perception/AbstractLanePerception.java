package org.opentrafficsim.road.gtu.lane.perception;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.Throw;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypes;
import org.opentrafficsim.core.gtu.perception.TimeStampedObject;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.perception.PerceivedObject;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.tactical.AbstractLaneBasedTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.LanePathInfo;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneDirection;
import org.opentrafficsim.road.network.speed.SpeedLimitProspect;
import org.opentrafficsim.road.network.speed.SpeedLimitTypes;

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
 */
public abstract class AbstractLanePerception implements LanePerception
{
    /** */
    private static final long serialVersionUID = 20151128L;

    /** The lane based GTU for which this perception module stores information. */
    private LaneBasedGTU gtu;

    /** The forward headway and (leader) object. */
    private TimeStampedObject<Headway> forwardHeadway;

    /** The backward headway and (follower) object. */
    private TimeStampedObject<Headway> backwardHeadway;

    /** The minimum speed limit of all lanes where the GTU is registered. */
    private TimeStampedObject<Speed> speedLimit;

    /** The adjacent lanes that are accessible for the GTU at the left side. */
    private TimeStampedObject<Map<Lane, Set<Lane>>> accessibleAdjacentLanesLeft;

    /** The adjacent lanes that are accessible for the GTU at the right side. */
    private TimeStampedObject<Map<Lane, Set<Lane>>> accessibleAdjacentLanesRight;

    /** The objects parallel to us on the left side. */
    private TimeStampedObject<Collection<Headway>> parallelHeadwaysLeft;

    /** The objects parallel to us on the right side. */
    private TimeStampedObject<Collection<Headway>> parallelHeadwaysRight;

    /** The GTUs on the left side. */
    private TimeStampedObject<Collection<Headway>> neighboringHeadwaysLeft;

    /** The GTUs on the right side. */
    private TimeStampedObject<Collection<Headway>> neighboringHeadwaysRight;

    /** The lanes and path we expect to take if we do not change lanes. */
    private TimeStampedObject<LanePathInfo> lanePathInfo;

    /** The structure of the lanes in front of the GTU. */
    // TODO private LaneStructure laneStructure;

    /**
     * Create a new LanePerception module. Because the constructor is often called inside the constructor of a GTU, this
     * constructor does not ask for the pointer to the GTU, as it is often impossible to provide at the time of construction.
     * Use the setter of the GTU instead.
     */
    public AbstractLanePerception()
    {
        super();
    }

    /** {@inheritDoc} */
    @Override
    public final void setGTU(final LaneBasedGTU laneBasedGtu)
    {
        this.gtu = laneBasedGtu;
    }

    /**
     * Check whether the GTU has been initialized, and returns the current time.
     * @return the current time according to the simulator.
     * @throws GTUException when the GTU was not initialized yet.
     */
    private Time getTimestamp() throws GTUException
    {
        if (this.gtu == null)
        {
            throw new GTUException("gtu value has not been initialized for LanePerception when perceiving.");
        }
        return this.gtu.getSimulator().getSimulatorTime().getTime();
    }

    /**
     * @throws GTUException when the GTU was not initialized yet.
     * @throws NetworkException when the speed limit for a GTU type cannot be retrieved from the network.
     * @throws ParameterException in case of not being able to retrieve parameter ParameterTypes.LOOKAHEAD
     */
    public final void updateLanePathInfo() throws GTUException, NetworkException, ParameterException
    {
        Time timestamp = getTimestamp();
        this.lanePathInfo =
            new TimeStampedObject<LanePathInfo>(AbstractLaneBasedTacticalPlanner.buildLanePathInfo(this.gtu, this.gtu
                .getBehavioralCharacteristics().getParameter(ParameterTypes.LOOKAHEAD)), timestamp);
    }

    /** {@inheritDoc} */
    @Override
    public final void updateSpeedLimit() throws GTUException, NetworkException
    {
        Time timestamp = getTimestamp();
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

    /** {@inheritDoc} */
    @Override
    public final void updateForwardHeadway() throws GTUException, NetworkException, ParameterException
    {
        Time timestamp = getTimestamp();
        if (this.lanePathInfo == null || this.lanePathInfo.getTimestamp().ne(timestamp))
        {
            updateLanePathInfo();
        }
        Length maximumForwardHeadway = this.gtu.getBehavioralCharacteristics().getParameter(ParameterTypes.LOOKAHEAD);
        this.forwardHeadway = new TimeStampedObject<>(forwardHeadway(maximumForwardHeadway), timestamp);
    }

    /** {@inheritDoc} */
    @Override
    public final void updateBackwardHeadway() throws GTUException, NetworkException, ParameterException
    {
        Time timestamp = getTimestamp();
        Length maximumReverseHeadway = this.gtu.getBehavioralCharacteristics().getParameter(ParameterTypes.LOOKBACKOLD);
        this.backwardHeadway = new TimeStampedObject<>(backwardHeadway(maximumReverseHeadway), timestamp);
    }

    /** {@inheritDoc} */
    @Override
    public final void updateAccessibleAdjacentLanesLeft() throws GTUException
    {
        Time timestamp = getTimestamp();
        Map<Lane, Set<Lane>> accessibleAdjacentLanesMap = new HashMap<>();
        for (Lane lane : this.gtu.getLanes().keySet())
        {
            Set<Lane> adjacentLanes = new HashSet<Lane>(1);
            adjacentLanes.addAll(lane.accessibleAdjacentLanes(LateralDirectionality.LEFT, this.gtu.getGTUType()));
            accessibleAdjacentLanesMap.put(lane, adjacentLanes);
        }
        this.accessibleAdjacentLanesLeft = new TimeStampedObject<>(accessibleAdjacentLanesMap, timestamp);
    }

    /** {@inheritDoc} */
    @Override
    public final void updateAccessibleAdjacentLanesRight() throws GTUException
    {
        Time timestamp = getTimestamp();
        Map<Lane, Set<Lane>> accessibleAdjacentLanesMap = new HashMap<>();
        for (Lane lane : this.gtu.getLanes().keySet())
        {
            Set<Lane> adjacentLanes = new HashSet<Lane>(1);
            adjacentLanes.addAll(lane.accessibleAdjacentLanes(LateralDirectionality.RIGHT, this.gtu.getGTUType()));
            accessibleAdjacentLanesMap.put(lane, adjacentLanes);
        }
        this.accessibleAdjacentLanesRight = new TimeStampedObject<>(accessibleAdjacentLanesMap, timestamp);
    }

    /** {@inheritDoc} */
    @Override
    public final void updateParallelHeadwaysLeft() throws GTUException
    {
        Time timestamp = getTimestamp();
        if (this.accessibleAdjacentLanesLeft == null || !timestamp.equals(this.accessibleAdjacentLanesLeft.getTimestamp()))
        {
            updateAccessibleAdjacentLanesLeft();
        }
        Set<Headway> parallelHeadwaySet = new HashSet<>();
        for (Lane lane : this.accessibleAdjacentLanesLeft.getObject().keySet())
        {
            for (Lane adjacentLane : this.accessibleAdjacentLanesLeft.getObject().get(lane))
            {
                parallelHeadwaySet.addAll(parallel(adjacentLane, timestamp));
            }
        }
        this.parallelHeadwaysLeft = new TimeStampedObject<>(parallelHeadwaySet, timestamp);
    }

    /** {@inheritDoc} */
    @Override
    public final void updateParallelHeadwaysRight() throws GTUException
    {
        Time timestamp = getTimestamp();
        if (this.accessibleAdjacentLanesRight == null || !timestamp.equals(this.accessibleAdjacentLanesRight.getTimestamp()))
        {
            updateAccessibleAdjacentLanesRight();
        }
        Set<Headway> parallelHeadwaySet = new HashSet<>();
        for (Lane lane : this.accessibleAdjacentLanesRight.getObject().keySet())
        {
            for (Lane adjacentLane : this.accessibleAdjacentLanesRight.getObject().get(lane))
            {
                parallelHeadwaySet.addAll(parallel(adjacentLane, timestamp));
            }
        }
        this.parallelHeadwaysRight = new TimeStampedObject<>(parallelHeadwaySet, timestamp);
    }

    /** {@inheritDoc} */
    @Override
    public final void updateLaneTrafficLeft() throws GTUException, NetworkException, ParameterException
    {
        Time timestamp = getTimestamp();
        if (this.accessibleAdjacentLanesLeft == null || !timestamp.equals(this.accessibleAdjacentLanesLeft.getTimestamp()))
        {
            updateAccessibleAdjacentLanesLeft();
        }

        if (this.parallelHeadwaysLeft == null || !timestamp.equals(this.parallelHeadwaysLeft.getTimestamp()))
        {
            updateParallelHeadwaysLeft();
        }

        // for the accessible lanes, see who is ahead of us and in front of us
        Length maximumForwardHeadway = this.gtu.getBehavioralCharacteristics().getParameter(ParameterTypes.LOOKAHEAD);
        Length maximumReverseHeadway = this.gtu.getBehavioralCharacteristics().getParameter(ParameterTypes.LOOKBACKOLD);
        this.neighboringHeadwaysLeft =
            new TimeStampedObject<>(collectNeighborLaneTraffic(LateralDirectionality.LEFT, timestamp, maximumForwardHeadway,
                maximumReverseHeadway), timestamp);
    }

    /** {@inheritDoc} */
    @Override
    public final void updateLaneTrafficRight() throws GTUException, NetworkException, ParameterException
    {
        Time timestamp = getTimestamp();
        if (this.accessibleAdjacentLanesRight == null || !timestamp.equals(this.accessibleAdjacentLanesRight.getTimestamp()))
        {
            updateAccessibleAdjacentLanesRight();
        }

        if (this.parallelHeadwaysRight == null || !timestamp.equals(this.parallelHeadwaysRight.getTimestamp()))
        {
            updateParallelHeadwaysRight();
        }

        // for the accessible lanes, see who is ahead of us and in front of us
        Length maximumForwardHeadway = this.gtu.getBehavioralCharacteristics().getParameter(ParameterTypes.LOOKAHEAD);
        Length maximumReverseHeadway = this.gtu.getBehavioralCharacteristics().getParameter(ParameterTypes.LOOKBACKOLD);
        this.neighboringHeadwaysRight =
            new TimeStampedObject<>(collectNeighborLaneTraffic(LateralDirectionality.RIGHT, timestamp,
                maximumForwardHeadway, maximumReverseHeadway), timestamp);
    }

    /** {@inheritDoc} */
    @Override
    public final Map<Lane, Set<Lane>> accessibleAdjacentLaneMap(final LateralDirectionality lateralDirection)
    {
        return lateralDirection.equals(LateralDirectionality.LEFT) ? this.accessibleAdjacentLanesLeft.getObject()
            : this.accessibleAdjacentLanesRight.getObject();
    }

    /** {@inheritDoc} */
    @Override
    public final Collection<Headway> getNeighboringHeadways(final LateralDirectionality lateralDirection)
    {
        return lateralDirection.equals(LateralDirectionality.LEFT) ? this.parallelHeadwaysLeft.getObject()
            : this.parallelHeadwaysRight.getObject();
    }

    /** {@inheritDoc} */
    @Override
    public final Collection<Headway> getParallelHeadways(final LateralDirectionality lateralDirection)
    {
        return lateralDirection.equals(LateralDirectionality.LEFT) ? this.neighboringHeadwaysLeft.getObject()
            : this.neighboringHeadwaysRight.getObject();
    }

    /**************************************************************************************************************************/
    /**************************************************** HEADWAY ALGORITHMS **************************************************/
    /**************************************************************************************************************************/

    /**
     * Determine which GTU is in front of this GTU. This method looks in all lanes where this GTU is registered, and not further
     * than the value of the given maxDistance. The minimum headway is returned of all Lanes where the GTU is registered. When
     * no GTU is found within the given maxDistance, a HeadwayGTU with <b>null</b> as the gtuId and maxDistance as the distance
     * is returned. The search will extend into successive lanes if the maxDistance is larger than the remaining length on the
     * lane. When Lanes (or underlying CrossSectionLinks) diverge, a route planner may be used to determine which kinks and
     * lanes to take into account and which ones not. When the Lanes (or underlying CrossSectionLinks) converge, "parallel"
     * traffic is not taken into account in the headway calculation. Instead, gap acceptance algorithms or their equivalent
     * should guide the merging behavior.<br>
     * <b>Note:</b> Headway is the net headway and calculated on a front-to-back basis.
     * @param maxDistance the maximum distance to look for the nearest GTU; positive values search forwards; negative values
     *            search backwards
     * @return HeadwayGTU; the headway and the GTU information
     * @throws GTUException when there is an error with the next lanes in the network.
     * @throws NetworkException when there is a problem with the route planner
     */
    private Headway forwardHeadway(final Length maxDistance) throws GTUException, NetworkException
    {
        LanePathInfo lpi = getLanePathInfo();
        return forwardHeadway(lpi, maxDistance);
    }

    /**
     * Determine which GTU is in front of this GTU. This method uses a given lanePathInfo to look forward, but not further than
     * the value of the given maxDistance. The minimum headway is returned of all Lanes where the GTU is registered. When no GTU
     * is found within the given maxDistance, a HeadwayGTU with <b>null</b> as the gtuId and maxDistance as the distance is
     * returned. The search will extend into successive lanes if the maxDistance is larger than the remaining length on the
     * lane. When Lanes (or underlying CrossSectionLinks) diverge, a route planner may be used to determine which kinks and
     * lanes to take into account and which ones not. When the Lanes (or underlying CrossSectionLinks) converge, "parallel"
     * traffic is not taken into account in the headway calculation. Instead, gap acceptance algorithms or their equivalent
     * should guide the merging behavior.<br>
     * <b>Note:</b> Headway is the net headway and calculated on a front-to-back basis.
     * @param lpi the lanePathInfo object that informs the headway algorithm in which lanes to look, and from which position on
     *            the first lane.
     * @param maxDistance the maximum distance to look for the nearest GTU; positive values search forwards; negative values
     *            search backwards
     * @return HeadwayGTU; the headway and the GTU information
     * @throws GTUException when there is an error with the next lanes in the network.
     * @throws NetworkException when there is a problem with the route planner
     */
    private Headway forwardHeadway(final LanePathInfo lpi, final Length maxDistance) throws GTUException, NetworkException
    {
        Throw.when(maxDistance.le(Length.ZERO), GTUException.class, "forwardHeadway: maxDistance should be positive");

        int ldIndex = 0;
        LaneDirection ld = lpi.getReferenceLaneDirection();
        double gtuPosFrontSI = lpi.getReferencePosition().si;
        if (lpi.getReferenceLaneDirection().getDirection().isPlus())
        {
            gtuPosFrontSI += this.gtu.getFront().getDx().si;
        }
        else
        {
            gtuPosFrontSI -= this.gtu.getFront().getDx().si;
        }

        // TODO end of lanepath

        while ((gtuPosFrontSI > ld.getLane().getLength().si || gtuPosFrontSI < 0.0)
            && ldIndex < lpi.getLaneDirectionList().size() - 1)
        {
            ldIndex++;
            if (ld.getDirection().isPlus()) // e.g. 1005 on length of lane = 1000
            {
                if (lpi.getLaneDirectionList().get(ldIndex).getDirection().isPlus())
                {
                    gtuPosFrontSI -= ld.getLane().getLength().si;
                }
                else
                {
                    gtuPosFrontSI = lpi.getLaneDirectionList().get(ldIndex).getLane().getLength().si - gtuPosFrontSI;
                }
                ld = lpi.getLaneDirectionList().get(ldIndex);
            }
            else
            // e.g. -5 on lane of whatever length
            {
                if (lpi.getLaneDirectionList().get(ldIndex).getDirection().isPlus())
                {
                    gtuPosFrontSI += ld.getLane().getLength().si;
                }
                else
                {
                    gtuPosFrontSI += lpi.getLaneDirectionList().get(ldIndex).getLane().getLength().si;
                }
                ld = lpi.getLaneDirectionList().get(ldIndex);
            }
        }

        double maxDistanceSI = maxDistance.si;
        Time time = this.gtu.getSimulator().getSimulatorTime().getTime();

        // look forward based on the provided lanePathInfo.
        Headway closest = headwayLane(ld, gtuPosFrontSI, 0.0, time);
        if (closest != null)
        {
            if (closest.getDistance().si > maxDistanceSI)
            {
                return new HeadwayDistance(maxDistanceSI);
            }
            return closest;
        }
        double cumDistSI = ld.getDirection().isPlus() ? ld.getLane().getLength().si - gtuPosFrontSI : gtuPosFrontSI;
        for (int i = ldIndex + 1; i < lpi.getLaneDirectionList().size(); i++)
        {
            ld = lpi.getLaneDirectionList().get(i);
            closest = headwayLane(ld, ld.getDirection().isPlus() ? 0.0 : ld.getLane().getLength().si, cumDistSI, time);
            if (closest != null)
            {
                if (closest.getDistance().si > maxDistanceSI)
                {
                    return new HeadwayDistance(maxDistanceSI);
                }
                return closest;
            }
            cumDistSI += ld.getLane().getLength().si;
        }
        return new HeadwayDistance(maxDistanceSI);
    }

    /**
     * Determine the positive headway on a lane, or null if no GTU can be found on this lane.
     * @param laneDirection the lane and direction to look
     * @param startPosSI the start position to look from in meters
     * @param cumDistSI the cumulative distance that has already been observed on other lanes
     * @param now the current time to determine the GTU positions on the lane
     * @return the HeadwayGTU, containing information on a GTU that is ahead of the given start position, or null if no GTU can
     *         be found on this lane
     * @throws GTUException when the GTUs ahead on the lane cannot be determined
     */
    private Headway headwayLane(final LaneDirection laneDirection, final double startPosSI, final double cumDistSI,
        final Time now) throws GTUException
    {
        Lane lane = laneDirection.getLane();
        LaneBasedGTU laneBasedGTU =
            lane.getGtuAhead(new Length(startPosSI, LengthUnit.SI), laneDirection.getDirection(), RelativePosition.REAR, now);
        if (laneBasedGTU == null)
        {
            return null;
        }
        double distanceSI = Math.abs(laneBasedGTU.position(lane, laneBasedGTU.getRear()).si - startPosSI);
        return new HeadwayGTUSimple(laneBasedGTU.getId(), laneBasedGTU.getGTUType(), new Length(cumDistSI + distanceSI,
            LengthUnit.SI), laneBasedGTU.getLength(), laneBasedGTU.getSpeed(), laneBasedGTU.getAcceleration());
    }

    /**
     * Determine which GTU is behind this GTU. This method looks in all lanes where this GTU is registered, and not further back
     * than the absolute value of the given maxDistance. The minimum net headway is returned of all Lanes where the GTU is
     * registered. When no GTU is found within the given maxDistance, <b>null</b> is returned. The search will extend into
     * successive lanes if the maxDistance is larger than the remaining length on the lane. When Lanes (or underlying
     * CrossSectionLinks) diverge, the headway algorithms have to look at multiple Lanes and return the minimum headway in each
     * of the Lanes. When the Lanes (or underlying CrossSectionLinks) converge, "parallel" traffic is not taken into account in
     * the headway calculation. Instead, gap acceptance algorithms or their equivalent should guide the merging behavior.<br>
     * <b>Note:</b> Headway is the net headway and calculated on a back-to-front basis.
     * @param maxDistance the maximum distance to look for the nearest GTU; it should have a negative value to search backwards
     * @return HeadwayGTU; the headway and the GTU information
     * @throws GTUException when there is an error with the next lanes in the network.
     * @throws NetworkException when there is a problem with the route planner
     */
    private Headway backwardHeadway(final Length maxDistance) throws GTUException, NetworkException
    {
        Throw.when(maxDistance.ge(Length.ZERO), GTUException.class, "backwardHeadway: maxDistance should be negative");
        Time time = this.gtu.getSimulator().getSimulatorTime().getTime();
        double maxDistanceSI = maxDistance.si;
        Headway foundHeadway = new HeadwayDistance(-maxDistanceSI);
        for (Lane lane : this.gtu.positions(this.gtu.getRear()).keySet())
        {
            Headway closest =
                headwayRecursiveBackwardSI(lane, this.gtu.getLanes().get(lane), this.gtu.position(lane, this.gtu.getRear(),
                    time).getSI(), 0.0, -maxDistanceSI, time);
            if (closest.getDistance().si < -maxDistanceSI && closest.getDistance().si < -foundHeadway.getDistance().si)
            {
                foundHeadway = closest;
            }
        }
        if (foundHeadway instanceof AbstractHeadwayGTU)
        {
            return new HeadwayGTUSimple(foundHeadway.getId(), ((AbstractHeadwayGTU) foundHeadway).getGtuType(), foundHeadway
                .getDistance().multiplyBy(-1.0), foundHeadway.getLength(), foundHeadway.getSpeed(), null);
        }
        if (foundHeadway instanceof HeadwayDistance)
        {
            return new HeadwayDistance(foundHeadway.getDistance().multiplyBy(-1.0));
        }
        // TODO allow observation of other objects as well.
        throw new GTUException("backwardHeadway not implemented yet for other object types than GTU");
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
    private Headway headwayRecursiveBackwardSI(final Lane lane, final GTUDirectionality direction,
        final double lanePositionSI, final double cumDistanceSI, final double maxDistanceSI, final Time when)
        throws GTUException
    {
        LaneBasedGTU otherGTU =
            lane.getGtuBehind(new Length(lanePositionSI, LengthUnit.SI), direction, RelativePosition.FRONT, when);
        if (otherGTU != null)
        {
            double distanceM = cumDistanceSI + lanePositionSI - otherGTU.position(lane, otherGTU.getFront(), when).getSI();
            if (distanceM > 0 && distanceM <= maxDistanceSI)
            {
                return new HeadwayGTUSimple(otherGTU.getId(), otherGTU.getGTUType(), new Length(distanceM, LengthUnit.SI),
                    otherGTU.getLength(), otherGTU.getSpeed(), null);
            }
            return new HeadwayDistance(Double.MAX_VALUE);
        }

        // Continue search on predecessor lanes.
        if (cumDistanceSI + lanePositionSI < maxDistanceSI)
        {
            // is there a predecessor link?
            if (lane.prevLanes(this.gtu.getGTUType()).size() > 0)
            {
                Headway foundMaxGTUDistanceSI = new HeadwayDistance(Double.MAX_VALUE);
                for (Lane prevLane : lane.prevLanes(this.gtu.getGTUType()).keySet())
                {
                    // What is behind us is INDEPENDENT of the followed route!
                    double traveledDistanceSI = cumDistanceSI + lanePositionSI;
                    // WRONG - adapt method to forward perception method!
                    Headway closest =
                        headwayRecursiveBackwardSI(prevLane, direction, prevLane.getLength().getSI(), traveledDistanceSI,
                            maxDistanceSI, when);
                    if (closest.getDistance().si < maxDistanceSI
                        && closest.getDistance().si < foundMaxGTUDistanceSI.getDistance().si)
                    {
                        foundMaxGTUDistanceSI = closest;
                    }
                }
                return foundMaxGTUDistanceSI;
            }
        }

        // No other GTU was not on one of the current lanes or their successors.
        return new HeadwayDistance(Double.MAX_VALUE);
    }

    /**************************************************************************************************************************/
    /************************************************ ADJACENT LANE TRAFFIC ***************************************************/
    /**************************************************************************************************************************/

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
    private Collection<Headway> parallel(final Lane lane, final Time when) throws GTUException
    {
        Collection<Headway> headwayCollection = new LinkedHashSet<Headway>();
        for (Lane l : this.gtu.getLanes().keySet())
        {
            // only take lanes that we can compare based on a shared design line
            if (l.getParentLink().equals(lane.getParentLink()))
            {
                // compare based on fractional positions.
                double posFractionRef = this.gtu.fractionalPosition(l, this.gtu.getReference(), when);
                double posFractionFront =
                    Math.max(0.0, posFractionRef + this.gtu.getFront().getDx().si / lane.getLength().si);
                double posFractionRear = Math.min(1.0, posFractionRef + this.gtu.getRear().getDx().si / lane.getLength().si);
                // double posFractionFront = Math.max(0.0, this.gtu.fractionalPosition(l, this.gtu.getFront(), when));
                // double posFractionRear = Math.min(1.0, this.gtu.fractionalPosition(l, this.gtu.getRear(), when));
                double posMin = Math.min(posFractionFront, posFractionRear);
                double posMax = Math.max(posFractionFront, posFractionRear);
                for (LaneBasedGTU otherGTU : lane.getGtuList())
                {
                    if (!otherGTU.equals(this)) // TODO
                    {
                        /*- cater for: *-----*         *-----*       *-----*       *----------*
                         *                *-----*    *----*      *------------*       *-----*
                         * where the GTUs can each drive in two directions (!)
                         */
                        double gtuFractionRef = otherGTU.fractionalPosition(lane, otherGTU.getReference(), when);
                        double gtuFractionFront =
                            Math.max(0.0, gtuFractionRef + otherGTU.getFront().getDx().si / lane.getLength().si);
                        double gtuFractionRear =
                            Math.min(1.0, gtuFractionRef + otherGTU.getRear().getDx().si / lane.getLength().si);
                        double gtuMin = Math.min(gtuFractionFront, gtuFractionRear);
                        double gtuMax = Math.max(gtuFractionFront, gtuFractionRear);
                        // TODO calculate real overlaps
                        Length overlapFront = new Length(1.0, LengthUnit.SI);
                        Length overlap = new Length(1.0, LengthUnit.SI);
                        Length overlapRear = new Length(1.0, LengthUnit.SI);
                        if ((gtuMin >= posMin && gtuMin <= posMax) || (gtuMax >= posMin && gtuMax <= posMax)
                            || (posMin >= gtuMin && posMin <= gtuMax) || (posMax >= gtuMin && posMax <= gtuMax))
                        {
                            headwayCollection.add(new HeadwayGTUSimple(otherGTU.getId(), otherGTU.getGTUType(),
                                overlapFront, overlap, overlapRear, otherGTU.getLength(), otherGTU.getSpeed(), otherGTU
                                    .getAcceleration()));
                        }
                    }
                }
            }
        }
        return headwayCollection;
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
    private Collection<Headway> parallel(final LateralDirectionality lateralDirection, final Time when) throws GTUException
    {
        Collection<Headway> gtuSet = new LinkedHashSet<Headway>();
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
     * FIXME In other places in OTS LEFT is positive (and RIGHT is negative). This should be made more consistent.
     * @param currentLane the lane to look for the best accessible adjacent lane
     * @param lateralDirection the direction (LEFT, RIGHT) to look at
     * @param longitudinalPosition Length; the position of the GTU along <cite>currentLane</cite>
     * @return the lane if it is accessible, or null if there is no lane, it is not accessible, or the driving direction does
     *         not match.
     */
    public final Lane bestAccessibleAdjacentLane(final Lane currentLane, final LateralDirectionality lateralDirection,
        final Length longitudinalPosition)
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
        double widestSeen = Double.NEGATIVE_INFINITY;
        for (Lane lane : candidates)
        {
            if (lane.getWidth(longitudinalPosition).getSI() > widestSeen)
            {
                widestSeen = lane.getWidth(longitudinalPosition).getSI();
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
     * @throws ParameterException in case of a parameter problem
     */
    private Collection<Headway> collectNeighborLaneTraffic(final LateralDirectionality directionality, final Time when,
        final Length maximumForwardHeadway, final Length maximumReverseHeadway) throws NetworkException, GTUException,
        ParameterException
    {
        Collection<Headway> result = new HashSet<Headway>();
        for (Headway p : parallel(directionality, when))
        {
            // TODO expand for other types of Headways
            result.add(new HeadwayGTUSimple(p.getId(), ((AbstractHeadwayGTU) p).getGtuType(), new Length(Double.NaN,
                LengthUnit.SI), p.getLength(), p.getSpeed(), p.getAcceleration()));
        }

        // forward
        for (Lane adjacentLane : accessibleAdjacentLaneMap(directionality).get(getLanePathInfo().getReferenceLane()))
        {
            LanePathInfo lpiAdjacent = buildLanePathInfoAdjacent(adjacentLane, directionality, when);
            Headway leader = forwardHeadway(lpiAdjacent, maximumForwardHeadway);
            if (null != leader.getId() && !result.contains(leader))
            {
                result.add(leader);
            }
        }

        // backward
        for (Lane lane : this.gtu.getLanes().keySet())
        {
            for (Lane adjacentLane : accessibleAdjacentLaneMap(directionality).get(lane))
            {
                Headway follower =
                    headwayRecursiveBackwardSI(adjacentLane, this.gtu.getLanes().get(lane), this.gtu.projectedPosition(
                        adjacentLane, this.gtu.getRear(), when).getSI(), 0.0, -maximumReverseHeadway.getSI(), when);
                if (follower instanceof AbstractHeadwayGTU)
                {
                    boolean found = false;
                    for (Headway headway : result)
                    {
                        if (headway.getId().equals(follower.getId()))
                        {
                            found = true;
                        }
                    }
                    if (!found)
                    {
                        result.add(new HeadwayGTUSimple(follower.getId(), ((AbstractHeadwayGTU) follower).getGtuType(),
                            follower.getDistance().multiplyBy(-1.0), follower.getLength(), follower.getSpeed(), null));
                    }
                }
                else if (follower instanceof HeadwayDistance) // always add for potential lane drop
                {
                    result.add(new HeadwayDistance(follower.getDistance().multiplyBy(-1.0)));
                }
                else
                {
                    throw new GTUException(
                        "collectNeighborLaneTraffic not yet suited to observe obstacles on neighboring lanes");
                }
            }
        }
        return result;
    }

    /**
     * Find a lanePathInfo left or right of the current LanePath.
     * @param adjacentLane the start adjacent lane for which we calculate the LanePathInfo
     * @param direction LateralDirectionality; either <cite>LateralDirectionality.LEFT</cite>, or
     *            <cite>LateralDirectionality.RIGHT</cite>
     * @param when DoubleScalar.Abs&lt;TimeUnit&gt;; the (current) time
     * @return the adjacent LanePathInfo
     * @throws GTUException when the GTU was not initialized yet.
     * @throws NetworkException when the speed limit for a GTU type cannot be retrieved from the network.
     * @throws ParameterException in case of a parameter problem
     */
    private LanePathInfo buildLanePathInfoAdjacent(final Lane adjacentLane, final LateralDirectionality direction,
        final Time when) throws GTUException, NetworkException, ParameterException
    {
        if (this.lanePathInfo == null || this.lanePathInfo.getTimestamp().ne(when))
        {
            updateLanePathInfo();
        }
        LanePathInfo lpi = getLanePathInfo();
        List<LaneDirection> laneDirectionList = new ArrayList<>();
        laneDirectionList.add(new LaneDirection(adjacentLane, lpi.getReferenceLaneDirection().getDirection()));
        Length referencePosition = this.gtu.projectedPosition(adjacentLane, this.gtu.getReference(), when);
        for (int i = 1; i < lpi.getLaneDirectionList().size(); i++)
        {
            LaneDirection ld = lpi.getLaneDirectionList().get(i);
            Set<Lane> accessibleLanes = ld.getLane().accessibleAdjacentLanes(direction, this.gtu.getGTUType());
            Lane adjLane = null;
            for (Lane lane : accessibleLanes)
            {
                if (lane.getParentLink().equals(ld.getLane().getParentLink()))
                {
                    adjLane = lane;
                }
            }
            if (adjLane == null)
            {
                break;
            }
            laneDirectionList.add(new LaneDirection(adjLane, ld.getDirection()));
        }
        return new LanePathInfo(null, laneDirectionList, referencePosition);
    }

    /**************************************************************************************************************************/
    /*************************************************** GETTERS FOR THE INFORMATION ******************************************/
    /**************************************************************************************************************************/

    /** {@inheritDoc} */
    @Override
    public final LaneBasedGTU getGTU()
    {
        return this.gtu;
    }

    /**
     * Retrieve the last perceived lane path info.
     * @return LanePathInfo
     */
    public final LanePathInfo getLanePathInfo()
    {
        return this.lanePathInfo.getObject();
    }

    /** {@inheritDoc} */
    @Override
    public final Headway getForwardHeadway()
    {
        return this.forwardHeadway.getObject();
    }

    /** {@inheritDoc} */
    @Override
    public final Headway getBackwardHeadway()
    {
        return this.backwardHeadway.getObject();
    }

    /** {@inheritDoc} */
    @Override
    public final Map<Lane, Set<Lane>> getAccessibleAdjacentLanesLeft()
    {
        return this.accessibleAdjacentLanesLeft.getObject();
    }

    /** {@inheritDoc} */
    @Override
    public final Map<Lane, Set<Lane>> getAccessibleAdjacentLanesRight()
    {
        return this.accessibleAdjacentLanesRight.getObject();
    }

    /** {@inheritDoc} */
    @Override
    public final Collection<Headway> getNeighboringHeadwaysLeft()
    {
        return this.neighboringHeadwaysLeft.getObject();
    }

    /** {@inheritDoc} */
    @Override
    public final Collection<Headway> getNeighboringHeadwaysRight()
    {
        return this.neighboringHeadwaysRight.getObject();
    }

    /** {@inheritDoc} */
    @Override
    public final Collection<Headway> getParallelHeadwaysLeft()
    {
        return this.parallelHeadwaysLeft.getObject();
    }

    /** {@inheritDoc} */
    @Override
    public final Collection<Headway> getParallelHeadwaysRight()
    {
        return this.parallelHeadwaysRight.getObject();
    }

    /** {@inheritDoc} */
    @Override
    public final Speed getSpeedLimit()
    {
        return this.speedLimit.getObject();
    }

    /** {@inheritDoc} */
    @Override
    public final Set<PerceivedObject> getPerceivedObjects()
    {
        // TODO getPerceivedObjects() in LanePerception
        return new HashSet<PerceivedObject>();
    }

    /** {@inheritDoc} */
    @Override
    public final TimeStampedObject<Headway> getTimeStampedForwardHeadway()
    {
        return this.forwardHeadway;
    }

    /** {@inheritDoc} */
    @Override
    public final TimeStampedObject<Headway> getTimeStampedBackwardHeadway()
    {
        return this.backwardHeadway;
    }

    /** {@inheritDoc} */
    @Override
    public final TimeStampedObject<Map<Lane, Set<Lane>>> getTimeStampedAccessibleAdjacentLanesLeft()
    {
        return this.accessibleAdjacentLanesLeft;
    }

    /** {@inheritDoc} */
    @Override
    public final TimeStampedObject<Map<Lane, Set<Lane>>> getTimeStampedAccessibleAdjacentLanesRight()
    {
        return this.accessibleAdjacentLanesRight;
    }

    /** {@inheritDoc} */
    @Override
    public final TimeStampedObject<Collection<Headway>> getTimeStampedNeighboringHeadwaysLeft()
    {
        return this.neighboringHeadwaysLeft;
    }

    /** {@inheritDoc} */
    @Override
    public final TimeStampedObject<Collection<Headway>> getTimeStampedNeighboringHeadwaysRight()
    {
        return this.neighboringHeadwaysRight;
    }

    /** {@inheritDoc} */
    @Override
    public final TimeStampedObject<Collection<Headway>> getTimeStampedParallelHeadwaysLeft()
    {
        return this.parallelHeadwaysLeft;
    }

    /** {@inheritDoc} */
    @Override
    public final TimeStampedObject<Collection<Headway>> getTimeStampedParallelHeadwaysRight()
    {
        return this.parallelHeadwaysRight;
    }

    /** {@inheritDoc} */
    @Override
    public final TimeStampedObject<Speed> getTimeStampedSpeedLimit()
    {
        return this.speedLimit;
    }

    /** {@inheritDoc} */
    @Override
    public final TimeStampedObject<Collection<PerceivedObject>> getTimeStampedPerceivedObjects() throws GTUException
    {
        // TODO getPerceivedObjects() in LanePerception
        return new TimeStampedObject<Collection<PerceivedObject>>(new HashSet<PerceivedObject>(), getTimestamp());
    }

    /** {@inheritDoc} */
    @Override
    public SortedSet<AbstractHeadwayGTU> getFirstLeaders(LateralDirectionality lat)
    {
        if (lat == null)
        {
            return getLeaders(RelativeLane.CURRENT);
        }
        else if (lat.isLeft())
        {
            return getLeaders(RelativeLane.LEFT);
        }
        return getLeaders(RelativeLane.RIGHT);
    }

    /** {@inheritDoc} */
    @Override
    public SortedSet<AbstractHeadwayGTU> getFirstFollowers(LateralDirectionality lat)
    {
        if (lat == null)
        {
            return getFollowers(RelativeLane.CURRENT);
        }
        else if (lat.isLeft())
        {
            return getFollowers(RelativeLane.LEFT);
        }
        return getFollowers(RelativeLane.RIGHT);
    }

    /** {@inheritDoc} */
    @Override
    public boolean existsGtuAlongside(LateralDirectionality lat)
    {
        for (Headway headway : getNeighboringHeadways(lat))
        {
            if (headway.isParallel())
            {
                return true;
            }
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public SortedSet<AbstractHeadwayGTU> getLeaders(RelativeLane lane)
    {
        SortedSet<AbstractHeadwayGTU> leaders = new TreeSet<>();
        if (lane.isCurrent())
        {
            if (getForwardHeadway() instanceof AbstractHeadwayGTU)
            {
                leaders.add((AbstractHeadwayGTU) getForwardHeadway());
            }
        }
        else
        {
            for (Headway headway : getNeighboringHeadways(lane.getLateralDirectionality()))
            {
                if (headway instanceof AbstractHeadwayGTU && headway.isAhead())
                {
                    leaders.add((AbstractHeadwayGTU) headway);
                }
            }
        }
        return leaders;
    }

    /** {@inheritDoc} */
    @Override
    public SortedSet<AbstractHeadwayGTU> getFollowers(RelativeLane lane)
    {
        SortedSet<AbstractHeadwayGTU> followers = new TreeSet<>();
        if (lane.isCurrent())
        {
            if (getBackwardHeadway() instanceof AbstractHeadwayGTU)
            {
                followers.add((AbstractHeadwayGTU) getBackwardHeadway());
            }
        }
        else
        {
            for (Headway headway : getNeighboringHeadways(lane.getLateralDirectionality()))
            {
                if (headway instanceof AbstractHeadwayGTU && headway.isBehind())
                {
                    followers.add((AbstractHeadwayGTU) headway);
                }
            }
        }
        return followers;
    }

    /** {@inheritDoc} */
    @Override
    public SortedSet<InfrastructureLaneChangeInfo> getInfrastructureLaneChangeInfo(RelativeLane lane)
    {
        return new TreeSet<>();
    }
    
    /** {@inheritDoc} */
    @Override
    public int getSplitNumber(InfrastructureLaneChangeInfo info)
    {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public SpeedLimitProspect getSpeedLimitProspect(RelativeLane lane)
    {
        SpeedLimitProspect slp = new SpeedLimitProspect();
        slp.addSpeedInfo(Length.ZERO, SpeedLimitTypes.FIXED_SIGN, getSpeedLimit());
        slp.addSpeedInfo(Length.ZERO, SpeedLimitTypes.MAX_VEHICLE_SPEED, getGTU().getMaximumVelocity());
        return slp;
    }

    /** {@inheritDoc} */
    @Override
    public Length getLegalLaneChangePossibility(RelativeLane fromLane, LateralDirectionality lat)
    {
        return Length.ZERO;
    }

    /** {@inheritDoc} */
    @Override
    public Length getPhysicalLaneChangePossibility(RelativeLane fromLane, LateralDirectionality lat)
    {
        return Length.ZERO;
    }

    /** {@inheritDoc} */
    @Override
    public SortedSet<RelativeLane> getCurrentCrossSection()
    {
        return new TreeSet<>();
    }

    /** {@inheritDoc} */
    @Override
    public SortedSet<HeadwayTrafficLight> getTrafficLights()
    {
        return new TreeSet<>();
    }

    /** {@inheritDoc} */
    @Override
    public SortedSet<HeadwayConflict> getIntersectionConflicts(RelativeLane lane)
    {
        return new TreeSet<>();
    }

    /** {@inheritDoc} */
    @Override
    public TimeStampedObject<SortedSet<AbstractHeadwayGTU>> getTimeStampedFirstLeaders(LateralDirectionality lat)
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public TimeStampedObject<SortedSet<AbstractHeadwayGTU>> getTimeStampedFirstFollowers(LateralDirectionality lat)
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public TimeStampedObject<Boolean> existsGtuAlongsideTimeStamped(LateralDirectionality lat)
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public TimeStampedObject<SortedSet<AbstractHeadwayGTU>> getTimeStampedLeaders(RelativeLane lane)
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public TimeStampedObject<SortedSet<AbstractHeadwayGTU>> getTimeStampedFollowers(RelativeLane lane)
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public TimeStampedObject<SortedSet<InfrastructureLaneChangeInfo>> getTimeStampedInfrastructureLaneChangeInfo(
        RelativeLane lane)
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public TimeStampedObject<Integer> getTimeStampedSplitNumber(InfrastructureLaneChangeInfo info)
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public TimeStampedObject<SpeedLimitProspect> getTimeStampedSpeedLimitProspect(RelativeLane lane)
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public TimeStampedObject<Length> getTimeStampedLegalLaneChangePossibility(RelativeLane fromLane,
        LateralDirectionality lat)
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public TimeStampedObject<Length> getTimeStampedPhysicalLaneChangePossibility(RelativeLane fromLane,
        LateralDirectionality lat)
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public TimeStampedObject<SortedSet<RelativeLane>> getTimeStampedCurrentCrossSection()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public TimeStampedObject<SortedSet<HeadwayTrafficLight>> getTimeStampedTrafficLights()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public TimeStampedObject<SortedSet<HeadwayConflict>> getTimeStampedIntersectionConflicts(RelativeLane lane)
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void updateFirstLeaders()
    {
    }

    /** {@inheritDoc} */
    @Override
    public void updateFirstFollowers()
    {
    }

    /** {@inheritDoc} */
    @Override
    public void updateGtuAlongside()
    {
    }

    /** {@inheritDoc} */
    @Override
    public void updateLeaders()
    {
    }

    /** {@inheritDoc} */
    @Override
    public void updateFollowers()
    {
    }

    /** {@inheritDoc} */
    @Override
    public void updateInfrastructureLaneChangeInfo()
    {
    }

    /** {@inheritDoc} */
    @Override
    public void updateSplitNumber()
    {
    }

    /** {@inheritDoc} */
    @Override
    public void updateSpeedLimitProspect()
    {
    }

    /** {@inheritDoc} */
    @Override
    public void updateLegalLaneChangePossibility()
    {
    }

    /** {@inheritDoc} */
    @Override
    public void updatePhysicalLaneChangePossibility()
    {
    }

    /** {@inheritDoc} */
    @Override
    public void updateCurrentCrossSection()
    {
    }

    /** {@inheritDoc} */
    @Override
    public void updateTrafficLights()
    {
    }

    /** {@inheritDoc} */
    @Override
    public void updateIntersectionConflicts()
    {
    }

}
