package org.opentrafficsim.car;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
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
        assertEquals("At t=initialTime the car should be at it's initial position", initialPosition.getValueSI(), referenceCar
                .getPosition(initialTime).getValueSI(), 0.0001);
        assertEquals("The car should store it's initial speed", initialSpeed.getValueSI(), referenceCar
                .getVelocity(initialTime).getValueSI(), 0.00001);
        assertEquals("The car should have an initial acceleration equal to 0", 0, referenceCar.acceleration.getValueSI(),
                0.0001);
    }
}
