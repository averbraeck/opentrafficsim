package org.opentrafficsim.road.gtu.lane.tactical.following;

import java.util.SortedMap;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;

/**
 * Methods that a car-following model has to implement. The characteristics are supplied to obtain parameters. Other input into
 * the methods describe the car-following situation.
 * @author Wouter Schakel
 */
public interface CarFollowingModel
{

    /**
     * Determines the desired speed.
     * @param behavioralCharacteristics Behavioral characteristics.
     * @param speedLimit Speed limit, static or dynamic.
     * @param enforcement Whether the speed limit is enforced by camera, section control, etc.
     * @param maximumVehicleSpeed Maximum speed of the vehicle.
     * @throws ParameterException If parameter exception occurs.
     * @return Desired speed.
     */
    Speed desiredSpeed(BehavioralCharacteristics behavioralCharacteristics, Speed speedLimit, boolean enforcement,
        Speed maximumVehicleSpeed) throws ParameterException;

    /**
     * Determines the desired headway.
     * @param behavioralCharacteristics Behavioral characteristics.
     * @param speed Speed to determine the desired headway at.
     * @throws ParameterException If parameter exception occurs.
     * @return Desired headway.
     */
    Length.Rel desiredHeadway(BehavioralCharacteristics behavioralCharacteristics, Speed speed) throws ParameterException;

    /**
     * Determines the acceleration if there is no reason to decelerate.
     * @param behavioralCharacteristics Behavioral characteristics.
     * @param speed Current speed.
     * @param speedLimit Speed limit, static or dynamic.
     * @param enforcement Whether the speed limit is enforced by camera, section control, etc.
     * @param maximumVehicleSpeed Maximum speed of the vehicle.
     * @throws ParameterException If parameter exception occurs.
     * @return Acceleration if there is no reason to decelerate.
     */
    Acceleration freeAcceleration(BehavioralCharacteristics behavioralCharacteristics, Speed speed, Speed speedLimit,
        boolean enforcement, Speed maximumVehicleSpeed) throws ParameterException;

    /**
     * Determines car-following acceleration. The implementation should be able to deal with:<br>
     * <ul><li>The current speed being higher than the desired speed.</li> <li>The headway being negative.</li></ul>
     * @param behavioralCharacteristics Behavioral characteristics.
     * @param speed Current speed.
     * @param speedLimit Speed limit, static or dynamic.
     * @param enforcement Whether the speed limit is enforced by camera, section control, etc.
     * @param maximumVehicleSpeed Maximum speed of the vehicle.
     * @param headway Net headway towards the leading vehicle.
     * @param leaderSpeed Speed of the leading vehicle.
     * @throws ParameterException If parameter exception occurs.
     * @return Car-following acceleration.
     */
    Acceleration followingAcceleration(BehavioralCharacteristics behavioralCharacteristics, Speed speed, Speed speedLimit,
        boolean enforcement, Speed maximumVehicleSpeed, Length.Rel headway, Speed leaderSpeed) throws ParameterException;

    /**
     * Multi-anticipative determination of car-following acceleration. The implementation should be able to deal with:<br>
     * <ul><li>The current speed being higher than the desired speed.</li> <li>The headway being negative. The tactical planner
     * determines whether multi-anticipative car-following is applied, including to how many leaders and within what distance,
     * by including these vehicles in the set. The car-following model itself may however only respond to the first vehicle.
     * @param behavioralCharacteristics Behavioral characteristics.</li></ul>
     * @param speed Current speed.
     * @param speedLimit Speed limit, static or dynamic.
     * @param enforcement Whether the speed limit is enforced by camera, section control, etc.
     * @param maximumVehicleSpeed Maximum speed of the vehicle.
     * @param leaders Set of leader headways and speeds, ordered by headway (closest first).
     * @throws ParameterException If parameter exception occurs.
     * @return Car-following acceleration.
     */
    Acceleration followingAcceleration(BehavioralCharacteristics behavioralCharacteristics, Speed speed, Speed speedLimit,
        boolean enforcement, Speed maximumVehicleSpeed, SortedMap<Length.Rel, Speed> leaders) throws ParameterException;

    /**
     * Return the name of the car-following model.
     * @return Name of the car-following model.
     */
    String getName();

    /**
     * Return the name complete of the car-following model.
     * @return Complete name of the car-following model.
     */
    String getLongName();

}
