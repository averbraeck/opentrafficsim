package org.opentrafficsim.draw.colorer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.opentrafficsim.draw.BoundsPaintScale;

/**
 * Interface of colorers that have a legend (as opposed to, or possibly additional to, a colorbar).
 * <p>
 * Copyright (c) 2024-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <T> type of object to color
 */
public interface LegendColorer<T> extends Colorer<T>
{

    /**
     * Return a list of legend entries (useful to make a legend of the colors used to render the GTUs).
     * @return the list of legend entries; the caller should not (try to) modify this List
     */
    List<LegendEntry> getLegend();

    /**
     * Create legend from a bounds paint scale.
     * @param scale bounds paint scale
     * @param format format string
     * @return legend from bounds paint scale
     */
    static List<LegendEntry> fromBoundsPaintScale(final BoundsPaintScale scale, final String format)
    {
        List<LegendEntry> legend = new ArrayList<>(6);
        for (int i = 0; i < scale.getBounds().length; i++)
        {
            String label = String.format(format, scale.getBounds()[i]);
            legend.add(new LegendEntry(scale.getBoundColors()[i], label, label));
        }
        return legend;
    }

    /**
     * Create legend from a bounds paint scale.
     * @param scale bounds paint scale
     * @param format format string
     * @param notApplicable color indicating the value is not applicable, which is added to he legend
     * @return legend from bounds paint scale
     */
    static List<LegendEntry> fromBoundsPaintScale(final BoundsPaintScale scale, final String format, final Color notApplicable)
    {
        List<LegendEntry> legend = fromBoundsPaintScale(scale, format);
        legend.add(new LegendEntry(notApplicable, "N/A", "N/A"));
        return legend;
    }

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
