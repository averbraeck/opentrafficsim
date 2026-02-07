package org.opentrafficsim.road.definitions;

import java.util.Locale;
import java.util.Optional;

import org.opentrafficsim.core.definitions.Defaults;

/**
 * This class houses defaults instances for different types, specific for road, such as lane types. The static fields should
 * only be accessed in the setup of a simulation. The simulation itself should be fed the relevant types, and not assume any
 * specific or more generic super type. Only in this way can simulations be run with entirely different type structures.
 * <p>
 * Copyright (c) 2022-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public abstract class DefaultsRoad extends Defaults
{

    /** Defaults for locale nl_NL. */
    public static final DefaultsRoadNl NL = new DefaultsRoadNl();

    /**
     * Constructor.
     * @param locale locale.
     */
    protected DefaultsRoad(final Locale locale)
    {
        super(locale);
    }

    /**
     * Returns a default value of a type, indicated by its name. This should only be used by parsers. Simulations defined in
     * code should access the relevant static fields directly for code maintainability.
     * @param clazz class instance of type T.
     * @param name name referring to a default through static field names, e.g. "NL.FREEWAY".
     * @param <T> type of the value.
     * @return returned default value, empty if the default could not be found.
     */
    public static <T> Optional<T> getByName(final Class<T> clazz, final String name)
    {
        return Optional.ofNullable(getByName(DefaultsRoad.class, clazz, name));
    }

}
