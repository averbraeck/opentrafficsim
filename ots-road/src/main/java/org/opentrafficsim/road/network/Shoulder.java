package org.opentrafficsim.road.network;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Duration;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.network.speed.LaneSpeedLimits;
import org.opentrafficsim.road.network.speed.SpeedLimit;
import org.opentrafficsim.road.network.speed.SpeedLimits;

/**
 * This class is mostly the same as a Lane. But as a shoulder it can be recognized by algorithms and models to be responded to
 * differently.
 * <p>
 * Copyright (c) 2024-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class Shoulder extends Lane
{

    /** Nearest lane. */
    private Lane lane;

    /**
     * Constructor specifying geometry.
     * @param link link
     * @param id the id of this lane within the link; should be unique within the link
     * @param geometry geometry
     * @param laneType lane type
     */
    public Shoulder(final CrossSectionLink link, final String id, final CrossSectionGeometry geometry, final LaneType laneType)
    {
        super(link, id, geometry, laneType, new LaneSpeedLimits(Map.of()));
    }

    /**
     * Returns one adjacent lane.
     * @param laneChangeDirection lane change direction
     * @param gtuType GTU type.
     * @return adjacent lane, empty if none
     */
    @Override
    public Optional<Lane> getAdjacentLane(final LateralDirectionality laneChangeDirection, final GtuType gtuType)
    {
        Set<Lane> adjLanes = accessibleAdjacentLanesPhysical(laneChangeDirection, gtuType);
        if (!adjLanes.isEmpty())
        {
            return Optional.of(adjLanes.iterator().next());
        }
        return Optional.empty();
    }

    @Override
    public double getZ()
    {
        return -0.00005;
    }

    @Override
    public SpeedLimits getSpeedLimits(final GtuType gtuType)
    {
        return getLane().getSpeedLimits(gtuType);
    }

    @Override
    public SpeedLimits getSpeedLimits(final GtuType gtuType, final Duration timeOfDay)
    {
        return getLane().getSpeedLimits(gtuType, timeOfDay);
    }

    @Override
    public Optional<SpeedLimit> getSpeedLimit()
    {
        return getLane().getSpeedLimit();
    }

    @Override
    public Optional<SpeedLimit> getSpeedLimit(final Duration timeOfDay)
    {
        return getLane().getSpeedLimit(timeOfDay);
    }

    /**
     * Returns the closest (at the start) lane to this shoulder.
     * @return the closest (at the start) lane to this shoulder
     */
    private Lane getLane()
    {
        if (this.lane == null)
        {
            double minDist = Double.POSITIVE_INFINITY;
            for (Lane laneInLink : getLink().getLanes())
            {
                double dist = laneInLink.getCenterLine().getFirst().distance(getCenterLine().getFirst());
                if (dist < minDist)
                {
                    this.lane = laneInLink;
                    minDist = dist;
                }
            }
            Throw.when(this.lane == null, IllegalStateException.class, "Shoulder is on a link without any lane.");
        }
        return this.lane;
    }

}
