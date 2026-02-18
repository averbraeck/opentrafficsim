package org.opentrafficsim.road.gtu.tactical.lmrs;

import java.util.SortedSet;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.immutablecollections.ImmutableLinkedHashMap;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeDuration;
import org.opentrafficsim.base.parameters.ParameterTypeLength;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.gtu.Stateless;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.perception.RelativeLane;
import org.opentrafficsim.road.gtu.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.tactical.TacticalContextEgo;
import org.opentrafficsim.road.gtu.tactical.util.lmrs.Desire;
import org.opentrafficsim.road.gtu.tactical.util.lmrs.MandatoryIncentive;
import org.opentrafficsim.road.network.LaneChangeInfo;

/**
 * Determines desire by assessing the number of required lane change to be performed and the distance within which these have to
 * be performed. Desire starts to increase from 0 linearly over a distance of x0 per required lane change, or per v*t0 per
 * required lane change. For v&gt;x0/t0 this gives that remaining time is critical, while for v&lt;x0/t0 remaining space is
 * critical. The desire is set towards the adjacent lane with a better situation. Negative desire towards the other lane, the
 * extent of which pertains to the other adjacent lane, is also set.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
    public Desire determineDesire(final TacticalContextEgo context,
            final ImmutableLinkedHashMap<Class<? extends MandatoryIncentive>, Desire> mandatoryDesire)
            throws ParameterException, OperationalPlanException
    {
        // desire to leave current lane
        InfrastructurePerception infra = context.getPerception().getPerceptionCategory(InfrastructurePerception.class);
        SortedSet<LaneChangeInfo> currentInfo = infra.getLegalLaneChangeInfo(RelativeLane.CURRENT);
        Length currentFirst = currentInfo.isEmpty() || currentInfo.first().numberOfLaneChanges() == 0 ? Length.POSITIVE_INFINITY
                : currentInfo.first().remainingDistance();
        double dCurr = getDesireToLeave(context, RelativeLane.CURRENT);

        // left
        double dLeft = 0.0;
        if (context.getPerception().getLaneStructure().exists(RelativeLane.LEFT)
                && infra.getLegalLaneChangePossibility(RelativeLane.CURRENT, LateralDirectionality.LEFT).neg().lt(currentFirst))
        {
            // desire to leave left lane
            dLeft = getDesireToLeave(context, RelativeLane.LEFT);
            // desire to leave from current to left lane
            dLeft = dLeft < dCurr ? dCurr : dLeft > dCurr ? -dLeft : 0.0;
        }

        // right
        double dRigh = 0.0;
        if (context.getPerception().getLaneStructure().exists(RelativeLane.RIGHT) && infra
                .getLegalLaneChangePossibility(RelativeLane.CURRENT, LateralDirectionality.RIGHT).neg().lt(currentFirst))
        {
            // desire to leave right lane
            dRigh = getDesireToLeave(context, RelativeLane.RIGHT);
            // desire to leave from current to right lane
            dRigh = dRigh < dCurr ? dCurr : dRigh > dCurr ? -dRigh : 0.0;
        }

        return new Desire(dLeft, dRigh);
    }

    /**
     * Calculates desire to leave a lane.
     * @param context tactical information such as parameters and car-following model
     * @param lane relative lane to evaluate
     * @return desire to leave a lane
     * @throws ParameterException in case of a parameter exception
     * @throws OperationalPlanException in case of perception exceptions
     */
    public static double getDesireToLeave(final TacticalContextEgo context, final RelativeLane lane)
            throws ParameterException, OperationalPlanException
    {
        InfrastructurePerception infra = context.getPerception().getPerceptionCategory(InfrastructurePerception.class);
        Length x0 = context.getParameters().getParameter(LOOKAHEAD);
        Duration t0 = context.getParameters().getParameter(T0);
        double dOut = 0.0;
        if (infra.getCrossSection().contains(lane))
        {
            for (LaneChangeInfo info : infra.getLegalLaneChangeInfo(lane))
            {
                double d = info.remainingDistance().lt0() ? info.numberOfLaneChanges()
                        : getDesireToLeave(x0, t0, info.remainingDistance(), info.numberOfLaneChanges(), context.getSpeed());
                dOut = d > dOut ? d : dOut;
            }
        }
        return dOut;
    }

    /**
     * Calculates desire to leave a lane for a single infrastructure info.
     * @param x0 relevant distance per lane change
     * @param t0 relevant time per lane change
     * @param x remaining distance for lane changes
     * @param n number of required lane changes
     * @param v current speed
     * @return desire to leave a lane for a single infrastructure info
     * @throws ParameterException in case of a parameter exception
     */
    public static double getDesireToLeave(final Length x0, final Duration t0, final Length x, final int n, final Speed v)
            throws ParameterException
    {
        double d1 = 1 - x.si / (n * x0.si);
        double d2 = 1 - (x.si / v.si) / (n * t0.si);
        d1 = d2 > d1 ? d2 : d1;
        return d1 < 0 ? 0 : d1;
    }

    @Override
    public String toString()
    {
        return "IncentiveRoute";
    }

}
