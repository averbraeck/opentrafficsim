package org.opentrafficsim.road.gtu.perception.categories;

import java.util.Optional;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.draw.point.DirectedPoint2d;
import org.djutils.exceptions.Try;
import org.djutils.immutablecollections.ImmutableSortedSet;
import org.djutils.math.AngleUtil;
import org.opentrafficsim.base.DistancedObject;
import org.opentrafficsim.base.OtsRuntimeException;
import org.opentrafficsim.base.parameters.ParameterTypeLength;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.perception.AbstractPerceptionCategory;
import org.opentrafficsim.core.gtu.perception.EgoPerception;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.LaneBasedGtu;
import org.opentrafficsim.road.gtu.perception.LanePerception;
import org.opentrafficsim.road.gtu.perception.RelativeLane;
import org.opentrafficsim.road.gtu.perception.structure.LaneRecord;
import org.opentrafficsim.road.gtu.perception.structure.LaneStructure;
import org.opentrafficsim.road.gtu.perception.structure.NavigatingIterable.Entry;
import org.opentrafficsim.road.network.Lane;
import org.opentrafficsim.road.network.LaneAccessLaw;
import org.opentrafficsim.road.network.LaneChangeInfo;
import org.opentrafficsim.road.network.Shoulder;
import org.opentrafficsim.road.network.object.SpeedBump;
import org.opentrafficsim.road.network.speed.SpeedLimits;

