package org.opentrafficsim.road.gtu.colorer;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.opentrafficsim.core.animation.gtu.colorer.GTUColorer;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.road.gtu.lane.tactical.Controllable;

/**
 * Color based on control state.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 23 feb. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class ControlColorer implements GTUColorer, Serializable
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
    public Color getColor(final GTU gtu)
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
