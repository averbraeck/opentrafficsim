package org.opentrafficsim.road.gtu.animation;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.animation.ColorInterpolator;
import org.opentrafficsim.core.gtu.animation.GTUColorer;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 20 apr. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class DesiredHeadwayColorer implements GTUColorer, Serializable
{

    /** */
    private static final long serialVersionUID = 20170420L;

    /** The legend. */
    private static final List<LegendEntry> LEGEND;

    /** Low color. */
    private static final Color LOW = Color.RED;

    /** Middle color. */
    private static final Color MIDDLE = Color.YELLOW;

    /** High color. */
    private static final Color HIGH = Color.GREEN;

    /** Unknown color. */
    protected static final Color UNKNOWN = Color.WHITE;

    static
    {
        LEGEND = new ArrayList<>(4);
        LEGEND.add(new LegendEntry(LOW, "Tmin", "Tmin"));
        LEGEND.add(new LegendEntry(MIDDLE, "Mean", "Mean"));
        LEGEND.add(new LegendEntry(HIGH, "Tmax", "Tmax"));
        LEGEND.add(new LegendEntry(UNKNOWN, "Unknown", "Unknown"));
    }

    /** {@inheritDoc} */
    @Override
    public final Color getColor(final GTU gtu)
    {
        Parameters params = gtu.getParameters();
        if (!params.contains(ParameterTypes.TMIN) || !params.contains(ParameterTypes.TMAX) || !params.contains(ParameterTypes.T))
        {
            return UNKNOWN;
        }
        try
        {
            double tMin = params.getParameter(ParameterTypes.TMIN).si;
            double tMax = params.getParameter(ParameterTypes.TMAX).si;
            double t = params.getParameter(ParameterTypes.T).si;
            if (t <= tMin)
            {
                return LOW;
            }
            if (t >= tMax)
            {
                return HIGH;
            }
            double tMean = (tMin + tMax) / 2.0;
            if (t < tMean)
            {
                return ColorInterpolator.interpolateColor(LOW, MIDDLE, (t - tMin) / (tMean - tMin));
            }
            return ColorInterpolator.interpolateColor(MIDDLE, HIGH, (t - tMean) / (tMax - tMean));
        }
        catch (ParameterException exception)
        {
            // Should not happen, we check parameters
            throw new RuntimeException("Could not obtain parameter", exception);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final List<LegendEntry> getLegend()
    {
        return LEGEND;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "Desired headway";
    }

}
