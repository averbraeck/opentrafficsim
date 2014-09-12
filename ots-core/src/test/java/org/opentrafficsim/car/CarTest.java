package org.opentrafficsim.car;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

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
 * @version Jul 11, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class CarTest
{
    /**
     * Test some basics of the Car class
     */
    @SuppressWarnings("static-method")
    @Test
    public void carTest()
    {
        DoubleScalar.Abs<TimeUnit> initialTime = new DoubleScalar.Abs<TimeUnit>(0, TimeUnit.SECOND);
        DoubleScalar.Abs<LengthUnit> initialPosition = new DoubleScalar.Abs<LengthUnit>(12, LengthUnit.METER);
        DoubleScalar.Rel<SpeedUnit> initialSpeed = new DoubleScalar.Rel<SpeedUnit>(34, SpeedUnit.KM_PER_HOUR);
        Car referenceCar = new Car(12345, null, null, initialTime, initialPosition, initialSpeed);
        assertEquals("The car should store it's ID", 12345, (int) referenceCar.getID());
        assertEquals("At t=initialTime the car should be at it's initial position", initialPosition.getValueSI(), referenceCar.getPosition(initialTime).getValueSI(), 0.0001);
        assertEquals("The car should store it's initial speed", initialSpeed.getValueSI(), referenceCar.getVelocity(initialTime).getValueSI(), 0.00001);
        assertEquals("The car should have an initial acceleration equal to 0", 0, referenceCar.acceleration.getValueSI(), 0.0001);
    }
}
