package org.opentrafficsim.demo.doc.tutorials;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.draw.colorer.LegendColorer;

/**
 * This class contains code snippets that are used in the documentation. Whenever errors arise in this code, they need to be
 * fixed -and- the code in the documentation needs to be updated.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
@SuppressWarnings("all")
// @docs/08-tutorials/visualization.md#how-to-add-a-gtu-colorer
public class PhoneColorer implements LegendColorer<Gtu>
{
    private final static Color UNKNOWN = Color.WHITE;

    private final static Color NO = Color.GREEN;

    private final static Color YES = Color.RED;

    private final static List<LegendEntry> LEGEND = new ArrayList<>();

    static
    {
        LEGEND.add(new LegendEntry(UNKNOWN, "Unknown", "Unknown whether the driver is on the phone."));
        LEGEND.add(new LegendEntry(NO, "No", "Driver is not on the phone."));
        LEGEND.add(new LegendEntry(YES, "Yes", "Driver is on the phone."));
    }

    @Override
    public List<LegendEntry> getLegend()
    {
        return LEGEND;
    }

    // @docs/08-tutorials/visualization.md#how-to-add-a-gtu-colorer
    @Override
    public Color getColor(final Gtu gtu)
    {
        if (gtu.getTacticalPlanner() instanceof PhonePlanner)
        {
            return ((PhonePlanner) gtu.getTacticalPlanner()).isOnThePhone() ? YES : NO;
        }
        return UNKNOWN;
    }

    // @docs/08-tutorials/visualization.md#how-to-add-a-gtu-colorer
    @Override
    public String getName()
    {
        return "Phone";
    }

    // @docs/08-tutorials/visualization.md#how-to-add-a-gtu-colorer
    public interface PhonePlanner
    {
        public boolean isOnThePhone();
    }
}
