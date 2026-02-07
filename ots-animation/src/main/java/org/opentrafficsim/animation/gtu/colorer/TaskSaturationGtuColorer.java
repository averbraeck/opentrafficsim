package org.opentrafficsim.animation.gtu.colorer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.draw.ColorInterpolator;
import org.opentrafficsim.draw.colorer.LegendColorer;
import org.opentrafficsim.road.gtu.lane.perception.mental.Fuller;
import org.opentrafficsim.road.gtu.lane.perception.mental.SumFuller;

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
        Optional<Double> ts = gtu.getParameters().getOptionalParameter(Fuller.TS);
        double tsCrit = gtu.getParameters().getOptionalParameter(SumFuller.TS_CRIT).orElse(1.0);
        double tsMax = gtu.getParameters().getOptionalParameter(SumFuller.TS_MAX).orElse(2.0);
        if (ts.isEmpty())
        {
            return NA;
        }
        if (ts.get() < tsCrit)
        {
            return SUBCRIT;
        }
        else if (ts.get() > tsMax)
        {
            return MAX;
        }
        double range = .5 * (tsMax - tsCrit);
        double mid = tsCrit + range;
        if (ts.get() < mid)
        {
            return ColorInterpolator.interpolateColor(SUBCRIT, MID, Math.max(0.0, Math.min(1.0, (ts.get() - tsCrit) / range)));
        }
        return ColorInterpolator.interpolateColor(MAX, MID, Math.max(0.0, Math.min(1.0, (tsMax - ts.get()) / range)));
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
