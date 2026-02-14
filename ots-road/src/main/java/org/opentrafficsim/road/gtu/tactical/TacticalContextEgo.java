package org.opentrafficsim.road.gtu.tactical;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.TreeMap;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.DistancedObject;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.gtu.perception.EgoPerception;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.LaneBasedGtu;
import org.opentrafficsim.road.gtu.perception.LanePerception;
import org.opentrafficsim.road.gtu.perception.RelativeLane;
import org.opentrafficsim.road.gtu.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.network.LanePosition;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

/**
 * Wraps a {@link LaneBasedGtu} and provides bundled and easy-access information that tactical models and their components need
 * regarding the ego-vehicle. This information may be cached, such that different components do not need to repeat expensive
 * steps to obtain certain information. Because of the caching, this is a throw-away class that should only have a lifetime
 * equal to a single model execution.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class TacticalContextEgo implements TacticalContext
{

    /** GTU. */
    private final LaneBasedGtu gtu;

    /** Perception. */
    private LanePerception perception;

    /** Route. */
    private Optional<Route> route;

    /** Desired speed from car-following model. */
    private Speed desiredSpeed;

    /** Car-following acceleration. */
    private Acceleration carFollowingAcceleration;

    /** Intents. */
    private Map<Class<?>, NavigableMap<Length, Object>> intents = new LinkedHashMap<>();

    /** Parameters. */
    private Parameters parameters;

    /** Car-following model. */
    private CarFollowingModel carFollowingModel;

    /** Speed limit info. */
    private SpeedLimitInfo speedLimitInfo;

    /** Ego length. */
    private Length length;

    /** Ego width. */
    private Length width;

    /** Ego speed. */
    private Speed speed;

    /** Ego acceleration. */
    private Acceleration acceleration;

    /**
     * Constructor.
     * @param gtu GTU
     */
    public TacticalContextEgo(final LaneBasedGtu gtu)
    {
        this.gtu = gtu;
    }

    /**
     * Returns the GTU. This is tagged as unsafe because models should not typically interfere with the simulation structure.
     * Still, sometimes information or commands are required outside of the tactical context. This should only be done with care
     * and full understanding of what is being done.
     * @return GTU
     */
    public LaneBasedGtu getUnsafeGtu()
    {
        return this.gtu;
    }

    /**
     * Returns the perception.
     * @return perception
     */
    public LanePerception getPerception()
    {
        if (this.perception == null)
        {
            this.perception = this.gtu.getTacticalPlanner().getPerception();
        }
        return this.perception;
    }

    /**
     * Returns the current time.
     * @return current time
     */
    public Duration getTime()
    {
        return getUnsafeGtu().getSimulator().getSimulatorTime();
    }

    /**
     * Returns the car-following acceleration from the car-following model and leaders perceived by the neighbor category.
     * @return car-following acceleration from the car-following model
     * @throws NoSuchElementException when neighbors perception is not present
     * @throws ParameterException if a parameter is not present
     */
    public Acceleration getCarFollowingAcceleration() throws ParameterException
    {
        if (this.carFollowingAcceleration == null)
        {
            var leaders = getPerception().getPerceptionCategoryOptional(NeighborsPerception.class)
                    .orElseThrow(() -> new NoSuchElementException(
                            "To obtain car-following acceleration a neighbors perception category is required."))
                    .getLeaders(RelativeLane.CURRENT);
            this.carFollowingAcceleration =
                    getCarFollowingModel().followingAcceleration(getParameters(), getSpeed(), getSpeedLimitInfo(), leaders);
        }
        return this.carFollowingAcceleration;
    }

    /**
     * Returns the current position.
     * @return current position
     */
    public LanePosition getPosition()
    {
        return getUnsafeGtu().getPosition();
    }

    /**
     * Add value for intent. The type (class) of the value determines the type of intent.
     * @param intent intent value
     * @param distance distance at which the intent applies
     */
    public void addIntent(final Object intent, final Length distance)
    {
        this.intents.computeIfAbsent(intent.getClass(), (clazz) -> new TreeMap<>()).put(distance, intent);
    }

    /**
     * Returns the nearest value of intent of type given by the class.
     * @param <T> value type
     * @param clazz class of type
     * @return nearest value of intent of type given by the class, empty if no such intent
     * @throws NullPointerException when any input is {@code null}
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<DistancedObject<T>> getIntent(final Class<T> clazz)
    {
        Throw.whenNull(clazz, "clazz");
        if (this.intents.containsKey(clazz))
        {
            Entry<Length, Object> entry = this.intents.get(clazz).firstEntry();
            return Optional.of(new DistancedObject<>((T) entry.getValue(), entry.getKey()));
        }
        return Optional.empty();
    }

    /**
     * Returns the nearest value of intent of type given by the class, if within a distance.
     * @param <T> value type
     * @param clazz class of type
     * @param horizon obtain intent only within this distance
     * @return nearest value of intent of type given by the class, empty if no such intent
     * @throws NullPointerException when any input is {@code null}
     */
    public <T> Optional<DistancedObject<T>> getIntent(final Class<T> clazz, final Length horizon)
    {
        Throw.whenNull(horizon, "horizon");
        Optional<DistancedObject<T>> intent = getIntent(clazz);
        if (intent.isPresent() && intent.get().distance().le(horizon))
        {
            return intent;
        }
        return Optional.empty();
    }

    @Override
    public String getId()
    {
        return getUnsafeGtu().getId();
    }

    @Override
    public GtuType getGtuType()
    {
        return getUnsafeGtu().getType();
    }

    @Override
    public Parameters getParameters()
    {
        if (this.parameters == null)
        {
            this.parameters = this.gtu.getParameters();
        }
        return this.parameters;
    }

    @Override
    public CarFollowingModel getCarFollowingModel()
    {
        if (this.carFollowingModel == null)
        {
            this.carFollowingModel = this.gtu.getTacticalPlanner().getCarFollowingModel();
        }
        return this.carFollowingModel;
    }

    @Override
    public SpeedLimitInfo getSpeedLimitInfo()
    {
        if (this.speedLimitInfo == null)
        {
            this.speedLimitInfo = getPerception().getPerceptionCategoryOptional(InfrastructurePerception.class)
                    .orElseThrow(() -> new NoSuchElementException("No infrastructure perception category."))
                    .getSpeedLimitProspect(RelativeLane.CURRENT).getSpeedLimitInfo(Length.ZERO);
        }
        return this.speedLimitInfo;
    }

    @Override
    public Speed getDesiredSpeed() throws ParameterException
    {
        if (this.desiredSpeed == null)
        {
            this.desiredSpeed = getCarFollowingModel().desiredSpeed(getParameters(), getSpeedLimitInfo());
        }
        return this.desiredSpeed;
    }

    @Override
    public Optional<Route> getRoute()
    {
        if (this.route == null)
        {
            this.route = getUnsafeGtu().getStrategicalPlanner().getRoute();
        }
        return this.route;
    }

    @Override
    public LateralDirectionality getLaneChangeDirection()
    {
        return getUnsafeGtu().getLaneChangeDirection();
    }

    @Override
    public Length getLength()
    {
        checkEgo();
        return this.length;
    }

    @Override
    public Length getWidth()
    {
        checkEgo();
        return this.width;
    }

    @Override
    public Speed getSpeed()
    {
        checkEgo();
        return this.speed;
    }

    @Override
    public Acceleration getAcceleration()
    {
        checkEgo();
        return this.acceleration;
    }

    /**
     * Check ego information is cached.
     */
    private void checkEgo()
    {
        if (this.length == null)
        {
            EgoPerception<?, ?> ego = getPerception().getPerceptionCategoryOptional(EgoPerception.class)
                    .orElseThrow(() -> new NoSuchElementException("No ego perception category."));
            this.length = ego.getLength();
            this.width = ego.getWidth();
            this.speed = ego.getSpeed();
            this.acceleration = ego.getAcceleration();
        }
    }

    @Override
    public String toString()
    {
        return "TacticalContextEgo [gtu=" + this.gtu.getId() + "]";
    }

}
