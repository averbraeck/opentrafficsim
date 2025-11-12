package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.perception.EgoPerception;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;

/**
 * Context category representing ego-vehicle-related state variables.
 * <p>
 * Provides direct access to low-level vehicle states such as speed,
 * which are frequently required by tactical and longitudinal control logic.
 * </p>
 * <p>
 * The values are lazily updated once per simulation tick and cached
 * within the {@link VehicleContextManager}.
 * </p>
 */
public class EgoContext extends ContextCategory implements UpdatableContext {

    /** Cache key for ego speed. */
    public static final String EGO_SPEED = "egoSpeed";
    public static final String CURRENT_CF_ACCELERATION = "currentCarFollowingAcceleration";
    // ----------------------------------------------------------------------
    // Construction
    // ----------------------------------------------------------------------

    /**
     * Constructs a new {@code EgoContext}.
     *
     * @param vehicle the ego vehicle associated with this context
     */
    public EgoContext(final MirovaTacticalPlanner vehicle) {
        super("Ego", vehicle);
    }

    // ----------------------------------------------------------------------
    // Lazy Accessors
    // ----------------------------------------------------------------------

    /**
     * Returns the current ego-vehicle speed as perceived in the last simulation tick.
     * <p>
     * This method uses lazy evaluation: the speed is only retrieved once per tick
     * from {@link EgoPerception} and then cached.
     * </p>
     *
     * @return current ego speed [m/s]
     */
    public Speed getEgoSpeed() {
        Speed cached = getCachedValue(EGO_SPEED, Speed.class);
        if (cached != null) return cached;

        Speed result = computeEgoSpeed();
        cacheValue(EGO_SPEED, result, true);
        return result;
    }

    /**
     * @return
     * @throws ParameterException
     * @throws GtuException
     * @throws NetworkException
     */
    public Acceleration getCurrentCarFollowingAcceleration() throws ParameterException, GtuException, NetworkException {
        Acceleration cached = getCachedValue(CURRENT_CF_ACCELERATION, Acceleration.class);
        if (cached != null) return cached;

        Acceleration result = this.vehicle.computeLongitudinalAcceleration();
        cacheValue(CURRENT_CF_ACCELERATION, result, true);
        return result;
    }

    // ----------------------------------------------------------------------
    // Safe computation wrapper
    // ----------------------------------------------------------------------

    /**
     * Safely computes the ego-vehicle speed from {@link EgoPerception}.
     * Returns zero speed in case of missing perception data or errors.
     *
     * @return ego speed or {@link Speed#ZERO} on error
     */
    private Speed computeEgoSpeed() {
        try {
            return this.vehicle.getPerception()
                    .getPerceptionCategory(EgoPerception.class)
                    .getSpeed();
        } catch (Exception e) {
            return Speed.ZERO;
        }
    }

    // ----------------------------------------------------------------------
    // Update handling
    // ----------------------------------------------------------------------

    /**
     * Marks the cached values as valid for the current simulation tick.
     * <p>
     * No immediate update is required, as values are computed lazily.
     * </p>
     *
     * @param vehicle the ego vehicle (unused)
     */
    @Override
    public void updateFromPerception(final MirovaTacticalPlanner vehicle) {
        markCacheValid();
    }

    /**
     * Returns a compact textual summary of the currently cached ego parameters.
     *
     * @return summary string
     */
    @Override
    public String toString() {
        return "EgoContext[" +
                "egoSpeed=" + getCachedValue(EGO_SPEED, Speed.class) +
                "]";
    }
}
