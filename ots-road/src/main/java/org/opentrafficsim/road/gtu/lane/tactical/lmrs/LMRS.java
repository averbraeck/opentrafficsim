package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import java.util.LinkedHashSet;

import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.IntersectionPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.lane.plan.operational.LaneOperationalPlanBuilder.LaneChange;
import org.opentrafficsim.road.gtu.lane.tactical.AbstractLaneBasedTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.util.ConflictUtil;
import org.opentrafficsim.road.gtu.lane.tactical.util.ConflictUtil.ConflictPlans;
import org.opentrafficsim.road.gtu.lane.tactical.util.SpeedLimitUtil;
import org.opentrafficsim.road.gtu.lane.tactical.util.TrafficLightUtil;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsUtil;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.MandatoryIncentive;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.VoluntaryIncentive;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsUtil.LmrsStatus;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;
import org.opentrafficsim.road.network.speed.SpeedLimitProspect;

/**
 * Implementation of the LMRS (Lane change Model with Relaxation and Synchronization). See Schakel, W.J., Knoop, V.L., and Van
 * Arem, B. (2012), <a href="http://victorknoop.eu/research/papers/TRB2012_LMRS_reviewed.pdf">LMRS: Integrated Lane Change Model
 * with Relaxation and Synchronization</a>, Transportation Research Records: Journal of the Transportation Research Board, No.
 * 2316, pp. 47-57. Note in the official versions of TRB and TRR some errors appeared due to the typesetting of the papers (not
 * in the preprint provided here). A list of errata for the official versions is found <a
 * href="http://victorknoop.eu/research/papers/Erratum_LMRS.pdf">here</a>.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 13, 2016 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class LMRS extends AbstractLaneBasedTacticalPlanner
{

    /** Serialization id. */
    private static final long serialVersionUID = 20160300L;

    /** Set of yield plans at conflicts with priority. Remembering for static model. */
    private final ConflictPlans yieldPlans = new ConflictPlans();

    /** Lane change status. */
    private final LaneChange laneChange = new LaneChange();

    /** LMRS status. */
    private final LmrsStatus lmrsStatus = new LmrsStatus();

    /** Set of mandatory lane change incentives. */
    private final LinkedHashSet<MandatoryIncentive> mandatoryIncentives = new LinkedHashSet<>();

    /** Set of voluntary lane change incentives. */
    private final LinkedHashSet<VoluntaryIncentive> voluntaryIncentives = new LinkedHashSet<>();

    /**
     * Constructor setting the car-following model.
     * @param carFollowingModel Car-following model.
     * @param gtu GTU
     */
    public LMRS(final CarFollowingModel carFollowingModel, final LaneBasedGTU gtu)
    {
        super(carFollowingModel, gtu);
    }

    /**
     * Adds a mandatory incentive. Ignores <tt>null</tt>.
     * @param incentive Incentive to add.
     */
    public final void addMandatoryIncentive(final MandatoryIncentive incentive)
    {
        if (incentive != null)
        {
            this.mandatoryIncentives.add(incentive);
        }
    }

    /**
     * Adds a voluntary incentive. Ignores <tt>null</tt>.
     * @param incentive Incentive to add.
     */
    public final void addVoluntaryIncentive(final VoluntaryIncentive incentive)
    {
        if (incentive != null)
        {
            this.voluntaryIncentives.add(incentive);
        }
    }

    /**
     * Sets the default lane change incentives.
     */
    public final void setDefaultIncentives()
    {
        this.mandatoryIncentives.clear();
        this.voluntaryIncentives.clear();
        this.mandatoryIncentives.add(new IncentiveRoute());
        this.voluntaryIncentives.add(new IncentiveSpeedWithCourtesy());
        this.voluntaryIncentives.add(new IncentiveKeep());
    }

    /** {@inheritDoc} */
    @Override
    public final OperationalPlan generateOperationalPlan(final Time startTime, final DirectedPoint locationAtStartTime)
        throws OperationalPlanException, GTUException, NetworkException, ParameterException
    {
        // obtain objects to get info
        getPerception().perceive();
        SpeedLimitProspect slp =
            getPerception().getPerceptionCategory(InfrastructurePerception.class).getSpeedLimitProspect(RelativeLane.CURRENT);
        SpeedLimitInfo sli = slp.getSpeedLimitInfo(Length.ZERO);
        BehavioralCharacteristics bc = getGtu().getBehavioralCharacteristics();

        // LMRS
        SimpleOperationalPlan simplePlan =
            LmrsUtil.determinePlan(getGtu(), startTime, this.lmrsStatus, getCarFollowingModel(), this.laneChange,
                getPerception(), this.mandatoryIncentives, this.voluntaryIncentives);

        // speed limits
        Speed speed = getGtu().getSpeed();
        simplePlan.minimumAcceleration(SpeedLimitUtil.considerSpeedLimitTransitions(bc, speed, slp, getCarFollowingModel()));

        // traffic lights
        // TODO traffic lights on route, possible on different lane (and possibly close)
        simplePlan.minimumAcceleration(TrafficLightUtil.respondToTrafficLights(bc, getPerception().getPerceptionCategory(
            IntersectionPerception.class).getTrafficLights(RelativeLane.CURRENT), getCarFollowingModel(), speed, sli));

        // conflicts
        simplePlan.minimumAcceleration(ConflictUtil.approachConflicts(bc, getPerception().getPerceptionCategory(
            IntersectionPerception.class).getConflicts(RelativeLane.CURRENT), getPerception().getPerceptionCategory(
            NeighborsPerception.class).getLeaders(RelativeLane.CURRENT), getCarFollowingModel(), getGtu().getLength(), speed,
            sli, this.yieldPlans));

        // create plan
        return buildPlanFromSimplePlan(getGtu(), startTime, bc, simplePlan, this.laneChange);

    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        String mandatory;
        mandatory = "mandatoryIncentives=" + this.mandatoryIncentives + ", ";
        String voluntary;
        if (!this.voluntaryIncentives.isEmpty())
        {
            voluntary = "voluntaryIncentives=" + this.voluntaryIncentives;
        }
        else
        {
            voluntary = "voluntaryIncentives=[]";
        }
        return "LMRS [" + mandatory + voluntary + "]";
    }

}
