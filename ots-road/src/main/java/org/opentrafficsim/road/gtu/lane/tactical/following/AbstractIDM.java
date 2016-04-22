package org.opentrafficsim.road.gtu.lane.tactical.following;

import static org.opentrafficsim.core.gtu.behavioralcharacteristics.AbstractParameterType.Check.POSITIVE;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Length.Rel;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypeDouble;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypes;

/**
 * Implementation of the IDM. See <a
 * href=https://en.wikipedia.org/wiki/Intelligent_driver_model>https://en.wikipedia.org/wiki/Intelligent_driver_model</a>
 * @author Wouter Schakel
 */
public abstract class AbstractIDM extends AbstractCarFollowingModel
{

    /** Speed limit adherence factor. */
    public static final ParameterTypeDouble DELTA = new ParameterTypeDouble("delta",
        "Acceleration flattening exponent towards desired speed.", 4.0, POSITIVE);

    /** {@inheritDoc} */
    public final Speed desiredSpeed(final BehavioralCharacteristics behavioralCharacteristics, final SpeedInfo speedInfo)
        throws ParameterException
    {
        Speed consideredSpeed = speedInfo.getSpeedLimit();
        if (!speedInfo.isEnforcement())
        {
            consideredSpeed = consideredSpeed.multiplyBy(behavioralCharacteristics.getParameter(ParameterTypes.FSPEED));
        }
        return consideredSpeed.le(speedInfo.getMaximumVehicleSpeed()) ? consideredSpeed : speedInfo.getMaximumVehicleSpeed();
    }

    /** {@inheritDoc} */
    public final Rel desiredHeadway(final BehavioralCharacteristics behavioralCharacteristics, final Speed speed)
        throws ParameterException
    {
        return behavioralCharacteristics.getParameter(ParameterTypes.S0).plus(
            speed.multiplyBy(behavioralCharacteristics.getParameter(ParameterTypes.T)));
    }

    /**
     * Determines the dynamic desired headway, which is non-negative.
     * @param behavioralCharacteristics Behavioral characteristics.
     * @param speed Current speed.
     * @param desiredHeadway Desired speed.
     * @param leaderSpeed Speed of the leading vehicle.
     * @return Dynamic desired headway.
     * @throws ParameterException In case of parameter exception.
     */
    protected final Length.Rel dynamicDesiredHeadway(final BehavioralCharacteristics behavioralCharacteristics,
        final Speed speed, final Rel desiredHeadway, final Speed leaderSpeed) throws ParameterException
    {
        double sStar = desiredHeadway.si + dynamicHeadwayTerm(behavioralCharacteristics, speed, leaderSpeed).si;
        /*
         * Due to a power of 2 in the IDM, negative values of sStar are not allowed. A negative sStar means that the leader is
         * faster to such an extent, that the equilibrium headway (s0+vT) is completely compensated by the dynamic part in
         * sStar. This might occur if a much faster leader changes lane closely in front. The compensation is limited to the
         * equilibrium headway (i.e. sStar = 0), which means the driver wants to follow with acceleration. Note that usually the
         * free term determines acceleration in such cases.
         */
        return new Length.Rel(sStar >= 0 ? sStar : 0, LengthUnit.SI);
    }

    /**
     * Determines the dynamic headway term. May be used on individual leaders for multi-anticipative following.
     * @param behavioralCharacteristics Behavioral characteristics.
     * @param speed Current speed.
     * @param leaderSpeed Speed of the leading vehicle.
     * @return Dynamic headway term.
     * @throws ParameterException In case of parameter exception.
     */
    protected final Length.Rel dynamicHeadwayTerm(final BehavioralCharacteristics behavioralCharacteristics,
        final Speed speed, final Speed leaderSpeed) throws ParameterException
    {
        Acceleration a = behavioralCharacteristics.getParameter(ParameterTypes.A);
        Acceleration b = behavioralCharacteristics.getParameter(ParameterTypes.B);
        return new Length.Rel(speed.si * (speed.si - leaderSpeed.si) / (2 * Math.sqrt(a.si + b.si)), LengthUnit.SI);
    }

}
