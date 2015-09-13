package org.opentrafficsim.core.gtu.lane;

import java.util.Map;
import java.util.Set;

import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.animation.LaneChangeUrgeGTUColorer;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel;
import org.opentrafficsim.core.gtu.following.HeadwayGTU;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.lane.Lane;

/**
 * This interface defines a lane based GTU.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$,
 *          initial version Oct 22, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public interface LaneBasedGTU extends GTU
{
    /**
     * v(t) = v0 + (t - t0) * a.
     * @return the velocity of the GTU, in the direction of the lane
     */
    Speed.Abs getLongitudinalVelocity();

    /**
     * Return the speed of this GTU at the specified time. <br>
     * v(t) = v0 + (t - t0) * a
     * @param when time for which the speed must be returned
     * @return DoubleScalarAbs&lt;SpeedUnit&gt;; the speed at the specified time
     */
    Speed.Abs getLongitudinalVelocity(Time.Abs when);

    /**
     * @return the acceleration (or deceleration) of the GTU, in the direction of the lane
     */
    Acceleration.Abs getAcceleration();

    /**
     * @param when time for which the speed must be returned
     * @return the acceleration (or deceleration) of the GTU, in the direction of the lane.
     */
    Acceleration.Abs getAcceleration(Time.Abs when);

    /**
     * @return the velocity of the GTU, perpendicular to the direction of the lane. Positive lateral velocity is "left" compared
     *         to the driving direction.
     */
    Speed.Abs getLateralVelocity();

    /** @return DoubleScalarAbs&lt;TimeUnit&gt;; the time of last evaluation. */
    Time.Abs getLastEvaluationTime();

    /** @return DoubleScalarAbs&lt;TimeUnit&gt;; the time of next evaluation. */
    Time.Abs getNextEvaluationTime();

    /**
     * insert GTU at a certain position. This can happen at setup (first initialization), and after a lane change of the GTU.
     * The relative position that will be registered is the referencePosition (dx, dy, dz) = (0, 0, 0). Front and rear positions
     * are relative towards this position.
     * @param lane the lane to add to the list of lanes on which the GTU is registered.
     * @param position the position on the lane.
     * @throws NetworkException on network inconsistency
     */
    void enterLane(Lane lane, Length.Rel position) throws NetworkException;

    /**
     * Unregister the GTU from a lane.
     * @param lane the lane to remove from the list of lanes on which the GTU is registered.
     */
    void leaveLane(Lane lane);

    /**
     * Return the longitudinal positions of a point relative to this GTU, relative to the center line of the Lanes in which the
     * vehicle is registered. <br>
     * <b>Note:</b> If a GTU is registered in multiple parallel lanes, the lateralLaneChangeModel is used to determine the
     * center line of the vehicle at this point in time. Otherwise, the average of the center positions of the lines will be
     * taken.
     * @param relativePosition the position on the vehicle relative to the reference point.
     * @return the lanes and the position on the lanes where the GTU is currently registered, for the given position of the GTU.
     * @throws NetworkException when the vehicle is not on one of the lanes on which it is registered.
     */
    Map<Lane, Length.Rel> positions(RelativePosition relativePosition) throws NetworkException;

    /**
     * Return the longitudinal positions of a point relative to this GTU, relative to the center line of the Lanes in which the
     * vehicle is registered.
     * @param relativePosition the position on the vehicle relative to the reference point.
     * @param when the future time for which to calculate the positions.
     * @return the lanes and the position on the lanes where the GTU will be registered at the time, for the given position of
     *         the GTU.
     * @throws NetworkException when the vehicle is not on one of the lanes on which it is registered.
     */
    Map<Lane, Length.Rel> positions(RelativePosition relativePosition, Time.Abs when) throws NetworkException;

    /**
     * Return the longitudinal position of a point relative to this GTU, relative to the center line of the Lane at the current
     * simulation time. <br>
     * @param lane the position on this lane will be returned.
     * @param relativePosition the position on the vehicle relative to the reference point.
     * @return DoubleScalarAbs&lt;LengthUnit&gt;; the position, relative to the center line of the Lane.
     * @throws NetworkException when the vehicle is not on the given lane.
     */
    Length.Rel position(Lane lane, RelativePosition relativePosition) throws NetworkException;

    /**
     * Return the longitudinal position of a point relative to this GTU, relative to the center line of the Lane.
     * @param lane the position on this lane will be returned.
     * @param relativePosition the position on the vehicle relative to the reference point.
     * @param when the future time for which to calculate the positions.
     * @return DoubleScalarAbs&lt;LengthUnit&gt;; the position, relative to the center line of the Lane.
     * @throws NetworkException when the vehicle is not on the given lane.
     */
    Length.Rel position(Lane lane, RelativePosition relativePosition, Time.Abs when) throws NetworkException;

    /**
     * Return the longitudinal positions of a point relative to this GTU, relative to the center line of the Lanes in which the
     * vehicle is registered, as fractions of the length of the lane. This is important when we want to see if two vehicles are
     * next to each other and we compare an 'inner' and 'outer' curve.<br>
     * @param relativePosition the position on the vehicle relative to the reference point.
     * @return the lanes and the position on the lanes where the GTU is currently registered, for the given position of the GTU.
     * @throws NetworkException when the vehicle is not on one of the lanes on which it is registered.
     */
    Map<Lane, Double> fractionalPositions(RelativePosition relativePosition) throws NetworkException;

    /**
     * Return the longitudinal positions of a point relative to this GTU, relative to the center line of the Lanes in which the
     * vehicle is registered, as fractions of the length of the lane. This is important when we want to see if two vehicles are
     * next to each other and we compare an 'inner' and 'outer' curve.
     * @param relativePosition the position on the vehicle relative to the reference point.
     * @param when the future time for which to calculate the positions.
     * @return the lanes and the position on the lanes where the GTU will be registered at the time, for the given position of
     *         the GTU.
     * @throws NetworkException when the vehicle is not on one of the lanes on which it is registered.
     */
    Map<Lane, Double> fractionalPositions(RelativePosition relativePosition, Time.Abs when) throws NetworkException;

    /**
     * Return the longitudinal position of a point relative to this GTU, relative to the center line of the Lane, as a fraction
     * of the length of the lane. This is important when we want to see if two vehicles are next to each other and we compare an
     * 'inner' and 'outer' curve.
     * @param lane the position on this lane will be returned.
     * @param relativePosition the position on the vehicle relative to the reference point.
     * @param when the future time for which to calculate the positions.
     * @return the fractional relative position on the lane at the given time.
     * @throws NetworkException when the vehicle is not on the given lane.
     */
    double fractionalPosition(Lane lane, RelativePosition relativePosition, Time.Abs when) throws NetworkException;

    /**
     * Return the longitudinal position of a point relative to this GTU, relative to the center line of the Lane, as a fraction
     * of the length of the lane. This is important when we want to see if two vehicles are next to each other and we compare an
     * 'inner' and 'outer' curve.<br>
     * @param lane the position on this lane will be returned.
     * @param relativePosition the position on the vehicle relative to the reference point.
     * @return the fractional relative position on the lane at the given time.
     * @throws NetworkException when the vehicle is not on the given lane.
     */
    double fractionalPosition(Lane lane, RelativePosition relativePosition) throws NetworkException;

    /**
     * Return the longitudinal position that this GTU would have if it were to change to another Lane with a/the current
     * CrossSectionLink.
     * @param projectionLane Lane; the lane onto which the position of this GTU must be projected
     * @param relativePosition RelativePosition; the point on this GTU that must be projected
     * @param when DoubleScalar.Abs&lt;TimeUnit&gt;; the time for which to project the position of this GTU
     * @return DoubleScalar.Rel&lt;LengthUnit&gt;; the position of this GTU in the projectionLane
     * @throws NetworkException when projectionLane it not in any of the CrossSectionLink that the GTU is on
     */
    Length.Rel projectedPosition(Lane projectionLane, RelativePosition relativePosition, Time.Abs when)
        throws NetworkException;

    /**
     * Determine which GTU in front of this GTU, or behind this GTU. This method looks in all lanes where this GTU is
     * registered, and not further than the absolute value of the given maxDistance. The minimum headway is returned of all
     * Lanes where the GTU is registered. When no GTU is found within the given maxDistance, <b>null</b> is returned. The search
     * will extend into successive lanes if the maxDistance is larger than the remaining length on the lane. When Lanes (or
     * underlying CrossSectionLinks) diverge, the headway algorithms have to look at multiple Lanes and return the minimum
     * headway in each of the Lanes. When the Lanes (or underlying CrossSectionLinks) converge, "parallel" traffic is not taken
     * into account in the headway calculation. Instead, gap acceptance algorithms or their equivalent should guide the merging
     * behavior.<br>
     * <b>Note:</b> Headway is the net headway and calculated on a front-to-back basis.
     * @param maxDistance the maximum distance to look for the nearest GTU; positive values search forwards; negative values
     *            search backwards
     * @return HeadwayGTU; the headway and the GTU
     * @throws NetworkException when there is an error with the next lanes in the network.
     */
    HeadwayGTU headway(Length.Rel maxDistance) throws NetworkException;

    /**
     * Determine by what distance the front of this GTU is behind the rear an other GTU, or the rear of this GTU is ahead of the
     * front of an other GTU. Only positive values are returned. This method only looks in the given lane, and not further than
     * the given maxDistance. When no vehicle is found within the given maxDistance,
     * <code>new DoubleScalar.Rel&lt;LengthUnit&gt;(Double.MAX_VALUE, METER)</code> is returned. The search will extend into
     * successive lanes if the maxDistance is larger than the length of the lane.<br>
     * <b>Note:</b> Headway is the net headway and calculated on a front-to-back basis.
     * @param lane the lane to look for another GTU
     * @param maxDistance the maximum distance to look for; if positive, the search is forwards; if negative, the search is
     *            backwards
     * @return HeadwayGTU; the headway and the GTU
     * @throws NetworkException when the vehicle's route is inconclusive or vehicles are not registered correctly on their lanes
     */
    HeadwayGTU headway(Lane lane, Length.Rel maxDistance) throws NetworkException;

    /**
     * Determine which GTUs are parallel with us on another lane, based on fractional positions. <br>
     * Note: When the GTU that calls the method is also registered on the given lane, it is excluded from the return set.
     * @param lane the lane to look for parallel (partial or full overlapping) GTUs.
     * @param when the future time for which to calculate the headway
     * @return the set of GTUs parallel to us on the other lane (partial overlap counts as parallel), based on fractional
     *         positions, or an empty set when no GTUs were found.
     * @throws NetworkException when the vehicle's route is inconclusive, when vehicles are not registered correctly on their
     *             lanes, or when the given lane is not parallel to one of the lanes where we are registered.
     */
    Set<LaneBasedGTU> parallel(Lane lane, Time.Abs when) throws NetworkException;

    /**
     * Determine which GTUs are parallel with us in a certain lateral direction, based on fractional positions. <br>
     * Note 1: This method will look to the adjacent lanes of all lanes where the vehicle has been registered.<br>
     * Note 2: When the GTU that calls the method is also registered on the given lane, it is excluded from the return set.
     * @param lateralDirection the direction of the adjacent lane(s) to look for parallel (partial or full overlapping) GTUs.
     * @param when the future time for which to calculate the headway
     * @return the set of GTUs parallel to us on other lane(s) in the given direction (partial overlap counts as parallel),
     *         based on fractional positions, or an empty set when no GTUs were found.
     * @throws NetworkException when the vehicle's route is inconclusive, when vehicles are not registered correctly on their
     *             lanes, or when there are no lanes parallel to one of the lanes where we are registered in the given
     *             direction.
     */
    Set<LaneBasedGTU> parallel(LateralDirectionality lateralDirection, Time.Abs when) throws NetworkException;

    /**
     * Determine whether there is a lane to the left or to the right of this lane, which is accessible from this lane, or null
     * if no lane could be found. The method takes the LongitidinalDirectionality of the lane into account. In other words, if
     * we drive FORWARD and look for a lane on the LEFT, and there is a lane but the Directionality of that lane is not FORWARD
     * or BOTH, null will be returned.<br>
     * A lane is called adjacent to another lane if the lateral edges are not more than a delta distance apart. This means that
     * a lane that <i>overlaps</i> with another lane is <b>not</b> returned as an adjacent lane. <br>
     * The algorithm also looks for RoadMarkerAcross elements between the lanes to determine the lateral permeability for a GTU.
     * A RoadMarkerAcross is seen as being between two lanes if its center line is not more than delta distance from the
     * relevant lateral edges of the two adjacent lanes. <br>
     * When there are multiple lanes that are adjacent, which could e.g. be the case if an overlapping tram lane and a car lane
     * are adjacent to the current lane, the widest lane that best matches the GTU accessibility of the provided GTUType is
     * returned. <br>
     * <b>Note:</b> LEFT is seen as a negative lateral direction, RIGHT as a positive lateral direction. <br>
     * @param currentLane the lane to look for the best accessible adjacent lane
     * @param lateralDirection the direction (LEFT, RIGHT) to look at
     * @param longitudinalPosition DoubleScalar.Rel&lt;LengthUnit&gt;; the position of the GTU along this Lane
     * @return the lane if it is accessible, or null if there is no lane, it is not accessible, or the driving direction does
     *         not match.
     */
    Lane bestAccessibleAdjacentLane(Lane currentLane, LateralDirectionality lateralDirection,
        Length.Rel longitudinalPosition);

    /**
     * Determine the time when this GTU will have covered the specified distance from the position of the last evaluation time.
     * @param distance DoubleScalar.Rel&lt;LengthUnit&gt;; the distance
     * @return DoubleScalar.Rel&lt;TimeUnit&gt;; the time, or null if this GTU stops before covering the specified distance
     */
    Time.Abs timeAtDistance(Length.Rel distance);

    /**
     * Determine the time since last evaluation when this GTU has covered the specified distance from the position of the last
     * evaluation time.
     * @param distance DoubleScalar.Rel&lt;LengthUnit&gt;; the distance
     * @return DoubleScalar.Rel&lt;TimeUnit&gt;; the time difference from last evaluation or null if this GTU stops before
     *         covering the specified distance
     */
    Time.Rel deltaTimeForDistance(Length.Rel distance);

    /**
     * Retrieve the GTU following model of this LaneBasedGTU.
     * @return GTUFollowingModel
     */
    GTUFollowingModel getGTUFollowingModel();

    /**
     * Return the distance available for the next needed lane change and the lateral direction of that lane change.
     * @return LaneChangeAndDirection; the available distance and lateral direction for the next required lane change
     */
    LaneChangeUrgeGTUColorer.LaneChangeDistanceAndDirection getLaneChangeDistanceAndDirection();
}
