package org.opentrafficsim.core.gtu.following;


/**
 * Fixed GTU following model. This GTU following model does not react in any way to other GTUs. Instead it has a predetermined
 * acceleration for a predetermined duration.<br>
 * Primary use is testing of lane based GTU movement.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$,
 *          initial version 6 feb. 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class FixedAccelerationModel extends AbstractGTUFollowingModel
{
    /** Acceleration that will be returned in GTUFollowingModelResult by computeAcceleration. */
    private Acceleration.Abs acceleration;

    /** Valid until time that will be returned in GTUFollowingModelResult by computeAcceleration. */
    private Time.Rel duration;

    /**
     * Create a new FixedAccelerationModel.
     * @param acceleration DoubleScalar.Abs&lt;AccelerationUnit&gt;; the acceleration that will be returned by the
     *            computeAcceleration methods
     * @param duration DoubleScalar.Abs&lt;TimeUnit&gt;; the duration that the acceleration will be maintained
     */
    public FixedAccelerationModel(final Acceleration.Abs acceleration, final Time.Rel duration)
    {
        this.acceleration = acceleration;
        this.duration = duration;
    }

    /**
     * Retrieve the duration of this FixedAccelerationModel.
     * @return DoubleScalar.Rel&lt;TimeUnit&gt;; the duration of this FixedAccelerationModel
     */
    public final Time.Rel getDuration()
    {
        return this.duration;
    }

    /**
     * Retrieve the acceleration of this FixedAccelerationModel.
     * @return DoubleScalar.Abs&lt;AccelerationUnit&gt;; the acceleration of this FixedAccelerationModel
     */
    public final Acceleration.Abs getAcceleration()
    {
        return this.acceleration;
    }

    /** {@inheritDoc} */
    @Override
    public final Acceleration.Abs computeAcceleration(final Speed.Abs followerSpeed, final Speed.Abs followerMaximumSpeed,
        final Speed.Abs leaderSpeed, final Length.Rel headway, final Speed.Abs speedLimit)
    {
        return this.acceleration;
    }

    /** {@inheritDoc} */
    @Override
    public final Acceleration.Abs maximumSafeDeceleration()
    {
        return new Acceleration.Abs(2, METER_PER_SECOND_2);
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

}
