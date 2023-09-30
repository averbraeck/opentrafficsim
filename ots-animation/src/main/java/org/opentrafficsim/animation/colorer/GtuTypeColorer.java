package org.opentrafficsim.animation.colorer;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.opentrafficsim.animation.gtu.colorer.GtuColorer;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.gtu.GtuType;

/**
 * Color by GTU type, based on the GtuType id or the enum id from the defaults.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class GtuTypeColorer implements GtuColorer, Serializable
{

    /** */
    private static final long serialVersionUID = 20180117L;

    /** The colors per GTU Type. */
    private final Map<String, Color> map = new LinkedHashMap<>();

    /** Index for next default color. */
    private int nextDefault = 0;

    /** Defaults colors. */
    private static Color[] standardColors = new Color[10];
    {
        standardColors[0] = Color.BLACK;
        standardColors[1] = new Color(0xa5, 0x2a, 0x2a);
        standardColors[2] = Color.RED;
        standardColors[3] = Color.ORANGE;
        standardColors[4] = Color.YELLOW;
        standardColors[5] = Color.GREEN;
        standardColors[6] = Color.BLUE;
        standardColors[7] = Color.MAGENTA;
        standardColors[8] = Color.GRAY;
        standardColors[9] = Color.WHITE;
    }

    /**
     * Adds a GTU type to the list with color based on the type.
     * @param gtuType GtuType; GTU type
     * @return this GTUTypeColorer
     */
    public GtuTypeColorer add(final GtuType gtuType)
    {
        this.map.put(gtuType.getId(), standardColors[this.nextDefault]);
        this.nextDefault++;
        if (this.nextDefault == standardColors.length)
        {
            this.nextDefault = 0;
        }
        return this;
    }

    /**
     * Adds a GTU type to the list with given color.
     * @param gtuType GtuType; GTU type
     * @param color Color; color
     * @return this GTUTypeColorer
     */
    public GtuTypeColorer add(final GtuType gtuType, final Color color)
    {
        this.map.put(gtuType.getId(), color);
        return this;
    }

    /**
     * Returns a colorer from a map.
     * @param gtuTypeColors Map&lt;GtuType, Color&gt;; colors per GTU type in the GTU type colorer.
     * @return GtuTypeColorer; based on the map.
     */
    public static GtuTypeColorer fromMap(final Map<GtuType, Color> gtuTypeColors)
    {
        GtuTypeColorer colorer = new GtuTypeColorer();
        for (Entry<GtuType, Color> entry : gtuTypeColors.entrySet())
        {
            colorer.add(entry.getKey(), entry.getValue());
        }
        return colorer;
    }

    /** {@inheritDoc} */
    @Override
    public Color getColor(final Gtu gtu)
    {
        GtuType gtuType = gtu.getType();
        Color color = this.map.get(gtuType.getId());
        while (gtuType != null && color == null)
        {
            gtuType = gtuType.getParent();
            color = this.map.get(gtuType.getId());
        }
        if (color == null)
        {
            return Color.white;
        }
        return color;
    }

    /** {@inheritDoc} */
    @Override
    public List<LegendEntry> getLegend()
    {
        List<LegendEntry> legend = new ArrayList<>();
        for (String name : this.map.keySet())
        {
            String nameCase = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
            legend.add(new LegendEntry(this.map.get(name), nameCase, nameCase));
        }
        return legend;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "GTU Type";
    }

}
