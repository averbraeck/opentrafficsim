package org.opentrafficsim.road.gtu.colorer;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.opentrafficsim.core.animation.gtu.colorer.GTUColorer;
import org.opentrafficsim.core.gtu.GTU;

/**
 * Fixed color colorer.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 17 jan. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class FixedColor implements GTUColorer, Serializable
{

    /** */
    private static final long serialVersionUID = 20180117L;

    /** The color. */
    private final Color color;

    /** Color name. */
    private final String name;

    /**
     * Constructor.
     * @param color Color; the color
     * @param name String; color name
     */
    public FixedColor(final Color color, final String name)
    {
        this.color = color;
        this.name = name;
    }

    /**
     * Constructor.
     * @param color Color; the color
     */
    public FixedColor(final Color color)
    {
        this(color, "Fixed color");
    }

    /** {@inheritDoc} */
    @Override
    public Color getColor(final GTU gtu)
    {
        return this.color;
    }

    /** {@inheritDoc} */
    @Override
    public List<LegendEntry> getLegend()
    {
        return new ArrayList<>();
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return this.name;
    }

}
