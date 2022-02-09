package org.opentrafficsim.draw.core;

import java.awt.font.TextAttribute;

/**
 * Weight of the text font in the explanation.
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
public enum TextWeight
{
    /** The lightest predefined weight. */
    WEIGHT_EXTRA_LIGHT(TextAttribute.WEIGHT_EXTRA_LIGHT),

    /** The standard light weight. */
    WEIGHT_LIGHT(TextAttribute.WEIGHT_LIGHT),

    /** An intermediate weight between WEIGHT_LIGHT and WEIGHT_STANDARD. */
    WEIGHT_DEMILIGHT(TextAttribute.WEIGHT_DEMILIGHT),

    /** The standard weight. */
    WEIGHT_REGULAR(TextAttribute.WEIGHT_REGULAR),

    /** A moderately heavier weight than WEIGHT_REGULAR. */
    WEIGHT_SEMIBOLD(TextAttribute.WEIGHT_SEMIBOLD),

    /** An intermediate weight between WEIGHT_REGULAR and WEIGHT_BOLD. */
    WEIGHT_MEDIUM(TextAttribute.WEIGHT_MEDIUM),

    /** A moderately lighter weight than WEIGHT_BOLD. */
    WEIGHT_DEMIBOLD(TextAttribute.WEIGHT_DEMIBOLD),

    /** The standard bold weight. */
    WEIGHT_BOLD(TextAttribute.WEIGHT_BOLD),

    /** A moderately heavier weight than WEIGHT_BOLD. */
    WEIGHT_HEAVY(TextAttribute.WEIGHT_HEAVY),

    /** An extra heavy weight. */
    WEIGHT_EXTRABOLD(TextAttribute.WEIGHT_EXTRABOLD),

    /** The heaviest predefined weight. */
    WEIGHT_ULTRABOLD(TextAttribute.WEIGHT_ULTRABOLD);

    /** the corresponding TextAttribute constant. */
    private final Number value;

    /**
     * @param value Number; the corresponding TextAttribute constant
     */
    TextWeight(final Number value)
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
