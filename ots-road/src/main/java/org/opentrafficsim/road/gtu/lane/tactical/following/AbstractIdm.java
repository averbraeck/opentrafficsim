package org.opentrafficsim.road.gtu.lane.tactical.following;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeAcceleration;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.base.parameters.ParameterTypeDuration;
import org.opentrafficsim.base.parameters.ParameterTypeLength;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.base.parameters.constraint.ConstraintInterface;
import org.opentrafficsim.core.gtu.Stateless;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionIterable;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedObject;
import org.opentrafficsim.road.gtu.lane.tactical.util.SpeedLimitUtil;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

/**
 * Implementation of the IDM. See <a
 * href=https://en.wikipedia.org/wiki/Intelligent_driver_model>https://en.wikipedia.org/wiki/Intelligent_driver_model</a>
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
// @docs/06-behavior/parameters.md
public abstract class AbstractIdm extends AbstractCarFollowingModel
{

    /** Acceleration parameter type. */
    // @docs/06-behavior/parameters.md
    protected static final ParameterTypeAcceleration A = ParameterTypes.A;

    /** Comfortable deceleration parameter type. */
    protected static final ParameterTypeAcceleration B = ParameterTypes.B;

    /** Desired headway parameter type. */
    protected static final ParameterTypeDuration T = ParameterTypes.T;

    /** Stopping distance parameter type. */
    protected static final ParameterTypeLength S0 = ParameterTypes.S0;

    /** Adjustment deceleration parameter type. */
    protected static final ParameterTypeAcceleration B0 = ParameterTypes.B0;

    /** Speed limit adherence factor parameter type. */
    protected static final ParameterTypeDouble FSPEED = ParameterTypes.FSPEED;

    /** Acceleration flattening. */
    // @docs/06-behavior/parameters.md
    public static final ParameterTypeDouble DELTA = new ParameterTypeDouble("delta",
            "Acceleration flattening exponent towards desired speed", 4.0, ConstraintInterface.POSITIVE);

    /** Default IDM desired headway model. */
    public static final IdmDesiredHeadwayModel HEADWAY = IdmDesiredHeadwayModel.SINGLETON;

    /** Default IDM desired speed model. */
    public static final IdmDesiredSpeedModel DESIRED_SPEED = IdmDesiredSpeedModel.SINGLETON;

    /**
     * Constructor with modular models for desired headway and desired speed.
     * @param desiredHeadwayModel desired headway model
     * @param desiredSpeedModel desired speed model
     */
    public AbstractIdm(final DesiredHeadwayModel desiredHeadwayModel, final DesiredSpeedModel desiredSpeedModel)
    {
        super(desiredHeadwayModel, desiredSpeedModel);
    }

    /**
     * Determination of car-following acceleration, possibly based on multiple leaders. This implementation calculates the IDM
     * free term, which is returned if there are no leaders. If there are leaders <code>combineInteractionTerm()</code> is
     * invoked to combine the free term with some implementation specific interaction term. The IDM free term is limited by a
     * deceleration of <code>B0</code> for cases where the current speed is above the desired speed. This method can be
     * overridden if the free term needs to be redefined.
     * @param parameters Parameters.
     * @param speed Current speed.
     * @param desiredSpeed Desired speed.
     * @param desiredHeadway Desired headway.
     * @param leaders Set of leader headways (guaranteed positive) and speeds, ordered by headway (closest first).
     * @throws ParameterException If parameter exception occurs.
     * @return Car-following acceleration.
     */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    protected Acceleration followingAcceleration(final Parameters parameters, final Speed speed, final Speed desiredSpeed,
            final Length desiredHeadway, final PerceptionIterable<? extends PerceivedObject> leaders) throws ParameterException
    {
        Acceleration a = parameters.getParameter(A);
        Acceleration b0 = parameters.getParameter(B0);
        double delta = parameters.getParameter(DELTA);
        double aFree = a.si * (1 - Math.pow(speed.si / desiredSpeed.si, delta));
        // limit deceleration in free term (occurs if speed > desired speed)
        aFree = aFree > -b0.si ? aFree : -b0.si;
        // return free term if there are no leaders
        if (leaders.isEmpty())
        {
            return Acceleration.instantiateSI(aFree);
        }
        // return combined acceleration
        return combineInteractionTerm(Acceleration.instantiateSI(aFree), parameters, speed, desiredSpeed, desiredHeadway,
                leaders);
    }

    /**
     * Combines an interaction term with the free term. There should be at least 1 leader for this method.
     * @param aFree Free term of acceleration.
     * @param parameters Parameters.
     * @param speed Current speed.
     * @param desiredSpeed Desired speed.
     * @param desiredHeadway Desired headway.
     * @param leaders Set of leader headways (guaranteed positive) and speeds, ordered by headway (closest first).
     * @return Combination of terms into a single acceleration.
     * @throws ParameterException In case of parameter exception.
     */
    protected abstract Acceleration combineInteractionTerm(Acceleration aFree, Parameters parameters, Speed speed,
            Speed desiredSpeed, Length desiredHeadway, PerceptionIterable<? extends PerceivedObject> leaders) throws ParameterException;

    /**
     * Determines the dynamic desired headway, which is non-negative.
     * @param parameters Parameters.
     * @param speed Current speed.
     * @param desiredHeadway Desired headway.
     * @param leaderSpeed Speed of the leading vehicle.
     * @return Dynamic desired headway.
     * @throws ParameterException In case of parameter exception.
     */
    protected final Length dynamicDesiredHeadway(final Parameters parameters, final Speed speed, final Length desiredHeadway,
            final Speed leaderSpeed) throws ParameterException
    {
        double sStar = desiredHeadway.si + dynamicHeadwayTerm(parameters, speed, leaderSpeed).si;
        /*
         * Due to a power of 2 in the IDM, negative values of sStar are not allowed. A negative sStar means that the leader is
         * faster to such an extent, that the equilibrium headway (s0+vT) is completely compensated by the dynamic part in
         * sStar. This might occur if a much faster leader changes lane closely in front. The compensation is limited to the
         * equilibrium headway minus the stopping distance (i.e. sStar > s0), which means the driver wants to follow with
         * acceleration. Note that usually the free term determines acceleration in such cases.
         */
        Length s0 = parameters.getParameter(S0);
        /*
         * Limit used to be 0, but the IDM is very sensitive there. With a decelerating leader, an ok acceleration in one time
         * step, may results in acceleration < -10 in the next.
         */
        return Length.instantiateSI(sStar >= s0.si ? sStar : s0.si);
    }

    /**
     * Determines the dynamic headway term. May be used on individual leaders for multi-anticipative following.
     * @param parameters Parameters.
     * @param speed Current speed.
     * @param leaderSpeed Speed of the leading vehicle.
     * @return Dynamic headway term.
     * @throws ParameterException In case of parameter exception.
     */
    protected final Length dynamicHeadwayTerm(final Parameters parameters, final Speed speed, final Speed leaderSpeed)
            throws ParameterException
    {
        Acceleration a = parameters.getParameter(A);
        Acceleration b = parameters.getParameter(B);
        return Length.instantiateSI(speed.si * (speed.si - leaderSpeed.si) / (2 * Math.sqrt(a.si * b.si)));
    }

    /**
     * IDM desired headway model.
     */
    public static class IdmDesiredHeadwayModel implements DesiredHeadwayModel, Stateless<IdmDesiredHeadwayModel>
    {
        /** Singleton instance. */
        public static final IdmDesiredHeadwayModel SINGLETON = new IdmDesiredHeadwayModel();

        @Override
        public IdmDesiredHeadwayModel get()
        {
            return SINGLETON;
        }

        @Override
        public Length desiredHeadway(final Parameters parameters, final Speed speed) throws ParameterException
        {
            return Length.instantiateSI(parameters.getParameter(S0).si + speed.si * parameters.getParameter(T).si);
        }
    }

    /**
     * IDM desired speed model.
     */
    public static class IdmDesiredSpeedModel implements DesiredSpeedModel, Stateless<IdmDesiredSpeedModel>
    {
        /** Singleton instance. */
        public static final IdmDesiredSpeedModel SINGLETON = new IdmDesiredSpeedModel();

        @Override
        public IdmDesiredSpeedModel get()
        {
            return SINGLETON;
        }

        @Override
        public Speed desiredSpeed(final Parameters parameters, final SpeedLimitInfo speedInfo) throws ParameterException
        {
            Speed consideredSpeed = SpeedLimitUtil.getLegalSpeedLimit(speedInfo).times(parameters.getParameter(FSPEED));
            Speed maxVehicleSpeed = SpeedLimitUtil.getMaximumVehicleSpeed(speedInfo);
            return consideredSpeed.le(maxVehicleSpeed) ? consideredSpeed : maxVehicleSpeed;
        }
    }

}
