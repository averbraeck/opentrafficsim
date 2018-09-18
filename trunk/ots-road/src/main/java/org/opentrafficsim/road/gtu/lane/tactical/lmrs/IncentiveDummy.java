package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Desire;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.MandatoryIncentive;

/**
 * Dummy desire disabling lane changes when used as the only incentive.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 13, 2016 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class IncentiveDummy implements MandatoryIncentive
{

    /** {@inheritDoc} */
    @Override
    public final Desire determineDesire(final Parameters parameters,
            final LanePerception perception, final CarFollowingModel carFollowingModel, final Desire mandatoryDesire)
            throws ParameterException, OperationalPlanException
    {
        return new Desire(0, 0);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "IncentiveDummy";
    }

}
