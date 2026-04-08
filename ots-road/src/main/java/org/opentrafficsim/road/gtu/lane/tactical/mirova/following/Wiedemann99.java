package org.opentrafficsim.road.gtu.lane.tactical.mirova.following;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeAcceleration;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.base.parameters.ParameterTypeDuration;
import org.opentrafficsim.base.parameters.ParameterTypeLength;
import org.opentrafficsim.base.parameters.ParameterTypeSpeed;
import org.opentrafficsim.base.parameters.ParameterTypeString;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;

import org.opentrafficsim.road.gtu.lane.tactical.following.DesiredHeadwayModel;
import org.opentrafficsim.road.gtu.lane.tactical.following.DesiredSpeedModel;
import org.opentrafficsim.road.gtu.lane.tactical.util.SpeedLimitUtil;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Compact Wiedemann-99 implementation built on top of {@link AbstractWiedemannModel}.
 * <p>
 * This class contains ONLY the model-specific formulas and thresholds.
 * All generic structure, regime selection, free-driving logic, and perception
 * handling is inherited from {@link AbstractWiedemannModel}.
 * </p>
 * <p>
 * Parameters CC0–CC9 must be present in the OTS {@link Parameters} object.
 * </p>
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 *
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public class Wiedemann99 extends AbstractWiedemannModel
{

    /** CC0 mapped to OTS S0 (standstill distance). */
    public static final ParameterTypeLength CC0 = ParameterTypes.S0;

    /** CC1 mapped to OTS T (headway factor). */
    public static final ParameterTypeDuration CC1 = ParameterTypes.T;

    /** Additional Wiedemann parameters. */
    public static final ParameterTypeLength CC2 = W99ParameterTypes.CC2;
    public static final ParameterTypeDuration CC3 = W99ParameterTypes.CC3;
    public static final ParameterTypeSpeed CC4 = W99ParameterTypes.CC4;
    public static final ParameterTypeSpeed CC5 = W99ParameterTypes.CC5;
    public static final ParameterTypeDouble CC6 = W99ParameterTypes.CC6;
    public static final ParameterTypeAcceleration CC7 = W99ParameterTypes.CC7;
    public static final ParameterTypeAcceleration CC8 = W99ParameterTypes.CC8;
    public static final ParameterTypeAcceleration CC9 = W99ParameterTypes.CC9;
    public static final ParameterTypeString CURRENT_DRIVING_MODE = W99ParameterTypes.CURRENT_DRIVING_MODE;

    /** Default IDM desired headway model adapted for W99. */
    public static final DesiredHeadwayModel HEADWAY = new DesiredHeadwayModel()
    {
        @Override
        public Length desiredHeadway(final Parameters parameters, final Speed speed) throws ParameterException
        {
            return Length.instantiateSI(parameters.getParameter(CC0).si + speed.si * parameters.getParameter(CC1).si);
        }
    };

    /** Default IDM desired speed model adapted for W99. */
    public static final DesiredSpeedModel DESIRED_SPEED = new DesiredSpeedModel()
    {
        @Override
        public Speed desiredSpeed(final Parameters parameters, final SpeedLimitInfo speedInfo) throws ParameterException
        {
            Speed consideredSpeed = SpeedLimitUtil.getLegalSpeedLimit(speedInfo).times(parameters.getParameter(FSPEED));
            Speed maxVehicleSpeed = SpeedLimitUtil.getMaximumVehicleSpeed(speedInfo);
            return consideredSpeed.le(maxVehicleSpeed) ? consideredSpeed : maxVehicleSpeed;
        }
    };

    /**
     * Create the W99 model with custom headway and speed models.
     *
     * @param headway  the desired headway model
     * @param vDesired the desired speed model
     * @param stream   the stochastic stream for randomizing thresholds
     */
    public Wiedemann99(
            final DesiredHeadwayModel headway,
            final DesiredSpeedModel vDesired,
            final StreamInterface stream)
    {
        super(headway, vDesired, stream);
    }

    /**
     * Convenience constructor using W99 defaults for headway and desired speed.
     *
     * @param stream the stochastic stream for randomizing thresholds
     */
    public Wiedemann99(final StreamInterface stream)
    {
        super(HEADWAY, DESIRED_SPEED, stream);
    }

    /* ======================================================================================
     * THRESHOLD COMPUTATIONS (SDXC, SDXO, SDXV, SDV, SDVC, SDVO)
     * ====================================================================================== */

    @Override
    protected Length computeSdxc(
            final Parameters p,
            final Speed dv,
            final Speed vFollower,
            final Speed vLeader) throws ParameterException
    {
        Length cc0 = p.getParameter(CC0);       // standstill distance
        Duration cc1 = p.getParameter(CC1);     // time headway factor

        if (vLeader.si <= 0.0)
        {
            return cc0;
        }

        // vSlower = either follower v or jittered interpolation
        Speed vSlower;
        if (dv.si >= 0)
        {
            vSlower = vFollower;
        }
        else
        {
            double jitter = this.stream.nextDouble() - 0.5;
            double vGuess = Math.max(vLeader.si + dv.si * jitter, 0.0);
            vSlower = Speed.instantiateSI(vGuess);
        }

        return cc0.plus(Length.instantiateSI(vSlower.si * cc1.si));
    }

    @Override
    protected Length computeSdxo(final Parameters p, final Length sdxc) throws ParameterException
    {
        return sdxc.plus(p.getParameter(CC2));
    }

    @Override
    protected Length computeSdxv(
            final Length sdxo,
            final Parameters p,
            final Speed dv) throws ParameterException
    {
        Duration cc3 = p.getParameter(CC3);
        Speed cc4 = p.getParameter(CC4);

        return Length.instantiateSI(
                sdxo.si + cc3.si * (dv.si - cc4.si)
        );
    }

    @Override
    protected Speed computeSdv(final Parameters p, final Length dx) throws ParameterException
    {
        double cc6SI = p.getParameter(CC6).doubleValue() * 0.0001 / 1.7; // convert from 10^-4 rad/s to 1/s
        return Speed.instantiateSI(cc6SI * dx.si * dx.si);
    }

    @Override
    protected Speed computeSdvc(
            final Speed sdv,
            final Parameters p,
            final Speed vLeader) throws ParameterException
    {
        Speed cc4 = p.getParameter(CC4);
        return (vLeader.si > 0)
                ? Speed.instantiateSI(cc4.si - sdv.si)
                : Speed.instantiateSI(0.0);
    }

    @Override
    protected Speed computeSdvo(
            final Speed sdv,
            final Parameters p,
            final Speed vFollower) throws ParameterException
    {
        Speed cc5 = p.getParameter(CC5);
        return (vFollower.si > cc5.si)
                ? Speed.instantiateSI(sdv.si + cc5.si)
                : sdv;
    }

    /* ======================================================================================
     * REGIMES (A, B, f, w)
     * ====================================================================================== */

    @Override
    protected Acceleration regimeTooClose(
            final Parameters p,
            final Length dx,
            final Speed dv,
            final Length sdxc,
            final Speed sdvo,
            final Speed vFollower,
            final Acceleration leaderAcc)
            throws ParameterException
    {
        Length cc0 = p.getParameter(CC0);
        Acceleration cc7 = p.getParameter(CC7);

        double dxSI = dx.si;
        double dvSI = dv.si;
        double sdvoSI = sdvo.si;
        double cc0SI = cc0.si;
        double cc7SI = cc7.si;
        double aL = leaderAcc.si;
        double vF = vFollower.si;

        double a;

        if (vF > 0)
        {
            if (dvSI < 0)
            {
                if (dxSI > cc0SI)
                {
                    a = Math.min(aL + dvSI * dvSI / (cc0SI - dxSI), 0.0);
                }
                else
                {
                    a = Math.min(aL + 0.5 * (dvSI - sdvoSI), 0.0);
                }
            }
            else
            {
                a = 0.0;
            }

            a = Math.min(a, -cc7SI);
            a = Math.max(a, -10 + 0.5 * Math.sqrt(vF));
        }
        else
        {
            a = -cc7SI;
        }

        p.setParameter(CURRENT_DRIVING_MODE, "A");
        a = Math.round(a * 100.0) / 100.0;
        return Acceleration.instantiateSI(a);
    }

    @Override
    protected Acceleration regimeClosingSpeedTooHigh(
            final Parameters p,
            final Length dx,
            final Speed dv,
            final Length sdxc) throws ParameterException
    {
        double dvSI = dv.si;
        double dxSI = dx.si;
        double sdxcSI = sdxc.si;

        double a = 0.5 * dvSI * dvSI / (-dxSI + sdxcSI - 0.1);
        a = Math.max(a, -10.0);

        p.setParameter(CURRENT_DRIVING_MODE, "B");
        a = Math.round(a * 100.0) / 100.0;
        return Acceleration.instantiateSI(a);
    }

    @Override
    protected Acceleration regimeFollowingOscillation(
            final Parameters p,
            final Length dx,
            final Speed dv,
            final Length sdxo,
            final Speed vFollower,
            final Speed vDesired,
            final Acceleration leaderAcc) throws ParameterException
    {
        Acceleration cc7 = p.getParameter(CC7);

        double aL = leaderAcc.si;
        double cc7SI = cc7.si;
        double vF = vFollower.si;
        double vDes = vDesired.si;

        double a;

        if (aL <= 0)
        {
            a = Math.min(aL, -cc7SI);
        }
        else
        {
            a = Math.max(aL, cc7SI);
            a = Math.min(a, vDes - vF);
        }

        p.setParameter(CURRENT_DRIVING_MODE, "f");
        a = Math.round(a * 100.0) / 100.0;
        return Acceleration.instantiateSI(a);
    }

    @Override
    protected Acceleration regimeFreeDriving(
            final Parameters p,
            final Length dx,
            final Speed dv,
            final Length sdxo,
            final Speed vFollower,
            final Speed vDesired) throws ParameterException
    {
        if (p.getParameter(CURRENT_DRIVING_MODE).equals("f"))
        {
            // keep oscillation acceleration sign when entering free driving from oscillation
            return p.getParameter(CC7);
        }
        Acceleration cc8 = p.getParameter(CC8);
        Acceleration cc9 = p.getParameter(CC9).minus(cc8).times(3.6 / 80.0);

        double dxSI = dx.si;
        double sdxoSI = sdxo.si;
        double dvSI = dv.si;
        double vF = vFollower.si;
        double vDes = vDesired.si;

        double aMax = cc8.si + cc9.si * Math.min(vF, 22.22) + this.stream.nextDouble();

        double a;
        if (dxSI < sdxoSI)
        {
            a = Math.min(dvSI * dvSI / (sdxoSI - dxSI), aMax);
        }
        else
        {
            a = aMax;
        }

        a = Math.min(a, vDes - vF);

        p.setParameter(CURRENT_DRIVING_MODE, "w");
        a = Math.round(a * 100.0) / 100.0;
        return Acceleration.instantiateSI(a);
    }

    @Override
    public String getLongName()
    {
        return "Wiedemann99";
    }

    @Override
    public String getName()
    {
        return "W99";
    }
}