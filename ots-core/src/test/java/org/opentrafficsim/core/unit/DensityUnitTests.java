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
 * @version Jun 5, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @param <M> Mass unit underlying this density unit
 * @param <L> Length unit underlying this density unit
 */
public class DensityUnitTests<M extends MassUnit, L extends LengthUnit> extends AbstractUnitTest<DensityUnit<?, ?>>
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
    public void densityKeys()
    {
        checkKeys(DensityUnit.KG_PER_METER_3, "DensityUnit.kilogram_per_cubic_meter", "DensityUnit.kg/m^3");
    }

    /**
     * Verify conversion factors, English names and abbreviations
     */
    @Test
    public void conversions()
    {
        // TODO XXXXXX ERROR checkUnitRatioNameAndAbbreviation(DensityUnit.KG_PER_METER_3, 1, 0.00000001,
        // "kilogram per cubic meter", "kg/m^3");
        checkUnitRatioNameAndAbbreviation(DensityUnit.GRAM_PER_CENTIMETER_3, 1000, 0.0001, "gram per cubic centimeter",
                "g/cm^3");
        // Check two conversions between two units
        assertEquals("one KG PER CUBIC METER is 0.0001 GRAM PER CUBIC CENTIMETER", 0.001,
                DensityUnit.KG_PER_METER_3.getMultiplicationFactorTo(DensityUnit.GRAM_PER_CENTIMETER_3), 0.000000001);
    }

}
