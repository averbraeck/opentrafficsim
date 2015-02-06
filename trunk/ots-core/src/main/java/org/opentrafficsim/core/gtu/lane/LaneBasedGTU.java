package org.opentrafficsim.core.gtu.lane;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.Set;

import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * The starting point of a LaneBasedTU is that it can be in <b>multiple lanes</b> at the same time. This can be due to a
 * lane change (lateral), or due to crossing a link (front of the GTU is on another Lane than rear of the GTU). If a
 * Lane is shorter than the length of the GTU (e.g. when we do node expansion on a crossing, this is very well
 * possible), a GTU could occupy dozens of Lanes at the same time.
 * <p>
 * When calculating a headway, the GTU has to look in successive lanes. When Lanes (or underlying CrossSectionLinks)
 * diverge, the headway algorithms have to look at multiple Lanes and return the minimum headway in each of the Lanes.
 * When the Lanes (or underlying CrossSectionLinks) converge, "parallel" traffic is not taken into account in the
 * headway calculation. Instead, gap acceptance algorithms or their equivalent should guide the merging behavior.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Oct 22, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <ID> The type of ID, e.g., String or Integer
 */
public interface LaneBasedGTU<ID> extends GTU<ID>
{
    /**
     * v(t) = v0 + (t - t0) * a.
     * @return the velocity of the GTU, in the direction of the lane
     * @throws RemoteException in case of problems getting the simulation time.
     */
    DoubleScalar.Abs<SpeedUnit> getLongitudinalVelocity() throws RemoteException;

    /**
     * Return the speed of this GTU at the specified time. <br>
     * v(t) = v0 + (t - t0) * a
     * @param when time for which the speed must be returned
     * @return DoubleScalarAbs&lt;SpeedUnit&gt;; the speed at the specified time
     */
    DoubleScalar.Abs<SpeedUnit> getLongitudinalVelocity(DoubleScalar.Abs<TimeUnit> when);

    /**
     * @return the acceleration (or deceleration) of the GTU, in the direction of the lane
     * @throws RemoteException in case of problems getting the simulation time.
     */
    DoubleScalar.Abs<AccelerationUnit> getAcceleration() throws RemoteException;

    /**
     * @param when time for which the speed must be returned
     * @return the acceleration (or deceleration) of the GTU, in the direction of the lane.
     */
    DoubleScalar.Abs<AccelerationUnit> getAcceleration(DoubleScalar.Abs<TimeUnit> when);

    /**
     * @return the velocity of the GTU, perpendicular to the direction of the lane. Positive lateral velocity is "left"
     *         compared to the driving direction.
     */
    DoubleScalar.Abs<SpeedUnit> getLateralVelocity();

    /** @return DoubleScalarAbs&lt;TimeUnit&gt;; the time of last evaluation. */
    DoubleScalar.Abs<TimeUnit> getLastEvaluationTime();

    /** @return DoubleScalarAbs&lt;TimeUnit&gt;; the time of next evaluation. */
    DoubleScalar.Abs<TimeUnit> getNextEvaluationTime();

    /**
     * Register the lane with a GTU, at the start of the lane. This means that the GTU will get an longitudinal position
     * equal to 0.0 meters. The relative position that will be registered is the referencePosition (dx, dy, dz) = (0, 0,
     * 0). Front and rear positions are relative towards this position.
     * @param lane the lane to add to the list of lanes on which the GTU is registered.
     */
    void addLane(Lane lane);

    /**
     * insert GTU at a certain position. This can happen at setup (first initialization), and after a lane change of the
     * GTU. The relative position that will be registered is the referencePosition (dx, dy, dz) = (0, 0, 0). Front and
     * rear positions are relative towards this position.
     * @param lane the lane to add to the list of lanes on which the GTU is registered.
     * @param position the position on the lane.
     */
    void addLane(Lane lane, DoubleScalar.Rel<LengthUnit> position);

    /**
     * Unregister the GTU from a lane.
     * @param lane the lane to remove from the list of lanes on which the GTU is registered.
     */
    void removeLane(Lane lane);

