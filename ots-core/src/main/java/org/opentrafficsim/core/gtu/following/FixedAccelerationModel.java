package org.opentrafficsim.core.gtu.following;

import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel;

/**
 * Fixed GTU following model. This GTU following model does not react in any way to other GTUs. In stead it has a
 * predetermined acceleration for a predetermined duration.<br/>
 * Primary use is testing of lane based GTU movement.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 6 feb. 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */
public class FixedAccelerationModel extends AbstractGTUFollowingModel
{
    /** Acceleration that will be returned in GTUFollowingModelResult by computeAcceleration. */
    private DoubleScalar.Abs<AccelerationUnit> acceleration;

    /** Valid until time that will be returned in GTUFollowingModelResult by computeAcceleration. */
    private DoubleScalar.Rel<TimeUnit> duration;

    /**
     * Create a new FixedAccelerationModel.
     * @param acceleration DoubleScalar.Abs&ltAccelerationUnit&gt;; the acceleration that will be returned by the
     *            computeAcceleration methods
     * @param duration DoubleScalar.Abs&lt;TimeUnit&gt;; the duration that the acceleration will be maintained
     */
    public FixedAccelerationModel(final DoubleScalar.Abs<AccelerationUnit> acceleration,
            final DoubleScalar.Rel<TimeUnit> duration)
    {
        this.acceleration = acceleration;
        this.duration = duration;
    }

    /**
     * Retrieve the duration of this FixedAccelerationModel.
     * @return DoubleScalar.Rel&lt;TimeUnit&gt;; the duration of this FixedAccelerationModel
     */
    public final DoubleScalar.Rel<TimeUnit> getDuration()
    {
        return this.duration;
    }

    /**
     * Retrieve the acceleration of this FixedAccelerationModel.
     * @return DoubleScalar.Abs&lt;AccelerationUnit&gt;; the acceleration of this FixedAccelerationModel
     */
    public final DoubleScalar.Abs<AccelerationUnit> getAcceleration()
    {
        return this.acceleration;
    }

    /** {@inheritDoc} */
    @Override
    public Abs<AccelerationUnit> computeAcceleration(Abs<SpeedUnit> followerSpeed, Abs<SpeedUnit> followerMaximumSpeed,
            Abs<SpeedUnit> leaderSpeed, Rel<LengthUnit> headway, Abs<SpeedUnit> speedLimit)
    {
        return this.acceleration;
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Abs<AccelerationUnit> maximumSafeDeceleration()
    {
        return new DoubleScalar.Abs<AccelerationUnit>(2, AccelerationUnit.METER_PER_SECOND_2);
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Rel<TimeUnit> getStepSize()
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
