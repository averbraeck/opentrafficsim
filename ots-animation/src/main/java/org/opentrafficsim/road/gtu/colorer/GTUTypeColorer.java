package org.opentrafficsim.road.gtu.colorer;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.opentrafficsim.core.animation.gtu.colorer.GTUColorer;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.GTUType;

/**
 * Color by GTU type, based on the GTUType id or the enum id from the defaults.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 17 jan. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class GTUTypeColorer implements GTUColorer, Serializable
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

    /** Default instance with colors for common GTUTypes. */
    public static final GTUTypeColorer DEFAULT = new GTUTypeColorer().add(GTUType.DEFAULTS.CAR, Color.BLUE)
            .add(GTUType.DEFAULTS.TRUCK, Color.RED).add(GTUType.DEFAULTS.VEHICLE, Color.GRAY)
            .add(GTUType.DEFAULTS.PEDESTRIAN, Color.YELLOW).add(GTUType.DEFAULTS.BICYCLE, Color.GREEN);

    /**
     * Adds a GTU type to the list with color from the default list.
     * @param gtuTypeEnum GTUType; GTU type
     * @return this GTUTypeColorer
     */
    public GTUTypeColorer add(final GTUType.DEFAULTS gtuTypeEnum)
    {
        this.map.put(gtuTypeEnum.getId(), standardColors[this.nextDefault]);
        this.nextDefault++;
        if (this.nextDefault == standardColors.length)
        {
            this.nextDefault = 0;
        }
        return this;
    }

    /**
     * Adds a GTU type to the list with color based on the type.
     * @param gtuType GTUType; GTU type
     * @return this GTUTypeColorer
     */
    public GTUTypeColorer add(final GTUType gtuType)
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
     * @param gtuType GTUType; GTU type
     * @param color Color; color
     * @return this GTUTypeColorer
     */
    public GTUTypeColorer add(final GTUType gtuType, final Color color)
    {
        this.map.put(gtuType.getId(), color);
        return this;
    }

    /**
     * Adds a GTU type based on its enum to the list with given color.
     * @param gtuTypeEnum GTUType.DEFAULTS; GTU type default enum
     * @param color Color; color
     * @return this GTUTypeColorer
     */
    public GTUTypeColorer add(final GTUType.DEFAULTS gtuTypeEnum, final Color color)
    {
        this.map.put(gtuTypeEnum.getId(), color);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public Color getColor(final GTU gtu)
    {
        GTUType gtuType = gtu.getGTUType();
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
