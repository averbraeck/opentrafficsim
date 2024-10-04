package org.opentrafficsim.draw.egtf;

/**
 * Kernel with maximum range and shape.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
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
     * @param xMax maximum spatial range
     * @param tMax maximum temporal range
     * @param shape shape of the kernel
     */
    Kernel(final double xMax, final double tMax, final KernelShape shape)
    {
        this.xMax = xMax;
        this.tMax = tMax;
        this.shape = shape;
    }

    /**
     * Returns a weight assuming given propagation speed.
     * @param c assumed propagation speed
     * @param dx distance between measurement and estimated point
     * @param dt time between measurement and estimated point
     * @return weight assuming given propagation speed
     */
    final double weight(final double c, final double dx, final double dt)
    {
        return this.shape.weight(c, dx, dt);
    }

    /**
     * Returns the from location of the valid data range.
     * @param x location of estimated point
     * @return from location of the valid data range
     */
    final double fromLocation(final double x)
    {
        return x - this.xMax;
    }

    /**
     * Returns the to location of the valid data range.
     * @param x location of estimated point
     * @return to location of the valid data range
     */
    final double toLocation(final double x)
    {
        return x + this.xMax;
    }

    /**
     * Returns the from time of the valid data range.
     * @param t time of estimated point
     * @return from time of the valid data range
     */
    final double fromTime(final double t)
    {
        return t - this.tMax;
    }

    /**
     * Returns the to time of the valid data range.
     * @param t time of estimated point
     * @return to time of the valid data range
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
