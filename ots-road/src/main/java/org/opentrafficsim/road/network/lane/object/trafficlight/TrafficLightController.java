package org.opentrafficsim.road.network.lane.object.trafficlight;

import org.djunits.value.vdouble.scalar.Duration;
import org.opentrafficsim.base.Identifiable;

/**
 * Interface for a controller for a number of traffic lights. The controller knows of groups of traffic lights that belong to
 * the same "phase".
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface TrafficLightController extends Identifiable
{
    /** @return the controller id. */
    @Override
    String getId();

    /** @return the number of phases. */
    int getNumberOfPhases();

    /** @return the phase id. */
    int getCurrentPhase();

    /** @return the time between phases. */
    Duration getClearanceDurationToNextPhase();

    /**
     * Add a traffic light to a phase.
     * @param phaseId int; the id of the phase.
     * @param trafficLight TrafficLight; the traffic light to add
     * @throws TrafficLightException when the phase was not created
     */
    void addTrafficLightToPhase(int phaseId, TrafficLight trafficLight) throws TrafficLightException;

}
