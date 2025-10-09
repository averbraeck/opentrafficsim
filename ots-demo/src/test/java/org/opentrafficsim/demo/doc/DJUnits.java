package org.opentrafficsim.demo.doc;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;

/**
 * This class contains code snippets that are used in the documentation. Whenever errors arise in this code, they need to be
 * fixed -and- the code in the documentation needs to be updated.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
@SuppressWarnings({"unused", "javadoc", "null", "designforextension"})
public class DJUnits
{

    static
    {
        // @docs/02-model-structure/djunits.md
        Speed speedKm = new Speed(90.0, SpeedUnit.KM_PER_HOUR);
        double speedKmps = speedKm.getInUnit(SpeedUnit.KM_PER_SECOND);

        // @docs/02-model-structure/djunits.md
        Speed speedSi = Speed.ofSI(25.0);

        // @docs/02-model-structure/djunits.md
        System.out.println(speedSi.equals(speedKm)); // true
        System.out.println(speedSi.si + ", " + speedSi); // 25.0, 25.0000000m/s
        System.out.println(speedKm.si + ", " + speedKm); // 25.0, 90.0000000km/h
    }

    // @docs/02-model-structure/djunits.md
    public Length move(final Speed v, final Duration t, final Acceleration a)
    {
        return v.times(t).plus(a.times(.5).times(t).times(t));
    }

    class MoveSi
    {
        // @docs/02-model-structure/djunits.md
        public Length move(final Speed v, final Duration t, final Acceleration a)
        {
            return Length.ofSI(v.si * t.si + .5 * a.si * t.si * t.si);
        }
    }

    static
    {
        Speed v = null;
        Duration t = null;
        Acceleration a = null;
        // @docs/02-model-structure/djunits.md
        Length.ofSI(v.si * t.si + .5 * a.si * t.si);
    }

}
