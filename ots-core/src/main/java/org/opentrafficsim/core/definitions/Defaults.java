package org.opentrafficsim.core.definitions;

import java.lang.reflect.Field;
import java.util.Locale;

import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.gtu.TemplateGtuType;

import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * This class houses defaults instances for different types, such as GTU types and lane types. The static fields should only be
 * accessed in the setup of a simulation. The simulation itself should be fed the relevant types, and not assume any specific or
 * more generic super type. Only in this way can simulations be run with entirely different type structures.
 * <p>
 * Copyright (c) 2022-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public abstract class Defaults
{

    /** Defaults for locale nl_NL. */
    public static final DefaultsNl NL = new DefaultsNl();

    /** Locale. */
    private final Locale locale;

    /**
     * Constructor.
     * @param locale Locale; locale.
     */
    protected Defaults(final Locale locale)
    {
        this.locale = locale;
    }

    /**
     * Returns the locale.
     * @return Locale; locale.
     */
    public Locale getLocale()
    {
        return this.locale;
    }

    /**
     * Returns a template for the given GTU type. This can be defined at the level of super types, returning {@code null} for
     * more specific types. There is no need to define a template for all default types defined for a locale, so long as at
     * least one parent of each type has a template defined.<br>
     * <br>
     * Note: implementations should not cache the template per GTU type, as different simulations may request templates for the
     * same GTU type, while having their separate random streams.
     * @param gtuType GtuType; GTU type.
     * @param randomStream StreamInterface; random stream.
     * @return TemplateGtuType; template, {@code null} if no default is defined.
     */
    public abstract TemplateGtuType getTemplate(GtuType gtuType, StreamInterface randomStream);

    /**
     * Returns a default value of a type, indicated by its name. This should only be used by parsers. Simulations defined in
     * code should access the relevant static fields directly for code maintainability.
     * @param clazz Class&lt;T&gtT;; class instance of type T.
     * @param name String; name referring to a default thought static field names, e.g. "NL.VEHICLE".
     * @param <T> type of the value.
     * @return T; returned default value.
     * @throws RuntimeException if the field does not exists, may not be accessed, or is not of type T.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getByName(final Class<T> clazz, final String name) throws RuntimeException
    {
        try
        {
            String[] subNames = name.split("\\.");
            Field field1 = Defaults.class.getDeclaredField(subNames[0]);
            Object defaults = field1.get(Defaults.class);
            Field field2 = defaults.getClass().getDeclaredField(subNames[1]);
            return (T) field2.get(defaults.getClass());
        }
        catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException ex)
        {
            throw new RuntimeException("Default " + name + " could not be loaded.", ex);
        }
    }

}
