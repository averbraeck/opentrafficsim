package org.opentrafficsim.core.unit;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.opentrafficsim.core.locale.Localization;
import org.opentrafficsim.core.unit.unitsystem.UnitSystem;
import org.reflections.Reflections;

/**
 * All units are internally <i>stored</i> relative to a standard unit with conversion factor. This means that e.g., a meter is
 * stored with conversion factor 1.0, whereas kilometer is stored with a conversion factor 1000.0. This means that if we want to
 * display a meter as kilometers, we have to <i>divide</i> by the conversion factor.
 * <p>
 * Copyright (c) 2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version May 15, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @param <U> the unit for transformation reasons
 */
public abstract class Unit<U extends Unit<U>> implements Serializable
{
    /** */
    private static final long serialVersionUID = 20140607;

    /** the key to the locale file for the long name of the unit. */
    private final String nameKey;

    /** the key to the locale file for the abbreviation of the unit. */
    private final String abbreviationKey;

    /** the unit system, e.g. SI or Imperial. */
    private final UnitSystem unitSystem;

    /** multiply by this number to convert to the standard (e.g., SI) unit. */
    private final double conversionFactorToStandardUnit;

    /** SI unit information. */
    private SICoefficients siCoefficients;

    /** static map of all defined coefficient strings, to avoid double creation and allow lookup. */
    private static final Map<String, SICoefficients> SI_COEFFICIENTS = new HashMap<String, SICoefficients>();

    /** static map of all defined coefficient strings, mapped to the existing units. */
    private static final Map<String, Map<Class<Unit<?>>, Unit<?>>> SI_UNITS =
        new HashMap<String, Map<Class<Unit<?>>, Unit<?>>>();

    /** a static map of all defined units. */
    private static final Map<String, Set<Unit<?>>> UNITS = new HashMap<String, Set<Unit<?>>>();

    /** localization information. */
    private static Localization localization = new Localization("localeunit");

    /** has this class been initialized? */
    private static boolean initialized = false;

    /** force all units to be loaded. */
    private static void initialize()
    {
        Reflections reflections = new Reflections("org.opentrafficsim.core.unit");
        @SuppressWarnings("rawtypes")
        Set<Class<? extends Unit>> classes = reflections.getSubTypesOf(Unit.class);

        for (@SuppressWarnings("rawtypes")
        Class<? extends Unit> clazz : classes)
        {
            try
            {
                Class.forName(clazz.getCanonicalName());
            }
            catch (Exception exception)
            {
                // TODO professional logging of errors
                // exception.printStackTrace();
                System.err.println("Could not load class " + clazz.getCanonicalName());
            }
        }
        initialized = true;
    }

    /**
     * Build a standard unit.
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     * @throws UnitException when unit cannot be added to the list of units
     */
    public Unit(final String nameKey, final String abbreviationKey, final UnitSystem unitSystem) throws UnitException
    {
        this.conversionFactorToStandardUnit = 1.0;
        this.nameKey = nameKey;
        this.abbreviationKey = abbreviationKey;
        this.unitSystem = unitSystem;
        addUnit(this);
    }

    /**
     * Build a unit with a conversion factor to another unit.
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     * @param referenceUnit the unit to convert to
     * @param conversionFactorToReferenceUnit multiply a value in this unit by the factor to convert to the given reference unit
     * @throws UnitException when unit cannot be added to the list of units
     */
    public Unit(final String nameKey, final String abbreviationKey, final UnitSystem unitSystem, final U referenceUnit,
        final double conversionFactorToReferenceUnit) throws UnitException
    {
        // as it can happen that this method is called for the standard unit (when it is still null) we have to catch
        // the null pointer for the reference unit here.
        if (referenceUnit == null)
        {
            this.conversionFactorToStandardUnit = 1.0;
        }
        else
        {
            this.conversionFactorToStandardUnit =
                referenceUnit.getConversionFactorToStandardUnit() * conversionFactorToReferenceUnit;
        }
        this.nameKey = nameKey;
        this.abbreviationKey = abbreviationKey;
        this.unitSystem = unitSystem;
        addUnit(this);
    }

