package org.opentrafficsim.road.gtu.lane.tactical.toledo;

import java.util.SortedMap;
import java.util.TreeMap;

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

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
     */
    public static void main(final String[] args) throws ParameterException
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

        SortedMap<Length, Speed> leaders = new TreeMap<>();
        while (x.eq0() || speed.gt0())
        {
            Length s = leader.minus(x);
            leaders.clear();
            leaders.put(s, Speed.ZERO);
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
