package org.opentrafficsim.road.gtu.lane.tactical;

import java.util.Optional;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.perception.EgoPerception;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

/**
 * Wraps a {@link LaneBasedGtu} and provides bundled and easy-access information that tactical models and their components need
 * regarding the ego-vehicle. This information may be cached, such that different components do not need to repeat expensive
 * steps to obtain certain information. Because of the caching, this is a throw-away class that should only have a lifetime
 * equal to a single model execution.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class TacticalContextEgo implements TacticalContext
{

    /** GTU. */
    private final LaneBasedGtu gtu;

    /** Perception. */
    private LanePerception perception;

    /** Route. */
    private Optional<Route> route;

    /** Parameters. */
    private Parameters parameters;

    /** Car-following model. */
    private CarFollowingModel carFollowingModel;

    /** Speed limit info. */
    private SpeedLimitInfo speedLimitInfo;

    /** Ego length. */
    private Length length;

    /** Ego width. */
    private Length width;

    /** Ego speed. */
    private Speed speed;

    /** Ego acceleration. */
    private Acceleration acceleration;

    /**
     * Constructor.
     * @param gtu GTU
     */
    public TacticalContextEgo(final LaneBasedGtu gtu)
    {
        this.gtu = gtu;
    }

    /**
     * Returns the GTU.
     * @return GTU
     */
    public LaneBasedGtu getGtu()
    {
        return this.gtu;
    }

    /**
     * Returns the perception.
     * @return perception
     */
    public LanePerception getPerception()
    {
        if (this.perception == null)
        {
            this.perception = this.gtu.getTacticalPlanner().getPerception();
        }
        return this.perception;
    }

    /**
     * Returns the route.
     * @return route
     */
    public Optional<Route> getRoute()
    {
        if (this.route == null)
        {
            this.route = getGtu().getStrategicalPlanner().getRoute();
        }
        return this.route;
    }

    /**
     * Returns the current time.
     * @return current time
     */
    public Duration getTime()
    {
        return getGtu().getSimulator().getSimulatorTime();
    }

    @Override
    public Parameters getParameters()
    {
        if (this.parameters == null)
        {
            this.parameters = this.gtu.getParameters();
        }
        return this.parameters;
    }

    @Override
    public CarFollowingModel getCarFollowingModel()
    {
        if (this.carFollowingModel == null)
        {
            this.carFollowingModel = this.gtu.getTacticalPlanner().getCarFollowingModel();
        }
        return this.carFollowingModel;
    }

    @Override
    public SpeedLimitInfo getSpeedLimitInfo()
    {
        if (this.speedLimitInfo == null)
        {
            this.speedLimitInfo = getPerception().getPerceptionCategoryOrNull(InfrastructurePerception.class)
                    .getSpeedLimitProspect(RelativeLane.CURRENT).getSpeedLimitInfo(Length.ZERO);
        }
        return this.speedLimitInfo;
    }

    @Override
    public Length getLength()
    {
        checkEgo();
        return this.length;
    }

    @Override
    public Length getWidth()
    {
        checkEgo();
        return this.width;
    }

    @Override
    public Speed getSpeed()
    {
        checkEgo();
        return this.speed;
    }

    @Override
    public Acceleration getAcceleration()
    {
        checkEgo();
        return this.acceleration;
    }

    /**
     * Check ego information is cached.
     */
    private void checkEgo()
    {
        if (this.length == null)
        {
            EgoPerception<?, ?> ego = getPerception().getPerceptionCategoryOrNull(EgoPerception.class);
            this.length = ego.getLength();
            this.width = ego.getWidth();
            this.speed = ego.getSpeed();
            this.acceleration = ego.getAcceleration();
        }
    }

}
