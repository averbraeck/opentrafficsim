package org.opentrafficsim.road.gtu.lane.perception.categories;

import java.util.SortedSet;
import java.util.TreeSet;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
import org.djutils.immutablecollections.ImmutableSortedSet;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeLength;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.perception.AbstractPerceptionCategory;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.structure.LaneRecord;
import org.opentrafficsim.road.gtu.lane.perception.structure.LaneStructure;
import org.opentrafficsim.road.network.LaneAccessLaw;
import org.opentrafficsim.road.network.LaneChangeInfo;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.Shoulder;
import org.opentrafficsim.road.network.speed.SpeedLimitProspect;
import org.opentrafficsim.road.network.speed.SpeedLimitTypes;

/**
 * Perceives information concerning the infrastructure, including splits, lanes, speed limits and road markings. This category
 * is optimized by cooperating closely with the {@code LaneStructure} and only updating internal information when the GTU is on
 * a new {@code Lane}. On the {@code Lane} information is defined relative to the start, and thus easily calculated at each
 * time.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class DirectInfrastructurePerception extends AbstractPerceptionCategory<LaneBasedGtu, LanePerception>
        implements InfrastructurePerception
{

    /** */
    private static final long serialVersionUID = 20160811L;

    /** Range of lane change info perception. */
    public static ParameterTypeLength PERCEPTION = ParameterTypes.PERCEPTION;

    /** Range of lane change possibility perception. */
    public static ParameterTypeLength LOOKAHEAD = ParameterTypes.LOOKAHEAD;

    /**
     * @param perception perception
     */
    public DirectInfrastructurePerception(final LanePerception perception)
    {
        super(perception);
    }

    /** {@inheritDoc} */
    @Override
    public final SortedSet<LaneChangeInfo> getLegalLaneChangeInfo(final RelativeLane lane)
    {
        return computeIfAbsent("legalLaneChangeInfo", () -> computeLaneChangeInfo(lane, LaneAccessLaw.LEGAL), lane);
    }

    /** {@inheritDoc} */
    @Override
    public final SortedSet<LaneChangeInfo> getPhysicalLaneChangeInfo(final RelativeLane lane)
    {
        return computeIfAbsent("physicalLaneChangeInfo", () -> computeLaneChangeInfo(lane, LaneAccessLaw.PHYSICAL), lane);
    }

    /** {@inheritDoc} */
    @Override
    public final SpeedLimitProspect getSpeedLimitProspect(final RelativeLane lane)
    {
        return computeIfAbsent("speedLimitProspect", () -> computeSpeedLimitProspect(lane), lane);
    }

    /** {@inheritDoc} */
    @Override
    public final Length getLegalLaneChangePossibility(final RelativeLane fromLane, final LateralDirectionality lat)
    {
        return computeIfAbsent("legalLaneChange", () -> computeLaneChangePossibility(fromLane, lat, LaneAccessLaw.LEGAL),
                fromLane, lat);
    }

    /** {@inheritDoc} */
    @Override
    public final Length getPhysicalLaneChangePossibility(final RelativeLane fromLane, final LateralDirectionality lat)
    {
        return computeIfAbsent("physicalLaneChange", () -> computeLaneChangePossibility(fromLane, lat, LaneAccessLaw.PHYSICAL),
                fromLane, lat);
    }

    /** {@inheritDoc} */
    @Override
    public final SortedSet<RelativeLane> getCrossSection()
    {
        return computeIfAbsent("crossSection", () -> getLaneStructure().getRootCrossSection());
    }

    /**
     * Compute lane change info.
     * @param lane lane.
     * @param laneLaw lane change law.
     * @return lane change info.
     */
    private SortedSet<LaneChangeInfo> computeLaneChangeInfo(final RelativeLane lane, final LaneAccessLaw laneLaw)
    {
        SortedSet<LaneChangeInfo> out = new TreeSet<>();
        Route route = getGtu().getStrategicalPlanner().getRoute();
        if (route == null)
        {
            return out;
        }
        LaneRecord record = getLaneStructure().getRootRecord(lane);
        Lane l = record.getLane();
        if (laneLaw.equals(LaneAccessLaw.LEGAL) && l instanceof Shoulder)
        {
            if (lane.isCurrent())
            {
                for (LateralDirectionality lat : new LateralDirectionality[] {LateralDirectionality.LEFT,
                        LateralDirectionality.RIGHT})
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
            Length range =
                    Try.assign(() -> getGtu().getParameters().getParameter(PERCEPTION), "Parameter PERCEPTION not available.");
            ImmutableSortedSet<LaneChangeInfo> set =
                    getGtu().getNetwork().getLaneChangeInfo(l, route, getGtu().getType(), range, laneLaw);
            if (set != null)
            {
                Length front = getGtu().getRelativePositions().get(RelativePosition.FRONT).dx();
                for (LaneChangeInfo laneChangeInfo : set)
                {
                    Length dist = laneChangeInfo.remainingDistance().plus(record.getStartDistance()).minus(front);
                    out.add(new LaneChangeInfo(laneChangeInfo.numberOfLaneChanges(), dist, laneChangeInfo.deadEnd(),
                            laneChangeInfo.lateralDirectionality()));
                }
            }
        }
        return out;
    }

    /**
     * Compute speed limit prospect.
     * @param lane lane.
     * @return speed limit prospect.
     */
    private SpeedLimitProspect computeSpeedLimitProspect(final RelativeLane lane)
    {
        // TODO: this is very limited information regarding what the prospect could have, is this is only maximum vehicle speed,
        // and legal speed on the lane
        SpeedLimitProspect slp = new SpeedLimitProspect(getGtu().getOdometer());
        slp.addSpeedInfo(Length.ZERO, SpeedLimitTypes.MAX_VEHICLE_SPEED, getGtu().getMaximumSpeed(), getGtu());
        Lane l = getLaneStructure().getRootRecord(lane).getLane();
        GtuType gtuType = getGtu().getType();
        slp.addSpeedInfo(Length.ZERO, SpeedLimitTypes.FIXED_SIGN, Try.assign(() -> l.getSpeedLimit(getGtu().getType()),
                "No speed limit for GTU type %s on lane %s.", gtuType, l.getFullId()), l);
        return slp;
    }

    /**
     * Compute lane change possibility.
     * @param fromLane lane to possibly change from.
     * @param lat direction to change to.
     * @param accessLaw legal or physical.
     * @return length over which a lane change is possible, or not for a negative value.
     */
    private Length computeLaneChangePossibility(final RelativeLane fromLane, final LateralDirectionality lat,
            final LaneAccessLaw accessLaw)
    {
        LaneRecord root = getLaneStructure().getRootRecord(fromLane);
        LaneRecord record = root;

        // check tail
        Length tail = getPerception().getGtu().getRear().dx();
        while (record != null && record.getStartDistance().gt(tail) && !record.getPrev().isEmpty())
        {
            if (record.getPrev().size() > 1)
            {
                return tail.minus(record.getStartDistance()); // merge prevents lane change
            }
            record = record.getPrev().iterator().next();
            if (!canChange(record, lat, accessLaw))
            {
                return tail.minus(record.getEndDistance());
            }
        }

        LaneRecord prevRecord = null;
        record = root;
        Length lookAhead;
        try
        {
            lookAhead = getPerception().getGtu().getParameters().getParameter(LOOKAHEAD);
        }
        catch (ParameterException ex)
        {
            lookAhead = Length.POSITIVE_INFINITY;
        }
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
                return d;
            }
            // nose is beyond lane, and next lane does not allow a lane change, we can do a !canChange() search
        }
        while (record != null && !canChange(record, lat, accessLaw))
        {
            prevRecord = record;
            record = record.getNext().isEmpty() ? null : record.getNext().iterator().next();
        }
        return getPerception().getGtu().getRear().dx().minus(prevRecord.getEndDistance());
    }

    /**
     * Returns whether the lane change is possible.
     * @param record record.
     * @param lat direction of lane change.
     * @param accessLaw legal or physical.
     * @return whether the lane change is possible.
     */
    private boolean canChange(final LaneRecord record, final LateralDirectionality lat, final LaneAccessLaw accessLaw)
    {
        return accessLaw.equals(LaneAccessLaw.LEGAL)
                ? !record.getLane().accessibleAdjacentLanesLegal(lat, getGtu().getType()).isEmpty()
                : !record.getLane().accessibleAdjacentLanesPhysical(lat, getGtu().getType()).isEmpty();
    }

    /**
     * Returns the lane structure.
     * @return lane structure.
     */
    private LaneStructure getLaneStructure()
    {
        return Try.assign(() -> getPerception().getLaneStructure(), "Parameters for lane structure not available.");
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "DirectInfrastructurePerception " + cacheAsString();
    }

}
