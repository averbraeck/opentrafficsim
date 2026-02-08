package org.opentrafficsim.animation.gtu.colorer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.draw.ColorInterpolator;
import org.opentrafficsim.draw.colorer.LegendColorer;
import org.opentrafficsim.road.gtu.lane.perception.mental.Fuller;

/**
 * Displays task saturation.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class TaskSaturationGtuColorer implements LegendColorer<Gtu>
{

    /** Full. */
    static final Color MAX = Color.RED;

    /** Medium. */
    static final Color MID = Color.YELLOW;

    /** Zero. */
    static final Color LOW = Color.GREEN;

    /** Not available. */
    static final Color NA = Color.WHITE;

    /** Legend. */
    static final List<LegendEntry> LEGEND;

    static
    {
        LEGEND = new ArrayList<>();
        LEGEND.add(new LegendEntry(LOW, "0.5", "sub-critical task saturation"));
        LEGEND.add(new LegendEntry(MID, "1.0", "medium task saturation"));
        LEGEND.add(new LegendEntry(MAX, "2.0", "max task saturation"));
        LEGEND.add(new LegendEntry(NA, "N/A", "N/A"));
    }

    /**
     * Constructor.
     */
    public TaskSaturationGtuColorer()
    {
        //
    }

    @Override
    public Color getColor(final Gtu gtu)
    {
        Optional<Double> ts = gtu.getParameters().getOptionalParameter(Fuller.TS);
        if (ts.isEmpty())
        {
            return NA;
        }
        if (ts.get() < 0.5)
        {
            return LOW;
        }
        else if (ts.get() > 2.0)
        {
            return MAX;
        }
        if (ts.get() < 1.0)
        {
            return ColorInterpolator.interpolateColor(LOW, MID, (ts.get() - 0.5) / 0.5);
        }
        return ColorInterpolator.interpolateColor(MAX, MID, (2.0 - ts.get()) / 1.0);
    }

    @Override
    public List<LegendEntry> getLegend()
    {
        return LEGEND;
    }

    @Override
    public String getName()
    {
        return "Task saturation";
    }

}
