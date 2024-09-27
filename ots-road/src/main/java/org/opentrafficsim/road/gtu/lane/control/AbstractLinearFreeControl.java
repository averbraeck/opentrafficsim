package org.opentrafficsim.road.gtu.lane.control;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.base.parameters.constraint.NumericConstraint;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtu;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

/**
 * Class that splits the desired acceleration of a controller in a fixed linear free term, and a term for following determined
 * by the sub-class.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public abstract class AbstractLinearFreeControl extends AbstractActuatedControl
{

    /** Desired speed error gain parameter. */
    public static final ParameterTypeDouble KF =
            new ParameterTypeDouble("kf", "Desired speed error gain", 0.075, NumericConstraint.POSITIVE);

    /**
     * Constructor using default sensors with no delay.
     * @param delayedActuation delayed actuation
     */
    public AbstractLinearFreeControl(final DelayedActuation delayedActuation)
    {
        super(delayedActuation);
    }

    /** {@inheritDoc} */
    @Override
    public final Acceleration getDesiredAcceleration(final LaneBasedGtu gtu,
            final PerceptionCollectable<HeadwayGtu, LaneBasedGtu> leaders, final Parameters settings) throws ParameterException
    {
        SpeedLimitInfo speedInfo;
        try
        {
            speedInfo = gtu.getTacticalPlanner().getPerception().getPerceptionCategory(InfrastructurePerception.class)
                    .getSpeedLimitProspect(RelativeLane.CURRENT).getSpeedLimitInfo(Length.ZERO);
        }
        catch (OperationalPlanException exception)
        {
            throw new RuntimeException("Infrastructure perception is not available.", exception);
        }
        Speed v0 = gtu.getTacticalPlanner().getCarFollowingModel().desiredSpeed(gtu.getParameters(), speedInfo);
        Acceleration a = Acceleration.instantiateSI(settings.getParameter(KF) * (v0.si - gtu.getSpeed().si));
        if (leaders.isEmpty())
        {
            return a;
        }
        return Acceleration.min(a, getFollowingAcceleration(gtu, leaders, settings));
    }

    /**
     * Returns the following acceleration of the longitudinal control. This method is only invoked if there is at least 1
     * leader.
     * @param gtu gtu
     * @param leaders leaders
     * @param settings system settings
     * @return following acceleration of the longitudinal control
     * @throws ParameterException if parameter is not present
     */
    public abstract Acceleration getFollowingAcceleration(LaneBasedGtu gtu,
            PerceptionCollectable<HeadwayGtu, LaneBasedGtu> leaders, Parameters settings) throws ParameterException;

}
