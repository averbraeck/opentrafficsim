package org.opentrafficsim.demo.geometry.shape;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.dsol.AbstractOTSModel;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.lane.OTSRoadNode;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Sep 9, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 */
public class ShapeModel extends AbstractOTSModel
{
    /** */
    private static final long serialVersionUID = 20140815L;

    /** Nodes from shape file. */
    private Map<String, OTSRoadNode> nodes;

    /** Links from shape file. */
    private Map<String, Link> shpLinks;

    /** the network. */
    private OTSRoadNetwork network = new OTSRoadNetwork("shape model network", true, getSimulator());

    /**
     * @param simulator OTSSimulatorInterface; the simulator
     */
    public ShapeModel(final OTSSimulatorInterface simulator)
    {
        super(simulator);
    }

    /** {@inheritDoc} */
    @Override
    public final void constructModel() throws SimRuntimeException
    {
        try
        {
            // Read the shape files with the function:
            this.nodes = ShapeFileReader.readNodes(network, "/gis/TESTcordonnodes.shp", "NODENR", true, true);
            this.shpLinks = new LinkedHashMap<>();
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
    public final OTSRoadNetwork getNetwork()
    {
        return this.network;
    }

    /** {@inheritDoc} */
    @Override
    public Serializable getSourceId()
    {
        return "ShapeModel";
    }

}
