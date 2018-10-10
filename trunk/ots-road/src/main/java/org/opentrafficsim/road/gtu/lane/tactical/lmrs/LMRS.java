package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import java.util.LinkedHashSet;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeClassList;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.base.parameters.constraint.ClassCollectionConstraint;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.perception.EgoPerception;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.animation.Blockable;
import org.opentrafficsim.road.gtu.animation.DesireBased;
import org.opentrafficsim.road.gtu.animation.Synchronizable;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.DelayedNeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.plan.operational.LaneChange;
import org.opentrafficsim.road.gtu.lane.plan.operational.LaneOperationalPlanBuilder;
import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.lane.tactical.AbstractLaneBasedTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Cooperation;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Desire;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.GapAcceptance;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Incentive;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsData;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsParameters;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsUtil;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.MandatoryIncentive;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Synchronization;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Tailgating;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.VoluntaryIncentive;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;
import org.opentrafficsim.road.network.speed.SpeedLimitProspect;

import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * Implementation of the LMRS (Lane change Model with Relaxation and Synchronization). See Schakel, W.J., Knoop, V.L., and Van
 * Arem, B. (2012), <a href="http://victorknoop.eu/research/papers/TRB2012_LMRS_reviewed.pdf">LMRS: Integrated Lane Change Model
 * with Relaxation and Synchronization</a>, Transportation Research Records: Journal of the Transportation Research Board, No.
 * 2316, pp. 47-57. Note in the official versions of TRB and TRR some errors appeared due to the typesetting of the papers (not
 * in the preprint provided here). A list of errata for the official versions is found
 * <a href="http://victorknoop.eu/research/papers/Erratum_LMRS.pdf">here</a>.
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 13, 2016 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class LMRS extends AbstractLaneBasedTacticalPlanner implements DesireBased, Synchronizable, Blockable
{

    /** Parameter type for mandatory lane change incentives. */
    public static final ParameterTypeClassList<MandatoryIncentive> MANDATORY = new ParameterTypeClassList<>("man.incent.",
            "Mandatory lane-change incentives.", ParameterTypeClassList.getValueClass(MandatoryIncentive.class));

    /** Parameter type for voluntary lane change incentives. */
    public static final ParameterTypeClassList<VoluntaryIncentive> VOLUNTARY = new ParameterTypeClassList<>("vol.incent.",
            "Voluntary lane-change incentives.", ParameterTypeClassList.getValueClass(VoluntaryIncentive.class));

    /** Parameter type for acceleration incentives. */
    public static final ParameterTypeClassList<AccelerationIncentive> ACCELERATION = new ParameterTypeClassList<>("acc.incent.",
            "Acceleration incentives.", ParameterTypeClassList.getValueClass(AccelerationIncentive.class),
            ClassCollectionConstraint.newInstance(AccelerationBusStop.class));

    /** Serialization id. */
    private static final long serialVersionUID = 20160300L;

    /** Lane change status. */
    private final LaneChange laneChange = new LaneChange();

    /** LMRS data. */
    private final LmrsData lmrsData;

    /** Set of mandatory lane change incentives. */
    private final LinkedHashSet<MandatoryIncentive> mandatoryIncentives = new LinkedHashSet<>();

    /** Set of voluntary lane change incentives. */
    private final LinkedHashSet<VoluntaryIncentive> voluntaryIncentives = new LinkedHashSet<>();

    /** Set of acceleration incentives. */
    private final LinkedHashSet<AccelerationIncentive> accelerationIncentives = new LinkedHashSet<>();

    /**
     * Constructor setting the car-following model.
     * @param carFollowingModel Car-following model.
     * @param gtu GTU
     * @param lanePerception perception
     * @param synchronization type of synchronization
     * @param cooperation type of cooperation
     * @param gapAcceptance gap-acceptance
     * @param tailGating tail gating
     */
    public LMRS(final CarFollowingModel carFollowingModel, final LaneBasedGTU gtu, final LanePerception lanePerception,
            final Synchronization synchronization, final Cooperation cooperation, final GapAcceptance gapAcceptance,
            final Tailgating tailGating)
    {
        super(carFollowingModel, gtu, lanePerception);
        this.lmrsData = new LmrsData(synchronization, cooperation, gapAcceptance, tailGating);
    }

    /**
     * Adds a mandatory incentive. Ignores {@code null}.
     * @param incentive MandatoryIncentive; Incentive to add.
     */
    public final void addMandatoryIncentive(final MandatoryIncentive incentive)
    {
        if (incentive != null)
        {
            this.mandatoryIncentives.add(incentive);
        }
    }

    /**
     * Adds a voluntary incentive. Ignores {@code null}.
     * @param incentive VoluntaryIncentive; Incentive to add.
     */
    public final void addVoluntaryIncentive(final VoluntaryIncentive incentive)
    {
        if (incentive != null)
        {
            this.voluntaryIncentives.add(incentive);
        }
    }

    /**
     * Adds an acceleration incentive. Ignores {@code null}.
     * @param incentive AccelerationIncentive; Incentive to add.
     */
    public final void addAccelerationIncentive(final AccelerationIncentive incentive)
    {
        if (incentive != null)
        {
            this.accelerationIncentives.add(incentive);
        }
    }

    /**
     * Sets the default lane change incentives.
     */
    public final void setDefaultIncentives()
    {
        this.mandatoryIncentives.clear();
        this.voluntaryIncentives.clear();
        this.accelerationIncentives.clear();
        this.mandatoryIncentives.add(new IncentiveRoute());
        // this.mandatoryIncentives.add(new IncentiveGetInLane());
        this.voluntaryIncentives.add(new IncentiveSpeedWithCourtesy());
        this.voluntaryIncentives.add(new IncentiveKeep());
        this.accelerationIncentives.add(new AccelerationSpeedLimitTransition());
        this.accelerationIncentives.add(new AccelerationTrafficLights());
        this.accelerationIncentives.add(new AccelerationConflicts());
    }

    /** {@inheritDoc} */
    @Override
    public final OperationalPlan generateOperationalPlan(final Time startTime, final DirectedPoint locationAtStartTime)
            throws OperationalPlanException, GTUException, NetworkException, ParameterException
    {

        // obtain objects to get info
        getPerception().perceive();
        SpeedLimitProspect slp = getPerception().getPerceptionCategory(InfrastructurePerception.class)
                .getSpeedLimitProspect(RelativeLane.CURRENT);
        SpeedLimitInfo sli = slp.getSpeedLimitInfo(Length.ZERO);
        Parameters params = getGtu().getParameters();

        // LMRS
        SimpleOperationalPlan simplePlan = LmrsUtil.determinePlan(getGtu(), startTime, getCarFollowingModel(), this.laneChange,
                this.lmrsData, getPerception(), this.mandatoryIncentives, this.voluntaryIncentives);

        // Lower acceleration from additional sources, consider adjacent lane when changing lane or synchronizing
        Speed speed = getPerception().getPerceptionCategory(EgoPerception.class).getSpeed();
        RelativeLane[] lanes;
        double dLeft = params.getParameterOrNull(LmrsParameters.DLEFT);
        double dRight = params.getParameterOrNull(LmrsParameters.DRIGHT);
        double dSync = params.getParameterOrNull(LmrsParameters.DSYNC);
        if (this.laneChange.isChangingLane())
        {
            lanes = new RelativeLane[] { RelativeLane.CURRENT, this.laneChange.getSecondLane(getGtu()) };
        }
        else if (dLeft >= dSync && dLeft >= dRight)
        {
            lanes = new RelativeLane[] { RelativeLane.CURRENT, RelativeLane.LEFT };
        }
        else if (dRight >= dSync)
        {
            lanes = new RelativeLane[] { RelativeLane.CURRENT, RelativeLane.RIGHT };
        }
        else
        {
            lanes = new RelativeLane[] { RelativeLane.CURRENT };
        }
        for (AccelerationIncentive incentive : this.accelerationIncentives)
        {
            for (RelativeLane lane : lanes)
            {
                incentive.accelerate(simplePlan, lane, getGtu(), getPerception(), getCarFollowingModel(), speed, params, sli);
            }
        }

        if (simplePlan.isLaneChange())
        {
            this.laneChange.setDesiredLaneChangeDuration(getGtu().getParameters().getParameter(ParameterTypes.LCDUR));
            // adjust lane based data in perception
            // TODO make this automatic within the perception itself, e.g. by a lane change event from the tactical planner
            if (getPerception().contains(DelayedNeighborsPerception.class))
            {
                getPerception().getPerceptionCategory(DelayedNeighborsPerception.class)
                        .changeLane(simplePlan.getLaneChangeDirection());
            }
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
    public State getSynchronizationState()
    {
        return this.lmrsData.getSynchronizationState();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isBlocking()
    {
        for (AccelerationIncentive acc : this.accelerationIncentives)
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
        return "LMRS [mandatoryIncentives=" + this.mandatoryIncentives + ", voluntaryIncentives=" + this.voluntaryIncentives
                + ", accelerationIncentives = " + this.accelerationIncentives + "]";
    }

}
