package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.VehicleTypes;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.exceptions.Try;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterType;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.perception.EgoPerception;
import org.opentrafficsim.core.gtu.perception.PerceptionCategory;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.DirectDefaultSimplePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.TrafficPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtu;
import org.opentrafficsim.road.gtu.lane.plan.operational.LaneChange;
import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.VotingArbiter.*;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ActionAdvice;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ActionState;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.DrivingTask;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.ManeuverPattern;
import org.opentrafficsim.road.gtu.lane.tactical.util.CarFollowingUtil;
import org.opentrafficsim.road.network.LaneChangeInfo;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;
import org.opentrafficsim.road.network.speed.SpeedLimitProspect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

public abstract class AbstractMirovaVehicle
{
    protected final VotingArbiter votingArbiter;

    protected final MirovaTacticalPlanner tacticalPlanner;

    protected final ArrayList<DrivingTask> listDrivingTasks;

    protected List<DrivingTask> listActiveDrivingTasks;

    protected boolean runningManeuver = false;

    protected ActionState currentActionState = null;

    protected final CarFollowingModel carFollowingModel;

    protected final LaneBasedGtu gtu;

    protected final LanePerception lanePerception;

    protected SimpleOperationalPlan operationalPlan;

    protected final LaneChange laneChange;

    protected Double desire = 0.0;

    protected Duration desireRelaxationTime = new Duration(0.0, DurationUnit.SI);

    // Konstruktor
    public AbstractMirovaVehicle(final CarFollowingModel carFollowingModel, final LaneBasedGtu gtu,
            final LanePerception lanePerception, final MirovaTacticalPlanner tacticalPlanner)
    {
        this.votingArbiter = new VotingArbiter();
        this.tacticalPlanner = tacticalPlanner;
        this.listDrivingTasks = new ArrayList<>();
        initializeDrivingTasks();
        this.listActiveDrivingTasks = new ArrayList<>();
        this.carFollowingModel = carFollowingModel;
        this.gtu = gtu;
        this.lanePerception = lanePerception;
        this.laneChange = Try.assign(() -> new LaneChange(gtu), "Parameter LCDUR is required.", GtuException.class);

    }

    public SimpleOperationalPlan update()
            throws OperationalPlanException, ParameterException, NullPointerException, IllegalArgumentException
    {
        updateDesire();
        this.operationalPlan = null;

        if (this.runningManeuver && this.currentActionState != null)
        {
            this.operationalPlan = this.currentActionState.update();
        }
        else
        {
            updateActiveDrivingTasks();
            List<ManeuverPattern> listManeuverPatterns = new ArrayList<>();

            for (DrivingTask task : this.listActiveDrivingTasks)
            {
                ManeuverPattern maneuverPattern = task.decideManeuverPattern();
                if (maneuverPattern != null)
                {
                    listManeuverPatterns.add(maneuverPattern);
                }
            }

            if (!listManeuverPatterns.isEmpty())
            {
                this.currentActionState = this.votingArbiter.execute(listManeuverPatterns);
                if (this.currentActionState != null)
                {
                    this.operationalPlan = this.currentActionState.update();
                }
            }

        }

        Acceleration minAcceleration = getMinAcceleration();

        if (this.operationalPlan != null)
        {
            if (this.operationalPlan.getAcceleration().le(minAcceleration))
            {
                return this.operationalPlan;
            }
            else
            {
                this.operationalPlan.setAcceleration(minAcceleration);
                return this.operationalPlan;
            }

        }

        else
        {
            return new SimpleOperationalPlan(minAcceleration, this.getGtu().getParameters().getParameter(ParameterTypes.DT));
        }

    }

    public void updateActiveDrivingTasks()
    {
        this.listActiveDrivingTasks.clear();
        for (DrivingTask task : this.listDrivingTasks)
        {
            if (task.getActivation())
            {
                this.listActiveDrivingTasks.add(task);
            }
        }
    }

    protected abstract void initializeDrivingTasks();

