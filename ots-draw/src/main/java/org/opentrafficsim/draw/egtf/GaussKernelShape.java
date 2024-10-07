package org.opentrafficsim.draw.egtf;

/**
 * Gaussian implementation of a shape.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class GaussKernelShape implements KernelShape
{

    /** Twice the spatial size of the kernel to the power of 2. */
    private final double sigma2;

    /** Twice the temporal size of the kernel to the power of 2. */
    private final double tau2;

    /**
     * Constructor.
     * @param sigma spatial size of the kernel
     * @param tau temporal size of the kernel
     */
    GaussKernelShape(final double sigma, final double tau)
    {
        this.sigma2 = 2.0 * sigma * sigma;
        this.tau2 = 2.0 * tau * tau;
    }

    /** {@inheritDoc} */
    @Override
    public double weight(final double c, final double dx, final double dt)
    {
        double dtt = dt - dx / c;
        return Math.exp(-(dx * dx) / this.sigma2 - dtt * dtt / this.tau2);
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "GaussKernelShape [sigma=" + Math.sqrt(this.sigma2 / 2.0) + ", tau=" + Math.sqrt(this.tau2 / 2.0) + "]";
    }

}
