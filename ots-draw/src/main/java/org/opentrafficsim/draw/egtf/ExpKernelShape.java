package org.opentrafficsim.draw.egtf;

/**
 * Exponential implementation of a shape. Used as default when kernels are created.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class ExpKernelShape implements KernelShape
{

    /** Spatial size of the kernel. */
    private final double sigma;

    /** Temporal size of the kernel. */
    private final double tau;

    /**
     * Constructor.
     * @param sigma double; spatial size of the kernel
     * @param tau double; temporal size of the kernel
     */
    ExpKernelShape(final double sigma, final double tau)
    {
        this.sigma = sigma;
        this.tau = tau;
    }

    /** {@inheritDoc} */
    @Override
    public double weight(final double c, final double dx, final double dt)
    {
        return Math.exp(-Math.abs(dx) / this.sigma - Math.abs(dt - dx / c) / this.tau);
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "ExpKernelShape [sigma=" + this.sigma + ", tau=" + this.tau + "]";
    }

}
