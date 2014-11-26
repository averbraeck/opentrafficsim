package org.opentrafficsim.core.gtu.following;

import java.rmi.RemoteException;
import java.util.Collection;

import org.opentrafficsim.core.gtu.LaneBasedGTU;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs;

/**
 * Abstract GTU following model.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Jul 2, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public interface GTUFollowingModel
{
    /**
     * Compute the acceleration that would be used to follow a set of leaders.<br />
     * TODO We should probably add a <i>be ready to stop before</i> argument to prevent vehicles that cannot see their
     * leader, or should slow down for a crossing from accelerating to unsafe speeds.
     * @param follower the GTU for which acceleration is computed
     * @param leaders Set&lt;LaneBasedGTU&gt;; the set of leaders to take into consideration
     * @param speedLimit DoubleScalarAbs&lt;SpeedUnit&gt;; the local speed limit
     * @return GTUFollowingModelResult; the result of application of the gtu following model
     * @throws RemoteException in case of simulator reachability problems
     */
    GTUFollowingModelResult computeAcceleration(final LaneBasedGTU<?> follower,
            final Collection<? extends LaneBasedGTU<?>> leaders, final DoubleScalar.Abs<SpeedUnit> speedLimit)
            throws RemoteException;

    /**
     * Compute the acceleration that would be used to follow a leader.<br />
     * TODO We should probably add a <i>be ready to stop before</i> argument to prevent vehicles that cannot see their
     * leader, or should slow down for a crossing from accelerating to unsafe speeds.
     * @param follower the GTU for which acceleration is computed
     * @param leader LaneBasedGTU&lt;?&gt;; the leader to take into consideration (may be null)
     * @param speedLimit DoubleScalarAbs&lt;SpeedUnit&gt;; the local speed limit
     * @return GTUFollowingModelResult; the result of application of the GTU following model
     * @throws RemoteException in case of simulator reachability problems
     */
    GTUFollowingModelResult computeAcceleration(final LaneBasedGTU<?> follower, final LaneBasedGTU<?> leader,
            final DoubleScalar.Abs<SpeedUnit> speedLimit) throws RemoteException;

    /**
     * Compute the acceleration that would be used to follow a leader.<br />
     * TODO We should probably add a <i>be ready to stop before</i> argument to prevent vehicles that cannot see their
     * leader, or should slow down for a crossing from accelerating to unsafe speeds.
     * @param follower the GTU for which acceleration is computed
     * @param leaderSpeed DoubleScalar.Abs&lt;SpeedUnit&gt;; the speed of the leader
     * @param headway DoubleScalar.Rel&lt;LengthUnit&gt;; the headway of the leader
     * @param speedLimit DoubleScalarAbs&lt;SpeedUnit&gt;; the local speed limit
     * @return GTUFollowingModelResult; the result of application of the GTU following model
     * @throws RemoteException in case of simulator reachability problems
     */
    GTUFollowingModelResult computeAcceleration(final LaneBasedGTU<?> follower,
            final DoubleScalar.Abs<SpeedUnit> leaderSpeed, final DoubleScalar.Rel<LengthUnit> headway,
            Abs<SpeedUnit> speedLimit) throws RemoteException;

    /**
     * Return the maximum safe deceleration for use in gap acceptance models. This is the deceleration that may be
     * enforced upon a new follower due to entering a road or changing into an adjacent lane. The result shall be a
     * <b>positive value</b>. In most car following models this value is named <cite>b</cite>.
     * @return DoubleScalar.Abs&lt;AccelerationUnit&gt;; must be a positive value!
     */
    DoubleScalar.Abs<AccelerationUnit> maximumSafeDeceleration();
    
    /**
     * Return the name of this GTU following model.
     * @return String; just the name of the GTU following model
     */
    String getName();
    
    /**
     * Return complete textual information about this instantiation of this GTU following model.
     * @return String; the name and parameter values of the GTU following model
     */
    String getLongName();

    /**
     * The result of a GTUFollowingModel evaluation shall be stored in an instance of this class.
     * <p>
     * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
     * reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version Jul 9, 2014 <br>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    class GTUFollowingModelResult
    {
        /** Acceleration that will be maintained during the current time slot. */
        private final DoubleScalar.Abs<AccelerationUnit> acceleration;

        /** Time when the current time slot ends. */
        private final DoubleScalar.Abs<TimeUnit> validUntil;

        /**
         * Create a new GTUFollowingModelResult.
         * @param acceleration DoubleScalarAbs&lt;AccelerationUnit&gt;; computed acceleration
         * @param validUntil DoubleScalarAbs&lt;TimeUnit&gt;; time when this result expires
         */
        public GTUFollowingModelResult(final DoubleScalar.Abs<AccelerationUnit> acceleration,
                final DoubleScalar.Abs<TimeUnit> validUntil)
        {
            this.acceleration = acceleration;
            this.validUntil = validUntil;
        }

        /**
         * @return acceleration.
         */
        public DoubleScalar.Abs<AccelerationUnit> getAcceleration()
        {
            return this.acceleration;
        }

        /**
         * @return validUntil.
         */
        public DoubleScalar.Abs<TimeUnit> getValidUntil()
        {
            return this.validUntil;
        }

        /**
         * {@inheritDoc}
         */
        public String toString()
        {
            return String.format("a=%s, valid until %s", this.acceleration, this.validUntil);
        }
    }
}
