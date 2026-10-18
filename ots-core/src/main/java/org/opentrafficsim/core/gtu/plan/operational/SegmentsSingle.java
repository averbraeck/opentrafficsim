package org.opentrafficsim.core.gtu.plan.operational;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;

/**
 * Segments from a single segment.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.<br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * @author Wouter Schakel
 */
public class SegmentsSingle implements Segments
{

    /** Speed. */
    private final Speed startSpeed;

    /** Speed. */
    private final Speed endSpeed;

    /** Duration. */
    private final Duration duration;

    /** Acceleration. */
    private final Acceleration acceleration;

    /** Length. */
    private final Length length;

    /**
     * Constructor.
     * @param startSpeed start speed
     * @param duration duration
     * @param acceleration acceleration
     */
    public SegmentsSingle(final Speed startSpeed, final Duration duration, final Acceleration acceleration)
    {
        this.startSpeed = startSpeed;
        this.duration = duration;
        this.acceleration = acceleration;
        double dt = this.duration.si;
        this.length = Length.ofSI(this.startSpeed.si * dt + 0.5 * this.acceleration.si * dt * dt);
        this.endSpeed = Speed.ofSI(getSpeedSI(0, duration.si));
    }

    @Override
    public int size()
    {
        return 1;
    }

    @Override
    public Speed getStartSpeed()
    {
        return this.startSpeed;
    }

    @Override
    public Speed getEndSpeed()
    {
        return this.endSpeed;
    }

    @Override
    public Duration getTotalDuration()
    {
        return this.duration;
    }

    @Override
    public Length getTotalLength()
    {
        return this.length;
    }

    @Override
    public boolean isStandStill()
    {
        return this.startSpeed.si == 0.0 && this.acceleration.si == 0.0;
    }

    @Override
    @SuppressWarnings("hiddenfield")
    public int getSegment(final double duration)
    {
        return 0;
    }

    @Override
    public double getStartSpeedSI(final int segment)
    {
        return this.startSpeed.si;
    }

    @Override
    public double getAccelerationSI(final int segment)
    {
        return this.acceleration.si;
    }

    @Override
    public double getStartDurationSI(final int segment)
    {
        return 0.0;
    }

    @Override
    public double getStartDistanceSI(final int segment)
    {
        return 0.0;
    }

    @Override
    public double getDurationSI(final int segment)
    {
        return this.duration.si;
    }

    @Override
    public double getSpeedSI(final int segment, final double durationInSegment)
    {
        return this.startSpeed.si + durationInSegment * this.acceleration.si;
    }

    @Override
    public double getDistanceSI(final int segment, final double durationInSegment)
    {
        return this.startSpeed.si * durationInSegment + .5 * this.acceleration.si * durationInSegment * durationInSegment;
    }

}
