package org.opentrafficsim.cosim.tactical;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.draw.point.DirectedPoint2d;
import org.djutils.draw.point.Point2d;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.DistancedObject;
import org.opentrafficsim.base.geometry.FractionalProjectionHelper.FractionalFallback;
import org.opentrafficsim.base.geometry.OtsLine2d;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterType;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.TurnIndicatorStatus;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.gtu.plan.operational.Segments;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.LaneBasedGtu;
import org.opentrafficsim.road.gtu.operational.LaneOperationalPlanBuilder;
import org.opentrafficsim.road.gtu.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.perception.LanePerception;
import org.opentrafficsim.road.gtu.perception.RelativeLane;
import org.opentrafficsim.road.gtu.tactical.Blockable;
import org.opentrafficsim.road.gtu.tactical.Synchronizable;
import org.opentrafficsim.road.gtu.tactical.TacticalContextEgo;
import org.opentrafficsim.road.gtu.tactical.following.AbstractCarFollowingModel;
import org.opentrafficsim.road.gtu.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.tactical.following.DesiredSpeedModel;
import org.opentrafficsim.road.gtu.tactical.lmrs.AbstractIncentivesTacticalPlanner;
import org.opentrafficsim.road.gtu.tactical.lmrs.AccelerationConflicts;
import org.opentrafficsim.road.gtu.tactical.lmrs.AccelerationIncentive;
import org.opentrafficsim.road.gtu.tactical.lmrs.DesireBased;
import org.opentrafficsim.road.gtu.tactical.util.DeadEndUtil;
import org.opentrafficsim.road.gtu.tactical.util.LaneChangeNotAllowedUtil;
import org.opentrafficsim.road.gtu.tactical.util.lmrs.Cooperation;
import org.opentrafficsim.road.gtu.tactical.util.lmrs.GapAcceptance;
import org.opentrafficsim.road.gtu.tactical.util.lmrs.LmrsData;
import org.opentrafficsim.road.gtu.tactical.util.lmrs.LmrsParameters;
import org.opentrafficsim.road.gtu.tactical.util.lmrs.LmrsUtil;
import org.opentrafficsim.road.gtu.tactical.util.lmrs.Synchronization;
import org.opentrafficsim.road.gtu.tactical.util.lmrs.Tailgating;
import org.opentrafficsim.road.network.Lane;
import org.opentrafficsim.road.network.speed.SpeedLimits;

import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEvent;

