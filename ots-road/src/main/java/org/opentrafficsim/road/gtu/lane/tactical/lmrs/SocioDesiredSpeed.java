package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.base.parameters.ParameterTypeSpeed;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedGtu;
import org.opentrafficsim.road.gtu.lane.tactical.following.DesiredSpeedModel;
import org.opentrafficsim.road.gtu.lane.tactical.following.Initialisable;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsParameters;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Tailgating;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

/**
 * Wrapper of a base-desired speed model. The speed may be increased due to social pressure from the follower.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class SocioDesiredSpeed implements DesiredSpeedModel, Initialisable
{

    /** Social pressure applied to the leader. */
    protected static final ParameterTypeDouble RHO = Tailgating.RHO;

    /** Socio-speed sensitivity parameter. */
    protected static final ParameterTypeDouble SOCIO = LmrsParameters.SOCIO;

    /** Vgain parameter; ego-speed sensitivity. */
    protected static final ParameterTypeSpeed VGAIN = LmrsParameters.VGAIN;

    /** GTU. */
    private LaneBasedGtu gtu;

    /** Base model for desired speed. */
    private final DesiredSpeedModel baseModel;

    /**
     * Constructor.
     * @param baseModel base model for desired speed
     */
    public SocioDesiredSpeed(final DesiredSpeedModel baseModel)
    {
        this.baseModel = baseModel;
    }

    @Override
    public Speed desiredSpeed(final Parameters parameters, final SpeedLimitInfo speedInfo) throws ParameterException
    {
        Speed desiredSpeed = this.baseModel.desiredSpeed(parameters, speedInfo);
        if (this.gtu == null)
        {
            return desiredSpeed;
        }
        PerceptionCollectable<PerceivedGtu, LaneBasedGtu> followers;
        LanePerception perception = this.gtu.getTacticalPlanner().getPerception();
        NeighborsPerception neighbors = perception.getPerceptionCategoryOrNull(NeighborsPerception.class);
        if (neighbors != null)
        {
            followers = neighbors.getFollowers(RelativeLane.CURRENT);
            if (!followers.isEmpty())
            {
                double sigma = parameters.getParameter(SOCIO);
                Speed vGain = parameters.getParameter(VGAIN);
                PerceivedGtu follower = followers.first();
                double rhoFollower = follower.getBehavior().socialPressure();
                desiredSpeed = Speed.ofSI(desiredSpeed.si + rhoFollower * sigma * vGain.si);
            }
        }
        return desiredSpeed;
    }

    @Override
    public void init(final LaneBasedGtu laneBasedGtu)
    {
        this.gtu = laneBasedGtu;
    }

}
