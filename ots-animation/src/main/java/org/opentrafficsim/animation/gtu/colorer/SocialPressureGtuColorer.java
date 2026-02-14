package org.opentrafficsim.animation.gtu.colorer;

import java.awt.Color;
import java.text.NumberFormat;
import java.util.List;

import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.draw.BoundsPaintScale;
import org.opentrafficsim.draw.colorer.AbstractLegendBarColorer;
import org.opentrafficsim.draw.colorer.NumberFormatUnit;
import org.opentrafficsim.road.gtu.tactical.util.lmrs.Tailgating;

/**
 * Colorer for social pressure.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class SocialPressureGtuColorer extends AbstractLegendBarColorer<Gtu, Double>
{

    /** No pressure color. */
    private static final Color NONE = Color.WHITE;

    /** Full pressure color. */
    private static final Color FULL = Color.RED;

    /** Not applicable color. */
    private static final Color NA = Color.YELLOW;

    /** Bounds paint scale. */
    private static final BoundsPaintScale SCALE = new BoundsPaintScale(new double[] {0.0, 1.0}, new Color[] {NONE, FULL});

    /** Number formatter. */
    private static final NumberFormatUnit FORMAT = new NumberFormatUnit("", 1);

    /**
     * Constructor.
     */
    public SocialPressureGtuColorer()
    {
        super((gtu) -> gtu.getParameters().getOptionalParameter(Tailgating.RHO), (rho) -> rho == null ? NA : SCALE.getPaint(rho),
                List.of(new LegendEntry(NONE, "None", "None: 0.0"), new LegendEntry(FULL, "Full", "Full: 1.0"),
                        new LegendEntry(NA, "N/A", "N/A")),
                SCALE);
    }

    @Override
    public NumberFormat getNumberFormat()
    {
        return FORMAT;
    }

    @Override
    public String getName()
    {
        return "Social pressure";
    }

}
