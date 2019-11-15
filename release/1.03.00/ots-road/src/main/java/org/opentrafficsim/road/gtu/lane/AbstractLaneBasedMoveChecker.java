package org.opentrafficsim.road.gtu.lane;

import java.rmi.RemoteException;

import org.opentrafficsim.core.gtu.MoveCheckerException;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.OTSNetwork;

import nl.tudelft.simulation.event.EventInterface;
import nl.tudelft.simulation.event.EventListenerInterface;

/**
 * Abstract class that listens to move events of GTUs so checks can be performed.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Aug 6, 2019 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public abstract class AbstractLaneBasedMoveChecker implements EventListenerInterface
{

    /** Network. */
    private final OTSNetwork network;

    /**
     * Constructor.
     * @param network OTSNetwork; network
     */
    public AbstractLaneBasedMoveChecker(final OTSNetwork network)
    {
        network.addListener(this, Network.GTU_ADD_EVENT);
        network.addListener(this, Network.GTU_REMOVE_EVENT);
        this.network = network;
    }

    /** {@inheritDoc} */
    @Override
    public void notify(final EventInterface event) throws RemoteException
    {
        if (event.getType().equals(LaneBasedGTU.LANEBASED_MOVE_EVENT))
        {
            try
            {
                checkMove(((LaneBasedGTU) event.getSource()));
            }
            catch (Exception ex)
            {
                throw new MoveCheckerException(ex);
            }
        }
        else if (event.getType().equals(Network.GTU_ADD_EVENT))
        {
            this.network.getGTU((String) event.getContent()).addListener(this, LaneBasedGTU.LANEBASED_MOVE_EVENT);
        }
        else if (event.getType().equals(Network.GTU_REMOVE_EVENT))
        {
            this.network.getGTU((String) event.getContent()).removeListener(this, LaneBasedGTU.LANEBASED_MOVE_EVENT);
        }
        else
        {
            throw new RemoteException("AbstractMoveChecker is a listener to an unknown event type.");
        }
    }

    /**
     * Check the move of the given GTU.
     * @param gtu LaneBasedGTU; GTU.
     * @throws Exception thrown when something is not all right
     */
    public abstract void checkMove(LaneBasedGTU gtu) throws Exception;

}
