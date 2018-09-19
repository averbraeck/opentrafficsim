package org.opentrafficsim.demo.geometry.shape;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.OTSNode;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simtime.SimTimeDoubleUnit;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

/**
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Sep 9, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public class ShapeModel implements OTSModelInterface
{
    /** */
    private static final long serialVersionUID = 20140815L;

    /** The simulator. */
    private DEVSSimulatorInterface.TimeDoubleUnit simulator;

    /** Nodes from shape file. */
    private Map<String, OTSNode> nodes;

    /** Links from shape file. */
    private Map<String, Link> shpLinks;
    
    /** the network. */
    private OTSNetwork network = new OTSNetwork("shape model network");

    /** {@inheritDoc} */
    @Override
    public final void constructModel(final SimulatorInterface<Time, Duration, SimTimeDoubleUnit> theSimulator)
            throws SimRuntimeException
    {
        this.simulator = (DEVSSimulatorInterface.TimeDoubleUnit) theSimulator;
        try
        {
            // Read the shape files with the function:
            this.nodes = ShapeFileReader.readNodes(network, "/gis/TESTcordonnodes.shp", "NODENR", true, true);
            this.shpLinks = new HashMap<>();
            ShapeFileReader.readLinks(network, "/gis/TESTcordonlinks_aangevuld.shp", this.shpLinks, this.nodes, this.simulator);

            this.simulator.scheduleEventAbs(Time.ZERO, this, this, "ntmFlowTimestep", null);
        }
        catch (Throwable exception)
        {
            exception.printStackTrace();
        }
    }

    /** {@inheritDoc} */
    @Override
    public final SimulatorInterface<Time, Duration, SimTimeDoubleUnit> getSimulator()
    {
        return this.simulator;
    }

    /** {@inheritDoc} */
    @Override
    public final OTSNetwork getNetwork()
    {
        return this.network;
    }
    
}
