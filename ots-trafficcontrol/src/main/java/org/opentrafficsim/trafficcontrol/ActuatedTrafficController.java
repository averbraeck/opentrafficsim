package org.opentrafficsim.trafficcontrol;

import java.awt.Container;

/**
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public interface ActuatedTrafficController extends TrafficController
{

    /**
     * Tell the traffic controller that the state of a detector has changed.
     * @param detectorId String; id of the detector
     * @param detectingGTU boolean;
     */
    void updateDetector(String detectorId, boolean detectingGTU);

    /**
     * Retrieve the Swing (for now) container in which the controller displays its current state.
     * @return Container; the display of the current state; may return null!
     */
    Container getDisplayContainer();

}
