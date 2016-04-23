package org.opentrafficsim.road.gtu.lane.tactical.following;

import java.util.SortedMap;

import org.djunits.unit.AccelerationUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;

/**
 * Default implementation where desired speed and headway are pre-calculated for car-following.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 2016 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public abstract class AbstractCarFollowingModel implements CarFollowingModel
{

    /**
     * Forwards the calculation to a similar method with desired speed and desired (equilibrium) headway pre-calculated.
     * Additionally, if the headway to the (first) leader is negative, <tt>Double.NEGATIVE_INFINITY</tt> is returned as an
     * 'inappropriate' acceleration, since car-following models are then undefined. This may for example occur when checking a
     * gap in an adjacent lane for lane changing. It is then up to the client to decide what to do. E.g. limit deceleration to
     * an extent depending on the circumstances, or divert from a certain behavior.
     * @param behavioralCharacteristics Behavioral characteristics.
     * @param speed Current speed.
     * @param speedInfo Info regarding the desired speed for car-following.
     * @param leaders Set of leader headways and speeds, ordered by headway (closest first).
     * @throws ParameterException If parameter exception occurs.
     * @return Car-following acceleration.
     */
    @Override
    public final Acceleration followingAcceleration(final BehavioralCharacteristics behavioralCharacteristics,
        final Speed speed, final SpeedInfo speedInfo, final SortedMap<Length, Speed> leaders) throws ParameterException
    {
        // Catch negative headway
        if (leaders.firstKey().si <= 0)
        {
            return new Acceleration(Double.NEGATIVE_INFINITY, AccelerationUnit.SI);
        }
        // Forward to method with desired speed and headway predetermined by this car-following model.
        return followingAcceleration(behavioralCharacteristics, speed, desiredSpeed(behavioralCharacteristics, speedInfo),
            desiredHeadway(behavioralCharacteristics, speed), leaders);
    }

    /**
     * Determination of car-following acceleration, possibly based on multiple leaders.
     * @param behavioralCharacteristics Behavioral characteristics.
     * @param speed Current speed.
     * @param desiredSpeed Desired speed.
     * @param desiredHeadway Desired headway.
     * @param leaders Set of leader headways (guaranteed positive) and speeds, ordered by headway (closest first).
     * @throws ParameterException If parameter exception occurs.
     * @return Car-following acceleration.
     */
    protected abstract Acceleration followingAcceleration(BehavioralCharacteristics behavioralCharacteristics, Speed speed,
        Speed desiredSpeed, Length desiredHeadway, SortedMap<Length, Speed> leaders) throws ParameterException;

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public String toString()
    {
        return getLongName();
    }

}
