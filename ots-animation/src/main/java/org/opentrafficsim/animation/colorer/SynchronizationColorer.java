package org.opentrafficsim.animation.colorer;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.opentrafficsim.animation.gtu.colorer.GtuColorer;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.road.gtu.lane.tactical.Synchronizable;

/**
 * Color based on synchronization state.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class SynchronizationColorer implements GtuColorer, Serializable
{

    /** */
    private static final long serialVersionUID = 20180223L;

    /** The legend. */
    private static final List<LegendEntry> LEGEND;

    /** Left color. */
    private static final Color C_NONE = Color.WHITE;

    /** No-left color. */
    private static final Color C_SYNCHRONIZING = Color.ORANGE;

    /** Right color. */
    private static final Color C_INDICATING = Color.RED;

    /** None color. */
    private static final Color C_COOPERATING = new Color(0, 128, 0);

    /** N/A color. */
    protected static final Color NA = Color.YELLOW;

    static
    {
        LEGEND = new ArrayList<>(6);
        LEGEND.add(new LegendEntry(C_NONE, "None", "None"));
        LEGEND.add(new LegendEntry(C_SYNCHRONIZING, "Synchronizing", "Synchonizing"));
        LEGEND.add(new LegendEntry(C_INDICATING, "Indicating", "Indicating"));
        LEGEND.add(new LegendEntry(C_COOPERATING, "Cooperating", "Cooperating"));
        LEGEND.add(new LegendEntry(NA, "N/A", "N/A"));
    }

    /**
     * Constructor.
     */
    public SynchronizationColorer()
    {
        //
    }

    @Override
    public Color getColor(final Gtu gtu)
    {
        if (!(gtu.getTacticalPlanner() instanceof Synchronizable))
        {
            return NA;
        }
        Synchronizable.State state = ((Synchronizable) gtu.getTacticalPlanner()).getSynchronizationState();
        if (state == null)
        {
            return NA;
        }
        switch (state)
        {
            case NONE:
                return C_NONE;
            case COOPERATING:
                return C_COOPERATING;
            case INDICATING:
                return C_INDICATING;
            case SYNCHRONIZING:
                return C_SYNCHRONIZING;
            default:
                return NA;
        }
    }

    @Override
    public List<LegendEntry> getLegend()
    {
        return LEGEND;
    }

    @Override
    public final String getName()
    {
        return "Synchronization";
    }

}