    /**
     * Returns the deceleration of the lane change follower in the specified direction. This method iterates through all
     * followers in the specified direction and calculates the minimum deceleration required to follow them.
     * @param laneChangeDirection The direction of the lane change (LEFT or RIGHT).
     * @return The deceleration required by the lane change follower.
     * @throws ParameterException
     * @throws IllegalArgumentException
     * @throws NullPointerException
     * @throws OperationalPlanException
     */
    public Acceleration getLaneChangeFollowerDeceleration(final LateralDirectionality laneChangeDirection)
            throws ParameterException, OperationalPlanException, NullPointerException, IllegalArgumentException
    {
        Acceleration followerDeceleration = new Acceleration(Double.POSITIVE_INFINITY, AccelerationUnit.SI);
        for (HeadwayGtu follower : getLanePerception().getPerceptionCategory(NeighborsPerception.class)
                .getFirstFollowers(laneChangeDirection))
        {
            setDesiredHeadway(follower.getParameters());
            Acceleration iteraryDeceleration =
                    CarFollowingUtil.followSingleLeader(follower.getCarFollowingModel(), follower.getParameters(),
                            follower.getSpeed(), follower.getSpeedLimitInfo(), follower.getDistance(), getGtu().getSpeed());
            followerDeceleration = Acceleration.min(followerDeceleration, iteraryDeceleration);
            resetDesiredHeadway(follower.getParameters());
        }
        return followerDeceleration;
    }

    /**
     * Returns the deceleration of the ego vehicle in the specified lane change direction. This method iterates through all
     * leaders in the specified direction and calculates the minimum deceleration required to follow them.
     * @param laneChangeDirection The direction of the lane change (LEFT or RIGHT).
     * @return The deceleration required by the ego vehicle during a lane change.
     * @throws ParameterException
     * @throws IllegalArgumentException
     * @throws NullPointerException
     * @throws OperationalPlanException
     */
    public Acceleration getLaneChangeEgoDeceleration(final LateralDirectionality laneChangeDirection)
            throws ParameterException, OperationalPlanException, NullPointerException, IllegalArgumentException
    {
        Acceleration egoDeceleration = new Acceleration(Double.POSITIVE_INFINITY, AccelerationUnit.SI);
        for (HeadwayGtu leader : getLanePerception().getPerceptionCategory(NeighborsPerception.class)
                .getFirstLeaders(laneChangeDirection))
        {
            setDesiredHeadway();
            Acceleration iteraryDeceleration =
                    CarFollowingUtil.followSingleLeader(getCarFollowingModel(), getGtu().getParameters(), getGtu().getSpeed(),
                            getLanePerception().getPerceptionCategory(InfrastructurePerception.class)
                                    .getSpeedLimitProspect(RelativeLane.CURRENT).getSpeedLimitInfo(Length.ZERO),
                            leader.getDistance(), leader.getSpeed());
            egoDeceleration = Acceleration.min(egoDeceleration, iteraryDeceleration);
            resetDesiredHeadway();
        }
        return egoDeceleration;
    }

    /**
     * Returns the free driving time in the specified lane change direction. This method iterates through all leaders in the
     * specified direction and calculates the minimum free driving time based on their speed and distance.
     * @param laneChangeDirection The direction of the lane change (LEFT or RIGHT).
     * @return The free driving time available for a lane change.
     * @throws ParameterException
     * @throws OperationalPlanException
     */
    public Duration getFreeDrivingTime(final LateralDirectionality laneChangeDirection)
            throws ParameterException, OperationalPlanException
    {
        Duration freeDrivingTime = new Duration(Double.POSITIVE_INFINITY, DurationUnit.SI);
        for (HeadwayGtu leader : getLanePerception().getPerceptionCategory(NeighborsPerception.class)
                .getFirstLeaders(LateralDirectionality.RIGHT))
        {
            Speed speedDeltaLeader = getGtu().getDesiredSpeed().minus(leader.getSpeed());
            Length distanceLeader = leader.getDistance();

            if (speedDeltaLeader.gt0())
            {
                Duration freeDrivingTimeIterary = new Duration(distanceLeader.si / speedDeltaLeader.si, DurationUnit.SI);
                freeDrivingTime = Duration.min(freeDrivingTime, freeDrivingTimeIterary);
            }

        }
        return freeDrivingTime;

    }

