package org.opentrafficsim.trafficcontrol;

import org.djutils.event.LocalEventProducer;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;

/**
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public abstract class AbstractTrafficController extends LocalEventProducer implements TrafficController
{

    /** */
    private static final long serialVersionUID = 20190221L;

    /** Id of this controller. */
    private final String id;

    /**
     * Constructor for traffic controller.
     * @param id String; id
     * @param simulator OtsSimulatorInterface; simulator
     */
    public AbstractTrafficController(final String id, final OtsSimulatorInterface simulator)
    {
        Throw.whenNull(id, "Id may not be null.");
        this.id = id;
        fireTimedEvent(TrafficController.TRAFFICCONTROL_CONTROLLER_CREATED,
                new Object[] {this.id, TrafficController.STARTING_UP}, simulator.getSimulatorTime());
    }

    /**
     * @return id.
     */
    @Override
    public String getId()
    {
        return this.id;
    }

}
