package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;

/**
 * Dummy desire disabling lane changes when used as the only incentive.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 13, 2016 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class IncentiveDummy implements MandatoryIncentive
{

    /** {@inheritDoc} */
    @Override
    public final Desire determineDesire(final LaneBasedGTU gtu, final Desire mandatoryDesire) throws ParameterException
    {
        return new Desire(0, 0); // XXXXX STUB
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "IncentiveDummy";
    }

}
