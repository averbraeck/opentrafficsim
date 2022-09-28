package org.opentrafficsim.road.gtu.lane.perception.categories;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.TimeStampedObject;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeLength;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.AbstractHeadwayGTU;
import org.opentrafficsim.road.gtu.lane.perception.headway.GTUStatus;
import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayDistance;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTUSimple;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayObject;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayTrafficLight;
import org.opentrafficsim.road.gtu.lane.tactical.AbstractLaneBasedTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.LanePathInfo;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneDirection;
import org.opentrafficsim.road.network.lane.object.LaneBasedObject;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLight;

/**
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class DirectDefaultSimplePerception extends LaneBasedAbstractPerceptionCategory implements DefaultSimplePerception
{

    /** */
    private static final long serialVersionUID = 20160811L;

    /** Look ahead parameter type. */
    protected static final ParameterTypeLength LOOKAHEAD = ParameterTypes.LOOKAHEAD;

    /** Look back parameter type. */
    protected static final ParameterTypeLength LOOKBACKOLD = ParameterTypes.LOOKBACKOLD;

    /** The forward headway and (leader) GTU. */
    private TimeStampedObject<Headway> forwardHeadwayGTU;

    /** The forward headway and (leader) object. */
    private TimeStampedObject<Headway> forwardHeadwayObject;

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

    /**
     * @param perception LanePerception; perception
     */
    public DirectDefaultSimplePerception(final LanePerception perception)
    {
        super(perception);
    }

    /** {@inheritDoc} */
    @Override
    public final void updateLanePathInfo() throws GtuException, NetworkException, ParameterException
    {
        Time timestamp = getTimestamp();
        this.lanePathInfo = new TimeStampedObject<>(
                AbstractLaneBasedTacticalPlanner.buildLanePathInfo(getGtu(), getGtu().getParameters().getParameter(LOOKAHEAD)),
                timestamp);
    }

    /** {@inheritDoc} */
    @Override
    public final void updateForwardHeadwayGTU() throws GtuException, NetworkException, ParameterException
    {
        Time timestamp = getTimestamp();
        if (this.lanePathInfo == null || this.lanePathInfo.getTimestamp().ne(timestamp))
        {
            updateLanePathInfo();
        }
        Length maximumForwardHeadway = getGtu().getParameters().getParameter(LOOKAHEAD);
        this.forwardHeadwayGTU = new TimeStampedObject<>(forwardHeadway(maximumForwardHeadway, true), timestamp);
    }

    /** {@inheritDoc} */
    @Override
    public final void updateForwardHeadwayObject() throws GtuException, NetworkException, ParameterException
    {
        Time timestamp = getTimestamp();
        if (this.lanePathInfo == null || this.lanePathInfo.getTimestamp().ne(timestamp))
        {
            updateLanePathInfo();
        }
        Length maximumForwardHeadway = getGtu().getParameters().getParameter(LOOKAHEAD);
        this.forwardHeadwayObject = new TimeStampedObject<>(forwardHeadway(maximumForwardHeadway, false), timestamp);
    }

    /** {@inheritDoc} */
    @Override
    public final void updateBackwardHeadway() throws GtuException, ParameterException, NetworkException
    {
        Time timestamp = getTimestamp();
        Length maximumReverseHeadway = getGtu().getParameters().getParameter(LOOKBACKOLD);
        this.backwardHeadway = new TimeStampedObject<>(backwardHeadway(maximumReverseHeadway), timestamp);
    }

    /** {@inheritDoc} */
    @Override
    public final void updateAccessibleAdjacentLanesLeft() throws GtuException
    {
        Time timestamp = getTimestamp();
        Map<Lane, Set<Lane>> accessibleAdjacentLanesMap = new LinkedHashMap<>();
        for (Lane lane : getGtu().positions(getGtu().getReference()).keySet())
        {
            Set<Lane> adjacentLanes = new LinkedHashSet<>(1);
            adjacentLanes.addAll(lane.accessibleAdjacentLanesLegal(LateralDirectionality.LEFT, getGtu().getGtuType(),
                    getGtu().getDirection(lane)));
            accessibleAdjacentLanesMap.put(lane, adjacentLanes);
        }
        this.accessibleAdjacentLanesLeft = new TimeStampedObject<>(accessibleAdjacentLanesMap, timestamp);
    }

    /** {@inheritDoc} */
    @Override
    public final void updateAccessibleAdjacentLanesRight() throws GtuException
    {
        Time timestamp = getTimestamp();
        Map<Lane, Set<Lane>> accessibleAdjacentLanesMap = new LinkedHashMap<>();
        for (Lane lane : getGtu().positions(getGtu().getReference()).keySet())
        {
            Set<Lane> adjacentLanes = new LinkedHashSet<>(1);
            adjacentLanes.addAll(lane.accessibleAdjacentLanesLegal(LateralDirectionality.RIGHT, getGtu().getGtuType(),
                    getGtu().getDirection(lane)));
            accessibleAdjacentLanesMap.put(lane, adjacentLanes);
        }
        this.accessibleAdjacentLanesRight = new TimeStampedObject<>(accessibleAdjacentLanesMap, timestamp);
    }

    /** {@inheritDoc} */
    @Override
    public final void updateNeighboringHeadwaysLeft() throws GtuException, ParameterException, NetworkException
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
        Length maximumForwardHeadway = getGtu().getParameters().getParameter(LOOKAHEAD);
        Length maximumReverseHeadway = getGtu().getParameters().getParameter(LOOKBACKOLD);
        this.neighboringHeadwaysLeft = new TimeStampedObject<>(
                collectNeighborLaneTraffic(LateralDirectionality.LEFT, timestamp, maximumForwardHeadway, maximumReverseHeadway),
                timestamp);
    }

    /** {@inheritDoc} */
    @Override
    public final void updateNeighboringHeadwaysRight() throws GtuException, ParameterException, NetworkException
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
        Length maximumForwardHeadway = getGtu().getParameters().getParameter(LOOKAHEAD);
        Length maximumReverseHeadway = getGtu().getParameters().getParameter(LOOKBACKOLD);
        this.neighboringHeadwaysRight = new TimeStampedObject<>(collectNeighborLaneTraffic(LateralDirectionality.RIGHT,
                timestamp, maximumForwardHeadway, maximumReverseHeadway), timestamp);
    }

    /** {@inheritDoc} */
    @Override
    public final void updateNeighboringHeadways(final LateralDirectionality lateralDirection)
            throws GtuException, ParameterException, NetworkException
    {
        if (lateralDirection.equals(LateralDirectionality.LEFT))
        {
            updateNeighboringHeadwaysLeft();
        }
        else
        {
            updateNeighboringHeadwaysRight();
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void updateParallelHeadwaysLeft() throws GtuException
    {
        Time timestamp = getTimestamp();
        if (this.accessibleAdjacentLanesLeft == null || !timestamp.equals(this.accessibleAdjacentLanesLeft.getTimestamp()))
        {
            updateAccessibleAdjacentLanesLeft();
        }
        Set<Headway> parallelHeadwaySet = new LinkedHashSet<>();
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
    public final void updateParallelHeadwaysRight() throws GtuException
    {
        Time timestamp = getTimestamp();
        if (this.accessibleAdjacentLanesRight == null || !timestamp.equals(this.accessibleAdjacentLanesRight.getTimestamp()))
        {
            updateAccessibleAdjacentLanesRight();
        }
        Set<Headway> parallelHeadwaySet = new LinkedHashSet<>();
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
    public final void updateParallelHeadways(final LateralDirectionality lateralDirection) throws GtuException
    {
        if (lateralDirection.equals(LateralDirectionality.LEFT))
        {
            updateParallelHeadwaysLeft();
        }
        else
        {
            updateParallelHeadwaysRight();
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void updateSpeedLimit() throws GtuException, NetworkException
    {
        Time timestamp = getTimestamp();
        // assess the speed limit where we are right now
        Lane lane = getGtu().getReferencePosition().getLane();
        this.speedLimit = new TimeStampedObject<>(lane.getSpeedLimit(getGtu().getGtuType()), timestamp);
    }

    /** {@inheritDoc} */
    @Override
    public final LanePathInfo getLanePathInfo()
    {
        return this.lanePathInfo.getObject();
    }

    /** {@inheritDoc} */
    @Override
    public final Headway getForwardHeadwayGTU()
    {
        return this.forwardHeadwayGTU.getObject();
    }

    /** {@inheritDoc} */
    @Override
    public final Headway getForwardHeadwayObject()
    {
        return this.forwardHeadwayObject.getObject();
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
    public final Map<Lane, Set<Lane>> getAccessibleAdjacentLanes(final LateralDirectionality lateralDirection)
    {
        return lateralDirection.equals(LateralDirectionality.LEFT) ? this.accessibleAdjacentLanesLeft.getObject()
                : this.accessibleAdjacentLanesRight.getObject();
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
    public final Collection<Headway> getNeighboringHeadways(final LateralDirectionality lateralDirection)
    {
        return lateralDirection.equals(LateralDirectionality.LEFT) ? this.neighboringHeadwaysLeft.getObject()
                : this.neighboringHeadwaysRight.getObject();
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
    public final Collection<Headway> getParallelHeadways(final LateralDirectionality lateralDirection)
    {
        return lateralDirection.equals(LateralDirectionality.LEFT) ? this.parallelHeadwaysLeft.getObject()
                : this.parallelHeadwaysRight.getObject();
    }

    /** {@inheritDoc} */
    @Override
    public final Speed getSpeedLimit()
    {
        return this.speedLimit.getObject();
    }

    /**
     * @return TimeStamped forwardHeadway, the forward headway and first object (GTU) in front
     */
    public final TimeStampedObject<Headway> getTimeStampedForwardHeadwayGTU()
    {
        return this.forwardHeadwayGTU;
    }

    /**
     * @return TimeStamped forwardHeadway, the forward headway and first object (not a GTU) in front
     */
    public final TimeStampedObject<Headway> getTimeStampedForwardHeadwayObject()
    {
        return this.forwardHeadwayObject;
    }

    /**
     * @return TimeStamped backwardHeadwayGTU, the backward headway and first object (e.g., a GTU) behind
     */
    public final TimeStampedObject<Headway> getTimeStampedBackwardHeadway()
    {
        return this.backwardHeadway;
    }

    /**
     * @return TimeStamped accessibleAdjacentLanesLeft, the accessible adjacent lanes on the left
     */
    public final TimeStampedObject<Map<Lane, Set<Lane>>> getTimeStampedAccessibleAdjacentLanesLeft()
    {
        return this.accessibleAdjacentLanesLeft;
    }

    /**
     * @return TimeStamped accessibleAdjacentLanesRight, the accessible adjacent lanes on the right
     */
    public final TimeStampedObject<Map<Lane, Set<Lane>>> getTimeStampedAccessibleAdjacentLanesRight()
    {
        return this.accessibleAdjacentLanesRight;
    }

    /**
     * @param lateralDirection LateralDirectionality; the direction to return the accessible adjacent lanes for
     * @return TimeStamped accessibleAdjacentLanesRight, the accessible adjacent lanes on the right
     */
    public final TimeStampedObject<Map<Lane, Set<Lane>>> getTimeStampedAccessibleAdjacentLanes(
            final LateralDirectionality lateralDirection)
    {
        return lateralDirection.equals(LateralDirectionality.LEFT) ? this.accessibleAdjacentLanesLeft
                : this.accessibleAdjacentLanesRight;
    }

    /**
     * @return TimeStamped neighboringHeadwaysLeft, the objects (e.g., GTUs) in parallel, in front and behind on the left
     *         neighboring lane, with their headway relative to our GTU, and information about the status of the adjacent
     *         objects
     */
    public final TimeStampedObject<Collection<Headway>> getTimeStampedNeighboringHeadwaysLeft()
    {
        return this.neighboringHeadwaysLeft;
    }

    /**
     * @return TimeStamped neighboringHeadwaysRight, the objects (e.g., GTUs) in parallel, in front and behind on the right
     *         neighboring lane, with their headway relative to our GTU, and information about the status of the adjacent
     *         objects
     */
    public final TimeStampedObject<Collection<Headway>> getTimeStampedNeighboringHeadwaysRight()
    {
        return this.neighboringHeadwaysRight;
    }

    /**
     * @param lateralDirection LateralDirectionality; the direction to return the neighboring headways for
     * @return TimeStamped neighboringHeadwaysRight, the objects (e.g., GTUs) in parallel, in front and behind on the right
     *         neighboring lane, with their headway relative to our GTU, and information about the status of the adjacent
     *         objects
     */
    public final TimeStampedObject<Collection<Headway>> getTimeStampedNeighboringHeadways(
            final LateralDirectionality lateralDirection)
    {
        return lateralDirection.equals(LateralDirectionality.LEFT) ? this.neighboringHeadwaysLeft
                : this.neighboringHeadwaysRight;
    }

    /**
     * @return TimeStamped parallelHeadwaysLeft, the parallel objects (e.g., GTUs) on the left, with information about their
     *         status and parallel overlap with our GTU.
     */
    public final TimeStampedObject<Collection<Headway>> getTimeStampedParallelHeadwaysLeft()
    {
        return this.parallelHeadwaysLeft;
    }

    /**
     * @return TimeStamped parallelHeadwaysRight, the parallel objects (e.g., GTUs) on the right, with information about their
     *         status and parallel overlap with our GTU.
     */
    public final TimeStampedObject<Collection<Headway>> getTimeStampedParallelHeadwaysRight()
    {
        return this.parallelHeadwaysRight;
    }

    /**
     * @param lateralDirection LateralDirectionality; the direction to return the parallel headways for
     * @return TimeStamped parallelHeadwaysRight, the parallel objects (e.g., GTUs) on the right, with information about their
     *         status and parallel overlap with our GTU.
     */
    public final TimeStampedObject<Collection<Headway>> getTimeStampedParallelHeadways(
            final LateralDirectionality lateralDirection)
    {
        return lateralDirection.equals(LateralDirectionality.LEFT) ? this.parallelHeadwaysLeft : this.parallelHeadwaysRight;
    }

    /**
     * @return TimeStamped speedLimit
     */
    public final TimeStampedObject<Speed> getTimeStampedSpeedLimit()
    {
        return this.speedLimit;
    }

    /**
     * Retrieve the time stamped last perceived lane path info.
     * @return LanePathInfo time stamped last perceived lane path info
     */
    public final TimeStampedObject<LanePathInfo> getTimeStampedLanePathInfo()
    {
        return this.lanePathInfo;
    }

    /** {@inheritDoc} */
    @Override
    public final Lane bestAccessibleAdjacentLane(final Lane currentLane, final LateralDirectionality lateralDirection,
            final Length longitudinalPosition)
    {
        Set<Lane> candidates = getAccessibleAdjacentLanes(lateralDirection).get(currentLane);
        if (candidates == null || candidates.isEmpty())
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

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "DirectDefaultSimplePerception";
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
     * @param maxDistance the maximum distance to look for the nearest Gtu; positive values search forwards; negative values
     *            search backwards
     * @param gtu boolean; look for gtu if true, for an object if false
     * @return HeadwayGtu; the headway and the GTU information
     * @throws GtuException when there is an error with the next lanes in the network.
     * @throws NetworkException when there is a problem with the route planner
     */
    private Headway forwardHeadway(final Length maxDistance, final boolean gtu) throws GtuException, NetworkException
    {
        LanePathInfo lpi = getLanePathInfo();
        return forwardHeadway(lpi, maxDistance, gtu);
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
     * @param lpi LanePathInfo; the lanePathInfo object that informs the headway algorithm in which lanes to look, and from
     *            which position on the first lane.
     * @param maxDistance the maximum distance to look for the nearest Gtu; positive values search forwards; negative values
     *            search backwards
     * @param gtu boolean; look for gtu if true, for an object if false
     * @return HeadwayGtu; the headway and the GTU information
     * @throws GtuException when there is an error with the next lanes in the network.
     * @throws NetworkException when there is a problem with the route planner
     */
    private Headway forwardHeadway(final LanePathInfo lpi, final Length maxDistance, final boolean gtu)
            throws GtuException, NetworkException
    {
        Throw.when(maxDistance.le0(), GtuException.class, "forwardHeadway: maxDistance should be positive");

        int ldIndex = 0;
        LaneDirection ld = lpi.getReferenceLaneDirection();
        double gtuPosFrontSI = lpi.getReferencePosition().si;
        if (lpi.getReferenceLaneDirection().getDirection().isPlus())
        {
            gtuPosFrontSI += getGtu().getFront().getDx().si;
        }
        else
        {
            gtuPosFrontSI -= getGtu().getFront().getDx().si;
        }

        // TODO end of lanepath

        while ((gtuPosFrontSI > ld.getLane().getLength().si || gtuPosFrontSI < 0.0)
                && ldIndex < lpi.getLaneDirectionList().size() - 1)
        {
            ldIndex++;
            if (ld.getDirection().isPlus()) // e.g. 1005 on length of lane = 1000
            {
                gtuPosFrontSI -= ld.getLane().getLength().si; // First subtract the length of the lane that the GTU is leaving
            }
            else
            // e.g. -5 on lane of whatever length
            {
                gtuPosFrontSI *= -1.0;
            }
            if (lpi.getLaneDirectionList().get(ldIndex).getDirection().isMinus())
            {
                gtuPosFrontSI = lpi.getLaneDirectionList().get(ldIndex).getLane().getLength().si - gtuPosFrontSI;
            }
            ld = lpi.getLaneDirectionList().get(ldIndex);
        }

        double maxDistanceSI = maxDistance.si;
        Time time = getGtu().getSimulator().getSimulatorAbsTime();

        // look forward based on the provided lanePathInfo.
        Headway closest = headwayLane(ld, gtuPosFrontSI, 0.0, time, gtu);
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
            closest = headwayLane(ld, ld.getDirection().isPlus() ? 0.0 : ld.getLane().getLength().si, cumDistSI, time, gtu);
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
     * Determine the positive headway on a lane, or null if no GTU or blocking object can be found on this lane.
     * @param laneDirection LaneDirection; the lane and direction to look
     * @param startPosSI double; the start position to look from in meters
     * @param cumDistSI double; the cumulative distance that has already been observed on other lanes
     * @param now Time; the current time to determine the GTU positions on the lane
     * @return the HeadwayGTU, containing information on a GTU that is ahead of the given start position, or null if no GTU can
     *         be found on this lane
     * @param gtu boolean; look for gtu if true, for an object if false
     * @throws GtuException when the GTUs ahead on the lane cannot be determined
     */
    private Headway headwayLane(final LaneDirection laneDirection, final double startPosSI, final double cumDistSI,
            final Time now, final boolean gtu) throws GtuException
    {
        Lane lane = laneDirection.getLane();

        if (gtu)
        {
            LaneBasedGTU laneBasedGTU = lane.getGtuAhead(new Length(startPosSI, LengthUnit.SI), laneDirection.getDirection(),
                    RelativePosition.REAR, now);
            if (laneBasedGTU == null)
            {
                return null;
            }
            double gtuDistanceSI = Math.abs(laneBasedGTU.position(lane, laneBasedGTU.getRear()).si - startPosSI);
            return new HeadwayGTUSimple(laneBasedGTU.getId(), laneBasedGTU.getGtuType(),
                    new Length(cumDistSI + gtuDistanceSI, LengthUnit.SI), laneBasedGTU.getLength(), laneBasedGTU.getWidth(),
                    laneBasedGTU.getSpeed(), laneBasedGTU.getAcceleration(), null, getGtuStatus(laneBasedGTU));
        }

        else

        {
            List<LaneBasedObject> laneBasedObjects =
                    lane.getObjectAhead(new Length(startPosSI, LengthUnit.SI), laneDirection.getDirection());
            if (laneBasedObjects == null)
            {
                return null;
            }
            double objectDistanceSI = Math.abs(laneBasedObjects.get(0).getLongitudinalPosition().si - startPosSI);
            LaneBasedObject lbo = laneBasedObjects.get(0);

            // handle the traffic light
            if (lbo instanceof TrafficLight)
            {
                TrafficLight tl = (TrafficLight) lbo;
                if (tl.getTrafficLightColor().isRed())
                {
                    if (cumDistSI + objectDistanceSI > breakingDistance(MAX_RED_DECELERATION, getGtu().getSpeed()).si)
                    {
                        return new HeadwayTrafficLight(tl, new Length(cumDistSI + objectDistanceSI, LengthUnit.SI));
                    }
                    return new HeadwayTrafficLight(tl, new Length(cumDistSI + objectDistanceSI, LengthUnit.SI));
                }
                if (tl.getTrafficLightColor().isYellow())
                {
                    // double maxDecel = -MAX_YELLOW_DECELERATION.si; // was 2.09;
                    // double brakingTime = getGtu().getSpeed().si / maxDecel;
                    // double brakingDistanceSI =
                    // getGtu().getSpeed().si * brakingTime - 0.5 * maxDecel * brakingTime * brakingTime;
                    if (cumDistSI + objectDistanceSI > breakingDistance(MAX_YELLOW_DECELERATION, getGtu().getSpeed()).si)
                    {
                        return new HeadwayTrafficLight(tl, new Length(cumDistSI + objectDistanceSI, LengthUnit.SI));
                    }
                }
                if (tl.getTrafficLightColor().isRed())
                {
                    System.err.println(
                            "Not braking for " + tl.getTrafficLightColor() + " because that would require excessive braking");
                }
                return null;
            }

            // other objects are always blocking, we assume
            return new HeadwayObject(laneBasedObjects.get(0).getId(), new Length(cumDistSI + objectDistanceSI, LengthUnit.SI));
        }
    }

    /**
     * Determine the braking distance.
     * @param deceleration Acceleration; the applied deceleration (should have a negative value)
     * @param initialSpeed Speed; the initial speed
     * @return double; the breaking distance
     */
    private Length breakingDistance(final Acceleration deceleration, final Speed initialSpeed)
    {
        double a = -deceleration.si;
        double brakingTime = initialSpeed.si / a;
        return new Length(0.5 * a * brakingTime * brakingTime, LengthUnit.SI);
    }

    /**
     * Returns a set of statuses for the GTU.
     * @param gtu LaneBasedGtu; gtu
     * @return set of statuses for the GTU
     */
    private GTUStatus[] getGtuStatus(final LaneBasedGTU gtu)
    {
        if (gtu.getTurnIndicatorStatus().isLeft())
        {
            return new GTUStatus[] {GTUStatus.LEFT_TURNINDICATOR};
        }
        if (gtu.getTurnIndicatorStatus().isRight())
        {
            return new GTUStatus[] {GTUStatus.RIGHT_TURNINDICATOR};
        }
        if (gtu.getTurnIndicatorStatus().isHazard())
        {
            return new GTUStatus[] {GTUStatus.EMERGENCY_LIGHTS};
        }
        return new GTUStatus[0];
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
     * @param maxDistance the maximum distance to look for the nearest Gtu; it should have a negative value to search backwards
     * @return HeadwayGtu; the headway and the GTU information
     * @throws GtuException when there is an error with the next lanes in the network.
     * @throws NetworkException when there is a problem with the route planner
     */
    private Headway backwardHeadway(final Length maxDistance) throws GtuException, NetworkException
    {
        Throw.when(maxDistance.ge0(), GtuException.class, "backwardHeadway: maxDistance should be negative");
        Time time = getGtu().getSimulator().getSimulatorAbsTime();
        double maxDistanceSI = maxDistance.si;
        Headway foundHeadway = new HeadwayDistance(-maxDistanceSI);
        for (Lane lane : getGtu().positions(getGtu().getRear()).keySet())
        {
            Headway closest = headwayRecursiveBackwardSI(lane, getGtu().getDirection(lane),
                    getGtu().position(lane, getGtu().getRear(), time).getSI(), 0.0, -maxDistanceSI, time);
            if (closest.getDistance().si < -maxDistanceSI
                    && closest.getDistance().si < /* NOT - */foundHeadway.getDistance().si)
            {
                foundHeadway = closest;
            }
        }
        if (foundHeadway instanceof AbstractHeadwayGTU)
        {
            return new HeadwayGTUSimple(foundHeadway.getId(), ((AbstractHeadwayGTU) foundHeadway).getGtuType(),
                    foundHeadway.getDistance().neg(), foundHeadway.getLength(), ((AbstractHeadwayGTU) foundHeadway).getWidth(),
                    foundHeadway.getSpeed(), foundHeadway.getAcceleration(), null);
        }
        if (foundHeadway instanceof HeadwayDistance)
        {
            return new HeadwayDistance(foundHeadway.getDistance().neg());
        }
        // TODO allow observation of other objects as well.
        throw new GtuException("backwardHeadway not implemented yet for other object types than GTU");
    }

    /**
     * Calculate the minimum headway, possibly on subsequent lanes, in backward direction (so between our back, and the other
     * GTU's front). Note: this method returns a POSITIVE number.
     * @param lane Lane; the lane where we are looking right now
     * @param direction GTUDirectionality; the direction we are driving on that lane
     * @param lanePositionSI double; from which position on this lane do we start measuring? This is the current position of the
     *            rear of the GTU when we measure in the lane where the original GTU is positioned, and lane.getLength() for
     *            each subsequent lane.
     * @param cumDistanceSI double; the distance we have already covered searching on previous lanes. Note: This is a POSITIVE
     *            number.
     * @param maxDistanceSI the maximum distance to look for in SI units; stays the same in subsequent calls. Note: this is a
     *            POSITIVE number.
     * @param when Time; the current or future time for which to calculate the headway
     * @return the headway in SI units when we have found the GTU, or a null GTU with a distance of Double.MAX_VALUE meters when
     *         no other GTU could not be found within maxDistanceSI meters
     * @throws GtuException when there is a problem with the geometry of the network
     */
    private Headway headwayRecursiveBackwardSI(final Lane lane, final GTUDirectionality direction, final double lanePositionSI,
            final double cumDistanceSI, final double maxDistanceSI, final Time when) throws GtuException
    {
        LaneBasedGTU otherGTU =
                lane.getGtuBehind(new Length(lanePositionSI, LengthUnit.SI), direction, RelativePosition.FRONT, when);
        if (otherGTU != null)
        {
            double distanceM = cumDistanceSI + lanePositionSI - otherGTU.position(lane, otherGTU.getFront(), when).getSI();
            if (distanceM > 0 && distanceM <= maxDistanceSI)
            {
                return new HeadwayGTUSimple(otherGTU.getId(), otherGTU.getGtuType(), new Length(distanceM, LengthUnit.SI),
                        otherGTU.getLength(), otherGTU.getWidth(), otherGTU.getSpeed(), otherGTU.getAcceleration(), null);
            }
            return new HeadwayDistance(Double.MAX_VALUE);
        }

        // Continue search on predecessor lanes.
        if (cumDistanceSI + lanePositionSI < maxDistanceSI)
        {
            // is there a predecessor link?
            if (lane.prevLanes(getGtu().getGtuType()).size() > 0)
            {
                Headway foundMaxGTUDistanceSI = new HeadwayDistance(Double.MAX_VALUE);
                for (Lane prevLane : lane.prevLanes(getGtu().getGtuType()).keySet())
                {
                    // What is behind us is INDEPENDENT of the followed route!
                    double traveledDistanceSI = cumDistanceSI + lanePositionSI;
                    // WRONG - adapt method to forward perception method!
                    Headway closest = headwayRecursiveBackwardSI(prevLane, direction, prevLane.getLength().getSI(),
                            traveledDistanceSI, maxDistanceSI, when);
                    if (closest.getDistance().si < maxDistanceSI
                            && closest.getDistance().si < foundMaxGTUDistanceSI.getDistance().si)
                    {
                        foundMaxGTUDistanceSI = closest;
                    }
                }
                return foundMaxGTUDistanceSI;
            }
        }

        // No other GTU was not on one of the current lanes or their predecessors.
        return new HeadwayDistance(Double.MAX_VALUE);
    }

    /**************************************************************************************************************************/
    /************************************************ ADJACENT LANE TRAFFIC ***************************************************/
    /**************************************************************************************************************************/

    /**
     * Determine which GTUs are parallel with us on another lane, based on fractional positions. <br>
     * Note: When the GTU that calls the method is also registered on the given lane, it is excluded from the return set.
     * @param lane Lane; the lane to look for parallel (partial or full overlapping) GTUs.
     * @param when Time; the future time for which to calculate the headway
     * @return the set of GTUs parallel to us on the other lane (partial overlap counts as parallel), based on fractional
     *         positions, or an empty set when no GTUs were found.
     * @throws GtuException when the vehicle's route is inconclusive, when vehicles are not registered correctly on their lanes,
     *             or when the given lane is not parallel to one of the lanes where we are registered.
     */
    private Collection<Headway> parallel(final Lane lane, final Time when) throws GtuException
    {
        Collection<Headway> headwayCollection = new LinkedHashSet<>();
        for (Lane l : getGtu().positions(getGtu().getReference()).keySet())
        {
            // only take lanes that we can compare based on a shared design line
            if (l.getParentLink().equals(lane.getParentLink()))
            {
                // compare based on fractional positions.
                double posFractionRef = getGtu().fractionalPosition(l, getGtu().getReference(), when);
                double posFractionFront = Math.max(0.0, posFractionRef + getGtu().getFront().getDx().si / lane.getLength().si);
                double posFractionRear = Math.min(1.0, posFractionRef + getGtu().getRear().getDx().si / lane.getLength().si);
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
                        if ((gtuMin >= posMin && gtuMin <= posMax) || (gtuMax >= posMin && gtuMax <= posMax)
                                || (posMin >= gtuMin && posMin <= gtuMax) || (posMax >= gtuMin && posMax <= gtuMax))
                        {
                            // TODO calculate real overlaps
                            Length overlapFront = new Length(1.0, LengthUnit.SI);
                            Length overlap = new Length(1.0, LengthUnit.SI);
                            Length overlapRear = new Length(1.0, LengthUnit.SI);
                            headwayCollection.add(new HeadwayGTUSimple(otherGTU.getId(), otherGTU.getGtuType(), overlapFront,
                                    overlap, overlapRear, otherGTU.getLength(), otherGTU.getWidth(), otherGTU.getSpeed(),
                                    otherGTU.getAcceleration(), null, getGtuStatus(otherGTU)));
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
     * @param lateralDirection LateralDirectionality; the direction of the adjacent lane(s) to look for parallel (partial or
     *            full overlapping) GTUs.
     * @param when Time; the future time for which to calculate the headway
     * @return the set of GTUs parallel to us on other lane(s) in the given direction (partial overlap counts as parallel),
     *         based on fractional positions, or an empty set when no GTUs were found.
     * @throws GtuException when the vehicle's route is inconclusive, when vehicles are not registered correctly on their lanes,
     *             or when there are no lanes parallel to one of the lanes where we are registered in the given direction.
     */
    private Collection<Headway> parallel(final LateralDirectionality lateralDirection, final Time when) throws GtuException
    {
        Collection<Headway> gtuSet = new LinkedHashSet<>();
        for (Lane lane : getGtu().positions(getGtu().getReference()).keySet())
        {
            for (Lane adjacentLane : getAccessibleAdjacentLanes(lateralDirection).get(lane))
            {
                gtuSet.addAll(parallel(adjacentLane, when));
            }
        }
        return gtuSet;
    }

    /**
     * Collect relevant traffic in adjacent lanes. Parallel traffic is included with headway equal to Double.NaN.
     * @param directionality LateralDirectionality; either <cite>LateralDirectionality.LEFT</cite>, or
     *            <cite>LateralDirectionality.RIGHT</cite>
     * @param when Time; the (current) time
     * @param maximumForwardHeadway Length; the maximum forward search distance
     * @param maximumReverseHeadway Length; the maximum reverse search distance
     * @return Collection&lt;LaneBasedGTU&gt;;
     * @throws NetworkException on network inconsistency
     * @throws GtuException on problems with the GTU state (e.g., position)
     * @throws ParameterException in case of a parameter problem
     */
    private Collection<Headway> collectNeighborLaneTraffic(final LateralDirectionality directionality, final Time when,
            final Length maximumForwardHeadway, final Length maximumReverseHeadway)
            throws NetworkException, GtuException, ParameterException
    {
        Collection<Headway> result = new LinkedHashSet<>();
        for (Headway p : parallel(directionality, when))
        {
            // TODO expand for other types of Headways
            // result.add(new HeadwayGTUSimple(p.getId(), ((AbstractHeadwayGTU) p).getGtuType(),
            // new Length(Double.NaN, LengthUnit.SI), p.getLength(), p.getSpeed(), p.getAcceleration()));
            result.add(p);
        }

        // forward
        for (Lane adjacentLane : getAccessibleAdjacentLanes(directionality).get(getLanePathInfo().getReferenceLane()))
        {
            LanePathInfo lpiAdjacent = buildLanePathInfoAdjacent(adjacentLane, directionality, when);
            Headway leader = forwardHeadway(lpiAdjacent, maximumForwardHeadway, true);
            if (null != leader.getId() && !result.contains(leader))
            {
                result.add(leader);
            }
        }

        // backward
        DirectedLanePosition ref = getGtu().getReferencePosition();
        Lane lane = ref.getLane();
        for (Lane adjacentLane : getAccessibleAdjacentLanes(directionality).get(lane))
        {

            double pos = adjacentLane.getLength().si * ref.getPosition().si / ref.getLane().getLength().si;
            pos = ref.getGtuDirection().isPlus() ? pos + getGtu().getRear().getDx().si : pos - getGtu().getRear().getDx().si;

            Headway follower = headwayRecursiveBackwardSI(adjacentLane, getGtu().getDirection(lane), pos, 0.0,
                    -maximumReverseHeadway.getSI(), when);
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
                            follower.getDistance().neg(), follower.getLength(), ((AbstractHeadwayGTU) follower).getWidth(),
                            follower.getSpeed(), follower.getAcceleration(), null));
                }
            }
            else if (follower instanceof HeadwayDistance) // always add for potential lane drop
            {
                result.add(new HeadwayDistance(follower.getDistance().neg()));
            }
            else
            {
                throw new GtuException("collectNeighborLaneTraffic not yet suited to observe obstacles on neighboring lanes");
            }
        }
        return result;
    }

    /**
     * Find a lanePathInfo left or right of the current LanePath.
     * @param adjacentLane Lane; the start adjacent lane for which we calculate the LanePathInfo
     * @param direction LateralDirectionality; either <cite>LateralDirectionality.LEFT</cite>, or
     *            <cite>LateralDirectionality.RIGHT</cite>
     * @param when Time; the (current) time
     * @return the adjacent LanePathInfo
     * @throws GtuException when the GTU was not initialized yet.
     * @throws NetworkException when the speed limit for a GTU type cannot be retrieved from the network.
     * @throws ParameterException in case of a parameter problem
     */
    private LanePathInfo buildLanePathInfoAdjacent(final Lane adjacentLane, final LateralDirectionality direction,
            final Time when) throws GtuException, NetworkException, ParameterException
    {
        if (this.lanePathInfo == null || this.lanePathInfo.getTimestamp().ne(when))
        {
            updateLanePathInfo();
        }
        LanePathInfo lpi = getLanePathInfo();
        List<LaneDirection> laneDirectionList = new ArrayList<>();
        laneDirectionList.add(new LaneDirection(adjacentLane, lpi.getReferenceLaneDirection().getDirection()));
        DirectedLanePosition ref = getGtu().getReferencePosition();
        Length referencePosition =
                Length.instantiateSI(adjacentLane.getLength().si * ref.getPosition().si / ref.getLane().getLength().si);
        for (int i = 1; i < lpi.getLaneDirectionList().size(); i++)
        {
            LaneDirection ld = lpi.getLaneDirectionList().get(i);
            Set<Lane> accessibleLanes = ld.getLane().accessibleAdjacentLanesLegal(direction, getGtu().getGtuType(),
                    lpi.getReferenceLaneDirection().getDirection());
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

}
