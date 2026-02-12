package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.point.DirectedPoint2d;
import org.opentrafficsim.base.DistancedObject;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.TurnIndicatorStatus;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.plan.operational.LaneOperationalPlanBuilder;
import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.lane.tactical.Blockable;
import org.opentrafficsim.road.gtu.lane.tactical.Synchronizable;
import org.opentrafficsim.road.gtu.lane.tactical.TacticalContextEgo;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.util.DeadEnUtil;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Cooperation;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.GapAcceptance;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsData;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsParameters;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsUtil;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Synchronization;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Tailgating;

/**
 * Implementation of the LMRS (Lane change Model with Relaxation and Synchronization). See Schakel, W.J., Knoop, V.L., and Van
 * Arem, B. (2012), <a href="http://victorknoop.eu/research/papers/TRB2012_LMRS_reviewed.pdf">LMRS: Integrated Lane Change Model
 * with Relaxation and Synchronization</a>, Transportation Research Records: Journal of the Transportation Research Board, No.
 * 2316, pp. 47-57. Note in the official versions of TRB and TRR some errors appeared due to the typesetting of the papers (not
 * in the preprint provided here). A list of errata for the official versions is found
 * <a href="http://victorknoop.eu/research/papers/Erratum_LMRS.pdf">here</a>.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class Lmrs extends AbstractIncentivesTacticalPlanner implements Synchronizable, Blockable
{

    /** Deviation object in case of no desired deviation. */
    private static final DistancedObject<Length> NO_DEVIATION = new DistancedObject<>(Length.ZERO, Length.ZERO);

    /** LMRS data. */
    private final LmrsData lmrsData;

    /**
     * Constructor setting the car-following model.
     * @param carFollowingModel car-following model
     * @param gtu GTU
     * @param lanePerception perception
     * @param synchronization type of synchronization
     * @param cooperation type of cooperation
     * @param gapAcceptance gap-acceptance
     * @param tailgating tailgating
     */
    public Lmrs(final CarFollowingModel carFollowingModel, final LaneBasedGtu gtu, final LanePerception lanePerception,
            final Synchronization synchronization, final Cooperation cooperation, final GapAcceptance gapAcceptance,
            final Tailgating tailgating)
    {
        super(carFollowingModel, gtu, lanePerception);
        this.lmrsData = new LmrsData(synchronization, cooperation, gapAcceptance, tailgating);
    }

    @Override
    public final OperationalPlan generateOperationalPlan(final Duration startTime, final DirectedPoint2d locationAtStartTime)
            throws GtuException, NetworkException, ParameterException
    {
        // Create tactical context
        TacticalContextEgo context = new TacticalContextEgo(getGtu());

        // LMRS
        SimpleOperationalPlan simplePlan = LmrsUtil.determinePlan(context, this.lmrsData, this);

        // Lower acceleration from additional sources, consider adjacent lane when changing lane or synchronizing
        RelativeLane[] lanes;
        double dLeft = context.getParameters().getParameter(LmrsParameters.DLEFT);
        double dRight = context.getParameters().getParameter(LmrsParameters.DRIGHT);
        double dSync = context.getParameters().getParameter(LmrsParameters.DSYNC);
        if (dLeft >= dSync && dLeft >= dRight)
        {
            lanes = new RelativeLane[] {RelativeLane.CURRENT, RelativeLane.LEFT};
        }
        else if (dRight >= dSync)
        {
            lanes = new RelativeLane[] {RelativeLane.CURRENT, RelativeLane.RIGHT};
        }
        else
        {
            lanes = new RelativeLane[] {RelativeLane.CURRENT};
        }
        for (RelativeLane lane : lanes)
        {
            if (getPerception().getLaneStructure().getRootCrossSection().contains(lane))
            {
                // On the current lane, consider all incentives. On adjacent lanes only consider incentives beyond the distance
                // over which a lane change is not yet possible, i.e. the merge distance.
                // TODO: consider route in incentives (only if not on current lane?)
                Length mergeDistance = lane.isCurrent() ? Length.ZERO
                        : Synchronization.getMergeDistance(getPerception(), lane.getLateralDirectionality());
                simplePlan.minimizeAcceleration(getAcceleration(context, lane, mergeDistance));
            }
        }

        // deal with dead-end situations
        simplePlan = DeadEnUtil.dealWithDeadEnd(context, simplePlan);

        // set turn indicator
        context.getIntent(TurnIndicatorStatus.class).ifPresentOrElse((d) -> getGtu().setTurnIndicatorStatus(d.object()),
                () -> getGtu().setTurnIndicatorStatus(TurnIndicatorStatus.NONE));

        // create plan
        return LaneOperationalPlanBuilder.buildPlanFromSimplePlan(getGtu(), simplePlan,
                getGtu().getParameters().getParameter(ParameterTypes.LCDUR),
                context.getIntent(Length.class).orElse(NO_DEVIATION));

    }

    @Override
    public Synchronizable.State getSynchronizationState()
    {
        return this.lmrsData.getSynchronizationState();
    }

    @Override
    public boolean isBlocking()
    {
        for (AccelerationIncentive acc : getAccelerationIncentives())
        {
            if (acc instanceof AccelerationConflicts)
            {
                return ((AccelerationConflicts) acc).isBlocking();
            }
        }
        return false;
    }

    @Override
    public final String toString()
    {
        return "LMRS [mandatoryIncentives=" + getMandatoryIncentives() + ", voluntaryIncentives=" + getVoluntaryIncentives()
                + ", accelerationIncentives = " + getAccelerationIncentives() + "]";
    }

}
