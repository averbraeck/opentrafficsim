package org.opentrafficsim.road.gtu.tactical.lmrs;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.immutablecollections.ImmutableLinkedHashMap;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.LaneBasedGtu;
import org.opentrafficsim.road.gtu.perception.RelativeLane;
import org.opentrafficsim.road.gtu.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.tactical.TacticalContextEgo;
import org.opentrafficsim.road.gtu.tactical.util.lmrs.Desire;
import org.opentrafficsim.road.gtu.tactical.util.lmrs.VoluntaryIncentive;
import org.opentrafficsim.road.network.LaneChangeInfo;

/**
 * Incentive involving changing lane regarding the route and infrastructure. This incentive regards a voluntary
 * as-soon-as-possible courtesy in changing lane for the route. For example at the start of weaving sections and ramps. While it
 * creates strong voluntary desire to change lane, it does not guarantee lane changes. Other voluntary incentives may counteract
 * it. For this reason a mandatory incentive regarding the route remains a necessity.
 * <p>
 * Note that this is a state-full incentive.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.<br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * @author Wouter Schakel
 */
public class IncentiveRouteCourtesy implements VoluntaryIncentive, EventListener
{

    /** Last opportunity. */
    private Opportunity opportunity;

    /** Whether this incentive is already listening to lane change events. */
    private boolean listening;

    @Override
    public Desire determineDesire(final TacticalContextEgo context, final Desire mandatoryDesire,
            final ImmutableLinkedHashMap<Class<? extends VoluntaryIncentive>, Desire> voluntaryDesire)
            throws ParameterException, OperationalPlanException
    {
        if (!this.listening)
        {
            context.getUnsafeGtu().addListener(this, LaneBasedGtu.LANE_CHANGE_EVENT);
            this.listening = true;
        }

        // State machine:
        // opportunity == null -> first opportunity with x > 0
        // opportunity (n, x) -> continuation of opportunity; allow x to reduce to 0 (i.e. in the ramp/weaving section)
        // lane change -> reduce/increase n by 1; if 0, set opportunity to null (see notify())
        InfrastructurePerception infra = context.getPerception().getPerceptionCategory(InfrastructurePerception.TYPE);
        Opportunity next = null;
        boolean firstInfo = true;
        for (LaneChangeInfo lcInfo : infra.getCourtesyLaneChangeInfo(RelativeLane.CURRENT))
        {
            if (lcInfo.remainingDistance().si > 0.0 || opportunityContinuation(lcInfo, firstInfo))
            {
                next = toOpportunity(lcInfo);
                break;
            }
            firstInfo = false;
        }

        Desire d;
        if (next != null)
        {
            Length x0 = context.getParameters().getParameter(ParameterTypes.LOOKAHEAD);
            Duration t0 = context.getParameters().getParameter(ParameterTypes.T0);
            double dCur = IncentiveRoute.getDesireToLeave(x0, t0, next.remainingDistance(), next.n(), context.getSpeed());
            double dAdj = IncentiveRoute.getDesireToLeave(x0, t0, next.remainingDistance(), next.n() + 1, context.getSpeed());
            d = next.direction().isLeft() ? new Desire(dCur, -dAdj) : new Desire(-dAdj, dCur);
        }
        else
        {
            d = Desire.ZERO;
        }
        this.opportunity = next;
        return d;
    }

    /**
     * Returns whether the input is a valid continuation of the existing opportunity.
     * @param lcInfo lane change info
     * @param firstInfo whether the given lane change information is the first
     * @return whether the input is a valid continuation of the existing opportunity
     */
    private boolean opportunityContinuation(final LaneChangeInfo lcInfo, final boolean firstInfo)
    {
        return firstInfo && this.opportunity != null && this.opportunity.n() == lcInfo.numberOfLaneChanges();
    }

    /**
     * Translates {@link LaneChangeInfo} to {@link Opportunity}.
     * @param lcInfo lane change info
     * @return opportunity
     */
    private static Opportunity toOpportunity(final LaneChangeInfo lcInfo)
    {
        return new Opportunity(lcInfo.numberOfLaneChanges(), lcInfo.remainingDistance(), lcInfo.lateralDirectionality());
    }

    @Override
    public void notify(final Event event)
    {
        if (this.opportunity != null)
        {
            LateralDirectionality dir = LateralDirectionality.valueOf((String) ((Object[]) event.getContent())[1]);
            int n = this.opportunity.n() + (this.opportunity.direction().equals(dir) ? -1 : 1);
            // if n == 0; forget opportunity; goal is reached
            this.opportunity =
                    n <= 0 ? null : new Opportunity(n, this.opportunity.remainingDistance(), this.opportunity.direction());
        }
    }

    /**
     * Opportunity for route related lane changes.
     * @author Wouter Schakel
     * @param n required number of lane changes
     * @param remainingDistance remaining distance
     * @param direction lateral directionality of required lane changes
     */
    private record Opportunity(int n, Length remainingDistance, LateralDirectionality direction)
    {
    }

}
