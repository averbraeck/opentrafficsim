package org.opentrafficsim.animation.gtu.colorer;

import java.awt.Color;
import java.text.NumberFormat;
import java.util.List;

import org.djunits.value.vdouble.scalar.Duration;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.draw.BoundsPaintScale;
import org.opentrafficsim.draw.colorer.AbstractLegendBarColorer;
import org.opentrafficsim.draw.colorer.NumberFormatUnit;

/**
 * Color on a scale from between two given limits.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class DesiredHeadwayGtuColorer extends AbstractLegendBarColorer<Gtu, Duration>
{

    /** Low color. */
    private static final Color LOW = Color.RED;

    /** Middle color. */
    private static final Color MIDDLE = Color.YELLOW;

    /** High color. */
    private static final Color HIGH = Color.GREEN;

    /** Unknown color. */
    private static final Color UNKNOWN = Color.WHITE;

    /** Number formatter. */
    private static final NumberFormatUnit FORMAT = new NumberFormatUnit("s", 2);

    /**
     * Constructor.
     * @param boundPaintScale bound paint scale
     * @param legend legend
     */
    private DesiredHeadwayGtuColorer(final BoundsPaintScale boundPaintScale, final List<LegendEntry> legend)
    {
        super((gtu) -> gtu.getParameters().getOptionalParameter(ParameterTypes.T),
                (t) -> t == null ? UNKNOWN : boundPaintScale.getPaint(t.si), legend, boundPaintScale);
    }

    /**
     * Constructor using input Tmin and Tmax.
     * @param tMin minimum headway
     * @param tMax maximum headway
     */
    public DesiredHeadwayGtuColorer(final Duration tMin, final Duration tMax)
    {
        this(new BoundsPaintScale(new double[] {tMin.si, (tMin.si + tMax.si) / 2.0, tMax.si}, new Color[] {LOW, MIDDLE, HIGH},
                UNKNOWN),
                List.of(new LegendEntry(LOW, String.format(FORMAT.getDoubleFormat(), tMin.si), "Tmin"),
                        new LegendEntry(MIDDLE, String.format(FORMAT.getDoubleFormat(), (tMin.si + tMax.si) / 2.0), "Mean"),
                        new LegendEntry(HIGH, String.format(FORMAT.getDoubleFormat(), tMax.si), "Tmax"),
                        new LegendEntry(UNKNOWN, "Unknown", "Unknown")));
    }

    @Override
    public NumberFormat getNumberFormat()
    {
        return FORMAT;
    }

    @Override
    public final String getName()
    {
        return "Desired headway";
    }

}
