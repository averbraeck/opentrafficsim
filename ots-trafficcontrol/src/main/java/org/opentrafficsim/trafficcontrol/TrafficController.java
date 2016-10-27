package org.opentrafficsim.trafficcontrol;

import nl.tudelft.simulation.event.EventProducerInterface;

/**
 * Interface for traffic light controllers.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Oct 14, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface TrafficController extends EventProducerInterface
{
    /**
     * Tell the traffic controller that the state of a detector has changed.
     * @param detectorId String; id of the detector
     * @param detectingGTU boolean;
     */
    public void updateDetector(String detectorId, boolean detectingGTU);

}
