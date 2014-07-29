package org.opentrafficsim.core.unit;

import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.OTHER;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.SI_DERIVED;

import org.opentrafficsim.core.unit.unitsystem.UnitSystem;

/**
 * Standard frequency units based on time.
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
public class FrequencyUnit extends Unit<FrequencyUnit>
{
    /** */
    private static final long serialVersionUID = 20140607L;

    /** the actual time unit, e.g. second. */
    private final TimeUnit timeUnit;

    /** hertz. */
    public static final FrequencyUnit HERTZ = new FrequencyUnit(TimeUnit.SECOND, "FrequencyUnit.Hertz",
            "FrequencyUnit.Hz", SI_DERIVED);

    /** kilohertz. */
    public static final FrequencyUnit KILOHERTZ = new FrequencyUnit("FrequencyUnit.kilohertz", "FrequencyUnit.kHz",
            SI_DERIVED, HERTZ, 1000.0);

    /** megahertz. */
    public static final FrequencyUnit MEGAHERTZ = new FrequencyUnit("FrequencyUnit.megahertz", "FrequencyUnit.MHz",
            SI_DERIVED, HERTZ, 1.0E6);

    /** gigahertz. */
    public static final FrequencyUnit GIGAHERTZ = new FrequencyUnit("FrequencyUnit.gigahertz", "FrequencyUnit.GHz",
            SI_DERIVED, HERTZ, 1.0E9);

    /** terahertz. */
    public static final FrequencyUnit TERAHERTZ = new FrequencyUnit("FrequencyUnit.terahertz", "FrequencyUnit.THz",
            SI_DERIVED, HERTZ, 1.0E12);

    /** revolutions per minute = 1/60 Hz. */
    public static final FrequencyUnit RPM = new FrequencyUnit("FrequencyUnit.revolutions_per_minute",
            "FrequencyUnit.rpm", OTHER, HERTZ, 1.0 / 60.0);

    /**
     * Define frequency unit based on time. You can define unit like "per second" (Hertz) here.
     * @param timeUnit the unit of time for the frequency unit, e.g., second
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     */
    public FrequencyUnit(final TimeUnit timeUnit, final String nameKey, final String abbreviationKey,
            final UnitSystem unitSystem)
    {
        super(nameKey, abbreviationKey, unitSystem, HERTZ, 1.0 / timeUnit.getConversionFactorToStandardUnit(), true);
        this.timeUnit = timeUnit;
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
    public FrequencyUnit(final String nameKey, final String abbreviationKey, final UnitSystem unitSystem,
            final FrequencyUnit referenceUnit, final double conversionFactorToReferenceUnit)
    {
        super(nameKey, abbreviationKey, unitSystem, referenceUnit, conversionFactorToReferenceUnit, true);
        this.timeUnit = referenceUnit.getTimeUnit();
    }

    /**
     * @return timeUnit
     */
    public TimeUnit getTimeUnit()
    {
        return this.timeUnit;
    }

    /**
     * @see org.opentrafficsim.core.unit.Unit#getStandardUnit()
     */
    @Override
    public FrequencyUnit getStandardUnit()
    {
        return HERTZ;
    }

    /**
     * @see org.opentrafficsim.core.unit.Unit#getSICoefficientsString()
     */
    @Override
    public String getSICoefficientsString()
    {
        return "s-1";
    }

}