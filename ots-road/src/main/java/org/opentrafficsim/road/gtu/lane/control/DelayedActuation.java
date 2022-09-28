package org.opentrafficsim.road.gtu.lane.control;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.opentrafficsim.base.parameters.ParameterTypeDuration;
import org.opentrafficsim.base.parameters.constraint.NumericConstraint;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;

/**
 * Delays the actuation of acceleration. This is not part of the vehicle model as that is used for both human and automated
 * control, which follow different vehicle capability semantics.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public interface DelayedActuation
{

    /** No delayed actuation. */
    DelayedActuation NONE = new DelayedActuation()
    {
        /** {@inheritDoc} */
        @Override
        public Acceleration delayActuation(final Acceleration desiredAcceleration, final LaneBasedGTU gtu)
        {
            return desiredAcceleration;
        }
    };

    /** Parameter for actuation delay. */
    ParameterTypeDuration TAU = new ParameterTypeDuration("tau_actuation", "Actuation delay", Duration.instantiateSI(0.1),
            NumericConstraint.POSITIVE);

    /** Tau delayed actuation. */
    DelayedActuation TAUDELAYED = new DelayedActuation()
    {
        /** {@inheritDoc} */
        @Override
        public Acceleration delayActuation(final Acceleration desiredAcceleration, final LaneBasedGTU gtu)
        {
            // TODO: numerical implementation of tau rule
            return desiredAcceleration.minus(gtu.getAcceleration());
        }
    };

    /**
     * Delays the actuation of acceleration.
     * @param desiredAcceleration Acceleration; desired acceleration
     * @param gtu LaneBasedGtu; gtu
     * @return Acceleration; delayed acceleration
     */
    Acceleration delayActuation(Acceleration desiredAcceleration, LaneBasedGTU gtu);

}
