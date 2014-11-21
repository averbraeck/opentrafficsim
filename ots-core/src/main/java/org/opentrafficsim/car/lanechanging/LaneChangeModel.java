package org.opentrafficsim.car.lanechanging;

import java.rmi.RemoteException;
import java.util.Collection;

import org.opentrafficsim.core.gtu.AbstractLaneBasedGTU;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel.GTUFollowingModelResult;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel;

/**
 * All lane change models must implement this interface.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 3 nov. 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */
public interface LaneChangeModel
{
    /**
     * Compute the acceleration and lane change. <br />
     * FIXME the parameters of this method will change. Hopefully will become straightforward to figure out the nearby
     * vehicles in the current and the adjacent lanes.
     * @param gtu GTU; the GTU for which the acceleration and lane change is computed
     * @param sameLaneGTUs Collection&lt;GTU&gt;; the set of observable GTUs in the current lane (can not be null and
     *            may include gtu)
     * @param preferredLaneGTUs Collection&lt;GTU&gt;; the set of observable GTUs in the adjacent lane where GTUs should
     *            drive in the absence of other traffic (must be null if there is no such lane)
     * @param nonPreferredLaneGTUs Collection&lt;GTU&gt;; the set of observable GTUs in the adjacent lane into which
     *            GTUs should merge to overtake other traffic (must be null if there is no such lane)
     * @param speedLimit DoubleScalarAbs&lt;SpeedUnit&gt;; the local speed limit
     * @param preferredLaneRouteIncentive DoubleScalar.Rel&lt;AccelerationUnit&gt;; route incentive to merge to the
     *            adjacent lane where GTUs should drive in the absence of other traffic
     * @param laneChangeThreshold DoubleScalar.Rel&lt;AccelerationUnit&gt;; changing threshold that prevents lane
     *            changes that have very little benefit
     * @param nonPreferredLaneRouteIncentive DoubleScalar.Rel&lt;AccelerationUnit&gt;; route incentive to merge to the
     *            adjacent lane into which GTUs should merge to overtake other traffic
     * @return LaneChangeModelResult; the result of the lane change and GTU following model
     * @throws RemoteException in case the simulation time cannot be retrieved.
     */
    LaneChangeModelResult computeLaneChangeAndAcceleration(final AbstractLaneBasedGTU<?> gtu,
            final Collection<AbstractLaneBasedGTU<?>> sameLaneGTUs,
            final Collection<AbstractLaneBasedGTU<?>> preferredLaneGTUs,
            final Collection<AbstractLaneBasedGTU<?>> nonPreferredLaneGTUs,
            final DoubleScalar.Abs<SpeedUnit> speedLimit,
            final DoubleScalar.Rel<AccelerationUnit> preferredLaneRouteIncentive,
            Rel<AccelerationUnit> laneChangeThreshold,
            final DoubleScalar.Rel<AccelerationUnit> nonPreferredLaneRouteIncentive) throws RemoteException;

    /**
     * The result of a LaneChangeModel evaluation shall be stored in an instance of this class. <br />
     * Currently lane changes are instantaneous. To make lane changes take realistic time an additional field will be
     * needed that records the lateral position and speed.
     * <p>
     * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
     * reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version 3 nov. 2014 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
     * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
     */
    class LaneChangeModelResult
    {
        /** The resulting acceleration and duration of validity. */
        private final GTUFollowingModelResult gfmr;

        /**
         * Lane change. This has one of the following values:
         * <table>
         * <tr>
         * <td>null:</td>
         * <td>Stay in the current lane</td>
         * </tr>
         * <tr>
         * <td>LateralDirectionality.LEFT:</td>
         * <td>Move to the Left adjacent lane</td>
         * </tr>
         * <tr>
         * <td>LateralDirectionality.RIGHT:</td>
         * <td>Move to the Right adjacent lane</td>
         * </tr>
         * </table>
         */
        private final LateralDirectionality laneChange;

        /**
         * Construct a new LaneChangeModelResult.
         * @param gfmr GTUFollowingModelResult; the acceleration and duration of validity of this result.
         * @param laneChange LateralDirectionality; this has one of the values:
         *            <table>
         *            <tr>
         *            <td>null:</td>
         *            <td>Stay in the current lane</td>
         *            </tr>
         *            <tr>
         *            <td>LateralDirectionality.LEFT:</td>
         *            <td>Move to the Left adjacent lane</td>
         *            </tr>
         *            <tr>
         *            <td>LateralDirectionality.RIGHT:</td>
         *            <td>Move to the Right adjacent lane</td>
         *            </tr>
         *            </table>
         */
        public LaneChangeModelResult(final GTUFollowingModelResult gfmr, final LateralDirectionality laneChange)
        {
            this.gfmr = gfmr;
            this.laneChange = laneChange;
        }

        /**
         * @return the GTUModelFollowingResult.
         */
        public final GTUFollowingModelResult getGfmr()
        {
            return this.gfmr;
        }

        /**
         * @return laneChange. This has one of the values:
         *         <table>
         *         <tr>
         *         <td>null:</td>
         *         <td>Stay in the current lane</td>
         *         </tr>
         *         <tr>
         *         <td>LateralDirectionality.LEFT:</td>
         *         <td>Move to the Left adjacent lane</td>
         *         </tr>
         *         <tr>
         *         <td>LateralDirectionality.RIGHT:</td>
         *         <td>Move to the Right adjacent lane</td>
         *         </tr>
         *         </table>
         */
        public final LateralDirectionality getLaneChange()
        {
            return this.laneChange;
        }

        /** {@inheritDoc} */
        public final String toString()
        {
            return this.gfmr.toString() + ", "
                    + (null == this.laneChange ? "no lane change" : this.laneChange.toString());
        }

    }

}
