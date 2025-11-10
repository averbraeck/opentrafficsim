package org.opentrafficsim.animation.gtu.colorer;

import java.awt.Color;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.draw.colorer.AbstractLegendColorer;
import org.opentrafficsim.road.gtu.lane.tactical.Synchronizable;
import org.opentrafficsim.road.gtu.lane.tactical.Synchronizable.State;

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
public class SynchronizationGtuColorer extends AbstractLegendColorer<Gtu, State>
{

    /** Synchronization colormap. */
    private static final Map<State, Color> COLOR_MAP = new EnumMap<>(Map.of(State.NONE, Color.WHITE, State.SYNCHRONIZING,
            Color.ORANGE, State.INDICATING, Color.RED, State.COOPERATING, new Color(0, 192, 0)));

    /** N/A color. */
    public static final Color NA = Color.YELLOW;

    /**
     * Constructor.
     */
    public SynchronizationGtuColorer()
    {
        super((gtu) -> gtu.getTacticalPlanner() instanceof Synchronizable sync ? sync.getSynchronizationState() : null,
                (state) -> state == null ? NA : COLOR_MAP.getOrDefault(state, NA),
                List.of(new LegendEntry(COLOR_MAP.get(State.NONE), "None", "None"),
                        new LegendEntry(COLOR_MAP.get(State.SYNCHRONIZING), "Synchronizing", "Synchonizing"),
                        new LegendEntry(COLOR_MAP.get(State.INDICATING), "Indicating", "Indicating"),
                        new LegendEntry(COLOR_MAP.get(State.COOPERATING), "Cooperating", "Cooperating"),
                        new LegendEntry(NA, "N/A", "N/A")));
    }

    @Override
    public final String getName()
    {
        return "Synchronization";
    }

}
