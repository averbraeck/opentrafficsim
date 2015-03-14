package org.opentrafficsim.core.unit;

import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.OTHER;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.SI_ACCEPTED;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.SI_DERIVED;

import org.opentrafficsim.core.unit.unitsystem.UnitSystem;

/**
 * Standard frequency units based on time.
 * <p>
 * Copyright (c) 2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version May 15, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class FrequencyUnit extends Unit<FrequencyUnit>
{
    /** */
    private static final long serialVersionUID = 20140607L;

    /** the actual time unit, e.g. second. */
    private final TimeUnit timeUnit;

    /** The SI unit for frequency is Hertz. */
    public static final FrequencyUnit SI;

    /** Hertz. */
    public static final FrequencyUnit HERTZ;

    /** kiloHertz. */
    public static final FrequencyUnit KILOHERTZ;

    /** megaHertz. */
    public static final FrequencyUnit MEGAHERTZ;

    /** gigaHertz. */
    public static final FrequencyUnit GIGAHERTZ;

    /** teraHertz. */
    public static final FrequencyUnit TERAHERTZ;

    /** Revolutions per minute = 1/60 Hz. */
    public static final FrequencyUnit RPM;

    /** 1/s. */
    public static final FrequencyUnit PER_SECOND;

    /** 1/min. */
    public static final FrequencyUnit PER_MINUTE;

    /** 1/hour. */
    public static final FrequencyUnit PER_HOUR;

    /** 1/day. */
    public static final FrequencyUnit PER_DAY;

    static
    {
        SI = new FrequencyUnit(TimeUnit.SECOND, "FrequencyUnit.Hertz", "FrequencyUnit.Hz", SI_DERIVED);
        HERTZ = SI;
        KILOHERTZ = new FrequencyUnit("FrequencyUnit.kilohertz", "FrequencyUnit.kHz", SI_DERIVED, HERTZ, 1000.0);
        MEGAHERTZ = new FrequencyUnit("FrequencyUnit.megahertz", "FrequencyUnit.MHz", SI_DERIVED, HERTZ, 1.0E6);
        GIGAHERTZ = new FrequencyUnit("FrequencyUnit.gigahertz", "FrequencyUnit.GHz", SI_DERIVED, HERTZ, 1.0E9);
        TERAHERTZ = new FrequencyUnit("FrequencyUnit.terahertz", "FrequencyUnit.THz", SI_DERIVED, HERTZ, 1.0E12);
        RPM = new FrequencyUnit("FrequencyUnit.revolutions_per_minute", "FrequencyUnit.rpm", OTHER, HERTZ, 1.0 / 60.0);
        PER_SECOND = new FrequencyUnit(TimeUnit.SECOND, "FrequencyUnit.per_second", "FrequencyUnit.1/s", SI_DERIVED);
        PER_MINUTE = new FrequencyUnit(TimeUnit.MINUTE, "FrequencyUnit.per_minute", "FrequencyUnit.1/min", SI_ACCEPTED);
        PER_HOUR = new FrequencyUnit(TimeUnit.HOUR, "FrequencyUnit.per_hour", "FrequencyUnit.1/h", SI_ACCEPTED);
        PER_DAY = new FrequencyUnit(TimeUnit.DAY, "FrequencyUnit.per_day", "FrequencyUnit.1/d", SI_ACCEPTED);
    }

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
    public final TimeUnit getTimeUnit()
    {
        return this.timeUnit;
    }

    /** {@inheritDoc} */
    @Override
    public final FrequencyUnit getStandardUnit()
    {
        return HERTZ;
    }

    /** {@inheritDoc} */
    @Override
    public final String getSICoefficientsString()
    {
        return "s-1";
    }

}
