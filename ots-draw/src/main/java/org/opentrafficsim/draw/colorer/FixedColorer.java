package org.opentrafficsim.draw.colorer;

import java.awt.Color;

/**
 * Fixed color colorer.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <T> type of object to color
 */
public class FixedColorer<T> implements Colorer<T>
{

    /*
     * The generics argument T is required as opposed to using Colorer<Object> such that the inheritance structure of a
     * sub-class can be consistent.
     */

    /** The color. */
    private final Color color;

    /** Color name. */
    private final String name;

    /**
     * Constructor.
     * @param color the color
     * @param name color name
     */
    public FixedColorer(final Color color, final String name)
    {
        this.color = color;
        this.name = name;
    }

    /**
     * Constructor.
     * @param color the color
     */
    public FixedColorer(final Color color)
    {
        this(color, "Fixed color");
    }

    @Override
    public Color getColor(final T object)
    {
        return this.color;
    }

    @Override
    public String getName()
    {
        return this.name;
    }

}
