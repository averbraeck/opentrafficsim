package org.opentrafficsim.draw.core;

import java.awt.font.TextAttribute;

/**
 * Width of the text font in the explanation.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Dec 11, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
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
     * @param value Number; the corresponding TextAttribute constant
     */
    TextWidth(final Number value)
    {
        this.value = value;
    }

    /**
     * @return value the corresponding TextAttribute constant
     */
    protected final Number getValue()
    {
        return this.value;
    }

}
