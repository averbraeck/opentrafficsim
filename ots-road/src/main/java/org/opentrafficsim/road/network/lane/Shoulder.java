package org.opentrafficsim.road.network.lane;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.draw.line.Polygon2d;
import org.opentrafficsim.core.geometry.OtsLine2d;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;

/**
 * This class is mostly the same as a Lane. But as a shoulder it can be recognized by algorithms and models to be responded to
 * differently.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class Shoulder extends Lane
{

    /** */
    private static final long serialVersionUID = 20240507L;

    /**
     * Constructor specifying geometry.
     * @param link link.
     * @param id the id of this lane within the link; should be unique within the link.
     * @param centerLine center line.
     * @param contour contour shape.
     * @param crossSectionSlices cross-section slices.
     * @param laneType lane type.
     * @throws NetworkException when no cross-section slice is defined.
     */
    public Shoulder(final CrossSectionLink link, final String id, final OtsLine2d centerLine, final Polygon2d contour,
            final List<CrossSectionSlice> crossSectionSlices, final LaneType laneType) throws NetworkException
    {
        super(link, id, centerLine, contour, crossSectionSlices, laneType, new LinkedHashMap<>());
    }

    /**
     * Returns one adjacent lane.
     * @param laneChangeDirection lane change direction
     * @param gtuType GTU type.
     * @return adjacent lane, {@code null} if none
     */
    @Override
    public Lane getAdjacentLane(final LateralDirectionality laneChangeDirection, final GtuType gtuType)
    {
        Set<Lane> adjLanes = accessibleAdjacentLanesPhysical(laneChangeDirection, gtuType);
        if (!adjLanes.isEmpty())
        {
            return adjLanes.iterator().next();
        }
        return null;
    }

    @Override
    public double getZ()
    {
        return -0.00005;
    }

    @Override
    public Speed getSpeedLimit(final GtuType gtuType) throws NetworkException
    {
        LateralDirectionality[] lats = getLink().getLaneKeepingPolicy().equals(LaneKeepingPolicy.KEEPRIGHT)
                ? new LateralDirectionality[] {LateralDirectionality.RIGHT, LateralDirectionality.LEFT}
                : new LateralDirectionality[] {LateralDirectionality.LEFT, LateralDirectionality.RIGHT};
        for (LateralDirectionality lat : lats)
        {
            Lane adjacentLane = getAdjacentLane(lat, gtuType);
            if (adjacentLane != null)
            {
                return adjacentLane.getSpeedLimit(gtuType);
            }
        }
        return Speed.ZERO;
    }
}
