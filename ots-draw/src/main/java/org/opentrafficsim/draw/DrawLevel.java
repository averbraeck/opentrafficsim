package org.opentrafficsim.draw;

/**
 * Z-level for use in {@code Locatable.getZ()} implementations. The goal of this is to use z-ordering to obtain the desired
 * drawing order.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public enum DrawLevel
{
    /** Annotation level (e.g. draggable for editing). */
    ANNOTATION(0.0004),

    /** Snap level (e.g. line indicating where draggable can be). */
    SNAP(0.0003),

    /** Selection indicator level. */
    SELECTION(0.0002),

    /** Label level. */
    LABEL(0.0001),

    /** GTU level. */
    GTU(0.0000),

    /** Node level. */
    NODE(-0.0001),

    /** Center line level. */
    CENTER_LINE(-0.0002),

    /** Line level. */
    LINK(-0.0003),

    /** Object level (detectors, traffic lights, etc.). */
    OBJECT(-0.0004),

    /** Lane marking level. */
    MARKING(-0.0005),

    /** Lane level. */
    LANE(-0.0006),

    /** Shoulder level. */
    SHOULDER(-0.0007);

    /** Z-level. */
    private final double z;

    /**
     * Constructor.
     * @param z z-level.
     */
    DrawLevel(final double z)
    {
        this.z = z;
    }

    /**
     * Z-level.
     * @return z-level.
     */
    public double getZ()
    {
        return this.z;
    }
}
