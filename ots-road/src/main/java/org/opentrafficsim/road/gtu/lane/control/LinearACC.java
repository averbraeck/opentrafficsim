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
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class LinearACC extends AbstractLinearFreeControl
{

    /** Gap error gain parameter. */
    public static final ParameterTypeDouble KS =
            new ParameterTypeDouble("ks", "Gap error gain", 0.2, NumericConstraint.POSITIVE);

    /** Speed error gain parameter. */
    public static final ParameterTypeDouble KV =
            new ParameterTypeDouble("kv", "Speed error gain", 0.4, NumericConstraint.POSITIVE);

    /**
     * Constructor using default sensors with no delay.
     * @param delayedActuation DelayedActuation; delayed actuation
     */
    public LinearACC(final DelayedActuation delayedActuation)
    {
        super(delayedActuation);
    }

    /** {@inheritDoc} */
    @Override
    public Acceleration getFollowingAcceleration(final LaneBasedGtu gtu,
            final PerceptionCollectable<HeadwayGtu, LaneBasedGtu> leaders, final Parameters settings) throws ParameterException
    {
        HeadwayGtu leader = leaders.first();
        double es =
                leader.getDistance().si - gtu.getSpeed().si * settings.getParameter(TDACC).si - settings.getParameter(X0).si;
        double ev = leader.getSpeed().si - gtu.getSpeed().si;
        return Acceleration.instantiateSI(settings.getParameter(KS) * es + settings.getParameter(KV) * ev);
    }

}
