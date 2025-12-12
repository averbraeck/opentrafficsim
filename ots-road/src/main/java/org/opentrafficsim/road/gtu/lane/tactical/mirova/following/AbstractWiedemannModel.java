package org.opentrafficsim.road.gtu.lane.tactical.mirova.following;

import org.djunits.value.vdouble.scalar.*;
import org.djunits.unit.AccelerationUnit;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeAcceleration;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.base.parameters.ParameterTypeDuration;
import org.opentrafficsim.base.parameters.ParameterTypeLength;
import org.opentrafficsim.base.parameters.ParameterTypeSpeed;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;

import org.opentrafficsim.road.gtu.lane.tactical.following.AbstractCarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.following.DesiredHeadwayModel;
import org.opentrafficsim.road.gtu.lane.tactical.following.DesiredSpeedModel;
import org.opentrafficsim.road.gtu.lane.tactical.util.SpeedLimitUtil;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionIterable;
import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;

import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Abstract base class for all Wiedemann-family car-following models
 * (Wiedemann-74, Wiedemann-99).
 *
 * <p>This class encapsulates all common structure:
 *
 * <ul>
 *   <li>Leader extraction (gap, relative speed, acceleration)</li>
 *   <li>Stochastic component injection</li>
 *   <li>Free-driving behavior</li>
 *   <li>Template-method selection of regimes A / B / f / w</li>
 *   <li>Type-safe unit handling (OTS Length, Speed, Acceleration)</li>
 * </ul>
 *
 * <p>Concrete Wiedemann variants (W74, W99) only need to override:
 * <ul>
 *   <li>{@link #computeSdxc}</li>
 *   <li>{@link #computeSdxo}</li>
 *   <li>{@link #computeSdxv}</li>
 *   <li>{@link #computeSdv}</li>
 *   <li>{@link #computeSdvc}</li>
 *   <li>{@link #computeSdvo}</li>
 *   <li>the four regime methods:
 *       {@link #regimeTooClose},
 *       {@link #regimeClosingSpeedTooHigh},
 *       {@link #regimeFollowingOscillation},
 *       {@link #regimeFreeDriving}}
 * </ul>
 *
 * This provides a unified framework for all Wiedemann-based OTS models.
 */
public abstract class AbstractWiedemannModel extends AbstractCarFollowingModel {

    /** Optional stochastic stream for variants that need noise (W99). */
    protected final StreamInterface stream;

    /** Speed limit adherence factor parameter type. */
    public static final ParameterTypeDouble FSPEED = ParameterTypes.FSPEED;
    /**
     * Construct an abstract Wiedemann model.
     * @param headway desired headway model (Wiedemann models override SDXC anyway)
     * @param desiredSpeed desired speed model
     * @param stream optional stochastic stream; can be null for deterministic W74
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

    /** SDXC – minimum safe following distance. */
    protected abstract Length computeSdxc(
            Parameters p, Speed dv, Speed vFollower, Speed vLeader) throws ParameterException;

    /** SDXO – upper threshold distance for following zone. */
    protected abstract Length computeSdxo(
            Parameters p, Length sdxc) throws ParameterException;

    /** SDXV – threshold for closing-speed-based braking. */
    protected abstract Length computeSdxv(
            Length sdxo, Parameters p, Speed dv) throws ParameterException;

    /** SDV – perceived speed-difference threshold. */
    protected abstract Speed computeSdv(
            Parameters p, Length dx) throws ParameterException;

    /** SDVC – dv-threshold for emergency braking. */
    protected abstract Speed computeSdvc(
            Speed sdv, Parameters p, Speed vLeader) throws ParameterException;

    /** SDVO – dv-threshold for oscillatory following. */
    protected abstract Speed computeSdvo(
            Speed sdv, Parameters p, Speed vFollower) throws ParameterException;

    /* =======================================================================================
     * ABSTRACT REGIME HOOKS
     * Concrete Wiedemann types implement these four acceleration rules.
     * ======================================================================================= */

    /**
     * Computes the acceleration in Wiedemann Regime A:
     * "Too Close" — defensive deceleration.
     *
     * <p>Unit-correct implementation:
     * Every quantity uses OTS {@link Speed}, {@link Length}, {@link Acceleration}.
     *
     * @param dx        actual gap [m]
     * @param dv        relative speed (leader - follower) [m/s]
     * @param sdxc      threshold SDXC [m]
     * @param sdvo      threshold SDVO [m/s]
     * @param leaderAcc leader acceleration [m/s²]
     * @param cc0       standstill distance [m]
     * @param cc7       oscillation deceleration [m/s²]
     * @param vFollower follower speed [m/s]
     * @return resulting acceleration
     */
    protected abstract Acceleration regimeTooClose(
            Parameters p,
            Length dx, Speed dv, Length sdxc, Speed sdvo,
            Speed vFollower, Acceleration leaderAcc) throws ParameterException;

    /**
     * Regime B: emergency-ish braking when closing speed is too large.
     *
     * @param dv    relative speed [m/s]
     * @param dx    actual gap [m]
     * @param sdxc  SDXC [m]
     * @return deceleration [m/s²]
     */
    protected abstract Acceleration regimeClosingSpeedTooHigh(
            Parameters p,
            Length dx, Speed dv, Length sdxc) throws ParameterException;

    /**
     * Regime f: oscillatory following (acc/dec around leader dynamics).
     *
     * @param leaderAcc  leader acceleration [m/s²]
     * @param cc7        oscillation acceleration [m/s²]
     * @param vFollower  follower speed [m/s]
     * @param vDesired   follower desired speed [m/s]
     * @return resulting acceleration
     */
    protected abstract Acceleration regimeFollowingOscillation(
            Parameters p,
            Length dx, Speed dv,
            Length sdxo,
            Speed vFollower, Speed vDesired,
            Acceleration leaderAcc) throws ParameterException;

    /**
     * Regime w: free acceleration toward desired speed.
     *
     * @param dv        relative speed [m/s]
     * @param dx        gap [m]
     * @param sdxo      upper following threshold SDXO [m]
     * @param cc8       standstill acceleration [m/s²]
     * @param cc9       accel-at-80-km/h component [m/s²]
     * @param vFollower follower speed
     * @param vDesired  desired speed
     * @return acceleration
     */
    protected abstract Acceleration regimeFreeDriving(
            Parameters p,
            Length dx, Speed dv,
            Length sdxo,
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
            return Acceleration.instantiateSI(Math.max(0.1 * dv, -1.0));
        }

        /* ================================
           LEADER PROPERTIES
         ================================ */
        Headway L = leaders.first();

        Length dx = L.getDistance();                           // gap
        Speed vLeader = L.getSpeed();                          // leader speed
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

