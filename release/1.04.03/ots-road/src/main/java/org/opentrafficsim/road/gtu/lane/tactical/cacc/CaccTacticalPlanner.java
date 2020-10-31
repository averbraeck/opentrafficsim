package org.opentrafficsim.road.gtu.lane.tactical.cacc;

import java.util.SortedSet;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeAcceleration;
import org.opentrafficsim.base.parameters.ParameterTypeDuration;
import org.opentrafficsim.base.parameters.ParameterTypeLength;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.TurnIndicatorStatus;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.InfrastructureLaneChangeInfo;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTU;
import org.opentrafficsim.road.gtu.lane.plan.operational.LaneChange;
import org.opentrafficsim.road.gtu.lane.plan.operational.LaneOperationalPlanBuilder;
import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.lane.tactical.AbstractLaneBasedTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.util.CarFollowingUtil;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Desire;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;
import org.opentrafficsim.road.network.speed.SpeedLimitProspect;

import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 27 sep. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
@SuppressWarnings("serial")
public class CaccTacticalPlanner extends AbstractLaneBasedTacticalPlanner
{

    /** Lane change info. */
    private final LaneChange laneChange;

    /** Longitudinal controller. */
    private final CaccController controller;

    /** Platoon. */
    private Platoon platoon;

    /** Look ahead parameter type. */
    protected static final ParameterTypeLength LOOKAHEAD = ParameterTypes.LOOKAHEAD;

    /** ... */
    public static final ParameterTypeDuration T_GAP = CaccParameters.T_GAP;

    /** Look-ahead time for mandatory lane changes parameter type. */
    public static final ParameterTypeDuration T0 = ParameterTypes.T0;

    /** Fixed model time step. */
    public static final ParameterTypeDuration DT = ParameterTypes.DT;

    /** Synchronization by platoon leader. */
    public static final ParameterTypeAcceleration A_REDUCED = CaccParameters.A_REDUCED;

    /**
     * @param carFollowingModel
     * @param gtu
     * @param lanePerception
     * @param controller
     */
    public CaccTacticalPlanner(final CarFollowingModel carFollowingModel, final LaneBasedGTU gtu,
            final LanePerception lanePerception, final CaccController controller)
    {
        super(carFollowingModel, gtu, lanePerception);
        this.controller = controller;
        this.laneChange = new LaneChange(gtu);
    }

    /**
     * Sets the platoon.
     * @param platoon Platoon; platoon
     */
    public void setPlatoon(final Platoon platoon)
    {
        this.platoon = platoon;
        this.controller.setPlatoon(platoon);
    }

