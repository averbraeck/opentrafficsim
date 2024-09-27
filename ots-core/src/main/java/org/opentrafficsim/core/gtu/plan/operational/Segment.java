package org.opentrafficsim.core.gtu.plan.operational;

import java.io.Serializable;

import org.djunits.unit.DurationUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.math.Solver;

/**
 * The segment of an operational plan contains a part of the speed profile of a movement in which some of the variables
 * determining movement (speed, acceleration) are constant.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. <br>
 * All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param isStandStill whether this is a stand still segment
 * @param startSpeed start speed.
 * @param duration the duration of the acceleration for this segment.
 * @param acceleration acceleration of this segment.
 */
public record Segment(boolean isStandStill, Speed startSpeed, Duration duration, Acceleration acceleration)
        implements Serializable
{

    /** */
    private static final long serialVersionUID = 20230421L;

    /**
     * Constructor.
     * @param startSpeed start speed.
     * @param duration the duration of the acceleration for this segment.
     * @param acceleration acceleration of this segment.
     */
    public Segment(final Speed startSpeed, final Duration duration, final Acceleration acceleration)
    {
        this(false, startSpeed, duration, acceleration);
    }

    /**
     * Constructor for stand-still segment.
     * @param duration duration.
     */
    private Segment(final Duration duration)
    {
        this(true, Speed.ZERO, duration, Acceleration.ZERO);
    }

    /**
     * Returns the speed at the end of the segment.
     * @return speed at the end of the segment.
     */
    public Speed endSpeed()
    {
        return speed(this.duration);
    }

    /**
     * Returns the total distance traveled during the segment.
     * @return total distance traveled during the segment.
     */
    public Length totalDistance()
    {
        return distance(this.duration);
    }

    /**
     * Returns the speed at the given duration relative to the start of the segment.
     * @param duration duration since start time of segment.
     * @return speed at the given duration relative to the start of the segment.
     */
    public Speed speed(final Duration duration)
    {
        Throw.when(duration.lt0(), IllegalArgumentException.class, "Duration must be positive.");
        Throw.when(duration.gt(this.duration), IllegalArgumentException.class, "Duration is beyond duration of segment.");
        if (this.isStandStill)
        {
            return Speed.ZERO;
        }
        return Speed.instantiateSI(this.startSpeed.si + duration.si * this.acceleration.si);
    }

    /**
     * Return the distance traveled at the given duration relative to the start of the segment.
     * @param duration duration since start time of segment.
     * @return distance traveled at the given duration relative to the start of the segment.
     */
    public Length distance(final Duration duration)
    {
        Throw.when(duration.lt0(), IllegalArgumentException.class, "Duration must be positive.");
        Throw.when(duration.gt(this.duration), IllegalArgumentException.class, "Duration is beyond duration of segment.");
        if (this.isStandStill)
        {
            return Length.ZERO;
        }
        return Length.instantiateSI(duration.si * this.startSpeed.si + .5 * this.acceleration.si * duration.si * duration.si);
    }

    /**
     * Returns the duration within the segment it takes to travel the distance from the start of the segment.
     * @param distance distance from the start of the segment.
     * @return duration within the segment it takes to travel the distance from the start of the segment.
     */
    public Duration durationAtDistance(final Length distance)
    {
        Throw.when(distance.lt0(), IllegalArgumentException.class, "Distance must be positive.");
        double[] solutions = Solver.solve(this.acceleration.si / 2, this.startSpeed.si, -distance.si);
        // Find the solution that occurs within our duration (there should be only one).
        for (double solution : solutions)
        {
            if (solution >= 0 && solution <= this.duration.si)
            {
                return new Duration(solution, DurationUnit.SI);
            }
        }
        return this.duration; // probably a rounding error
    }

    /**
     * Creates a stand-still segment.
     * @param duration duration.
     * @return segment with zero speed and acceleration.
     */
    public static Segment standStill(final Duration duration)
    {
        return new Segment(duration);
    }

}
