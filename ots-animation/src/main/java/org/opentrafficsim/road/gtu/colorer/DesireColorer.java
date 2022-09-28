package org.opentrafficsim.road.gtu.colorer;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.opentrafficsim.core.animation.ColorInterpolator;
import org.opentrafficsim.core.animation.gtu.colorer.GTUColorer;

/**
 * Super class with default coloring of left and right desire value.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public abstract class DesireColorer implements GTUColorer, Serializable
{

    /** */
    private static final long serialVersionUID = 20171304L;

    /** The legend. */
    private static final List<LegendEntry> LEGEND;

    /** Left color. */
    private static final Color LEFT = Color.RED;

    /** No-left color. */
    private static final Color NO_LEFT = Color.MAGENTA;

    /** Right color. */
    private static final Color RIGHT = Color.BLUE;

    /** No-right color. */
    private static final Color NO_RIGHT = Color.CYAN;

    /** None color. */
    private static final Color NONE = Color.WHITE;

    /** N/A color. */
    protected static final Color NA = Color.YELLOW;

    static
    {
        LEGEND = new ArrayList<>(6);
        LEGEND.add(new LegendEntry(LEFT, "Left", "Full left: 1.0"));
        LEGEND.add(new LegendEntry(NO_LEFT, "Not left", "Full no-left: -1.0"));
        LEGEND.add(new LegendEntry(NONE, "None", "None: 0.0"));
        LEGEND.add(new LegendEntry(RIGHT, "Right", "Full right: 1.0"));
        LEGEND.add(new LegendEntry(NO_RIGHT, "Not right", "Full no-right: -1.0"));
        LEGEND.add(new LegendEntry(NA, "N/A", "N/A"));
    }

    /** {@inheritDoc} */
    @Override
    public final List<LegendEntry> getLegend()
    {
        return LEGEND;
    }

    /**
     * Returns a color based on desire.
     * @param dLeft double; left desire
     * @param dRight double; right desire
     * @return color based on desire
     */
    protected final Color getColor(final double dLeft, final double dRight)
    {
        Color target;
        double f;
        if (Math.abs(dLeft) >= Math.abs(dRight))
        {
            if (dLeft < 0)
            {
                target = NO_LEFT;
                f = dLeft < -1.0 ? 1.0 : -dLeft;
            }
            else
            {
                target = LEFT;
                f = dLeft > 1.0 ? 1.0 : dLeft;
            }
        }
        else
        {
            if (dRight < 0)
            {
                target = NO_RIGHT;
                f = dRight < -1.0 ? 1.0 : -dRight;
            }
            else
            {
                target = RIGHT;
                f = dRight > 1.0 ? 1.0 : dRight;
            }
        }
        return ColorInterpolator.interpolateColor(NONE, target, f);
    }

}
