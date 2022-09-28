package org.opentrafficsim.demo.fd;

import java.io.Serializable;

import org.opentrafficsim.core.dsol.AbstractOTSModel;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.road.network.OTSRoadNetwork;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * Fundamental diagram model.
 * <p>
 * Copyright (c) 2020-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="https://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class FundamentalDiagramModel extends AbstractOTSModel
{

    /** */
    private static final long serialVersionUID = 20200516L;

    /** The network. */
    private OTSRoadNetwork network;

    /**
     * @param simulator OTSSimulatorInterface; the simulator to use
     */
    public FundamentalDiagramModel(final OTSSimulatorInterface simulator)
    {
        super(simulator, "FD Demo", "Fundamental Diagram Demo");
    }

    /** {@inheritDoc} */
    @Override
    public OTSNetwork getNetwork()
    {
        return this.network;
    }

    /** {@inheritDoc} */
    @Override
    public void constructModel() throws SimRuntimeException
    {
        this.network = new OTSRoadNetwork("FdNetwork", true, getSimulator());
    }

    /** {@inheritDoc} */
    @Override
    public Serializable getSourceId()
    {
        return "FdDemo";
    }

}