    /**
     * Generate operational plan.
     */
    @Override
    public OperationalPlan generateOperationalPlan(final Time startTime, final DirectedPoint locationAtStartTime)
            throws OperationalPlanException, GTUException, NetworkException, ParameterException
    {

        // Perception objects
        LanePerception perception = getPerception();
        getGtu().getTacticalPlanner().getPerception().perceive(); // update perception
        ControllerPerceptionCategory sensors = getPerception().getPerceptionCategory(ControllerPerceptionCategory.class);
        InfrastructurePerception infra = getPerception().getPerceptionCategory(InfrastructurePerception.class);

        // Current GTU
        LaneBasedGTU gtu = getGtu();
        Speed speed = gtu.getSpeed(); // Actual speed, not perceived
        Parameters parameters = gtu.getParameters();

        // Speed limit
        SpeedLimitProspect slp = getPerception().getPerceptionCategory(InfrastructurePerception.class)
                .getSpeedLimitProspect(RelativeLane.CURRENT);
        SpeedLimitInfo sli = slp.getSpeedLimitInfo(Length.ZERO);

        // Platoon list
        String gtuId = gtu.getId();

        // Initialize operational plan
        SimpleOperationalPlan plan = new SimpleOperationalPlan(Acceleration.ZERO, Duration.ZERO);

        // Determine desire to change lanes (current, left, right)
        SortedSet<InfrastructureLaneChangeInfo> currentInfo = infra.getInfrastructureLaneChangeInfo(RelativeLane.CURRENT);
        Length currentFirst = currentInfo.isEmpty() || currentInfo.first().getRequiredNumberOfLaneChanges() == 0
                ? Length.POSITIVE_INFINITY : currentInfo.first().getRemainingDistance();

        double dCurr = 0.0;
        double dLeft = 0.0;
        double dRigh = 0.0;

        // Desire for current lane
        if (infra.getCrossSection().contains(RelativeLane.CURRENT))
        {
            dCurr = determineDesire(infra, parameters, speed, RelativeLane.CURRENT);
        }

        // Desire for left lane
        if (perception.getLaneStructure().getExtendedCrossSection().contains(RelativeLane.LEFT)
                && infra.getLegalLaneChangePossibility(RelativeLane.CURRENT, LateralDirectionality.LEFT).neg().lt(currentFirst))
        {
            // Desire to leave left lane
            dLeft = determineDesire(infra, parameters, speed, RelativeLane.LEFT);
            // desire to leave from current to left lane
            dLeft = dLeft < dCurr ? dCurr : dLeft > dCurr ? -dLeft : 0;
        }

        // Desire for right lane
        if (perception.getLaneStructure().getExtendedCrossSection().contains(RelativeLane.RIGHT) && infra
                .getLegalLaneChangePossibility(RelativeLane.CURRENT, LateralDirectionality.RIGHT).neg().lt(currentFirst))
        {
            // Desire to leave right lane
            dRigh = determineDesire(infra, parameters, speed, RelativeLane.RIGHT);
            // desire to leave from current to right lane
            dRigh = dRigh < dCurr ? dCurr : dRigh > dCurr ? -dRigh : 0;
        }
        // Offset to ensure keep right policy
        dRigh = dRigh + 0.01;
        Desire desire = new Desire(dLeft, dRigh);

        // Determine and set direction of lane change, based on desire and if not already changing lanes
        LateralDirectionality laneChangeDirection;
        laneChangeDirection = LateralDirectionality.NONE;
        double thresholdLeft = 0.1; // Threshold for changing to the left
        double thresholdRigh = 0.0; // Threshold for changing to the right

        // By default; direction is straight
        LateralDirectionality direction = LateralDirectionality.NONE;
        TurnIndicatorStatus turndirection;

        if (this.platoon != null)
        {
            if (desire.leftIsLargerOrEqual() && desire.getLeft() > thresholdLeft
                    && infra.getLegalLaneChangePossibility(RelativeLane.CURRENT, LateralDirectionality.LEFT).gt0())
            {
                // Direction is desired and possible (set blinker accordingly)
                turndirection = TurnIndicatorStatus.LEFT;

                if (this.platoon.canInitiateLaneChangeProcess())
                {
                    laneChangeDirection = LateralDirectionality.LEFT;
                    this.platoon.initiateLaneChange(laneChangeDirection);
                }
            }
            else if (!desire.leftIsLargerOrEqual() && desire.getRight() > thresholdRigh
                    && infra.getLegalLaneChangePossibility(RelativeLane.CURRENT, LateralDirectionality.RIGHT).gt0())
            {
                // Direction is desired and possible (set blinker accordingly)
                turndirection = TurnIndicatorStatus.RIGHT;

                if (this.platoon.canInitiateLaneChangeProcess())
                {
                    laneChangeDirection = LateralDirectionality.RIGHT;
                    this.platoon.initiateLaneChange(laneChangeDirection);
                }
            }
            else
            {
                turndirection = TurnIndicatorStatus.NONE;
            }

            // Direction of initiated lane change, returns NONE if truck should not change lanes (yet)
            direction = this.platoon.shouldChangeLane(gtuId);

        }
        else
        {
            turndirection = TurnIndicatorStatus.NONE;
        }

        // Leader and follower (adjacent lane) based on desired lane change direction
        HeadwayGTU adjacentLeader = sensors.getLeader(direction);
        HeadwayGTU adjacentFollower = sensors.getFollower(direction);

        // By default; merging is not allowed in the desired direction
        LateralDirectionality allowedDirection = LateralDirectionality.NONE;

        // Car-following acceleration in case of lane-change
        CarFollowingModel cfEgo = this.getCarFollowingModel();
        Acceleration b0 = parameters.getParameter(ParameterTypes.B0);

        // Check if the desire to change lanes is there and if it is legally possible to change lanes
        if (!direction.isNone() && infra.getLegalLaneChangePossibility(RelativeLane.CURRENT, direction).gt0())
        {

            gtu.setTurnIndicatorStatus(turndirection);

            // Only if a lane change has occurred already -> do the following
            if (this.platoon.laneChangeInProgress())
            {
                if (adjacentLeader == null || adjacentFollower == null
                        || (adjacentLeader.getOverlap() == null && adjacentFollower.getOverlap() == null
                                && CarFollowingUtil.followSingleLeader(cfEgo, parameters, gtu.getSpeed(), sli,
                                        adjacentLeader.getDistance(), adjacentLeader.getSpeed()).ge(b0.neg())
                                || (this.platoon.isInPlatoon(adjacentLeader.getId()) && adjacentLeader.getOverlap() == null
                                        && adjacentFollower.getOverlap() == null)))
                {
                    if (this.platoon.getIndex(gtuId) == (this.platoon.numberOfChanged() - 1))
                    {
                        // Current gtu is the leader of the platoon
                        if (this.platoon.isInPlatoon(adjacentFollower.getId()))
                        {
                            // Space till rear of platoon is free
                            allowedDirection = direction;
                            this.platoon.addLaneChange(getGtu());
                            // this.laneChange.setDesiredLaneChangeDuration(getGtu().getParameters().getParameter(ParameterTypes.LCDUR));
                            gtu.setTurnIndicatorStatus(TurnIndicatorStatus.NONE);

                        }
                    }
                }
            }
            else if (adjacentLeader == null || adjacentFollower == null
                    || (adjacentLeader.getOverlap() == null && adjacentFollower.getOverlap() == null
                            && CarFollowingUtil.followSingleLeader(cfEgo, parameters, gtu.getSpeed(), sli,
                                    adjacentLeader.getDistance(), adjacentLeader.getSpeed()).ge(b0.neg())))
            {
                // No overlap with leader AND follower (can be the same gtu)
                if (adjacentFollower == null
                        || CarFollowingUtil
                                .followSingleLeader(adjacentFollower.getCarFollowingModel(), parameters,
                                        adjacentFollower.getSpeed(), sli, adjacentFollower.getDistance(), gtu.getSpeed())
                                .ge(b0.neg()))
                {
                    // Gap is acceptable based on acceleration adjustment; merging is allowed in desired direction
                    allowedDirection = direction;
                    this.platoon.addLaneChange(getGtu());
                    gtu.setTurnIndicatorStatus(TurnIndicatorStatus.NONE);
                }
            }
        }
        // Break.on(gtu, "10", 0.0, true);
        // Decrease acceleration to create gap (only leading vehicle in platoon)
        // Also checks whether there are platoon vehicles behind (otherwise do not decrease speed)
        // && this.platoon.laneChangeInProgress() -> changed to when direction is on
        // String leaderID = (platoon.getId(platoon.size()-1));

        if (this.platoon != null && this.platoon.getIndex(gtuId) == 0 && !this.platoon.canInitiateLaneChangeProcess()
        // && turndirection != TurnIndicatorStatus.NONE
        // && this.platoon.laneChangeInProgress()
        // && this.platoon.isInPlatoon(sensors.getFollower(LateralDirectionality.NONE).getId())
                && allowedDirection.isNone() && !direction.isNone()
                && infra.getLegalLaneChangePossibility(RelativeLane.CURRENT, direction).gt0())
        {
            // We are the leading vehicle, we want to change to a direction but are not allowed (no space)
            // Now only if the last vehicle has changed already
            Acceleration atemp = this.controller.calculateAcceleration(gtu);
            Acceleration reduction = parameters.getParameter(A_REDUCED);
            Acceleration amax = parameters.getParameter(CaccParameters.A_MAX);
            Acceleration amin = parameters.getParameter(CaccParameters.A_MIN);
            Acceleration areduced = atemp.minus(reduction);
            // Limit deceleration by upper and lower limits from parameters
            Acceleration aredLimit = Acceleration
                    .instantiateSI(areduced.si < amax.si ? (areduced.si > amin.si ? areduced.si : amin.si) : amax.si);
            plan = new SimpleOperationalPlan(aredLimit, parameters.getParameter(DT), allowedDirection);

        }
        else
        {
            plan = new SimpleOperationalPlan(this.controller.calculateAcceleration(gtu), parameters.getParameter(DT),
                    allowedDirection);
        }

        return LaneOperationalPlanBuilder.buildPlanFromSimplePlan(getGtu(), startTime, plan, this.laneChange);

    }

    /** ... 
     * @param infra InfrastructurePerception
     * @param parameters Parameters
     * @param speed Speed
     * @param laneDirection RelativeLane
     * @return Double determined desire
     * @throws ParameterException */
    private Double determineDesire(final InfrastructurePerception infra, final Parameters parameters, final Speed speed,
            final RelativeLane laneDirection) throws ParameterException
    {
        double dCurr;

        double dOut = 0.0; // reset dOut

        for (InfrastructureLaneChangeInfo info : infra.getInfrastructureLaneChangeInfo(laneDirection))
        {
            double d;

            // Desire to leave current lane based on remaining distance
            double d1 = 1 - info.getRemainingDistance().si
                    / (info.getRequiredNumberOfLaneChanges() * parameters.getParameter(LOOKAHEAD).si);
            double d2 = 1 - (info.getRemainingDistance().si / speed.si)
                    / (info.getRequiredNumberOfLaneChanges() * parameters.getParameter(T0).si);
            d1 = d2 > d1 ? d2 : d1;

            d = d1 < 0 ? 0 : d1;

            dOut = d > dOut ? d : dOut;
        }

        dCurr = dOut;

        return dCurr;
    }

}
