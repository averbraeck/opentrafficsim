package org.opentrafficsim.road.gtu.lane.plan.operational;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.geometry.Bezier;
import org.opentrafficsim.core.geometry.DirectedPoint;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSLine3D.FractionalFallback;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.perception.EgoPerception;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedTacticalPlanner;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneDirection;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * Lane change status across operational plans. This class allows lane based tactical planners to perform lane changes without
 * having to deal with many complexities concerning paths and lane registration. The main purpose of the tactical planner is to
 * request a path using {@code getPath()} for each step of the tactical planner.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jul 26, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class LaneChange implements Serializable
{

    /** */
    private static final long serialVersionUID = 20160811L;

    /** Total lane change duration. */
    private Duration desiredLaneChangeDuration;

    /** Fraction of lane change had. */
    private double fraction;

    /** Boundary length within which the lane change should be performed. */
    private Length boundary;

    /** Lane change path. */
    private LaneChangePath laneChangePath = LaneChangePath.SINE_INTERP;

    /** Whether the GTU is changing lane. */
    private LateralDirectionality laneChangeDirectionality = null;

    /** Minimum lane change distance. */
    private final Length minimumLaneChangeDistance;

    /** Instance to invoke static method through scheduled event on. */
    private static final LaneOperationalPlanBuilder BUILDER = new LaneOperationalPlanBuilder();

    /** Minimum distance required to perform a lane change as factor on vehicle length. */
    public static double MIN_LC_LENGTH_FACTOR = 1.5;

    /**
     * Constructor.
     * @param gtu LaneBasedGTU; gtu
     */
    public LaneChange(final LaneBasedGTU gtu)
    {
        this.minimumLaneChangeDistance = gtu.getLength().times(MIN_LC_LENGTH_FACTOR);
    }

    /**
     * Constructor.
     * @param minimumLaneChangeDistance Length; minimum lane change distance
     * @param desiredLaneChangeDuration Duration; deaired lane change duration
     */
    public LaneChange(final Length minimumLaneChangeDistance, final Duration desiredLaneChangeDuration)
    {
        this.minimumLaneChangeDistance = minimumLaneChangeDistance;
        this.desiredLaneChangeDuration = desiredLaneChangeDuration;
    }

    /**
     * Returns the minimum lane change distance.
     * @return Length; minimum lane change distance
     */
    public Length getMinimumLaneChangeDistance()
    {
        return this.minimumLaneChangeDistance;
    }

    /**
     * Sets the desired lane change duration. Should be set by a tactical planner.
     * @param duration Duration; desired lane change duration
     */
    public void setDesiredLaneChangeDuration(final Duration duration)
    {
        this.desiredLaneChangeDuration = duration;
    }

    /**
     * Sets the distance within which a lane change should be finished. Should be set by a tactical planner. In case of a single
     * lane change required before some point, this is not required as the found center line length is intrinsically limited.
     * For multiple lane changes being required, space after a lane change is required.
     * @param boundary Length; boundary
     */
    public void setBoundary(final Length boundary)
    {
        this.boundary = boundary;
    }

    /**
     * Returns the fraction of the lane change performed.
     * @return double; fraction of lane change performed
     */
    public double getFraction()
    {
        return this.fraction;
    }

    /**
     * Sets a lane change path.
     * @param laneChangePath LaneChangePath; lane change path
     */
    public void setLaneChangePath(final LaneChangePath laneChangePath)
    {
        this.laneChangePath = laneChangePath;
    }

    /**
     * Return whether the GTU is changing lane.
     * @return whether the GTU is changing lane
     */
    public final boolean isChangingLane()
    {
        return this.laneChangeDirectionality != null;
    }

    /**
     * Return whether the GTU is changing left.
     * @return whether the GTU is changing left
     */
    public final boolean isChangingLeft()
    {
        return LateralDirectionality.LEFT.equals(this.laneChangeDirectionality);
    }

    /**
     * Return whether the GTU is changing right.
     * @return whether the GTU is changing right
     */
    public final boolean isChangingRight()
    {
        return LateralDirectionality.RIGHT.equals(this.laneChangeDirectionality);
    }

    /**
     * Return lateral lane change direction.
     * @return LateralDirectionality; lateral lane change direction
     */
    public final LateralDirectionality getDirection()
    {
        return this.laneChangeDirectionality;
    }

    /**
     * Second lane of lane change relative to the reference lane. Note that the reference lane may either be the source or the
     * target lane. Thus, the second lane during a lane change may either be the left or right lane, regardless of the lane
     * change direction.
     * @param gtu LaneBasedGTU; the GTU
     * @return target lane of lane change
     * @throws OperationalPlanException If no lane change is being performed.
     */
    public final RelativeLane getSecondLane(final LaneBasedGTU gtu) throws OperationalPlanException
    {
        Throw.when(!isChangingLane(), OperationalPlanException.class,
                "Target lane is requested, but no lane change is being performed.");
        Map<Lane, Length> map;
        DirectedLanePosition dlp;
        try
        {
            map = gtu.positions(gtu.getReference());
            dlp = gtu.getReferencePosition();
        }
        catch (GTUException exception)
        {
            throw new OperationalPlanException("Second lane of lane change could not be determined.", exception);
        }
        Set<Lane> accessibleLanes = dlp.getLane().accessibleAdjacentLanesPhysical(this.laneChangeDirectionality,
                gtu.getGTUType(), dlp.getGtuDirection());
        if (!accessibleLanes.isEmpty() && map.containsKey(accessibleLanes.iterator().next()))
        {
            return isChangingLeft() ? RelativeLane.LEFT : RelativeLane.RIGHT;
        }
        return isChangingLeft() ? RelativeLane.RIGHT : RelativeLane.LEFT;
    }

    /**
     * Returns the path for a lane change. Lane change initialization and finalization events are automatically performed.
     * @param timeStep Duration; plan time step
     * @param gtu LaneBasedGTU; gtu
     * @param from DirectedLanePosition; current position on the from lane (i.e. not necessarily the reference position)
     * @param startPosition DirectedPoint; current position in 2D
     * @param planDistance Length; absolute distance that will be covered during the time step
     * @param laneChangeDirection LateralDirectionality; lane change direction
     * @return OTSLine3D; path
     * @throws OTSGeometryException on path or shape error
     */
    public final OTSLine3D getPath(final Duration timeStep, final LaneBasedGTU gtu, final DirectedLanePosition from,
            final DirectedPoint startPosition, final Length planDistance, final LateralDirectionality laneChangeDirection)
            throws OTSGeometryException
    {
        // initiate lane change
        boolean favoured = false;
        if (!isChangingLane())
        {
            favoured = true;
            this.laneChangeDirectionality = laneChangeDirection;
            Try.execute(() -> gtu.initLaneChange(laneChangeDirection), "Error during lane change initialization.");
        }
        // Wouter: why would we ever not favor a side during a lane change
        favoured = true;

        // determine longitudinal distance along the from lanes
        /*
         * We take 3 factors in to account. The first two are 1) minimum physical lane change length, and 2) desired lane change
         * duration. With the current mean speed of the plan, we take the maximum. So at very low speeds, the minimum physical
         * length may increase the lane change duration. We also have 3) the maximum length before a lane change needs to have
         * been performed. To overcome simulation troubles, we allow this to result in an even shorter length than the minimum
         * physical distance. So: length = min( max("1", "2"), "3" ). These distances are all considered along the from-lanes.
         * Actual path distance is different.
         */
        Speed meanSpeed = planDistance.divide(timeStep);
        double minDuration = this.minimumLaneChangeDistance.si / meanSpeed.si;
        double laneChangeDuration = Math.max(this.desiredLaneChangeDuration.si, minDuration);
        if (this.boundary != null)
        {
            double maxDuration = this.boundary.si / meanSpeed.si;
            laneChangeDuration = Math.min(laneChangeDuration, maxDuration);
        }

        double totalLength = laneChangeDuration * meanSpeed.si;
        double fromDist = (1.0 - this.fraction) * totalLength; // remaining distance along from lanes to lane change end
        Throw.when(fromDist < 0.0, RuntimeException.class, "Lane change results in negative distance along from lanes.");

        // get fractional location there, build lane lists as we search over the distance
        LaneDirection fromLane = from.getLaneDirection();
        List<LaneDirection> fromLanes = new ArrayList<>();
        List<LaneDirection> toLanes = new ArrayList<>();
        fromLanes.add(fromLane);
        toLanes.add(fromLane.getAdjacentLaneDirection(this.laneChangeDirectionality, gtu));
        double endPosFrom = from.getPosition().si + fromDist;
        while (endPosFrom + gtu.getFront().getDx().si > fromLane.getLane().getLength().si)
        {
            LaneDirection nextFromLane;
            if (!favoured)
            {
                nextFromLane = fromLane.getNextLaneDirection(gtu);
            }
            else
            {
                Set<LaneDirection> nexts = fromLane.getNextForRoute(gtu);
                if (nexts != null && !nexts.isEmpty())
                {
                    Iterator<LaneDirection> it = nexts.iterator();
                    nextFromLane = it.next();
                    while (it.hasNext())
                    {
                        nextFromLane =
                                LaneBasedTacticalPlanner.mostOnSide(nextFromLane, it.next(), this.laneChangeDirectionality);
                    }
                }
                else
                {
                    nextFromLane = null;
                }
            }
            if (nextFromLane == null)
            {
                // there are no lanes to move on, restrict lane change length/duration (given fixed mean speed)
                double endFromPosLimit = fromLane.getLane().getLength().si - gtu.getFront().getDx().si;
                double f = 1.0 - (endPosFrom - endFromPosLimit) / fromDist;
                laneChangeDuration *= f;
                endPosFrom = endFromPosLimit;
                break;
            }
            endPosFrom -= fromLane.getLane().getLength().si;
            LaneDirection nextToLane = nextFromLane.getAdjacentLaneDirection(this.laneChangeDirectionality, gtu);
            if (nextToLane == null)
            {
                // there are no lanes to change to, restrict lane change length/duration (given fixed mean speed)
                double endFromPosLimit = fromLane.getLane().getLength().si - gtu.getFront().getDx().si;
                double f = 1.0 - (endPosFrom - endFromPosLimit) / fromDist;
                laneChangeDuration *= f;
                endPosFrom = endFromPosLimit;
                break;
            }
            fromLane = nextFromLane;
            fromLanes.add(fromLane);
            toLanes.add(nextToLane);
        }
        // for long vehicles and short lanes, revert
        while (endPosFrom < 0.0)
        {
            fromLanes.remove(fromLanes.size() - 1);
            toLanes.remove(toLanes.size() - 1);
            fromLane = fromLanes.get(fromLanes.size() - 1);
            endPosFrom += fromLane.getLane().getLength().si;
        }
        // finally, get location at the final lane available
        double endFractionalPositionFrom = fromLane.fractionAtCoveredDistance(Length.instantiateSI(endPosFrom));

        DirectedLanePosition fromAdjusted = from;
        while (fromAdjusted.getGtuDirection().isPlus() ? fromAdjusted.getPosition().gt(fromAdjusted.getLane().getLength())
                : fromAdjusted.getPosition().lt0())
        {
            // the from position is beyond the first lane (can occur if it is not the ref position)
            fromLanes.remove(0);
            toLanes.remove(0);
            Length beyond = fromAdjusted.getGtuDirection().isPlus()
                    ? fromAdjusted.getPosition().minus(fromAdjusted.getLane().getLength()) : fromAdjusted.getPosition().neg();
            Length pos =
                    fromLanes.get(0).getDirection().isPlus() ? beyond : fromLanes.get(0).getLane().getLength().minus(beyond);
            fromAdjusted =
                    Try.assign(() -> new DirectedLanePosition(fromLanes.get(0).getLane(), pos, fromLanes.get(0).getDirection()),
                            OTSGeometryException.class, "Info for lane is null.");
        }

        // get path from shape

        // create center lines
        double startFractionalPositionFrom = from.getPosition().si / from.getLane().getLength().si;
        OTSLine3D fromLine = getLine(fromLanes, startFractionalPositionFrom, endFractionalPositionFrom);
        // project for toLane
        double startFractionalPositionTo = toLanes.get(0).getLane().getCenterLine().projectFractional(null, null,
                startPosition.x, startPosition.y, FractionalFallback.ENDPOINT);
        int last = fromLanes.size() - 1;
        double frac = fromLanes.get(last).getDirection().isPlus() ? endFractionalPositionFrom : 1.0 - endFractionalPositionFrom;
        DirectedPoint p = fromLanes.get(last).getLane().getCenterLine().getLocationFraction(frac);
        double endFractionalPositionTo = toLanes.get(last).getLane().getCenterLine().projectFractional(null, null, p.x, p.y,
                FractionalFallback.ENDPOINT);
        startFractionalPositionTo = startFractionalPositionTo >= 0.0 ? startFractionalPositionTo : 0.0;
        endFractionalPositionTo = endFractionalPositionTo <= 1.0 ? endFractionalPositionTo : 1.0;
        endFractionalPositionTo = endFractionalPositionTo <= 0.0 ? endFractionalPositionFrom : endFractionalPositionTo;
        // check for poor projection (end location is difficult to project; we have no path yet so we use the from lane)
        if (fromLanes.size() == 1 && endFractionalPositionTo <= startFractionalPositionTo)
        {
            endFractionalPositionTo = Math.min(Math.max(startFractionalPositionTo + 0.001, endFractionalPositionFrom), 1.0);
        }
        if (startFractionalPositionTo >= 1.0)
        {
            toLanes.remove(0);
            startFractionalPositionTo = 0.0;
        }
        OTSLine3D toLine = getLine(toLanes, startFractionalPositionTo, endFractionalPositionTo);

        OTSLine3D path = this.laneChangePath.getPath(timeStep, planDistance, meanSpeed, fromAdjusted, startPosition,
                laneChangeDirection, fromLine, toLine, Duration.instantiateSI(laneChangeDuration), this.fraction);
        // update
        // TODO: this assumes the time step will not be interrupted
        this.fraction += timeStep.si / laneChangeDuration; // the total fraction this step increases

        // deal with lane change end
        double requiredLength = planDistance.si - path.getLength().si;
        if (requiredLength > 0.0 || this.fraction > 0.999)
        {
            try
            {
                gtu.getSimulator().scheduleEventNow(gtu, BUILDER, "scheduleLaneChangeFinalization",
                        new Object[] {gtu, Length.min(planDistance, path.getLength()), laneChangeDirection});
            }
            catch (SimRuntimeException exception)
            {
                throw new RuntimeException("Error during lane change finalization.", exception);
            }
            // add length to path on to lanes
            if (requiredLength > 0.0)
            {
                LaneDirection toLane = toLanes.get(toLanes.size() - 1);
                int n = path.size();
                // ignore remainder of first lane if fraction is at the end of the lane
                if (0.0 < endFractionalPositionFrom && endFractionalPositionFrom < 1.0)
                {
                    OTSLine3D remainder = toLane.getDirection().isPlus()
                            ? toLane.getLane().getCenterLine().extractFractional(endFractionalPositionTo, 1.0)
                            : toLane.getLane().getCenterLine().extractFractional(0.0, endFractionalPositionTo).reverse();
                    path = OTSLine3D.concatenate(0.001, path, remainder);
                    requiredLength = planDistance.si - path.getLength().si;
                }
                // add further lanes
                while (toLane != null && requiredLength > 0.0)
                {
                    toLane = toLane.getNextLaneDirection(gtu);
                    if (toLane != null) // let's hope we will move on to a sink
                    {
                        OTSLine3D remainder = toLane.getDirection().isPlus() ? toLane.getLane().getCenterLine()
                                : toLane.getLane().getCenterLine().reverse();
                        path = OTSLine3D.concatenate(Lane.MARGIN.si, path, remainder);
                        requiredLength = planDistance.si - path.getLength().si + Lane.MARGIN.si;
                    }
                }
                // filter near-duplicate point which results in projection exceptions
                if (this.fraction > 0.999) // this means point 'target' is essentially at the design line
                {
                    OTSPoint3D[] points = new OTSPoint3D[path.size() - 1];
                    System.arraycopy(path.getPoints(), 0, points, 0, n - 1);
                    System.arraycopy(path.getPoints(), n, points, n - 1, path.size() - n);
                    path = new OTSLine3D(points);
                }
            }
            // reset lane change
            this.laneChangeDirectionality = null;
            this.boundary = null;
            this.fraction = 0.0;
        }

        return path;
    }

    /**
     * Returns a line from the lane center lines, cutting of at the from position and the end fractional position.
     * @param lanes List&lt;LaneDirection&gt;; lanes
     * @param startFractionalPosition double; current fractional GTU position on first lane
     * @param endFractionalPosition double; target fractional GTU position on last lane
     * @return OTSLine3D; line from the lane center lines
     * @throws OTSGeometryException on fraction outside of range
     */
    private OTSLine3D getLine(final List<LaneDirection> lanes, final double startFractionalPosition,
            final double endFractionalPosition) throws OTSGeometryException
    {
        OTSLine3D line = null;
        for (LaneDirection lane : lanes)
        {
            if (line == null && lane.equals(lanes.get(lanes.size() - 1)))
            {
                if (lane.getDirection().isPlus())
                {
                    line = lane.getLane().getCenterLine().extractFractional(startFractionalPosition, endFractionalPosition);
                }
                else
                {
                    line = lane.getLane().getCenterLine().extractFractional(startFractionalPosition, endFractionalPosition)
                            .reverse();
                }
            }
            else if (line == null)
            {
                if (lane.getDirection().isPlus())
                {
                    line = lane.getLane().getCenterLine().extractFractional(startFractionalPosition, 1.0);
                }
                else
                {
                    line = lane.getLane().getCenterLine().extractFractional(0.0, startFractionalPosition).reverse();
                }
            }
            else if (lane.equals(lanes.get(lanes.size() - 1)))
            {
                if (lane.getDirection().isPlus())
                {
                    line = OTSLine3D.concatenate(Lane.MARGIN.si, line,
                            lane.getLane().getCenterLine().extractFractional(0.0, endFractionalPosition));
                }
                else
                {
                    line = OTSLine3D.concatenate(Lane.MARGIN.si, line,
                            lane.getLane().getCenterLine().extractFractional(endFractionalPosition, 1.0).reverse());
                }
            }
            else
            {
                if (lane.getDirection().isPlus())
                {
                    line = OTSLine3D.concatenate(Lane.MARGIN.si, line, lane.getLane().getCenterLine());
                }
                else
                {
                    line = OTSLine3D.concatenate(Lane.MARGIN.si, line, lane.getLane().getCenterLine().reverse());
                }
            }
        }
        return line;
    }

    /**
     * Checks whether the given GTU has sufficient space relative to a {@code Headway}.
     * @param gtu LaneBasedGTU; gtu
     * @param headway Headway; headway
     * @return boolean; whether the given GTU has sufficient space relative to a {@code Headway}
     */
    public boolean checkRoom(final LaneBasedGTU gtu, final Headway headway)
    {
        if (this.desiredLaneChangeDuration == null)
        {
            this.desiredLaneChangeDuration = Try.assign(() -> gtu.getParameters().getParameter(ParameterTypes.LCDUR),
                    "LaneChange; the desired lane change duration should be set or paramater LCDUR should be defined.");
        }

        EgoPerception<?, ?> ego = gtu.getTacticalPlanner().getPerception().getPerceptionCategoryOrNull(EgoPerception.class);
        Speed egoSpeed = ego == null ? gtu.getSpeed() : ego.getSpeed();
        Acceleration egoAcceleration = ego == null ? gtu.getAcceleration() : ego.getAcceleration();
        Speed speed = headway.getSpeed() == null ? Speed.ZERO : headway.getSpeed();
        Acceleration acceleration = headway.getAcceleration() == null ? Acceleration.ZERO : headway.getAcceleration();
        Length s0 = gtu.getParameters().getParameterOrNull(ParameterTypes.S0);
        s0 = s0 == null ? Length.ZERO : s0;

        Length distanceToStop;
        if (speed.eq0())
        {
            distanceToStop = Length.ZERO;
        }
        else
        {
            Acceleration b = gtu.getParameters().getParameterOrNull(ParameterTypes.B);
            b = b == null ? Acceleration.ZERO : b.neg();
            Acceleration a = Acceleration.min(Acceleration.max(b, acceleration.plus(b)), acceleration);
            if (a.ge0())
            {
                return true;
            }
            double t = speed.si / -a.si;
            distanceToStop = Length.instantiateSI(speed.si * t + .5 * a.si * t * t);
        }

        Length availableDistance = headway.getDistance().plus(distanceToStop);
        double t = this.desiredLaneChangeDuration.si;
        if (egoAcceleration.lt0())
        {
            t = Math.min(egoSpeed.si / -egoAcceleration.si, t);
        }
        Length requiredDistance = Length
                .max(Length.instantiateSI(egoSpeed.si * t + .5 * egoAcceleration.si * t * t), this.minimumLaneChangeDistance)
                .plus(s0);
        return availableDistance.gt(requiredDistance);
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "LaneChange [fraction=" + this.fraction + ", laneChangeDirectionality=" + this.laneChangeDirectionality + "]";
    }

    /**
     * Provides a (partial) path during lane changes.
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 30 apr. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    public interface LaneChangePath
    {

        /** Sine-shaped interpolation between center lines. */
        LaneChangePath SINE_INTERP = new InterpolatedLaneChangePath()
        {
            /** {@inheritDoc} */
            @Override
            double longitudinalFraction(final double lateralFraction)
            {
                return 1.0 - Math.acos(2.0 * (lateralFraction - 0.5)) / Math.PI;
            }

            /** {@inheritDoc} */
            @Override
            double lateralFraction(final double longitudinalFraction)
            {
                return 0.5 - 0.5 * Math.cos(longitudinalFraction * Math.PI);
            }
        };

        /** Linear interpolation between center lines. */
        LaneChangePath LINEAR = new InterpolatedLaneChangePath()
        {

            @Override
            double longitudinalFraction(final double lateralFraction)
            {
                return lateralFraction;
            }

            @Override
            double lateralFraction(final double longitudinalFraction)
            {
                return longitudinalFraction;
            }
        };

        /** A simple Bezier curve directly to the lane change target position. */
        LaneChangePath BEZIER = new LaneChangePath()
        {
            /** {@inheritDoc} */
            @Override
            public OTSLine3D getPath(final Duration timeStep, final Length planDistance, final Speed meanSpeed,
                    final DirectedLanePosition from, final DirectedPoint startPosition,
                    final LateralDirectionality laneChangeDirection, final OTSLine3D fromLine, final OTSLine3D toLine,
                    final Duration laneChangeDuration, final double lcFraction) throws OTSGeometryException
            {
                DirectedPoint target = toLine.getLocationFraction(1.0);
                return Bezier.cubic(64, startPosition, target, 0.5);
            }
        };

        /** The target point (including rotation) for the coming time step is based on a sine wave. */
        LaneChangePath SINE = new SequentialLaneChangePath()
        {
            /** {@inheritDoc} */
            @Override
            protected double lateralFraction(final double lcFraction)
            {
                return -1.0 / (2 * Math.PI) * Math.sin(2 * Math.PI * lcFraction) + lcFraction;
            }

            /** {@inheritDoc} */
            @Override
            protected double angle(final double width, final double cumulLcLength, final double totalLcLength)
            {
                return Math.atan((-width * Math.cos(2 * Math.PI * cumulLcLength / totalLcLength) / totalLcLength)
                        + width / totalLcLength);
            }
        };

        /** The target point (including rotation) for the coming time step is based on a 3rd-degree polynomial. */
        LaneChangePath POLY3 = new SequentialLaneChangePath()
        {
            /** {@inheritDoc} */
            @Override
            protected double lateralFraction(final double lcFraction)
            {
                return 3 * (lcFraction * lcFraction) - 2 * (lcFraction * lcFraction * lcFraction);
            }

            /** {@inheritDoc} */
            @Override
            protected double angle(final double width, final double cumulLcLength, final double totalLcLength)
            {
                return Math.atan(cumulLcLength * 6 * width / (totalLcLength * totalLcLength)
                        - cumulLcLength * cumulLcLength * 6 * width / (totalLcLength * totalLcLength * totalLcLength));
            }
        };

        /**
         * A helper class to allow a lane change to follow a sequential determination of the target position (including
         * rotation) for each time step.
         * <p>
         * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
         * reserved. <br>
         * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
         * <p>
         * @version $Revision$, $LastChangedDate$, by $Author$, initial version 30 apr. 2018 <br>
         * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
         * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
         * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
         */
        abstract class SequentialLaneChangePath implements LaneChangePath
        {
            /** {@inheritDoc} */
            @Override
            public OTSLine3D getPath(final Duration timeStep, final Length planDistance, final Speed meanSpeed,
                    final DirectedLanePosition from, final DirectedPoint startPosition,
                    final LateralDirectionality laneChangeDirection, final OTSLine3D fromLine, final OTSLine3D toLine,
                    final Duration laneChangeDuration, final double lcFraction) throws OTSGeometryException
            {
                DirectedPoint toTarget = toLine.getLocationFraction(1.0);
                DirectedPoint fromTarget = fromLine.getLocationFraction(1.0);
                double width = laneChangeDirection.isRight() ? fromTarget.distance(toTarget) : -fromTarget.distance(toTarget);
                double dFraction = timeStep.si / laneChangeDuration.si;
                return getPathRecursive(planDistance, meanSpeed, 1.0, width, from, startPosition, fromLine, toLine,
                        laneChangeDuration, lcFraction, dFraction);
            }

            /**
             * Attempts to derive a path. If the resulting path is shorter than {@code planDistance} (e.g. lane change towards
             * the inside of a curve), this method calls itself using a larger look-ahead distance.
             * @param planDistance Length; plan distance
             * @param meanSpeed Speed; mean speed during plan
             * @param buffer double; buffer factor to assure sufficient path length is found, increased recursively
             * @param width double; lateral deviation from from lanes at lane change end
             * @param from DirectedLanePosition; current position on the from-lanes
             * @param startPosition DirectedPoint; current 2D position
             * @param fromLine OTSLine3D; from line
             * @param toLine OTSLine3D; to line
             * @param laneChangeDuration Duration; current considered duration of the entire lane change
             * @param lcFraction double; lane change fraction at beginning of the plan
             * @param dFraction double; additional lane change fraction to be made during the plan
             * @return OTSLine3D a (partial) path for a lane change
             * @throws OTSGeometryException on wrong fractional position
             */
            private OTSLine3D getPathRecursive(final Length planDistance, final Speed meanSpeed, final double buffer,
                    final double width, final DirectedLanePosition from, final DirectedPoint startPosition,
                    final OTSLine3D fromLine, final OTSLine3D toLine, final Duration laneChangeDuration,
                    final double lcFraction, final double dFraction) throws OTSGeometryException
            {
                // factor on path length to not overshoot a fraction of 1.0 in lane change progress, i.e. <1 if lane change will
                // be finished in the coming time step
                double cutoff = (1.0 - lcFraction) / (dFraction * buffer);
                cutoff = cutoff > 1.0 ? 1.0 : cutoff;

                // lane change fraction at end of plan
                double lcFractionEnd = lcFraction + dFraction * buffer * cutoff;

                // lateral fraction at that point according to shape
                double f = lateralFraction(lcFractionEnd);

                // from-lane length
                double totalLcLength = meanSpeed.si * laneChangeDuration.si;
                double cumulLcLength = totalLcLength * lcFractionEnd;

                // TODO: sequential is disabled as LaneChangePath now uses 2 OTSLine3D's instead of 2 List<Lane>'s. This was
                // done as the code using LaneChangePath (i.e. LaneChange) required more details on fractional positions itself.
                return null;
                /*-
                // find lane we will end up on at the end of the plan
                double positionAtEnd = (from.getGtuDirection().isPlus() ? from.getPosition().si
                        : from.getLane().getLength().si - from.getPosition().si) + planDistance.si * buffer * cutoff;
                for (int i = 0; i < fromLanes.size(); i++)
                {
                    LaneDirection fromLane = fromLanes.get(i);
                    if (fromLane.getLength().si >= positionAtEnd)
                    {
                        // get target point by interpolation between from and to lane
                        double endFraction = fromLane.fractionAtCoveredDistance(Length.instantiateSI(positionAtEnd));
                        DirectedPoint pFrom = fromLane.getLocationFraction(endFraction);
                        DirectedPoint pTo = toLanes.get(i).getLocationFraction(endFraction);
                        DirectedPoint target = new DirectedPoint((1 - f) * pFrom.x + f * pTo.x, (1 - f) * pFrom.y + f * pTo.y,
                                (1 - f) * pFrom.z + f * pTo.z);
                        // set rotation according to shape, relative to lane center line
                        target.setRotZ(
                                (1 - f) * pFrom.getRotZ() + f * pTo.getRotZ() - angle(width, cumulLcLength, totalLcLength));
                        // Bezier path towards that point
                        OTSLine3D path = Bezier.cubic(64, startPosition, target, 0.5);
                        // check if long enough, otherwise look further (e.g. changing to inside of curve gives a shorter path)
                        if (path.getLength().si < planDistance.si && cutoff == 1.0)
                        {
                            return getPathRecursive(planDistance, meanSpeed, buffer * 1.25, width, from, startPosition,
                                    fromLine, toLine, laneChangeDuration, lcFraction, dFraction);
                        }
                        return path;
                    }
                    positionAtEnd -= fromLane.getLength().si;
                }
                Throw.when(lcFraction + dFraction < 0.999, RuntimeException.class,
                        "No partial path for lane change could be determined; fromLanes are too short.");
                return null;
                */
            }

            /**
             * Returns the fractional lateral deviation given a fraction of lane change being completed.
             * @param lcFraction double; fraction of lane change
             * @return double; lateral deviation
             */
            protected abstract double lateralFraction(double lcFraction);

            /**
             * Returns the angle, relative to the lane center line, at the given cumulative length for a lane change of given
             * total length and lateral deviation.
             * @param width double; lateral deviation from from lanes at lane change end
             * @param cumulLcLength double; cumulative length (along from lanes) covered so far
             * @param totalLcLength double; total (along from lanes) length to cover in lane change
             * @return double; angle, relative to the lane center line
             */
            protected abstract double angle(double width, double cumulLcLength, double totalLcLength);
        }

        /**
         * Helper class for interpolation between the from and to center lines.
         * <p>
         * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
         * reserved. <br>
         * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
         * <p>
         * @version $Revision$, $LastChangedDate$, by $Author$, initial version May 3, 2019 <br>
         * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
         * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
         * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
         */
        abstract class InterpolatedLaneChangePath implements LaneChangePath
        {

            /** {@inheritDoc} */
            @Override
            public OTSLine3D getPath(final Duration timeStep, final Length planDistance, final Speed meanSpeed,
                    final DirectedLanePosition from, final DirectedPoint startPosition,
                    final LateralDirectionality laneChangeDirection, final OTSLine3D fromLine, final OTSLine3D toLine,
                    final Duration laneChangeDuration, final double lcFraction) throws OTSGeometryException
            {

                double dx = fromLine.get(0).getLocation().x - startPosition.x;
                double dy = fromLine.get(0).getLocation().y - startPosition.y;
                double distFromLoc = Math.sqrt(dx * dx + dy * dy);
                dx = fromLine.get(0).getLocation().x - toLine.get(0).getLocation().x;
                dy = fromLine.get(0).getLocation().y - toLine.get(0).getLocation().y;
                double distFromTo = Math.sqrt(dx * dx + dy * dy);
                double startLateralFraction = distFromLoc / distFromTo;
                // Location is not on path in z-direction, so using .distance() create bugs
                // PK: added test for NaN (which occurs when fromLine and toLine start on top of each other.
                if (Double.isNaN(startLateralFraction) || startLateralFraction > 1.0)
                {
                    startLateralFraction = 1.0;
                }
                double startLongitudinalFractionTotal = longitudinalFraction(startLateralFraction);

                double nSegments = Math.ceil((64 * (1.0 - lcFraction)));
                List<OTSPoint3D> pointList = new ArrayList<>();
                double zStart = (1.0 - startLateralFraction) * fromLine.get(0).z + startLateralFraction * toLine.get(0).z;
                pointList.add(new OTSPoint3D(startPosition.x, startPosition.y, zStart));
                for (int i = 1; i <= nSegments; i++)
                {
                    double f = i / nSegments;
                    double longitudinalFraction = startLongitudinalFractionTotal + f * (1.0 - startLongitudinalFractionTotal);
                    double lateralFraction = lateralFraction(longitudinalFraction);
                    double lateralFractionInv = 1.0 - lateralFraction;
                    DirectedPoint fromPoint = fromLine.getLocationFraction(f);
                    DirectedPoint toPoint = toLine.getLocationFraction(f);
                    pointList.add(new OTSPoint3D(lateralFractionInv * fromPoint.x + lateralFraction * toPoint.x,
                            lateralFractionInv * fromPoint.y + lateralFraction * toPoint.y,
                            lateralFractionInv * fromPoint.z + lateralFraction * toPoint.z));
                }

                OTSLine3D line = new OTSLine3D(pointList);
                // clean line for projection inconsistencies (position -> center lines -> interpolated new position)
                double angleChange = Math.abs(line.getLocation(Length.ZERO).getRotZ() - startPosition.getRotZ());
                int i = 1;
                while (angleChange > Math.PI / 4)
                {
                    i++;
                    if (i >= pointList.size() - 2)
                    {
                        // return original if we can't clean the line, perhaps extreme road curvature or line with 2 points
                        return new OTSLine3D(pointList);
                    }
                    List<OTSPoint3D> newPointList = new ArrayList<>(pointList.subList(i, pointList.size()));
                    newPointList.add(0, pointList.get(0));
                    line = new OTSLine3D(newPointList);
                    angleChange = Math.abs(line.getLocation(Length.ZERO).getRotZ() - startPosition.getRotZ());
                }
                return line;
            }

            /**
             * Transform lateral to longitudinal fraction.
             * @param lateralFraction double; lateral fraction
             * @return double; transformation of lateral to longitudinal fraction
             */
            abstract double longitudinalFraction(double lateralFraction);

            /**
             * Transform longitudinal to lateral fraction.
             * @param longitudinalFraction double; longitudinal fraction
             * @return double; transformation of longitudinal to lateral fraction
             */
            abstract double lateralFraction(double longitudinalFraction);

        }

        /**
         * Returns a (partial) path for a lane change. The method is called both at the start and during a lane change, and
         * should return a valid path. This path should at least have a length of {@code planDistance}, unless the lane change
         * will be finished during the coming time step. In that case, the caller of this method is to lengthen the path along
         * the center line of the target lane.
         * @param timeStep Duration; time step
         * @param planDistance Length; distance covered during the operational plan
         * @param meanSpeed Speed; mean speed during time step
         * @param from DirectedLanePosition; current position on the from-lanes
         * @param startPosition DirectedPoint; current 2D position
         * @param laneChangeDirection LateralDirectionality; lane change direction
         * @param fromLine OTSLine3D; from line
         * @param toLine OTSLine3D; to line
         * @param laneChangeDuration Duration; current considered duration of the entire lane change
         * @param lcFraction double; fraction of lane change done so far
         * @return OTSLine3D a (partial) path for a lane change
         * @throws OTSGeometryException on wrong fractional position
         */
        OTSLine3D getPath(Duration timeStep, Length planDistance, Speed meanSpeed, DirectedLanePosition from,
                DirectedPoint startPosition, LateralDirectionality laneChangeDirection, OTSLine3D fromLine, OTSLine3D toLine,
                Duration laneChangeDuration, double lcFraction) throws OTSGeometryException;
    }
}
