package org.opentrafficsim.road.gtu.tactical;

import org.opentrafficsim.base.parameters.ParameterTypeClass;
import org.opentrafficsim.base.parameters.ParameterTypeDuration;
import org.opentrafficsim.base.parameters.ParameterTypeLength;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.road.gtu.LaneBasedGtu;
import org.opentrafficsim.road.gtu.perception.LanePerception;
import org.opentrafficsim.road.gtu.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.tactical.lmrs.Lmrs;

/**
 * A lane-based tactical planner generates an operational plan for the lane-based GTU. It can ask the strategic planner for
 * assistance on the route to take when the network splits. This abstract class contains a number of helper methods that make it
 * easy to implement a tactical planner.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public abstract class AbstractLaneBasedTacticalPlanner implements LaneBasedTacticalPlanner
{

    /** Tactical planner parameter. */
    public static final ParameterTypeClass<LaneBasedTacticalPlanner> LANE_TACTICAL_PLANNER = new ParameterTypeClass<>(
            "lane tactical planner", "Lane-based tactical planner class.", LaneBasedTacticalPlanner.class, Lmrs.class);

    /** Look ahead parameter type. */
    protected static final ParameterTypeLength LOOKAHEAD = ParameterTypes.LOOKAHEAD;

    /** Time step parameter type. */
    protected static final ParameterTypeDuration DT = ParameterTypes.DT;

    /** The car-following model. */
    private CarFollowingModel carFollowingModel;

    /** The perception. */
    private final LanePerception lanePerception;

    /** GTU. */
    private final LaneBasedGtu gtu;

    /**
     * Instantiates a tactical planner.
     * @param carFollowingModel car-following model
     * @param gtu GTU
     * @param lanePerception perception
     */
    public AbstractLaneBasedTacticalPlanner(final CarFollowingModel carFollowingModel, final LaneBasedGtu gtu,
            final LanePerception lanePerception)
    {
        setCarFollowingModel(carFollowingModel);
        this.gtu = gtu;
        this.lanePerception = lanePerception;
    }

    @Override
    public final LaneBasedGtu getGtu()
    {
        return this.gtu;
    }

    @Override
    public final CarFollowingModel getCarFollowingModel()
    {
        return this.carFollowingModel;
    }

    /**
     * Sets the car-following model.
     * @param carFollowingModel Car-following model to set.
     */
    public final void setCarFollowingModel(final CarFollowingModel carFollowingModel)
    {
        this.carFollowingModel = carFollowingModel;
    }

    @Override
    public final LanePerception getPerception()
    {
        return this.lanePerception;
    }

}
