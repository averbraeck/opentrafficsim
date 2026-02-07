package org.opentrafficsim.web.test;

import org.opentrafficsim.core.dsol.AbstractOtsModel;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.factory.xml.parser.XmlParser;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * TJunctionModel.java.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class TJunctionModel extends AbstractOtsModel
{
    /** The network. */
    private RoadNetwork network;

    /**
     * Constructor.
     * @param simulator the simulator for this model
     */
    public TJunctionModel(final OtsSimulatorInterface simulator)
    {
        super(simulator);
    }

    @Override
    public void constructModel() throws SimRuntimeException
    {
        try
        {
            this.network = new RoadNetwork("TJunction", getSimulator());
            new XmlParser(this.network).setResource("/resources/conflict/TJunction.xml").setScenario("1").build();
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    @Override
    public RoadNetwork getNetwork()
    {
        return this.network;
    }
}
