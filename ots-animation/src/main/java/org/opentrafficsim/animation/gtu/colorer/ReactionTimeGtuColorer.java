package org.opentrafficsim.animation.gtu.colorer;

import java.awt.Color;
import java.text.NumberFormat;

import org.djunits.value.vdouble.scalar.Duration;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.draw.BoundsPaintScale;
import org.opentrafficsim.draw.Colors;
import org.opentrafficsim.draw.colorer.AbstractLegendBarColorer;
import org.opentrafficsim.draw.colorer.LegendColorer;
import org.opentrafficsim.draw.colorer.NumberFormatUnit;

/**
 * Colors the reaction time.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class ReactionTimeGtuColorer extends AbstractLegendBarColorer<Gtu, Duration>
{

    /** Number formatter. */
    private static final NumberFormatUnit FORMAT = new NumberFormatUnit("s", 1);

    /**
     * Constructor.
     * @param boundsPaintScale bounds paint scale
     * @param notApplicable color of GTUs without reported reaction time
     */
    public ReactionTimeGtuColorer(final BoundsPaintScale boundsPaintScale, final Color notApplicable)
    {
        super((gtu) -> gtu.getParameters().getOptionalParameter(ParameterTypes.TR),
                (t) -> t == null ? notApplicable : boundsPaintScale.getPaint(t.si),
                LegendColorer.fromBoundsPaintScale(boundsPaintScale, FORMAT.getDoubleFormat(), notApplicable),
                boundsPaintScale);
    }

    /**
     * Constructor.
     * @param maxReactionTime maximum reaction time.
     */
    public ReactionTimeGtuColorer(final Duration maxReactionTime)
    {
        this(new BoundsPaintScale(new double[] {0.0, maxReactionTime.si / 2.0, maxReactionTime.si}, Colors.GREEN_RED),
                Color.BLUE);
    }

    @Override
    public NumberFormat getNumberFormat()
    {
        return FORMAT;
    }

    @Override
    public String getName()
    {
        return "Reaction time";
    }

}
