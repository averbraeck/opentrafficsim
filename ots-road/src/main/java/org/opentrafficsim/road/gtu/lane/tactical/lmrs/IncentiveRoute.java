package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import java.util.SortedSet;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeDuration;
import org.opentrafficsim.base.parameters.ParameterTypeLength;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.Stateless;
import org.opentrafficsim.core.gtu.perception.EgoPerception;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Desire;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.MandatoryIncentive;
import org.opentrafficsim.road.network.LaneChangeInfo;

/**
 * Determines desire by assessing the number of required lane change to be performed and the distance within which these have to
 * be performed. Desire starts to increase from 0 linearly over a distance of x0 per required lane change, or per v*t0 per
 * required lane change. For v&gt;x0/t0 this gives that remaining time is critical, while for v&lt;x0/t0 remaining space is
 * critical. The desire is set towards the adjacent lane with a better situation. Negative desire towards the other lane, the
 * extent of which pertains to the other adjacent lane, is also set.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class IncentiveRoute implements MandatoryIncentive, Stateless<IncentiveRoute>
{

    /** Look ahead parameter type. */
    protected static final ParameterTypeLength LOOKAHEAD = ParameterTypes.LOOKAHEAD;

    /** Look-ahead time for mandatory lane changes parameter type. */
    public static final ParameterTypeDuration T0 = ParameterTypes.T0;

    /** Singleton instance. */
    public static final IncentiveRoute SINGLETON = new IncentiveRoute();

    @Override
    public IncentiveRoute get()
    {
        return SINGLETON;
    }

    /**
     * Constructor.
     */
    private IncentiveRoute()
    {
        //
    }

    @Override
    public Desire determineDesire(final Parameters parameters, final LanePerception perception,
            final CarFollowingModel carFollowingModel, final Desire mandatoryDesire)
            throws ParameterException, OperationalPlanException
    {
        Speed speed = perception.getPerceptionCategory(EgoPerception.class).getSpeed();
        InfrastructurePerception infra = perception.getPerceptionCategory(InfrastructurePerception.class);

        // desire to leave current lane
        SortedSet<LaneChangeInfo> currentInfo = infra.getLegalLaneChangeInfo(RelativeLane.CURRENT);
        Length currentFirst = currentInfo.isEmpty() || currentInfo.first().numberOfLaneChanges() == 0 ? Length.POSITIVE_INFINITY
                : currentInfo.first().remainingDistance();
        double dCurr = getDesireToLeave(parameters, infra, RelativeLane.CURRENT, speed);
        double dLeft = 0;
        if (perception.getLaneStructure().exists(RelativeLane.LEFT)
                && infra.getLegalLaneChangePossibility(RelativeLane.CURRENT, LateralDirectionality.LEFT).neg().lt(currentFirst))
        {
            // desire to leave left lane
            dLeft = getDesireToLeave(parameters, infra, RelativeLane.LEFT, speed);
            // desire to leave from current to left lane
            dLeft = dLeft < dCurr ? dCurr : dLeft > dCurr ? -dLeft : 0;
        }
        double dRigh = 0;
        if (perception.getLaneStructure().exists(RelativeLane.RIGHT) && infra
                .getLegalLaneChangePossibility(RelativeLane.CURRENT, LateralDirectionality.RIGHT).neg().lt(currentFirst))
        {
            // desire to leave right lane
            dRigh = getDesireToLeave(parameters, infra, RelativeLane.RIGHT, speed);
            // desire to leave from current to right lane
            dRigh = dRigh < dCurr ? dCurr : dRigh > dCurr ? -dRigh : 0;
        }
        return new Desire(dLeft, dRigh);
    }

    /**
     * Calculates desire to leave a lane.
     * @param params parameters
     * @param infra infrastructure perception
     * @param lane relative lane to evaluate
     * @param speed speed
     * @return desire to leave a lane
     * @throws ParameterException in case of a parameter exception
     * @throws OperationalPlanException in case of perception exceptions
     */
    private static double getDesireToLeave(final Parameters params, final InfrastructurePerception infra,
            final RelativeLane lane, final Speed speed) throws ParameterException, OperationalPlanException
    {
        double dOut = 0.0;
        if (infra.getCrossSection().contains(lane))
        {
            for (LaneChangeInfo info : infra.getLegalLaneChangeInfo(lane))
            {
                double d = info.remainingDistance().lt0() ? info.numberOfLaneChanges()
                        : getDesireToLeave(params, info.remainingDistance(), info.numberOfLaneChanges(), speed);
                dOut = d > dOut ? d : dOut;
            }
        }
        return dOut;
    }

    /**
     * Calculates desire to leave a lane for a single infrastructure info.
     * @param params parameters
     * @param x remaining distance for lane changes
     * @param n number of required lane changes
     * @param v current speed
     * @return desire to leave a lane for a single infrastructure info
     * @throws ParameterException in case of a parameter exception
     */
    public static double getDesireToLeave(final Parameters params, final Length x, final int n, final Speed v)
            throws ParameterException
    {
        double d1 = 1 - x.si / (n * params.getParameter(LOOKAHEAD).si);
        double d2 = 1 - (x.si / v.si) / (n * params.getParameter(T0).si);
        d1 = d2 > d1 ? d2 : d1;
        return d1 < 0 ? 0 : d1;
    }

    @Override
    public String toString()
    {
        return "IncentiveRoute";
    }

}
