package org.opentrafficsim.core.egtf;

/**
 * Kernel with maximum range and shape.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class Kernel
{

    /** Maximum spatial range. */
    private final double xMax;

    /** Maximum temporal range. */
    private final double tMax;

    /** Shape of the kernel. */
    private final KernelShape shape;

    /**
     * Constructor.
     * @param xMax double; maximum spatial range
     * @param tMax double; maximum temporal range
     * @param shape KernelShape; shape of the kernel
     */
    Kernel(final double xMax, final double tMax, final KernelShape shape)
    {
        this.xMax = xMax;
        this.tMax = tMax;
        this.shape = shape;
    }

    /**
     * Returns a weight assuming given propagation speed.
     * @param c double; assumed propagation speed
     * @param dx double; distance between measurement and estimated point
     * @param dt double; time between measurement and estimated point
     * @return double; weight assuming given propagation speed
     */
    final double weight(final double c, final double dx, final double dt)
    {
        return this.shape.weight(c, dx, dt);
    }

    /**
     * Returns the from location of the valid data range.
     * @param x double; location of estimated point
     * @return double; from location of the valid data range
     */
    final double fromLocation(final double x)
    {
        return x - this.xMax;
    }

    /**
     * Returns the to location of the valid data range.
     * @param x double; location of estimated point
     * @return double; to location of the valid data range
     */
    final double toLocation(final double x)
    {
        return x + this.xMax;
    }

    /**
     * Returns the from time of the valid data range.
     * @param t double; time of estimated point
     * @return double; from time of the valid data range
     */
    final double fromTime(final double t)
    {
        return t - this.tMax;
    }

    /**
     * Returns the to time of the valid data range.
     * @param t double; time of estimated point
     * @return double; to time of the valid data range
     */
    final double toTime(final double t)
    {
        return t + this.tMax;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "Kernel [tMax=" + this.tMax + ", xMax=" + this.xMax + ", shape=" + this.shape + "]";
    }

}