    // Getter und Setter für runningManeuver
    public boolean isRunningManeuver()
    {
        return this.runningManeuver;
    }

    public void setRunningManeuver(final boolean runningManeuver)
    {
        this.runningManeuver = runningManeuver;
    }

    // Getter und Setter für currentActionState
    public ActionState getCurrentActionState()
    {
        return this.currentActionState;
    }

    public void setCurrentActionState(final ActionState currentActionState)
    {
        this.currentActionState = currentActionState;
    }

    public CarFollowingModel getCarFollowingModel()
    {
        return this.carFollowingModel;
    }

    public LaneBasedGtu getGtu()
    {
        return this.gtu;
    }

    public LanePerception getLanePerception()
    {
        return this.lanePerception;
    }

    public LaneChange getLaneChange()
    {
        return this.laneChange;
    }

    /**
     * Sets the time headway parameter T based on the current lane change desire.
     * <p>
     * The value of T is interpolated between TMAX (no desire, {@code desire} = 0) and TMIN (full desire, {@code desire} = 1).
     * The desire value is clamped to the range [0, 1].
     * </p>
     * @param desire lane change desire, where 0 means no desire (T = TMAX) and 1 means full desire (T = TMIN)
     * @throws ParameterException if T, TMIN, or TMAX is not present in the parameters
     */
    public void setDesiredHeadway(final Parameters params) throws ParameterException
    {
        double limitedDesire = this.desire < 0 ? 0 : this.desire > 1 ? 1 : this.desire;
        double tDes = limitedDesire * params.getParameter(ParameterTypes.TMIN).si
                + (1 - limitedDesire) * params.getParameter(ParameterTypes.TMAX).si;
        double t = params.getParameter(ParameterTypes.T).si;
        params.setParameterResettable(ParameterTypes.T, Duration.instantiateSI(tDes < t ? tDes : t));
    }

    public void setDesiredHeadway() throws ParameterException
    {
        Double desire = this.getDesire() == null ? 0.0 : this.getDesire();
        setDesiredHeadway(this.getGtu().getParameters());
    }

    /**
     * Resets the time headway parameter T to its default value.
     * @throws ParameterException if T is not present in the parameters
     */
    public void resetDesiredHeadway(final Parameters params) throws ParameterException
    {
        params.resetParameter(ParameterTypes.T);
    }

    public void resetDesiredHeadway() throws ParameterException
    {
        Double desire = this.getDesire() == null ? 0.0 : this.getDesire();
        resetDesiredHeadway(this.getGtu().getParameters());
    }

    /**
     * Calculates the acceleration using the car-following model, adjusting the time headway T according to the specified lane
     * change desire. The value of T is interpolated between TMAX (no desire, {@code desire} = 0) and TMIN (full desire,
     * {@code desire} = 1).
     * <p>
     * Temporarily sets T based on the desire, computes the acceleration, and then resets T to its original value.
     * </p>
     * @param leader the headway to the leader vehicle (returns free acceleration if leaders are empty)
     * @param desire lane change desire, where 0 means no desire and 1 means full desire
     * @return the calculated acceleration based on the adjusted headway
     * @throws ParameterException if a required parameter is not defined
     * @throws OperationalPlanException if an error occurs during acceleration calculation
     */
    public Acceleration desireBasedFollowingAcceleration(final Headway leader)
            throws ParameterException, OperationalPlanException
    {
        Double desire = this.getDesire() == null ? 0.0 : this.getDesire();
        Parameters params = this.getGtu().getParameters();
        Speed egoSpeed = getLanePerception().getPerceptionCategory(EgoPerception.class).getSpeed();
        SpeedLimitInfo speedLimitInfo = this.getLanePerception().getPerceptionCategory(InfrastructurePerception.class)
                .getSpeedLimitProspect(RelativeLane.CURRENT).getSpeedLimitInfo(new Length(1.0, LengthUnit.SI));
        // set T
        setDesiredHeadway();
        // calculate acceleration
        Acceleration a = CarFollowingUtil.followSingleLeader(getCarFollowingModel(), params, egoSpeed, speedLimitInfo,
                leader.getDistance(), leader.getSpeed());
        // reset T
        resetDesiredHeadway();
        return a;
    }

