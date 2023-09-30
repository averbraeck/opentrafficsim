package org.opentrafficsim.animation;

/**
 * StripeType.java.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
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
