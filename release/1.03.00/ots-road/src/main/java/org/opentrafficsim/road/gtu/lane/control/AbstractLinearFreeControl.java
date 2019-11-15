package org.opentrafficsim.road.gtu.lane.control;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.base.parameters.constraint.NumericConstraint;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTU;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

/**
 * Class that splits the desired acceleration of a controller in a fixed linear free term, and a term for following determined
 * by the sub-class.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Mar 14, 2019 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public abstract class AbstractLinearFreeControl extends AbstractActuatedControl
{

    /** Desired speed error gain parameter. */
    public static final ParameterTypeDouble KF =
            new ParameterTypeDouble("kf", "Desired speed error gain", 0.075, NumericConstraint.POSITIVE);

    /**
     * Constructor using default sensors with no delay.
     * @param delayedActuation DelayedActuation; delayed actuation
     */
    public AbstractLinearFreeControl(final DelayedActuation delayedActuation)
    {
        super(delayedActuation);
    }

    /** {@inheritDoc} */
    @Override
    public final Acceleration getDesiredAcceleration(final LaneBasedGTU gtu,
            final PerceptionCollectable<HeadwayGTU, LaneBasedGTU> leaders, final Parameters settings) throws ParameterException
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
     * @param gtu LaneBasedGTU; gtu
     * @param leaders PerceptionCollectable&lt;HeadwayGTU, LaneBasedGTU&gt;; leaders
     * @param settings Parameters; system settings
     * @return Acceleration; following acceleration of the longitudinal control
     * @throws ParameterException if parameter is not present
     */
    public abstract Acceleration getFollowingAcceleration(LaneBasedGTU gtu,
            PerceptionCollectable<HeadwayGTU, LaneBasedGTU> leaders, Parameters settings) throws ParameterException;

}
