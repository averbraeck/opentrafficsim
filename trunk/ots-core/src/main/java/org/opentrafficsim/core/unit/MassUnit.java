package org.opentrafficsim.core.unit;

import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.IMPERIAL;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.MTS;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.SI_ACCEPTED;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.SI_BASE;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.US_CUSTOMARY;

import org.opentrafficsim.core.unit.unitsystem.UnitSystem;

/**
 * Standard mass units. Several conversion factors have been taken from <a
 * href="http://en.wikipedia.org/wiki/Conversion_of_units">http://en.wikipedia.org/wiki/Conversion_of_units</a>.
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
 * @version May 15, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class MassUnit extends Unit<MassUnit>
{
    /** */
    private static final long serialVersionUID = 20140607L;

    /** kilogram */
    public static final MassUnit KILOGRAM = new MassUnit("MassUnit.kilogram", "MassUnit.kg", SI_BASE);

    /** gram */
    public static final MassUnit GRAM = new MassUnit("MassUnit.gram", "MassUnit.g", SI_BASE, KILOGRAM, 0.001);

    /** pound */
    public static final MassUnit POUND = new MassUnit("MassUnit.pound", "MassUnit.lb", IMPERIAL, KILOGRAM, 0.45359237);

    /** pound */
    public static final MassUnit OUNCE = new MassUnit("MassUnit.ounce", "MassUnit.oz", IMPERIAL, POUND, 1.0 / 16.0);

    /** long ton = 2240 lb */
    public static final MassUnit TON_LONG = new MassUnit("MassUnit.long_ton", "MassUnit.long_tn", IMPERIAL, POUND,
            2240.0);

    /** short ton = 2000 lb */
    public static final MassUnit TON_SHORT = new MassUnit("MassUnit.short_ton", "MassUnit.sh_tn", US_CUSTOMARY, POUND,
            2000.0);

    /** metric ton = 1000 kg */
    public static final MassUnit TON_METRIC = new MassUnit("MassUnit.metric_ton", "MassUnit.t", SI_ACCEPTED,
            KILOGRAM, 1000.0);

    /** metric ton = 1000 kg */
    public static final MassUnit TONNE = new MassUnit("MassUnit.tonne_(mts)", "MassUnit.t_(mts)", MTS,
            KILOGRAM, 1000.0);
    
    /** electronvolt via E=mc^2 */
    public static final MassUnit ELECTRONVOLT = new MassUnit("MassUnit.electronvolt", "MassUnit.eV", SI_ACCEPTED,
            KILOGRAM, 1.78266184539E-36);

    /** dalton */
    public static final MassUnit DALTON = new MassUnit("MassUnit.dalton", "MassUnit.Da", SI_ACCEPTED,
            KILOGRAM, 1.6605388628E-27);

    /**
     * Build a standard unit.
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     */
    public MassUnit(final String nameKey, final String abbreviationKey, final UnitSystem unitSystem)
    {
        super(nameKey, abbreviationKey, unitSystem);
    }

    /**
     * Build a unit with a conversion factor to another unit.
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     * @param referenceUnit the unit to convert to
     * @param conversionFactorToReferenceUnit multiply a value in this unit by the factor to convert to the given
     *            reference unit
     */
    public MassUnit(String nameKey, String abbreviationKey, final UnitSystem unitSystem, MassUnit referenceUnit,
            double conversionFactorToReferenceUnit)
    {
        super(nameKey, abbreviationKey, unitSystem, referenceUnit, conversionFactorToReferenceUnit);
    }

    /**
     * @see org.opentrafficsim.core.unit.Unit#getStandardUnit()
     */
    @Override
    public MassUnit getStandardUnit()
    {
        return KILOGRAM;
    }

}
