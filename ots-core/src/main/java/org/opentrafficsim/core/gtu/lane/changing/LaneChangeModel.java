package org.opentrafficsim.core.gtu.lane.changing;

import java.util.Collection;

import org.opentrafficsim.core.OTS_SCALAR;
import org.opentrafficsim.core.gtu.following.HeadwayGTU;
import org.opentrafficsim.core.gtu.lane.LaneBasedGTU;

/**
 * All lane change models must implement this interface.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$,
 *          initial version 3 nov. 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */
public interface LaneChangeModel extends OTS_SCALAR
{
    /**
     * Compute the acceleration and lane change.
     * @param gtu GTU; the GTU for which the acceleration and lane change is computed
     * @param sameLaneTraffic Collection&lt;GTU&gt;; the set of observable GTUs in the current lane (can not be null and may
     *            include the <code>gtu</code>)
     * @param rightLaneTraffic Collection&lt;GTU&gt;; the set of observable GTUs in the adjacent lane where GTUs should drive in
     *            the absence of other traffic (must be null if there is no such lane)
     * @param leftLaneTraffic Collection&lt;GTU&gt;; the set of observable GTUs in the adjacent lane into which GTUs should
     *            merge to overtake other traffic (must be null if there is no such lane)
     * @param speedLimit DoubleScalarAbs&lt;SpeedUnit&gt;; the local speed limit
     * @param preferredLaneRouteIncentive DoubleScalar.Rel&lt;AccelerationUnit&gt;; route incentive to merge to the adjacent
     *            lane where GTUs should drive in the absence of other traffic
     * @param laneChangeThreshold DoubleScalar.Rel&lt;AccelerationUnit&gt;; threshold that prevents lane changes that have very
     *            little benefit
     * @param nonPreferredLaneRouteIncentive DoubleScalar.Rel&lt;AccelerationUnit&gt;; route incentive to merge to the adjacent
     *            lane into which GTUs should merge to overtake other traffic
     * @return LaneMovementStep; the result of the lane change and GTU following model
     */
    @SuppressWarnings("checkstyle:parameternumber")
    LaneMovementStep computeLaneChangeAndAcceleration(final LaneBasedGTU gtu,
        final Collection<HeadwayGTU> sameLaneTraffic, final Collection<HeadwayGTU> rightLaneTraffic,
        final Collection<HeadwayGTU> leftLaneTraffic, final Speed.Abs speedLimit,
        final Acceleration.Rel preferredLaneRouteIncentive, final Acceleration.Rel laneChangeThreshold,
        final Acceleration.Rel nonPreferredLaneRouteIncentive);

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

}
