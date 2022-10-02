package org.opentrafficsim.core.gtu.plan.operational;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.exceptions.Throw;
import org.djutils.logger.CategoryLogger;
import org.opentrafficsim.core.geometry.DirectedPoint;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.geometry.OtsLine3D;
import org.opentrafficsim.core.geometry.OtsPoint3D;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.math.Solver;

/**
 * An Operational plan describes a path through the world with a speed profile that a GTU intends to follow. The OperationalPlan
 * can be updated or replaced at any time (including before it has been totally executed), for which a tactical planner is
 * responsible. The operational plan is implemented using segments of the movement (time, location, speed, acceleration) that
 * the GTU will use to plan its location and movement. Within an OperationalPlan the GTU cannot reverse direction along the path
 * of movement. This ensures that the timeAtDistance method will never have to select among several valid solutions.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class OperationalPlan implements Serializable
{
    /** */
    private static final long serialVersionUID = 20151114L;

    /** The path to follow from a certain time till a certain time. */
    private final OtsLine3D path;

    /** The absolute start time when we start executing the path. */
    private final Time startTime;

    /** The GTU speed when we start executing the path. */
    private final Speed startSpeed;

    /** The segments that make up the path with an acceleration, constant speed or deceleration profile. */
    private final List<OperationalPlan.Segment> operationalPlanSegmentList;

    /** The duration of executing the entire operational plan. */
    private final Duration totalDuration;

    /** The length of the entire operational plan. */
    private final Length totalLength;

    /** The speed at the end of the operational plan. */
    private final Speed endSpeed;

    /** Is this operational plan a wait plan? */
    private final boolean waitPlan;

    /** GTU for debugging purposes. */
    private final Gtu gtu;

    /**
     * An array of relative start times of each segment, expressed in the SI unit, where the last time is the overall ending
     * time of the operational plan.
     */
    private final double[] segmentStartTimesRelSI;

    /**
     * The maximum difference in the length of the path and the calculated driven distance implied by the segment list. The same
     * constant is also used as a maximum between speeds of segments that should align in terms of speed.
     */
    static final double MAX_DELTA_SI = 1E-6;

    /** The drifting speed. Speeds under this value will be cropped to zero. */
    public static final double DRIFTING_SPEED_SI = 1E-3;

    /**
     * Construct an operational plan.
     * @param gtu Gtu; the GTU for debugging purposes
     * @param path OTSLine3D; the path to follow from a certain time till a certain time. The path should have &lt;i&gt;at
     *            least&lt;/i&gt; the length
     * @param startTime Time; the absolute start time when we start executing the path
     * @param startSpeed Speed; the GTU speed when we start executing the path
     * @param operationalPlanSegmentList List&lt;Segment&gt;; the segments that make up the path with an acceleration, constant
     *            speed or deceleration profile
     * @throws OperationalPlanException when the path is too short for the operation
     */
    public OperationalPlan(final Gtu gtu, final OtsLine3D path, final Time startTime, final Speed startSpeed,
            final List<Segment> operationalPlanSegmentList) throws OperationalPlanException
    {

        this.waitPlan = false;
        this.gtu = gtu;
        this.startTime = startTime;
        this.startSpeed = startSpeed;
        this.operationalPlanSegmentList = operationalPlanSegmentList;
        this.segmentStartTimesRelSI = new double[this.operationalPlanSegmentList.size() + 1];

        // check the driven distance of the segments
        Speed v0 = this.startSpeed;
        double distanceSI = 0.0;
        double durationSI = 0.0;
        for (int i = 0; i < this.operationalPlanSegmentList.size(); i++)
        {
            Segment segment = this.operationalPlanSegmentList.get(i);
            if (Math.abs(v0.si) < DRIFTING_SPEED_SI && segment.accelerationSI(0.0) == 0.0)
            {
                v0 = Speed.ZERO;
            }
            segment.setV0(v0);
            this.segmentStartTimesRelSI[i] = durationSI;
            distanceSI += segment.distanceSI();
            v0 = segment.endSpeed();
            durationSI += segment.getDuration().si;
        }
        this.segmentStartTimesRelSI[this.segmentStartTimesRelSI.length - 1] = durationSI;
        try
        {
            this.path = path.extract(0.0, Math.min(distanceSI, path.getLengthSI()));
        }
        catch (OtsGeometryException exception)
        {
            throw new OperationalPlanException(exception);
        }
        this.totalDuration = new Duration(durationSI, DurationUnit.SI);
        this.totalLength = new Length(distanceSI, LengthUnit.SI);
        this.endSpeed = v0;

        // double pathDistanceDeviation = Math.abs(this.totalLength.si - this.path.getLengthSI()) / this.totalLength.si;
        // if (pathDistanceDeviation < -0.01 || pathDistanceDeviation > 0.01)
        // {
        // System.err.println("path length and driven distance deviate more than 1% for operationalPlan: " + this);
        // }
    }

    /**
     * Build a plan where the GTU will wait for a certain time.
     * @param gtu Gtu; the GTU for debugging purposes
     * @param waitPoint DirectedPoint; the point at which the GTU will wait
     * @param startTime Time; the current time or a time in the future when the plan should start
     * @param duration Duration; the waiting time
     * @throws OperationalPlanException when construction of a waiting path fails
     */
    public OperationalPlan(final Gtu gtu, final DirectedPoint waitPoint, final Time startTime, final Duration duration)
            throws OperationalPlanException
    {
        this.waitPlan = true;
        this.gtu = gtu;
        this.startTime = startTime;
        this.startSpeed = Speed.ZERO;
        this.endSpeed = Speed.ZERO;
        this.totalDuration = duration;
        this.totalLength = Length.ZERO;

        // make a path
        OtsPoint3D p2 = new OtsPoint3D(waitPoint.x + Math.cos(waitPoint.getRotZ()), waitPoint.y + Math.sin(waitPoint.getRotZ()),
                waitPoint.z);
        try
        {
            this.path = new OtsLine3D(new OtsPoint3D(waitPoint), p2);
        }
        catch (OtsGeometryException exception)
        {
            throw new OperationalPlanException(exception);
        }

        this.operationalPlanSegmentList = new ArrayList<>();
        Segment segment = new SpeedSegment(duration);
        segment.setV0(Speed.ZERO);
        this.operationalPlanSegmentList.add(segment);
        this.segmentStartTimesRelSI = new double[2];
        this.segmentStartTimesRelSI[0] = 0.0;
        this.segmentStartTimesRelSI[1] = duration.si;
    }

    /**
     * Return the path that will be traveled. If the plan is a wait plan, the start point of the path is good; the end point of
     * the path is bogus (should only be used to determine the orientation of the GTU).
     * @return the path
     */
    public final OtsLine3D getPath()
    {
        return this.path;
    }

    /**
     * Return the (absolute) start time of the operational plan.
     * @return startTime
     */
    public final Time getStartTime()
    {
        return this.startTime;
    }

    /**
     * Return the start speed of the entire plan.
     * @return startSpeed
     */
    public final Speed getStartSpeed()
    {
        return this.startSpeed;
    }

    /**
     * @return the end speed when completing the entire plan.
     */
    public final Speed getEndSpeed()
    {
        return this.endSpeed;
    }

    /**
     * Return the segments (parts with constant speed, acceleration or deceleration) of the operational plan. <br>
     * The caller MUST NOT MODIFY the returned object.
     * @return operationalPlanSegmentList
     */
    public final List<OperationalPlan.Segment> getOperationalPlanSegmentList()
    {
        return this.operationalPlanSegmentList;
    }

    /**
     * Return the time it will take to complete the entire operational plan.
     * @return the time it will take to complete the entire operational plan
     */
    public final Duration getTotalDuration()
    {
        return this.totalDuration;
    }

    /**
     * Return the distance the entire operational plan will cover.
     * @return the distance of the entire operational plan
     */
    public final Length getTotalLength()
    {
        return this.totalLength;
    }

    /**
     * Return the time it will take to complete the entire operational plan.
     * @return the time it will take to complete the entire operational plan
     */
    public final Time getEndTime()
    {
        return this.startTime.plus(this.totalDuration);
    }

    /**
     * Provide the end location of this operational plan as a DirectedPoint.
     * @return the end location
     */
    public final DirectedPoint getEndLocation()
    {
        try
        {
            if (this.waitPlan)
            {
                return this.path.getLocationFraction(0.0); // no move...
            }
            else
            {
                return this.path.getLocationFraction(1.0);
            }
        }
        catch (OtsGeometryException exception)
        {
            // should not happen -- only for fractions less than 0.0 or larger than 1.0.
            throw new RuntimeException(exception);
        }
    }

    /**
     * Store a Segment and the progress within that segment in one Object.
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. <br>
     * All rights reserved. <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     */
    private class SegmentProgress implements Serializable
    {
        /** */
        private static final long serialVersionUID = 20160000L;

        /** Active Segment. */
        private final Segment segment;

        /** Start time of the Segment. */
        private final Time segmentStartTime;

        /** Position on the path of the plan. */
        private final Length segmentStartPosition;

        /**
         * Construct a new SegmentProgress object.
         * @param segment Segment; the Segment
         * @param segmentStartTime Time; the start time of the Segment
         * @param segmentStartPosition Length; the position of the start of the segment on the path of the OperationalPlan
         */
        SegmentProgress(final Segment segment, final Time segmentStartTime, final Length segmentStartPosition)
        {
            this.segment = segment;
            this.segmentStartTime = segmentStartTime;
            this.segmentStartPosition = segmentStartPosition;
        }

        /**
         * Retrieve the Segment.
         * @return Segment
         */
        public final Segment getSegment()
        {
            return this.segment;
        }

        /**
         * Retrieve the start time of the Segment.
         * @return Time; the start time of the Segment
         */
        public final Time getSegmentStartTime()
        {
            return this.segmentStartTime;
        }

        /**
         * Retrieve the fractionalPosition at the start of the Segment.
         * @return double; the fractional position at the start of the Segment
         */
        public final Length getSegmentStartPosition()
        {
            return this.segmentStartPosition;
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return String.format("SegmentProgress segment=%s startpos.rel=%s starttime.abs=%s", this.segment,
                    this.segmentStartPosition, this.segmentStartTime);
        }
    }

    /**
     * Find the Segment and the progress within that Segment at a specified time.
     * @param time Time; the time
     * @return SegmentProgress; the Segment and progress within that segment, or null when no Segment applies to the specified
     *         time
     * @throws OperationalPlanException when SegmentProgress cannot be determined
     */
    private SegmentProgress getSegmentProgress(final Time time) throws OperationalPlanException
    {
        if (time.lt(this.startTime))
        {
            throw new OperationalPlanException(
                    this.gtu + ", t = " + time + "SegmentProgress cannot be determined for time before startTime "
                            + getStartTime() + " of this OperationalPlan");
        }
        double cumulativeDistance = 0;
        for (int i = 0; i < this.segmentStartTimesRelSI.length - 1; i++)
        {
            if (this.startTime.si + this.segmentStartTimesRelSI[i + 1] >= time.si)
            {
                return new SegmentProgress(this.operationalPlanSegmentList.get(i),
                        new Time(this.startTime.si + this.segmentStartTimesRelSI[i], TimeUnit.DEFAULT),
                        new Length(cumulativeDistance, LengthUnit.SI));
            }
            cumulativeDistance += this.operationalPlanSegmentList.get(i).distanceSI();
        }
        throw new OperationalPlanException(this.gtu + ", t = " + time
                + " SegmentProgress cannot be determined for time after endTime " + getEndTime() + " of this OperationalPlan");
    }

    /**
     * Return the time when the GTU will reach the given distance.
     * @param distance Length; the distance to calculate the time for
     * @return the time it will take to have traveled the given distance
     */
    public final Time timeAtDistance(final Length distance)
    {
        Throw.when(getTotalLength().lt(distance), RuntimeException.class, "Requesting %s from a plan with length %s", distance,
                getTotalLength());
        double remainingDistanceSI = distance.si;
        double timeAtStartOfSegment = this.startTime.si;
        Iterator<Segment> it = this.operationalPlanSegmentList.iterator();
        while (it.hasNext() && remainingDistanceSI >= 0.0)
        {
            Segment segment = it.next();
            double distanceOfSegment = segment.distanceSI();
            if (distanceOfSegment > remainingDistanceSI)
            {
                return new Time(timeAtStartOfSegment + segment.timeAtDistance(Length.instantiateSI(remainingDistanceSI)).si,
                        TimeUnit.DEFAULT);
            }
            remainingDistanceSI -= distanceOfSegment;
            timeAtStartOfSegment += segment.getDurationSI();
        }
        return new Time(Double.NaN, TimeUnit.DEFAULT);
    }

    /**
     * Calculate the location at the given time.
     * @param time Time; the absolute time to look for a location
     * @return the location at the given time.
     * @throws OperationalPlanException when the time is after the validity of the operational plan
     */
    public final DirectedPoint getLocation(final Time time) throws OperationalPlanException
    {
        SegmentProgress sp = getSegmentProgress(time);
        Segment segment = sp.getSegment();
        Duration deltaT = time.minus(sp.getSegmentStartTime());
        double distanceTraveledInSegment = segment.distanceSI(deltaT.si);
        double startDistance = sp.getSegmentStartPosition().si;
        double fraction = (startDistance + distanceTraveledInSegment) / this.path.getLengthSI();
        // if (this.gtu.getId().equals("2"))
        // {
        // System.out.println(time.si + "\t" + fraction + "\t" + this.gtu.getOdometer().si + "\t" + startDistance + "\t"
        // + distanceTraveledInSegment + "\t " + this.path.getLengthSI() + "\t" + deltaT.si + "\t"
        // + System.nanoTime());
        // }
        DirectedPoint p;
        try
        {
            p = this.path.getLocationFraction(fraction, 0.01);
        }
        catch (OtsGeometryException exception)
        {
            this.gtu.getSimulator().getLogger().always().error("OperationalPlan.getLocation(): " + exception.getMessage());
            DirectedPoint q = this.path.getLocationFractionExtended(fraction);
            p = new DirectedPoint(q.x, q.y, q.z + 0.001);
        }
        return p;
    }

    /**
     * Calculate the speed of the GTU after the given duration since the start of the plan.
     * @param time Duration; the relative time to look for a location
     * @return the location after the given duration since the start of the plan.
     * @throws OperationalPlanException when the time is after the validity of the operational plan
     */
    public final Speed getSpeed(final Duration time) throws OperationalPlanException
    {
        return getSpeed(time.plus(this.startTime));
    }

    /**
     * Calculate the speed of the GTU at the given time.
     * @param time Time; the absolute time to look for a location
     * @return the location at the given time.
     * @throws OperationalPlanException when the time is after the validity of the operational plan
     */
    public final Speed getSpeed(final Time time) throws OperationalPlanException
    {
        SegmentProgress sp = getSegmentProgress(time);
        return new Speed(sp.getSegment().speedSI(time.minus(sp.getSegmentStartTime()).si), SpeedUnit.SI);
    }

    /**
     * Calculate the acceleration of the GTU after the given duration since the start of the plan.
     * @param time Duration; the relative time to look for a location
     * @return the location after the given duration since the start of the plan.
     * @throws OperationalPlanException when the time is after the validity of the operational plan
     */
    public final Acceleration getAcceleration(final Duration time) throws OperationalPlanException
    {
        return getAcceleration(time.plus(this.startTime));
    }

    /**
     * Calculate the acceleration of the GTU at the given time.
     * @param time Time; the absolute time to look for a location
     * @return the location at the given time.
     * @throws OperationalPlanException when the time is after the validity of the operational plan
     */
    public final Acceleration getAcceleration(final Time time) throws OperationalPlanException
    {
        SegmentProgress sp = getSegmentProgress(time);
        return new Acceleration(sp.getSegment().accelerationSI(time.minus(sp.getSegmentStartTime()).si), AccelerationUnit.SI);
    }

    /**
     * Calculate the location after the given duration since the start of the plan.
     * @param time Duration; the relative time to look for a location
     * @return the location after the given duration since the start of the plan.
     * @throws OperationalPlanException when the time is after the validity of the operational plan
     */
    public final DirectedPoint getLocation(final Duration time) throws OperationalPlanException
    {
        double distanceSI = getTraveledDistanceSI(time);
        return this.path.getLocationExtendedSI(distanceSI);
    }

    /**
     * Calculate the location after the given duration since the start of the plan.
     * @param time Time; the relative time to look for a location
     * @param pos RelativePosition; relative position
     * @return the location after the given duration since the start of the plan.
     * @throws OperationalPlanException when the time is after the validity of the operational plan
     */
    public final DirectedPoint getLocation(final Time time, final RelativePosition pos) throws OperationalPlanException
    {
        double distanceSI = getTraveledDistanceSI(time) + pos.getDx().si;
        return this.path.getLocationExtendedSI(distanceSI);
    }

    /**
     * Calculate the distance traveled as part of this plan after the given duration since the start of the plan. This method
     * returns the traveled distance as a double in SI units.
     * @param duration Duration; the relative time to calculate the traveled distance
     * @return the distance traveled as part of this plan after the given duration since the start of the plan.
     * @throws OperationalPlanException when the time is after the validity of the operational plan
     */
    public final double getTraveledDistanceSI(final Duration duration) throws OperationalPlanException
    {
        return getTraveledDistanceSI(this.startTime.plus(duration));
    }

    /**
     * Calculate the distance traveled as part of this plan after the given duration since the start of the plan.
     * @param duration Duration; the relative time to calculate the traveled distance
     * @return the distance traveled as part of this plan after the given duration since the start of the plan.
     * @throws OperationalPlanException when the time is after the validity of the operational plan
     */
    public final Length getTraveledDistance(final Duration duration) throws OperationalPlanException
    {
        return new Length(getTraveledDistanceSI(duration), LengthUnit.SI);
    }

    /**
     * Calculate the distance traveled as part of this plan at the given absolute time. This method returns the traveled
     * distance as a double in SI units.
     * @param time Time; the absolute time to calculate the traveled distance for as part of this plan
     * @return the distance traveled as part of this plan at the given time
     * @throws OperationalPlanException when the time is after the validity of the operational plan
     */
    public final double getTraveledDistanceSI(final Time time) throws OperationalPlanException
    {
        Throw.when(time.lt(this.getStartTime()), OperationalPlanException.class,
                "getTravelDistance exception: requested traveled distance before start of plan");
        Throw.when(time.si > this.getEndTime().si + 1e-6, OperationalPlanException.class,
                "getTravelDistance exception: requested traveled distance beyond end of plan");
        if (this.operationalPlanSegmentList.size() == 1)
        {
            return this.operationalPlanSegmentList.get(0).distanceSI(time.si - this.startTime.si);
        }
        SegmentProgress sp = getSegmentProgress(time);
        return sp.getSegmentStartPosition().si + sp.getSegment().distanceSI(time.minus(sp.getSegmentStartTime()).si);
    }

    /**
     * Calculates when the GTU will be at the given point. The point does not need to be at the traveled path, as the point is
     * projected to the path at 90 degrees. The point may for instance be the end of a lane, which is crossed by a GTU possibly
     * during a lane change.
     * @param point DirectedPoint; point with angle, which will be projected to the path at 90 degrees
     * @param upstream boolean; true if the point is upstream of the path
     * @return Time; time at point
     */
    public final Time timeAtPoint(final DirectedPoint point, final boolean upstream)
    {
        OtsPoint3D p1 = new OtsPoint3D(point);
        // point at 90 degrees
        OtsPoint3D p2 = new OtsPoint3D(point.x - Math.sin(point.getRotZ()), point.y + Math.cos(point.getRotZ()), point.z);
        double traveledDistanceAlongPath = 0.0;
        try
        {
            if (upstream)
            {
                OtsPoint3D p = OtsPoint3D.intersectionOfLines(this.path.get(0), this.path.get(1), p1, p2);
                double dist = traveledDistanceAlongPath - this.path.get(0).distance(p).si;
                dist = dist >= 0.0 ? dist : 0.0; // negative in case of a gap
                return timeAtDistance(Length.instantiateSI(dist));
            }
            for (int i = 0; i < this.path.size() - 1; i++)
            {
                OtsPoint3D prevPoint = this.path.get(i);
                OtsPoint3D nextPoint = this.path.get(i + 1);
                OtsPoint3D p = OtsPoint3D.intersectionOfLines(prevPoint, nextPoint, p1, p2);
                if (p == null)
                {
                    // point too close, check next section
                    continue;
                }
                boolean onSegment =
                        prevPoint.distanceSI(nextPoint) + 2e-5 > Math.max(prevPoint.distanceSI(p), nextPoint.distanceSI(p));
                // (prevPoint.x - p.x) * (nextPoint.x - p.x) <= 2e-5/* PK 1e-6 is too small */
                // && (prevPoint.y - p.y) * (nextPoint.y - p.y) <= 2e-5/* PK 1e-6 is too small */;
                // if (i > 64)
                // {
                // System.err.println(String.format("i=%d, prevPoint=%s, nextPoint=%s, intersection=%s, onSegment=%s", i,
                // prevPoint, nextPoint, p, onSegment));
                // }
                if (p != null // on segment, or last segment
                        && (i == this.path.size() - 2 || onSegment))
                {
                    // point is on the line
                    traveledDistanceAlongPath += this.path.get(i).distance(p).si;
                    if (traveledDistanceAlongPath > this.path.getLengthSI())
                    {
                        return Time.instantiateSI(Double.NaN);
                        // Time.instantiateSI(getEndTime().si - 1e-9); // -1e-9 prevents that next move() reschedules enter
                    }
                    return timeAtDistance(Length.instantiateSI(traveledDistanceAlongPath));
                }
                else
                {
                    traveledDistanceAlongPath += this.path.get(i).distance(this.path.get(i + 1)).si;
                }
            }
        }
        catch (OtsGeometryException exception)
        {
            throw new RuntimeException("Index out of bounds on projection of point to path of operational plan", exception);
        }
        this.gtu.getSimulator().getLogger().always().error("timeAtPoint failed");
        return null;
    }

    /**
     * Calculate the distance traveled as part of this plan at the given absolute time.
     * @param time Time; the absolute time to calculate the traveled distance for as part of this plan
     * @return the distance traveled as part of this plan at the given time
     * @throws OperationalPlanException when the time is after the validity of the operational plan
     */
    public final Length getTraveledDistance(final Time time) throws OperationalPlanException
    {
        return new Length(getTraveledDistanceSI(time.minus(this.startTime)), LengthUnit.SI);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.operationalPlanSegmentList == null) ? 0 : this.operationalPlanSegmentList.hashCode());
        result = prime * result + ((this.path == null) ? 0 : this.path.hashCode());
        result = prime * result + ((this.startSpeed == null) ? 0 : this.startSpeed.hashCode());
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
        if (this.operationalPlanSegmentList == null)
        {
            if (other.operationalPlanSegmentList != null)
                return false;
        }
        else if (!this.operationalPlanSegmentList.equals(other.operationalPlanSegmentList))
            return false;
        if (this.path == null)
        {
            if (other.path != null)
                return false;
        }
        else if (!this.path.equals(other.path))
            return false;
        if (this.startSpeed == null)
        {
            if (other.startSpeed != null)
                return false;
        }
        else if (!this.startSpeed.equals(other.startSpeed))
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
        return "OperationalPlan [path=" + this.path + ", startTime=" + this.startTime + ", startSpeed=" + this.startSpeed
                + ", operationalPlanSegmentList=" + this.operationalPlanSegmentList + ", totalDuration=" + this.totalDuration
                + ", segmentStartTimesSI=" + Arrays.toString(this.segmentStartTimesRelSI) + ", endSpeed = " + this.endSpeed
                + "]";
    }

    /****************************************************************************************************************/
    /******************************************** SEGMENT DEFINITIONS ***********************************************/
    /****************************************************************************************************************/

    /**
     * The segment of an operational plan contains a part of the speed profile of a movement in which some of the variables
     * determining movement (speed, acceleration) are constant.
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. <br>
     * All rights reserved. <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     */
    public abstract static class Segment implements Serializable
    {
        /** */
        private static final long serialVersionUID = 20151114L;

        /** The duration of the acceleration or speed for this segment. */
        @SuppressWarnings("checkstyle:visibilitymodifier")
        protected final Duration duration;

        /** The initial speed for this segment. */
        @SuppressWarnings("checkstyle:visibilitymodifier")
        protected Speed v0;

        /**
         * @param duration Duration; the duration of the acceleration or speed for this segment
         */
        public Segment(final Duration duration)
        {
            this.duration = duration;
        }

        /**
         * @param v0 the initial speed of this segment; called from the Operational Plan constructor.
         */
        final void setV0(final Speed v0)
        {
            this.v0 = v0;
        }

        /**
         * @return duration the duration of the acceleration or speed for this segment
         */
        public final Duration getDuration()
        {
            return this.duration;
        }

        /**
         * @return duration the duration of the acceleration or speed for this segment
         */
        public final double getDurationSI()
        {
            return this.duration.si;
        }

        /**
         * Calculate the distance covered by a GTU in this segment.
         * @return distance covered
         */
        final double distanceSI()
        {
            return distanceSI(getDuration().si);
        }

        /**
         * Calculate the distance covered by a GTU in this segment after relative time t.
         * @param t double; the relative time since starting this segment for which to calculate the distance covered
         * @return distance covered
         */
        abstract double distanceSI(double t);

        /**
         * Calculate the speed of a GTU in this segment after relative time t.
         * @param t double; the relative time since starting this segment for which to calculate the speed
         * @return speed at relative time t
         */
        abstract double speedSI(double t);

        /**
         * Calculate the acceleration of a GTU in this segment after relative time t.
         * @param t double; the relative time since starting this segment for which to calculate the acceleration
         * @return acceleration at relative time t
         */
        abstract double accelerationSI(double t);

        /**
         * Calculate the end speed for this segment.
         * @return speed at end of the segment
         */
        abstract Speed endSpeed();

        /**
         * Calculate the time it takes for the GTU to travel from the start of this Segment to the specified distance within
         * this Segment.
         * @param distance Length; the distance for which the travel time has to be calculated
         * @return the time at distance
         */
        abstract Duration timeAtDistance(Length distance);

        /** {@inheritDoc} */
        @SuppressWarnings("checkstyle:designforextension")
        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((this.duration == null) ? 0 : this.duration.hashCode());
            result = prime * result + ((this.v0 == null) ? 0 : this.v0.hashCode());
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
            Segment other = (Segment) obj;
            if (this.duration == null)
            {
                if (other.duration != null)
                    return false;
            }
            else if (!this.duration.equals(other.duration))
                return false;
            if (this.v0 == null)
            {
                if (other.v0 != null)
                    return false;
            }
            else if (!this.v0.equals(other.v0))
                return false;
            return true;
        }
    }

    /**
     * The segment of an operational plan in which the acceleration is constant.
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. <br>
     * All rights reserved. <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     */
    public static class AccelerationSegment extends Segment
    {
        /** */
        private static final long serialVersionUID = 20151114L;

        /** The acceleration for the given duration. */
        private final Acceleration acceleration;

        /**
         * @param duration Duration; the duration of the constant acceleration for this segment
         * @param acceleration Acceleration; the acceleration for the given duration
         */
        public AccelerationSegment(final Duration duration, final Acceleration acceleration)
        {
            super(duration);
            this.acceleration = acceleration;
        }

        /** {@inheritDoc} */
        @Override
        final double distanceSI(final double t)
        {
            return this.v0.si * t + 0.5 * this.acceleration.si * t * t;
        }

        /** {@inheritDoc} */
        @Override
        final double accelerationSI(final double t)
        {
            return this.acceleration.si;
        }

        /** {@inheritDoc} */
        @Override
        final double speedSI(final double t)
        {
            return this.v0.si + this.acceleration.si * t;
        }

        /** {@inheritDoc} */
        @Override
        final Speed endSpeed()
        {
            return this.v0.plus(this.acceleration.times(getDuration()));
        }

        /** {@inheritDoc} */
        @Override
        public final int hashCode()
        {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + ((this.acceleration == null) ? 0 : this.acceleration.hashCode());
            return result;
        }

        /** {@inheritDoc} */
        @Override
        @SuppressWarnings("checkstyle:needbraces")
        public final boolean equals(final Object obj)
        {
            if (this == obj)
                return true;
            if (!super.equals(obj))
                return false;
            if (getClass() != obj.getClass())
                return false;
            AccelerationSegment other = (AccelerationSegment) obj;
            if (this.acceleration == null)
            {
                if (other.acceleration != null)
                    return false;
            }
            else if (!this.acceleration.equals(other.acceleration))
                return false;
            return true;
        }

        /** {@inheritDoc} */
        @Override
        public final Duration timeAtDistance(final Length distance)
        {
            double[] solutions = Solver.solve(this.acceleration.si / 2, this.v0.si, -distance.si);
            // Find the solution that occurs within our duration (there should be only one).
            for (double solution : solutions)
            {
                if (solution >= 0 && solution <= this.duration.si)
                {
                    return new Duration(solution, DurationUnit.SI);
                }
            }
            CategoryLogger.always().error("AccelerationSegment " + this + " timeAtDistance( " + distance + ") failed");
            return new Duration(Double.NaN, DurationUnit.SI); // No valid solution; let the caller deal with that
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "AccelerationSegment [t=" + this.duration + ", v0=" + this.v0 + ", a=" + this.acceleration + "]";
        }

    }

    /**
     * The segment of an operational plan in which the speed is constant.
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. <br>
     * All rights reserved. <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     */
    public static class SpeedSegment extends Segment
    {
        /** */
        private static final long serialVersionUID = 20151114L;

        /**
         * @param duration Duration; the duration of the constant speed for this segment
         */
        public SpeedSegment(final Duration duration)
        {
            super(duration);
        }

        /** {@inheritDoc} */
        @Override
        final double distanceSI(final double t)
        {
            return this.v0.si * t;
        }

        /** {@inheritDoc} */
        @Override
        final double accelerationSI(final double t)
        {
            return 0.0;
        }

        /** {@inheritDoc} */
        @Override
        final double speedSI(final double t)
        {
            return this.v0.si;
        }

        /** {@inheritDoc} */
        @Override
        final Speed endSpeed()
        {
            return this.v0;
        }

        /**
         * @return speed
         */
        public final Speed getSpeed()
        {
            return this.v0;
        }

        /** {@inheritDoc} */
        @Override
        public final Duration timeAtDistance(final Length distance)
        {
            double[] solution = Solver.solve(this.v0.si, -distance.si);
            if (solution.length > 0 && solution[0] >= 0 && solution[0] <= getDurationSI())
            {
                return new Duration(solution[0], DurationUnit.SI);
            }
            CategoryLogger.always().error("SpeedSegment " + this + " timeAtDistance( " + distance + ") failed");
            return null; // No valid solution
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "SpeedSegment [t=" + this.duration + ", v0=" + this.v0 + "]";
        }

    }

}
