package org.opentrafficsim.core.gtu;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.Set;

import org.opentrafficsim.core.network.Lane;
import org.opentrafficsim.core.network.LaneLocation;
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
     * @param delta the reference point relative to the vehicle's position for which we want to know the location on all lanes
     *            where the vehicle is registered.
     * @return the location of the GTU, relative to one or more Lanes
     */
    Set<LaneLocation> getCurrentLocation(GTUReferencePoint delta);

    /**
     * @param delta the preference point relative to the vehicle's position.
     * @param location the location on a lane to which we want to calculate the relative distance.
     * @return the relative distance of a point on this vehicle to another point in a lane.
     */
    DoubleScalar.Rel<LengthUnit> longitudinalDistance(GTUReferencePoint delta, LaneLocation location);

    /**
     * @return the velocity of the GTU, in the direction of the lane
     * @throws RemoteException in case of problems getting the simulation time.
     */
    DoubleScalar.Abs<SpeedUnit> getCurrentLongitudinalVelocity() throws RemoteException;

    /** @return the velocity of the GTU, perpendicular to the direction of the lane */
    DoubleScalar.Abs<SpeedUnit> getCurrentLateralVelocity();

    /** @return DoubleScalarAbs&lt;TimeUnit&gt;; the time of last evaluation. */
    DoubleScalar.Abs<TimeUnit> getLastEvaluationTime();

    /** @return DoubleScalarAbs&lt;TimeUnit&gt;; the time of next evaluation. */
    DoubleScalar.Abs<TimeUnit> getNextEvaluationTime();

    /** @return the lanes and the position on the lanes where the GTU is currently registered. */
    Map<Lane, DoubleScalar.Abs<LengthUnit>> getLongitudinalPositions();

    /**
     * Return the position of the front bumper of this Car.
     * @param lane the position on this lane will be returned.
     * @param when time for which the position must be returned.
     * @return DoubleScalarAbs&lt;LengthUnit&gt;; the position at the specified time
     * @exception NetworkException when the vehicle is not on the given lane.
     */
    DoubleScalar.Abs<LengthUnit> positionOfFront(final Lane lane, final DoubleScalar.Abs<TimeUnit> when)
            throws NetworkException;

    /**
     * Return the position of the rear bumper of this Car.
     * @param lane the position on this lane will be returned.
     * @param when time for which the position must be returned.
     * @return DoubleScalarAbs&lt;LengthUnit&gt;; the position at the specified time
     * @exception NetworkException when the vehicle is not on the given lane.
     */
    DoubleScalar.Abs<LengthUnit> positionOfRear(final Lane lane, final DoubleScalar.Abs<TimeUnit> when) throws NetworkException;

    /**
     * Determine by what distance the front of this GTU is ahead or behind the front an other GTU. Positive values indicate that
     * this GTU is ahead, negative values indicate behind. This method works for GTUs in different lanes.
     * @param otherGTU the GTU to which the headway must be returned
     * @return DoubleScalarRel&lt;LengthUnit&gt;; the headway
     * @throws RemoteException when simulator time cannot be retrieved.
     */
    DoubleScalar.Rel<LengthUnit> headway(final LaneBasedGTU<?> otherGTU) throws RemoteException;

}
