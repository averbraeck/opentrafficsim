package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context;

import org.djunits.unit.LinearDensityUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.LinearDensity;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.TrafficPerception;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;

/**
 * Context category providing aggregated macroscopic traffic characteristics around the ego vehicle.
 * <p>
 * Forms a part of <b>Layer 1 (Perception & Context)</b> in the MiRoVA architecture.
 * This includes average speeds and densities on the current and adjacent lanes.
 * The information is lazily evaluated and cached once per simulation tick.
 * </p>
 * <p>
 * These macroscopic quantities can be used for higher-level tactical reasoning,
 * such as evaluating flow conditions, cooperative behavior, or capacity effects.
 * </p>
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 *
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public class MacroTrafficContext extends ContextCategory implements UpdatableContext {

    // ----------------------------------------------------------------------
    // Cached value keys
    // ----------------------------------------------------------------------

    /** Cache key for average speed on the current lane. */
    private static final String AVG_SPEED_CURRENT = "avgSpeed_CURRENT";
    /** Cache key for average speed on the left lane. */
    private static final String AVG_SPEED_LEFT = "avgSpeed_LEFT";
    /** Cache key for average speed on the right lane. */
    private static final String AVG_SPEED_RIGHT = "avgSpeed_RIGHT";
    /** Cache key for traffic density on the current lane. */
    private static final String DENSITY_CURRENT = "density_CURRENT";
    /** Cache key for traffic density on the left lane. */
    private static final String DENSITY_LEFT = "density_LEFT";
    /** Cache key for traffic density on the right lane. */
    private static final String DENSITY_RIGHT = "density_RIGHT";

    // ----------------------------------------------------------------------
    // Construction
    // ----------------------------------------------------------------------

    /**
     * Constructs a new {@code MacroTrafficContext} for the given vehicle.
     *
     * @param vehicle the ego vehicle executing this context category
     */
    public MacroTrafficContext(final MirovaTacticalPlanner vehicle) {
        super("MacroTraffic", vehicle);
    }

    // ----------------------------------------------------------------------
    // Lazy Accessors
    // ----------------------------------------------------------------------

    /**
     * Returns the average speed of traffic on the current lane.
     * <p>
     * Uses the {@link TrafficPerception} category to retrieve the instantaneous,
     * lane-aggregated mean speed. If unavailable, returns {@code NaN}.
     * </p>
     *
     * @return average lane speed
     * @throws ParameterException if parameter lookup fails
     * @throws OperationalPlanException if perception access fails
     */
    public Speed getAverageSpeedCurrent() throws OperationalPlanException, ParameterException {
        Speed cached = getCachedValue(AVG_SPEED_CURRENT, Speed.class);
        if (cached != null) {
            return cached;
        }

        Speed result = computeAverageSpeed(RelativeLane.CURRENT);
        cacheValue(AVG_SPEED_CURRENT, result, true);
        return result;
    }

    /**
     * Returns the average speed on the left adjacent lane.
     *
     * @return average lane speed
     * @throws ParameterException if parameter lookup fails
     * @throws OperationalPlanException if perception access fails
     */
    public Speed getAverageSpeedLeft() throws OperationalPlanException, ParameterException {
        Speed cached = getCachedValue(AVG_SPEED_LEFT, Speed.class);
        if (cached != null) {
            return cached;
        }

        Speed result = computeAverageSpeed(RelativeLane.LEFT);
        cacheValue(AVG_SPEED_LEFT, result, true);
        return result;
    }

    /**
     * Returns the average speed on the right adjacent lane.
     *
     * @return average lane speed
     * @throws ParameterException if parameter lookup fails
     * @throws OperationalPlanException if perception access fails
     */
    public Speed getAverageSpeedRight() throws OperationalPlanException, ParameterException {
        Speed cached = getCachedValue(AVG_SPEED_RIGHT, Speed.class);
        if (cached != null) {
            return cached;
        }

        Speed result = computeAverageSpeed(RelativeLane.RIGHT);
        cacheValue(AVG_SPEED_RIGHT, result, true);
        return result;
    }

    /**
     * Returns the estimated traffic density on the current lane.
     * <p>
     * Uses {@link TrafficPerception} to retrieve the instantaneous macroscopic
     * density. Returns NaN if perception data are not available.
     * </p>
     *
     * @return traffic density
     * @throws ParameterException if parameter lookup fails
     * @throws OperationalPlanException if perception access fails
     */
    public LinearDensity getDensityCurrent() throws OperationalPlanException, ParameterException {
        LinearDensity cached = getCachedValue(DENSITY_CURRENT, LinearDensity.class);
        if (cached != null) {
            return cached;
        }

        LinearDensity result = computeDensity(RelativeLane.CURRENT);
        cacheValue(DENSITY_CURRENT, result, true);
        return result;
    }

    /**
     * Returns the estimated traffic density on the left adjacent lane.
     *
     * @return traffic density
     * @throws ParameterException if parameter lookup fails
     * @throws OperationalPlanException if perception access fails
     */
    public LinearDensity getDensityLeft() throws OperationalPlanException, ParameterException {
        LinearDensity cached = getCachedValue(DENSITY_LEFT, LinearDensity.class);
        if (cached != null) {
            return cached;
        }

        LinearDensity result = computeDensity(RelativeLane.LEFT);
        cacheValue(DENSITY_LEFT, result, true);
        return result;
    }

    /**
     * Returns the estimated traffic density on the right adjacent lane.
     *
     * @return traffic density
     * @throws ParameterException if parameter lookup fails
     * @throws OperationalPlanException if perception access fails
     */
    public LinearDensity getDensityRight() throws OperationalPlanException, ParameterException {
        LinearDensity cached = getCachedValue(DENSITY_RIGHT, LinearDensity.class);
        if (cached != null) {
            return cached;
        }

        LinearDensity result = computeDensity(RelativeLane.RIGHT);
        cacheValue(DENSITY_RIGHT, result, true);
        return result;
    }

    /**
     * Returns the density on the specified lane.
     * <p>
     * Wrapper around lane-specific accessors that generalizes lookup
     * for {@link RelativeLane#CURRENT}, {@link RelativeLane#LEFT},
     * and {@link RelativeLane#RIGHT}.
     * </p>
     *
     * @param lane the lane for which to retrieve density
     * @return traffic density
     * @throws ParameterException if parameter lookup fails
     * @throws OperationalPlanException if perception access fails
     * @throws IllegalArgumentException if the lane is unsupported
     */
    public LinearDensity getDensity(final RelativeLane lane)
            throws OperationalPlanException, ParameterException {
        if (lane.equals(RelativeLane.CURRENT)) {
            return getDensityCurrent();
        } else if (lane.equals(RelativeLane.LEFT)) {
            return getDensityLeft();
        } else if (lane.equals(RelativeLane.RIGHT)) {
            return getDensityRight();
        } else {
            throw new IllegalArgumentException("Unsupported lane: " + lane);
        }
    }

    /**
     * Returns the average speed on the specified lane.
     * <p>
     * Wrapper for generalized access, used for convenience
     * in tactical reasoning logic.
     * </p>
     *
     * @param lane the lane for which to retrieve average speed
     * @return average lane speed
     * @throws ParameterException if parameter lookup fails
     * @throws OperationalPlanException if perception access fails
     * @throws IllegalArgumentException if the lane is unsupported
     */
    public Speed getAverageSpeed(final RelativeLane lane)
            throws OperationalPlanException, ParameterException {
        if (lane.equals(RelativeLane.CURRENT)) {
            return getAverageSpeedCurrent();
        } else if (lane.equals(RelativeLane.LEFT)) {
            return getAverageSpeedLeft();
        } else if (lane.equals(RelativeLane.RIGHT)) {
            return getAverageSpeedRight();
        } else {
            throw new IllegalArgumentException("Unsupported lane: " + lane);
        }
    }

    // ----------------------------------------------------------------------
    // Core Computations
    // ----------------------------------------------------------------------

    /**
     * Computes the average speed on a given lane using {@link TrafficPerception}.
     *
     * @param lane the lane to evaluate (CURRENT, LEFT, RIGHT)
     * @return average speed
     * @throws ParameterException if perception parameters fail
     * @throws OperationalPlanException if access to perception data fails
     */
    private Speed computeAverageSpeed(final RelativeLane lane)
            throws ParameterException, OperationalPlanException {
        TrafficPerception trafficPerception = this.vehicle.getPerception()
                .getPerceptionCategory(TrafficPerception.class);

        if (trafficPerception == null) {
            return new Speed(Double.NaN, SpeedUnit.SI);
        }

        return trafficPerception.getSpeed(lane);
    }

    /**
     * Computes the traffic density on a given lane using {@link TrafficPerception}.
     *
     * @param lane the lane for which to estimate density
     * @return density
     * @throws ParameterException if parameter lookup fails
     * @throws OperationalPlanException if perception access fails
     */
    private LinearDensity computeDensity(final RelativeLane lane)
            throws OperationalPlanException, ParameterException {
        TrafficPerception trafficPerception = this.vehicle.getPerception()
                .getPerceptionCategory(TrafficPerception.class);

        if (trafficPerception == null) {
            return new LinearDensity(Double.NaN, LinearDensityUnit.PER_KILOMETER);
        }

        return trafficPerception.getDensity(lane);
    }

    // ----------------------------------------------------------------------
    // Context Lifecycle
    // ----------------------------------------------------------------------

    /**
     * Marks the context as valid for the current simulation tick.
     * <p>
     * This context uses lazy evaluation and thus does not update
     * any data preemptively. Values are recomputed on demand when
     * first accessed during the tick.
     * </p>
     *
     * @param vehicle the associated {@link MirovaTacticalPlanner} executing the update
     */
    @Override
    public void updateFromPerception(final MirovaTacticalPlanner vehicle) {
        markCacheValid();
    }

    /**
     * Returns a string representation summarizing cached key values.
     *
     * @return human-readable description of cached values
     */
    @Override
    public String toString() {
        return "MacroTrafficContext[" +
                "avgSpeedCurrent=" + getCachedValue(AVG_SPEED_CURRENT, Speed.class) +
                ", densityCurrent=" + getCachedValue(DENSITY_CURRENT, LinearDensity.class) +
                "]";
    }
}