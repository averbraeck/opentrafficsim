package org.opentrafficsim.animation.gtu.colorer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.draw.ColorInterpolator;
import org.opentrafficsim.draw.colorer.AbstractLegendColorer;
import org.opentrafficsim.road.gtu.perception.mental.channel.ChannelFuller;

/**
 * Colorer for level of attention.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class AttentionGtuColorer extends AbstractLegendColorer<Gtu, Double>
{

    /** Full. */
    private static final Color MAX = Color.RED;

    /** Medium. */
    private static final Color MID = Color.YELLOW;

    /** Zero. */
    private static final Color MIN = Color.GREEN;

    /** Not available. */
    private static final Color NA = Color.WHITE;

    /** Legend. */
    private static final List<LegendEntry> LEGEND;

    static
    {
        LEGEND = new ArrayList<>();
        LEGEND.add(new LegendEntry(MIN, "0.0", "0.0 max attention"));
        LEGEND.add(new LegendEntry(MID, "0.5", "0.5 max attention"));
        LEGEND.add(new LegendEntry(MAX, "1.0", "1.0 max attention"));
        LEGEND.add(new LegendEntry(NA, "N/A", "N/A"));
    }

    /**
     * Constructor.
     */
    public AttentionGtuColorer()
    {
        super((g) -> g.getParameters().getOptionalParameter(ChannelFuller.ATT), (att) ->
        {
            return att == null ? NA : (att < 0.5 ? ColorInterpolator.interpolateColor(MIN, MID, att / 0.5)
                    : ColorInterpolator.interpolateColor(MID, MAX, (att - 0.5) / 0.5));
        }, LEGEND);
    }

    @Override
    public String getName()
    {
        return "Attention";
    }

}
