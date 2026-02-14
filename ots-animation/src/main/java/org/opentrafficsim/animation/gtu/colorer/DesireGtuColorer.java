package org.opentrafficsim.animation.gtu.colorer;

import java.awt.Color;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.draw.ColorInterpolator;
import org.opentrafficsim.draw.colorer.AbstractLegendColorer;
import org.opentrafficsim.road.gtu.tactical.util.lmrs.Desire;

/**
 * Super class with default coloring of left and right desire value.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public abstract class DesireGtuColorer extends AbstractLegendColorer<Gtu, Desire>
{

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

    /**
     * Constructor.
     * @param valueFunction value function
     */
    public DesireGtuColorer(final Function<Gtu, Optional<Desire>> valueFunction)
    {
        super(valueFunction, DesireGtuColorer::getColor,
                List.of(new LegendEntry(LEFT, "Left", "Full left: 1.0"),
                        new LegendEntry(NO_LEFT, "Not left", "Full no-left: -1.0"), new LegendEntry(NONE, "None", "None: 0.0"),
                        new LegendEntry(RIGHT, "Right", "Full right: 1.0"),
                        new LegendEntry(NO_RIGHT, "Not right", "Full no-right: -1.0"), new LegendEntry(NA, "N/A", "N/A")));
    }

    /**
     * Returns a color based on desire.
     * @param desire desire
     * @return color based on desire
     */
    private static Color getColor(final Desire desire)
    {
        if (desire == null)
        {
            return NA;
        }
        Color target;
        double f;
        double dLeft = desire.left();
        double dRight = desire.right();
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
