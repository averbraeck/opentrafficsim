package core.value.vfloat.scalar;

import static org.junit.Assert.*;

import org.junit.Test;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.TemperatureUnit;
import org.opentrafficsim.core.unit.Unit;
import org.opentrafficsim.core.value.vfloat.scalar.FloatScalar;
import org.opentrafficsim.core.value.vfloat.scalar.FloatScalarAbs;
import org.opentrafficsim.core.value.vfloat.scalar.FloatScalarRel;

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
 * @version Jun 25, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class FloatScalarTest
{
    /**
     * Test creator, verify the various fields in the created objects, test conversions to other units
     */
    @SuppressWarnings("static-method")
    @Test
    public void basics()
    {
        TemperatureUnit tempUnit = TemperatureUnit.DEGREE_CELSIUS;
        float value = 38.0f;
        FloatScalarAbs<TemperatureUnit> temperatureFS = new FloatScalarAbs<TemperatureUnit>(value, tempUnit);
        assertEquals("Unit should be Celsius", tempUnit, temperatureFS.getUnit());
        assertEquals("Value is what we put in", value, temperatureFS.getValueInUnit(), 0.0001);
        assertEquals("Value in SI is equivalent in Kelvin", 311.15f, temperatureFS.getValueSI(), 0.05);
        assertEquals("Value in Fahrenheit", 100.4f, temperatureFS.getValueInUnit(TemperatureUnit.DEGREE_FAHRENHEIT), 0.1);
        FloatScalarAbs<TemperatureUnit> u2 = new FloatScalarAbs<TemperatureUnit>(temperatureFS);
        temperatureFS.setDisplayUnit(TemperatureUnit.DEGREE_FAHRENHEIT);
        assertEquals("Unit should now be Fahrenheit", TemperatureUnit.DEGREE_FAHRENHEIT, temperatureFS.getUnit());
        assertEquals("Value in unit is now the equivalent in Fahrenheit", 100.4f, temperatureFS.getValueInUnit(), 0.05);
        assertEquals("Value in SI is equivalent in Kelvin", 311.15f, temperatureFS.getValueSI(), 0.1);
        assertEquals("Value in Fahrenheit", 100.4f, temperatureFS.getValueInUnit(TemperatureUnit.DEGREE_FAHRENHEIT), 0.1);
        assertTrue("Value is absolute", temperatureFS.isAbsolute());
        assertFalse("Value is absolute", temperatureFS.isRelative());
        assertEquals("Unit of copy made before calling setUnit should be unchanged", tempUnit, u2.getUnit());
        
        LengthUnit lengthUnit = LengthUnit.INCH;
        value = 12f;
        FloatScalarRel<LengthUnit> lengthFS = new FloatScalarRel<LengthUnit>(value, lengthUnit);
        System.out.println("lengthFS is " + lengthFS);
        assertEquals("Unit should be Inch", lengthUnit, lengthFS.getUnit());
        assertEquals("Value is what we put in", value, lengthFS.getValueInUnit(), 0.0001);
        assertEquals("Value in SI is equivalent in Meter", 0.3048f, lengthFS.getValueSI(), 0.0005);
        assertEquals("Value in Foot", 1f, lengthFS.getValueInUnit(LengthUnit.FOOT), 0.0001);
        FloatScalarRel<LengthUnit> copy = new FloatScalarRel<LengthUnit>(lengthFS);
        lengthFS.setDisplayUnit(LengthUnit.MILLIMETER);
        assertEquals("Unit should not be Millimeter", LengthUnit.MILLIMETER, lengthFS.getUnit());
        assertEquals("Unit of copy should still be in Inch", LengthUnit.INCH, copy.getUnit());
    }
}
