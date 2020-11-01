package org.opentrafficsim.core.dsol;

import org.opentrafficsim.core.network.OTSNetwork;

import nl.tudelft.simulation.dsol.model.DSOLModel;

/**
 * OTSModelInterface described the generic properties of an OTSModel such as the network and the model name.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$,
 *          initial version Aug 15, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public interface OTSModelInterface extends DSOLModel.TimeDoubleUnit<OTSSimulatorInterface>
{
    /**
     * Return the traffic network for the simulation.
     * @return the network.
     */
    OTSNetwork getNetwork();

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
