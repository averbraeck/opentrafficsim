package org.opentrafficsim.core.dsol;

import org.djunits.value.vdouble.scalar.Duration;
import org.opentrafficsim.core.network.OtsNetwork;

import nl.tudelft.simulation.dsol.model.DSOLModel;

/**
 * OtsModelInterface described the generic properties of an OtsModel such as the network and the model name.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 */
public interface OtsModelInterface extends DSOLModel<Duration, OtsSimulatorInterface>
{
    /**
     * Return the traffic network for the simulation.
     * @return the network.
     */
    OtsNetwork getNetwork();

    /**
     * Return a very short description of the simulation.
     * @return String; short description of the simulation
     */
    String getShortName();

    /**
     * Return a description of the simulation (HTML formatted).
     * @return String; HTML text describing the simulation
     */
    String getDescription();
}
