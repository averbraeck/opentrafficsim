package org.opentrafficsim.core.animation.gtu.colorer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.draw.core.Colors;

/**
 * Color GTUs based on their id. If the id ends on one or more digits, the value that those digits constitute is used.
 * Otherwise, the hash code of the string representation of the id is used.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
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
        for (int i = 0; i < Colors.COLORS.length; i++)
        {
            LEGEND.add(new LegendEntry(Colors.COLORS[i], Colors.NAMES[i], Colors.NAMES[i]));
        }
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
