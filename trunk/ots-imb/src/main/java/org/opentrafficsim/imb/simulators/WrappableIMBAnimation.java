package org.opentrafficsim.imb.simulators;

import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.simulationengine.WrappableAnimation;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$,
 *          initial version Sep 2, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface WrappableIMBAnimation extends WrappableAnimation
{
    /**
     * Retrieve the Network.
     * @return OTSNetwork
     */
    OTSNetwork getNetwork();
    
}
