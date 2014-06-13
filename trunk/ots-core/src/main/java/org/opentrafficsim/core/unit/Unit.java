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
 * All units are internally <u>stored</u> relative to a standard unit with conversion factor. This means that e.g., a
 * meter is stored with conversion factor 1.0, whereas kilometer is stored with a conversion factor 1000.0. This means
 * that if we want to display a meter as kilometers, we have to <u>divide<u> by the conversion factor.
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
 * @param <U> the unit for transformation reasons
 */
public abstract class Unit<U extends Unit<U>> implements Serializable
{
    /** */
    private static final long serialVersionUID = 20140607;

    /** the key to the locale file for the long name of the unit */
    private final String nameKey;

    /** the key to the locale file for the abbreviation of the unit */
    private final String abbreviationKey;

    /** the unit system, e.g. SI or Imperial */
    private final UnitSystem unitSystem;

    /** multiply by this number to convert to the standard (e.g., SI) unit */
    private final double conversionFactorToStandardUnit;

    /** a static map of all defined units */
    private static Map<String, Set<Unit<?>>> UNITS = new HashMap<String, Set<Unit<?>>>();
    
    /** localization information */
    private Localization localization = new Localization("localeunit");
    
    /** has this class been initialized? */
    private static boolean initialized = false;

    /** force all units to be loaded */
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
                // TODO: professional logging of errors
                // exception.printStackTrace();
                System.err.println("Could not load class " + clazz.getCanonicalName());
            }
        }
    }

    /**
     * Build a standard unit.
     * @param nameKey the key to the locale file for the long name of the unit
     * @param abbreviationKey the key to the locale file for the abbreviation of the unit
     * @param unitSystem the unit system, e.g. SI or Imperial
     */
    public Unit(final String nameKey, final String abbreviationKey, final UnitSystem unitSystem)
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
     * @param conversionFactorToReferenceUnit multiply a value in this unit by the factor to convert to the given
     *            reference unit
     */
    public Unit(final String nameKey, final String abbreviationKey, final UnitSystem unitSystem, final U referenceUnit,
            final double conversionFactorToReferenceUnit)
    {
        // as it can happen that this method is called for the standard unit (when it is still null) we have to catch
        // the null pointer for the reference unit here.
        if (referenceUnit == null)
            this.conversionFactorToStandardUnit = 1.0;
        else
            this.conversionFactorToStandardUnit =
                    referenceUnit.getConversionFactorToStandardUnit() * conversionFactorToReferenceUnit;
        this.nameKey = nameKey;
        this.abbreviationKey = abbreviationKey;
        this.unitSystem = unitSystem;
        addUnit(this);
    }

    /**
     * Add a unit to the overview collection of existing units.
     * @param unit the unit to add. It will be stored in a set belonging to the simple class name String, e.g.
     *            "ForceUnit".
     */
    private void addUnit(final Unit<U> unit)
    {
        if (!UNITS.containsKey(unit.getClass().getSimpleName()))
            UNITS.put(unit.getClass().getSimpleName(), new HashSet<Unit<?>>());
        UNITS.get(unit.getClass().getSimpleName()).add(unit);
    }

    /**
     * Return a set of defined units for a given unit type.
     * @param unitClass the class for which the units are requested, e.g. ForceUnit.class
     * @return the set of defined units belonging to the provided class. The empty set will be returned in case the unit
     *         type does not have any units.
     */
    @SuppressWarnings("unchecked")
    public static <V extends Unit<V>> Set<V> getUnits(final Class<V> unitClass)
    {
        if (!initialized)
            initialize();
        Set<V> returnSet = new HashSet<V>();
        if (UNITS.containsKey(unitClass.getSimpleName()))
        {
            for (Unit<?> unit : UNITS.get(unitClass.getSimpleName()))
                returnSet.add((V) unit);
        }
        return returnSet;
    }

    /**
     * Return a copy of the set of all defined units for this unit type.
     * @return the set of defined units belonging to this Unit class. The empty set will be returned in case the unit
     *         type does not have any units.
     */
    @SuppressWarnings("unchecked")
    public Set<Unit<U>> getAllUnitsOfThisType()
    {
        if (!initialized)
            initialize();
        Set<Unit<U>> returnSet = new HashSet<Unit<U>>();
        if (UNITS.containsKey(this.getClass().getSimpleName()))
        {
            for (Unit<?> unit : UNITS.get(this.getClass().getSimpleName()))
                returnSet.add((Unit<U>) unit);
        }
        return returnSet;
    }

    /**
     * @return name, e.g. meters per second
     */
    public String getName()
    {
        return this.localization.getString(this.nameKey);
    }

    /**
     * @return name key, e.g. TimeUnit.MetersPerSecond
     */
    public String getNameKey()
    {
        return this.nameKey;
    }

    /**
     * @return abbreviation, e.g., m/s
     */
    public String getAbbreviation()
    {
        return this.localization.getString(this.abbreviationKey);
    }

    /**
     * @return abbreviation key, e.g. TimeUnit.m/s
     */
    public String getAbbreviationKey()
    {
        return this.abbreviationKey;
    }

    /**
     * @return conversionFactorToStandardUnit. Multiply by this number to convert to the standard (e.g., SI) unit
     */
    public double getConversionFactorToStandardUnit()
    {
        return this.conversionFactorToStandardUnit;
    }

    /**
     * @return unitSystem, e.g. SI or Imperial
     */
    public UnitSystem getUnitSystem()
    {
        return this.unitSystem;
    }

    /**
     * @return the SI standard unit for this unit, or the de facto standard unit if SI is not available
     */
    public abstract U getStandardUnit();

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return getAbbreviation();
    }

}
