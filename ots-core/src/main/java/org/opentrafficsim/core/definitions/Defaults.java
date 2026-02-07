package org.opentrafficsim.core.definitions;

import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Optional;

/**
 * This class houses defaults instances for different types, such as GTU types and link types. The static fields should only be
 * accessed in the setup of a simulation. The simulation itself should be fed the relevant types, and not assume any specific or
 * more generic super type. Only in this way can simulations be run with entirely different type structures.
 * <p>
 * Copyright (c) 2022-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public abstract class Defaults
{

    /** Defaults for locale nl_NL. */
    public static final DefaultsNl NL = new DefaultsNl();

    /** Locale. */
    private final Locale locale;

    /**
     * Constructor.
     * @param locale locale.
     */
    protected Defaults(final Locale locale)
    {
        this.locale = locale;
    }

    /**
     * Returns the locale.
     * @return locale.
     */
    public Locale getLocale()
    {
        return this.locale;
    }

    /**
     * Returns a default value of a type, indicated by its name. This should only be used by parsers. Simulations defined in
     * code should access the relevant static fields directly for code maintainability.
     * @param clazz class instance of type T.
     * @param name name referring to a default through static field names, e.g. "NL.VEHICLE".
     * @param <T> type of the value.
     * @return returned default value, empty if the default could not be found.
     */
    public static <T> Optional<T> getByName(final Class<T> clazz, final String name)
    {
        return Optional.ofNullable(getByName(Defaults.class, clazz, name));
    }

    /**
     * Returns a default value of a type, indicated by its name. This should only be used by parsers. Simulations defined in
     * code should access the relevant static fields directly for code maintainability.
     * @param defaultsClass defaults class.
     * @param clazz class instance of type T.
     * @param name name referring to a default through static field names, e.g. "NL.VEHICLE".
     * @param <T> type of the value.
     * @return returned default value, {@code null} if the default could not be found.
     */
    @SuppressWarnings("unchecked")
    protected static <T> T getByName(final Class<? extends Defaults> defaultsClass, final Class<T> clazz, final String name)
    {
        try
        {
            String[] subNames = name.split("\\.");
            Field field1 = defaultsClass.getDeclaredField(subNames[0]);
            Object defaults = field1.get(defaultsClass);
            Field field2 = defaults.getClass().getDeclaredField(subNames[1]);
            return (T) field2.get(defaults.getClass());
        }
        catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException ex)
        {
            return null;
        }
    }

}
