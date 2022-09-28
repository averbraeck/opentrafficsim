package org.opentrafficsim.core.animation.gtu.colorer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.opentrafficsim.core.gtu.Gtu;

/**
 * Color GTUs based on their id. If the id ends on one or more digits, the value that those digits constitute is used.
 * Otherwise, the hash code of the string representation of the id is used.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class IdGtuColorer implements GtuColorer
{
    /** The legend. */
    public static final ArrayList<LegendEntry> LEGEND;

    /**
     * Construct a new IdGtuColorer.
     */
    static
    {
        LEGEND = new ArrayList<LegendEntry>();
        LEGEND.add(new LegendEntry(Color.BLACK, "black", "black"));
        LEGEND.add(new LegendEntry(new Color(0xa5, 0x2a, 0x2a), "brown", "brown"));
        LEGEND.add(new LegendEntry(Color.RED, "red", "red"));
        LEGEND.add(new LegendEntry(Color.ORANGE, "orange", "orange"));
        LEGEND.add(new LegendEntry(Color.YELLOW, "yellow", "yellow"));
        LEGEND.add(new LegendEntry(Color.GREEN, "green", "green"));
        LEGEND.add(new LegendEntry(Color.BLUE, "blue", "blue"));
        LEGEND.add(new LegendEntry(Color.MAGENTA, "magenta", "magenta"));
        LEGEND.add(new LegendEntry(Color.GRAY, "gray", "gray"));
        LEGEND.add(new LegendEntry(Color.WHITE, "white", "white"));
    }

    /** {@inheritDoc} */
    @Override
    public final Color getColor(final Gtu gtu)
    {
        return LEGEND.get(Math.abs(gtu.getId().hashCode() % LEGEND.size())).getColor();
    }

    /** {@inheritDoc} */
    @Override
    public final List<LegendEntry> getLegend()
    {
        return LEGEND;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "ID";
    }

}
