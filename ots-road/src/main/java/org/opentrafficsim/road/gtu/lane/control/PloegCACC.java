package org.opentrafficsim.road.gtu.lane.control;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTU;

/**
 * Linear CACC implementation based on derivatives by Jeroen Ploeg.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class PloegCACC extends PloegACC
{

    /** Acceleration error gain parameter. */
    public static final ParameterTypeDouble KA = LinearCACC.KA;

    /**
     * Constructor using default sensors with no delay.
     * @param delayedActuation DelayedActuation; delayed actuation
     */
    public PloegCACC(final DelayedActuation delayedActuation)
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
            return super.getFollowingAcceleration(gtu, leaders, settings);
        }
        double es =
                leader.getDistance().si - gtu.getSpeed().si * settings.getParameter(TDCACC).si - settings.getParameter(X0).si;
        double esd = leader.getSpeed().si - gtu.getSpeed().si - gtu.getAcceleration().si * settings.getParameter(TDCACC).si;
        double kaui = settings.getParameter(KA) * leader.getAcceleration().si;
        return Acceleration.instantiateSI(settings.getParameter(KS) * es + settings.getParameter(KD) * esd + kaui);
    }

}
