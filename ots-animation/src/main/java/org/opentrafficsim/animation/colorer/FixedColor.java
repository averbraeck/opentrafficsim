package org.opentrafficsim.animation.colorer;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.opentrafficsim.animation.gtu.colorer.GtuColorer;
import org.opentrafficsim.core.gtu.Gtu;

/**
 * Fixed color colorer.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class FixedColor implements GtuColorer, Serializable
{

    /** */
    private static final long serialVersionUID = 20180117L;

    /** The color. */
    private final Color color;

    /** Color name. */
    private final String name;

    /**
     * Constructor.
     * @param color the color
     * @param name color name
     */
    public FixedColor(final Color color, final String name)
    {
        this.color = color;
        this.name = name;
    }

    /**
     * Constructor.
     * @param color the color
     */
    public FixedColor(final Color color)
    {
        this(color, "Fixed color");
    }

    @Override
    public Color getColor(final Gtu gtu)
    {
        return this.color;
    }

    @Override
    public List<LegendEntry> getLegend()
    {
        return new ArrayList<>();
    }

    @Override
    public String getName()
    {
        return this.name;
    }

}