    public Acceleration desireBasedFollowingAcceleration() throws ParameterException, OperationalPlanException
    {
        Headway leader = getLanePerception().getPerceptionCategory(DirectDefaultSimplePerception.class).getForwardHeadwayGtu();
        return desireBasedFollowingAcceleration(leader);
    }

    public Acceleration freeAcceleration() throws OperationalPlanException, ParameterException
    {
        Parameters params = this.getGtu().getParameters();
        Speed egoSpeed = getLanePerception().getPerceptionCategory(EgoPerception.class).getSpeed();
        SpeedLimitInfo speedLimitInfo = this.getLanePerception().getPerceptionCategory(InfrastructurePerception.class)
                .getSpeedLimitProspect(RelativeLane.CURRENT).getSpeedLimitInfo(new Length(1.0, LengthUnit.SI));
        Acceleration freeAcceleration =
                CarFollowingUtil.freeAcceleration(getCarFollowingModel(), params, egoSpeed, speedLimitInfo);
        return freeAcceleration;
    }

    public Acceleration deadEndAcceleration() throws ParameterException, OperationalPlanException
    {
        SortedSet<LaneChangeInfo> currentLaneLCInfo = getLanePerception().getPerceptionCategory(InfrastructurePerception.class)
                .getLegalLaneChangeInfo(RelativeLane.CURRENT);
        Length currentLaneLCRemainingDistance =
                currentLaneLCInfo.isEmpty() || currentLaneLCInfo.first().numberOfLaneChanges() == 0 ? Length.POSITIVE_INFINITY
                        : currentLaneLCInfo.first().remainingDistance();
        Parameters params = this.getGtu().getParameters();
        Speed egoSpeed = getLanePerception().getPerceptionCategory(EgoPerception.class).getSpeed();
        SpeedLimitInfo speedLimitInfo = this.getLanePerception().getPerceptionCategory(InfrastructurePerception.class)
                .getSpeedLimitProspect(RelativeLane.CURRENT).getSpeedLimitInfo(new Length(1.0, LengthUnit.SI));

        Acceleration a =
                CarFollowingUtil.stop(this.carFollowingModel, params, egoSpeed, speedLimitInfo, currentLaneLCRemainingDistance);

        return a;

    }

    public Acceleration getMinAcceleration() throws OperationalPlanException, ParameterException {
        Acceleration minAcceleration = Acceleration.min(freeAcceleration(), desireBasedFollowingAcceleration(), deadEndAcceleration());
        return minAcceleration;
    }

    public Double getDesire()
    {
        return this.desire;
    }

    public void setDesire(final Double desire, final Duration desireRelaxationTime)
    {
        this.desire = desire;
        this.desireRelaxationTime = desireRelaxationTime;
    }

    public void updateDesire() throws ParameterException
    {
        if (this.desireRelaxationTime.si > 0.0)
        {
            Duration dt = this.getGtu().getParameters().getParameter(ParameterTypes.DT);

            this.desire -= this.desire * dt.si / this.desireRelaxationTime.si;
            if (this.desire < 0.0)
            {
                this.desire = 0.0;
            }

            this.desireRelaxationTime = Duration
                    .instantiateSI(this.desireRelaxationTime.si - dt.si < 0 ? 0.0 : this.desireRelaxationTime.si - dt.si);
        }
    }
}
