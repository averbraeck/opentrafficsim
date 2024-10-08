package org.opentrafficsim.core.gtu.plan.operational;

import java.io.Serializable;
import java.util.Arrays;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.draw.point.OrientedPoint2d;
import org.djutils.draw.point.Point2d;
import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
import org.djutils.immutablecollections.ImmutableList;
import org.opentrafficsim.core.geometry.OtsLine2d;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.gtu.RelativePosition;

/**
 * An Operational plan describes a path through the world with a speed profile that a GTU intends to follow. The OperationalPlan
 * can be updated or replaced at any time (including before it has been totally executed), for which a tactical planner is
 * responsible. The operational plan is implemented using segments of the movement (time, location, speed, acceleration) that
 * the GTU will use to plan its location and movement. Within an OperationalPlan the GTU cannot reverse direction along the path
 * of movement. This ensures that the timeAtDistance method will never have to select among several valid solutions.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class OperationalPlan implements Serializable
{
    /** */
    private static final long serialVersionUID = 20151114L;

    /** The path to follow from a certain time till a certain time. */
    private final OtsLine2d path;

    /** The absolute start time when we start executing the path. */
    private final Time startTime;

    /** The segments that make up the path with an acceleration, constant speed or deceleration profile. */
    private final Segments segments;

    /** The duration of executing the entire operational plan. */
    private final Duration totalDuration;

    /** The length of the entire operational plan. */
    private final Length totalLength;

    /** GTU for debugging purposes. */
    private final Gtu gtu;

    /**
     * An array of relative start times of each segment, expressed in the SI unit, where the last time is the overall ending
     * time of the operational plan.
     */
    private final double[] segmentStartDurations;

    /**
     * An array of relative start distances of each segment, expressed in the SI unit, where the last distance is the overall
     * ending distance of the operational plan.
     */
    private final double[] segmentStartDistances;

    /** The drifting speed. Speeds under this value will be cropped to zero. */
    public static final double DRIFTING_SPEED_SI = 1E-3;

    /**
     * Creates a stand-still plan at a point. A 1m path in the direction of the point is created.
     * @param gtu GTU.
     * @param point point.
     * @param startTime start time.
     * @param duration duration.
     * @return stand-still plan.
     */
    public static OperationalPlan standStill(final Gtu gtu, final OrientedPoint2d point, final Time startTime,
            final Duration duration)
    {
        Point2d p2 = new Point2d(point.x + Math.cos(point.getDirZ()), point.y + Math.sin(point.getDirZ()));
        OtsLine2d path = Try.assign(() -> new OtsLine2d(point, p2), "Unexpected geometry exception.");
        return new OperationalPlan(gtu, path, startTime, Segments.standStill(duration));
    }

    /**
     * Construct an operational plan. The plan will be as long as the minimum of the path or segments allow.
     * @param gtu the GTU for debugging purposes
     * @param path the path to follow from a certain time till a certain time. The path should have &lt;i&gt;at least&lt;/i&gt;
     *            the length
     * @param startTime the absolute start time when we start executing the path
     * @param segments the segments that make up the longitudinal dynamics
     */
    public OperationalPlan(final Gtu gtu, final OtsLine2d path, final Time startTime, final Segments segments)
    {
        this.gtu = gtu;
        this.startTime = startTime;
        this.segments = segments;
        this.segmentStartDurations = new double[this.segments.size() + 1];
        this.segmentStartDistances = new double[this.segments.size() + 1];

        Length pathLength = path.getTypedLength();
        Duration segmentsDuration = Duration.ZERO;
        Length segmentsLength = Length.ZERO;
        for (int i = 0; i < this.segments.size(); i++)
        {
            this.segmentStartDurations[i] = segmentsDuration.si;
            this.segmentStartDistances[i] = segmentsLength.si;
            Segment segment = this.segments.get(i);
            segmentsDuration = segmentsDuration.plus(segment.duration());
            segmentsLength = segmentsLength.plus(segment.totalDistance());
        }
        this.segmentStartDurations[this.segments.size()] = segmentsDuration.si;
        this.segmentStartDistances[this.segments.size()] = segmentsLength.si;

        // If segmentsLength == 0, we have a stand-still plan with non-zero length path. This path is required as a degenerate
        // OtsLine2d (with <2 points) is not allowed. In that case (in else) do not truncate path.
        if (segmentsLength.gt0() && pathLength.gt(segmentsLength))
        {
            this.totalDuration = segmentsDuration;
            this.totalLength = segmentsLength;
            this.path = Try.assign(() -> path.extract(0.0, this.totalLength.si), "Unexpected path truncation exception.");
        }
        else if (segmentsLength.gt(pathLength))
        {
            this.totalLength = pathLength;
            int i = this.segments.size();
            while (i > 1 && this.segmentStartDistances[i - 1] > pathLength.si)
            {
                i--;
            }
            double distanceInLast = this.totalLength.si - this.segmentStartDistances[i - 1];
            Duration timeInLast = this.segments.get(i - 1).durationAtDistance(Length.instantiateSI(distanceInLast));
            this.totalDuration = Duration.instantiateSI(timeInLast.si + this.segmentStartDurations[i - 1]);
            this.path = path;
        }
        else
        {
            this.totalDuration = segmentsDuration;
            this.totalLength = segmentsLength;
            this.path = path;
        }
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
     * Return the (absolute) start time of the operational plan.
     * @return startTime
     */
    public Time getStartTime()
    {
        return this.startTime;
    }

    /**
     * Return the start speed of the entire plan.
     * @return startSpeed
     */
    public Speed getStartSpeed()
    {
        return this.segments.get(0).startSpeed();
    }

    /**
     * Return the segments (parts with constant speed, acceleration or deceleration) of the operational plan.
     * @return ImmutableList&lt;OperationalPlan.Segment&gt;; segmentList
     */
    public ImmutableList<Segment> getOperationalPlanSegmentList()
    {
        return this.segments.getSegments();
    }

    /**
     * Return the time it will take to complete the entire operational plan.
     * @return the time it will take to complete the entire operational plan
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
     * Return the time it will take to complete the entire operational plan.
     * @return the time it will take to complete the entire operational plan
     */
    public Time getEndTime()
    {
        return this.startTime.plus(this.totalDuration);
    }

    /**
     * Provide the end location of this operational plan as a DirectedPoint.
     * @return the end location
     */
    public OrientedPoint2d getEndLocation()
    {
        return Try.assign(() -> this.path.getLocationPointFraction(Math.min(1.0, this.totalLength.si / this.path.getLength())),
                "Unexpected exception for path extraction till 1.0.");
    }

    /**
     * Returns the index of the segment covering the given time.
     * @param time time.
     * @return index of the segment covering the given time.
     */
    private int getSegment(final Time time)
    {
        double duration = time.si - this.startTime.si;
        int segment = 0;
        while (segment < this.segments.size() - 1 && this.segmentStartDurations[segment + 1] < duration)
        {
            segment++;
        }
        return segment;
    }

    /**
     * Return the time when the GTU will reach the given distance.
     * @param distance the distance to calculate the time for
     * @return the time it will take to have traveled the given distance
     */
    public final Time timeAtDistance(final Length distance)
    {
        Throw.when(getTotalLength().lt(distance), IllegalArgumentException.class, "Requesting %s from a plan with length %s",
                distance, getTotalLength());
        int segment = 0;
        while (segment < this.segments.size() && this.segmentStartDistances[segment + 1] < distance.si)
        {
            segment++;
        }
        Duration durationInSegment = this.segments.get(segment)
                .durationAtDistance(Length.instantiateSI(distance.si - this.segmentStartDistances[segment]));
        return Time.instantiateSI(this.startTime.si + this.segmentStartDurations[segment] + durationInSegment.si);
    }

    /**
     * Calculate the location after the given duration since the start of the plan.
     * @param duration the relative time to look for a location
     * @return the location after the given duration since the start of the plan.
     * @throws OperationalPlanException when the time is after the validity of the operational plan
     */
    public final OrientedPoint2d getLocation(final Duration duration) throws OperationalPlanException
    {
        return getLocation(this.startTime.plus(duration));
    }

    /**
     * Calculate the location at the given time.
     * @param time the absolute time to look for a location
     * @return the location at the given time.
     * @throws OperationalPlanException when the time is after the validity of the operational plan
     */
    public final OrientedPoint2d getLocation(final Time time) throws OperationalPlanException
    {
        Throw.when(time.lt(this.startTime), OperationalPlanException.class, "Requested time is before start time.");
        Throw.when(time.gt(this.getEndTime()), OperationalPlanException.class, "Requested time is beyond end time.");
        double fraction = this.totalLength.eq0() ? 0.0 : getTraveledDistance(time).si / this.totalLength.si;
        return this.path.getLocationPointFraction(fraction, 0.01);
    }

    /**
     * Calculate the location after the given duration since the start of the plan.
     * @param time the relative time to look for a location
     * @param pos relative position
     * @return the location after the given duration since the start of the plan.
     * @throws OperationalPlanException when the time is after the validity of the operational plan
     */
    public final OrientedPoint2d getLocation(final Time time, final RelativePosition pos) throws OperationalPlanException
    {
        double distanceSI = getTraveledDistance(time).si + pos.dx().si;
        return this.path.getLocationExtendedSI(distanceSI);
    }

    /**
     * Calculate the speed of the GTU after the given duration since the start of the plan.
     * @param time the relative time to look for a location
     * @return the location after the given duration since the start of the plan.
     * @throws OperationalPlanException when the time is after the validity of the operational plan
     */
    public final Speed getSpeed(final Duration time) throws OperationalPlanException
    {
        return getSpeed(time.plus(this.startTime));
    }

    /**
     * Calculate the speed of the GTU at the given time.
     * @param time the absolute time to look for a location
     * @return the location at the given time.
     * @throws OperationalPlanException when the time is after the validity of the operational plan
     */
    public final Speed getSpeed(final Time time) throws OperationalPlanException
    {
        int segment = getSegment(time);
        Duration durationInSegment = Duration.instantiateSI(time.si - this.startTime.si - this.segmentStartDurations[segment]);
        durationInSegment = fixDoublePrecision(durationInSegment, segment);
        return this.segments.get(segment).speed(durationInSegment);
    }

    /**
     * Maximize to segment duration in case of double precision issue.
     * @param durationInSegment duration in segment.
     * @param segment segment number.
     * @return duration in segment, maximized to segment duration if beyond within 1e-6.
     */
    private Duration fixDoublePrecision(final Duration durationInSegment, final int segment)
    {
        if (this.segments.get(segment).duration().lt(durationInSegment)
                && durationInSegment.si - this.segments.get(segment).duration().si < 1e-6)
        {
            return this.segments.get(segment).duration();
        }
        return durationInSegment;
    }

    /**
     * Calculate the acceleration of the GTU after the given duration since the start of the plan.
     * @param time the relative time to look for a location
     * @return the location after the given duration since the start of the plan.
     * @throws OperationalPlanException when the time is after the validity of the operational plan
     */
    public final Acceleration getAcceleration(final Duration time) throws OperationalPlanException
    {
        return getAcceleration(time.plus(this.startTime));
    }

    /**
     * Calculate the acceleration of the GTU at the given time.
     * @param time the absolute time to look for a location
     * @return the location at the given time.
     * @throws OperationalPlanException when the time is after the validity of the operational plan
     */
    public final Acceleration getAcceleration(final Time time) throws OperationalPlanException
    {
        return this.segments.get(getSegment(time)).acceleration();
    }

    /**
     * Calculate the distance traveled as part of this plan after the given duration since the start of the plan.
     * @param duration the relative time to calculate the traveled distance
     * @return the distance traveled as part of this plan after the given duration since the start of the plan.
     * @throws OperationalPlanException when the time is after the validity of the operational plan
     */
    public final Length getTraveledDistance(final Duration duration) throws OperationalPlanException
    {
        return getTraveledDistance(this.startTime.plus(duration));
    }

    /**
     * Calculate the distance traveled as part of this plan at the given absolute time.
     * @param time the absolute time to calculate the traveled distance for as part of this plan
     * @return the distance traveled as part of this plan at the given time
     * @throws OperationalPlanException when the time is after the validity of the operational plan
     */
    public Length getTraveledDistance(final Time time) throws OperationalPlanException
    {
        Throw.when(time.lt(this.getStartTime()), OperationalPlanException.class,
                "getTravelDistance exception: requested traveled distance before start of plan");
        Throw.when(time.si > this.getEndTime().si + 1e-6, OperationalPlanException.class,
                "getTravelDistance exception: requested traveled distance beyond end of plan");
        int segment = getSegment(time);
        Duration durationInSegment = Duration.instantiateSI(time.si - this.startTime.si - this.segmentStartDurations[segment]);
        durationInSegment = fixDoublePrecision(durationInSegment, segment);
        double distanceInSegment = this.segments.get(segment).distance(durationInSegment).si;
        return Length.instantiateSI(this.segmentStartDistances[segment] + distanceInSegment);
    }

    /**
     * Calculates when the GTU will be at the given point. The point does not need to be at the traveled path, as the point is
     * projected to the path at 90 degrees. The point may for instance be the end of a lane, which is crossed by a GTU possibly
     * during a lane change.
     * @param point point with angle, which will be projected to the path at 90 degrees
     * @param upstream true if the point is upstream of the path
     * @return time at point
     */
    public final Time timeAtPoint(final OrientedPoint2d point, final boolean upstream)
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
                return timeAtDistance(Length.instantiateSI(dist));
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
                        return Time.instantiateSI(Double.NaN);
                    }
                    return timeAtDistance(Length.instantiateSI(traveledDistanceAlongPath));
                }
                else
                {
                    traveledDistanceAlongPath += this.path.get(i).distance(this.path.get(i + 1));
                }
            }
        }
        catch (IndexOutOfBoundsException exception)
        {
            throw new RuntimeException("Index out of bounds on projection of point to path of operational plan", exception);
        }
        this.gtu.getSimulator().getLogger().always().error("timeAtPoint failed");
        return null;
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public String toString()
    {
        return "OperationalPlan [path=" + this.path + ", startTime=" + this.startTime + ", segments=" + this.segments
                + ", totalDuration=" + this.totalDuration + ", segmentStartTimesSI="
                + Arrays.toString(this.segmentStartDurations) + "]";
    }

}
