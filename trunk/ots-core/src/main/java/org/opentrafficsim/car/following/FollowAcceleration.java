package org.opentrafficsim.car.following;

import java.util.HashSet;
import java.util.Set;

import org.opentrafficsim.car.Car;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.MutableDoubleScalar;

/**
 * Determine acceleration (deceleration) for a Car that follows another car.
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
 * @version Sep 19, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public final class FollowAcceleration
{
    /**
     * This class should never be instantiated.
     */
    private FollowAcceleration()
    {
        // This class should never be instantiated
    }

    /**
     * Compute the acceleration (or deceleration) for a Car following another Car.
     * @param follower Car; the car that is following
     * @param leader Car; the car that is leading
     * @param when DoubleScalar.Abs&lt;TimeUnit&gt;; the current time
     * @param carFollowingModel CarFollowingModel; the car following model that is used to compute the result
     * @param speedLimit DoubleScalar.Abs&lt;SpeedUnit&gt;; the speed limit
     * @return DoubleScalar.Abs&lt;AccelerationUnit&gt;; the acceleration (deceleration) for the following car in order
     *         to not collide with the leader car
     */
    public static DoubleScalar.Abs<AccelerationUnit> acceleration(final Car follower, final Car leader,
            final DoubleScalar.Abs<TimeUnit> when, final CarFollowingModel carFollowingModel,
            final DoubleScalar.Abs<SpeedUnit> speedLimit)
    {
        if (null != leader)
        {
            MutableDoubleScalar.Rel<LengthUnit> headway =
                    MutableDoubleScalar.minus(leader.positionOfRear(when), follower.positionOfFront(when));
            if (headway.getValueSI() <= 0)
            { // Immediate collision; return a prohibitive negative value
                return new DoubleScalar.Abs<AccelerationUnit>(Double.NEGATIVE_INFINITY,
                        AccelerationUnit.METER_PER_SECOND_2);
            }
        }
        // Wrap the leader in a set, then apply the car following model
        Set<Car> leaders = new HashSet<Car>(1);
        if (null != leader)
        {
            leaders.add(leader);
        }
        return carFollowingModel.computeAcceleration(follower, leaders, speedLimit).acceleration;
    }

}
