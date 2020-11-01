package org.opentrafficsim.core.egtf;

/**
 * Shape interface for a kernel.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 24 okt. 2018 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
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
