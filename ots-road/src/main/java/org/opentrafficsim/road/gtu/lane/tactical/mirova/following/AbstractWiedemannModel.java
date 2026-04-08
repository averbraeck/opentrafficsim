package org.opentrafficsim.road.gtu.lane.tactical.mirova.following;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionIterable;
import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;
import org.opentrafficsim.road.gtu.lane.tactical.following.AbstractCarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.following.DesiredHeadwayModel;
import org.opentrafficsim.road.gtu.lane.tactical.following.DesiredSpeedModel;

import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Abstract base class for all Wiedemann-family car-following models (Wiedemann-74, Wiedemann-99).
 * <p>
 * This class encapsulates all common structure:
 * <ul>
 * <li>Leader extraction (gap, relative speed, acceleration)</li>
 * <li>Stochastic component injection</li>
 * <li>Free-driving behavior</li>
 * <li>Template-method selection of regimes A / B / f / w</li>
 * <li>Type-safe unit handling (OTS Length, Speed, Acceleration)</li>
 * </ul>
 * <p>
 * Concrete Wiedemann variants (W74, W99) only need to override:
 * <ul>
 * <li>{@link #computeSdxc}</li>
 * <li>{@link #computeSdxo}</li>
 * <li>{@link #computeSdxv}</li>
 * <li>{@link #computeSdv}</li>
 * <li>{@link #computeSdvc}</li>
 * <li>{@link #computeSdvo}</li>
 * <li>the four regime methods:
 * {@link #regimeTooClose},
 * {@link #regimeClosingSpeedTooHigh},
 * {@link #regimeFollowingOscillation},
 * {@link #regimeFreeDriving}</li>
 * </ul>
 * <p>
 * This provides a unified framework for all Wiedemann-based OTS models.
 * </p>
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 *
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public abstract class AbstractWiedemannModel extends AbstractCarFollowingModel {

    /** Optional stochastic stream for variants that need noise (e.g., W99). */
    protected final StreamInterface stream;

    /** Speed limit adherence factor parameter type. */
    public static final ParameterTypeDouble FSPEED = ParameterTypes.FSPEED;

    /**
     * Construct an abstract Wiedemann model.
     * * @param headway      desired headway model (Wiedemann models typically override SDXC anyway)
     * @param desiredSpeed desired speed model
     * @param stream       optional stochastic stream; can be null for deterministic models
     */
    protected AbstractWiedemannModel(
            final DesiredHeadwayModel headway,
            final DesiredSpeedModel desiredSpeed,
            final StreamInterface stream)
    {
        super(headway, desiredSpeed);
        this.stream = stream;
    }

    /* =======================================================================================
     * ABSTRACT THRESHOLD COMPUTATION HOOKS
     * Every Wiedemann model must define its own thresholds.
     * ======================================================================================= */

    /**
     * Computes SDXC – minimum safe following distance.
     * * @param p         the parameter set
     * @param dv        relative speed (leader - follower)
     * @param vFollower speed of the follower (ego)
     * @param vLeader   speed of the leader
     * @return the computed SDXC length
     * @throws ParameterException if a required parameter is missing
     */
    protected abstract Length computeSdxc(
            Parameters p, Speed dv, Speed vFollower, Speed vLeader) throws ParameterException;

    /**
     * Computes SDXO – upper threshold distance for following zone.
     * * @param p    the parameter set
     * @param sdxc the pre-computed SDXC threshold
     * @return the computed SDXO length
     * @throws ParameterException if a required parameter is missing
     */
    protected abstract Length computeSdxo(
            Parameters p, Length sdxc) throws ParameterException;

    /**
     * Computes SDXV – threshold for closing-speed-based braking.
     * * @param sdxo the pre-computed SDXO threshold
     * @param p    the parameter set
     * @param dv   relative speed (leader - follower)
     * @return the computed SDXV length
     * @throws ParameterException if a required parameter is missing
     */
    protected abstract Length computeSdxv(
            Length sdxo, Parameters p, Speed dv) throws ParameterException;

    /**
     * Computes SDV – perceived speed-difference threshold.
     * * @param p  the parameter set
     * @param dx actual gap distance
     * @return the computed SDV speed
     * @throws ParameterException if a required parameter is missing
     */
    protected abstract Speed computeSdv(
            Parameters p, Length dx) throws ParameterException;

    /**
     * Computes SDVC – dv-threshold for emergency braking.
     * * @param sdv     pre-computed SDV threshold
     * @param p       the parameter set
     * @param vLeader speed of the leader
     * @return the computed SDVC speed
     * @throws ParameterException if a required parameter is missing
     */
    protected abstract Speed computeSdvc(
            Speed sdv, Parameters p, Speed vLeader) throws ParameterException;

    /**
     * Computes SDVO – dv-threshold for oscillatory following.
     * * @param sdv       pre-computed SDV threshold
     * @param p         the parameter set
     * @param vFollower speed of the follower (ego)
     * @return the computed SDVO speed
     * @throws ParameterException if a required parameter is missing
     */
    protected abstract Speed computeSdvo(
            Speed sdv, Parameters p, Speed vFollower) throws ParameterException;

    /* =======================================================================================
     * ABSTRACT REGIME HOOKS
     * Concrete Wiedemann types implement these four acceleration rules.
     * ======================================================================================= */

    /**
     * Computes the acceleration in Wiedemann Regime A:
     * "Too Close" — defensive deceleration.
     * <p>
     * Every quantity uses OTS units: {@link Speed}, {@link Length}, {@link Acceleration}.
     * </p>
     *
     * @param p         the parameter set
     * @param dx        actual gap
     * @param dv        relative speed (leader - follower)
     * @param sdxc      threshold SDXC
     * @param sdvo      threshold SDVO
     * @param vFollower follower speed
     * @param leaderAcc leader acceleration
     * @return the computed defensive acceleration
     * @throws ParameterException if a required parameter is missing
     */
    protected abstract Acceleration regimeTooClose(
            Parameters p, Length dx, Speed dv, Length sdxc, Speed sdvo,
            Speed vFollower, Acceleration leaderAcc) throws ParameterException;

    /**
     * Computes the acceleration in Regime B:
     * emergency-ish braking when closing speed is too large.
     *
     * @param p    the parameter set
     * @param dx   actual gap
     * @param dv   relative speed (leader - follower)
     * @param sdxc threshold SDXC
     * @return the computed braking deceleration
     * @throws ParameterException if a required parameter is missing
     */
    protected abstract Acceleration regimeClosingSpeedTooHigh(
            Parameters p, Length dx, Speed dv, Length sdxc) throws ParameterException;

    /**
     * Computes the acceleration in Regime f:
     * oscillatory following (acc/dec around leader dynamics).
     *
     * @param p         the parameter set
     * @param dx        actual gap
     * @param dv        relative speed (leader - follower)
     * @param sdxo      upper following threshold SDXO
     * @param vFollower follower speed
     * @param vDesired  follower desired speed
     * @param leaderAcc leader acceleration
     * @return the computed oscillatory acceleration
     * @throws ParameterException if a required parameter is missing
     */
    protected abstract Acceleration regimeFollowingOscillation(
            Parameters p, Length dx, Speed dv, Length sdxo,
            Speed vFollower, Speed vDesired, Acceleration leaderAcc) throws ParameterException;

    /**
     * Computes the acceleration in Regime w:
     * free acceleration toward desired speed.
     *
     * @param p         the parameter set
     * @param dx        actual gap
     * @param dv        relative speed (leader - follower)
     * @param sdxo      upper following threshold SDXO
     * @param vFollower follower speed
     * @param vDesired  follower desired speed
     * @return the computed free-driving acceleration
     * @throws ParameterException if a required parameter is missing
     */
    protected abstract Acceleration regimeFreeDriving(
            Parameters p, Length dx, Speed dv, Length sdxo,
            Speed vFollower, Speed vDesired) throws ParameterException;


    /* =======================================================================================
     * MAIN ENTRY POINT – identical for all Wiedemann models
     * ======================================================================================= */

    @Override
    protected final Acceleration followingAcceleration(
            final Parameters p,
            final Speed vFollower,
            final Speed vDesired,
            final Length ignoredHeadway,
            final PerceptionIterable<? extends Headway> leaders)
            throws ParameterException
    {
        /* ================================
           FREE DRIVING (NO LEADER)
         ================================ */
        if (leaders.isEmpty())
        {
            double dv = vDesired.si - vFollower.si;
            // Simple P-controller for free driving without a leader
            return Acceleration.instantiateSI(Math.max(0.1 * dv, -1.0));
        }

        /* ================================
           LEADER PROPERTIES
         ================================ */
        Headway L = leaders.first();

        Length dx = L.getDistance();
        Speed vLeader = L.getSpeed();
        Speed dv = Speed.instantiateSI(vLeader.si - vFollower.si);
        Acceleration aLeader = (L.getAcceleration() == null)
                ? Acceleration.instantiateSI(0)
                : L.getAcceleration();

        /* ================================
           THRESHOLD COMPUTATION
         ================================ */
        Length sdxc = computeSdxc(p, dv, vFollower, vLeader);
        Length sdxo = computeSdxo(p, sdxc);
        Length sdxv = computeSdxv(sdxo, p, dv);
        Speed  sdv  = computeSdv(p, dx);
        Speed  sdvc = computeSdvc(sdv, p, vLeader);
        Speed  sdvo = computeSdvo(sdv, p, vFollower);

        /* ================================
           REGIME SELECTION (Template Method)
         ================================ */

        // Regime A – Too Close
        if (dv.si < sdvo.si && dx.si <= sdxc.si)
        {
            return regimeTooClose(p, dx, dv, sdxc, sdvo, vFollower, aLeader);
        }

        // Regime B – Closing too fast
        if (dv.si < sdvc.si && dx.si < sdxv.si)
        {
            return regimeClosingSpeedTooHigh(p, dx, dv, sdxc);
        }

        // Regime f – Following oscillation
        if (dv.si < sdvo.si && dx.si < sdxo.si)
        {
            return regimeFollowingOscillation(p, dx, dv, sdxo, vFollower, vDesired, aLeader);
        }

        // Regime w – Free driving
        return regimeFreeDriving(p, dx, dv, sdxo, vFollower, vDesired);
    }
}