    /**
     * Return the longitudinal positions of a point relative to this GTU, relative to the center line of the Lanes in
     * which the vehicle is registered. <br>
     * <b>Note:</b> If a GTU is registered in multiple parallel lanes, the lateralLaneChangeModel is used to determine
     * the center line of the vehicle at this point in time. Otherwise, the average of the center positions of the lines
     * will be taken.
     * @param relativePosition the position on the vehicle relative to the reference point.
     * @return the lanes and the position on the lanes where the GTU is currently registered, for the given position of
     *         the GTU.
     * @throws RemoteException when simulator time cannot be retrieved.
     * @throws NetworkException when the vehicle is not on one of the lanes on which it is registered.
     */
    Map<Lane, DoubleScalar.Rel<LengthUnit>> positions(RelativePosition relativePosition) throws NetworkException,
            RemoteException;

    /**
     * Return the longitudinal positions of a point relative to this GTU, relative to the center line of the Lanes in
     * which the vehicle is registered. <br>
     * s(t) = s0 + v0 * (t - t0) + 0.5 . a . (t - t0)^2
     * @param relativePosition the position on the vehicle relative to the reference point.
     * @param when the future time for which to calculate the positions.
     * @return the lanes and the position on the lanes where the GTU will be registered at the time, for the given
     *         position of the GTU.
     * @throws NetworkException when the vehicle is not on one of the lanes on which it is registered.
     */
    Map<Lane, DoubleScalar.Rel<LengthUnit>> positions(RelativePosition relativePosition, DoubleScalar.Abs<TimeUnit> when)
            throws NetworkException;

    /**
     * Return the longitudinal position of a point relative to this GTU, relative to the center line of the Lane. <br>
     * @param lane the position on this lane will be returned.
     * @param relativePosition the position on the vehicle relative to the reference point.
     * @return DoubleScalarAbs&lt;LengthUnit&gt;; the position, relative to the center line of the Lane.
     * @throws NetworkException when the vehicle is not on the given lane.
     * @throws RemoteException when simulator time cannot be retrieved.
     */
    DoubleScalar.Rel<LengthUnit> position(Lane lane, RelativePosition relativePosition) throws NetworkException,
            RemoteException;

    /**
     * Return the longitudinal position of a point relative to this GTU, relative to the center line of the Lane. <br>
     * s(t) = s0 + v0 * (t - t0) + 0.5 . a . (t - t0)^2
     * @param lane the position on this lane will be returned.
     * @param relativePosition the position on the vehicle relative to the reference point.
     * @param when the future time for which to calculate the positions.
     * @return DoubleScalarAbs&lt;LengthUnit&gt;; the position, relative to the center line of the Lane.
     * @throws NetworkException when the vehicle is not on the given lane.
     */
    DoubleScalar.Rel<LengthUnit> position(Lane lane, RelativePosition relativePosition, DoubleScalar.Abs<TimeUnit> when)
            throws NetworkException;

    /**
     * Return the longitudinal positions of a point relative to this GTU, relative to the center line of the Lanes in
     * which the vehicle is registered, as fractions of the length of the lane. This is important when we want to see if
     * two vehicles are next to each other and we compare an 'inner' and 'outer' curve.<br>
     * @param relativePosition the position on the vehicle relative to the reference point.
     * @return the lanes and the position on the lanes where the GTU is currently registered, for the given position of
     *         the GTU.
     * @throws RemoteException when simulator time cannot be retrieved.
     * @throws NetworkException when the vehicle is not on one of the lanes on which it is registered.
     */
    Map<Lane, Double> fractionalPositions(RelativePosition relativePosition) throws NetworkException, RemoteException;

    /**
     * Return the longitudinal positions of a point relative to this GTU, relative to the center line of the Lanes in
     * which the vehicle is registered, as fractions of the length of the lane. This is important when we want to see if
     * two vehicles are next to each other and we compare an 'inner' and 'outer' curve.<br>
     * s(t) = s0 + v0 * (t - t0) + 0.5 . a . (t - t0)^2
     * @param relativePosition the position on the vehicle relative to the reference point.
     * @param when the future time for which to calculate the positions.
     * @return the lanes and the position on the lanes where the GTU will be registered at the time, for the given
     *         position of the GTU.
     * @throws NetworkException when the vehicle is not on one of the lanes on which it is registered.
     */
    Map<Lane, Double> fractionalPositions(RelativePosition relativePosition, DoubleScalar.Abs<TimeUnit> when)
            throws NetworkException;

