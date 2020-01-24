package org.opentrafficsim.core.graphs.imb;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.djunits.value.vdouble.scalar.Duration;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.draw.graphs.AbstractContourPlot;
import org.opentrafficsim.draw.graphs.AbstractPlot;
import org.opentrafficsim.draw.graphs.TrajectoryPlot;
import org.opentrafficsim.imb.IMBException;
import org.opentrafficsim.imb.connector.Connector;
import org.opentrafficsim.imb.connector.Connector.IMBEventType;
import org.opentrafficsim.imb.transceiver.AbstractTransceiver;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * OTS can publish graphs to IMB, e.g. trajectory graphs, flow graphs and density graphs.<br>
 * When a graph is published for the first time, a NEW message is sent to IMB to identify the graph, the image resolution, and
 * the lane(s) for which the graph is created. The CHANGE message is posted whenever an updated graph is posted. When a Graph is
 * no longer published, a DELETE event is posted. The Graph NEW messages are posted after the Network NEW, Node NEW, Link NEW,
 * and Lane NEW messages are posted, as it has to be able to identify Lanes.
 * <p>
 * <style>table,th,td {border:1px solid grey; border-style:solid; text-align:left; border-collapse: collapse;}</style>
 * <h2>NEW</h2>
 * <table style="width:800px;"><caption>&nbsp;</caption>
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
 * <td>graphId</td>
 * <td>String</td>
 * <td>a unique id for the graph, e.g. a UUID string</td>
 * </tr>
 * <tr>
 * <td>width</td>
 * <td>int</td>
 * <td>width of the graph in pixels</td>
 * </tr>
 * <tr>
 * <td>height</td>
 * <td>int</td>
 * <td>height of the graph in pixels</td>
 * </tr>
 * <tr>
 * <td>description</td>
 * <td>String</td>
 * <td>textual description of the graph</td>
 * </tr>
 * <tr>
 * <td>graphType</td>
 * <td>String</td>
 * <td>type of graph; one of TRAJECTORY, SPEED_CONTOUR, ACCELERATION_CONTOUR, DENSITY_CONTOUR, FLOW_CONTOUR,
 * FUNDAMENTAL_DIAGRAM</td>
 * </tr>
 * <tr>
 * <td>time_resolution</td>
 * <td>double</td>
 * <td>For the four types of contour graphs, and the trajectory graph, it provides the aggregation in seconds. 0.0 for other
 * types of graphs. When the value is 0.0 for the trajectory graph, the graph is updated on the basis of events from the
 * GTU.</td>
 * </tr>
 * <tr>
 * <td>value_resolution</td>
 * <td>double</td>
 * <td>For the four types of contour graphs, it provides the aggregation in the SI unit of the value. 0.0 for other types of
 * graphs</td>
 * </tr>
 * <tr>
 * <td>networkId</td>
 * <td>String</td>
 * <td>id of the Network for which the Graph is made</td>
 * </tr>
 * <tr>
 * <td>numberOfLanes</td>
 * <td>int</td>
 * <td>number of Link-Lane combinations for this Graph</td>
 * </tr>
 * <tr>
 * <td>linkId_1</td>
 * <td>String</td>
 * <td>id of the first Link; unique within the Network</td>
 * </tr>
 * <tr>
 * <td>laneId_1</td>
 * <td>String</td>
 * <td>id of the first Lane, unique within the Link</td>
 * </tr>
 * <tr>
 * <td>...</td>
 * <td>&nbsp;</td>
 * <td>&nbsp;</td>
 * </tr>
 * <tr>
 * <td>linkId_n</td>
 * <td>String</td>
 * <td>id of the last Link; unique within the Network</td>
 * </tr>
 * <tr>
 * <td>laneId_n</td>
 * <td>String</td>
 * <td>id of the last Lane, unique within the Link</td>
 * </tr>
 * <tr>
 * <td>transmissionInterval</td>
 * <td>double</td>
 * <td>transmission interval of the graph in seconds</td>
 * </tr>
 * </tbody>
 * </table>
 * </p>
 * <p>
 * <h2>CHANGE</h2>
 * <table style="width:800px;"><caption>&nbsp;</caption>
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
 * <td>graphId</td>
 * <td>String</td>
 * <td>the unique id for the graph, e.g. a UUID string</td>
 * </tr>
 * <tr>
 * <td>width</td>
 * <td>int</td>
 * <td>width of the graph in pixels</td>
 * </tr>
 * <tr>
 * <td>height</td>
 * <td>int</td>
 * <td>height of the graph in pixels</td>
 * </tr>
 * <tr>
 * <td>image data</td>
 * <td>byte[]</td>
 * <td>image in PNG format; starts with the standard 8-byte signature 89 50 4E 47 0D 0A 1A 0A.</td>
 * </tr>
 * </tbody>
 * </table>
 * </p>
 * <p>
 * <h2>DELETE</h2>
 * <table style="width:800px;"><caption>&nbsp;</caption>
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
 * <td>graphId</td>
 * <td>String</td>
 * <td>the unique id for the graph that is removed</td>
 * </tr>
 * <tr>
 * <td>width</td>
 * <td>int</td>
 * <td>width of the graph in pixels</td>
 * </tr>
 * <tr>
 * <td>height</td>
 * <td>int</td>
 * <td>height of the graph in pixels</td>
 * </tr>
 * </tbody>
 * </table>
 * <br>
 * Note: when two resolutions for the same graph are sent over the network, they have the same graphId but a different width
 * and/or height. This means that the combination of graphId, width, and height creates a unique key for the graph.
 * </p>
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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

    /** The Network for which the graph is made. */
    private final Network network;

    /** The width of the graph, in pixels. */
    private final int width;

    /** The height of the graph, in pixels. */
    private final int height;

    /** The interval between generation of graphs. */
    private final Duration transmissionInterval;

    // TODO handle the DELETE message

    /**
     * Construct a new GraphTransceiver.
     * @param connector Connector; the IMB connector
     * @param simulator OTSSimulatorInterface; the simulator
     * @param network Network; the network
     * @param width int; the width of the graph, in pixels
     * @param height int; the height of the graph, in pixels
     * @param plot AbstractPlot; the graph
     * @param transmissionInterval Duration; the interval between generation of graphs
     * @throws IMBException when the message cannot be posted, or the scheduling of the publish event fails
     */
    public GraphTransceiver(final Connector connector, OTSSimulatorInterface simulator, Network network, final int width,
            final int height, final AbstractPlot plot, final Duration transmissionInterval) throws IMBException
    {
        super("Graph", connector, simulator);
        this.network = network;
        this.width = width;
        this.height = height;
        this.transmissionInterval = transmissionInterval;

        List<Object> newMessage = new ArrayList<>();
        newMessage.add(getSimulator().getSimulatorTime().si);
        newMessage.add(plot.getId());
        newMessage.add(width);
        newMessage.add(height);
        newMessage.add(plot.getCaption());
        newMessage.add(plot.getGraphType().toString());
        if (plot instanceof TrajectoryPlot)
        {
            // WS this could be anything, including GTU move events
            Duration interval = null; // ((TrajectoryPlot<?>) plot).getSampleInterval(); // should be Duration
            newMessage.add(interval == null ? 0.0d : interval.si);
        }
        else if (plot instanceof AbstractContourPlot)
        {
            newMessage.add(((AbstractContourPlot<?>) plot).getTimeGranularity());
        }
        else
        {
            newMessage.add(0.0d);
        }
        newMessage.add(plot instanceof AbstractContourPlot ? ((AbstractContourPlot<?>) plot).getSpaceGranularity() : 0.0d);
        newMessage.add(this.network.getId());
        newMessage.add(0); // WS not all plots have a path
        // newMessage.add(plot.getPath().size());
        // for (Lane lane : plot.getPath())
        // {
        // newMessage.add(lane.getParentLink().getId());
        // newMessage.add(lane.getId());
        // }
        newMessage.add(transmissionInterval.si);

        getConnector().postIMBMessage("Graph", IMBEventType.NEW, newMessage.toArray());

        try
        {
            simulator.scheduleEventRel(this.transmissionInterval, this, this, "makePNG", new Object[] {plot});
        }
        catch (SimRuntimeException exception)
        {
            throw new IMBException(exception);
        }
    }

    /**
     * @param plot AbstractPlot; the plot to generate the PNG for
     * @throws IOException when the creation of the PNG has failed
     * @throws IMBException when the transmission of the IMB message fails
     * @throws SimRuntimeException when the scheduling of the next publish event fails
     */
    public void makePNG(final AbstractPlot plot) throws IOException, IMBException, SimRuntimeException
    {
        byte[] png = plot.encodeAsPng(this.width, this.height, 32.0);
        getConnector().postIMBMessage("Graph", IMBEventType.CHANGE,
                new Object[] {getSimulator().getSimulatorTime().si, plot.getId(), this.width, this.height, png});
        getSimulator().scheduleEventRel(this.transmissionInterval, this, this, "makePNG", new Object[] {plot});
    }

    /** {@inheritDoc} */
    @Override
    public Serializable getSourceId()
    {
        return "GraphTransceiver";
    }
}
