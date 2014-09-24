package org.opentrafficsim.core.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.OTHER;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.opentrafficsim.core.locale.DefaultLocale;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Jun 6, 2014 <br>
 * @author <a href="http://tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class VolumeUnitTest extends AbstractUnitTest<VolumeUnit>
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
        checkKeys(VolumeUnit.CUBIC_METER, "VolumeUnit.cubic_meter", "VolumeUnit.m^3");
    }

    /**
     * Verify conversion factors, English names and abbreviations.
     */
    @Test
    public final void conversions()
    {
        checkUnitRatioNameAndAbbreviation(VolumeUnit.CUBIC_METER, 1, 0.00000001, "cubic meter", "m^3");
        checkUnitRatioNameAndAbbreviation(VolumeUnit.CUBIC_DECIMETER, 0.001, 0.0000000001, "cubic decimeter", "dm^3");
        checkUnitRatioNameAndAbbreviation(VolumeUnit.LITER, 0.001, 0.0000000001, "liter", "L");
        // Check two conversions between non-standard units
        assertEquals("one CUBIC MILE is about 5451776000 CUBIC YARD", 5451776000.,
                getMultiplicationFactorTo(VolumeUnit.CUBIC_MILE, VolumeUnit.CUBIC_YARD), 0.5);
        assertEquals("one CUBIC YARD is 1.83426465e-10 CUBIC MILE", 1.83426465e-10,
                getMultiplicationFactorTo(VolumeUnit.CUBIC_YARD, VolumeUnit.CUBIC_MILE), 0.0000000001);
        // Check conversion factor to standard unit for all remaining time units
        checkUnitRatioNameAndAbbreviation(VolumeUnit.CUBIC_CENTIMETER, 0.000001, 0.000000000001, "cubic centimeter", "cm^3");
        checkUnitRatioNameAndAbbreviation(VolumeUnit.CUBIC_KM, 1e9, 1, "cubic kilometer", "km^3");
        checkUnitRatioNameAndAbbreviation(VolumeUnit.CUBIC_MILE, 4.16818183e9, 1000, "cubic mile", "mi^3");
        checkUnitRatioNameAndAbbreviation(VolumeUnit.CUBIC_FOOT, 0.0283168, 0.0000001, "cubic foot", "ft^3");
        checkUnitRatioNameAndAbbreviation(VolumeUnit.CUBIC_INCH, 1.6387e-5, 1e-9, "cubic inch", "in^3");
        checkUnitRatioNameAndAbbreviation(VolumeUnit.CUBIC_YARD, 0.764554858, 0.0000001, "cubic yard", "yd^3");
        checkUnitRatioNameAndAbbreviation(VolumeUnit.GALLON_US_FLUID, 0.0037854, 0.0000001, "gallon (US)", "gal(US)");
        checkUnitRatioNameAndAbbreviation(VolumeUnit.OUNCE_US_FLUID, 0.000029574, 0.000000001, "ounce (fluid US)", "US fl oz");
        // checkUnitRatioNameAndAbbreviation(VolumeUnit.OUNCE_IMP_FLUID, .00002841306, 0.00000000001,
        // "horsepower (metric)", "hp(M)");
        // checkUnitRatioNameAndAbbreviation(VolumeUnit.PINT_US_FLUID, 0.000473176473, 0.0000000000001, "pt(US fl)", "hp(M)");
        // checkUnitRatioNameAndAbbreviation(VolumeUnit.PINT_IMP, 735.49875, 0.00001, "horsepower (metric)", "hp(M)");
        // checkUnitRatioNameAndAbbreviation(VolumeUnit.QUART_US_FLUID, 0.000946353, 0.0000000001, "qt(US fl)", "hp(M)");
        // checkUnitRatioNameAndAbbreviation(VolumeUnit.QUART_IMP, 0.00113652, 0.000005, "quart (imperial)", "qt (imp)");
    }

    /**
     * Verify that we can create our own power unit.
     */
    @Test
    public final void createVolumeUnit()
    {
        VolumeUnit myVU =
                new VolumeUnit(UnitLocalizationsTest.DONOTCHECKPREFIX + "VolumeUnit.Barrel",
                        UnitLocalizationsTest.DONOTCHECKPREFIX + "VolumeUnit.brl", OTHER, VolumeUnit.LITER, 119.240471);
        assertTrue("Can create a new VolumeUnit", null != myVU);
        checkUnitRatioNameAndAbbreviation(myVU, 0.119240471, 0.000001, "!Barrel!", "!brl!");
    }

}