/**
 * Tactical planner that uses the LMRS, but overrides actions based on {@code Commands} typically invoked by a
 * {@code CommandsHandler}. This class is similar to the {@code Lmrs} tactical planner.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class ScenarioTacticalPlanner extends AbstractIncentivesTacticalPlanner implements DesireBased, Synchronizable, Blockable
{

    /** Time between execution of model during dead reckoning. */
    private static final Duration DEAD_RECKONING_MODEL_STEP = Duration.ofSI(0.5);

    /** Duration to extrapolate dead reckoning. */
    private static final Duration DEAD_RECKONING_HORIZON = Duration.ofSI(2.0);

    /** Deviation object in case of no desired deviation. */
    private static final DistancedObject<Length> NO_DEVIATION = new DistancedObject<>(Length.ZERO, Length.ZERO);

    /** LMRS data. */
    private final LmrsData lmrsData;

    /** Overruled acceleration. */
    private Acceleration accelerationCommand;

    /** Overruled indicator. */
    private LateralDirectionality indicatorCommand;

    /** Overruled lane change ability. */
    private boolean laneChangesEnabledCommand = true;

    /** Lane change command, this overrules laneChangesEnabledCommand. */
    private LateralDirectionality laneChangeCommand;

    /** Operational plan to sent to external. */
    private OperationalPlan lastIntendedPlan = null;

    /** Applies dead-reckoning to follow an external source of vehicle movement. */
    private boolean deadReckoning;

    /**
     * Whether dead-reckoning is part of a hybrid mode where the external simulation determines movement based on the intention
     * of a model plan.
     */
    private boolean hybridDeadReckoning;

    /** Speed for dead-reckoning. */
    private Speed deadReckoningSpeed;

    /** Acceleration for dead-reckoning. */
    private Acceleration deadReckoningAcceleration;

    /** Time of last model execution to set model parameters for surrounding vehicle while dead reckoning. */
    private Duration lastDeadReckoningModelExecution;

    /** Desired speed model for when the model should be reset. */
    private DesiredSpeedModel desiredSpeedModel;

    /** Synchronization state. */
    private Synchronizable.State syncState = Synchronizable.State.NONE;

    /**
     * Constructor setting the car-following model.
     * @param carFollowingModel Car-following model.
     * @param gtu GTU
     * @param lanePerception perception
     * @param synchronization type of synchronization
     * @param cooperation type of cooperation
     * @param gapAcceptance gap-acceptance
     * @param tailgating tail gating
     */
    public ScenarioTacticalPlanner(final CarFollowingModel carFollowingModel, final LaneBasedGtu gtu,
            final LanePerception lanePerception, final Synchronization synchronization, final Cooperation cooperation,
            final GapAcceptance gapAcceptance, final Tailgating tailgating)
    {
        super(carFollowingModel, gtu, lanePerception);
        this.lmrsData = new LmrsData(synchronization, cooperation, gapAcceptance, tailgating);
    }

    @Override
    public final OperationalPlan generateOperationalPlan(final Duration startTime, final DirectedPoint2d locationAtStartTime)
            throws OperationalPlanException, GtuException, NetworkException, ParameterException
    {
        if (!this.deadReckoning || this.lastDeadReckoningModelExecution == null
                || startTime.minus(this.lastDeadReckoningModelExecution).si >= DEAD_RECKONING_MODEL_STEP.si)
        {
            this.lastDeadReckoningModelExecution = this.lastDeadReckoningModelExecution == null ? startTime
                    : this.lastDeadReckoningModelExecution.plus(DEAD_RECKONING_MODEL_STEP); // prevent drift due to delay

            // LMRS
            TacticalContextEgo context = new TacticalContextEgo(getGtu());
            SimpleOperationalPlan simplePlan = LmrsUtil.determinePlan(context, this.lmrsData, this);

            // Lower acceleration from additional sources, consider adjacent lane when changing lane or synchronizing
            RelativeLane[] lanes;
            Parameters params = getGtu().getParameters();
            double dLeft = params.getParameter(LmrsParameters.DLEFT);
            double dRight = params.getParameter(LmrsParameters.DRIGHT);
            double dSync = params.getParameter(LmrsParameters.DSYNC);
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
                // On the current lane, consider all incentives. On adjacent lanes only consider incentives beyond the distance
                // over which a lane change is not yet possible, i.e. the merge distance.
                Length mergeDistance = lane.isCurrent() ? Length.ZERO
                        : Synchronization.getMergeDistance(getPerception(), lane.getLateralDirectionality());
                for (AccelerationIncentive incentive : getAccelerationIncentives())
                {
                    incentive.accelerate(context, lane, mergeDistance);
                }
            }

            // apply overruling commands
            simplePlan = applyOverrulingCommands(context, simplePlan);

            // deal with dead-end situations and lane changes that are not allowed
            simplePlan = LaneChangeNotAllowedUtil.preventLaneChange(context, DeadEndUtil.dealWithDeadEnd(context, simplePlan));

            // set turn indicator
            context.getIntent(TurnIndicatorStatus.class).ifPresentOrElse((d) -> getGtu().setTurnIndicatorStatus(d.object()),
                    () -> getGtu().setTurnIndicatorStatus(TurnIndicatorStatus.NONE));

            // create plan
            if (!this.deadReckoning || this.hybridDeadReckoning)
            {
                OperationalPlan operationalPlan = LaneOperationalPlanBuilder.buildPlanFromSimplePlan(getGtu(), simplePlan,
                        getGtu().getParameters().getParameter(ParameterTypes.LCDUR),
                        context.getIntent(Length.class).orElse(NO_DEVIATION));
                this.lastIntendedPlan = operationalPlan;
                this.syncState = this.lmrsData.getSynchronizationState();
                if (this.hybridDeadReckoning)
                {
                    /*
                     * In hybrid mode we want to return this plan so it is sent by a listener to LANEBASED_MOVE_EVENT. But we do
                     * not actually want to move based on this. Therefore we schedule another interrupt at this time. As the
                     * value of lastDeadReckoningModelExecution is updated, this second move will only do dead-reckoning.
                     */
                    getGtu().getSimulator().scheduleEventNow(() -> interruptMove(locationAtStartTime));
                }
                return operationalPlan;
            }
        }

        // Dead reckoning, limit lane change desire
        Parameters params = getGtu().getParameters();
        double dLeft = params.getParameter(LmrsParameters.DLEFT);
        double dRight = params.getParameter(LmrsParameters.DRIGHT);
        double dCoop = params.getParameter(LmrsParameters.DCOOP);
        if (this.indicatorCommand != null && !this.indicatorCommand.isNone())
        {
            // Indicator, keep lane change desire above (or equal to) dCoop so others cooperate
            if (this.indicatorCommand.isLeft())
            {
                params.setParameter(LmrsParameters.DLEFT, Math.max(dLeft, dCoop));
            }
            else
            {
                params.setParameter(LmrsParameters.DRIGHT, Math.max(dRight, dCoop));
            }
            this.syncState = Synchronizable.State.INDICATING;
        }
        else
        {
            // No indicator, keep lane change desire below dCoop so others do not cooperate
            double dSync = params.getParameter(LmrsParameters.DSYNC);
            double dSyncCoopMid = .5 * (dSync + dCoop);
            params.setParameter(LmrsParameters.DLEFT, Math.min(dLeft, dSyncCoopMid));
            params.setParameter(LmrsParameters.DRIGHT, Math.min(dRight, dSyncCoopMid));
            this.syncState = Synchronizable.State.NONE;
            // Note: State.SYNCHRONIZING has no meaning with dead reckoning
        }

        // Create operational plan from current position
        changeLaneOnDeadReckoning(locationAtStartTime);
        boolean toStandStill = this.deadReckoningAcceleration.lt0()
                && this.deadReckoningSpeed.si / -this.deadReckoningAcceleration.si < DEAD_RECKONING_HORIZON.si;
        double t = toStandStill ? this.deadReckoningSpeed.si / -this.deadReckoningAcceleration.si : DEAD_RECKONING_HORIZON.si;
        double distance = Math.max(1.0, this.deadReckoningSpeed.si * t + .5 * this.deadReckoningAcceleration.si * t * t);
        double x = locationAtStartTime.x + Math.cos(locationAtStartTime.dirZ) * distance;
        double y = locationAtStartTime.y - Math.sin(locationAtStartTime.dirZ) * distance;
        OtsLine2d path = new OtsLine2d(locationAtStartTime, new Point2d(x, y));
        // Segments.off() takes care of standstill
        return new OperationalPlan(getGtu(), path, startTime,
                Segments.off(this.deadReckoningSpeed, DEAD_RECKONING_HORIZON, this.deadReckoningAcceleration));
    }

    /**
     * Apply commands to overrule acceleration, lane change and indicators. Note that indicator intent is overruled within the
     * context. Acceleration and changing lane are overruled within the returned simple plan.
     * @param context tactical context
     * @param simplePlan simple plan from model
     * @return overruled plan
     */
    private SimpleOperationalPlan applyOverrulingCommands(final TacticalContextEgo context,
            final SimpleOperationalPlan simplePlan)
    {
        SimpleOperationalPlan out = simplePlan;
        if (this.accelerationCommand != null)
        {
            out.setAcceleration(this.accelerationCommand);
        }
        if (this.indicatorCommand != null && !this.indicatorCommand.isNone())
        {
            if (this.indicatorCommand.isLeft())
            {
                context.addIntent(TurnIndicatorStatus.LEFT, Length.ZERO);
            }
            else
            {
                context.addIntent(TurnIndicatorStatus.RIGHT, Length.ZERO);
            }
        }
        if (!this.laneChangesEnabledCommand)
        {
            context.addIntent(TurnIndicatorStatus.NONE, Length.ZERO);
            if (out.isLaneChange())
            {
                out = new SimpleOperationalPlan(out.getAcceleration(), out.getDuration());
            }
        }
        if (this.laneChangeCommand != null) // this overrules 'this.laneChangesEnabled == false'
        {
            out = new SimpleOperationalPlan(out.getAcceleration(), out.getDuration(), this.laneChangeCommand);
            this.laneChangeCommand = null; // trigger, not a state
        }
        return out;
    }

    /**
     * Change lane when needed as the new location is closer to an adjacent lane.
     * @param location location
     * @throws GtuException exception
     */
    private void changeLaneOnDeadReckoning(final DirectedPoint2d location) throws GtuException
    {
        Lane refLane = getGtu().getPositionOrRoaming().lane();
        double minDistance = distance(refLane, location);
        LateralDirectionality lc = null;
        for (LateralDirectionality lat : new LateralDirectionality[] {LateralDirectionality.LEFT, LateralDirectionality.RIGHT})
        {
            for (Lane adjLane : refLane.accessibleAdjacentLanesPhysical(lat, getGtu().getType()))
            {
                double distance = distance(adjLane, location);
                if (distance < minDistance)
                {
                    minDistance = distance;
                    lc = lat;
                }
            }
        }
        if (lc != null)
        {
            getGtu().changeLaneInstantaneously(lc);
            // set referenceLaneIndex to null to finalize any ongoing lane change
            try
            {
                Field referenceLaneIndexField = LaneBasedGtu.class.getDeclaredField("referenceLaneIndex");
                referenceLaneIndexField.setAccessible(true);
                referenceLaneIndexField.set(getGtu(), 0);
            }
            catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e)
            {
                // ignore
            }
        }
    }

    /**
     * Returns distance from the location to the lane.
     * @param lane lane
     * @param location location
     * @return distance from the location to the lane
     */
    private double distance(final Lane lane, final DirectedPoint2d location)
    {
        double frac = lane.getCenterLine().projectFractionalAt(lane.getLink().getStartNode().getHeading(),
                lane.getLink().getEndNode().getHeading(), location.x, location.y, FractionalFallback.ENDPOINT);
        return lane.getCenterLine().getLocationFractionExtended(frac).distance(location);
    }

    @Override
    public Synchronizable.State getSynchronizationState()
    {
        return this.syncState;
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

    /**
     * Sets a fixed acceleration.
     * @param acceleration acceleration;
     */
    public void setAcceleration(final Acceleration acceleration)
    {
        this.accelerationCommand = acceleration;
        interruptMove(getGtu().getLocation());
    }

    /**
     * Removes fixed acceleration.
     */
    public void resetAcceleration()
    {
        this.accelerationCommand = null;
        interruptMove(getGtu().getLocation());
    }

    /**
     * Sets the indicator.
     * @param indicator indicator
     * @param duration duration
     */
    public void setIndicator(final LateralDirectionality indicator, final Duration duration)
    {
        this.indicatorCommand = indicator;
        getGtu().getSimulator().scheduleEventRel(duration, this, "resetIndicator", new Object[0]);
        interruptMove(getGtu().getLocation());
    }

    /**
     * Resets the indicator.
     */
    @SuppressWarnings("unused") // scheduled
    private void resetIndicator()
    {
        this.indicatorCommand = null;
        interruptMove(getGtu().getLocation());
    }

    /**
     * Disable lane changes.
     */
    public void disableLaneChanges()
    {
        this.laneChangesEnabledCommand = false;
        interruptMove(getGtu().getLocation());
    }

    /**
     * Enable lane changes.
     */
    public void enableLaneChanges()
    {
        this.laneChangesEnabledCommand = true;
        interruptMove(getGtu().getLocation());
    }

    /**
     * Sets the desired speed.
     * @param speed desired speed.
     */
    public void setDesiredSpeed(final Speed speed)
    {
        clearCache();
        this.desiredSpeedModel =
                ((AbstractCarFollowingModel) getCarFollowingModel()).setDesiredSpeedModel(new DesiredSpeedModel()
                {
                    @Override
                    public Speed desiredSpeed(final Parameters parameters, final SpeedLimits speedLimits,
                            final Speed maxVehicleSpeed) throws ParameterException
                    {
                        return speed;
                    }
                });
        interruptMove(getGtu().getLocation());
    }

    /**
     * Reset desired speed.
     */
    public void resetDesiredSpeed()
    {
        clearCache();
        Throw.when(this.desiredSpeedModel == null, IllegalStateException.class,
                "Attempting to reset desired speed, but no desired speed was ever set.");
        ((AbstractCarFollowingModel) getCarFollowingModel()).setDesiredSpeedModel(this.desiredSpeedModel);
        this.desiredSpeedModel = null;
        interruptMove(getGtu().getLocation());
    }

    /**
     * Clears the cache for desired speed and acceleration, so the set desired speed has effect even if a plan has been
     * calculated at the same time.
     */
    private void clearCache()
    {
        try
        {
            // clear time of cached desired speed, so a new value will be calculated
            Field speedCacheField = LaneBasedGtu.class.getDeclaredField("desiredSpeedTime");
            speedCacheField.setAccessible(true);
            speedCacheField.set(getGtu(), null);
            // clear time of cached acceleration, so a new value will be calculated
            Field accelerationCacheField = LaneBasedGtu.class.getDeclaredField("carFollowingAccelerationTime");
            accelerationCacheField.setAccessible(true);
            accelerationCacheField.set(getGtu(), null);
        }
        catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Set parameter.
     * @param parameter parameter type, either complete dot notation to static field, or ID in
     *            {@link org.opentrafficsim.cosim.Parameters Parameters}.
     * @param value string representation of the value
     * @throws ParameterException when the parameter value does not comply to the type
     */
    @SuppressWarnings("unchecked")
    public void setParameter(final String parameter, final String value) throws ParameterException
    {
        ParameterType<?> parameterType;
        try
        {
            int dot = parameter.lastIndexOf(".");
            if (dot < 0)
            {
                parameterType = org.opentrafficsim.cosim.Parameters.get(parameter);
            }
            else
            {
                Class<?> clazz = Class.forName(parameter.substring(0, dot));
                Field field = clazz.getDeclaredField(parameter.substring(dot + 1, parameter.length()));
                parameterType = (ParameterType<?>) field.get(null);
            }
        }
        catch (NoSuchFieldException | ClassNotFoundException | IllegalArgumentException | IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
        if (parameterType.getValueClass().equals(Acceleration.class))
        {
            getGtu().getParameters().setParameter((ParameterType<Acceleration>) parameterType, Acceleration.valueOf(value));
        }
        else if (parameterType.getValueClass().equals(Duration.class))
        {
            getGtu().getParameters().setParameter((ParameterType<Duration>) parameterType, Duration.valueOf(value));
        }
        else if (parameterType.getValueClass().equals(Length.class))
        {
            getGtu().getParameters().setParameter((ParameterType<Length>) parameterType, Length.valueOf(value));
        }
        else if (parameterType.getValueClass().equals(Speed.class))
        {
            getGtu().getParameters().setParameter((ParameterType<Speed>) parameterType, Speed.valueOf(value));
        }
        else if (parameterType.getValueClass().equals(Time.class))
        {
            getGtu().getParameters().setParameter((ParameterType<Time>) parameterType, Time.valueOf(value));
        }
        else if (parameterType.getValueClass().equals(Double.class))
        {
            getGtu().getParameters().setParameter((ParameterType<Double>) parameterType, Double.valueOf(value));
        }
        else
        {
            throw new RuntimeException("Setting parameter of type " + parameterType.getValueClass() + " is not supported.");
        }
    }

    /**
     * Initiates a lane change.
     * @param direction lane change direction.
     */
    public void changeLane(final LateralDirectionality direction)
    {
        this.laneChangeCommand = direction;
        interruptMove(getGtu().getLocation());
    }

    /**
     * Returns the last intended operational plan, and internally sets it to null. Next calls will return null until the
     * behavioural model has run again.
     * @return last intended operational plan
     */
    public OperationalPlan pullLastIntendedPlan()
    {
        OperationalPlan plan = this.lastIntendedPlan;
        this.lastIntendedPlan = null;
        return plan;
    }

    /**
     * Invokes {@code interruptMove} on the GTU through reflection. This will cancel the scheduled move event, and trigger a new
     * move now.
     * @param location location
     */
    private void interruptMove(final DirectedPoint2d location)
    {
        try
        {
            // there's a bug in interruptMove(), so need to perform its contents indirectly
            Field field = Gtu.class.getDeclaredField("nextMoveEvent");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            SimEvent<Duration> event = (SimEvent<Duration>) field.get(getGtu());
            getGtu().getSimulator().cancelEvent(event);

            Method move = Gtu.class.getDeclaredMethod("move", DirectedPoint2d.class);
            move.setAccessible(true);
            move.invoke(getGtu(), location);
        }
        catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchFieldException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Starts dead reckoning.
     * @param hybrid whether dead-reckoning is part of a hybrid mode where the external simulation determines movement based on
     *            the intention of a model plan
     */
    public void startDeadReckoning(final boolean hybrid)
    {
        this.lastDeadReckoningModelExecution = null;
        this.hybridDeadReckoning = hybrid;
        deadReckoning(getGtu().getLocation(), getGtu().getSpeed(), Acceleration.ZERO);
    }

    /**
     * Sets location, speed and acceleration for dead reckoning.
     * @param location location
     * @param speed speed
     * @param accel acceleration
     */
    public void deadReckoning(final DirectedPoint2d location, final Speed speed, final Acceleration accel)
    {
        this.deadReckoning = true;
        this.deadReckoningSpeed = speed;
        this.deadReckoningAcceleration = accel;
        interruptMove(location);
    }

    /**
     * Stops dead reckoning and recalculates a plan.
     */
    public void stopDeadReckoning()
    {
        this.deadReckoning = false;
        this.lastDeadReckoningModelExecution = null;
        this.hybridDeadReckoning = false;
        interruptMove(getGtu().getLocation());
    }

    @Override
    public final String toString()
    {
        return "ScenarioTacticalPlanner [mandatoryIncentives=" + getMandatoryIncentives() + ", voluntaryIncentives="
                + getVoluntaryIncentives() + ", accelerationIncentives = " + getAccelerationIncentives() + "]";
    }

}
