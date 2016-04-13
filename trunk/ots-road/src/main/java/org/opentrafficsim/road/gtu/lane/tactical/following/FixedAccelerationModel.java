package org.opentrafficsim.road.gtu.lane.tactical.following;

import java.util.SortedMap;

import org.djunits.unit.AccelerationUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Length.Rel;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;

/**
 * Fixed GTU following model. This GTU following model does not react in any way to other GTUs. Instead it has a predetermined
 * acceleration for a predetermined duration.<br>
 * Primary use is testing of lane based GTU movement.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision: 1378 $, $LastChangedDate: 2015-09-03 13:38:01 +0200 (Thu, 03 Sep 2015) $, by $Author: averbraeck $,
 *          initial version 6 feb. 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class FixedAccelerationModel extends AbstractGTUFollowingModelMobil
{
    /** Acceleration that will be returned in GTUFollowingModelResult by computeAcceleration. */
    private Acceleration acceleration;

    /** Valid until time that will be returned in GTUFollowingModelResult by computeAcceleration. */
    private Time.Rel duration;

    /**
     * Create a new FixedAccelerationModel.
     * @param acceleration Acceleration; the acceleration that will be returned by the computeAcceleration methods
     * @param duration Time.Rel; the duration that the acceleration will be maintained
     */
    public FixedAccelerationModel(final Acceleration acceleration, final Time.Rel duration)
    {
        this.acceleration = acceleration;
        this.duration = duration;
    }

    /**
     * Retrieve the duration of this FixedAccelerationModel.
     * @return Time.Rel; the duration of this FixedAccelerationModel
     */
    public final Time.Rel getDuration()
    {
        return this.duration;
    }

    /**
     * Retrieve the acceleration of this FixedAccelerationModel.
     * @return Acceleration; the acceleration of this FixedAccelerationModel
     */
    public final Acceleration getAcceleration()
    {
        return this.acceleration;
    }

    /** {@inheritDoc} */
    @Override
    public final Acceleration computeAcceleration(final Speed followerSpeed, final Speed followerMaximumSpeed,
        final Speed leaderSpeed, final Length.Rel headway, final Speed speedLimit, final Time.Rel stepSize)
    {
        return this.acceleration;
    }

    /** {@inheritDoc} */
    @Override
    public final Acceleration computeAcceleration(final Speed followerSpeed, final Speed followerMaximumSpeed,
        final Speed leaderSpeed, final Length.Rel headway, final Speed speedLimit)
    {
        return this.acceleration;
    }

    /** {@inheritDoc} */
    @Override
    public final Acceleration getMaximumSafeDeceleration()
    {
        // TODO should be specified in constructor
        return new Acceleration(2, AccelerationUnit.METER_PER_SECOND_2);
    }

    /** {@inheritDoc} */
    @Override
    public final Time.Rel getStepSize()
    {
        return this.duration;
    }

    /** {@inheritDoc} */
    @Override
    public final String getName()
    {
        return "Fixed";
    }

    /** {@inheritDoc} */
    @Override
    public final String getLongName()
    {
        return "Fixed GTU following model";
    }

    /** {@inheritDoc} */
    public final String toString()
    {
        return "FixedAccelerationModel " + this.duration + ", " + this.acceleration;
    }
    
    // The following is inherited from CarFollowingModel
    
    /** {@inheritDoc} */
    @Override
    public final Speed desiredSpeed(final BehavioralCharacteristics behavioralCharacteristics, final Speed speedLimit, 
        final boolean enforcement, final Speed maximumVehicleSpeed)
        throws ParameterException
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final Rel desiredHeadway(final BehavioralCharacteristics behavioralCharacteristics, final Speed speed) 
            throws ParameterException
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final Acceleration freeAcceleration(final BehavioralCharacteristics behavioralCharacteristics, final Speed speed, 
        final Speed speedLimit, final boolean enforcement, final Speed maximumVehicleSpeed) throws ParameterException
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final Acceleration followingAcceleration(final BehavioralCharacteristics behavioralCharacteristics, 
        final Speed speed, final Speed speedLimit, final boolean enforcement, final Speed maximumVehicleSpeed, 
        final Rel headway, final Speed leaderSpeed) throws ParameterException
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final Acceleration followingAcceleration(final BehavioralCharacteristics behavioralCharacteristics, 
        final Speed speed, final Speed speedLimit, final boolean enforcement, final Speed maximumVehicleSpeed, 
        final SortedMap<Rel, Speed> leaders) throws ParameterException
    {
        return null;
    }

}
