package org.opentrafficsim.road.gtu.tactical.util.lmrs;

import java.util.Optional;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.NamedConstants;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.road.gtu.perception.object.PerceivedGtu;
import org.opentrafficsim.road.gtu.tactical.following.AbstractIdm;

/**
 * Relaxation is a common sub-component in the logic of synchronization, cooperation and gap-acceptance. It also contains the
 * subsequent relaxation of gaps. This pertains to the following calls:
 * <ul>
 * <li>{@link Relaxation#setRelaxation} and {@link Relaxation#resetRelaxation} are invoked by various components to asses a
 * prospective situation in an adjacent lane, accounting for some level of relaxation applicable to the situation.</li>
 * <li>{@link Relaxation#isSafeGap} may be invoked by some gap-acceptance models. Not all implementations of {@link Relaxation}
 * may support this. The right combination of gap-acceptance and relaxation is required.</li>
 * </ul>
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.<br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * @author Wouter Schakel
 */
public interface Relaxation extends NamedConstants
{

    /** Parameter key for T. This value might e.g. be set by relaxation, its initialization, and tailgating. */
    Object T_KEY = new Object()
    {
        @Override
        public String toString()
        {
            return "T_KEY";
        }
    };

    /** Headway relaxation using T between Tmin and Tmax. */
    Relaxation LMRS = new HeadwayRelaxation()
    {
        @Override
        Duration interpolateHeadway(final Parameters params, final double relaxation) throws ParameterException
        {
            double tMin = params.getParameter(ParameterTypes.TMIN).si;
            double tMax = params.getParameter(ParameterTypes.TMAX).si;
            return Duration.ofSI(relaxation * tMin + (1.0 - relaxation) * tMax);
        }

        @Override
        public String name()
        {
            return "LMRS";
        }
    };

    /** Headway relaxation using T from zero until Tmax. */
    Relaxation STATIC = new HeadwayRelaxation()
    {
        @Override
        Duration interpolateHeadway(final Parameters params, final double relaxation) throws ParameterException
        {
            return Duration.ofSI((1.0 - relaxation) * params.getParameter(ParameterTypes.TMAX).si);
        }

        @Override
        public String name()
        {
            return "STATIC";
        }
    };

