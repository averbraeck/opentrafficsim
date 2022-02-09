package org.opentrafficsim.road.gtu.lane.control;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.base.parameters.constraint.NumericConstraint;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTU;

/**
 * Simple linear CACC implementation.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Mar 13, 2019 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class LinearCACC extends LinearACC
{

    /** Acceleration error gain parameter. */
    public static final ParameterTypeDouble KA =
            new ParameterTypeDouble("ka", "Acceleration error gain", 1.0, NumericConstraint.POSITIVE);

    /**
     * Constructor using default sensors with no delay.
     * @param delayedActuation DelayedActuation; delayed actuation
     */
    public LinearCACC(final DelayedActuation delayedActuation)
    {
        super(delayedActuation);
    }

    /** {@inheritDoc} */
    @Override
    public Acceleration getFollowingAcceleration(final LaneBasedGTU gtu,
            final PerceptionCollectable<HeadwayGTU, LaneBasedGTU> leaders, final Parameters settings) throws ParameterException
    {
        HeadwayGTU leader = leaders.first();
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
