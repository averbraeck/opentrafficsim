package org.opentrafficsim.core.unit;

import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.IMPERIAL;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.OTHER;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.SI_BASE;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.SI_DERIVED;

import org.opentrafficsim.core.unit.unitsystem.UnitSystem;

/**
 * Temperature units.
 * <p>
 * Copyright (c) 2014 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved.
 * <p>
 * See for project information <a href="http://www.opentrafficsim.org/"> www.opentrafficsim.org</a>.
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
 * @version Jun 5, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class TemperatureUnit extends OffsetUnit<TemperatureUnit>
{
    /** */
    private static final long serialVersionUID = 20140605L;

    /** Kelvin */
    public static final TemperatureUnit KELVIN = new TemperatureUnit("TemperatureUnit.kelvin", "TemperatureUnit.K",
            SI_BASE, 1.0, 0.0);

    /** Degree Celcius */
    public static final TemperatureUnit DEGREE_CELCIUS = new TemperatureUnit("TemperatureUnit.degree_Celcius",
            "TemperatureUnit.dgC", SI_DERIVED, 1.0, -273.15);

    /** Degree Fahrenheit */
    public static final TemperatureUnit DEGREE_FAHRENHEIT = new TemperatureUnit("TemperatureUnit.degree_Fahrenheit",
            "TemperatureUnit.dgF", IMPERIAL, 5.0 / 9.0, -459.67);

    /** Degree Rankine */
    public static final TemperatureUnit DEGREE_RANKINE = new TemperatureUnit("TemperatureUnit.degree_Rankine",
            "TemperatureUnit.dgR", OTHER, 5.0 / 9.0, 0.0);

    /** Degree Reaumur */
    public static final TemperatureUnit DEGREE_REAUMUR = new TemperatureUnit("TemperatureUnit.degree_Reaumur",
            "TemperatureUnit.dgRe", OTHER, 4.0 / 5.0, -273.15);

    /**
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     * @param conversionFactorToStandardUnit multiply by this number to convert to the standard unit
     * @param offsetToKelvin the offsetToKelvin to add to convert to the standard (e.g., SI) unit
     */
    public TemperatureUnit(final String nameKey, final String abbreviationKey, final UnitSystem unitSystem,
            final double conversionFactorToStandardUnit, final double offsetToKelvin)
    {
        super(nameKey, abbreviationKey, unitSystem, KELVIN, conversionFactorToStandardUnit, offsetToKelvin);
    }

    /**
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     * @param referenceUnit the unit to convert to
     * @param conversionFactorToReferenceUnit multiply a value in this unit by the factor to convert to the given
     *            reference unit
     * @param offsetToKelvin the offsetToKelvin to add to convert to the standard (e.g., SI) unit
     */
    public TemperatureUnit(final String nameKey, final String abbreviationKey, final UnitSystem unitSystem,
            final TemperatureUnit referenceUnit, final double conversionFactorToReferenceUnit,
            final double offsetToKelvin)
    {
        super(nameKey, abbreviationKey, unitSystem, referenceUnit, conversionFactorToReferenceUnit, offsetToKelvin);
    }

    /**
     * @see org.opentrafficsim.core.unit.Unit#getStandardUnit()
     */
    @Override
    public TemperatureUnit getStandardUnit()
    {
        return KELVIN;
    }

}