    /** Relaxation based on T from zero until Tmax and KAPPA regarding speed difference. */
    Relaxation DYNAMIC = new Relaxation()
    {

        /** Parameter key for KAPPA. */
        public static final Object KAPPA_KEY = new Object()
        {
            @Override
            public String toString()
            {
                return "Relaxation.DYNAMIC.KAPPA_KEY";
            }
        };

        @Override
        public void setRelaxation(final Parameters params, final double relaxation) throws ParameterException
        {
            double urgency = Math.max(0.0, Math.min(relaxation, 1.0));
            double tRelaxed = (1.0 - urgency) * params.getParameter(ParameterTypes.TMAX).si;
            params.setParameterOverride(ParameterTypes.T, Duration.ofSI(tRelaxed));
        }

        @Override
        public void resetRelaxation(final Parameters params) throws ParameterException
        {
            params.removeParameterOverride(ParameterTypes.T);
        }

        @Override
        public void initRelaxation(final Parameters params, final Speed egoSpeed, final PerceivedGtu leader,
                final RelaxationTrigger trigger) throws ParameterException
        {
            double k = requiredRelaxationLevel(params, egoSpeed, leader.getDistance().si, leader.getSpeed());
            k = Math.max(0.0, Math.min(k, 1.0));
            double tMax = params.getParameter(ParameterTypes.TMAX).si;
            params.setClaimedParameter(AbstractIdm.KAPPA, k, KAPPA_KEY);
            params.setClaimedParameter(ParameterTypes.T, Duration.ofSI((1 - k) * tMax), T_KEY);
        }

        /**
         * Returns the level of relaxation required to follow an object at given distance and speed. This is analyzed purely in
         * the distance domain, though including a dynamic component based on speed difference. This is analogous to the dynamic
         * desired headway {@code s*} from the IDM. If we set {@code k = [0 1]} as the level of relaxation, this method returns:
         * <p>
         * {@code (s*(k=0)-s) / (s*(k=0) - s*(k=1))}
         * <p>
         * Or in words "desired gap deficit" / "available gap reduction from relaxation". Here, {@code s} is the current
         * distance to an object. {@code s*(k)} is given by:
         * <p>
         * {@code s*(k) = s0 + max(0, (1-k)*v*T + (dv * ((1-k)*v + k*abs(dv)) / (2*sqrt(a*b))))}
         * <p>
         * In this equation {@code k} both reduces the static part of the desired headway ({@code v*T}), while also reducing the
         * sensitivity to the speed difference from {@code v} to {@code abs(dv)}. It thus simplifies to:
         * <p>
         * {@code s*(k=0) = s0 + max(0, v*T + (dv * v) / (2*sqrt(a*b)))}<br>
         * {@code s*(k=1) = s0 + max(0, (dv * abs(dv)) / (2*sqrt(a*b)))}
         * <p>
         * The first is the regular IDM dynamic desired headway, while the latter reflects constant deceleration collision
         * avoidance. The returned level is not bounded to the range [0 1] such that callers can use this information regarding
         * the suitability to interact with the object. The actual level of relaxation applied should be bound to [0 1].
         * @param params parameters
         * @param egoSpeed ego speed
         * @param distance object distance
         * @param leaderSpeed object speed
         * @return unbounded (can be &lt;0 or &gt;1) level of relaxation required to follow an object
         * @throws ParameterException in case of a missing parameter
         */
        private double requiredRelaxationLevel(final Parameters params, final Speed egoSpeed, final double distance,
                final Speed leaderSpeed) throws ParameterException
        {
            double s0 = params.getParameter(ParameterTypes.S0).si;
            double tMax = params.getParameter(ParameterTypes.TMAX).si;
            double a = params.getParameter(ParameterTypes.A).si;
            double b = params.getParameter(ParameterTypes.B).si;
            double v = egoSpeed.si;
            double dv = v - leaderSpeed.si;

            double sqrtab2 = 2.0 * Math.sqrt(a * b);
            double sk0 = Math.max(0.0, v * tMax + v * dv / sqrtab2); // excluding s0, which does not matter for the difference
            double sk1 = Math.max(0.0, Math.abs(dv) * dv / sqrtab2);
            return (s0 + sk0 - distance) / (sk0 - sk1);
        }

        @Override
        public void relax(final Parameters params) throws ParameterException
        {
            double ratio = params.getParameter(ParameterTypes.DT).si / params.getParameter(ParameterTypes.TAU).si;
            ratio = Math.min(ratio, 1.0);
            double ratio1 = 1.0 - ratio;

            double k = params.getParameter(AbstractIdm.KAPPA);
            params.setClaimedParameter(AbstractIdm.KAPPA, ratio1 * k, KAPPA_KEY);

            double t = params.getParameter(ParameterTypes.T).si;
            double tMax = params.getParameter(ParameterTypes.TMAX).si;
            params.setClaimedParameter(ParameterTypes.T, Duration.ofSI(ratio1 * t + ratio * tMax), T_KEY);
        }

        @Override
        public Optional<Double> requiredRelaxationLevel(final Parameters params, final Speed egoSpeed,
                final PerceivedGtu leader) throws ParameterException
        {
            // add s0 + l to achieve a logic based on "ego nose reasonably behind leader nose"
            double s = leader.getDistance().si + leader.getLength().si + params.getParameter(ParameterTypes.S0).si;
            return Optional.of(requiredRelaxationLevel(params, egoSpeed, s, leader.getSpeed()));
        }

        @Override
        public boolean isSafeGap(final Parameters params, final Speed followerSpeed, final Length leaderDistance,
                final Speed leaderSpeed, final double relaxation) throws ParameterException
        {
            double s0 = params.getParameter(ParameterTypes.S0).si;
            double t = params.getParameter(ParameterTypes.TMAX).si;
            double b = params.getParameter(ParameterTypes.B).si;
            double k = relaxation;
            double k1 = 1.0 - relaxation;
            double v = followerSpeed.si;
            double dv = v - leaderSpeed.si;
            return s0 + Math.max(0.0, k1 * v * t + dv * (k1 * v + k * Math.abs(dv)) / (2 * b)) <= leaderDistance.si;
        }

        @Override
        public String name()
        {
            return "DYNAMIC";
        }

    };

    /**
     * Sets level of relaxation in to parameters such that a gap can be assessed. Implementation are expected to use
     * {@link Parameters#setParameterOverride}.
     * @param params parameters
     * @param relaxation level of relaxation in the range [0 1]
     * @throws ParameterException when a parameter is missing or out of bounds
     */
    void setRelaxation(Parameters params, double relaxation) throws ParameterException;

    /**
     * Reset parameter changes as performed by {@link #setRelaxation}. Implementation are expected to use
     * {@link Parameters#removeParameterOverride}.
     * @param params parameters
     * @throws ParameterException when a parameter is missing or out of bounds
     */
    void resetRelaxation(Parameters params) throws ParameterException;