    /**
     * Return the longitudinal position of a point relative to this GTU, relative to the center line of the Lane, as a
     * fraction of the length of the lane. This is important when we want to see if two vehicles are next to each other
     * and we compare an 'inner' and 'outer' curve.<br>
     * s(t) = s0 + v0 * (t - t0) + 0.5 . a . (t - t0)^2
     * @param lane the position on this lane will be returned.
     * @param relativePosition the position on the vehicle relative to the reference point.
     * @param when the future time for which to calculate the positions.
     * @return the fractional relative position on the lane at the given time.
     * @throws NetworkException when the vehicle is not on the given lane.
     */
    double fractionalPosition(Lane lane, RelativePosition relativePosition, DoubleScalar.Abs<TimeUnit> when)
            throws NetworkException;

    /**
     * Return the longitudinal position of a point relative to this GTU, relative to the center line of the Lane, as a
     * fraction of the length of the lane. This is important when we want to see if two vehicles are next to each other
     * and we compare an 'inner' and 'outer' curve.<br>
     * @param lane the position on this lane will be returned.
     * @param relativePosition the position on the vehicle relative to the reference point.
     * @return the fractional relative position on the lane at the given time.
     * @throws NetworkException when the vehicle is not on the given lane.
     * @throws RemoteException when simulator time cannot be retrieved.
     */
    double fractionalPosition(Lane lane, RelativePosition relativePosition) throws NetworkException, RemoteException;

    /**
     * Determine by what distance the front of this GTU is behind the rear an other GTU, or the rear of this GTU is
     * ahead of the front of another GTU. Only positive values are returned. This method looks in all lanes where the
     * GTU is registered, and not further than the given maxDistance. When no vehicle is found within the given
     * maxDistance, a very large value is returned. The search will extend into successive lanes if the maxDistance is
     * larger than the remaining length in the lane. When Lanes (or underlying CrossSectionLinks) diverge, the headway
     * algorithms have to look at multiple Lanes and return the minimum headway in each of the Lanes. When the Lanes (or
     * underlying CrossSectionLinks) converge, "parallel" traffic is not taken into account in the headway calculation.
     * Instead, gap acceptance algorithms or their equivalent should guide the merging behavior.<br>
     * <b>Note:</b> Headway is the net headway and calculated on a front-to-back basis.
     * @param maxDistance the maximum distance to look for the nearest GTU; positive values search forwards; negative
     *            values search backwards
     * @return DoubleScalarRel&lt;LengthUnit&gt;; the headway
     * @throws RemoteException when simulator time cannot be retrieved.
     * @throws NetworkException when there is an error with the next lanes in the network.
     */
    DoubleScalar.Rel<LengthUnit> headway(DoubleScalar.Rel<LengthUnit> maxDistance) throws RemoteException,
            NetworkException;

    /**
     * Determine which GTU in front of this GTU, or behind this GTU. This method looks in all lanes where this GTU is
     * registered, and not further than the absolute value of the given maxDistance. The minimum headway is returned of
     * all Lanes where the GTU is registered. When no GTU is found within the given maxDistance, <b>null</b> is
     * returned. The search will extend into successive lanes if the maxDistance is larger than the remaining length on
     * the lane. When Lanes (or underlying CrossSectionLinks) diverge, the headway algorithms have to look at multiple
     * Lanes and return the minimum headway in each of the Lanes. When the Lanes (or underlying CrossSectionLinks)
     * converge, "parallel" traffic is not taken into account in the headway calculation. Instead, gap acceptance
     * algorithms or their equivalent should guide the merging behavior.<br>
     * <b>Note:</b> Headway is the net headway and calculated on a front-to-back basis.
     * @param maxDistance the maximum distance to look for the nearest GTU; positive values search forwards; negative
     *            values search backwards
     * @return DoubleScalarRel&lt;LengthUnit&gt;; the headway
     * @throws RemoteException when simulator time cannot be retrieved.
     * @throws NetworkException when there is an error with the next lanes in the network.
     */
    LaneBasedGTU<?> headwayGTU(DoubleScalar.Rel<LengthUnit> maxDistance) throws RemoteException, NetworkException;

