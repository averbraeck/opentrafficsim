package org.opentrafficsim.animation.colorer;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.opentrafficsim.animation.gtu.colorer.GtuColorer;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.road.gtu.lane.tactical.Controllable;

/**
 * Color based on control state.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class ControlColorer implements GtuColorer, Serializable
{

    /** */
    private static final long serialVersionUID = 20190501L;

    /** The legend. */
    private static final List<LegendEntry> LEGEND;

    /** Left color. */
    private static final Color C_NONE = Color.WHITE;

    /** No-left color. */
    private static final Color C_DISABLED = Color.RED;

    /** Right color. */
    private static final Color C_ENABLED = Color.GREEN;

    /** N/A color. */
    protected static final Color NA = Color.YELLOW;

    static
    {
        LEGEND = new ArrayList<>(6);
        LEGEND.add(new LegendEntry(C_NONE, "None", "None"));
        LEGEND.add(new LegendEntry(C_DISABLED, "Disabled", "Disabled"));
        LEGEND.add(new LegendEntry(C_ENABLED, "Enabled", "Enabled"));
        LEGEND.add(new LegendEntry(NA, "N/A", "N/A"));
    }

    /** {@inheritDoc} */
    @Override
    public Color getColor(final Gtu gtu)
    {
        if (!(gtu.getTacticalPlanner() instanceof Controllable))
        {
            return NA;
        }
        Controllable.State state = ((Controllable) gtu.getTacticalPlanner()).getControlState();
        if (state == null)
        {
            return NA;
        }
        switch (state)
        {
            case NONE:
                return C_NONE;
            case DISABLED:
                return C_DISABLED;
            case ENABLED:
                return C_ENABLED;
            default:
                return NA;
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<LegendEntry> getLegend()
    {
        return LEGEND;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "Control";
    }

}
