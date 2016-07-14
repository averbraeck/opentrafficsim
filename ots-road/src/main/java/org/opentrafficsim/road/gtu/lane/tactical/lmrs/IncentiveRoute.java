package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypes;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.InfrastructureLaneChangeInfo;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;

/**
 * Determines desire by assessing the number of required lane change to be performed and the distance within which these have to
 * be performed. Desire starts to increase from 0 linearly over a distance of x0 per required lane change, or per v*t0 per
 * required lane change. For v&gt;x0/t0 this gives that remaining time is critical, while for v&lt;x0/t0 remaining space is
 * critical. The desire is set towards the adjacent lane with a better situation. Negative desire towards the other lane, the
 * extent of which pertains to the other adjacent lane, is also set.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 13, 2016 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class IncentiveRoute implements MandatoryIncentive
{

    /** {@inheritDoc} */
    @Override
    public final Desire determineDesire(final LaneBasedGTU gtu, final Desire mandatoryDesire) throws ParameterException
    {
        BehavioralCharacteristics bc = gtu.getBehavioralCharacteristics();
        LanePerception perception = gtu.getPerception();
        Speed v = gtu.getSpeed();
        // desire to leave each lane
        double dLeft = getDesireToLeave(bc, perception, RelativeLane.LEFT, v);
        double dCurr = getDesireToLeave(bc, perception, RelativeLane.CURRENT, v);
        double dRigh = getDesireToLeave(bc, perception, RelativeLane.RIGHT, v);
        // change to desire to change left and right
        dLeft = dLeft < dCurr ? dCurr : dLeft > dCurr ? -dLeft : 0;
        dRigh = dRigh < dCurr ? dCurr : dRigh > dCurr ? -dRigh : 0;
        return new Desire(dLeft, dRigh);
    }

    /**
     * Calculates desire to leave a lane.
     * @param bc behavioral characteristics
     * @param perception perception
     * @param lane relative lane to evaluate
     * @param v current speed
     * @return desire to leave a lane
     * @throws ParameterException in case of a parameter exception
     */
    private double getDesireToLeave(final BehavioralCharacteristics bc, final LanePerception perception,
        final RelativeLane lane, final Speed v) throws ParameterException
    {
        double dOut = Double.NEGATIVE_INFINITY;
        if (perception.getCurrentCrossSection().contains(lane))
        {
            for (InfrastructureLaneChangeInfo info : perception.getInfrastructureLaneChangeInfo(lane))
            {
                double d = getDesireToLeave(bc, info.getRemainingDistance(), info.getRequiredNumberOfLaneChanges(), v);
                dOut = d > dOut ? d : dOut;
            }
        }
        return dOut;
    }

    /**
     * Calculates desire to leave a lane for a single infrastructure info.
     * @param bc behavioral characteristics
     * @param x remaining distance for lane changes
     * @param n number of required lane changes
     * @param v current speed
     * @return desire to leave a lane for a single infrastructure info
     * @throws ParameterException in case of a parameter exception
     */
    private double getDesireToLeave(final BehavioralCharacteristics bc, final Length x, final int n, final Speed v)
        throws ParameterException
    {
        double d1 = 1 - x.si / (n * bc.getParameter(ParameterTypes.LOOKAHEAD).si);
        double d2 = 1 - (x.si / v.si) / (n * bc.getParameter(ParameterTypes.T0).si);
        d1 = d2 > d1 ? d2 : d1;
        return d1 < 0 ? 0 : d1;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "IncentiveRoute";
    }

}
