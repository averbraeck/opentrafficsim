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
 * Linear CACC implementation based on derivatives by Jeroen Ploeg.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Mar 13, 2019 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class PloegCACC extends LinearCACC
{

    /**
     * Constructor using default sensors with no delay.
     * @param delayedActuation DelayedActuation; delayed actuation
     */
    public PloegCACC(final DelayedActuation delayedActuation)
    {
        super(delayedActuation);
    }

    /** Gap error derivative gain parameter. */
    public static final ParameterTypeDouble KD =
            new ParameterTypeDouble("kd", "Gap error derivative gain", 0.7, NumericConstraint.POSITIVE);

    /** {@inheritDoc} */
    @Override
    public Acceleration getFollowingAcceleration(final LaneBasedGTU gtu,
            final PerceptionCollectable<HeadwayGTU, LaneBasedGTU> leaders) throws ParameterException
    {
        Parameters params = gtu.getParameters();
        HeadwayGTU leader = leaders.first();
        double es;
        double esd;
        double kaui;
        if (leader.getAcceleration() == null)
        {
            // ACC mode
            es = leader.getDistance().si - gtu.getSpeed().si * params.getParameter(TDACC).si - params.getParameter(X0).si;
            esd = leader.getSpeed().si - gtu.getSpeed().si - gtu.getAcceleration().si * params.getParameter(TDACC).si;
            kaui = 0.0;
        }
        else
        {
            // CACC mode
            es = leader.getDistance().si - gtu.getSpeed().si * params.getParameter(TDCACC).si - params.getParameter(X0).si;
            esd = leader.getSpeed().si - gtu.getSpeed().si - gtu.getAcceleration().si * params.getParameter(TDCACC).si;
            kaui = params.getParameter(KA) * leader.getAcceleration().si;
        }
        return Acceleration.createSI(params.getParameter(KS) * es + params.getParameter(KD) * esd + kaui);
    }

}
