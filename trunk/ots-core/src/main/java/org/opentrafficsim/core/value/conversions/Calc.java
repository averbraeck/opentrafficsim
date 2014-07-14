package org.opentrafficsim.core.value.conversions;

import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalarAbs;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalarRel;

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
 * @version Jul 4, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class Calc
{
    /**
     * Distance is speed times time. <br />
     * s(t) = v * t
     * @param speed DoubleScalarRel&lt;SpeedUnit&gt;; the speed
     * @param time DoubleScalarRel&lt;TimeUnit&gt;; the time
     * @return DoubleScalarRel&lt;LengthUnit&gt;; the resulting distance
     */
    public static DoubleScalarRel<LengthUnit> speedTimesTime(DoubleScalarRel<SpeedUnit> speed,
            DoubleScalarRel<TimeUnit> time)
    {
        return new DoubleScalarRel<LengthUnit>(speed.getValueSI() * time.getValueSI(), LengthUnit.METER);
    }

    /**
     * Distance is 0.5 times acceleration times time squared/ <br />
     * s(t) = 0.5 * a * t * t
     * @param acceleration DoubleScalarRel&lt;AccelerationUnit&gt;; the acceleration
     * @param time DoubleScalarAbs&lt;TimeUnit&gt;; the time
     * @return DoubleScalarRel&lt;LengthUnit&gt;; the resulting distance
     */
    public static DoubleScalarRel<LengthUnit> accelerationTimesTimeSquaredDiv(
            DoubleScalarAbs<AccelerationUnit> acceleration, DoubleScalarRel<TimeUnit> time)
    {
        double t = time.getValueSI();
        return new DoubleScalarRel<LengthUnit>(0.5 * acceleration.getValueSI() * t * t, LengthUnit.METER);
    }

    /**
     * Speed is acceleration times time. <br />
     * v(t) = a * t
     * @param acceleration DoubleScalarRel&lt;AccelerationUnit&gt;; the acceleration
     * @param time DoubleScalarRel&lt;TimeUnit&gt;; the time
     * @return DoubleScalarRel&lt;SpeedUnit&gt;; the resulting speed
     */
    public static DoubleScalarRel<SpeedUnit> accelerationTimesTime(DoubleScalarAbs<AccelerationUnit> acceleration,
            DoubleScalarRel<TimeUnit> time)
    {
        return new DoubleScalarRel<SpeedUnit>(acceleration.getValueSI() * time.getValueSI(), SpeedUnit.METER_PER_SECOND);
    }

    /**
     * Time is speed divided by acceleration. <br />
     * t = v / a
     * @param speed DoubleScalarRel&lt;SpeedUnit&gt;; the speed
     * @param acceleration DoubleScalarRel&ltAccelerationUnit&gt;; the acceleration
     * @return DoubleScalarRel&lt;TimeUnit&gt;; the time it takes to accelerate using the given acceleration from 0 to
     *         the indicated speed
     */
    public static DoubleScalarRel<TimeUnit> speedDividedByAcceleration(DoubleScalarRel<SpeedUnit> speed,
            DoubleScalarRel<AccelerationUnit> acceleration)
    {
        return new DoubleScalarRel<TimeUnit>(speed.getValueSI() / acceleration.getValueSI(), TimeUnit.SECOND);
    }
}
