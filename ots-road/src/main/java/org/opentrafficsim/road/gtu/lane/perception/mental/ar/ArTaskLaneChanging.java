package org.opentrafficsim.road.gtu.lane.perception.mental.ar;

import org.djunits.value.vdouble.scalar.Duration;
import org.djutils.exceptions.Try;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.Stateless;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.TaskHeadwayCollector;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsParameters;

/**
 * Lane changing task based on car-following (as gap-acceptance proxy), and an underlying consideration to include adjacent
 * lanes.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class ArTaskLaneChanging extends ArTaskHeadwayBased implements Stateless<ArTaskLaneChanging>
{

    /** Singleton instance. */
    public static final ArTaskLaneChanging SINGLETON = new ArTaskLaneChanging();

    /**
     * Constructor.
     */
    private ArTaskLaneChanging()
    {
        super("lane-changing");
    }

    @Override
    protected Duration getHeadway(final LanePerception perception, final LaneBasedGtu gtu, final Parameters parameters)
            throws ParameterException
    {
        NeighborsPerception neighbors = Try.assign(() -> perception.getPerceptionCategory(NeighborsPerception.class),
                "NeighborsPerception not available.");
        double lat = Try.assign(() -> getConsideration(perception, gtu, parameters), "Exception during lateral consideration.");
        RelativeLane lane;
        if (Math.abs(lat) < 1e-9)
        {
            return null;
        }
        if (lat < 0.0)
        {
            lane = RelativeLane.LEFT;
            lat = -lat;
        }
        else
        {
            lane = RelativeLane.RIGHT;
        }
        if (perception.getLaneStructure().exists(lane) && neighbors.isGtuAlongside(lane.getLateralDirectionality()))
        {
            return Duration.ZERO;
        }
        Duration h1 = neighbors.getLeaders(lane).collect(new TaskHeadwayCollector(getSpeed()));
        Duration h2 = neighbors.getFollowers(lane).collect(new TaskHeadwayCollector(getSpeed()));
        if (h1 == null)
        {
            return h2 == null ? null : h2.divide(lat);
        }
        if (h2 == null)
        {
            return h1 == null ? null : h1.divide(lat);
        }
        if (h1.eq0() && h2.eq0())
        {
            return Duration.ZERO;
        }
        if (h1.eq(Duration.POSITIVE_INFINITY) && h2.eq(Duration.POSITIVE_INFINITY))
        {
            return Duration.POSITIVE_INFINITY;
        }
        return Duration.ofSI(h1.si * h2.si / (lat * (h1.si + h2.si)));
    }

    /**
     * Returns fraction of lateral consideration, &lt;0 for left lane, &gt;0 for right lane. Should be in the range -1 ... 1.
     * @param perception perception
     * @param gtu gtu
     * @param parameters parameters
     * @return demand of this task
     * @throws ParameterException if a parameter is missing or out of bounds
     * @throws GtuException exceptions pertaining to the GTU
     */
    private double getConsideration(final LanePerception perception, final LaneBasedGtu gtu, final Parameters parameters)
            throws ParameterException, GtuException
    {
        double dLeft = gtu.getParameters().getParameter(LmrsParameters.DLEFT);
        double dRight = gtu.getParameters().getParameter(LmrsParameters.DRIGHT);
        if (dLeft > dRight && dLeft > 0.0)
        {
            return dLeft > 1.0 ? -1.0 : -dLeft;
        }
        else if (dRight > dLeft && dRight > 0.0)
        {
            return dRight > 1.0 ? 1.0 : dRight;
        }
        return 0.0;
    }

    @Override
    public ArTaskLaneChanging get()
    {
        return SINGLETON;
    }

}
