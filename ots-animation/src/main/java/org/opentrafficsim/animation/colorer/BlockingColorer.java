package org.opentrafficsim.animation.colorer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.opentrafficsim.animation.gtu.colorer.GtuColorer;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.gtu.plan.tactical.TacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.Blockable;

/**
 * Displays whether a GTU is blocking conflicts.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */

public class BlockingColorer implements GtuColorer
{

    /** Blocking color. */
    private static final Color BLOCKING = Color.RED;

    /** Not blocking color. */
    private static final Color NOT_BLOCKING = Color.WHITE;

    /** Legend. */
    private static final List<LegendEntry> LEGEND = new ArrayList<>();

    static
    {
        LEGEND.add(new LegendEntry(Color.RED, "Not blocking", "Not blocking"));
        LEGEND.add(new LegendEntry(Color.WHITE, "Blocking", "Blocking"));
    }

    /** {@inheritDoc} */
    @Override
    public List<LegendEntry> getLegend()
    {
        return LEGEND;
    }

    /** {@inheritDoc} */
    @Override
    public Color getColor(final Gtu gtu)
    {
        TacticalPlanner<?, ?> tact = gtu.getTacticalPlanner();
        if (tact instanceof Blockable && ((Blockable) tact).isBlocking())
        {
            return BLOCKING;
        }
        return NOT_BLOCKING;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "Blocking";
    }

}
