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
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionIterable;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionIterableSet;
import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;
import org.opentrafficsim.road.gtu.lane.tactical.util.CarFollowingUtil.CarFollowingHeadway;

/**
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jul 20, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */

public class ToledoCFTester
{

    /**
     * @param args arguments for the run (should be empty at the moment)
     * @throws ParameterException when Toledo parameters cannot be found
     * @throws GTUException when CarFollowingHeadway cannot be calculated
     */
    public static void main(final String[] args) throws ParameterException, GTUException
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

            a = Acceleration.max(a, speed.divideBy(dt).neg());
            t = t.plus(dt);
            x = new Length(x.si + speed.si * dt.si + .5 * a.si * dt.si * dt.si, LengthUnit.SI);
            speed = new Speed(speed.si + a.si * dt.si, SpeedUnit.SI);

        }

    }

}
