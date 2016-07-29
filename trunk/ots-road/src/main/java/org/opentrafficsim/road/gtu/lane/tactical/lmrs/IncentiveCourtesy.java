package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;

/**
 * Determines lane change desire for courtesy lane changes, which are performed to supply space for other drivers. In case
 * drivers in adjacent lanes have desire to change to the current lane, the driver has desire to change to the other adjacent
 * lane. The level of desire depends on lane change courtesy, as well as the distance of the leading vehicle for which desire
 * exists. This desire exists for only a single vehicle, i.e. the one giving maximum desire. A negative desire may also result
 * for leaders in the 2nd adjacent lane desiring to change to the 1st adjacent lane. By not changing to the 1st adjacent lane,
 * room is reserved for the leader on the 2nd adjacent lane.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 13, 2016 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class IncentiveCourtesy implements VoluntaryIncentive
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
        return "IncentiveCourtesy";
    }

}
