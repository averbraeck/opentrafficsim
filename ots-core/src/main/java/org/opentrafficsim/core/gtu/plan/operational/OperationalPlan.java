package org.opentrafficsim.core.gtu.plan.operational;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.draw.point.DirectedPoint2d;
import org.djutils.draw.point.Point2d;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.OtsRuntimeException;
import org.opentrafficsim.base.geometry.OtsLine2d;
import org.opentrafficsim.base.logger.Logger;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.gtu.RelativePosition;

/**
 * An Operational plan describes a path through the world with a speed profile that a GTU intends to follow. The OperationalPlan
 * can be updated or replaced at any time (including before it has been totally executed), for which a tactical planner is
 * responsible. The operational plan is implemented using segments of the movement (time, location, speed, acceleration) that
 * the GTU will use to plan its location and movement. Within an OperationalPlan the GTU cannot reverse direction along the path
 * of movement. This ensures that the timeAtDistance method will never have to select among several valid solutions.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Alexander Verbraeck
 * @author Peter Knoppers
 * @author Wouter Schakel
 */
public class OperationalPlan
{
    /** The path to follow from a certain time till a certain time. */
    private final OtsLine2d path;

    /** The absolute start time when we start executing the path. */
    private final Duration startTime;

    /** The absolute end time when we stop executing the path. */
    private final Duration endTime;

    /** The segments that make up the path with an acceleration, constant speed or deceleration profile. */
    private final Segments segments;

    /** The duration of executing the entire operational plan. */
    private final Duration totalDuration;

    /** The length of the entire operational plan. */
    private final Length totalLength;

    /** The drifting speed. Speeds under this value will be cropped to zero. */
    public static final Speed DRIFTING_SPEED = Speed.ofSI(0.001);

    /** The drifting speed. Accelerations under this value will be cropped to zero. */
    public static final Acceleration DRIFTING_ACCELERATION = Acceleration.ofSI(0.001);

    /** Minimum distance of an operational plan path; anything shorter will be truncated to 0. */
    public static final Length MINIMUM_CREDIBLE_PATH_LENGTH = Length.ofSI(0.001);

    // In about 25% if the cases the requested time is equal to the previous call to getTraveledDistance(Duration), so we cache.

    /** Time of latest returned travel distance. */
    private double lastTimeSI = Double.NaN;

    /** Latest returned travel distance. */
    private Length lastTraveledDistance;

    /**
     * Creates a stand-still plan at a point. A 1m path in the direction of the point is created.
     * @param gtu GTU.
     * @param point point.
     * @param startTime start time.
     * @param duration duration.
     * @return stand-still plan.
     */
    public static OperationalPlan standStill(final Gtu gtu, final DirectedPoint2d point, final Duration startTime,
            final Duration duration)
    {
        Point2d p2 = new Point2d(point.x + Math.cos(point.getDirZ()), point.y + Math.sin(point.getDirZ()));
        OtsLine2d path = new OtsLine2d(point, p2);
        return new OperationalPlan(gtu, path, startTime, new SegmentsSingle(Speed.ZERO, duration, Acceleration.ZERO));
    }

    /**
     * Construct an operational plan. The plan will be as long as the minimum of the path or segments allow.
     * @param gtu the GTU for debugging purposes
     * @param path the path to follow from a certain time till a certain time. The path should have &lt;i&gt;at least&lt;/i&gt;
     *            the length
     * @param startTime start time
     * @param segments the segments that make up the longitudinal dynamics
     */
    public OperationalPlan(final Gtu gtu, final OtsLine2d path, final Duration startTime, final Segments segments)
    {
        this.startTime = startTime;
        this.segments = segments;

        // If segmentsLength == 0, we have a stand-still plan with non-zero length path. This path is required as a degenerate
        // OtsLine2d (with <2 points) is not allowed. In that case (in else) do not truncate path.
        Length pathLength = path.getTypedLength();
        double segmentsLength = segments.getTotalLength().si;
        if (segmentsLength > 0.0 && pathLength.si >= segmentsLength)
        {
            // path longer than segments, truncate path
            this.totalDuration = segments.getTotalDuration();
            this.totalLength = segments.getTotalLength();
            this.path = path.extract(0.0, this.totalLength.si);
        }
        else if (segmentsLength > pathLength.si)
        {
            // segments longer than path, truncate total duration
            this.totalLength = pathLength;
            int i = this.segments.size();
            while (i > 1 && this.segments.getStartDistanceSI(i - 1) > pathLength.si)
            {
                i--;
            }
            double distanceInLast = this.totalLength.si - this.segments.getStartDistanceSI(i - 1);
            double timeInLast = this.segments.durationInSegment(i - 1, distanceInLast);
            this.totalDuration = Duration.ofSI(timeInLast + this.segments.getStartDistanceSI(i - 1));
            this.path = path;
        }
        else
        {
            this.totalDuration = segments.getTotalDuration();
            this.totalLength = Length.ZERO;
            this.path = path.extract(0.0, Math.min(pathLength.si, 1.0));
        }
        this.endTime = Duration.ofSI(this.startTime.si + this.totalDuration.si);
    }

