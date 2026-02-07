package org.opentrafficsim.trafficcontrol;

import java.awt.Container;
import java.util.Optional;

/**
 * Interface for actuated traffic controllers.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface ActuatedTrafficController extends TrafficController
{

    /**
     * Tell the traffic controller that the state of a detector has changed.
     * @param detectorId id of the detector
     * @param detectingGTU detecting GTU
     */
    void updateDetector(String detectorId, boolean detectingGTU);

    /**
     * Retrieve the Swing (for now) container in which the controller displays its current state.
     * @return the display of the current state; may return empty
     */
    Optional<Container> getDisplayContainer();

}
