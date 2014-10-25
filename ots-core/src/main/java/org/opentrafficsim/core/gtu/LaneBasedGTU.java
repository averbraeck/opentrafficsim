package org.opentrafficsim.core.gtu;

import java.rmi.RemoteException;
import java.util.Map;

import org.opentrafficsim.core.network.Lane;
import org.opentrafficsim.core.network.LinkLocation;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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

    /** @return the velocity of the GTU, perpendicular to the direction of the lane */
    DoubleScalar.Abs<SpeedUnit> getCurrentLateralVelocity();

    /** @return DoubleScalarAbs&lt;TimeUnit&gt;; the time of last evaluation. */
    DoubleScalar.Abs<TimeUnit> getLastEvaluationTime();

    /** @return DoubleScalarAbs&lt;TimeUnit&gt;; the time of next evaluation. */
    DoubleScalar.Abs<TimeUnit> getNextEvaluationTime();

    /**
     * @return the lanes and the position on the lanes where the GTU is currently registered, for the center position of the
     *         GTU.
     */
    Map<Lane, DoubleScalar.Abs<LengthUnit>> getLongitudinalPositions();

    /**
     * Return the position of the front bumper of this GTU, relative to the center line of the Lane.
     * @param lane the position on this lane will be returned.
     * @return DoubleScalarAbs&lt;LengthUnit&gt;; the position, relative to the center line of the Lane.
     * @exception NetworkException when the vehicle is not on the given lane.
     */
    DoubleScalar.Abs<LengthUnit> positionOfFront(Lane lane) throws NetworkException;

    /**
     * Return the position of the front bumper of this GTU, relative to the center line of the Lane. <br>
     * s(t) = s0 + v0 * (t - t0) + 0.5 . a . (t - t0)^2
     * @param lane the position on this lane will be returned.
     * @param when the future time for which to calculate the headway
     * @return DoubleScalarAbs&lt;LengthUnit&gt;; the position, relative to the center line of the Lane.
     * @exception NetworkException when the vehicle is not on the given lane.
     */
    DoubleScalar.Abs<LengthUnit> positionOfFront(Lane lane, DoubleScalar.Abs<TimeUnit> when) throws NetworkException;

    /**
     * Return the position of the front bumper of this GTU.
     * @return The position based on a projection on the design line.
     * @throws RemoteException when simulator time cannot be retrieved.
     */
    LinkLocation positionOfFront() throws RemoteException;

    /**
     * Return the position of the front bumper of this GTU. <br>
     * s(t) = s0 + v0 * (t - t0) + 0.5 . a . (t - t0)^2
     * @param when the future time for which to calculate the headway
     * @return The position based on a projection on the design line.
     * @throws RemoteException when simulator time cannot be retrieved.
     */
    LinkLocation positionOfFront(DoubleScalar.Abs<TimeUnit> when) throws RemoteException;

    /**
     * Return the position of the rear bumper of this GTU.
     * @param lane the position on this lane will be returned.
     * @return DoubleScalarAbs&lt;LengthUnit&gt;; the position at the specified time
     * @exception NetworkException when the vehicle is not on the given lane.
     */
    DoubleScalar.Abs<LengthUnit> positionOfRear(Lane lane) throws NetworkException;

    /**
     * Return the position of the rear bumper of this GTU.<br>
     * s(t) = s0 + v0 * (t - t0) + 0.5 . a . (t - t0)^2 - gtu.length
     * @param lane the position on this lane will be returned.
     * @param when the future time for which to calculate the headway
     * @return DoubleScalarAbs&lt;LengthUnit&gt;; the position at the specified time
     * @exception NetworkException when the vehicle is not on the given lane.
     */
    DoubleScalar.Abs<LengthUnit> positionOfRear(Lane lane, DoubleScalar.Abs<TimeUnit> when) throws NetworkException;

    /**
     * Return the position of the rear bumper of this GTU.
     * @return The position based on a projection on the design line.
     * @throws RemoteException when simulator time cannot be retrieved.
     */
    LinkLocation positionOfRear() throws RemoteException;

    /**
     * Return the position of the rear bumper of this GTU.<br>
     * s(t) = s0 + v0 * (t - t0) + 0.5 . a . (t - t0)^2 - gtu.length
     * @param when the future time for which to calculate the headway
     * @return The position based on a projection on the design line.
     * @throws RemoteException when simulator time cannot be retrieved.
     */
    LinkLocation positionOfRear(DoubleScalar.Abs<TimeUnit> when) throws RemoteException;

    /**
     * Determine by what distance the front of this GTU is behind the front an other GTU. Only positive values are returned.
     * This method only looks in the current lane, and not further than the given maxDistance. When no vehicle is found within
     * the given maxDistance, maxDistance is returned. The search will extend into successive lanes if the maxDistance is larger
     * than the length of the lane.<br>
     * <b>Note:</b> Headway is calculated on a front-to-front basis.
     * @param maxDistance the maximum distance to look for
     * @return DoubleScalarRel&lt;LengthUnit&gt;; the headway
     * @throws RemoteException when simulator time cannot be retrieved.
     */
    DoubleScalar.Rel<LengthUnit> headwayInCurrentLane(DoubleScalar<LengthUnit> maxDistance) throws RemoteException;

    /**
     * Determine which GTU in front of us. Only positive values are returned. This method only looks in the current lane, and
     * not further than the given maxDistance. When no GTU is found within the given maxDistance, <b>null</b> is returned. The
     * search will extend into successive lanes if the maxDistance is larger than the length of the lane.<br>
     * <b>Note:</b> Headway is calculated on a front-to-front basis.
     * @param maxDistance the maximum distance to look for
     * @return DoubleScalarRel&lt;LengthUnit&gt;; the headway
     * @throws RemoteException when simulator time cannot be retrieved.
     */
    LaneBasedGTU<?> headwayGTUInCurrentLane(DoubleScalar<LengthUnit> maxDistance) throws RemoteException;

    /**
     * Determine by what distance the front of this GTU is ahead or behind the front an other GTU. Positive values indicate that
     * this GTU is ahead, negative values indicate behind. This method works for GTUs in different lanes. The search will extend
     * into successive lanes if the maxDistance is larger than the length of the lane. If the other GTU is in the same lane, a
     * lane based distance will be returned. If the other GTU is in another lane, a distance based on a projection on the design
     * line will be returned. <br>
     * <b>Note:</b> Headway is calculated on a front-to-front basis.
     * @param otherGTU the GTU to which the headway must be returned
     * @return DoubleScalarRel&lt;LengthUnit&gt;; the headway
     * @throws RemoteException when simulator time cannot be retrieved.
     */
    DoubleScalar.Rel<LengthUnit> headway(LaneBasedGTU<?> otherGTU) throws RemoteException;

    /**
     * Determine by what distance the front of this GTU is ahead or behind the front an other GTU. Positive values indicate that
     * this GTU is ahead, negative values indicate behind. This method works for GTUs in different lanes. The search will extend
     * into successive lanes if the maxDistance is larger than the length of the lane. If the other GTU is in the same lane, a
     * lane based distance will be returned. If the other GTU is in another lane, a distance based on a projection on the design
     * line will be returned. <br>
     * <b>Note:</b> Headway is calculated on a front-to-front basis.
     * @param otherGTU the GTU to which the headway must be returned
     * @param when the future time for which to calculate the headway
     * @return DoubleScalarRel&lt;LengthUnit&gt;; the headway
     * @throws RemoteException when simulator time cannot be retrieved.
     */
    DoubleScalar.Rel<LengthUnit> headway(LaneBasedGTU<?> otherGTU, DoubleScalar.Abs<TimeUnit> when) throws RemoteException;

    /**
     * Determine by what distance the front of this GTU is behind the front an other GTU. Only positive values are returned.
     * This method only looks in the given lane, and not further than the given maxDistance. When no vehicle is found within the
     * given maxDistance, maxDistance is returned. The search will extend into successive lanes if the maxDistance is larger
     * than the length of the lane.<br>
     * <b>Note:</b> Headway is calculated on a front-to-front basis.
     * @param lane the lane to look for another GTU.
     * @param maxDistance the maximum distance to look for
     * @return DoubleScalarRel&lt;LengthUnit&gt;; the headway
     * @throws RemoteException when simulator time cannot be retrieved.
     */
    DoubleScalar.Rel<LengthUnit> headwayInLane(Lane lane, DoubleScalar<LengthUnit> maxDistance) throws RemoteException;

    /**
     * Determine which GTU in front of us. Only positive values are returned. This method only looks in the given lane, and not
     * further than the given maxDistance. When no GTU is found within the given maxDistance, <b>null</b> is returned. The
     * search will extend into successive lanes if the maxDistance is larger than the length of the lane.<br>
     * <b>Note:</b> Headway is calculated on a front-to-front basis.
     * @param lane the lane to look for another GTU.
     * @param maxDistance the maximum distance to look for
     * @return DoubleScalarRel&lt;LengthUnit&gt;; the headway
     * @throws RemoteException when simulator time cannot be retrieved.
     */
    LaneBasedGTU<?> headwayGTUInLane(Lane lane, DoubleScalar<LengthUnit> maxDistance) throws RemoteException;

}