    /**
     * Return the path that will be traveled. If the plan is a wait plan, the start point of the path is good; the end point of
     * the path is bogus (should only be used to determine the orientation of the GTU).
     * @return the path
     */
    public OtsLine2d getPath()
    {
        return this.path;
    }

    /**
     * Return the start time of the operational plan.
     * @return the start time of the operational plan
     */
    public Duration getStartTime()
    {
        return this.startTime;
    }

    /**
     * Return the end speed of the operational plan.
     * @return the end speed of the operational plan
     */
    public Speed getStartSpeed()
    {
        return this.segments.getStartSpeed();
    }

    /**
     * Return the duration it will take to complete the entire operational plan.
     * @return the duration it will take to complete the entire operational plan
     */
    public Duration getTotalDuration()
    {
        return this.totalDuration;
    }

    /**
     * Return the distance the entire operational plan will cover.
     * @return the distance of the entire operational plan
     */
    public Length getTotalLength()
    {
        return this.totalLength;
    }

    /**
     * Return the time at which the entire operational plan is completed.
     * @return the time at which the entire operational plan is completed
     */
    public Duration getEndTime()
    {
        return this.endTime;
    }

    /**
     * Provide the end location of this operational plan as a DirectedPoint.
     * @return the end location
     */
    public DirectedPoint2d getEndLocation()
    {
        return this.path.getLocationFraction(Math.min(1.0, this.totalLength.si / this.path.getLength()));
    }

    /**
     * Returns the segments.
     * @return segments
     */
    public Segments getSegments()
    {
        return this.segments;
    }

    /**
     * Returns the index of the segment covering the given time.
     * @param time simulation time
     * @return index of the segment covering the given time.
     */
    private int getSegment(final Duration time)
    {
        return this.segments.getSegment(time.si - this.startTime.si);
    }

    /**
     * Calculate the location after the given duration since the start of the plan.
     * @param duration duration relative to start of the plan
     * @return the location after the given duration since the start of the plan.
     * @throws IllegalArgumentException when the duration is after the validity of the operational plan
     */
    public DirectedPoint2d getLocationFromStart(final Duration duration)
    {
        return getLocation(this.startTime.plus(duration));
    }

    /**
     * Calculate the location at the given time.
     * @param time simulation time
     * @return the location at the given time.
     * @throws IllegalArgumentException when the time is after the validity of the operational plan
     */
    public DirectedPoint2d getLocation(final Duration time)
    {
        Throw.when(time.lt(this.startTime), IllegalArgumentException.class, "Requested time is before start time.");
        Throw.when(time.gt(this.getEndTime()), IllegalArgumentException.class, "Requested time is beyond end time.");
        double fraction = this.totalLength.eq0() ? 0.0 : getTraveledDistance(time).si / this.totalLength.si;
        return this.path.getLocationFraction(fraction, 0.01);
    }

    /**
     * Calculate the location after the given duration since the start of the plan.
     * @param time simulation time
     * @param pos relative position
     * @return the location after the given duration since the start of the plan.
     * @throws IllegalArgumentException when the time is after the validity of the operational plan
     */
    public DirectedPoint2d getLocation(final Duration time, final RelativePosition pos)
    {
        double distanceSI = getTraveledDistance(time).si + pos.dx().si;
        return this.path.getLocationExtended(distanceSI);
    }

    /**
     * Calculate the speed of the GTU after the given duration since the start of the plan.
     * @param duration duration relative to start of the plan
     * @return the location after the given duration since the start of the plan.
     * @throws IllegalArgumentException when the duration is after the validity of the operational plan
     */
    public Speed getSpeedFromStart(final Duration duration)
    {
        return getSpeed(duration.plus(this.startTime));
    }

