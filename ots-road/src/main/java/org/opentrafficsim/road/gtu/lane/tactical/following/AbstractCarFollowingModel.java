package org.opentrafficsim.road.gtu.lane.tactical.following;

import java.util.SortedMap;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.Throw;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;
import org.opentrafficsim.road.network.speed.SpeedLimitTypeSpeedLegal;
import org.opentrafficsim.road.network.speed.SpeedLimitTypes;

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
     * @param behavioralCharacteristics behavioral characteristics
     * @param speed current speed
     * @param speedLimitInfo info regarding the desired speed for car-following
     * @param leaders set of leader headways and speeds, ordered by headway (closest first)
     * @return car-following acceleration
     * @throws ParameterException if parameter exception occurs
     * @throws NullPointerException if any input is null
     */
    @Override
    public final Acceleration followingAcceleration(final BehavioralCharacteristics behavioralCharacteristics,
        final Speed speed, final SpeedLimitInfo speedLimitInfo, final SortedMap<Length, Speed> leaders)
        throws ParameterException
    {
        Throw.whenNull(behavioralCharacteristics, "Behavioral characteristics may not be null.");
        Throw.whenNull(speed, "Speed may not be null.");
        Throw.whenNull(speedLimitInfo, "Speed limit info may not be null.");
        Throw.whenNull(leaders, "Leaders may not be null.");
        // Catch negative headway
        if (leaders.firstKey().si <= 0)
        {
            return new Acceleration(Double.NEGATIVE_INFINITY, AccelerationUnit.SI);
        }
        // Forward to method with desired speed and headway predetermined by this car-following model.
        return followingAcceleration(behavioralCharacteristics, speed, desiredSpeed(behavioralCharacteristics,
            speedLimitInfo), desiredHeadway(behavioralCharacteristics, speed), leaders);
    }

    /**
     * Determination of car-following acceleration, possibly based on multiple leaders.
     * @param behavioralCharacteristics behavioral characteristics
     * @param speed current speed
     * @param desiredSpeed desired speed
     * @param desiredHeadway desired headway
     * @param leaders set of leader headways (guaranteed positive) and speeds, ordered by headway (closest first)
     * @return car-following acceleration
     * @throws ParameterException if parameter exception occurs
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

    /**
     * Returns the minimum of speed of speed limit types MAX_LEGAL_VEHICLE_SPEED, ROAD_CLASS, FIXED_SIGN and DYNAMIC_SIGN. This
     * method may be overridden by subclasses to implement additional behavior regarding legal speed limits.
     * @param speedLimitInfo speed limit info
     * @return minimum of speed of speed limit types MAX_LEGAL_VEHICLE_SPEED, ROAD_CLASS, FIXED_SIGN and DYNAMIC_SIGN
     * @throws NullPointerException if speed limit info is null
     */
    @SuppressWarnings("checkstyle:designforextension")
    public Speed getLegalSpeedLimit(final SpeedLimitInfo speedLimitInfo)
    {
        Throw.whenNull(speedLimitInfo, "Speed limit info may not be null.");
        Speed result = new Speed(Double.POSITIVE_INFINITY, SpeedUnit.SI);
        for (SpeedLimitTypeSpeedLegal lsl : new SpeedLimitTypeSpeedLegal[] {SpeedLimitTypes.MAX_LEGAL_VEHICLE_SPEED,
            SpeedLimitTypes.ROAD_CLASS, SpeedLimitTypes.FIXED_SIGN, SpeedLimitTypes.DYNAMIC_SIGN})
        {
            Speed s = speedLimitInfo.getSpeedInfo(lsl);
            result = s.lt(result) ? s : result;
        }
        return result;
    }

    /**
     * Returns the speed of speed limit type MAX_VEHICLE_SPEED. This method may be overridden by subclasses to implement
     * additional behavior regarding maximum vehicle speed limits.
     * @param speedLimitInfo speed limit info
     * @return speed of speed limit type MAX_VEHICLE_SPEED
     * @throws NullPointerException if speed limit info is null
     */
    @SuppressWarnings("checkstyle:designforextension")
    public Speed getMaximumVehicleSpeed(final SpeedLimitInfo speedLimitInfo)
    {
        Throw.whenNull(speedLimitInfo, "Speed limit info may not be null.");
        return speedLimitInfo.getSpeedInfo(SpeedLimitTypes.MAX_VEHICLE_SPEED);
    }

}
