package org.opentrafficsim.road.gtu.lane;

import java.rmi.RemoteException;

import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.opentrafficsim.core.gtu.MoveCheckerException;
import org.opentrafficsim.core.network.Network;

/**
 * Abstract class that listens to move events of GTUs so checks can be performed.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public abstract class AbstractLaneBasedMoveChecker implements EventListener
{
    /** */
    private static final long serialVersionUID = 1L;

    /** Network. */
    private final Network network;

    /**
     * Constructor.
     * @param network network
     */
    public AbstractLaneBasedMoveChecker(final Network network)
    {
        network.addListener(this, Network.GTU_ADD_EVENT);
        network.addListener(this, Network.GTU_REMOVE_EVENT);
        this.network = network;
    }

    /** {@inheritDoc} */
    @Override
    public void notify(final Event event) throws RemoteException
    {
        if (event.getType().equals(LaneBasedGtu.LANEBASED_MOVE_EVENT))
        {
            try
            {
                Object[] payload = (Object[]) event.getContent();
                checkMove((LaneBasedGtu) this.network.getGTU((String) payload[0]));
            }
            catch (Exception ex)
            {
                throw new MoveCheckerException(ex);
            }
        }
        else if (event.getType().equals(Network.GTU_ADD_EVENT))
        {
            this.network.getGTU((String) event.getContent()).addListener(this, LaneBasedGtu.LANEBASED_MOVE_EVENT);
        }
        else if (event.getType().equals(Network.GTU_REMOVE_EVENT))
        {
            this.network.getGTU((String) event.getContent()).removeListener(this, LaneBasedGtu.LANEBASED_MOVE_EVENT);
        }
        else
        {
            throw new RemoteException("AbstractMoveChecker is a listener to an unknown event type.");
        }
    }

    /**
     * Check the move of the given GTU.
     * @param gtu GTU.
     * @throws Exception thrown when something is not all right
     */
    public abstract void checkMove(LaneBasedGtu gtu) throws Exception;

}
