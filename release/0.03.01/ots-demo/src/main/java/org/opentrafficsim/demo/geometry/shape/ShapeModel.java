package org.opentrafficsim.demo.geometry.shape;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.OTSNode;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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

    /** the simulator. */
    private OTSDEVSSimulatorInterface simulator;

    /** nodes from shape file. */
    private Map<String, OTSNode> nodes;

    /** links from shape file. */
    private Map<String, Link> shpLinks;

    /** {@inheritDoc} */
    @Override
    public final void constructModel(
        final SimulatorInterface<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> theSimulator)
        throws SimRuntimeException, RemoteException
    {
        this.simulator = (OTSDEVSSimulatorInterface) theSimulator;
        try
        {
            // Read the shape files with the function:
            this.nodes = ShapeFileReader.readNodes("/gis/TESTcordonnodes.shp", "NODENR", true, true);
            this.shpLinks = new HashMap<>();
            ShapeFileReader.readLinks("/gis/TESTcordonlinks_aangevuld.shp", this.shpLinks, this.nodes, this.simulator);

            this.simulator.scheduleEventAbs(new DoubleScalar.Abs<TimeUnit>(0.0, TimeUnit.SECOND), this, this,
                "ntmFlowTimestep", null);
        }
        catch (Throwable exception)
        {
            exception.printStackTrace();
        }
    }

    /** {@inheritDoc} */
    @Override
    public final SimulatorInterface<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> getSimulator()
        throws RemoteException
    {
        return this.simulator;
    }

}
