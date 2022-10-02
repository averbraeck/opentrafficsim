package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.exceptions.Try;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.geometry.DirectedPoint;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.perception.EgoPerception;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.plan.operational.LaneChange;
import org.opentrafficsim.road.gtu.lane.plan.operational.LaneOperationalPlanBuilder;
import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.lane.tactical.Blockable;
import org.opentrafficsim.road.gtu.lane.tactical.DesireBased;
import org.opentrafficsim.road.gtu.lane.tactical.Synchronizable;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Cooperation;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Desire;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.GapAcceptance;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Incentive;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsData;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsParameters;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsUtil;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Synchronization;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Tailgating;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;
import org.opentrafficsim.road.network.speed.SpeedLimitProspect;

/**
 * Implementation of the LMRS (Lane change Model with Relaxation and Synchronization). See Schakel, W.J., Knoop, V.L., and Van
 * Arem, B. (2012), <a href="http://victorknoop.eu/research/papers/TRB2012_LMRS_reviewed.pdf">LMRS: Integrated Lane Change Model
 * with Relaxation and Synchronization</a>, Transportation Research Records: Journal of the Transportation Research Board, No.
 * 2316, pp. 47-57. Note in the official versions of TRB and TRR some errors appeared due to the typesetting of the papers (not
 * in the preprint provided here). A list of errata for the official versions is found
 * <a href="http://victorknoop.eu/research/papers/Erratum_LMRS.pdf">here</a>.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class Lmrs extends AbstractIncentivesTacticalPlanner implements DesireBased, Synchronizable, Blockable
{

    /** Serialization id. */
    private static final long serialVersionUID = 20160300L;

    /** Lane change status. */
    private final LaneChange laneChange;

    /** LMRS data. */
    private final LmrsData lmrsData;

    /**
     * Constructor setting the car-following model.
     * @param carFollowingModel CarFollowingModel; Car-following model.
     * @param gtu LaneBasedGtu; GTU
     * @param lanePerception LanePerception; perception
     * @param synchronization Synchronization; type of synchronization
     * @param cooperation Cooperation; type of cooperation
     * @param gapAcceptance GapAcceptance; gap-acceptance
     * @param tailgating Tailgating; tail gating
     */
    public Lmrs(final CarFollowingModel carFollowingModel, final LaneBasedGtu gtu, final LanePerception lanePerception,
            final Synchronization synchronization, final Cooperation cooperation, final GapAcceptance gapAcceptance,
            final Tailgating tailgating)
    {
        super(carFollowingModel, gtu, lanePerception);
        this.laneChange = Try.assign(() -> new LaneChange(gtu), "Parameter LCDUR is required.", GtuException.class);
        this.lmrsData = new LmrsData(synchronization, cooperation, gapAcceptance, tailgating);
    }

    /** {@inheritDoc} */
    @Override
    public final OperationalPlan generateOperationalPlan(final Time startTime, final DirectedPoint locationAtStartTime)
            throws OperationalPlanException, GtuException, NetworkException, ParameterException
    {
        // obtain objects to get info
        SpeedLimitProspect slp = getPerception().getPerceptionCategory(InfrastructurePerception.class)
                .getSpeedLimitProspect(RelativeLane.CURRENT);
        SpeedLimitInfo sli = slp.getSpeedLimitInfo(Length.ZERO);
        Parameters params = getGtu().getParameters();

        // LMRS
        SimpleOperationalPlan simplePlan = LmrsUtil.determinePlan(getGtu(), startTime, getCarFollowingModel(), this.laneChange,
                this.lmrsData, getPerception(), getMandatoryIncentives(), getVoluntaryIncentives());

        // Lower acceleration from additional sources, consider adjacent lane when changing lane or synchronizing
        Speed speed = getPerception().getPerceptionCategory(EgoPerception.class).getSpeed();
        RelativeLane[] lanes;
        double dLeft = params.getParameterOrNull(LmrsParameters.DLEFT);
        double dRight = params.getParameterOrNull(LmrsParameters.DRIGHT);
        double dSync = params.getParameterOrNull(LmrsParameters.DSYNC);
        if (this.laneChange.isChangingLane())
        {
            lanes = new RelativeLane[] {RelativeLane.CURRENT, this.laneChange.getSecondLane(getGtu())};
        }
        else if (dLeft >= dSync && dLeft >= dRight)
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
            // On the current lane, consider all incentives. On adjacent lanes only consider incentives beyond the distance over
            // which a lane change is not yet possible, i.e. the merge distance.
            // TODO: consider route in incentives (only if not on current lane?)
            Length mergeDistance = lane.isCurrent() ? Length.ZERO
                    : Synchronization.getMergeDistance(getPerception(), lane.getLateralDirectionality());
            for (AccelerationIncentive incentive : getAccelerationIncentives())
            {
                incentive.accelerate(simplePlan, lane, mergeDistance, getGtu(), getPerception(), getCarFollowingModel(), speed,
                        params, sli);
            }
        }

        if (simplePlan.isLaneChange())
        {
            this.laneChange.setDesiredLaneChangeDuration(getGtu().getParameters().getParameter(ParameterTypes.LCDUR));
            // adjust lane based data in perception
        }

        // set turn indicator
        simplePlan.setTurnIndicator(getGtu());

        // create plan
        return LaneOperationalPlanBuilder.buildPlanFromSimplePlan(getGtu(), startTime, simplePlan, this.laneChange);

    }

    /** {@inheritDoc} */
    @Override
    public final Desire getLatestDesire(final Class<? extends Incentive> incentiveClass)
    {
        return this.lmrsData.getLatestDesire(incentiveClass);
    }

    /** {@inheritDoc} */
    @Override
    public Synchronizable.State getSynchronizationState()
    {
        return this.lmrsData.getSynchronizationState();
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "LMRS [mandatoryIncentives=" + getMandatoryIncentives() + ", voluntaryIncentives=" + getVoluntaryIncentives()
                + ", accelerationIncentives = " + getAccelerationIncentives() + "]";
    }

}
