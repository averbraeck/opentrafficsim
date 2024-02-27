package org.opentrafficsim.road.gtu.lane.tactical.toledo;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterSet;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionIterable;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionIterableSet;
import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;
import org.opentrafficsim.road.gtu.lane.tactical.util.CarFollowingUtil.CarFollowingHeadway;

/**
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */

public class ToledoCfTester
{

    /**
     * @param args String[]; arguments for the run (should be empty at the moment)
     * @throws ParameterException when Toledo parameters cannot be found
     * @throws GtuException when CarFollowingHeadway cannot be calculated
     */
    public static void main(final String[] args) throws ParameterException, GtuException
    {

        ParameterSet params = new ParameterSet();
        params.setDefaultParameters(ToledoCarFollowing.class);
        ToledoCarFollowing cf = new ToledoCarFollowing();

        Speed desiredSpeed = new Speed(120, SpeedUnit.KM_PER_HOUR);
        Duration dt = new Duration(0.5, DurationUnit.SECOND);
        Time t = Time.ZERO;

        Speed speed = Speed.ZERO;
        Length x = Length.ZERO;
        Length leader = new Length(300, LengthUnit.METER);

        while (x.eq0() || speed.gt0())
        {
            Length s = leader.minus(x);
            PerceptionIterable<Headway> leaders = new PerceptionIterableSet<>(new CarFollowingHeadway(s, Speed.ZERO));
            Length desiredHeadway = cf.desiredHeadway(params, speed);
            Acceleration a = cf.followingAcceleration(params, speed, desiredSpeed, desiredHeadway, leaders);
            System.out.println("t=" + t + ", v=" + speed + ", s=" + s + ", a=" + a);

            a = Acceleration.max(a, speed.divide(dt).neg());
            t = t.plus(dt);
            x = new Length(x.si + speed.si * dt.si + .5 * a.si * dt.si * dt.si, LengthUnit.SI);
            speed = new Speed(speed.si + a.si * dt.si, SpeedUnit.SI);

        }

    }

}
