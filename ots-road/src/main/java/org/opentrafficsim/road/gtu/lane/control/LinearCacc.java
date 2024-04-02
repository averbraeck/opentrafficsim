package org.opentrafficsim.road.gtu.lane.control;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.base.parameters.constraint.NumericConstraint;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtu;

/**
 * Simple linear CACC implementation.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class LinearCacc extends LinearAcc
{

    /** Acceleration error gain parameter. */
    public static final ParameterTypeDouble KA =
            new ParameterTypeDouble("ka", "Acceleration error gain", 1.0, NumericConstraint.POSITIVE);

    /**
     * Constructor using default sensors with no delay.
     * @param delayedActuation DelayedActuation; delayed actuation
     */
    public LinearCacc(final DelayedActuation delayedActuation)
    {
        super(delayedActuation);
    }

    /** {@inheritDoc} */
    @Override
    public Acceleration getFollowingAcceleration(final LaneBasedGtu gtu,
            final PerceptionCollectable<HeadwayGtu, LaneBasedGtu> leaders, final Parameters settings) throws ParameterException
    {
        HeadwayGtu leader = leaders.first();
        if (leader.getAcceleration() == null)
        {
            // ACC mode
            return super.getFollowingAcceleration(gtu, leaders, settings);
        }
        double es =
                leader.getDistance().si - gtu.getSpeed().si * settings.getParameter(TDCACC).si - settings.getParameter(X0).si;
        double ev = leader.getSpeed().si - gtu.getSpeed().si;
        double kaui = settings.getParameter(KA) * leader.getAcceleration().si;
        return Acceleration.instantiateSI(settings.getParameter(KS) * es + settings.getParameter(KV) * ev + kaui);
    }

}
