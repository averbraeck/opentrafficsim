package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.VehicleTypes;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.DurationUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.exceptions.Try;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.perception.EgoPerception;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.TrafficPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsPerception;
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
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;
import org.opentrafficsim.road.network.speed.SpeedLimitProspect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    {

        if (this.runningManeuver && this.currentActionState != null)
        {
            this.operationalPlan = this.currentActionState.update();
        }
        else
        {
            updateActiveDrivingTasks();
            List<ActionAdvice> listActionAdvices = new ArrayList<>();

            for (DrivingTask task : this.listActiveDrivingTasks)
            {
                ManeuverPattern maneuverPattern = task.decideManeuverPattern();
                if (maneuverPattern != null)
                {
                    listActionAdvices.add(maneuverPattern);
                }
            }

            if (!listActionAdvices.isEmpty())
            {
                this.currentActionState = this.votingArbiter.execute(listActionAdvices);
                if (this.currentActionState != null)
                {
                    this.operationalPlan = this.currentActionState.update();
                }
            }
            else
            {
                this.operationalPlan =
                        new SimpleOperationalPlan(this.gtu.getCarFollowingAcceleration(), this.tacticalPlanner.getDT());
            }
        }

        if (this.operationalPlan.getAcceleration().si <= this.gtu.getCarFollowingAcceleration().si)
        {
            return this.operationalPlan;
        }
        // If the maneuver acceleration is greater than the car following acceleration, we use the car following acceleration
        else
        {
            return new SimpleOperationalPlan(this.gtu.getCarFollowingAcceleration(), this.tacticalPlanner.getDT());
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
            Acceleration iteraryDeceleration =
                    CarFollowingUtil.followSingleLeader(follower.getCarFollowingModel(), follower.getParameters(),
                            follower.getSpeed(), follower.getSpeedLimitInfo(), follower.getDistance(), getGtu().getSpeed());
            followerDeceleration = Acceleration.min(followerDeceleration, iteraryDeceleration);
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
            Acceleration iteraryDeceleration =
                    CarFollowingUtil.followSingleLeader(getCarFollowingModel(), getGtu().getParameters(), getGtu().getSpeed(),
                            getLanePerception().getPerceptionCategory(InfrastructurePerception.class)
                                    .getSpeedLimitProspect(RelativeLane.CURRENT).getSpeedLimitInfo(Length.ZERO),
                            leader.getDistance(), leader.getSpeed());
            egoDeceleration = Acceleration.min(egoDeceleration, iteraryDeceleration);
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
}