package org.opentrafficsim.demo.ntm.ntmModel;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.junit.Test;
import org.opentrafficsim.car.Car;
import org.opentrafficsim.car.following.CarFollowingModel;
import org.opentrafficsim.car.following.IDMPlus;
import org.opentrafficsim.car.following.CarFollowingModel.CarFollowingModelResult;
import org.opentrafficsim.core.dsol.OTSDEVSSimulator;
import org.opentrafficsim.core.location.Line;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.demo.ntm.CsvFileReader;
import org.opentrafficsim.demo.ntm.NTMSettings;
import org.opentrafficsim.demo.ntm.ShapeFileReader;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 3 Oct 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @author <a href="http://www.citg.tudelft.nl">Yufei Yuan</a>
 */
public class NtmModelTest
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
        assertEquals("At t=initialTime the car should be at it's initial position", initialPosition.getSI(), referenceCar
                .getPosition(initialTime).getSI(), 0.0001);
        assertEquals("The car should store it's initial speed", initialSpeed.getSI(), referenceCar
                .getVelocity(initialTime).getSI(), 0.00001);

                
        
    }
}
