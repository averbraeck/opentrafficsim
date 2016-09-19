package org.opentrafficsim.imb.transceiver.urbanstrategy;

import nl.tno.imb.TByteBuffer;
import nl.tudelft.simulation.dsol.SimRuntimeException;

import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.graphs.AbstractOTSPlot;
import org.opentrafficsim.imb.IMBException;
import org.opentrafficsim.imb.connector.Connector;
import org.opentrafficsim.imb.connector.Connector.IMBEventType;
import org.opentrafficsim.imb.transceiver.AbstractTransceiver;
import org.opentrafficsim.simulationengine.SimpleSimulatorInterface;

/**
 * OTS publishes events about the created graphs to IMB.<br>
 * At the start of the OTS simulation, or when a graph is added later, a NEW message is sent to IMB. The CHANGE message is
 * posted whenever a graph is update (currently this happens every 10 seconds of simulation time). When a graph is removed a
 * DELETE event should be posted, but this is not yet happening as graphs cannot currently be deleted.
 * <p>
 * <style>table,th,td {border:1px solid grey; border-style:solid; text-align:left; border-collapse: collapse;}</style>
 * <h2>NEW</h2>
 * <table summary="" style="width:800px;">
 * <thead>
 * <tr>
 * <th style="width:25%;">Variable</th>
 * <th style="width:15%;">Type</th>
 * <th style="width:60%;">Comments</th>
 * </tr>
 * </thead><tbody>
 * <tr>
 * <td>timestamp</td>
 * <td>double</td>
 * <td>time of the event, in simulation time seconds</td>
 * </tr>
 * <tr>
 * <td>networkId</td>
 * <td>String</td>
 * <td>Id of the Network where the Link resides</td>
 * </tr>
 * </tbody>
 * </table>
 * </p>
 * <p>
 * <h2>CHANGE</h2>
 * <table summary="" style="width:800px;">
 * <thead>
 * <tr>
 * <th style="width:25%;">Variable</th>
 * <th style="width:15%;">Type</th>
 * <th style="width:60%;">Comments</th>
 * </tr>
 * </thead><tbody>
 * <tr>
 * <td>timestamp</td>
 * <td>double</td>
 * <td>time of the event, in simulation time seconds</td>
 * </tr>
 * <tr>
 * <td>networkId</td>
 * <td>String</td>
 * <td>Id of the Network where the Lane resides</td>
 * </tr>
 * <tr>
 * <td>image data</td>
 * <td>byte[]</td>
 * <td>image data is encoded according to the PNG standard</td>
 * </tr>
 * </tbody>
 * </table>
 * </p>
 * <p>
 * <h2>DELETE</h2>
 * <table summary="" style="width:800px;">
 * <thead>
 * <tr>
 * <th style="width:25%;">Variable</th>
 * <th style="width:15%;">Type</th>
 * <th style="width:60%;">Comments</th>
 * </tr>
 * </thead><tbody>
 * <tr>
 * <td>timestamp</td>
 * <td>double</td>
 * <td>time of the event, in simulation time seconds</td>
 * </tr>
 * <tr>
 * <td>networkId</td>
 * <td>String</td>
 * <td>Id of the Network where the Link resides</td>
 * </tr>
 * </tbody>
 * </table>
 * <br>
 * TODO: all messages currently have the graph type and lane information encoded in the name. This should be part of the IMB
 * payload. This name contains spaces and that is asking for problems.
 * </p>
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Sep 16, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class GraphTransceiver extends AbstractTransceiver
{

    /** */
    private static final long serialVersionUID = 20160919L;

    /** The interval between generation of graphs. */
    static final Duration INTERVAL = new Duration(10, TimeUnit.SECOND);

    /** The Network. */
    private final Network network;

    /**
     * Construct a new GraphTransceiver.
     * @param connector Connector; the IMB connector
     * @param simulator SimpleSimulatorInterface; the simulator
     * @param network Network; the network
     * @param plot AbstractOTSPlot; the graph
     */
    public GraphTransceiver(final Connector connector, SimpleSimulatorInterface simulator, Network network,
            final AbstractOTSPlot plot)
    {
        super("Graph." + plot.getName(), connector, simulator);
        this.network = network;
        try
        {
            getConnector().postIMBMessage("Graph." + plot.getCaption(), IMBEventType.NEW,
                    new Object[] { getSimulator().getSimulatorTime().getTime().si, this.network.getId() });
        }
        catch (IMBException exception1)
        {
            exception1.printStackTrace();
        }
        // TODO figure out a way to send IMBEventType.DELETE when we die or a graph is removed (not yet possible)
        try
        {
            simulator.scheduleEventRel(INTERVAL, this, this, "makePNG", new Object[] { plot });
        }
        catch (SimRuntimeException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * @param plot
     */
    public void makePNG(final AbstractOTSPlot plot)
    {
        byte[] payload = plot.generatePNG();
        // try
        // {
        // FileOutputStream stream = new FileOutputStream("d:/plaatje.png");
        // try
        // {
        // stream.write(payload);
        // }
        // finally
        // {
        // stream.close();
        // }
        // }
        // catch (FileNotFoundException exception2)
        // {
        // exception2.printStackTrace();
        // }
        // catch (IOException exception2)
        // {
        // exception2.printStackTrace();
        // }
        try
        {
            getConnector().postIMBMessage(
                    "Graph." + plot.getCaption(),
                    IMBEventType.CHANGE,
                    new Object[] { getSimulator().getSimulatorTime().getTime().si, this.network.getId(),
                            new TByteBuffer(payload) });
        }
        catch (IMBException exception1)
        {
            exception1.printStackTrace();
        }
        try
        {
            getSimulator().scheduleEventRel(INTERVAL, this, this, "makePNG", new Object[] { plot });
        }
        catch (SimRuntimeException exception)
        {
            exception.printStackTrace();
        }
    }
}