    /**
     * Determine by what distance the front of this GTU is ahead or behind the front an other GTU. Positive values
     * indicate that this GTU is ahead, negative values indicate behind. This method works for GTUs in different lanes.
     * The search will extend into successive lanes if the maxDistance is larger than the length of the lane. If the
     * other GTU is in the same lane, a lane based distance will be returned. If the other GTU is in another lane, a
     * distance based on a projection on the design line will be returned. <br>
     * <b>Note 1:</b> Headway is the net headway and calculated on a front-to-back basis. <br>
     * <b>Note 2:</b> When the other vehicle's front is behind our front, a negative distance is returned. <br>
     * <b>Note 3:</b> The typical use of the method is <code>follower.headway(leader, maxX)</code>.<br>
     * <b>Note 4:</b> <code>headway</code> searches for maxDistance in front of the car when maxDistance is positive,
     * and behind the car when headway is negative.<br>
     * <b>Note 5:</b> When we cannot find the other GTU,
     * <code>new DoubleScalar.Rel<LengthUnit>(Double.MAX_VALUE, LengthUnit.METER)</code> is returned.<br>
     * @param otherGTU the GTU to which the headway must be returned
     * @return DoubleScalarRel&lt;LengthUnit&gt;; the headway
     * @param maxDistance the maximum distance to look for
     * @throws RemoteException when simulator time cannot be retrieved.
     * @throws NetworkException when the vehicle's route is inconclusive or vehicles are not registered correctly on
     *             their lanes.
     */
    DoubleScalar.Rel<LengthUnit> headway(LaneBasedGTU<?> otherGTU, DoubleScalar.Rel<LengthUnit> maxDistance)
            throws RemoteException, NetworkException;

    /**
     * Determine by what distance the front of this GTU is ahead or behind the front an other GTU. Positive values
     * indicate that this GTU is ahead, negative values indicate behind. This method works for GTUs in different lanes.
     * The search will extend into successive lanes if the maxDistance is larger than the length of the lane. If the
     * other GTU is in the same lane, a lane based distance will be returned. If the other GTU is in another lane, a
     * distance based on a projection on the design line will be returned. <br>
     * <b>Note 1:</b> Headway is the net headway and calculated on a front-to-back basis.<br>
     * <b>Note 2:</b> When the other vehicle's front is behind our front, a negative distance is returned. <br>
     * <b>Note 3:</b> The typical use of the method is <code>follower.headway(leader, maxX, time)</code>.<br>
     * <b>Note 4:</b> <code>headway</code> searches for maxDistance in front of the car when maxDistance is positive,
     * and behind the car when headway is negative.<br>
     * <b>Note 5:</b> When we cannot find the other GTU,
     * <code>new DoubleScalar.Rel<LengthUnit>(Double.MAX_VALUE, LengthUnit.METER)</code> is returned.
     * @param otherGTU the GTU to which the headway must be returned
     * @param maxDistance the maximum distance to look for
     * @param when the future time for which to calculate the headway
     * @return DoubleScalarRel&lt;LengthUnit&gt;; the headway
     * @throws RemoteException when simulator time cannot be retrieved.
     * @throws NetworkException when the vehicle's route is inconclusive or vehicles are not registered correctly on
     *             their lanes.
     */
    DoubleScalar.Rel<LengthUnit> headway(LaneBasedGTU<?> otherGTU, DoubleScalar.Rel<LengthUnit> maxDistance,
            DoubleScalar.Abs<TimeUnit> when) throws RemoteException, NetworkException;

    /**
     * Determine by what distance the front of this GTU is behind the rear an other GTU, or the rear of this GTU is
     * ahead of the front of an other GTU. Only positive values are returned. This method only looks in the given lane,
     * and not further than the given maxDistance. When no vehicle is found within the given maxDistance,
     * <code>new DoubleScalar.Rel<LengthUnit>(Double.MAX_VALUE, LengthUnit.METER)</code> is returned. The search will
     * extend into successive lanes if the maxDistance is larger than the length of the lane.<br>
     * <b>Note:</b> Headway is the net headway and calculated on a front-to-back basis.
     * @param lane the lane to look for another GTU
     * @param maxDistance the maximum distance to look for; if positive, the search is forwards; if negative, the search
     *            is backwards
     * @return DoubleScalarRel&lt;LengthUnit&gt;; the headway
     * @throws RemoteException when simulator time cannot be retrieved
     * @throws NetworkException when the vehicle's route is inconclusive or vehicles are not registered correctly on
     *             their lanes
     */
    DoubleScalar.Rel<LengthUnit> headway(Lane lane, DoubleScalar.Rel<LengthUnit> maxDistance) throws RemoteException,
            NetworkException;

