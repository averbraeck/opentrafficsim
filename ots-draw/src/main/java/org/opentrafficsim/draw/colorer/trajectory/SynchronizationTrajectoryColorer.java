package org.opentrafficsim.draw.colorer.trajectory;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import org.opentrafficsim.kpi.sampling.data.ExtendedDataType;

/**
 * Synchronziation colorer for trajectory.
 * <p>
 * Copyright (c) 2024-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class SynchronizationTrajectoryColorer extends ExtendedDataTrajectoryColorer<String, String> implements TrajectoryColorer
{

    /*
     * This class is different from SynchronizationGtuColorer, and does not share a common superclass, as Synchronization.State
     * is not known at the generic context and because NONE is black rather than white for trajectories.
     */

    /** Synchronization colormap. */
    private static final Map<String, Color> COLOR_MAP = Map.of("NONE", Color.BLACK, "SYNCHRONIZING", Color.ORANGE, "INDICATING",
            Color.RED, "COOPERATING", new Color(0, 192, 0));

    /** N/A color. */
    public static final Color NA = Color.YELLOW;

    /**
     * Constructor. The used extended data type should store synchronization state as strings NONE, SYNCHRONIZING, INDICATING or
     * COOPERATING. An empty value is also allowed.
     * @param dataType extended data type in sampler which stores synchronization state
     */
    public SynchronizationTrajectoryColorer(final ExtendedDataType<? extends String, ?, ?, ?> dataType)
    {
        super(dataType, (str) -> str, (str) -> COLOR_MAP.containsKey(str) ? COLOR_MAP.get(str) : NA,
                List.of(new LegendEntry(COLOR_MAP.get("NONE"), "None", "None"),
                        new LegendEntry(COLOR_MAP.get("SYNCHRONIZING"), "Synchronizing", "Synchronizing"),
                        new LegendEntry(COLOR_MAP.get("INDICATING"), "Indicating", "Indicating"),
                        new LegendEntry(COLOR_MAP.get("COOPERATING"), "Cooperating", "Cooperating"),
                        new LegendEntry(NA, "N/A", "N/A")));
    }

}
