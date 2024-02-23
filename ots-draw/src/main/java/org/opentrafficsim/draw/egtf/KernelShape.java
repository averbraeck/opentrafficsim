package org.opentrafficsim.draw.egtf;

/**
 * Shape interface for a kernel.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public interface KernelShape
{

    /**
     * Calculates a weight.
     * @param c double; assumed propagation speed
     * @param dx double; distance between measurement and estimated point
     * @param dt double; time between measurement and estimated point
     * @return double; weight
     */
    double weight(double c, double dx, double dt);

}
