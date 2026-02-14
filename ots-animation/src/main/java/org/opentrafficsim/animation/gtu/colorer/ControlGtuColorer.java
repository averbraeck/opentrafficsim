package org.opentrafficsim.animation.gtu.colorer;

import java.awt.Color;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.draw.colorer.AbstractLegendColorer;
import org.opentrafficsim.road.gtu.tactical.Controllable;
import org.opentrafficsim.road.gtu.tactical.Controllable.State;

/**
 * Color based on control state.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class ControlGtuColorer extends AbstractLegendColorer<Gtu, State>
{

    /** Control colormap. */
    private static final Map<State, Color> COLOR_MAP =
            new EnumMap<>(Map.of(State.NONE, Color.WHITE, State.DISABLED, Color.RED, State.ENABLED, Color.GREEN));

    /** N/A color. */
    private static final Color NA = Color.YELLOW;

    /**
     * Constructor.
     */
    public ControlGtuColorer()
    {
        super((gtu) -> gtu.getTacticalPlanner() instanceof Controllable contr
                ? Optional.of(contr.getControlState()) : Optional.empty(),
                (state) -> state == null ? NA : COLOR_MAP.getOrDefault(state, NA),
                List.of(new LegendEntry(COLOR_MAP.get(State.NONE), "None", "None"),
                        new LegendEntry(COLOR_MAP.get(State.ENABLED), "Disabled", "Disabled"),
                        new LegendEntry(COLOR_MAP.get(State.DISABLED), "Enabled", "Enabled"),
                        new LegendEntry(NA, "N/A", "N/A")));
    }

    @Override
    public final String getName()
    {
        return "Control";
    }

}
