package org.opentrafficsim.base.logger;

import org.djutils.logger.LogCategory;

/**
 * Predefined categories for Category logging. <br>
 * <br>
 * Copyright (c) 2003-2019 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
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
