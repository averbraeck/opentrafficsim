package org.opentrafficsim.base.logger;

import org.djutils.logger.LogCategory;

/**
 * Predefined categories for Category logging.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public final class Cat
{
    /** */
    private Cat()
    {
        // Utility class
    }

    /** BASE project. */
    public static final LogCategory BASE = new LogCategory("BASE");

    /** CORE project. */
    public static final LogCategory CORE = new LogCategory("CORE");

    /** ROAD project. */
    public static final LogCategory ROAD = new LogCategory("ROAD");

    /** PARSER projects. */
    public static final LogCategory PARSER = new LogCategory("PARSER");

}
