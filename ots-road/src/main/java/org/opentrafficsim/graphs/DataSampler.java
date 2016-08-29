package org.opentrafficsim.graphs;

import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.network.lane.Lane;

import nl.tudelft.simulation.event.EventInterface;
import nl.tudelft.simulation.event.EventListenerInterface;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Aug 26, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class DataSampler implements EventListenerInterface
{
    /** The (ordered) set of observed lanes. */
    private final List<Lane> lanes;
    
    /** The OTS Network that knows about all the GTUs and Lanes. */
    private final OTSNetwork network;
    
    /** The GTUs currently in the network. */
    private final Set<String> gtus = new HashSet<>();

    /**
     * @param network the network
     * @param lanes lanes to sample on
     * 
     */
    public DataSampler(final OTSNetwork network, final List<Lane> lanes)
    {
        this.lanes = lanes;
        this.network = network;
        network.addListener(this, Network.GTU_ADD_EVENT);
        network.addListener(this, Network.GTU_REMOVE_EVENT);
    }
    
    /** {@inheritDoc} */
    @Override
    public final void notify(final EventInterface event) throws RemoteException
    {
        if (event.getType().equals(Network.GTU_ADD_EVENT))
        {
            Object[] addInfo = (Object[]) event.getContent();
            this.gtus.add(addInfo[0].toString());
            GTU gtu = this.network.getGTU(addInfo[0].toString());
            gtu.addListener(this, LaneBasedGTU.MOVE_EVENT);
        }
        else if (event.getType().equals(Network.GTU_REMOVE_EVENT))
        {
            Object[] removeInfo = (Object[]) event.getContent();
            this.gtus.remove(removeInfo[0].toString());
            GTU gtu = this.network.getGTU(removeInfo[0].toString());
            gtu.removeListener(this, LaneBasedGTU.MOVE_EVENT);
        }
        else if (event.getType().equals(LaneBasedGTU.MOVE_EVENT))
        {
            // Ignored.
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "DataSampler [lanes=" + this.lanes + ", network=" + this.network + ", #gtus=" + this.gtus.size() + "]";
    }

}
;