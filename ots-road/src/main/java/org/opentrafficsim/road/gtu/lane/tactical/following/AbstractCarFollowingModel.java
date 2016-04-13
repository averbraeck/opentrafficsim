package org.opentrafficsim.road.gtu.lane.tactical.following;

import java.util.SortedMap;
import java.util.TreeMap;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;

/**
 * Implements free, single-leader and multi-anticipative methods as forwards to a new multi-anticipative method with desired
 * headway and desired speed pre-calculated. The information forwarded to the new method implies a leader at infinite headway
 * for free acceleration, and a single vehicle in the set for single-leader acceleration. As a result, implementations of this
 * class only need to implement a single, and simpler, method covering different calls to the car-following model. The headway
 * towards the first vehicle being followed is guaranteed to be positive.
 * @author Wouter Schakel
 */
public abstract class AbstractCarFollowingModel implements CarFollowingModel
{

    /** {@inheritDoc} */
    public final Acceleration freeAcceleration(final BehavioralCharacteristics behavioralCharacteristics, final Speed speed,
        final Speed speedLimit, final boolean enforcement, final Speed maximumVehicleSpeed) throws ParameterException
    {
        // Fill map with a single leader at infinite headway and the same speed.
        SortedMap<Length.Rel, Speed> leaders = new TreeMap<Length.Rel, Speed>();
        leaders.put(new Length.Rel(Double.POSITIVE_INFINITY, LengthUnit.SI), speed);
        return followingAcceleration(behavioralCharacteristics, speed, desiredSpeed(behavioralCharacteristics, speedLimit,
            enforcement, maximumVehicleSpeed), desiredHeadway(behavioralCharacteristics, speed), leaders);
    }

    /** {@inheritDoc} */
    public final Acceleration followingAcceleration(final BehavioralCharacteristics behavioralCharacteristics,
        final Speed speed, final Speed speedLimit, final boolean enforcement, final Speed maximumVehicleSpeed,
        final Length.Rel headway, final Speed leaderSpeed) throws ParameterException
    {
        // Catch negative headway
        if (headway.si <= 0)
        {
            // The car-following model is undefined for this case, return 'inappropriate' acceleration. Whatever uses
            // the car-following model has to figure out what to do in this situation. E.g. limit deceleration to an
            // extent depending on the circumstances, or divert from a certain behavior.
            return new Acceleration(Double.NEGATIVE_INFINITY, AccelerationUnit.SI);
        }
        // Fill map with the single leader.
        SortedMap<Length.Rel, Speed> leaders = new TreeMap<Length.Rel, Speed>();
        leaders.put(headway, leaderSpeed);
        return followingAcceleration(behavioralCharacteristics, speed, desiredSpeed(behavioralCharacteristics, speedLimit,
            enforcement, maximumVehicleSpeed), desiredHeadway(behavioralCharacteristics, speed), leaders);
    }

    /** {@inheritDoc} */
    public final Acceleration followingAcceleration(final BehavioralCharacteristics behavioralCharacteristics,
        final Speed speed, final Speed speedLimit, final boolean enforcement, final Speed maximumVehicleSpeed,
        final SortedMap<Length.Rel, Speed> leaders) throws ParameterException
    {
        // Catch negative headway
        if (leaders.firstKey().si <= 0)
        {
            // The car-following model is undefined for this case, return 'inappropriate' acceleration. Whatever uses
            // the car-following model has to figure out what to do in this situation. E.g. limit deceleration to an
            // extent depending on the circumstances, or divert from a certain behavior.
            return new Acceleration(Double.NEGATIVE_INFINITY, AccelerationUnit.SI);
        }
        // Forward to method with desired speed and headway predetermined by this car-following model.
        return followingAcceleration(behavioralCharacteristics, speed, desiredSpeed(behavioralCharacteristics, speedLimit,
            enforcement, maximumVehicleSpeed), desiredHeadway(behavioralCharacteristics, speed), leaders);
    }

    /**
     * Multi-anticipative determination of car-following acceleration. The implementation should be able to deal with the
     * current speed being higher than the desired speed. The tactical planner determines whether multi-anticipative
     * car-following is applied, including to how many leaders and within what distance, by including these vehicles in the set.
     * The car-following model itself may however only respond to the first vehicle.
     * @param behavioralCharacteristics Behavioral characteristics.
     * @param speed Current speed.
     * @param desiredSpeed Desired speed.
     * @param desiredHeadway Desired headway.
     * @param leaders Set of leader headways (guaranteed positive) and speeds, ordered by headway (closest first).
     * @throws ParameterException If parameter exception occurs.
     * @return Car-following acceleration.
     */
    protected abstract Acceleration followingAcceleration(BehavioralCharacteristics behavioralCharacteristics, Speed speed,
        Speed desiredSpeed, Length.Rel desiredHeadway, SortedMap<Length.Rel, Speed> leaders) throws ParameterException;

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public String toString()
    {
        return getLongName();
    }

}