    /**
     * Build a standard unit.
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     * @param safe Boolean; if true, a UnitException is silently ignored; if false a UnitException is an Error
     */
    public Unit(final String nameKey, final String abbreviationKey, final UnitSystem unitSystem, final boolean safe)
    {
        this.conversionFactorToStandardUnit = 1.0;
        this.nameKey = nameKey;
        this.abbreviationKey = abbreviationKey;
        this.unitSystem = unitSystem;
        try
        {
            addUnit(this);
        }
        catch (UnitException ue)
        {
            if (!safe)
            {
                throw new Error(ue);
            }
            // TODO complain wherever we can
        }
    }

    /**
     * Build a unit with a conversion factor to another unit.
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     * @param referenceUnit the unit to convert to
     * @param conversionFactorToReferenceUnit multiply a value in this unit by the factor to convert to the given reference unit
     * @param safe Boolean; if true, a UnitException is silently ignored; if false a UnitException is an Error
     */
    public Unit(final String nameKey, final String abbreviationKey, final UnitSystem unitSystem, final U referenceUnit,
        final double conversionFactorToReferenceUnit, final boolean safe)
    {
        // as it can happen that this method is called for the standard unit (when it is still null) we have to catch
        // the null pointer for the reference unit here.
        if (referenceUnit == null)
        {
            this.conversionFactorToStandardUnit = 1.0;
        }
        else
        {
            this.conversionFactorToStandardUnit =
                referenceUnit.getConversionFactorToStandardUnit() * conversionFactorToReferenceUnit;
        }
        this.nameKey = nameKey;
        this.abbreviationKey = abbreviationKey;
        this.unitSystem = unitSystem;
        try
        {
            addUnit(this);
        }
        catch (UnitException ue)
        {
            if (!safe)
            {
                throw new Error(ue);
            }
            // TODO complain wherever we can
        }
    }

    /**
     * Add a unit to the overview collection of existing units, and resolve the coefficients.
     * @param unit the unit to add. It will be stored in a set belonging to the simple class name String, e.g. "ForceUnit".
     * @throws UnitException when parsing or normalizing the SI coefficient string fails.
     */
    private void addUnit(final Unit<U> unit) throws UnitException
    {
        if (!UNITS.containsKey(unit.getClass().getSimpleName()))
        {
            UNITS.put(unit.getClass().getSimpleName(), new HashSet<Unit<?>>());
        }
        UNITS.get(unit.getClass().getSimpleName()).add(unit);

        // resolve the SI coefficients, and normalize string
        String siCoefficientsString = SICoefficients.normalize(getSICoefficientsString()).toString();
        if (SI_COEFFICIENTS.containsKey(siCoefficientsString))
        {
            this.siCoefficients = SI_COEFFICIENTS.get(siCoefficientsString);
        }
        else
        {
            this.siCoefficients = new SICoefficients(SICoefficients.parse(siCoefficientsString));
            SI_COEFFICIENTS.put(siCoefficientsString, this.siCoefficients);
        }

        // add the standard unit
        Map<Class<Unit<?>>, Unit<?>> unitMap = SI_UNITS.get(siCoefficientsString);
        if (unitMap == null)
        {
            unitMap = new HashMap<Class<Unit<?>>, Unit<?>>();
            SI_UNITS.put(siCoefficientsString, unitMap);
        }
        if (!unitMap.containsKey(unit.getClass()))
        {
            @SuppressWarnings("unchecked")
            Class<Unit<?>> clazz = (Class<Unit<?>>) unit.getClass();
            if (this.getStandardUnit() == null)
            {
                unitMap.put(clazz, this);
            }
            else
            {
                unitMap.put(clazz, this.getStandardUnit());
            }
        }
    }

    /**
     * Return a set of defined units for a given unit type.
     * @param <V> the unit type to use in this method.
     * @param unitClass the class for which the units are requested, e.g. ForceUnit.class
     * @return the set of defined units belonging to the provided class. The empty set will be returned in case the unit type
     *         does not have any units.
     */
    @SuppressWarnings("unchecked")
    public static <V extends Unit<V>> Set<V> getUnits(final Class<V> unitClass)
    {
        if (!initialized)
        {
            initialize();
        }
        Set<V> returnSet = new HashSet<V>();
        if (UNITS.containsKey(unitClass.getSimpleName()))
        {
            for (Unit<?> unit : UNITS.get(unitClass.getSimpleName()))
            {
                returnSet.add((V) unit);
            }
        }
        return returnSet;
    }

