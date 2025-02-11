package org.opentrafficsim.draw;

import java.awt.font.TextAttribute;

/**
 * Width of the text font in the explanation.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public enum TextWidth
{
    /** condensed font. */
    WIDTH_CONDENSED(TextAttribute.WIDTH_CONDENSED),

    /** moderately condensed font. */
    WIDTH_SEMI_CONDENSED(TextAttribute.WIDTH_SEMI_CONDENSED),

    /** regular font. */
    WIDTH_REGULAR(TextAttribute.WIDTH_REGULAR),

    /** moderately extended font. */
    WIDTH_SEMI_EXTENDED(TextAttribute.WIDTH_SEMI_EXTENDED),

    /** extended font. */
    WIDTH_EXTENDED(TextAttribute.WIDTH_EXTENDED);

    /** the corresponding TextAttribute constant. */
    private final Number value;

    /**
     * Constructor.
     * @param value the corresponding TextAttribute constant
     */
    TextWidth(final Number value)
    {
        this.value = value;
    }

    /**
     * Returns the value.
     * @return value the corresponding TextAttribute constant
     */
    protected final Number getValue()
    {
        return this.value;
    }

}
