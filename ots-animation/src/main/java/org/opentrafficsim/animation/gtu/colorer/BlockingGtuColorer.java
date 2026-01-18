package org.opentrafficsim.animation.gtu.colorer;

import java.awt.Color;
import java.util.List;
import java.util.Optional;

import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.draw.colorer.AbstractLegendColorer;
import org.opentrafficsim.road.gtu.lane.tactical.Blockable;

/**
 * Displays whether a GTU is blocking conflicts.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class BlockingGtuColorer extends AbstractLegendColorer<Gtu, Boolean>
{

    /** Blocking color. */
    private static final Color BLOCKING = Color.RED;

    /** Not blocking color. */
    private static final Color NOT_BLOCKING = Color.WHITE;

    /** Unknown blocking color. */
    private static final Color NA = Color.YELLOW;

    /**
     * Constructor.
     */
    public BlockingGtuColorer()
    {
        super((gtu) -> gtu.getTacticalPlanner() instanceof Blockable block ? Optional.of(block.isBlocking()) : Optional.empty(),
                (blocking) -> blocking == null ? NA : (blocking ? BLOCKING : NOT_BLOCKING),
                List.of(new LegendEntry(NOT_BLOCKING, "Not blocking", "Not blocking"),
                        new LegendEntry(BLOCKING, "Blocking", "Blocking"), new LegendEntry(NA, "N/A", "N/A")));
    }

    @Override
    public String getName()
    {
        return "Blocking";
    }

}
