package org.opentrafficsim.road.gtu.lane.tactical.following;

import static org.opentrafficsim.core.gtu.behavioralcharacteristics.AbstractParameterType.Check.POSITIVE;

import java.util.SortedMap;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypeDouble;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypes;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

/**
 * Implementation of the IDM. See <a
 * href=https://en.wikipedia.org/wiki/Intelligent_driver_model>https://en.wikipedia.org/wiki/Intelligent_driver_model</a>
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 22, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public abstract class AbstractIDM extends AbstractCarFollowingModel
{

    /** Speed limit adherence factor. */
    public static final ParameterTypeDouble DELTA = new ParameterTypeDouble("delta",
        "Acceleration flattening exponent towards desired speed.", 4.0, POSITIVE);

    /** {@inheritDoc} */
    @Override
    public final Speed desiredSpeed(final BehavioralCharacteristics behavioralCharacteristics, final SpeedLimitInfo speedInfo)
        throws ParameterException
    {
        Speed consideredSpeed = speedInfo.getLegalSpeedLimit();
        if (!speedInfo.isEnforced())
        {
            consideredSpeed = consideredSpeed.multiplyBy(behavioralCharacteristics.getParameter(ParameterTypes.FSPEED));
        }
        return consideredSpeed.le(speedInfo.getMaximumVehicleSpeed()) ? consideredSpeed : speedInfo.getMaximumVehicleSpeed();
    }

    /** {@inheritDoc} */
    @Override
    public final Length desiredHeadway(final BehavioralCharacteristics behavioralCharacteristics, final Speed speed)
        throws ParameterException
    {
        return behavioralCharacteristics.getParameter(ParameterTypes.S0).plus(
            speed.multiplyBy(behavioralCharacteristics.getParameter(ParameterTypes.T)));
    }

    /**
     * Determination of car-following acceleration, possibly based on multiple leaders. This implementation calculates the IDM
     * free term, which is returned if there are no leaders. If there are leaders <tt>combineInteractionTerm()</tt> is invoked
     * to combine the free term with some implementation specific interaction term. The IDM free term is limited by a 
     * deceleration of <tt>B0</tt> for cases where the current speed is above the desired speed. This method can be overridden 
     * if the free term needs to be redefined. 
     * @param behavioralCharacteristics Behavioral characteristics.
     * @param speed Current speed.
     * @param desiredSpeed Desired speed.
     * @param desiredHeadway Desired headway.
     * @param leaders Set of leader headways (guaranteed positive) and speeds, ordered by headway (closest first).
     * @throws ParameterException If parameter exception occurs.
     * @return Car-following acceleration.
     */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    protected Acceleration followingAcceleration(final BehavioralCharacteristics behavioralCharacteristics,
        final Speed speed, final Speed desiredSpeed, final Length desiredHeadway, final SortedMap<Length, Speed> leaders)
        throws ParameterException
    {
        Acceleration a = behavioralCharacteristics.getParameter(ParameterTypes.A);
        Acceleration b0 = behavioralCharacteristics.getParameter(ParameterTypes.B0);
        double delta = behavioralCharacteristics.getParameter(DELTA);
        double aFree = a.si * (1 - Math.pow(speed.si / desiredSpeed.si, delta));
        // limit deceleration in free term (occurs if speed > desired speed)
        aFree = aFree > -b0.si ? aFree : -b0.si;
        // return free term if there are no leaders
        if (leaders.isEmpty())
        {
            return new Acceleration(aFree, AccelerationUnit.SI);
        }
        // return combined acceleration
        return combineInteractionTerm(new Acceleration(aFree, AccelerationUnit.SI), behavioralCharacteristics, speed,
            desiredSpeed, desiredHeadway, leaders);
    }

    /**
     * Combines an interaction term with the free term. There should be at least 1 leader for this method.
     * @param aFree Free term of acceleration.
     * @param behavioralCharacteristics Behavioral characteristics.
     * @param speed Current speed.
     * @param desiredSpeed Desired speed.
     * @param desiredHeadway Desired headway.
     * @param leaders Set of leader headways (guaranteed positive) and speeds, ordered by headway (closest first).
     * @return Combination of terms into a single acceleration.
     * @throws ParameterException In case of parameter exception.
     */
    protected abstract Acceleration combineInteractionTerm(Acceleration aFree,
        BehavioralCharacteristics behavioralCharacteristics, Speed speed, Speed desiredSpeed, Length desiredHeadway,
        SortedMap<Length, Speed> leaders) throws ParameterException;

    /**
     * Determines the dynamic desired headway, which is non-negative.
     * @param behavioralCharacteristics Behavioral characteristics.
     * @param speed Current speed.
     * @param desiredHeadway Desired headway.
     * @param leaderSpeed Speed of the leading vehicle.
     * @return Dynamic desired headway.
     * @throws ParameterException In case of parameter exception.
     */
    protected final Length dynamicDesiredHeadway(final BehavioralCharacteristics behavioralCharacteristics,
        final Speed speed, final Length desiredHeadway, final Speed leaderSpeed) throws ParameterException
    {
        double sStar = desiredHeadway.si + dynamicHeadwayTerm(behavioralCharacteristics, speed, leaderSpeed).si;
        /*
         * Due to a power of 2 in the IDM, negative values of sStar are not allowed. A negative sStar means that the leader is
         * faster to such an extent, that the equilibrium headway (s0+vT) is completely compensated by the dynamic part in
         * sStar. This might occur if a much faster leader changes lane closely in front. The compensation is limited to the
         * equilibrium headway (i.e. sStar = 0), which means the driver wants to follow with acceleration. Note that usually the
         * free term determines acceleration in such cases.
         */
        return new Length(sStar >= 0 ? sStar : 0, LengthUnit.SI);
    }

    /**
     * Determines the dynamic headway term. May be used on individual leaders for multi-anticipative following.
     * @param behavioralCharacteristics Behavioral characteristics.
     * @param speed Current speed.
     * @param leaderSpeed Speed of the leading vehicle.
     * @return Dynamic headway term.
     * @throws ParameterException In case of parameter exception.
     */
    protected final Length dynamicHeadwayTerm(final BehavioralCharacteristics behavioralCharacteristics, final Speed speed,
        final Speed leaderSpeed) throws ParameterException
    {
        Acceleration a = behavioralCharacteristics.getParameter(ParameterTypes.A);
        Acceleration b = behavioralCharacteristics.getParameter(ParameterTypes.B);
        return new Length(speed.si * (speed.si - leaderSpeed.si) / (2 * Math.sqrt(a.si + b.si)), LengthUnit.SI);
    }

}
