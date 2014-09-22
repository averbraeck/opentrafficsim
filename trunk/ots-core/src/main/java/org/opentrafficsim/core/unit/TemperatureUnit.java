package org.opentrafficsim.core.unit;

import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.IMPERIAL;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.OTHER;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.SI_BASE;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.SI_DERIVED;

import org.opentrafficsim.core.unit.unitsystem.UnitSystem;

/**
 * Temperature units.
 * <p>
 * Copyright (c) 2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Jun 5, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class TemperatureUnit extends OffsetUnit<TemperatureUnit>
{
    /** */
    private static final long serialVersionUID = 20140605L;

    /** Kelvin. */
    public static final TemperatureUnit KELVIN;

    /** Degree Celsius. */
    public static final TemperatureUnit DEGREE_CELSIUS;

    /** Degree Fahrenheit. */
    public static final TemperatureUnit DEGREE_FAHRENHEIT;

    /** Degree Rankine. */
    public static final TemperatureUnit DEGREE_RANKINE;

    /** Degree Reaumur. */
    public static final TemperatureUnit DEGREE_REAUMUR;

    static
    {
        KELVIN = new TemperatureUnit("TemperatureUnit.kelvin", "TemperatureUnit.K", SI_BASE, 1.0, 0.0);
        DEGREE_CELSIUS =
                new TemperatureUnit("TemperatureUnit.degree_Celsius", "TemperatureUnit.dgC", SI_DERIVED, 1.0, -273.15);
        DEGREE_FAHRENHEIT =
                new TemperatureUnit("TemperatureUnit.degree_Fahrenheit", "TemperatureUnit.dgF", IMPERIAL, 5.0 / 9.0,
                        -459.67);
        DEGREE_RANKINE =
                new TemperatureUnit("TemperatureUnit.degree_Rankine", "TemperatureUnit.dgR", OTHER, 5.0 / 9.0, 0.0);
        DEGREE_REAUMUR =
                new TemperatureUnit("TemperatureUnit.degree_Reaumur", "TemperatureUnit.dgRe", OTHER, 4.0 / 5.0, -273.15);
    }

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

    /** {@inheritDoc} */
    @Override
    public final TemperatureUnit getStandardUnit()
    {
        return KELVIN;
    }

    /** {@inheritDoc} */
    @Override
    public final String getSICoefficientsString()
    {
        return "K";
    }

}
