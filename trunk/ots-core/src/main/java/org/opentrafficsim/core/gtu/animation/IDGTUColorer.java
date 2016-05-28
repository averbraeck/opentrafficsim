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
    private final ArrayList<LegendEntry> legend;

    /**
     * Construct a new IDGTUColorer.
     */
    public IDGTUColorer()
    {
        this.legend = new ArrayList<LegendEntry>(4);
        this.legend.add(new LegendEntry(Color.BLACK, "black", "black"));
        this.legend.add(new LegendEntry(new Color(0xa5, 0x2a, 0x2a), "brown", "brown"));
        this.legend.add(new LegendEntry(Color.RED, "red", "red"));
        this.legend.add(new LegendEntry(Color.ORANGE, "orange", "orange"));
        this.legend.add(new LegendEntry(Color.YELLOW, "yellow", "yellow"));
        this.legend.add(new LegendEntry(Color.GREEN, "green", "green"));
        this.legend.add(new LegendEntry(Color.BLUE, "blue", "blue"));
        this.legend.add(new LegendEntry(Color.MAGENTA, "magenta", "magenta"));
        this.legend.add(new LegendEntry(Color.GRAY, "gray", "gray"));
        this.legend.add(new LegendEntry(Color.WHITE, "white", "white"));
    }

    /** {@inheritDoc} */
    @Override
    public final Color getColor(final GTU gtu)
    {
        String idString = "" + gtu.getId();
        int firstDigit = idString.length();
        while (firstDigit > 0)
        {
            if (Character.isDigit(idString.charAt(firstDigit - 1)))
            {
                firstDigit--;
            }
            else
            {
                break;
            }
        }
        int idKey;
        if (firstDigit == idString.length())
        {
            idKey = idString.hashCode();
        }
        else
        {
            idKey = Integer.parseInt(idString.substring(firstDigit));
        }
        return this.legend.get(idKey % this.legend.size()).getColor();
    }

    /** {@inheritDoc} */
    @Override
    public final List<LegendEntry> getLegend()
    {
        return this.legend;
    }

    /** {@inheritDoc} */
    public final String toString()
    {
        return "ID";
    }

}