    /**
     * Calculate the speed of the GTU at the given time.
     * @param time simulation time
     * @return the location at the given time.
     * @throws IllegalArgumentException when the time is after the validity of the operational plan
     */
    public Speed getSpeed(final Duration time)
    {
        if (time.si == this.startTime.si)
        {
            return this.segments.getStartSpeed();
        }
        if (time.si == this.endTime.si)
        {
            return this.segments.getEndSpeed();
        }
        int segment = getSegment(time);
        double durationInSegment = time.si - this.startTime.si - this.segments.getStartDurationSI(segment);
        durationInSegment = fixDoublePrecision(segment, durationInSegment);
        return Speed.ofSI(this.segments.getSpeedSI(segment, durationInSegment));
    }

    /**
     * Maximize to segment duration in case of double precision issue.
     * @param segment segment number.
     * @param durationInSegment duration in segment.
     * @return duration in segment, maximized to segment duration if beyond within 1e-6.
     */
    private double fixDoublePrecision(final int segment, final double durationInSegment)
    {
        double segmentDuration = this.segments.getDurationSI(segment);
        if (segmentDuration < durationInSegment && durationInSegment - segmentDuration < 1e-6)
        {
            return segmentDuration;
        }
        return durationInSegment;
    }

    /**
     * Calculate the acceleration of the GTU after the given duration since the start of the plan.
     * @param duration duration relative to start of the plan
     * @return the location after the given duration since the start of the plan.
     * @throws IllegalArgumentException when the time is after the validity of the operational plan
     */
    public Acceleration getAccelerationFromStart(final Duration duration)
    {
        return getAcceleration(duration.plus(this.startTime));
    }

    /**
     * Calculate the acceleration of the GTU at the given time.
     * @param time simulation time
     * @return the location at the given time.
     * @throws IllegalArgumentException when the time is after the validity of the operational plan
     */
    public Acceleration getAcceleration(final Duration time)
    {
        return Acceleration.ofSI(this.segments.getAccelerationSI(getSegment(time)));
    }

    /**
     * Calculate the distance traveled as part of this plan after the given duration since the start of the plan.
     * @param duration duration relative to start of the plan
     * @return the distance traveled as part of this plan after the given duration since the start of the plan.
     * @throws IllegalArgumentException when the duration is after the validity of the operational plan
     */
    public Length getTraveledDistanceFromStart(final Duration duration)
    {
        return getTraveledDistance(this.startTime.plus(duration));
    }

    /**
     * Calculate the distance traveled as part of this plan at the given absolute time.
     * @param time simulation time
     * @return the distance traveled as part of this plan at the given time
     * @throws IllegalArgumentException when the time is after the validity of the operational plan
     */
    public Length getTraveledDistance(final Duration time)
    {
        Throw.when(time.si < this.startTime.si, IllegalArgumentException.class,
                "getTravelDistance exception: requested traveled distance before start of plan");
        Throw.when(time.si > this.endTime.si + 1e-6, IllegalArgumentException.class,
                "getTravelDistance exception: requested traveled distance beyond end of plan");
        if (time.si == this.startTime.si)
        {
            return Length.ZERO;
        }
        if (time.si == this.endTime.si)
        {
            return this.totalLength;
        }
        if (time.si == this.lastTimeSI)
        {
            return this.lastTraveledDistance;
        }
        int segment = getSegment(time);
        double durationInSegment = time.si - this.startTime.si - this.segments.getStartDurationSI(segment);
        durationInSegment = fixDoublePrecision(segment, durationInSegment);
        double distanceInSegment = this.segments.getDistanceSI(segment, durationInSegment);
        this.lastTraveledDistance = Length.ofSI(this.segments.getStartDistanceSI(segment) + distanceInSegment);
        this.lastTimeSI = time.si;
        return this.lastTraveledDistance;
    }

