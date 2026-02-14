package org.opentrafficsim.road.gtu.perception.categories;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.LinearDensity;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.TimeStampedObject;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeLength;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.perception.AbstractPerceptionCategory;
import org.opentrafficsim.road.gtu.LaneBasedGtu;
import org.opentrafficsim.road.gtu.perception.LanePerception;
import org.opentrafficsim.road.gtu.perception.RelativeLane;
import org.opentrafficsim.road.gtu.perception.categories.AnticipationSpeed.SpeedSet;
import org.opentrafficsim.road.gtu.perception.categories.neighbors.NeighborsPerception;

/**
 * Traffic perception using neighbors perception. Speed is anticipated as in the LMRS. Density is simply distance of the
 * farthest leader divided by the number of leaders.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class AnticipationTrafficPerception extends AbstractPerceptionCategory<LaneBasedGtu, LanePerception>
        implements TrafficPerception
{

    /** Look ahead parameter type. */
    protected static final ParameterTypeLength LOOKAHEAD = ParameterTypes.LOOKAHEAD;

    /** Last time speed was updated. */
    private Duration lastSpeedTime = null;

    /** Anticipated speed by vehicles in the left lane. */
    private Map<RelativeLane, Double> antFromLeft = new LinkedHashMap<>();

    /** Anticipated speed by vehicles in the lane. */
    private Map<RelativeLane, Double> antInLane = new LinkedHashMap<>();

    /** Anticipated speed by vehicles in the right lane. */
    private Map<RelativeLane, Double> antFromRight = new LinkedHashMap<>();

    /** Anticipated speed combined. */
    private Map<RelativeLane, Speed> speed = new LinkedHashMap<>();

    /** Anticipated density combined. */
    private Map<RelativeLane, TimeStampedObject<LinearDensity>> density = new LinkedHashMap<>();

    /** Density collector. */
    private static final AnticipationDensity DENSITY = new AnticipationDensity();

    /**
     * Constructor.
     * @param perception perception
     */
    public AnticipationTrafficPerception(final LanePerception perception)
    {
        super(perception);
    }

    @Override
    public Speed getSpeed(final RelativeLane lane, final Speed desiredSpeed) throws ParameterException
    {
        Duration now = getTimestamp();
        if (this.lastSpeedTime == null || this.lastSpeedTime.si < now.si)
        {
            // due to lane interdependency, we clear all
            this.antFromLeft.clear();
            this.antInLane.clear();
            this.antFromRight.clear();
            this.speed.clear();
            this.lastSpeedTime = now;
        }
        Speed vAnt = this.speed.get(lane);
        if (vAnt == null)
        {
            LaneBasedGtu gtu = getPerception().getGtu();
            vAnt = anticipationSpeed(lane, gtu.getParameters(),
                    getPerception().getPerceptionCategoryOptional(NeighborsPerception.class)
                            .orElseThrow(() -> new NoSuchElementException("No neighbors perception category")),
                    getPerception().getPerceptionCategoryOptional(InfrastructurePerception.class).orElseThrow(
                            () -> new NoSuchElementException("No infrastructure perception category")),
                    desiredSpeed);
            this.speed.put(lane, vAnt);
        }
        return vAnt;
    }

    /**
     * Returns the anticipation speed in a lane. GTU's in adjacent lanes with their indicator towards the given lane are
     * included in the evaluation.
     * @param lane lane to assess
     * @param params parameters
     * @param neighbors neighbors perception
     * @param infra infrastructure perception
     * @param desiredSpeed desired speed
     * @return anticipation speed in lane
     * @throws ParameterException on missing parameter
     */
    private Speed anticipationSpeed(final RelativeLane lane, final Parameters params, final NeighborsPerception neighbors,
            final InfrastructurePerception infra, final Speed desiredSpeed) throws ParameterException
    {
        if (!this.antInLane.containsKey(lane))
        {
            anticipateSpeedFromLane(lane, params, neighbors, desiredSpeed);
        }
        double v = this.antInLane.get(lane);
        if (infra.getCrossSection().contains(lane.getLeft()))
        {
            if (!this.antFromLeft.containsKey(lane))
            {
                anticipateSpeedFromLane(lane.getLeft(), params, neighbors, desiredSpeed);
            }
            double fromLeft = this.antFromLeft.get(lane);
            v = v < fromLeft ? v : fromLeft;
        }
        if (infra.getCrossSection().contains(lane.getRight()))
        {
            if (!this.antFromRight.containsKey(lane))
            {
                anticipateSpeedFromLane(lane.getRight(), params, neighbors, desiredSpeed);
            }
            double fromRight = this.antFromRight.get(lane);
            v = v < fromRight ? v : fromRight;
        }
        return Speed.ofSI(v);
    }

    /**
     * Anticipate speed from the GTUs in one lane. This affects up to 3 lanes, all this information is stored.
     * @param lane lane to assess
     * @param params parameters
     * @param neighbors neighbors perception
     * @param desiredSpeed desired speed
     * @throws ParameterException on missing parameter
     */
    private void anticipateSpeedFromLane(final RelativeLane lane, final Parameters params, final NeighborsPerception neighbors,
            final Speed desiredSpeed) throws ParameterException
    {
        AnticipationSpeed anticipationSpeed = new AnticipationSpeed(desiredSpeed, params.getParameter(LOOKAHEAD), lane);
        SpeedSet speedSet = neighbors.getLeaders(lane).collect(anticipationSpeed);
        this.antFromLeft.put(lane.getRight(), speedSet.getRight().si);
        this.antInLane.put(lane, speedSet.getCurrent().si);
        this.antFromRight.put(lane.getLeft(), speedSet.getLeft().si);
    }

    @Override
    public LinearDensity getDensity(final RelativeLane lane)
    {
        Duration now = getTimestamp();
        TimeStampedObject<LinearDensity> tK = this.density.get(lane);
        if (tK == null || tK.timestamp().si < now.si)
        {
            LinearDensity k = getPerception().getPerceptionCategoryOptional(NeighborsPerception.class)
                    .orElseThrow(() -> new NoSuchElementException("No neighbors perception category")).getLeaders(lane)
                    .collect(DENSITY);
            this.density.put(lane, new TimeStampedObject<>(k, now));
            return k;
        }
        else
        {
            return tK.object();
        }
    }

    @Override
    public String toString()
    {
        return "AnticipationTrafficPerception [lastSpeedTime=" + this.lastSpeedTime + ", speed=" + this.speed + ", density="
                + this.density + "]";
    }

}
