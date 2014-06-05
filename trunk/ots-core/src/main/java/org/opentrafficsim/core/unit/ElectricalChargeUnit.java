package org.opentrafficsim.core.unit;

/**
 * Units for electrical charge.
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
 * @param <EC> the electrical current unit type
 * @param <T> the time unit type
 */
public class ElectricalChargeUnit<EC extends ElectricalCurrentUnit, T extends TimeUnit> extends
        Unit<ElectricalChargeUnit<EC, T>>
{
    /** */
    private static final long serialVersionUID = 20140603L;

    /** the unit of electrical current, e.g., Ampere */
    private final EC electricalCurrentUnit;

    /** the unit of time, e.g., second */
    private final T timeUnit;

    /** Coulomb = A.s */
    public static final ElectricalChargeUnit<ElectricalCurrentUnit, TimeUnit> COULOMB =
            new ElectricalChargeUnit<ElectricalCurrentUnit, TimeUnit>(ElectricalCurrentUnit.AMPERE, TimeUnit.SECOND,
                    "ElectricalChargeUnit.coulomb", "ElectricalChargeUnit.C");

    /** milliampere hour */
    public static final ElectricalChargeUnit<ElectricalCurrentUnit, TimeUnit> MILLIAMPERE_HOUR =
            new ElectricalChargeUnit<ElectricalCurrentUnit, TimeUnit>(ElectricalCurrentUnit.MILLIAMPERE, TimeUnit.HOUR,
                    "ElectricalChargeUnit.milliampere_hour", "ElectricalChargeUnit.mAh");

    /** Faraday */
    public static final ElectricalChargeUnit<ElectricalCurrentUnit, TimeUnit> FARADAY =
            new ElectricalChargeUnit<ElectricalCurrentUnit, TimeUnit>("ElectricalChargeUnit.faraday", "ElectricalChargeUnit.F",
                    COULOMB, 96485.3383);
    
    /** atomic unit of charge */
    public static final ElectricalChargeUnit<ElectricalCurrentUnit, TimeUnit> ATOMIC_UNIT =
            new ElectricalChargeUnit<ElectricalCurrentUnit, TimeUnit>("ElectricalChargeUnit.atomic_unit_of_charge",
                    "ElectricalChargeUnit.au",
                    COULOMB, 1.602176462E-19);

    /**
     * @param electricalCurrentUnit the unit of electrical current for the electrical charge unit, e.g., meter
     * @param timeUnit the unit of time for the electrical charge unit, e.g., second
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     */
    public ElectricalChargeUnit(final EC electricalCurrentUnit, final T timeUnit, final String nameKey,
            final String abbreviationKey)
    {
        super(nameKey, abbreviationKey, electricalCurrentUnit.getConversionFactorToStandardUnit()
                * timeUnit.getConversionFactorToStandardUnit());
        this.electricalCurrentUnit = electricalCurrentUnit;
        this.timeUnit = timeUnit;
    }

    /**
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param referenceUnit the unit to convert to
     * @param conversionFactorToReferenceUnit multiply a value in this unit by the factor to convert to the given
     *            reference unit
     */
    public ElectricalChargeUnit(final String nameKey, final String abbreviationKey,
            final ElectricalChargeUnit<EC, T> referenceUnit, final double conversionFactorToReferenceUnit)
    {
        super(nameKey, abbreviationKey, referenceUnit, conversionFactorToReferenceUnit);
        this.electricalCurrentUnit = referenceUnit.getElectricalCurrentUnit();
        this.timeUnit = referenceUnit.getTimeUnit();
    }

    /**
     * @return electricalCurrentUnit
     */
    public EC getElectricalCurrentUnit()
    {
        return this.electricalCurrentUnit;
    }

    /**
     * @return timeUnit
     */
    public T getTimeUnit()
    {
        return this.timeUnit;
    }

}
