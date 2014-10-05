package org.opentrafficsim.core.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.opentrafficsim.core.locale.DefaultLocale;
import org.opentrafficsim.core.unit.unitsystem.UnitSystem;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Jun 6, 2014 <br>
 * @author <a href="http://tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class FlowVolumeUnitTest extends AbstractUnitTest<FlowVolumeUnit>
{
    /**
     * Set the locale to "en" so we know what texts should be retrieved from the resources.
     */
    @SuppressWarnings("static-method")
    @Before
    public final void setup()
    {
        DefaultLocale.setLocale(new Locale("en"));
    }

    /**
     * Verify the result of some get*Key methods.
     */
    @Test
    public final void keys()
    {
        checkKeys(FlowVolumeUnit.CUBIC_METER_PER_SECOND, "FlowVolumeUnit.cubic_meter_per_second",
                "FlowVolumeUnit.m^3/s");
    }

    /**
     * Verify conversion factors, English names and abbreviations.
     */
    @Test
    public final void conversions()
    {
        checkUnitRatioNameAndAbbreviation(FlowVolumeUnit.CUBIC_METER_PER_SECOND, 1, 0.000001, "cubic meter per second",
                "m^3/s");
        checkUnitRatioNameAndAbbreviation(FlowVolumeUnit.CUBIC_METER_PER_MINUTE, 0.0166667, 0.000001,
                "cubic meter per minute", "m^3/min");
        // Check two conversions between non-standard units
        assertEquals("one CUBIC METER PER HOUR is about 2.205 CUBIC_METER_PER_MINUTED", 0.01666667,
                getMultiplicationFactorTo(FlowVolumeUnit.CUBIC_METER_PER_HOUR, FlowVolumeUnit.CUBIC_METER_PER_MINUTE),
                0.00001);
        assertEquals("one CUBIC METER PER MINUTE is 60 CUBIC_METER_PER_HOUR", 60,
                getMultiplicationFactorTo(FlowVolumeUnit.CUBIC_METER_PER_MINUTE, FlowVolumeUnit.CUBIC_METER_PER_HOUR),
                0.0001);
    }

    /**
     * Verify that we can create our own FlowVolume unit.
     */
    @Test
    public final void createFLowVolumeUnit()
    {
        FlowVolumeUnit myFVU =
                new FlowVolumeUnit(UnitLocalizationsTest.DONOTCHECKPREFIX + "FlowVolumeUnit.TrucksPerHour",
                        UnitLocalizationsTest.DONOTCHECKPREFIX + "FlowVolumeUnit.tph", UnitSystem.OTHER,
                        FlowVolumeUnit.CUBIC_METER_PER_HOUR, 100);
        assertTrue("Can create a new FlowMassUnit", null != myFVU);
        checkUnitRatioNameAndAbbreviation(myFVU, 100. / 3600, 0.0001, "!TrucksPerHour!", "!tph!");
    }

}
