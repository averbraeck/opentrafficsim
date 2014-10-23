package org.opentrafficsim.core.gtu.following;

import java.rmi.RemoteException;
import java.util.Collection;

import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * Abstract GTU following model.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Jul 2, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public interface GTUFollowingModel
{
    /**
     * Compute the acceleration that would be used to follow a set of leaders.
     * @param gtu the GTU for which acceleration is computed
     * @param leaders Set&lt;GTU&gt;; the set of leaders to take into consideration
     * @param speedLimit DoubleScalarAbs&lt;SpeedUnit&gt;; the local speed limit
     * @return GTUFollowingModelResult; the result of application of the gtu following model
     */
    GTUFollowingModelResult computeAcceleration(final GTU<?> gtu, final Collection<GTU<?>> leaders,
            final DoubleScalar.Abs<SpeedUnit> speedLimit);

    /**
     * Compute the acceleration and lane change.
     * @param gtu GTU; the GTU for which the acceleration and lane change is computed
     * @param sameLaneGTUs Collection&lt;GTU&gt;; the set of observable GTUs in the current lane (can not be null)
     * @param preferredLaneGTUs Collection&lt;GTU&gt;; the set of observable GTUs in the adjacent lane where gtus should drive
     *            in the absence of other traffic (must be null if there is no such lane)
     * @param nonPreferredLaneGTUs Collection&lt;GTU&gt;; the set of observable GTUs in the adjacent lane into which gtus should
     *            merge to overtake other traffic (must be null if there is no such lane)
     * @param speedLimit DoubleScalarAbs&lt;SpeedUnit&gt;; the local speed limit
     * @param preferredLaneRouteIncentive Double; route incentive to merge to the adjacent lane where gtus should drive in the
     *            absence of other traffic
     * @param nonPreferredLaneRouteIncentive Double; route incentive to merge to the adjacent lane into which gtus should merge
     *            to overtake other traffic
     * @return GTUFollowingModelResult; the result of the lane change and gtu following model
     * @throws RemoteException
     */
    GTUFollowingModelResult computeLaneChangeAndAcceleration(final GTU<?> gtu, final Collection<GTU<?>> sameLaneGTUs,
            final Collection<GTU<?>> preferredLaneGTUs, final Collection<GTU<?>> nonPreferredLaneGTUs,
            final DoubleScalar.Abs<SpeedUnit> speedLimit, double preferredLaneRouteIncentive,
            double nonPreferredLaneRouteIncentive) throws RemoteException;

    /**
     * The result of a GTUFollowingModel evaluation shall be stored in an instance of this class.
     * <p>
     * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. 
     * All rights reserved. <br>
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
         * Lane change; 0: stay in current lane; -1 merge onto adjacent overtaking lane; +1 merge towards the default lane.
         */
        private final int laneChange;

        /**
         * Create a new GTUFollowingModelResult.
         * @param acceleration DoubleScalarAbs&lt;AccelerationUnit&gt;; computed acceleration
         * @param validUntil DoubleScalarAbs&lt;TimeUnit&gt;; time when this result expires
         * @param laneChange Integer; the lane determined change; 0: stay in current lane; -1 merge onto adjacent overtaking
         *            lane; +1 merge towards the default lane
         */
        public GTUFollowingModelResult(final DoubleScalar.Abs<AccelerationUnit> acceleration,
                final DoubleScalar.Abs<TimeUnit> validUntil, final int laneChange)
        {
            this.acceleration = acceleration;
            this.validUntil = validUntil;
            this.laneChange = laneChange;
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
         * @return laneChange.
         */
        public int getLaneChange()
        {
            return this.laneChange;
        }
    }
}