    /**
     * Initializes the headway relaxation upon having a new leading vehicle.
     * @param params parameters
     * @param egoSpeed ego speed
     * @param leader new leading vehicle
     * @param trigger trigger
     * @throws ParameterException when a parameter is missing or out of bounds
     */
    void initRelaxation(Parameters params, Speed egoSpeed, PerceivedGtu leader, RelaxationTrigger trigger)
            throws ParameterException;

    /**
     * Performs a step in the relaxation process.
     * @param params parameters
     * @throws ParameterException when a parameter is missing or out of bounds
     */
    void relax(Parameters params) throws ParameterException;

    /**
     * Returns the level of required relaxation in order to synchronize with a vehicle in the adjacent lane. This applies to
     * both synchronization and cooperation. The returned value may be larger than 1 to indicate excessive situations. Values
     * below 0 indicate no required relaxation at all.
     * @param params parameters
     * @param egoSpeed ego speed
     * @param leader leading vehicle in adjacent lane
     * @return whether the vehicle in the adjacent lane is accepted for synchronization or cooperation, empty if implementation
     *         has no logic for it
     * @throws ParameterException when a parameter is missing or out of bounds
     */
    Optional<Double> requiredRelaxationLevel(Parameters params, Speed egoSpeed, PerceivedGtu leader) throws ParameterException;

    /**
     * Returns whether the gap expressed in leader-follower distance and speed is accepted. This either involves a prospective
     * follower relative to the ego vehicle, or the ego vehicle relative to the prospective leader.
     * @param params parameters
     * @param followerSpeed follower speed
     * @param leaderDistance distance to leader
     * @param leaderSpeed leader speed
     * @param relaxation level of relaxation in the following dynamics
     * @return whether the gap expressed in leader-follower distance and speed is accepted
     * @throws ParameterException when a parameter is missing or out of bounds
     */
    boolean isSafeGap(Parameters params, Speed followerSpeed, Length leaderDistance, Speed leaderSpeed, double relaxation)
            throws ParameterException;

    /**
     * Relaxation trigger.
     */
    enum RelaxationTrigger
    {
        /** Ego lane change. */
        EGO_LANE_CHANGE,

        /** New leader. */
        NEW_LEADER
    }

    /**
     * Implementation using only the desired headway parameters using a supplied logic to determine this.
     */
    abstract class HeadwayRelaxation implements Relaxation
    {

        /**
         * Interpolate headway.
         * @param params parameters
         * @param relaxation level of relaxation in the range [0 1]
         * @return interpolated headway
         * @throws ParameterException when a parameter is missing or out of bounds
         */
        abstract Duration interpolateHeadway(Parameters params, double relaxation) throws ParameterException;

        @Override
        public void setRelaxation(final Parameters params, final double relaxation) throws ParameterException
        {
            params.setParameterOverride(ParameterTypes.T, interpolateHeadway(params, Math.max(0.0, Math.min(relaxation, 1.0))));
        }

        @Override
        public void resetRelaxation(final Parameters params) throws ParameterException
        {
            params.removeParameterOverride(ParameterTypes.T);
        }

        @Override
        public void initRelaxation(final Parameters params, final Speed egoSpeed, final PerceivedGtu leader,
                final RelaxationTrigger trigger) throws ParameterException
        {
            double relaxation = switch (trigger)
            {
                case EGO_LANE_CHANGE -> params.getParameter(LmrsParameters.DLC);
                case NEW_LEADER -> leader.getParameters().getParameter(LmrsParameters.DLC);
            };
            params.setClaimedParameter(ParameterTypes.T, interpolateHeadway(params, Math.max(0.0, Math.min(relaxation, 1.0))),
                    T_KEY);
        }

        @Override
        public void relax(final Parameters params) throws ParameterException
        {
            double ratio = params.getParameter(ParameterTypes.DT).si / params.getParameter(ParameterTypes.TAU).si;
            ratio = Math.min(ratio, 1.0);
            double t = params.getParameter(ParameterTypes.T).si;
            double tMax = params.getParameter(ParameterTypes.TMAX).si;
            if (t != tMax)
            {
                params.setClaimedParameter(ParameterTypes.T, Duration.ofSI((1.0 - ratio) * t + ratio * tMax), T_KEY);
            }
        }

        @Override
        public Optional<Double> requiredRelaxationLevel(final Parameters params, final Speed egoSpeed,
                final PerceivedGtu leader) throws ParameterException
        {
            return Optional.empty();
        }

        @Override
        public boolean isSafeGap(final Parameters params, final Speed followerSpeed, final Length leaderDistance,
                final Speed leaderSpeed, final double relaxation) throws ParameterException
        {
            throw new UnsupportedOperationException("Relaxation implementation cannot determine the safety of a gap directly. "
                    + "Use different relaxation or gap acceptance model.");
        }

    }

}
