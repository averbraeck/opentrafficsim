package org.opentrafficsim.road.gtu.lane.plan.operational;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Angle;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.draw.curve.BezierCubic2d;
import org.djutils.draw.curve.Flattener2d;
import org.djutils.draw.curve.Flattener2d.MaxDeviationAndAngle;
import org.djutils.draw.point.DirectedPoint2d;
import org.djutils.draw.point.Point2d;
import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
import org.djutils.logger.CategoryLogger;
import org.djutils.math.AngleUtil;
import org.opentrafficsim.base.geometry.OtsLine2d;
import org.opentrafficsim.base.geometry.OtsLine2d.FractionalFallback;
import org.opentrafficsim.core.gtu.plan.operational.Segments;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LanePosition;
import org.opentrafficsim.road.network.lane.object.detector.LaneDetector;
import org.opentrafficsim.road.network.lane.object.detector.SinkDetector;

/**
 * Builder for several often used operational plans. E.g., decelerate to come to a full stop at the end of a shape; accelerate
 * to reach a certain speed at the end of a curve; drive constant on a curve; decelerate or accelerate to reach a given end
 * speed at the end of a curve, etc.<br>
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public final class LaneOperationalPlanBuilder
{

    /** Length within which GTUs snap to the lane center line. */
    private static final Length SNAP = Length.instantiateSI(1e-3);

    /** Typical lane width for which the typical lane change duration applies. */
    private static final Length LANE_WIDTH = Length.instantiateSI(3.5);

    /** Max angle of numerical simplification. */
    private static final double FLATTEN_ANGLE = Math.PI / 360.0;

    /** Flattener for paths. */
    private static final Flattener2d FLATTENER = new MaxDeviationAndAngle(0.01, FLATTEN_ANGLE);

    /** Constructor. */
    private LaneOperationalPlanBuilder()
    {
        // class should not be instantiated
    }

    /**
     * Builds a plan from any position not on a lane, towards a reasonable location on a lane on the route.
     * @param gtu GTU
     * @param acceleration acceleration
     * @param duration time step
     * @param tManeuver maneuver time, e.g. lane change
     * @return plan from any position not on a lane, to a reasonable location on a lane on the route
     * @throws IllegalStateException if the GTU is on a lane
     */
    public static LaneBasedOperationalPlan buildRoamingPlan(final LaneBasedGtu gtu, final Acceleration acceleration,
            final Duration duration, final Duration tManeuver)
    {
        gtu.setLaneChangeDirection(LateralDirectionality.NONE);
        LanePosition nearestPosition = gtu.getRoamingPosition();
        Length deviation = Length.ZERO;
        boolean deviative = true;
        OtsLine2d path = getPath(gtu, nearestPosition, acceleration, duration, tManeuver, deviation, deviative);
        Time now = gtu.getSimulator().getSimulatorAbsTime();
        Segments segments = Segments.off(gtu.getSpeed(), duration, acceleration);
        return Try.assign(() -> new LaneBasedOperationalPlan(gtu, path, now, segments, deviative),
                "Building roaming plan produced inconsistent LaneBasedOperationalPlan.");
    }

    /**
     * Build operational plan from a simple plan. Sets or resets the GTU lane change direction.
     * @param gtu GTU
     * @param simplePlan simple operational plan
     * @param tManeuver maneuver time, e.g. lane change
     * @return plan from position on a lane
     * @throws IllegalStateException if the GTU is not on a lane
     */
    public static LaneBasedOperationalPlan buildPlanFromSimplePlan(final LaneBasedGtu gtu,
            final SimpleOperationalPlan simplePlan, final Duration tManeuver)
    {
        Throw.when(gtu.getLane() == null, IllegalStateException.class,
                "Requested to build plan from simple plan for roaming GTU.");
        boolean deviative = false;
        LanePosition nearestPosition;
        if (simplePlan.isLaneChange())
        {
            if (gtu.isInstantaneousLaneChange())
            {
                gtu.changeLaneInstantaneously(simplePlan.getLaneChangeDirection());
                nearestPosition = gtu.getPosition();
            }
            else
            {
                gtu.setLaneChangeDirection(simplePlan.getLaneChangeDirection());
                deviative = true;
                nearestPosition = gtu.getPosition();
                // TODO: use different method to get Lane once lane methods are changed
                Lane lane = nearestPosition.lane().getAdjacent(simplePlan.getLaneChangeDirection(), gtu.getType());
                Throw.when(lane == null, IllegalStateException.class, "Starting lane change without adjacent lane.");
                double fraction = lane.getCenterLine().projectFractional(lane.getLink().getStartNode().getHeading(),
                        lane.getLink().getEndNode().getHeading(), gtu.getLocation().x, gtu.getLocation().y,
                        FractionalFallback.ENDPOINT);
                nearestPosition = new LanePosition(lane, lane.getLength().times(fraction));
            }
        }
        else
        {
            gtu.setLaneChangeDirection(LateralDirectionality.NONE);
            nearestPosition = gtu.getPosition();
        }
        deviative = deviative || simplePlan.getDeviation().si > SNAP.si
                || nearestPosition.getLocation().distance(gtu.getLocation()) > SNAP.si;
        OtsLine2d path = getPath(gtu, nearestPosition, simplePlan.getAcceleration(), simplePlan.getDuration(), tManeuver,
                simplePlan.getDeviation(), deviative);
        Time now = gtu.getSimulator().getSimulatorAbsTime();
        Segments segments = Segments.off(gtu.getSpeed(), simplePlan.getDuration(), simplePlan.getAcceleration());
        boolean finalDeviative = deviative;
        return Try.assign(() -> new LaneBasedOperationalPlan(gtu, path, now, segments, finalDeviative),
                "Building operational plan produced inconsistent LaneBasedOperationalPlan.");
    }

    /**
     * Returns a path towards a target point that is found by moving along the lanes from the nearest position.
     * @param gtu GTU
     * @param nearestPosition nearest position, i.e. the the location of the GTU projected to the (target) lane, or some closest
     *            point during roaming
     * @param acceleration acceleration
     * @param timeStep time step
     * @param tManeuver maneuver time, e.g. lane change
     * @param deviation desired deviation from lane center
     * @param deviative true if the GTU will not strictly follow the center line
     * @return path towards a target point that is found by moving along the lanes from the start position
     */
    private static OtsLine2d getPath(final LaneBasedGtu gtu, final LanePosition nearestPosition,
            final Acceleration acceleration, final Duration timeStep, final Duration tManeuver, final Length deviation,
            final boolean deviative)
    {
        Length laneGap = Length.ZERO;
        HorizonSpace horizonSpace =
                getHorizonSpace(gtu, nearestPosition, acceleration, timeStep, tManeuver, deviation, laneGap);
        if (deviative)
        {
            return bezierToHorizon(gtu, nearestPosition, deviation, horizonSpace);
        }
        return getCenterLinePath(gtu, nearestPosition, horizonSpace.horizon(), acceleration, timeStep, tManeuver, deviation);
    }

    /**
     * Create path as a Bezier to a target point that will be on the horizon.
     * @param gtu GTU
     * @param nearestPosition nearest position, i.e. the the location of the GTU projected to the (target) lane, or some closest
     *            point during roaming
     * @param deviation desired deviation from lane center
     * @param horizonSpace information regarding the considered horizon
     * @return path as a Bezier to a target point that will be on the horizon
     */
    private static OtsLine2d bezierToHorizon(final LaneBasedGtu gtu, final LanePosition nearestPosition, final Length deviation,
            final HorizonSpace horizonSpace)
    {
        DirectedPoint2d target = getTargetPoint(gtu, nearestPosition, deviation, horizonSpace);
        target = extrapolateToHorizon(gtu, target, horizonSpace.horizon());
        return bezierToTarget(gtu, target);
    }

    /**
     * Returns a path constructed from lane center lines. If a gap between lanes is found, this method will revert back to a
     * deviative path. The horizon is then recalculated including the found lane gap. A path to a target point on the horizon is
     * then returned.
     * @param gtu GTU
     * @param nearestPosition start position
     * @param length minimum
     * @param acceleration acceleration
     * @param timeStep time step
     * @param tManeuver maneuver time, e.g. lane change
     * @param deviation desired deviation from lane center
     * @return path constructed from lane center lines, or a bezier path in case of a lane gap between center lines
     */
    private static OtsLine2d getCenterLinePath(final LaneBasedGtu gtu, final LanePosition nearestPosition, final Length length,
            final Acceleration acceleration, final Duration timeStep, final Duration tManeuver, final Length deviation)
    {
        Length startDistance = nearestPosition.position();
        Lane lane = nearestPosition.lane();
        List<Point2d> points = new ArrayList<>();
        Point2d lastPoint = null;
        double cumulDist = 0.0;
        while (lane != null)
        {
            OtsLine2d centerLine =
                    startDistance.gt0() ? lane.getCenterLine().extract(startDistance, lane.getLength()) : lane.getCenterLine();
            if (lastPoint != null && lastPoint.distance(centerLine.getFirst()) > SNAP.si)
            {
                // It is not appropriate to follow the lane center lines due to a lane gap
                Length laneGap = Length.instantiateSI(lastPoint.distance(centerLine.getFirst()));
                HorizonSpace horizonSpace =
                        getHorizonSpace(gtu, nearestPosition, acceleration, timeStep, tManeuver, deviation, laneGap);
                return bezierToHorizon(gtu, nearestPosition, deviation, horizonSpace);
            }
            for (Point2d point : centerLine)
            {
                if (lastPoint != null)
                {
                    double d = lastPoint.distance(point);
                    if (d < SNAP.si)
                    {
                        continue;
                    }
                    cumulDist += d;
                }
                points.add(point);
                if (cumulDist >= length.si)
                {
                    return new OtsLine2d(points);
                }
                lastPoint = point;
            }
            startDistance = Length.ZERO;
            lane = gtu.getNextLaneForRoute(lane);
        }
        // Minimum length not reached, add extrapolated point to reach required length
        Point2d lastLastPoint = points.get(points.size() - 2);
        double direction = lastLastPoint.directionTo(lastPoint);
        double r = length.si - cumulDist + SNAP.si;
        points.add(lastPoint.translate(r * Math.cos(direction), r * Math.sin(direction)));
        return new OtsLine2d(points);
    }

    /**
     * Returns relevant information regarding the considered horizon. The horizon (radius) is determined as:
     * <ul>
     * <li>At least the length of the operational plan</li>
     * <li>At least the GTU turn radius (=diameter)</li>
     * <li>At least several factors on tManeuver at the current speed:
     * <ul>
     * <li>[0...1] for a deviation from the desired deviation of [0...3.5]m, or 1 for larger deviation</li>
     * <li>[0...1] for an angle of [0...pi/4] between the direction of the GTU and the direction at the target, or 1 for larger
     * angles</li>
     * <li>[2...0] for an angle of [0...pi/2] between the direction of the GTU and the direction towards the target, or 0 for
     * larger angles</li>
     * </ul>
     * </li>
     * </ul>
     * The bullet on deviation implements a typical lane change horizon.<br>
     * The before-last bullet reflects that maneuvers take more length when the GTU is at an angle.<br>
     * The last bullet reflects roaming situations where the GTU is approaching the target at a near-right angle, in which case
     * more rotation is required than a normal maneuver, and hence a factor larger than 1 is applied.
     * @param gtu GTU
     * @param nearestPosition nearest position, i.e. the location of the GTU projected to the (target) lane, or some closest
     *            point during roaming
     * @param acceleration acceleration
     * @param timeStep time step
     * @param tManeuver maneuver time, e.g. lane change
     * @param deviation desired deviation from lane center
     * @param laneGap known gap between longitudinally connected lanes
     * @return information regarding the considered horizon
     */
    private static HorizonSpace getHorizonSpace(final LaneBasedGtu gtu, final LanePosition nearestPosition,
            final Acceleration acceleration, final Duration timeStep, final Duration tManeuver, final Length deviation,
            final Length laneGap)
    {
        DirectedPoint2d nearestPoint = nearestPosition.getLocation();
        double dx = nearestPoint.x - gtu.getLocation().x;
        double dy = nearestPoint.y - gtu.getLocation().y;
        double dCenterLine = Math.hypot(dx, dy);
        double leftOfLane;
        if (dCenterLine <= SNAP.si)
        {
            leftOfLane = 1.0;
        }
        else if (dy == 0.0)
        {
            leftOfLane = -Math.signum(dx);
        }
        else if (dx == 0.0)
        {
            leftOfLane = -Math.signum(dy);
        }
        else
        {
            leftOfLane = Math.signum(Math.cos(Math.sin(nearestPoint.dirZ) * dx - nearestPoint.dirZ) * dy);
        }
        Length distanceToNearest = Length.instantiateSI(Math.abs(deviation.si - leftOfLane * dCenterLine));

        // Lateral deviation: 0 to 1 within first {LANE_WIDTH}m (also applies to lane gap)
        double fLatDeviation = Math.min(1.0, Math.max(distanceToNearest.si, laneGap.si) / LANE_WIDTH.si);

        // Difference direction at target point and vehicle direction: 0 to 1 within pi/4
        double dDirection = Math.abs(AngleUtil.normalizeAroundZero(nearestPoint.dirZ - gtu.getLocation().dirZ));
        double fDirection = Math.min(1, 4 * dDirection / Math.PI);

        // Difference direction to target and vehicle direction: 2 to 0 within pi/2
        // For these maneuvers more time is required than a normal lane change
        Direction dirToNearest = Direction.instantiateSI(Math.atan2(dy, dx));
        double fToTarget = 0.0;
        if (dCenterLine > SNAP.si)
        {
            double dToTarget = Math.abs(AngleUtil.normalizeAroundZero(dirToNearest.si - gtu.getLocation().dirZ));
            fToTarget = Math.max(0, 2 - 4 * dToTarget / Math.PI);
        }

        // Combine factors
        double tHorizon = tManeuver.si * Math.max(Math.max(fLatDeviation, fDirection), fToTarget);

        // Figure out horizon
        Length turnDiameter = gtu.getVehicleModel().getTurnRadius(gtu);
        double rPlan;
        if (acceleration.lt0() && gtu.getSpeed().si / -acceleration.si < timeStep.si)
        {
            double t = gtu.getSpeed().si / -acceleration.si;
            rPlan = gtu.getSpeed().si * t + .5 * acceleration.si * t * t;
        }
        else
        {
            rPlan = gtu.getSpeed().si * timeStep.si + .5 * acceleration.si * timeStep.si * timeStep.si;
        }
        Length horizon = Length.instantiateSI(Math.max(Math.max(turnDiameter.si, rPlan), gtu.getSpeed().si * tHorizon));
        return new HorizonSpace(distanceToNearest, dirToNearest, horizon, turnDiameter, nearestPoint);
    }

    /**
     * Hold information on the horizon.
     * @param distanceToNearest distance towards the nearest point
     * @param dirToNearest distance to the nearest point
     * @param horizon horizon radius
     * @param turnDiameter GTU turn radius (=diameter)
     * @param nearestPoint nearest point, e.g. GTU position projected to (target) lane
     */
    private record HorizonSpace(Length distanceToNearest, Direction dirToNearest, Length horizon, Length turnDiameter,
            DirectedPoint2d nearestPoint)
    {
    }

    /**
     * Returns a target point that is found by moving along the lanes from the nearest position. The general procedure is:
     * <ul>
     * <li>Far (closest point on lane is beyond horizon)</li>
     * <ul>
     * <li>Ahead (closest point on lane is within pi/4 of straight ahead)</li>
     * <ul>
     * <li>go to closest point on lane</li>
     * </ul>
     * <li>Behind</li>
     * <ul>
     * <li>turn to horizon edge closest to closest point on lane</li>
     * </ul>
     * </ul>
     * <li>Close</li>
     * <ul>
     * <li>Intersect (horizon intersects lane within pi/4* of straight ahead)</li>
     * <ul>
     * <li>go to point where horizon intersects lane</li>
     * </ul>
     * <li>Prevent turn flipping (horizon edges closest to same point on lane)</li>
     * <ul>
     * <li>go to point on lane closest to either horizon edge</li>
     * </ul>
     * <li>Turn</li>
     * <ul>
     * <li>turn to horizon edge closest to lane</li>
     * </ul>
     * </ul>
     * </ul>
     * *) or narrower when limited by vehicle turning radius
     * @param gtu GTU
     * @param nearestPosition nearest position, i.e. the the location of the GTU projected to the (target) lane, or some closest
     *            point during roaming
     * @param deviation desired deviation from lane center
     * @param horizonSpace information on horizon
     * @return target point that is found by moving along the lanes from the nearest position
     */
    private static DirectedPoint2d getTargetPoint(final LaneBasedGtu gtu, final LanePosition nearestPosition,
            final Length deviation, final HorizonSpace horizonSpace)
    {
        if (horizonSpace.distanceToNearest().gt(horizonSpace.horizon()))
        {
            // Far away: lane is beyond horizon, go to closest point
            if (Math.abs(horizonSpace.dirToNearest().si - gtu.getLocation().dirZ) < Math.PI / 4.0)
            {
                // Closest point is ahead, use direction but limit to horizon for reasonable path curvature
                return new DirectedPoint2d(
                        gtu.getLocation().x + horizonSpace.horizon().si * Math.cos(horizonSpace.dirToNearest().si),
                        gtu.getLocation().y + horizonSpace.horizon().si * Math.sin(horizonSpace.dirToNearest().si),
                        horizonSpace.dirToNearest().si);
            }
            else
            {
                // Closest point is behind, go to edge of the horizon (left or right) that is closest to closest point on lane
                double alpha = Math.PI / 2.0;
                double alphaMin = gtu.getLocation().dirZ - alpha;
                double alphaMax = gtu.getLocation().dirZ + alpha;

                double x1 = gtu.getLocation().x + horizonSpace.horizon().si * Math.cos(alphaMin);
                double y1 = gtu.getLocation().y + horizonSpace.horizon().si * Math.sin(alphaMin);
                Point2d nearest1 = nearestPosition.lane().getCenterLine().closestPointOnPolyLine(new Point2d(x1, y1));
                double dist1sq = Math.hypot(x1 - nearest1.x, y1 - nearest1.y);

                double x2 = gtu.getLocation().x + horizonSpace.horizon().si * Math.cos(alphaMax);
                double y2 = gtu.getLocation().y + horizonSpace.horizon().si * Math.sin(alphaMax);
                Point2d nearest2 = nearestPosition.lane().getCenterLine().closestPointOnPolyLine(new Point2d(x2, y2));
                double dist2sq = Math.hypot(x2 - nearest2.x, y2 - nearest2.y);

                if (nearest1.directionTo(nearest2) < SNAP.si)
                {
                    // Same point, let's not flip left/right each step, but just go there
                    double dirToAdjustedTarget = Math.atan2(nearest1.y - gtu.getLocation().y, nearest1.x - gtu.getLocation().x);
                    return new DirectedPoint2d(nearest1, dirToAdjustedTarget);
                }
                if (dist1sq <= dist2sq)
                {
                    return new DirectedPoint2d(x1, y1, gtu.getLocation().dirZ + Math.PI);
                }
                return new DirectedPoint2d(x2, y2, gtu.getLocation().dirZ - Math.PI);
            }
        }

        // Close(ish): go to point where horizon intersects lane
        double alpha = Math.PI / 4.0;
        double rVehicle = horizonSpace.turnDiameter().si;
        double rHorizon = horizonSpace.horizon().si;
        if (rVehicle > .5 * rHorizon)
        {
            // Not the whole horizon might be reachable due to turn radius

            /* {@formatter:off}
             * Intersection of 2 circles to find max horizon angle (alpha|a)
             *  1) circle with radius rHorizon around (0, 0) = A
             *  2) circle with radius rVehicle around (-rVehicle, 0) = B
             * Intersection P creates triangle with circle centers A and B.
             * Arc A-P is how the vehicle can turn towards horizon at P.
             * Side A-P of length rHorizon is split at mid-point "#".
             * A new line B-# creates two right triangles /\#-B-P & /\#-B-A.
             * Line B-# has length "z". To find alpha:
             *  - Note that /_P-A-B is 90deg - a. Angle /_#-B-A is 90deg
             *    minus /_P-A-B, and therefore /_#-B-A = a.
             *  - From this: a = arctan(.5 * rHorizon / z) =>
             *    z = .5 * rHorizon / tan(a)
             *  - Pythagoras gives: z = sqrt(rVehicle^2 - (.5 * rHorizon)^2)
             *  - Equating and solving for a, simplifying fraction in tan by
             *    multiplying numerator and denominator by 2=sqrt(4), gives:
             *
             *     a = arctan(rHorizon / sqrt(4 * rVehicle^2 - rHorizon^2))
             *
             *            T    |   | <--"ahead" in plane of vehicle
             *             '.2a|   |
             *               '.P-''|''-..
             *    rVehicle .-'  \  |rHorizon
             *          .-'/ rHor#a|       \
             *       .-'  /       \|        \ <--horizon at rHorizon
             *     B---------------A         |
             *       ^  rVehicle    (0, 0)  /
             *       |     \               /
             *  turn radius '.           .'
             *                '-..___..-'
             *
             * {@formatter:on}
             */
            alpha = Math.min(alpha, Math.atan(rHorizon / Math.sqrt(4 * rVehicle * rVehicle - rHorizon * rHorizon)));
        }

        // Return intersection of lane path and horizon
        LanePosition endPosition =
                getTargetLanePosition(gtu, nearestPosition, horizonSpace.horizon(), Angle.instantiateSI(alpha));
        if (endPosition != null)
        {
            return endPosition.getLocation();
        }

        // Make turn
        double alphaMin = gtu.getLocation().dirZ - alpha;
        double alphaMax = gtu.getLocation().dirZ + alpha;
        Point2d pMin = new Point2d(gtu.getLocation().x + rHorizon * Math.cos(alphaMin),
                gtu.getLocation().y + rHorizon * Math.sin(alphaMin));
        Point2d nearest1 = nearestPosition.lane().getCenterLine().closestPointOnPolyLine(pMin);
        double dist1sq = nearest1.distance(pMin);
        Point2d pMax = new Point2d(gtu.getLocation().x + rHorizon * Math.cos(alphaMax),
                gtu.getLocation().y + rHorizon * Math.sin(alphaMax));
        Point2d nearest2 = nearestPosition.lane().getCenterLine().closestPointOnPolyLine(pMax);
        double dist2sq = nearest2.distance(pMax);
        if (nearest1.distance(nearest2) < SNAP.si)
        {
            // Same point, let's not flip left/right every step, but just go there
            return new DirectedPoint2d(nearest1, gtu.getLocation().directionTo(nearest1));
        }
        else if (dist1sq <= dist2sq)
        {
            /*
             * 2 * alpha is added or subtracted. This is to obtain the direction of line line P-T in the figure above, which is
             * the same as the angle of the arc A-P at P. The arc hits P at an angle 'a' with the line A-P (symmetry). The line
             * A-P is also at an angle 'a' relative to vertical. Hence, line P-T makes an angle of 2a as this is the opposite
             * corner between vertical and P-T.
             */
            return new DirectedPoint2d(nearest1, gtu.getLocation().dirZ - 2 * alpha);
        }
        return new DirectedPoint2d(nearest2, gtu.getLocation().dirZ + 2 * alpha);
    }

    /**
     * Returns the target position by following lanes from the start position until a point is found that is at the horizon
     * distance removed from the GTU. If such a point is not within the viewport, {@code null} is returned. This method is part
     * of {@code getTargetPoint()}, but also used for non-deviative plans directly.
     * @param gtu GTU
     * @param startPosition start position of path, which should be the projected location on an adjacent lane for a lane change
     * @param horizon distance as the crow flies of the next target point
     * @param viewport horizontal angle within which the horizon is considered, relative to straight ahead, both left and right
     * @return lane path for a movement step of a GTU, {@code null} if no such point within viewport
     */
    private static LanePosition getTargetLanePosition(final LaneBasedGtu gtu, final LanePosition startPosition,
            final Length horizon, final Angle viewport)
    {
        DirectedPoint2d loc0 = gtu.getLocation();
        OtsLine2d laneCenter =
                startPosition.lane().getCenterLine().extract(startPosition.position(), startPosition.lane().getLength());
        Lane lane = startPosition.lane();
        Set<Lane> coveredLanes = new LinkedHashSet<>();
        double r2 = horizon.si * horizon.si;
        double distCumulLane = startPosition.position().si;
        while (!coveredLanes.contains(lane))
        {
            coveredLanes.add(lane);

            // If the first point on the next lane is beyond the horizon, the horizon is in a gap between two lanes. The first
            // point of the next lane can be considered the focus point.
            if (Math.hypot(laneCenter.getFirst().x - loc0.x, laneCenter.getFirst().y - loc0.y) > horizon.si)
            {
                double alpha = Math.atan2(laneCenter.getFirst().y - loc0.y, laneCenter.getFirst().x - loc0.x) - loc0.dirZ;
                return Math.abs(alpha) <= viewport.si ? new LanePosition(lane, Length.ZERO) : null;
            }

            // Find horizon point on lane
            for (int i = 0; i < laneCenter.size() - 1; i++)
            {
                double dx = laneCenter.getX(i + 1) - laneCenter.getX(i);
                double dy = laneCenter.getY(i + 1) - laneCenter.getY(i);
                double dr = Math.hypot(dx, dy);

                // Check if line segment crosses circle (method from https://mathworld.wolfram.com/Circle-LineIntersection.html)
                double det = (laneCenter.getX(i) - loc0.x) * (laneCenter.getY(i + 1) - loc0.y)
                        - (laneCenter.getX(i + 1) - loc0.x) * (laneCenter.getY(i) - loc0.y);
                double dr2 = dr * dr;
                double det2 = det * det;
                double discriminant = r2 * dr2 - det2;
                if (discriminant >= 0.0)
                {
                    double sgn = dy < 0.0 ? -1.0 : 1.0;
                    double sqrtDisc = Math.sqrt(discriminant);
                    // Up to two crossing points
                    double pointSign = 1.0;
                    for (int j = 0; j < 2; j++)
                    {
                        double xP = loc0.x + (det * dy + pointSign * sgn * dx * sqrtDisc) / dr2;
                        double yP = loc0.y + (-det * dx + pointSign * Math.abs(dy) * sqrtDisc) / dr2;
                        double alpha = AngleUtil.normalizeAroundZero(Math.atan2(yP - loc0.y, xP - loc0.x) - loc0.dirZ);
                        // In viewing port?
                        if (Math.abs(alpha) <= viewport.si)
                        {
                            double f = Math.abs(dx) > 0.0 && Math.abs(dx) > Math.abs(dy) ? (xP - laneCenter.getX(i)) / dx
                                    : (dy == 0.0 ? 0.0 : (yP - laneCenter.getY(i)) / dy);
                            // Crosses within line segment?
                            if (f >= 0.0 && f <= 1.0)
                            {
                                return new LanePosition(lane, Length.instantiateSI(distCumulLane + f * dr));
                            }
                        }
                        pointSign = -1.0;
                    }
                }
                distCumulLane += dr;
            }

            // Move to next lane
            Lane nextLane = gtu.getNextLaneForRoute(lane);
            if (nextLane == null)
            {
                return new LanePosition(lane, Length.instantiateSI(startPosition.lane().getCenterLine().getLength()));
            }
            lane = nextLane;
            laneCenter = lane.getCenterLine();
            distCumulLane = 0.0;
        }

        // Encountered a loop, return last position if within alpha
        double alpha = AngleUtil
                .normalizeAroundZero(Math.atan2(laneCenter.getLast().y - loc0.y, laneCenter.getLast().x - loc0.x) - loc0.dirZ);
        return Math.abs(alpha) <= viewport.si ? new LanePosition(lane, lane.getLength()) : null;
    }

    /**
     * Extrapolate target point further in its direction up to horizon, if it is closer than horizon. This can happen if the end
     * of a route is reached
     * @param gtu GTU
     * @param target target point
     * @param horizon horizon
     * @return extrapolated target point further in its direction up to horizon
     */
    private static DirectedPoint2d extrapolateToHorizon(final LaneBasedGtu gtu, final DirectedPoint2d target,
            final Length horizon)
    {
        double dx = target.x - gtu.getLocation().x;
        double dy = target.y - gtu.getLocation().y;
        double dist = Math.hypot(dx, dy);
        if (dist >= horizon.si)
        {
            return target;
        }
        /*
         * {@formatter:off}
         * Relative to vehicle (A), we need a point P on the horizon at an
         * angle 'a'. This point is 'x' extrapolated beyond target (B) at
         * angle target.rotZ.
         *  x = dx + x * cos(target.rotZ) = horizon * cos(a)       [1]
         *  y = dy + x * sin(target.rotZ) = horizon * sin(a)       [2]
         * Solving a = f(...) for [2] and substituting in [1], and
         * solving this for s, gives a large equation with two solutions.
         *
         *                 ..--''' <-- Horizon at horizon from A
         * target.rotZ  .-'x
         *     <-------P------B (dx, dy) <-- target
         *      (x, y)' ''-.  a\
         *           |      ''-.A <-- vehicle
         *            .          (0, 0)
         * {@formatter:on}
         */
        double cosTarget = Math.cos(target.dirZ);
        double sinTarget = Math.sin(target.dirZ);
        double c = cosTarget * dx;
        double s = sinTarget * dy;
        double d = Math
                .sqrt(-cosTarget * cosTarget * dy * dy + 2 * c * s - sinTarget * sinTarget * dx * dx + horizon.si * horizon.si);
        double x = Math.max(-c - s - d, -c - s + d); // positive solution
        return new DirectedPoint2d(target.x + x * cosTarget, target.y + x * sinTarget, target.dirZ);
    }

    /**
     * Create Bezier path from current location of the GTU towards the target point. The path is flattened with a default
     * flattener using a maximum deviation of 0.1m and maximum angle 0.5 degrees.
     * @param gtu GTU
     * @param target target point
     * @return Bezier path from current location of the GTU towards the target point
     */
    private static OtsLine2d bezierToTarget(final LaneBasedGtu gtu, final DirectedPoint2d target)
    {
        double angleShift = Math.abs(AngleUtil.normalizeAroundZero(gtu.getLocation().dirZ - target.dirZ));
        double dirToTarget = gtu.getLocation().directionTo(target);
        if (angleShift < FLATTEN_ANGLE && Math.abs(AngleUtil.normalizeAroundZero(dirToTarget - target.dirZ)) < FLATTEN_ANGLE
                && Math.abs(AngleUtil.normalizeAroundZero(dirToTarget - gtu.getLocation().dirZ)) < FLATTEN_ANGLE)
        {
            // current position and direction sufficiently in line with target position and direction to simplify as straight
            return new OtsLine2d(gtu.getLocation(), target);
        }
        // Shape points at shapeFactor of inter-point distance:
        // 1/3rd when angle between points < pi/2
        // then increases linearly to 2/3rds for an angle of pi
        double shapeFactor = (1.0 + 2.0 * Math.max(0.0, angleShift - 0.5 * Math.PI) / Math.PI) / 3;
        double rControl = shapeFactor * Math.hypot(gtu.getLocation().x - target.x, gtu.getLocation().y - target.y);
        Point2d p2 = new Point2d(gtu.getLocation().x + rControl * Math.cos(gtu.getLocation().dirZ),
                gtu.getLocation().y + rControl * Math.sin(gtu.getLocation().dirZ));
        Point2d p3 = new Point2d(target.x - rControl * Math.cos(target.dirZ), target.y - rControl * Math.sin(target.dirZ));
        BezierCubic2d bezier = new BezierCubic2d(gtu.getLocation(), p2, p3, target);
        return new OtsLine2d(bezier.toPolyLine(FLATTENER));
    }

    /**
     * Creates a path along lane center lines.
     * @param gtu gtu
     * @param distance minimum distance
     * @return path along lane center lines
     */
    @Deprecated
    public static OtsLine2d createPathAlongCenterLine(final LaneBasedGtu gtu, final Length distance)
    {
        OtsLine2d path = null;
        LanePosition ref = gtu.getPosition();
        double f = ref.lane().fraction(ref.position());
        if (f < 1.0)
        {
            path = ref.lane().getCenterLine().extractFractional(Math.max(f, 0.0), 1.0);
        }
        Lane prevFrom = null;
        Lane from = ref.lane();
        Length prevPos = null;
        Length pos = ref.position();
        int n = 1;
        while (path == null || path.getLength() < distance.si + n * Lane.MARGIN.si)
        {
            n++;
            prevFrom = from;
            if (null == from)
            {
                CategoryLogger.always().warn("About to die: GTU {} has null from value", gtu.getId());
            }
            from = gtu.getNextLaneForRoute(from);
            // if (from != null && from.getType().equals(Lane.SHOULDER))
            // {
            // CategoryLogger.always().warn("GTU {} on link {} will move on to shoulder.", gtu.getId(),
            // ref.getLane().getLink().getId());
            // }
            prevPos = pos;
            pos = Length.ZERO;
            if (from == null)
            {
                // check sink detector
                for (LaneDetector detector : prevFrom.getDetectors(prevPos, prevFrom.getLength(), gtu.getType()))
                {
                    if (detector instanceof SinkDetector && ((SinkDetector) detector).willDestroy(gtu))
                    {
                        // just add some length so the GTU is happy to go to the sink
                        DirectedPoint2d end = path.getLocationExtendedSI(distance.si + n * Lane.MARGIN.si);
                        List<Point2d> points = path.getPointList();
                        points.add(end);
                        return new OtsLine2d(points);
                    }
                }
                // force lane change, and create path from there
                for (LateralDirectionality lat : new LateralDirectionality[] {LateralDirectionality.LEFT,
                        LateralDirectionality.RIGHT})
                {
                    Lane latLane = prevFrom.getAdjacentLane(lat, gtu.getType());
                    if (latLane != null && gtu.getNextLaneForRoute(latLane) != null)
                    {
                        gtu.changeLaneInstantaneously(lat);
                        CategoryLogger.always().warn("GTU {} on link {} is forced to change lane towards {}", gtu.getId(),
                                ref.lane().getLink().getId(), lat);
                        return createPathAlongCenterLine(gtu, distance);
                    }
                }
                CategoryLogger.always().error("GTU {} on link {} has nowhere to go and no sink detector either", gtu.getId(),
                        ref.lane().getLink().getId());
                gtu.destroy();
                return path;
            }
            if (path == null)
            {
                path = from.getCenterLine();
            }
            else
            {
                path = OtsLine2d.concatenate(Lane.MARGIN.si, path, from.getCenterLine());
            }
        }
        return path;
    }

}