    /**
     * Calculates the duration at which the given distance is traveled.
     * @param traveledDistance traveled distance
     * @return duration since start of the plan at which the given distance is traveled
     * @throws IllegalArgumentException if the traveled distance is beyond the plan length
     */
    public Duration getDurationAtDistance(final Length traveledDistance)
    {
        Throw.when(traveledDistance.si > this.totalLength.si, IllegalArgumentException.class,
                "Traveled distance for requested time is beyond plan length.");
        int segment = 0;
        while (segment < this.segments.size() - 1 && this.segments.getStartDistanceSI(segment + 1) < traveledDistance.si)
        {
            segment++;
        }
        double dt = this.segments.durationInSegment(segment, traveledDistance.si - this.segments.getStartDistanceSI(segment));
        return Duration.ofSI(this.segments.getStartDurationSI(segment) + dt);
    }

    /**
     * Calculates the time at which the given distance is traveled.
     * @param traveledDistance traveled distance
     * @return simulation time at which the given distance is traveled
     * @throws IllegalArgumentException if the traveled distance is beyond the plan length
     */
    public Duration getTimeAtDistance(final Length traveledDistance)
    {
        return this.startTime.plus(getDurationAtDistance(traveledDistance));
    }

    /**
     * Calculates when the GTU will be at the given point. The point does not need to be at the traveled path, as the point is
     * projected to the path at 90 degrees. The point may for instance be the end of a lane, which is crossed by a GTU possibly
     * during a lane change.
     * @param point point with angle, which will be projected to the path at 90 degrees
     * @param upstream true if the point is upstream of the path
     * @return simulation time at point
     */
    public Duration timeAtPoint(final DirectedPoint2d point, final boolean upstream)
    {
        Point2d p1 = point;
        // point at 90 degrees
        Point2d p2 = new Point2d(point.x - Math.sin(point.getDirZ()), point.y + Math.cos(point.getDirZ()));
        double traveledDistanceAlongPath = 0.0;
        try
        {
            if (upstream)
            {
                Point2d p = Point2d.intersectionOfLines(this.path.get(0), this.path.get(1), p1, p2);
                double dist = traveledDistanceAlongPath - this.path.get(0).distance(p);
                dist = dist >= 0.0 ? dist : 0.0; // negative in case of a gap
                return getTimeAtDistance(Length.ofSI(dist));
            }
            for (int i = 0; i < this.path.size() - 1; i++)
            {
                Point2d prevPoint = this.path.get(i);
                Point2d nextPoint = this.path.get(i + 1);
                Point2d p = Point2d.intersectionOfLines(prevPoint, nextPoint, p1, p2);
                if (p == null)
                {
                    // point too close, check next section
                    continue;
                }
                boolean onSegment =
                        prevPoint.distance(nextPoint) + 2e-5 > Math.max(prevPoint.distance(p), nextPoint.distance(p));
                if (p != null // on segment, or last segment
                        && (i == this.path.size() - 2 || onSegment))
                {
                    // point is on the line
                    traveledDistanceAlongPath += this.path.get(i).distance(p);
                    if (traveledDistanceAlongPath > this.path.getLength())
                    {
                        return Duration.NaN;
                    }
                    return getTimeAtDistance(Length.ofSI(traveledDistanceAlongPath));
                }
                else
                {
                    traveledDistanceAlongPath += this.path.get(i).distance(this.path.get(i + 1));
                }
            }
        }
        catch (IndexOutOfBoundsException exception)
        {
            throw new OtsRuntimeException("Index out of bounds on projection of point to path of operational plan", exception);
        }
        Logger.ots().error("timeAtPoint failed");
        return null;
    }

    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.segments == null) ? 0 : this.segments.hashCode());
        result = prime * result + ((this.path == null) ? 0 : this.path.hashCode());
        result = prime * result + ((this.startTime == null) ? 0 : this.startTime.hashCode());
        return result;
    }

    @SuppressWarnings({"checkstyle:needbraces", "checkstyle:designforextension"})
    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OperationalPlan other = (OperationalPlan) obj;
        if (this.segments == null)
        {
            if (other.segments != null)
                return false;
        }
        else if (!this.segments.equals(other.segments))
            return false;
        if (this.path == null)
        {
            if (other.path != null)
                return false;
        }
        else if (!this.path.equals(other.path))
            return false;
        if (this.startTime == null)
        {
            if (other.startTime != null)
                return false;
        }
        else if (!this.startTime.equals(other.startTime))
            return false;
        return true;
    }

    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public String toString()
    {
        return "OperationalPlan [path=" + this.path + ", startTime=" + this.startTime + ", segments=" + this.segments
                + ", totalDuration=" + this.totalDuration + "]";
    }

}
