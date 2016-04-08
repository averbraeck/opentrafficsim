package org.opentrafficsim.road.gtu.lane.tactical.following;

import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Length.Rel;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time.Abs;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.drivercharacteristics.ParameterException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;

/**
 * Implements free, single-leader and multi-anticipative methods as forwards to a new multi-anticipative method with
 * desired headway and speed pre-calculated. The information forwarded to the new method implies a leader at infinite
 * headway for free acceleration, and a single vehicle in the set for single-leader acceleration. As a result, 
 * implementations of this class only need to implement a single, and simpler, method covering different calls to the 
 * car-following model. The headway towards the first vehicle being followed is guaranteed to be positive.
 * TODO: do not implement GTUFollowingModelOld
 * @author Wouter Schakel
 */
public abstract class AbstractCarFollowingModel implements CarFollowingModel, GTUFollowingModelOld  {

	/** {@inheritDoc} */
	public Acceleration freeAcceleration(LaneBasedGTU gtu, Speed speed, Speed speedLimit, boolean enforcement, 
			Speed maximumVehicleSpeed) throws ParameterException {
		// Fill map with a single leader at infinite headway and the same speed.
		SortedMap<Length.Rel, Speed> leaders = new TreeMap<Length.Rel, Speed>();
		leaders.put(new Length.Rel(Double.POSITIVE_INFINITY, LengthUnit.SI), speed);
		return followingAcceleration(gtu, speed, this.desiredSpeed(gtu, speedLimit, enforcement, maximumVehicleSpeed), 
				this.desiredHeadway(gtu, speed), leaders);
	}

	/** {@inheritDoc} */
	public Acceleration followingAcceleration(LaneBasedGTU gtu, Speed speed, Speed speedLimit, boolean enforcement, 
			Speed maximumVehicleSpeed, Length.Rel headway, Speed leaderSpeed) throws ParameterException {
		// Catch negative headway
		if (headway.si<=0) {
			// The car-following model is undefined for this case, return 'inappropriate' acceleration. Whatever uses
			// the car-following model has to figure out what to do in this situation. E.g. limit deceleration to an
			// extent depending on the circumstances, or divert from a certain behavior. 
			return new Acceleration(Double.NEGATIVE_INFINITY, AccelerationUnit.SI);
		}
		// Fill map with the single leader.
		SortedMap<Length.Rel, Speed> leaders = new TreeMap<Length.Rel, Speed>();
		leaders.put(headway, leaderSpeed);
		return followingAcceleration(gtu, speed, this.desiredSpeed(gtu, speedLimit, enforcement, maximumVehicleSpeed), 
				this.desiredHeadway(gtu, speed), leaders);
	}

	/** {@inheritDoc} */
	public Acceleration followingAcceleration(LaneBasedGTU gtu, Speed speed, Speed speedLimit, boolean enforcement, 
			Speed maximumVehicleSpeed, SortedMap<Length.Rel, Speed> leaders) throws ParameterException {
		// Catch negative headway
		if (leaders.firstKey().si<=0) {
			// The car-following model is undefined for this case, return 'inappropriate' acceleration. Whatever uses
			// the car-following model has to figure out what to do in this situation. E.g. limit deceleration to an
			// extent depending on the circumstances, or divert from a certain behavior. 
			return new Acceleration(Double.NEGATIVE_INFINITY, AccelerationUnit.SI);
		}
		// Forward to method with desired speed and headway predetermined by this car-following model.
		return followingAcceleration(gtu, speed, this.desiredSpeed(gtu, speedLimit, enforcement, maximumVehicleSpeed), 
				this.desiredHeadway(gtu, speed), leaders);
	}
	
	/**
	 * Multi-anticipative determination of car-following acceleration. The implementation should be able to deal with
	 * the current speed being higher than the desired speed. The tactical planner determines whether multi-anticipative 
	 * car-following is applied, including to how many leaders and within what distance, by including these vehicles in 
	 * the set. The car-following model itself may however only respond to the first vehicle.
	 * @param gtu GTU for which the acceleration is calculated.
	 * @param speed Current speed.
	 * @param desiredSpeed Desired speed.
	 * @param desiredHeadway Desired headway.
	 * @param leaders Set of leader headways (guaranteed positive) and speeds, ordered by headway (closest first).
     * @throws ParameterException If parameter exception occurs.
	 * @return Car-following acceleration.
	 */
	protected abstract Acceleration followingAcceleration(LaneBasedGTU gtu, Speed speed, Speed desiredSpeed, 
			Length.Rel desiredHeadway, SortedMap<Length.Rel, Speed> leaders) throws ParameterException;

	
	// TODO: Remove methods below
	
	@Override
	public AccelerationStep computeAccelerationStep(LaneBasedGTU gtu,
			Speed leaderSpeed, Rel headway, Rel maxDistance, Speed speedLimit)
			throws GTUException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AccelerationStep computeAccelerationStep(LaneBasedGTU gtu,
			Speed leaderSpeed, Rel headway, Rel maxDistance, Speed speedLimit,
			org.djunits.value.vdouble.scalar.Time.Rel stepSize)
			throws GTUException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Acceleration computeAcceleration(Speed followerSpeed,
			Speed followerMaximumSpeed, Speed leaderSpeed, Rel headway,
			Speed speedLimit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Acceleration computeAcceleration(Speed followerSpeed,
			Speed followerMaximumSpeed, Speed leaderSpeed, Rel headway,
			Speed speedLimit, org.djunits.value.vdouble.scalar.Time.Rel stepSize) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AccelerationStep computeAccelerationStep(Speed followerSpeed,
			Speed leaderSpeed, Rel headway, Speed speedLimit, Abs currentTime) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AccelerationStep computeAccelerationStep(Speed followerSpeed,
			Speed leaderSpeed, Rel headway, Speed speedLimit, Abs currentTime,
			org.djunits.value.vdouble.scalar.Time.Rel stepSize) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DualAccelerationStep computeDualAccelerationStep(LaneBasedGTU gtu,
			Collection<HeadwayGTU> otherGtuHeadways, Rel maxDistance,
			Speed speedLimit) throws GTUException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DualAccelerationStep computeDualAccelerationStep(LaneBasedGTU gtu,
			Collection<HeadwayGTU> otherGtuHeadways, Rel maxDistance,
			Speed speedLimit, org.djunits.value.vdouble.scalar.Time.Rel stepSize)
			throws GTUException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AccelerationStep computeAccelerationStepWithNoLeader(
			LaneBasedGTU gtu, Rel maxDistance, Speed speedLimit)
			throws GTUException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AccelerationStep computeAccelerationStepWithNoLeader(
			LaneBasedGTU gtu, Rel maxDistance, Speed speedLimit,
			org.djunits.value.vdouble.scalar.Time.Rel stepSize)
			throws GTUException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Rel minimumHeadway(Speed followerSpeed, Speed leaderSpeed,
			Rel precision, Rel maxDistance, Speed speedLimit,
			Speed followerMaximumSpeed) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Acceleration getMaximumSafeDeceleration() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public org.djunits.value.vdouble.scalar.Time.Rel getStepSize() {
		// TODO Auto-generated method stub
		return null;
	}

	
}
