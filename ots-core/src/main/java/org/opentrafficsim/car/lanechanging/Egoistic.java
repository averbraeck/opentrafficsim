package org.opentrafficsim.car.lanechanging;

import java.util.Set;

import org.opentrafficsim.car.Car;
import org.opentrafficsim.car.following.CarFollowingModel;
import org.opentrafficsim.car.following.FollowAcceleration;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.MutableDoubleScalar;

/**
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
public final class Egoistic
{
    /**
     * This class should never be instantiated.
     */
    private Egoistic()
    {
        // This class should never be instantiated.
    }

    /**
     * @param referenceCar Car; the car that considers changing lane
     * @param otherCars Set&lt;Car&gt;; the cars in the target lane
     * @param keepCurrentLaneBias DoubleScalar.Abs&lt;AccelerationUnit&gt;; the bias against changing towards the target
     *            lane
     * @param maximumDeceleration DoubleScalar.Abs&lt;AccelerationUnit&gt;; the maximum (deemed safe) deceleration. This
     *            is a positive value, about 2 m/s/s.
     * @param speedLimit DoubleScalar.Abs&lt;SpeedUnit&gt;; the speed limit
     * @return DoubleScalar.Abs&lt;AccelerationUnit&gt;; the lowest acceleration (highest deceleration) incurred if the
     *         lane change is carried out
     */
    public static DoubleScalar.Abs<AccelerationUnit> acceleration(final Car referenceCar, final Set<Car> otherCars,
            final DoubleScalar.Abs<AccelerationUnit> keepCurrentLaneBias,
            final DoubleScalar.Rel<AccelerationUnit> maximumDeceleration, final DoubleScalar.Abs<SpeedUnit> speedLimit)
    {
        Car leader = null;
        DoubleScalar.Rel<LengthUnit> leaderHeadway = null;
        Car follower = null;
        DoubleScalar.Rel<LengthUnit> followerHeadway = null;
        DoubleScalar.Abs<TimeUnit> when = referenceCar.getNextEvaluationTime();
        DoubleScalar.Abs<LengthUnit> referenceCarPosition = referenceCar.getPosition(when);
        // TODO: if otherCars are sorted in some way, scanning the entire list like this is not needed.

        // TODO: the car following model already deals with a Set of leaders; can't we do something similar with the set
        // of followers?
        for (Car c : otherCars)
        {
            DoubleScalar.Rel<LengthUnit> headway =
                    MutableDoubleScalar.minus(c.getPosition(when), referenceCarPosition).immutable();
            if (headway.getValueSI() < 0)
            {
                if (null == follower || followerHeadway.getValueSI() > headway.getValueSI())
                {
                    follower = c;
                    followerHeadway = headway;
                }
            }
            else
            {
                if (null == leader || leaderHeadway.getValueSI() < headway.getValueSI())
                {
                    leader = c;
                    leaderHeadway = headway;
                }
            }
        }
        CarFollowingModel carFollowingModel = referenceCar.getCarFollowingModel();
        DoubleScalar.Abs<AccelerationUnit> followerAcceleration =
                FollowAcceleration.acceleration(follower, referenceCar, when, carFollowingModel, speedLimit);
        if (followerAcceleration.getValueSI() < -maximumDeceleration.getValueSI())
        {
            return new DoubleScalar.Abs<AccelerationUnit>(Double.NEGATIVE_INFINITY, AccelerationUnit.METER_PER_SECOND_2);
        }
        DoubleScalar.Abs<AccelerationUnit> referenceAcceleration =
                FollowAcceleration.acceleration(referenceCar, leader, when, carFollowingModel, speedLimit);
        if (followerAcceleration.getValueSI() < referenceAcceleration.getValueSI())
        {
            return followerAcceleration;
        }
        return referenceAcceleration;
    }
}
