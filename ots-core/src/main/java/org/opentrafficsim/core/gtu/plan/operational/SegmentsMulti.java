package org.opentrafficsim.core.gtu.plan.operational;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;

/**
 * List of segments describing the longitudinal dynamics of a operational plan along the path.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 */
public class SegmentsMulti implements Segments
{

    /** Start speed for each segment. */
    private final double[] startSpeed;

    /** Duration of each segment. */
    private final double[] duration;

    /** Acceleration in each segment. */
    private final double[] acceleration;

    /** Start duration of each segment. */
    private final double[] startDuration;

    /** Start distance of each segment. */
    private final double[] startDistance;

    /** Start speed. */
    private final Speed startSpeedUnit;

    /** End speed. */
    private final Speed endSpeedUnit;

    /** Total duration. */
    private final Duration totalDuration;

    /** Total length. */
    private final Length totalLength;

    /**
     * Constructor.
     * @param startSpeed start speed for each segment
     * @param duration duration of each segment
     * @param acceleration acceleration in each segment
     */
    public SegmentsMulti(final double[] startSpeed, final double[] duration, final double[] acceleration)
    {
        this.startSpeed = startSpeed;
        this.duration = duration;
        this.acceleration = acceleration;
        this.startSpeedUnit = Speed.ofSI(startSpeed[0]);
        this.startDuration = new double[startSpeed.length];
        this.startDistance = new double[startSpeed.length];
        for (int i = 0; i < startSpeed.length - 1; i++)
        {
            double dt = duration[i];
            this.startDuration[i + 1] = this.startDuration[i] + dt;
            this.startDistance[i + 1] = this.startDistance[i] + this.startSpeed[i] * dt + .5 * this.acceleration[i] * dt * dt;
        }
        int last = this.duration.length - 1;
        double dt = this.duration[last];
        this.totalDuration = Duration.ofSI(this.startDuration[last] + dt);
        this.totalLength =
                Length.ofSI(this.startDistance[last] + this.startSpeed[last] * dt + .5 * this.acceleration[last] * dt * dt);
        this.endSpeedUnit = Speed.ofSI(getSpeedSI(last, this.duration[last]));
    }

    @Override
    public int size()
    {
        return this.startSpeed.length;
    }

    @Override
    public Speed getStartSpeed()
    {
        return this.startSpeedUnit;
    }

    @Override
    public Speed getEndSpeed()
    {
        return this.endSpeedUnit;
    }

    @Override
    public Duration getTotalDuration()
    {
        return this.totalDuration;
    }

    @Override
    public Length getTotalLength()
    {
        return this.totalLength;
    }

    @Override
    public boolean isStandStill()
    {
        return false;
    }

    @Override
    @SuppressWarnings("hiddenfield")
    public int getSegment(final double duration)
    {
        int segment = 0;
        while (segment < size() - 1 && this.startDuration[segment + 1] < duration)
        {
            segment++;
        }
        return segment;
    }

    @Override
    public double getStartSpeedSI(final int segment)
    {
        return this.startSpeed[segment];
    }

    @Override
    public double getAccelerationSI(final int segment)
    {
        return this.acceleration[segment];
    }

    @Override
    public double getStartDurationSI(final int segment)
    {
        return this.startDuration[segment];
    }

    @Override
    public double getStartDistanceSI(final int segment)
    {
        return this.startDistance[segment];
    }

    @Override
    public double getDurationSI(final int segment)
    {
        return this.duration[segment];
    }

    @Override
    public double getSpeedSI(final int segment, final double durationInSegment)
    {
        return this.startSpeed[segment] + durationInSegment * this.acceleration[segment];
    }

    @Override
    public double getDistanceSI(final int segment, final double durationInSegment)
    {
        return this.startSpeed[segment] * durationInSegment
                + .5 * this.acceleration[segment] * durationInSegment * durationInSegment;
    }

}
