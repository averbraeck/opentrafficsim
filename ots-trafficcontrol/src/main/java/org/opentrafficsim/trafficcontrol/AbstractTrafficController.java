package org.opentrafficsim.trafficcontrol;

import org.djutils.event.EventProducer;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;

/**
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public abstract class AbstractTrafficController extends EventProducer implements TrafficController
{

    /** */
    private static final long serialVersionUID = 20190221L;

    /** Id of this controller. */
    private final String id;

    /**
     * Constructor for traffic controller.
     * @param id String; id
     * @param simulator OTSSimulatorInterface; simulator
     */
    public AbstractTrafficController(final String id, final OTSSimulatorInterface simulator)
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
