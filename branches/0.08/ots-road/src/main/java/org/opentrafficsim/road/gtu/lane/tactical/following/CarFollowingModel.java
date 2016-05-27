package org.opentrafficsim.road.gtu.lane.tactical.following;

import java.util.SortedMap;

import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.opentrafficsim.core.gtu.drivercharacteristics.ParameterException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;

/**
 * Methods that a car-following model has to implement. The GTU is supplied to obtain characteristics such as 
 * parameters. Other input into the methods describe the car-following situation. The GTU should thus <b>not</b> be used
 * to obtain: position, speed, speed limit at current location, leader, etc.
 * @author Wouter Schakel
 */
public interface CarFollowingModel {

	/**
	 * Determines the desired speed.
	 * @param gtu GTU for which the acceleration is calculated.
	 * @param speedLimit Speed limit, static or dynamic.
	 * @param enforcement Whether the speed limit is enforced by camera, section control, etc.
	 * @param maximumVehicleSpeed Maximum speed of the vehicle.
     * @throws ParameterException If parameter exception occurs.
	 * @return Desired speed.
	 */
	Speed desiredSpeed(LaneBasedGTU gtu, Speed speedLimit, boolean enforcement, Speed maximumVehicleSpeed) 
			throws ParameterException;				

	/**
	 * Determines the desired headway.
	 * @param gtu GTU for which the acceleration is calculated.
	 * @param speed Speed to determine the desired headway at.
     * @throws ParameterException If parameter exception occurs.
	 * @return Desired headway.
	 */
	Length.Rel desiredHeadway(LaneBasedGTU gtu, Speed speed) throws ParameterException;

	/**
	 * Determines the acceleration if there is no reason to decelerate.
	 * @param gtu GTU for which the acceleration is calculated.
	 * @param speed Current speed.
	 * @param speedLimit Speed limit, static or dynamic.
	 * @param enforcement Whether the speed limit is enforced by camera, section control, etc.
	 * @param maximumVehicleSpeed Maximum speed of the vehicle.
	 * @throws ParameterException If parameter exception occurs.
     * @return Acceleration if there is no reason to decelerate.
	 */ 
	Acceleration freeAcceleration(LaneBasedGTU gtu, Speed speed, Speed speedLimit, boolean enforcement, 
			Speed maximumVehicleSpeed) throws ParameterException;

	/**
	 * Determines car-following acceleration. The implementation should be able to deal with:<br>
	 * The current speed being higher than the desired speed.
	 * The headway being negative.<br><br>
	 * @param gtu GTU for which the acceleration is calculated.
	 * @param speed Current speed.
	 * @param speedLimit Speed limit, static or dynamic.
	 * @param enforcement Whether the speed limit is enforced by camera, section control, etc.
	 * @param maximumVehicleSpeed Maximum speed of the vehicle.
	 * @param headway Net headway towards the leading vehicle.
	 * @param leaderSpeed Speed of the leading vehicle.
     * @throws ParameterException If parameter exception occurs.
	 * @return Car-following acceleration.
	 */
	Acceleration followingAcceleration(LaneBasedGTU gtu, Speed speed, Speed speedLimit, boolean enforcement, 
			Speed maximumVehicleSpeed, Length.Rel headway, Speed leaderSpeed) throws ParameterException;

	/**
	 * Multi-anticipative determination of car-following acceleration. The implementation should be able to deal with:<br>
	 * The current speed being higher than the desired speed.
	 * The headway being negative.
	 * The tactical planner determines whether multi-anticipative car-following is applied, including to how many
	 * leaders and within what distance, by including these vehicles in the set. The car-following model itself may
	 * however only respond to the first vehicle.
	 * @param gtu GTU for which the acceleration is calculated.
	 * @param speed Current speed.
	 * @param speedLimit Speed limit, static or dynamic.
	 * @param enforcement Whether the speed limit is enforced by camera, section control, etc.
	 * @param maximumVehicleSpeed Maximum speed of the vehicle.
	 * @param leaders Set of leader headways and speeds, ordered by headway (closest first).
     * @throws ParameterException If parameter exception occurs.
	 * @return Car-following acceleration.
	 */
	Acceleration followingAcceleration(LaneBasedGTU gtu, Speed speed, Speed speedLimit, boolean enforcement, 
			Speed maximumVehicleSpeed, SortedMap<Length.Rel, Speed> leaders) throws ParameterException;

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