package org.opentrafficsim.road.gtu.colorer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.opentrafficsim.core.animation.ColorInterpolator;
import org.opentrafficsim.core.animation.gtu.colorer.GTUColorer;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.road.gtu.lane.perception.mental.Fuller;

/**
 * Displays task saturation.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 10 apr. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class TaskSaturationColorer implements GTUColorer
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

    /** {@inheritDoc} */
    @Override
    public Color getColor(final GTU gtu)
    {
        Double ts = gtu.getParameters().getParameterOrNull(Fuller.TS);
        Double tsCrit = gtu.getParameters().getParameterOrNull(Fuller.TS_CRIT);
        Double tsMax = gtu.getParameters().getParameterOrNull(Fuller.TS_MAX);
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
            return ColorInterpolator.interpolateColor(SUBCRIT, MID, (ts - tsCrit) / range);
        }
        return ColorInterpolator.interpolateColor(MAX, MID, (tsMax - ts) / range);
    }

    /** {@inheritDoc} */
    @Override
    public List<LegendEntry> getLegend()
    {
        return LEGEND;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "Task saturation";
    }

}
