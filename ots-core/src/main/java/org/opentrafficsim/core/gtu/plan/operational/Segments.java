package org.opentrafficsim.core.gtu.plan.operational;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.math.Solver;

/**
 * Segments describe the longitudinal kinematics of an operational plan. Each segment has a constant acceleration. All durations
 * within this class are either relative to the start of the first segment, or relative to the start of a specific segment.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.<br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * @author Alexander Verbraeck
 * @author Peter Knoppers
 * @author Wouter Schakel
 */
// except for fixed end-points this interface uses doubles instead of DJUNIT scalars as this class is heavily used
public interface Segments
{

    /**
     * Returns the number of segments.
     * @return number of segments.
     */
    int size();

    /**
     * Returns the start speed of the segments.
     * @return the start speed of the segments
     */
    Speed getStartSpeed();

    /**
     * Returns the end speed of the segments.
     * @return the end speed of the segments
     */
    Speed getEndSpeed();

    /**
     * Returns the total duration of the segments.
     * @return the total duration of the segments
     */
    Duration getTotalDuration();

    /**
     * Returns the total length of the segments.
     * @return the total length of the segments
     */
    Length getTotalLength();

    /**
     * Returns whether this is a stand-still plan.
     * @return whether this is a stand-still plan
     */
    boolean isStandStill();

    /**
     * Returns the index of the segment covering the given duration since start of segments.
     * @param duration duration since start of segments
     * @return index of the segment covering the given duration since start of segments
     */
    int getSegment(double duration);

    /**
     * Returns the duration within the segment it takes to cover the given distance.
     * @param segment segment
     * @param distanceInSegment distance
     * @return the duration within the segment it takes to cover the given distance
     */
    default double durationInSegment(final int segment, final double distanceInSegment)
    {
        double[] solutions = Solver.solve(getAccelerationSI(segment) / 2, getStartSpeedSI(segment), -distanceInSegment);
        // Find the solution that occurs within our duration (there should be only one).
        double segmentDuration = getDurationSI(segment);
        for (double solution : solutions)
        {
            if (solution >= 0 && solution <= segmentDuration)
            {
                return solution;
            }
        }
        return segmentDuration; // probably a rounding error
    }

    /**
     * Returns the start speed of the segment.
     * @param segment segment
     * @return the start speed of the segment
     */
    double getStartSpeedSI(int segment);

    /**
     * Returns the acceleration of the segment.
     * @param segment segment
     * @return the acceleration of the segment
     */
    double getAccelerationSI(int segment);

    /**
     * Returns the start duration of the segment relative to the first segment start.
     * @param segment segment
     * @return the start duration of the segment relative to the first segment start
     */
    double getStartDurationSI(int segment);

    /**
     * Returns the start distance of the segment relative to the first segment start.
     * @param segment segment
     * @return the start distance of the segment relative to the first segment start
     */
    double getStartDistanceSI(int segment);

    /**
     * Returns the duration of the segment.
     * @param segment segment
     * @return the duration of the segment
     */
    double getDurationSI(int segment);

    /**
     * Returns the speed at the given duration within the given segment.
     * @param segment segment
     * @param durationInSegment duration within the segment
     * @return the speed at the given duration within the given segment
     */
    double getSpeedSI(int segment, double durationInSegment);

    /**
     * Returns the distance at the given duration within the given segment.
     * @param segment segment
     * @param durationInSegment duration within the segment
     * @return the distance at the given duration within the given segment
     */
    double getDistanceSI(int segment, double durationInSegment);

    /**
     * Instantiates segments with a given start speed. If the acceleration reaches stand-still during the provided duration, two
     * segments are created. The first with given negative acceleration until stand-still, and a second stand-still segment for
     * the remainder of the duration.
     * @param startSpeed start speed of the first segment
     * @param duration segment duration
     * @param acceleration segment acceleration
     * @return segments
     */
    static Segments of(final Speed startSpeed, final Duration duration, final Acceleration acceleration)
    {
        if (acceleration.lt0() && startSpeed.si / -acceleration.si < duration.si)
        {
            double durationTillStandStill = startSpeed.si / -acceleration.si;
            double length = startSpeed.si * durationTillStandStill
                    + .5 * acceleration.si * durationTillStandStill * durationTillStandStill;
            if (length < OperationalPlan.MINIMUM_CREDIBLE_PATH_LENGTH.si && startSpeed.si < OperationalPlan.DRIFTING_SPEED.si
                    && acceleration.si < OperationalPlan.DRIFTING_ACCELERATION.si)
            {
                return new SegmentsSingle(Speed.ZERO, duration, Acceleration.ZERO);
            }
            return new SegmentsMulti(new double[] {startSpeed.si, 0.0},
                    new double[] {durationTillStandStill, duration.si - durationTillStandStill},
                    new double[] {acceleration.si, 0.0});
        }
        else
        {
            double length = startSpeed.si * duration.si + .5 * acceleration.si * duration.si * duration.si;
            if (length < OperationalPlan.MINIMUM_CREDIBLE_PATH_LENGTH.si && startSpeed.si < OperationalPlan.DRIFTING_SPEED.si
                    && acceleration.si < OperationalPlan.DRIFTING_ACCELERATION.si)
            {
                return new SegmentsSingle(Speed.ZERO, duration, Acceleration.ZERO);
            }
            return new SegmentsSingle(startSpeed, duration, acceleration);
        }
    }

}
