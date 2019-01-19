package org.opentrafficsim.core.animation;

/**
 * StripeType.java. <br>
 * <br>
 * Copyright (c) 2003-2019 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public enum StripeType
{
    /** Single solid line. */
    SOLID,

    /** Line |¦ allow to go to left, but not to right. */
    LEFTONLY,

    /** Line ¦| allow to go to right, but not to left. */
    RIGHTONLY,

    /** Dashes ¦ allow to cross in both directions. */
    DASHED,

    /** Double solid line ||, don't cross. */
    DOUBLE,

    /** Block : allow to cross in both directions. */
    BLOCK
}
