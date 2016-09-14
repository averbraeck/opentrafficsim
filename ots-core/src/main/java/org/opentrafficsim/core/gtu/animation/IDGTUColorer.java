package org.opentrafficsim.core.gtu.animation;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.opentrafficsim.core.gtu.GTU;

/**
 * Color GTUs based on their id. If the id ends on one or more digits, the value that those digits constitute is used.
 * Otherwise, the hash code of the string representation of the id is used.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$,
 *          initial version 28 mei 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class IDGTUColorer implements GTUColorer, Serializable
{
    /** */
    private static final long serialVersionUID = 20150000L;

    /** The legend. */
    public static final ArrayList<LegendEntry> LEGEND;

    /**
     * Construct a new IDGTUColorer.
     */
    static
    {
        LEGEND = new ArrayList<LegendEntry>(4);
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
    public final Color getColor(final GTU gtu)
    {
        return gtu.getBaseColor();
    }

    /** {@inheritDoc} */
    @Override
    public final List<LegendEntry> getLegend()
    {
        return LEGEND;
    }

    /** {@inheritDoc} */
    public final String toString()
    {
        return "ID";
    }

}
