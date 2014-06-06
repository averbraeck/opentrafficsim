package org.opentrafficsim.core.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.opentrafficsim.core.locale.DefaultLocale;

/**
 * <p>
 * Copyright (c) 2002-2014 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights
 * reserved.
 * <p>
 * See for project information <a href="http://www.simulation.tudelft.nl/"> www.simulation.tudelft.nl</a>.
 * <p>
 * The DSOL project is distributed under the following BSD-style license:<br>
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
 * @version Jun 6, 2014 <br>
 * @author <a href="http://tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <L> Length unit underlying this Volume unit
 */
public class VolumeUnitTests<L extends LengthUnit> extends AbstractUnitTest<VolumeUnit<?>>
{
    /**
     * Set the locale to "en" so we know what texts should be retrieved from the resources
     */
    @SuppressWarnings("static-method")
    @Before
    public void setup()
    {
        DefaultLocale.setLocale(new Locale("en"));
    }

    /**
     * Verify the result of some get*Key methods
     */
    @Test
    public void volumeKeys()
    {
        checkKeys(VolumeUnit.CUBIC_METER, "VolumeUnit.cubic_meter", "VolumeUnit.m^3");
    }

    /**
     * Verify conversion factors, English names and abbreviations
     */
    @Test
    public void conversions()
    {
        checkUnitRatioNameAndAbbreviation(VolumeUnit.CUBIC_METER, 1, 0.00000001, "cubic meter", "m^3");
        checkUnitRatioNameAndAbbreviation(VolumeUnit.CUBIC_DECIMETER, 0.001, 0.0000000001, "cubic decimeter", "dm^3");
        // checkUnitRatioNameAndAbbreviation(VolumeUnit.LITER, 0.001, 0.0000000001, "foot pound-force per minute",
        // "ft.lbf/min");
        // Check two conversions between non-standard units
        assertEquals("one CUBIC MILE is about 5451776000 CUBIC YARD", 5451776000.,
                getMultiplicationFactorTo(VolumeUnit.CUBIC_MILE, VolumeUnit.CUBIC_YARD), 0.5);
        assertEquals("one CUBIC YARD is 1.83426465e-10 CUBIC MILE", 1.83426465e-10,
                getMultiplicationFactorTo(VolumeUnit.CUBIC_YARD, VolumeUnit.CUBIC_MILE), 0.0000000001);
        // Check conversion factor to standard unit for all remaining time units
        checkUnitRatioNameAndAbbreviation(VolumeUnit.CUBIC_CENTIMETER, 0.000001, 0.000000000001, "cubic centimeter",
                "cm^3");
        checkUnitRatioNameAndAbbreviation(VolumeUnit.CUBIC_KM, 1000000000., 1, "cubic kilometer", "km^3");
        checkUnitRatioNameAndAbbreviation(VolumeUnit.CUBIC_MILE, 4.16818183e9, 1000, "cubic mile", "mi^3");
        checkUnitRatioNameAndAbbreviation(VolumeUnit.CUBIC_FOOT, 0.0283168, 0.0000001, "cubic foot", "ft^3");
        checkUnitRatioNameAndAbbreviation(VolumeUnit.CUBIC_INCH, 1.6387e-5, 1e-9, "cubic inch", "in^3");
        checkUnitRatioNameAndAbbreviation(VolumeUnit.CUBIC_YARD, 0.764554858, 0.0000001, "cubic yard", "yd^3");
        // TODO ERROR checkUnitRatioNameAndAbbreviation(VolumeUnit.GALLON_US_FLUID, 0.0037854, 0.00000001, "horsepower (metric)",
        //        "hp(M)");
        // TODO IDEM checkUnitRatioNameAndAbbreviation(VolumeUnit.OUNCE_US_FLUID, 0.000029574, 0.0000000001, "horsepower (metric)", "hp(M)");
        // TODO IDEM checkUnitRatioNameAndAbbreviation(VolumeUnit.OUNCE_IMP_FLUID, .00002841306, 0.00000000001, "horsepower (metric)",
        //        "hp(M)");
        //checkUnitRatioNameAndAbbreviation(VolumeUnit.PINT_US_FLUID, 735.49875, 0.00001, "horsepower (metric)", "hp(M)");
        //checkUnitRatioNameAndAbbreviation(VolumeUnit.PINT_IMP, 735.49875, 0.00001, "horsepower (metric)", "hp(M)");
        //checkUnitRatioNameAndAbbreviation(VolumeUnit.QUART_US_FLUID, 735.49875, 0.00001, "horsepower (metric)", "hp(M)");
        //checkUnitRatioNameAndAbbreviation(VolumeUnit.QUART_IMP, 735.49875, 0.00001, "horsepower (metric)", "hp(M)");
    }

    /**
     * Verify that we can create our own power unit
     */
    @Test
    public void createVolumeUnit()
    {
        VolumeUnit<LengthUnit> myVU =
                new VolumeUnit<LengthUnit>("VolumeUnit.Barrel", "VolumeUnit.brl", VolumeUnit.LITER, 119.240471);
        assertTrue("Can create a new VolumeUnit", null != myVU);
        checkUnitRatioNameAndAbbreviation(myVU, 119.240471, 0.000001, "!Barrel!", "!brl!");
    }

}
