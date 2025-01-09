package org.opentrafficsim.animation.gtu.colorer;

import java.awt.Color;
import java.util.List;

import org.opentrafficsim.animation.Colorer;
import org.opentrafficsim.core.gtu.Gtu;

/**
 * Determine the fill color for a GTU.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface GtuColorer extends Colorer<Gtu>
{
    /**
     * Return a list of legend entries (useful to make a legend of the colors used to render the GTUs).
     * @return the list of legend entries; the caller should not (try to) modify this List
     */
    List<LegendEntry> getLegend();

    /**
     * Packs a Color, a short description and a long description in one object.
     * @param color the color of the new LegendEntry
     * @param name the name of the new LegendEntry (should be terse)
     * @param description description of the new LegendEntry (may use HTML)
     */
    record LegendEntry(Color color, String name, String description)
    {
    }
}
