package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context;

import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;

/**
 * Tracks and computes the relaxation phenomenon according to Keane and Gao (2021).
 * <p>
 * This class stores the initial space headway deficit (gamma_s) and the speed difference (gamma_v) after a lane change or
 * cut-in. It provides exponentially decaying virtual buffers for both distance and speed. This represents the "2p"
 * (two-parameter) relaxation model, allowing independent decay rates for space and speed errors.
 * </p>
 * <p>
 * Copyright (c) 2026 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public class RelaxationState
{
    /** The absolute simulation time when the lane change or cut-in occurred. */
    private final Duration startTime;

    /** The initial space headway deficit [m] at the time of the event (gamma_s). */
    private final Length initialSpaceDeficit;

    /** The initial speed difference [m/s] between the old and new leader (gamma_v). */
    private final Speed initialSpeedDeficit;

    /** The relaxation time constant [s] for the space headway (Tau_s). */
    private final Duration tauSpace;

    /** The relaxation time constant [s] for the leader speed (Tau_v). */
    private final Duration tauSpeed;

    /**
     * Constructs a new RelaxationState to track virtual distance and speed buffers over time.
     * @param startTime the absolute simulation time the lane change or cut-in occurred
     * @param initialSpaceDeficit the initial missing distance to the desired space headway
     * @param initialSpeedDeficit the speed difference (oldLeaderSpeed - newLeaderSpeed)
     * @param tauSpace the time constant for the spatial exponential decay
     * @param tauSpeed the time constant for the speed exponential decay
     */
    public RelaxationState(final Duration startTime, final Length initialSpaceDeficit, final Speed initialSpeedDeficit,
            final Duration tauSpace, final Duration tauSpeed)
    {
        this.startTime = startTime;
        this.initialSpaceDeficit = initialSpaceDeficit;
        this.initialSpeedDeficit = initialSpeedDeficit;
        this.tauSpace = tauSpace;
        this.tauSpeed = tauSpeed;
    }

    /**
     * Computes the remaining virtual distance buffer for the given simulation time.
     * <p>
     * The virtual buffer decays exponentially based on elapsed time and Tau_s.
     * </p>
     * @param currentTime the current absolute simulation time
     * @return the virtual distance buffer to add to the actual headway, never negative
     */
    public Length getVirtualSpaceBuffer(final Duration currentTime)
    {
        double elapsedSi = currentTime.si - this.startTime.si;

        if (elapsedSi < 0.0 || this.initialSpaceDeficit.si <= 0.0)
        {
            return Length.ZERO;
        }

        double bufferSi = this.initialSpaceDeficit.si * Math.exp(-elapsedSi / this.tauSpace.si);
        return new Length(bufferSi, LengthUnit.SI);
    }

    /**
     * Computes the remaining virtual speed buffer for the given simulation time.
     * <p>
     * The virtual buffer decays exponentially based on elapsed time and Tau_v. If the new leader is faster than the old leader
     * (negative deficit), no buffer is applied. Allowing a negative buffer would artificially reduce the perceived leader
     * speed, triggering unsafe and unnecessary severe decelerations in the CF model.
     * </p>
     * @param currentTime the current absolute simulation time
     * @return the virtual speed buffer to add to the actual leader speed
     */
    public Speed getVirtualSpeedBuffer(final Duration currentTime)
    {
        double elapsedSi = currentTime.si - this.startTime.si;

        // BUGFIX: Prevent negative speed buffers and invalid Tau to avoid massive decelerations.
        if (elapsedSi < 0.0 || this.initialSpeedDeficit.si <= 0.0 || this.tauSpeed.si <= 0.0)
        {
            return Speed.ZERO;
        }

        double bufferSi = this.initialSpeedDeficit.si * Math.exp(-elapsedSi / this.tauSpeed.si);
        return Speed.instantiateSI(bufferSi);
    }
}