/**
 * Perceives information concerning the infrastructure, including splits, lanes, speed limits and road markings. This category
 * is optimized by cooperating closely with the {@code LaneStructure} and only updating internal information when the GTU is on
 * a new {@code Lane}. On the {@code Lane} information is defined relative to the start, and thus easily calculated at each
 * time.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class DirectInfrastructurePerception extends AbstractPerceptionCategory<LaneBasedGtu, LanePerception>
        implements InfrastructurePerception
{

    /** Range of lane change info perception. */
    public static final ParameterTypeLength LANE_STRUCTURE = ParameterTypes.LANE_STRUCTURE;

    /** Range of lane change possibility perception. */
    public static final ParameterTypeLength LOOKAHEAD = ParameterTypes.LOOKAHEAD;

    /**
     * Constructor.
     * @param perception perception
     */
    public DirectInfrastructurePerception(final LanePerception perception)
    {
        super(perception);
    }

    @Override
    public SortedSet<LaneChangeInfo> getLegalLaneChangeInfo(final RelativeLane lane)
    {
        return computeIfAbsent("legalLaneChangeInfo", () -> computeLaneChangeInfo(lane, LaneAccessLaw.LEGAL), lane);
    }

    @Override
    public SortedSet<LaneChangeInfo> getPhysicalLaneChangeInfo(final RelativeLane lane)
    {
        return computeIfAbsent("physicalLaneChangeInfo", () -> computeLaneChangeInfo(lane, LaneAccessLaw.PHYSICAL), lane);
    }

    @Override
    public SpeedLimits getSpeedLimits(final RelativeLane lane)
    {
        return computeIfAbsent("speedLimitProspect", () -> computeSpeedLimits(lane), lane);
    }

    @Override
    public Length getLegalLaneChangePossibility(final RelativeLane fromLane, final LateralDirectionality lat)
    {
        return computeIfAbsent("legalLaneChange", () -> computeLaneChangePossibility(fromLane, lat, LaneAccessLaw.LEGAL),
                fromLane, lat);
    }

    @Override
    public Length getPhysicalLaneChangePossibility(final RelativeLane fromLane, final LateralDirectionality lat)
    {
        return computeIfAbsent("physicalLaneChange", () -> computeLaneChangePossibility(fromLane, lat, LaneAccessLaw.PHYSICAL),
                fromLane, lat);
    }

    @Override
    public SortedSet<RelativeLane> getCrossSection()
    {
        return computeIfAbsent("crossSection", () -> getLaneStructure().getRootCrossSection());
    }

    /**
     * Compute lane change info.
     * @param lane lane
     * @param laneLaw lane change law
     * @return lane change info
     */
    private SortedSet<LaneChangeInfo> computeLaneChangeInfo(final RelativeLane lane, final LaneAccessLaw laneLaw)
    {
        SortedSet<LaneChangeInfo> out = new TreeSet<>();
        Optional<Route> route = getGtu().getStrategicalPlanner().getRoute();
        if (route.isEmpty())
        {
            return out;
        }
        LaneRecord record = getLaneStructure().getRootRecord(lane);
        Lane l = record.getLane();
        if (laneLaw.equals(LaneAccessLaw.LEGAL) && l instanceof Shoulder)
        {
            if (lane.isCurrent())
            {
                for (LateralDirectionality lat : LateralDirectionality.LEFT_AND_RIGHT)
                {
                    if (!record.getLane().accessibleAdjacentLanesPhysical(lat, getGtu().getType()).isEmpty())
                    {
                        out.add(new LaneChangeInfo(1, Length.ZERO, true, lat));
                    }
                }
            }
        }
        else
        {
            Length range = getGtu().getParameters().getOptionalParameter(LANE_STRUCTURE)
                    .orElseThrow(() -> new OtsRuntimeException("Parameter LANE_STRUCTURE not available."));
            Length front = getGtu().getRelativePositions().get(RelativePosition.FRONT).dx();
            ImmutableSortedSet<LaneChangeInfo> set = getGtu().getNetwork().getLaneChangeInfo(l, route.get(), getGtu().getType(),
                    range.minus(record.getStartDistance()).plus(front), laneLaw);
            for (LaneChangeInfo laneChangeInfo : set)
            {
                Length dist = laneChangeInfo.remainingDistance().plus(record.getStartDistance()).minus(front);
                out.add(new LaneChangeInfo(laneChangeInfo.numberOfLaneChanges(), dist, laneChangeInfo.deadEnd(),
                        laneChangeInfo.lateralDirectionality()));
            }
        }
        return out;
    }

    /**
     * Compute speed limit prospect.
     * @param lane lane
     * @return speed limit prospect
     */
    private SpeedLimits computeSpeedLimits(final RelativeLane lane)
    {
        return getLaneStructure().getRootRecord(lane).getLane().getSpeedLimits(getPerception().getGtu().getType(),
                getPerception().getGtu().getSimulator().getTimeOfDay());
    }

    /**
     * Compute lane change possibility.
     * @param fromLane lane to possibly change from
     * @param lat direction to change to
     * @param accessLaw legal or physical
     * @return length over which a lane change is possible, or not for a negative value
     */
    private Length computeLaneChangePossibility(final RelativeLane fromLane, final LateralDirectionality lat,
            final LaneAccessLaw accessLaw)
    {
        LaneRecord root = getLaneStructure().getRootRecord(fromLane);
        LaneRecord record = root;

        // check tail
        Length tail = getPerception().getGtu().getRear().dx();
        Length tailImpossibility = null;
        while (record != null && record.getStartDistance().gt(tail) && !record.getPrev().isEmpty())
        {
            if (record.getPrev().size() > 1)
            {
                tailImpossibility = tail.minus(record.getStartDistance()); // merge prevents lane change
                break;
            }
            record = record.getPrev().iterator().next();
            if (!canChange(record, lat, accessLaw))
            {
                tailImpossibility = tail.minus(record.getEndDistance());
                break;
            }
        }

        LaneRecord prevRecord = null;
        record = root;
        Length lookAhead =
                getPerception().getGtu().getParameters().getOptionalParameter(LOOKAHEAD).orElse(Length.POSITIVE_INFINITY);
        if (canChange(record, lat, accessLaw))
        {
            while (record != null && canChange(record, lat, accessLaw))
            {
                if (record.getEndDistance().gt(lookAhead))
                {
                    return Length.POSITIVE_INFINITY;
                }
                prevRecord = record;
                record = record.getNext().isEmpty() ? null : record.getNext().iterator().next();
            }
            Length d = prevRecord.getEndDistance().minus(getPerception().getGtu().getFront().dx());
            if (d.gt0())
            {
                return tailImpossibility == null ? d : tailImpossibility;
            }
            // nose is beyond lane, and next lane does not allow a lane change, we can do a !canChange() search
        }
        while (record != null && !canChange(record, lat, accessLaw) && record.getStartDistance().lt(lookAhead))
        {
            prevRecord = record;
            record = record.getNext().isEmpty() ? null : record.getNext().iterator().next();
        }
        return getPerception().getGtu().getRear().dx().minus(prevRecord.getEndDistance());
    }

    /**
     * Returns whether the lane change is possible.
     * @param record record
     * @param lat direction of lane change
     * @param accessLaw legal or physical
     * @return whether the lane change is possible
     */
    private boolean canChange(final LaneRecord record, final LateralDirectionality lat, final LaneAccessLaw accessLaw)
    {
        return accessLaw.equals(LaneAccessLaw.LEGAL)
                ? !record.getLane().accessibleAdjacentLanesLegal(lat, getGtu().getType()).isEmpty()
                : !record.getLane().accessibleAdjacentLanesPhysical(lat, getGtu().getType()).isEmpty();
    }

    @Override
    public SortedMap<Length, DirectedPoint2d> getPathScan()
    {
        return computeIfAbsent("pathScan", () -> computePathScan());
    }

    /**
     * Compute path scan.
     * @return path scan
     */
    private SortedMap<Length, DirectedPoint2d> computePathScan()
    {
        SortedMap<Length, DirectedPoint2d> out = new TreeMap<>();
        EgoPerception<?, ?> ego =
                Try.assign(() -> getPerception().getPerceptionCategory(EgoPerception.class), "No ego perception.");
        Parameters parameters = getPerception().getGtu().getParameters();

        // distance step based on current speed and model time step
        Length step = Length.max(ego.getLength(),
                parameters.getOptionalParameter(ParameterTypes.DT).orElseThrow().times(ego.getSpeed()));

        // horizon based on braking distance from current speed and b
        double b = parameters.getOptionalParameter(ParameterTypes.B).orElseThrow().si;
        double brakeTime = ego.getSpeed().si / b;
        double brakeDistance = ego.getSpeed().si * brakeTime - .5 * b * brakeTime * brakeTime;

        // scan for integer number of steps
        int last = ((int) Math.ceil(brakeDistance / step.si)) + 1; // +1 as this is exclusive
        branchPath(step, 1, last, getPerception().getGtu().getLocation().dirZ,
                getLaneStructure().getRootRecord(RelativeLane.CURRENT), out);

        return out;
    }

    /**
     * Branches along downstream lanes to find relevant points.
     * @param step step between points
     * @param first first index (earlier already found in branch)
     * @param last last index (exclusive)
     * @param prevPhi angle of previous point
     * @param record lane record
     * @param map map to store output in
     */
    private void branchPath(final Length step, final int first, final int last, final double prevPhi, final LaneRecord record,
            final SortedMap<Length, DirectedPoint2d> map)
    {
        double phi = prevPhi;
        for (int i = first; i < last; i++)
        {
            Length ahead = step.times(i);
            if (ahead.gt(record.getStartDistance().plus(record.getLength())))
            {
                for (LaneRecord next : record.getNext())
                {
                    branchPath(step, i, last, phi, next, map);
                }
                return;
            }
            Length position = ahead.minus(record.getStartDistance());
            DirectedPoint2d point = record.getLane().getCenterLine().getLocation(position);
            // ignore if essentially the same direction as the previous point
            if (Math.abs(AngleUtil.normalizeAroundZero(point.dirZ - phi)) > 1e-3)
            {
                phi = point.dirZ;
                map.merge(ahead, point, (p1, p2) ->
                {
                    double angle1 = AngleUtil.normalizeAroundZero(p1.dirZ - getGtu().getLocation().dirZ);
                    double angle2 = AngleUtil.normalizeAroundZero(p2.dirZ - getGtu().getLocation().dirZ);
                    return Math.abs(angle1) > Math.abs(angle2) ? p1 : p2;
                });
            }
        }
    }

    @Override
    public Optional<DistancedObject<Speed>> getSpeedBump()
    {
        return computeIfAbsent("speedBump", () -> computeSpeedBump());
    }

    /**
     * Compute speed bump.
     * @return speed bump
     */
    private Optional<DistancedObject<Speed>> computeSpeedBump()
    {
        Iterable<Entry<SpeedBump>> speedBumps =
                getLaneStructure().getDownstreamObjects(RelativeLane.CURRENT, SpeedBump.class, RelativePosition.FRONT, true);
        for (Entry<SpeedBump> entry : speedBumps)
        {
            // first speed bump only
            return Optional
                    .of(new DistancedObject<>(entry.object().getSpeed(getPerception().getGtu().getType()), entry.distance()));
        }
        return Optional.empty();
    }

    /**
     * Returns the lane structure.
     * @return lane structure
     */
    private LaneStructure getLaneStructure()
    {
        return Try.assign(() -> getPerception().getLaneStructure(), "Parameters for lane structure not available.");
    }

    @Override
    public final String toString()
    {
        return "DirectInfrastructurePerception " + cacheAsString();
    }

}
