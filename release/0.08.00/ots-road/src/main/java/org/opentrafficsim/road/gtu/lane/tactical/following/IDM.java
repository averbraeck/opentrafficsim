package org.opentrafficsim.road.gtu.lane.tactical.following;

import java.util.SortedMap;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Length.Rel;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.drivercharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.drivercharacteristics.ParameterTypeDouble;
import org.opentrafficsim.core.gtu.drivercharacteristics.ParameterTypes;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;

/**
 * Implementation of the IDM.
 * See <a href=https://en.wikipedia.org/wiki/Intelligent_driver_model>https://en.wikipedia.org/wiki/Intelligent_driver_model</a>
 * @author Wouter Schakel
 */
public class IDM extends AbstractCarFollowingModel {

	/** Speed limit adherence factor. */
    public static final ParameterTypeDouble DELTA = new ParameterTypeDouble("delta", 
    		"Acceleration flattening exponent towards desired speed.", 4.0) {
    	public void check(double value) throws ParameterException {
    		ParameterException.failIf(value<=0, "Parameter of type delta may not have a negative or zero value.");
    	}
    };
	
	/** {@inheritDoc} */
	public Speed desiredSpeed(LaneBasedGTU gtu, Speed speedLimit, boolean enforcement, Speed maximumVehicleSpeed) 
			throws ParameterException {
		if (!enforcement) {
			speedLimit = speedLimit.multiplyBy(gtu.getBehavioralCharacteristics().getParameter(ParameterTypes.FSPEED));
		}
		return speedLimit.le(maximumVehicleSpeed) ? speedLimit : maximumVehicleSpeed;
	}

	/** {@inheritDoc} */
	public Rel desiredHeadway(LaneBasedGTU gtu, Speed speed) throws ParameterException {
		return gtu.getBehavioralCharacteristics().getLengthParameter(ParameterTypes.S0).plus(
				speed.multiplyBy(gtu.getBehavioralCharacteristics().getTimeParameter(ParameterTypes.T)));
	}

	/** {@inheritDoc} */
	public String getName() {
		return "IDM";
	}

	/** {@inheritDoc} */
	public String getLongName() {
		return "Intelligent Driver Model";
	}

	/** {@inheritDoc} */
	protected Acceleration followingAcceleration(LaneBasedGTU gtu, Speed speed, Speed desiredSpeed, Rel desiredHeadway,
			SortedMap<Rel, Speed> leaders) throws ParameterException {
		Acceleration a = gtu.getBehavioralCharacteristics().getAccelerationParameter(ParameterTypes.A);
		double delta = gtu.getBehavioralCharacteristics().getParameter(DELTA);
		double sStar = dynamicDesiredHeadway(gtu, speed, desiredHeadway, leaders.get(leaders.firstKey())).si;
		return new Acceleration(a.si * (1-Math.pow(speed.si/desiredSpeed.si, delta)-
				(sStar/leaders.firstKey().si)*(sStar/leaders.firstKey().si)), AccelerationUnit.SI);
	}
	
	/**
	 * Determines the dynamic desired headway, which is non-negative.
	 * @param gtu GTU for which the dynamic desired headway is calculated.
	 * @param speed Current speed.
	 * @param desiredHeadway Desired speed.
	 * @param leaderSpeed Speed of the leading vehicle.
	 * @return Dynamic desired headway.
	 * @throws ParameterException x
	 */
	protected Length.Rel dynamicDesiredHeadway(LaneBasedGTU gtu, Speed speed, Rel desiredHeadway, Speed leaderSpeed) 
			throws ParameterException {
		double sStar = desiredHeadway.si + dynamicHeadwayTerm(gtu, speed, leaderSpeed).si;
		/*
		 * Due to a power of 2 in the IDM, negative values of sStar are not allowed. A negative sStar means that the 
		 * leader is faster to such an extent, that the equilibrium headway (s0+vT) is completely compensated by the 
		 * dynamic part in sStar. This might occur if a much faster leader changes lane closely in front. The 
		 * compensation is limited to the equilibrium headway (i.e. sStar = 0), which means the driver wants to follow 
		 * with acceleration. Note that usually the free term determines acceleration in such cases.
		 */
		return new Length.Rel(sStar>=0 ? sStar : 0, LengthUnit.SI);
	}
	
	/**
	 * Determines the dynamic headway term. May be used on individual leaders for multi-anticipative following.
	 * @param gtu GTU for which the dynamic desired headway term is calculated.
	 * @param speed Current speed.
	 * @param leaderSpeed Speed of the leading vehicle.
	 * @return Dynamic headway term.
	 * @throws ParameterException  x
	 */
	protected Length.Rel dynamicHeadwayTerm(LaneBasedGTU gtu, Speed speed, Speed leaderSpeed) throws ParameterException {
		Acceleration a = gtu.getBehavioralCharacteristics().getAccelerationParameter(ParameterTypes.A);
		Acceleration b = gtu.getBehavioralCharacteristics().getAccelerationParameter(ParameterTypes.B);
		return new Length.Rel(speed.si*(speed.si-leaderSpeed.si) / (2*Math.sqrt(a.si + b.si)), LengthUnit.SI);
	}

}