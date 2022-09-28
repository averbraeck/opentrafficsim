package org.opentrafficsim.road.gtu.lane;

import java.rmi.RemoteException;

import org.djutils.event.EventInterface;
import org.djutils.event.EventListenerInterface;
import org.opentrafficsim.core.gtu.MoveCheckerException;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.OTSNetwork;

/**
 * Abstract class that listens to move events of GTUs so checks can be performed.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public abstract class AbstractLaneBasedMoveChecker implements EventListenerInterface
{
    /** */
    private static final long serialVersionUID = 1L;

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
                checkMove((LaneBasedGTU) event.getSourceId());
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