    /**
     * Return a copy of the set of all defined units for this unit type.
     * @return the set of defined units belonging to this Unit class. The empty set will be returned in case the unit type does
     *         not have any units.
     */
    // TODO call static method from the instance method? The two are now too similar.
    @SuppressWarnings("unchecked")
    public final Set<Unit<U>> getAllUnitsOfThisType()
    {
        if (!initialized)
        {
            initialize();
        }
        Set<Unit<U>> returnSet = new HashSet<Unit<U>>();
        if (UNITS.containsKey(this.getClass().getSimpleName()))
        {
            for (Unit<?> unit : UNITS.get(this.getClass().getSimpleName()))
            {
                returnSet.add((Unit<U>) unit);
            }
        }
        return returnSet;
    }

    /**
     * @return name, e.g. meters per second
     */
    public final String getName()
    {
        return localization.getString(this.nameKey);
    }

    /**
     * @return name key, e.g. TimeUnit.MetersPerSecond
     */
    public final String getNameKey()
    {
        return this.nameKey;
    }

    /**
     * @return abbreviation, e.g., m/s
     */
    public final String getAbbreviation()
    {
        return localization.getString(this.abbreviationKey);
    }

    /**
     * @return abbreviation key, e.g. TimeUnit.m/s
     */
    public final String getAbbreviationKey()
    {
        return this.abbreviationKey;
    }

    /**
     * @return conversionFactorToStandardUnit. Multiply by this number to convert to the standard (e.g., SI) unit
     */
    public final double getConversionFactorToStandardUnit()
    {
        return this.conversionFactorToStandardUnit;
    }

    /**
     * @return unitSystem, e.g. SI or Imperial
     */
    public final UnitSystem getUnitSystem()
    {
        return this.unitSystem;
    }

    /**
     * @return the SI standard unit for this unit, or the de facto standard unit if SI is not available
     */
    public abstract U getStandardUnit();

    /**
     * @return the SI standard coefficients for this unit, e.g., kgm/s2 or m-2s2A or m^-2.s^2.A or m^-2s^2A (not necessarily
     *         normalized)
     */
    public abstract String getSICoefficientsString();

    /**
     * @return the SI coefficients
     */
    public final SICoefficients getSICoefficients()
    {
        return this.siCoefficients;
    }

    /**
     * @param normalizedSICoefficientsString the normalized string (e.g., kg.m/s2) to look up
     * @return a set with the Units belonging to this string, or an empty set when it does not exist
     */
    public static Set<Unit<?>> lookupUnitWithSICoefficients(final String normalizedSICoefficientsString)
    {
        if (!initialized)
        {
            initialize();
        }
        if (SI_UNITS.containsKey(normalizedSICoefficientsString))
        {
            return new HashSet<Unit<?>>(SI_UNITS.get(normalizedSICoefficientsString).values());
        }
        return new HashSet<Unit<?>>();
    }

    /**
     * @param normalizedSICoefficientsString the normalized string (e.g., kg.m/s2) to look up
     * @return a set of Units belonging to this string, or a set with a new unit when it does not yet exist
     */
    // TODO call other static method? The two are now too similar.
    public static Set<Unit<?>> lookupOrCreateUnitWithSICoefficients(final String normalizedSICoefficientsString)
    {
        if (!initialized)
        {
            initialize();
        }
        if (SI_UNITS.containsKey(normalizedSICoefficientsString))
        {
            return new HashSet<Unit<?>>(SI_UNITS.get(normalizedSICoefficientsString).values());
        }
        SIUnit unit = new SIUnit("SIUnit." + normalizedSICoefficientsString);
        Set<Unit<?>> unitSet = new HashSet<Unit<?>>();
        unitSet.add(unit);
        return unitSet;
    }

    /**
     * @param normalizedSICoefficientsString the normalized string (e.g., kg.m/s2) to look up
     * @return the Unit belonging to this string, or a new unit when it does not yet exist
     */
    public static SIUnit lookupOrCreateSIUnitWithSICoefficients(final String normalizedSICoefficientsString)
    {
        if (!initialized)
        {
            initialize();
        }
        if (SI_UNITS.containsKey(normalizedSICoefficientsString)
            && SI_UNITS.get(normalizedSICoefficientsString).containsKey(SIUnit.class))
        {
            return (SIUnit) SI_UNITS.get(normalizedSICoefficientsString).get(SIUnit.class);
        }
        SIUnit unit = new SIUnit("SIUnit." + normalizedSICoefficientsString);
        return unit;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return getAbbreviation();
    }

}
