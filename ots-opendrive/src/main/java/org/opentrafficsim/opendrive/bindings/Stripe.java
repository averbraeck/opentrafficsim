package org.opentrafficsim.opendrive.bindings;

/**
 * Stripe contains a placeholder for the stripe type enum.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class Stripe
{

    /**
     * Constructor.
     */
    public Stripe()
    {
        //
    }

    /**
     * Stripe type.
     */
    public enum Type
    {
        /** Block. */
        BLOCK,

        /** Dashed. */
        DASHED,

        /** Double line. */
        DOUBLE,

        /** Left lane changes only. */
        LEFT,

        /** Right lane changes only. */
        RIGHT,

        /** Solid line. */
        SOLID;
    }

}
