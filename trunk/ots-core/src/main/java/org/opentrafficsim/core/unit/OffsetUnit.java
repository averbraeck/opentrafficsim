package org.opentrafficsim.core.unit;

import org.opentrafficsim.core.unit.unitsystem.UnitSystem;

/**
 * The OffsetUnit provides a unit where scales can have an offset, such as the temperature scale. Internally, all units are
 * internally <i>stored</i> as a standard unit with an offset and a conversion factor. This means that e.g., Kelvin is stored
 * with offset 0.0 and conversion factor 1.0, whereas degree Celsius is stored with offset -273.15 and conversion factor 1.0.
 * This means that if we have a Temperature, it is stored in Kelvins, and if we want to display it in degree Celsius, we have to
 * <i>divide</i> by the conversion factor and <i>subtract</i> the offset.
 * <p>
 * Copyright (c) 2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$, initial versionJun 5, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @param <U> the unit type
 */
public abstract class OffsetUnit<U extends Unit<U>> extends Unit<U>
{
    /** */
    private static final long serialVersionUID = 20140607L;

    /** the offset that has to be taken into account for conversions. */
    private final double offsetToStandardUnit;

    /**
     * Build a standard unit.
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     */
    public OffsetUnit(final String nameKey, final String abbreviationKey, final UnitSystem unitSystem)
    {
        super(nameKey, abbreviationKey, unitSystem, true);
        this.offsetToStandardUnit = 0.0;
    }

    /**
     * Build an offset unit with a conversion factor and offset to another unit.
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     * @param referenceUnit the unit to convert to
     * @param conversionFactorToReferenceUnit multiply a value in this unit by the factor to convert to the given reference unit
     * @param offsetToStandardUnit the offset to add to convert to the standard (e.g., SI) unit
     */
    public OffsetUnit(final String nameKey, final String abbreviationKey, final UnitSystem unitSystem,
        final U referenceUnit, final double conversionFactorToReferenceUnit, final double offsetToStandardUnit)
    {
        super(nameKey, abbreviationKey, unitSystem, referenceUnit, conversionFactorToReferenceUnit, true);
        this.offsetToStandardUnit = offsetToStandardUnit;
    }

    /**
     * @return offset to the standard unit. E.g., -273.15 to go from degrees Celsius to Kelvin
     */
    public final double getOffsetToStandardUnit()
    {
        return this.offsetToStandardUnit;
    }

}
