package org.opentrafficsim.core.unit;

import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.SI_ACCEPTED;
import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.SI_DERIVED;

import org.opentrafficsim.core.unit.unitsystem.UnitSystem;

/**
 * The flow rate is the number of units (cars, pedestrians) that pass through a given surface per unit of time.
 * <p>
 * Copyright (c) 2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version May 15, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class FlowUnit extends Unit<FlowUnit>
{
    /** */
    private static final long serialVersionUID = 20140921L;

    /** the unit of time for the flow unit, e.g., second. */
    private final TimeUnit timeUnit;

    /** 1/s. */
    public static final FlowUnit PER_SECOND;

    /** 1/min. */
    public static final FlowUnit PER_MINUTE;

    /** 1/hour. */
    public static final FlowUnit PER_HOUR;

    /** 1/day. */
    public static final FlowUnit PER_DAY;

    static
    {
        PER_SECOND = new FlowUnit(TimeUnit.SECOND, "FlowUnit.per_second", "FlowUnit.1/s", SI_DERIVED);
        PER_MINUTE = new FlowUnit(TimeUnit.MINUTE, "FlowUnit.per_minute", "FlowUnit.1/min", SI_ACCEPTED);
        PER_HOUR = new FlowUnit(TimeUnit.HOUR, "FlowUnit.per_hour", "FlowUnit.1/h", SI_ACCEPTED);
        PER_DAY = new FlowUnit(TimeUnit.DAY, "FlowUnit.per_day", "FlowUnit.1/d", SI_ACCEPTED);
    }

    /**
     * Create a flow unit based on mass and time.
     * @param timeUnit the unit of time for the flow unit, e.g., second
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     */
    public FlowUnit(final TimeUnit timeUnit, final String nameKey, final String abbreviationKey,
            final UnitSystem unitSystem)
    {
        super(nameKey, abbreviationKey, unitSystem, PER_SECOND, 1.0d / timeUnit.getConversionFactorToStandardUnit(),
                true);
        this.timeUnit = timeUnit;
    }

    /**
     * Create a flow unit based on another flow unit.
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     * @param referenceUnit the unit to convert to
     * @param conversionFactorToReferenceUnit multiply a value in this unit by the factor to convert to the given
     *            reference unit
     */
    public FlowUnit(final String nameKey, final String abbreviationKey, final UnitSystem unitSystem,
            final FlowUnit referenceUnit, final double conversionFactorToReferenceUnit)
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
    public final FlowUnit getStandardUnit()
    {
        return PER_SECOND;
    }

    /** {@inheritDoc} */
    @Override
    public final String getSICoefficientsString()
    {
        return "1/s";
    }

}
