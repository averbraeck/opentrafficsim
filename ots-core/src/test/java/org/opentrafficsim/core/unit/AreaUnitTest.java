package org.opentrafficsim.core.unit;

import static org.junit.Assert.assertEquals;

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
 * @version Jun 4, 2014 <br>
 * @author <a href="http://tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class AreaUnitTest extends AbstractUnitTest<AreaUnit>
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
    public void keys()
    {
        checkKeys(AreaUnit.SQUARE_METER, "AreaUnit.square_meter", "AreaUnit.m^2");
    }

    /**
     * Verify conversion factors, English names and abbreviations
     */
    @Test
    public void conversions()
    {
        checkUnitRatioNameAndAbbreviation(AreaUnit.SQUARE_METER, 1, 0.00000001, "square meter", "m^2");
        checkUnitRatioNameAndAbbreviation(AreaUnit.SQUARE_KM, 1000000, 0.05, "square kilometer", "km^2");
        checkUnitRatioNameAndAbbreviation(AreaUnit.SQUARE_MILE, 2589990, 2, "square mile", "mi^2");
        // Check two conversions between non-standard units
        assertEquals("one SQUARE MILE is 640 ACRE", 640,
                getMultiplicationFactorTo(AreaUnit.SQUARE_MILE, AreaUnit.ACRE), 0.1);
        // Check conversion factor to standard unit for all remaining area units
        checkUnitRatioNameAndAbbreviation(AreaUnit.ARE, 100, 0.001, "are", "a");
        checkUnitRatioNameAndAbbreviation(AreaUnit.HECTARE, 10000, 0.01, "hectare", "ha");
        checkUnitRatioNameAndAbbreviation(AreaUnit.SQUARE_FOOT, 0.092903, 0.000001, "square foot", "ft^2");
        checkUnitRatioNameAndAbbreviation(AreaUnit.SQUARE_INCH, 0.00064516, 0.00000001, "square inch", "in^2");
        checkUnitRatioNameAndAbbreviation(AreaUnit.SQUARE_YARD, 0.836127, 0.000001, "square yard", "yd^2");
        checkUnitRatioNameAndAbbreviation(AreaUnit.ACRE, 4046.9, 0.05, "acre", "ac");
    }

}
