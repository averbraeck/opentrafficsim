package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Desire;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsParameters;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.VoluntaryIncentive;

/**
 * Determines lane change desire in order to adhere to keeping right or left. Such desire only exists if the route and speed
 * (considered within an anticipation distance) are not affected on the adjacent lane. The level of lane change desire is only
 * sufficient to overcome the lowest threshold for free lane changes.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class IncentiveKeep implements VoluntaryIncentive
{

    /** Free lane change threshold parameter type. */
    protected static final ParameterTypeDouble DFREE = LmrsParameters.DFREE;

    /** {@inheritDoc} */
    @Override
    public final Desire determineDesire(final Parameters parameters, final LanePerception perception,
            final CarFollowingModel carFollowingModel, final Desire mandatoryDesire, final Desire voluntaryDesire)
            throws ParameterException, OperationalPlanException
    {
        if (mandatoryDesire.getRight() < 0 || voluntaryDesire.getRight() < 0
                || !perception.getLaneStructure().getExtendedCrossSection().contains(RelativeLane.RIGHT))
        {
            // no desire to go right if more dominant incentives provide a negative desire to go right
            return new Desire(0, 0);
        }
        // keep right with dFree
        return new Desire(0, parameters.getParameter(DFREE));
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "IncentiveKeep";
    }

}
