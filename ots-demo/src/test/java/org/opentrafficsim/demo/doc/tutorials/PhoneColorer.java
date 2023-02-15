package org.opentrafficsim.demo.doc.tutorials;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.opentrafficsim.core.animation.gtu.colorer.GtuColorer;
import org.opentrafficsim.core.gtu.Gtu;

// @docs/08-tutorials/visualization.md#how-to-add-a-gtu-colorer
public class PhoneColorer implements GtuColorer
{
    private final static Color UNKNOWN = Color.WHITE;

    private final static Color NO = Color.GREEN;

    private final static Color YES = Color.RED;

    private final static List<LegendEntry> LEGEND = new ArrayList<>();

    static
    {
        LEGEND.add(new LegendEntry(UNKNOWN, "Unknown", "Unkkown whether the driver is on the phone."));
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
    public String toString()
    {
        return "Phone";
    }
    
    // @docs/08-tutorials/visualization.md#how-to-add-a-gtu-colorer
    public interface PhonePlanner
    {
        public boolean isOnThePhone();
    }
}


