package org.opentrafficsim.car.following;

import java.util.Collection;

import org.opentrafficsim.car.Car;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalarAbs;

/**
 * Abstract car following model.
 * <p>
 * Copyright (c) 2002-2014 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights
 * reserved.
 * <p>
 * See for project information <a href="http://www.simulation.tudelft.nl/"> www.simulation.tudelft.nl</a>.
 * <p>
 * The OpenTrafficSim project is distributed under the following BSD-style license:<br>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * <ul>
 * <li>Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.</li>
 * <li>Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.</li>
 * <li>Neither the name of Delft University of Technology, nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.</li>
 * </ul>
 * This software is provided by the copyright holders and contributors "as is" and any express or implied warranties,
 * including, but not limited to, the implied warranties of merchantability and fitness for a particular purpose are
 * disclaimed. In no event shall the copyright holder or contributors be liable for any direct, indirect, incidental,
 * special, exemplary, or consequential damages (including, but not limited to, procurement of substitute goods or
 * services; loss of use, data, or profits; or business interruption) however caused and on any theory of liability,
 * whether in contract, strict liability, or tort (including negligence or otherwise) arising in any way out of the use
 * of this software, even if advised of the possibility of such damage.
 * @version Jul 2, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public interface CarFollowingModel
{
    /**
     * Compute the acceleration that would be used to follow a set of leaders.
     * @param car Car; the Car for which acceleration is computed
     * @param leaders Set&lt;Car&gt;; the set of leaders to take into consideration
     * @param speedLimit DoubleScalarAbs&lt;SpeedUnit&gt;; the local speed limit
     * @return CarFollowingModelResult; the result of application of the car following model
     */
    CarFollowingModelResult computeAcceleration(final Car car, final Collection<Car> leaders,
            final DoubleScalarAbs<SpeedUnit> speedLimit);
    
    /**
     * Compute the acceleration and lane change.
     * @param car Car; the Car for which the acceleration and lane change is computed
     * @param sameLaneCars Collection&lt;Car&gt;; the set of observable Cars in the current lane (can not be null)
     * @param preferredLaneCars Collection&lt;Car&gt;; the set of observable Cars in the adjacent lane where cars should
     *            drive in the absence of other traffic (must be null if there is no such lane)
     * @param nonPreferredLaneCars Collection&lt;Car&gt;; the set of observable Cars in the adjacent lane into which
     *            cars should merge to overtake other traffic (must be null if there is no such lane)
     * @param speedLimit DoubleScalarAbs&lt;SpeedUnit&gt;; the local speed limit
     * @param preferredLaneRouteIncentive Double; route incentive to merge to the adjacent lane where cars should drive
     *            in the absence of other traffic
     * @param nonPreferredLaneRouteIncentive Double; route incentive to merge to the adjacent lane into which cars
     *            should merge to overtake other traffic
     * @return CarFollowingModelResult; the result of the lane change and car following model
     */
    CarFollowingModelResult computeLaneChangeAndAcceleration(final Car car, final Collection<Car> sameLaneCars,
            final Collection<Car> preferredLaneCars, final Collection<Car> nonPreferredLaneCars,
            final DoubleScalarAbs<SpeedUnit> speedLimit, double preferredLaneRouteIncentive,
            double nonPreferredLaneRouteIncentive);
    
    /**
     * The result of a CarFollowingModel evaluation shall be stored in an instance of this class.
     * <p>
     * Copyright (c) 2002-2014 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights
     * reserved.
     * <p>
     * See for project information <a href="http://www.simulation.tudelft.nl/"> www.simulation.tudelft.nl</a>.
     * <p>
     * The OpenTrafficSim project is distributed under the following BSD-style license:<br>
     * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
     * following conditions are met:
     * <ul>
     * <li>Redistributions of source code must retain the above copyright notice, this list of conditions and the
     * following disclaimer.</li>
     * <li>Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
     * following disclaimer in the documentation and/or other materials provided with the distribution.</li>
     * <li>Neither the name of Delft University of Technology, nor the names of its contributors may be used to endorse
     * or promote products derived from this software without specific prior written permission.</li>
     * </ul>
     * This software is provided by the copyright holders and contributors "as is" and any express or implied
     * warranties, including, but not limited to, the implied warranties of merchantability and fitness for a particular
     * purpose are disclaimed. In no event shall the copyright holder or contributors be liable for any direct,
     * indirect, incidental, special, exemplary, or consequential damages (including, but not limited to, procurement of
     * substitute goods or services; loss of use, data, or profits; or business interruption) however caused and on any
     * theory of liability, whether in contract, strict liability, or tort (including negligence or otherwise) arising
     * in any way out of the use of this software, even if advised of the possibility of such damage.
     * @version Jul 9, 2014 <br>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    class CarFollowingModelResult
    {
        /** Acceleration that will be maintained during the current time slot. */
        public final DoubleScalarAbs<AccelerationUnit> acceleration;

        /** Time when the current time slot ends. */
        public final DoubleScalarAbs<TimeUnit> validUntil;

        /**
         * Lane change; 0: stay in current lane; -1 merge onto adjacent overtaking lane; +1 merge towards the default
         * lane.
         */
        public final int laneChange;

        /**
         * Create a new CarFollowingModelResult.
         * @param acceleration DoubleScalarAbs&lt;AccelerationUnit&gt;; computed acceleration
         * @param validUntil DoubleScalarAbs&ltTimeUnit&gt;; time when this result expires
         * @param laneChange Integer; the lane determined change; 0: stay in current lane; -1 merge onto adjacent
         *            overtaking lane; +1 merge towards the default lane
         */
        public CarFollowingModelResult(final DoubleScalarAbs<AccelerationUnit> acceleration,
                final DoubleScalarAbs<TimeUnit> validUntil, final int laneChange)
        {
            this.acceleration = acceleration;
            this.validUntil = validUntil;
            this.laneChange = laneChange;
        }
    }
}
