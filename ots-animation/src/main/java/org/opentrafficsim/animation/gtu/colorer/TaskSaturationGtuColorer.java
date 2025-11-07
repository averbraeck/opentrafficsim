package org.opentrafficsim.animation.gtu.colorer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.draw.ColorInterpolator;
import org.opentrafficsim.draw.colorer.LegendColorer;
import org.opentrafficsim.road.gtu.lane.perception.mental.Fuller;
import org.opentrafficsim.road.gtu.lane.perception.mental.SumFuller;

/**
 * Displays task saturation.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
    static final Color SUBCRIT = Color.GREEN;

    /** Not available. */
    static final Color NA = Color.WHITE;

    /** Legend. */
    static final List<LegendEntry> LEGEND;

    static
    {
        LEGEND = new ArrayList<>();
        LEGEND.add(new LegendEntry(SUBCRIT, "sub-critical", "sub-critical task saturation"));
        LEGEND.add(new LegendEntry(MID, "medium", "medium task saturation"));
        LEGEND.add(new LegendEntry(MAX, "max", "max task saturation"));
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
        Double ts = gtu.getParameters().getParameterOrNull(Fuller.TS);
        Double tsCrit = gtu.getParameters().getParameterOrNull(SumFuller.TS_CRIT);
        Double tsMax = gtu.getParameters().getParameterOrNull(SumFuller.TS_MAX);
        if (ts == null || tsCrit == null || tsMax == null)
        {
            return NA;
        }
        if (ts < tsCrit)
        {
            return SUBCRIT;
        }
        else if (ts > tsMax)
        {
            return MAX;
        }
        double range = .5 * (tsMax - tsCrit);
        double mid = tsCrit + range;
        if (ts < mid)
        {
            return ColorInterpolator.interpolateColor(SUBCRIT, MID, Math.max(0.0, Math.min(1.0, (ts - tsCrit) / range)));
        }
        return ColorInterpolator.interpolateColor(MAX, MID, Math.max(0.0, Math.min(1.0, (tsMax - ts) / range)));
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
