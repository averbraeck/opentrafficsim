package org.opentrafficsim.core.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.SI_DERIVED;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.opentrafficsim.core.locale.DefaultLocale;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial versionJun 5, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class DensityUnitTest extends AbstractUnitTest<DensityUnit>
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
        checkKeys(DensityUnit.KG_PER_METER_3, "DensityUnit.kilogram_per_cubic_meter", "DensityUnit.kg/m^3");
    }

    /**
     * Verify conversion factors, English names and abbreviations.
     */
    @Test
    public final void conversions()
    {
        checkUnitRatioNameAndAbbreviation(DensityUnit.KG_PER_METER_3, 1, 0.00000001, "kilogram per cubic meter",
                "kg/m^3");
        checkUnitRatioNameAndAbbreviation(DensityUnit.GRAM_PER_CENTIMETER_3, 1000, 0.0001, "gram per cubic centimeter",
                "g/cm^3");
        // Check two conversions between two units
        assertEquals("one KG PER CUBIC METER is 0.0001 GRAM PER CUBIC CENTIMETER", 0.001,
                getMultiplicationFactorTo(DensityUnit.KG_PER_METER_3, DensityUnit.GRAM_PER_CENTIMETER_3), 0.000000001);
    }

    /**
     * Verify that we can create our own density unit.
     */
    @Test
    public final void createDensityUnit()
    {
        DensityUnit myDU =
                new DensityUnit(UnitLocalizationsTest.DONOTCHECKPREFIX + "DensityUnit.DensityUnit",
                        UnitLocalizationsTest.DONOTCHECKPREFIX + "DensityUnit.SPCF", SI_DERIVED,
                        DensityUnit.KG_PER_METER_3, 515.317882);
        assertTrue("Can create a new DensityUnit", null != myDU);
        checkUnitRatioNameAndAbbreviation(myDU, 515.3, 0.1, "!DensityUnit!", "!SPCF!");
    }

}
