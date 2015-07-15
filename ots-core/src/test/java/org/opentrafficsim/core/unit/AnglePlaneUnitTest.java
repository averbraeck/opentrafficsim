package org.opentrafficsim.core.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.OTHER;

import java.util.ArrayList;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.opentrafficsim.core.locale.DefaultLocale;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vfloat.scalar.FloatScalar;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial versionJun 4, 2014 <br>
 * @author <a href="http://tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class AnglePlaneUnitTest extends AbstractUnitTest<AnglePlaneUnit>
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
        checkKeys(AnglePlaneUnit.RADIAN, "AnglePlaneUnit.radian", "AnglePlaneUnit.rad");
    }

    /**
     * Verify conversion factors, English names and abbreviations.
     */
    @Test
    public final void conversions()
    {
        checkUnitRatioNameAndAbbreviation(AnglePlaneUnit.DEGREE, 2 * Math.PI / 360, 0.000001, "degree", "\u00b0");
        checkUnitRatioNameAndAbbreviation(AnglePlaneUnit.ARCMINUTE, 2 * Math.PI / 360 / 60, 0.0001, "arcminute", "\'");
        checkUnitRatioNameAndAbbreviation(AnglePlaneUnit.GRAD, 2 * Math.PI / 400, 0.00001, "gradian", "grad");
        // Check two conversions between non-standard units
        assertEquals("one GRAD is about 54 ARCMINUTE", 54,
                getMultiplicationFactorTo(AnglePlaneUnit.GRAD, AnglePlaneUnit.ARCMINUTE), 0.5);
        assertEquals("one ARCMINUTE is about 0.0185 GRAD", 0.0185,
                getMultiplicationFactorTo(AnglePlaneUnit.ARCMINUTE, AnglePlaneUnit.GRAD), 0.0001);
        // Check conversion factor to standard unit for all remaining time units
        checkUnitRatioNameAndAbbreviation(AnglePlaneUnit.CENTESIMAL_ARCMINUTE, 0.00015708, 0.0000001,
                "centesimal arcminute", "\'");
        checkUnitRatioNameAndAbbreviation(AnglePlaneUnit.CENTESIMAL_ARCSECOND, 1.57079e-6, 0.1, "centesimal arcsecond",
                "\"");
    }

    /**
     * Verify that we can create our own angle unit.
     */
    @Test
    public final void createAngleUnit()
    {
        AnglePlaneUnit myAPU =
                new AnglePlaneUnit(UnitLocalizationsTest.DONOTCHECKPREFIX + "AnglePlaneUnit.point",
                        UnitLocalizationsTest.DONOTCHECKPREFIX + "AnglePlaneUnit.pt", OTHER, AnglePlaneUnit.RADIAN,
                        0.19634954085);
        assertTrue("Can create a new AngleUnit", null != myAPU);
        checkUnitRatioNameAndAbbreviation(myAPU, 0.19634954085, 0.0000001, "!point!", "!pt!");
    }

    /**
     * Check normalize for all data types.
     * @param expected double; expected value after normalization
     * @param input double; value to normalize
     */
    private void checkDoubleNormalize(double input)
    {
        double margin = 0.00000000001;
        double expected = input;
        while (expected > 0)
        {
            expected -= 2 * Math.PI;
        }
        while (expected < 0)
        {
            expected += 2 * Math.PI;
        }
        assertEquals("double normalize", expected, AnglePlaneUnit.normalize(input), margin);
        DoubleScalar.Abs<AnglePlaneUnit> dsa = new DoubleScalar.Abs<AnglePlaneUnit>(input, AnglePlaneUnit.SI);
        assertEquals("DoubleScalar.Abs normalize", expected, AnglePlaneUnit.normalize(dsa).getSI(), margin);
        DoubleScalar.Rel<AnglePlaneUnit> dsr = new DoubleScalar.Rel<AnglePlaneUnit>(input, AnglePlaneUnit.SI);
        assertEquals("DoubleScalar.Rel normalize", expected, AnglePlaneUnit.normalize(dsr).getSI(), margin);
    }

    /**
     * Check normalize for all data types.
     * @param expected double; expected value after normalization
     * @param input double; value to normalize
     */
    private void checkFloatNormalize(float input)
    {
        double margin = 0.00001;
        float expected = input;
        while (expected > 0)
        {
            expected -= 2 * Math.PI;
        }
        while (expected < 0)
        {
            expected += 2 * Math.PI;
        }
        assertEquals("float normalize", expected, AnglePlaneUnit.normalize(input), margin);
        FloatScalar.Abs<AnglePlaneUnit> fsa = new FloatScalar.Abs<AnglePlaneUnit>(input, AnglePlaneUnit.SI);
        assertEquals("FloatScalar.Abs normalize", expected, AnglePlaneUnit.normalize(fsa).getSI(), margin);
        FloatScalar.Rel<AnglePlaneUnit> fsr = new FloatScalar.Rel<AnglePlaneUnit>(input, AnglePlaneUnit.SI);
        assertEquals("FloatScalar.Rek normalize", expected, AnglePlaneUnit.normalize(fsr).getSI(), margin);
    }

    /**
     * Verify that the normalizations work as intended.
     */
    @Test
    public final void normalizations()
    {
        for (int i = -100; i <= 100; i++)
        {
            double doubleValue = i * Math.PI / 10;
            checkDoubleNormalize(doubleValue - i * Math.ulp(doubleValue));
            checkDoubleNormalize(doubleValue + i * Math.ulp(doubleValue));
            float floatValue = (float) (i * Math.PI / 10);
            checkFloatNormalize(floatValue - i * Math.ulp(floatValue));
            checkFloatNormalize(floatValue + i * Math.ulp(floatValue));
        }
    }

}