    /**
     * Determine which GTU in front of us. Only positive values are returned. This method only looks in the given lane,
     * and not further than the given maxDistance. When no GTU is found within the given maxDistance, <b>null</b> is
     * returned. The search will extend into successive lanes if the maxDistance is larger than the length of the lane.<br>
     * <b>Note:</b> Headway is the net headway and calculated on a front-to-back basis.
     * @param lane the lane to look for another GTU.
     * @param maxDistance the maximum distance to look for
     * @return DoubleScalarRel&lt;LengthUnit&gt;; the headway
     * @throws RemoteException when simulator time cannot be retrieved.
     * @throws NetworkException when the vehicle's route is inconclusive or vehicles are not registered correctly on
     *             their lanes.
     */
    LaneBasedGTU<?> headwayGTU(Lane lane, DoubleScalar.Rel<LengthUnit> maxDistance) throws RemoteException,
            NetworkException;

    /**
     * Determine which GTUs are parallel with us on another lane, based on fractional positions. <br>
     * Note: When the GTU that calls the method is also registered on the given lane, it is excluded from the return
     * set.
     * @param lane the lane to look for parallel (partial or full overlapping) GTUs.
     * @param when the future time for which to calculate the headway
     * @return the set of GTUs parallel to us on the other lane (partial overlap counts as parallel), based on
     *         fractional positions, or an empty set when no GTUs were found.
     * @throws RemoteException when simulator time cannot be retrieved.
     * @throws NetworkException when the vehicle's route is inconclusive, when vehicles are not registered correctly on
     *             their lanes, or when the given lane is not parallel to one of the lanes where we are registered.
     */
    Set<LaneBasedGTU<?>> parallel(Lane lane, DoubleScalar.Abs<TimeUnit> when) throws RemoteException, NetworkException;

    /**
     * Determine which GTUs are parallel with us in a certain lateral direction, based on fractional positions. <br>
     * Note 1: This method will look to the adjacent lanes of all lanes where the vehicle has been registered.<br>
     * Note 2: When the GTU that calls the method is also registered on the given lane, it is excluded from the return
     * set.
     * @param lateralDirection the direction of the adjacent lane(s) to look for parallel (partial or full overlapping)
     *            GTUs.
     * @param when the future time for which to calculate the headway
     * @return the set of GTUs parallel to us on other lane(s) in the given direction (partial overlap counts as
     *         parallel), based on fractional positions, or an empty set when no GTUs were found.
     * @throws RemoteException when simulator time cannot be retrieved.
     * @throws NetworkException when the vehicle's route is inconclusive, when vehicles are not registered correctly on
     *             their lanes, or when there are no lanes parallel to one of the lanes where we are registered in the
     *             given direction.
     */
    Set<LaneBasedGTU<?>> parallel(LateralDirectionality lateralDirection, DoubleScalar.Abs<TimeUnit> when)
            throws RemoteException, NetworkException;

    /**
     * Determine the time when this GTU will have covered the specified distance from the position of the last
     * evaluation time.
     * @param distance DoubleScalar.Rel&lt;LengthUnit&gt;; the distance
     * @return DoubleScalar.Rel&lt;TimeUnit&gt;; the time, or null if this GTU stops before covering the specified
     *         distance
     */
    DoubleScalar.Abs<TimeUnit> timeAtDistance(DoubleScalar.Rel<LengthUnit> distance);

    /**
     * Determine the time since last evaluation when this GTU has covered the specified distance from the position of
     * the last evaluation time.
     * @param distance DoubleScalar.Rel&lt;LengthUnit&gt;; the distance
     * @return DoubleScalar.Rel&lt;TimeUnit&gt;; the time difference from last evaluation or null if this GTU stops
     *         before covering the specified distance
     */
    DoubleScalar.Rel<TimeUnit> deltaTimeForDistance(DoubleScalar.Rel<LengthUnit> distance);

    /**
     * Retrieve the GTU following model of this LaneBasedGTU.
     * @return GTUFollowingModel
     */
    GTUFollowingModel getGTUFollowingModel();

}
