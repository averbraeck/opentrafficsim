package org.opentrafficsim.road.gtu.lane.tactical.mirova.following;

import java.io.Serializable;

import org.djunits.unit.AccelerationUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionIterable;
import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;
import org.opentrafficsim.road.gtu.lane.tactical.following.AbstractIdm;
import org.opentrafficsim.road.gtu.lane.tactical.following.DesiredHeadwayModel;
import org.opentrafficsim.road.gtu.lane.tactical.following.DesiredSpeedModel;
import org.opentrafficsim.road.gtu.lane.tactical.following.IdmPlus;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.MirovaParameters;

/**
 * Custom implementation of the IDM+ for the MiRoVA framework supporting dynamic headways.
 * <p>
 * This class extends the standard IDM+ and implements the {@link DynamicHeadwayProvider} interface. It exposes dynamic headway
 * calculations safely to the cognitive layers and intercepts the interaction term to filter out unrealistic deceleration spikes
 * (e.g., during cut-ins). It applies a stateless kinematic bounding: if the raw IDM+ deceleration exceeds comfortable limits,
 * it caps the deceleration at {@code BCRIT} unless physical kinematics strictly demand a hard emergency braking
 * ({@code B_MAX}).
 * </p>
 * <p>
 * Copyright (c) 2026 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public class MirovaIdmPlus extends AbstractIdm implements DynamicHeadwayProvider, Serializable
{
    /** Serial version UID for serialization. */
    private static final long serialVersionUID = 20260430L;

    /**
     * Default constructor using default models for desired headway and desired speed.
     */
    public MirovaIdmPlus()
    {
        super(HEADWAY, DESIRED_SPEED);
    }

    /**
     * Constructor with modular models for desired headway and desired speed.
     * @param desiredHeadwayModel DesiredHeadwayModel; the desired headway model to use.
     * @param desiredSpeedModel DesiredSpeedModel; the desired speed model to use.
     */
    public MirovaIdmPlus(final DesiredHeadwayModel desiredHeadwayModel, final DesiredSpeedModel desiredSpeedModel)
    {
        super(desiredHeadwayModel, desiredSpeedModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getName()
    {
        return "MiRoVA-IDM+";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getLongName()
    {
        return "MiRoVA Intelligent Driver Model+ with Kinematic Bounding";
    }

    /**
     * {@inheritDoc}
     * <p>
     * Safely wraps the protected method from {@link AbstractIdm} to make it available to the MiRoVA cognitive layers via the
     * {@link DynamicHeadwayProvider} interface.
     * </p>
     */
    @Override
    public Length calculateDynamicDesiredHeadway(final Parameters parameters, final Speed speed, final Length desiredHeadway,
            final Speed leaderSpeed) throws ParameterException
    {
        return super.dynamicDesiredHeadway(parameters, speed, desiredHeadway, leaderSpeed);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Obtains the raw IDM+ interaction acceleration and applies stateless kinematic bounding. If the IDM+ demands a
     * deceleration stronger than {@code BCRIT}, the physically required deceleration is calculated. If the physics tolerate it,
     * the deceleration is capped at {@code BCRIT}. Otherwise, emergency braking at {@code B_MAX} is applied.
     * </p>
     * @param aFree Acceleration; the acceleration calculated for free-flow conditions.
     * @param parameters Parameters; the parameter set of the GTU.
     * @param speed Speed; the current speed of the ego GTU.
     * @param desiredSpeed Speed; the desired speed of the ego GTU.
     * @param desiredHeadway Length; the static desired headway.
     * @param leaders PerceptionIterable&lt;? extends Headway&gt;; the perceived leading vehicles.
     * @return Acceleration; the bounded interaction acceleration.
     * @throws ParameterException if a required parameter is missing.
     */
    @Override
    protected final Acceleration combineInteractionTerm(final Acceleration aFree, final Parameters parameters,
            final Speed speed, final Speed desiredSpeed, final Length desiredHeadway,
            final PerceptionIterable<? extends Headway> leaders) throws ParameterException
    {
        // 1. Get raw IDM+ acceleration using the superclass implementation
        Acceleration a = parameters.getParameter(A);
        Headway leader = leaders.first();
        double sRatio =
                dynamicDesiredHeadway(parameters, speed, desiredHeadway, leader.getSpeed()).si / leader.getDistance().si;
        double aInt = a.si * (1 - sRatio * sRatio);
        Acceleration aIdm = new Acceleration(aInt < aFree.si ? aInt : aFree.si, AccelerationUnit.SI);
        Acceleration bCrit = parameters.getParameter(MirovaParameters.B_CRIT);

        // 2. If IDM deceleration is within comfortable limits (or we are accelerating), accept it
        if (aIdm.si >= bCrit.si)
        {
            return aIdm;
        }

        // 3. We are exceeding bCrit. Assess kinematic necessity.
        Length s = leader.getDistance();
        Length s0 = parameters.getParameter(ParameterTypes.S0);
        Speed deltaV = speed.minus(leader.getSpeed());

        double dKinSi = 0.0; // Required deceleration (positive value)

        if (deltaV.gt0() && s.gt(s0))
        {
            // d_kin = (dv^2) / (2 * (s - s0))
            dKinSi = -(deltaV.si * deltaV.si) / (2.0 * (s.si - s0.si));
        }
        else if (s.le(s0) && deltaV.gt0())
        {
            // Crash imminent
            dKinSi = Double.NEGATIVE_INFINITY;
        }

        // 4. Apply stateless fallback logic
        if (dKinSi >= bCrit.si)
        {
            // Physics allow us to handle this with a comfortable critical brake (filters cut-in shock)
            return bCrit;
        }
        else if (dKinSi > parameters.getParameter(MirovaParameters.B_MAX).si)
        {
            // Physics demand severe action, fall back to max deceleration
            return parameters.getParameter(MirovaParameters.B_MAX);
        }
        else
        {
            // Physics demand more than critical braking, but not beyond max deceleration. Apply kinematic bound.
            return new Acceleration(-9.0, AccelerationUnit.SI);
        }
    }
}
