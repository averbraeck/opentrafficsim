package org.opentrafficsim.core.unit;

/**
 * Units for electric charge.
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
 * @param <EC> the electric current unit type
 * @param <T> the time unit type
 */
public class ElectricChargeUnit<EC extends ElectricCurrentUnit, T extends TimeUnit> extends
        Unit<ElectricChargeUnit<EC, T>>
{
    /** */
    private static final long serialVersionUID = 20140603L;

    /** the unit of electric current, e.g., Ampere */
    private final EC electricCurrentUnit;

    /** the unit of time, e.g., second */
    private final T timeUnit;

    /** Coulomb = A.s */
    public static final ElectricChargeUnit<ElectricCurrentUnit, TimeUnit> COULOMB =
            new ElectricChargeUnit<ElectricCurrentUnit, TimeUnit>(ElectricCurrentUnit.AMPERE, TimeUnit.SECOND,
                    "ElectricChargeUnit.coulomb", "ElectricChargeUnit.C");

    /** milliampere hour */
    public static final ElectricChargeUnit<ElectricCurrentUnit, TimeUnit> MILLIAMPERE_HOUR =
            new ElectricChargeUnit<ElectricCurrentUnit, TimeUnit>(ElectricCurrentUnit.MILLIAMPERE, TimeUnit.HOUR,
                    "ElectricChargeUnit.milliampere_hour", "ElectricChargeUnit.mAh");

    /** Faraday */
    public static final ElectricChargeUnit<ElectricCurrentUnit, TimeUnit> FARADAY =
            new ElectricChargeUnit<ElectricCurrentUnit, TimeUnit>("ElectricChargeUnit.faraday", "ElectricChargeUnit.F",
                    COULOMB, 96485.3383);

    /**
     * @param electricCurrentUnit the unit of electric current for the electric charge unit, e.g., meter
     * @param timeUnit the unit of time for the electric charge unit, e.g., second
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     */
    public ElectricChargeUnit(final EC electricCurrentUnit, final T timeUnit, final String nameKey,
            final String abbreviationKey)
    {
        super(nameKey, abbreviationKey, electricCurrentUnit.getConversionFactorToStandardUnit()
                * timeUnit.getConversionFactorToStandardUnit());
        this.electricCurrentUnit = electricCurrentUnit;
        this.timeUnit = timeUnit;
    }

    /**
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param referenceUnit the unit to convert from
     * @param conversionFactorToReferenceUnit multiply by this number to convert from the reference unit
     */
    public ElectricChargeUnit(final String nameKey, final String abbreviationKey,
            final ElectricChargeUnit<EC, T> referenceUnit, final double conversionFactorToReferenceUnit)
    {
        super(nameKey, abbreviationKey, referenceUnit, conversionFactorToReferenceUnit);
        this.electricCurrentUnit = referenceUnit.getElectricCurrentUnit();
        this.timeUnit = referenceUnit.getTimeUnit();
    }

    /**
     * @return electricCurrentUnit
     */
    public EC getElectricCurrentUnit()
    {
        return this.electricCurrentUnit;
    }

    /**
     * @return timeUnit
     */
    public T getTimeUnit()
    {
        return this.timeUnit;
    }

}
