package org.opentrafficsim.draw;

import java.awt.font.TextAttribute;

/**
 * Weight of the text font in the explanation.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
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
     * Constructor.
     * @param value the corresponding TextAttribute constant
     */
    TextWeight(final Number value)
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
