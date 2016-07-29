package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;

/**
 * Determines desire out of hierarchal courtesy. For right-hand driving this is towards the right if the follower has a higher
 * desired speed. If the left follower has a higher desired speed, a negative desire towards the left exists. For left-hand
 * driving it is the other way around. Hierarchal desire depends on the level of hierarchal courtesy.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 13, 2016 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class IncentiveHierarchal implements VoluntaryIncentive
{

    /** {@inheritDoc} */
    @Override
    public final Desire determineDesire(final BehavioralCharacteristics behavioralCharacteristics,
        final LanePerception perception, final Desire mandatoryDesire, final Desire voluntaryDesire)
        throws ParameterException, OperationalPlanException
    {
        return new Desire(0, 0); // XXXXX STUB
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "IncentiveHierarchal";
    }

}
