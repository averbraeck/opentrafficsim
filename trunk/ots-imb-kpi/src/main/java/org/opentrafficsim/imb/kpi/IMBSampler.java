package org.opentrafficsim.imb.kpi;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.imb.IMBException;
import org.opentrafficsim.imb.connector.Connector;
import org.opentrafficsim.imb.connector.IMBConnector;
import org.opentrafficsim.imb.transceiver.Transceiver;
import org.opentrafficsim.kpi.interfaces.GtuTypeDataInterface;
import org.opentrafficsim.kpi.sampling.KpiGtuDirectionality;
import org.opentrafficsim.kpi.sampling.KpiLaneDirection;
import org.opentrafficsim.kpi.sampling.Query;
import org.opentrafficsim.kpi.sampling.Sampler;
import org.opentrafficsim.kpi.sampling.meta.MetaDataGtuType;
import org.opentrafficsim.kpi.sampling.meta.MetaDataSet;

import nl.tno.imb.TByteBuffer;
import nl.tno.imb.TEventEntry;
import nl.tudelft.simulation.language.d3.CartesianPoint;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 12 okt. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */

public class IMBSampler extends Sampler
{
    /** The IMBConnector. */
    private final IMBConnector imbConnector;

    /** The last received timestamp. */
    private Time lastTimestamp = Time.ZERO;

    /** The recording start times per KpiLaneDirection. */
    private final Map<KpiLaneDirection, Time> startRecordingMap = new HashMap<>();

    /** The recording stop times per KpiLaneDirection. */
    private final Map<KpiLaneDirection, Time> stopRecordingMap = new HashMap<>();

    /** the nodes. */
    protected final Map<String, NodeData> nodes = new HashMap<>();

    /** the links. */
    protected final Map<String, LinkData> links = new HashMap<>();

    /** the lanes. */
    protected final Map<String, LaneData> lanes = new HashMap<>();

    /** the gtus. */
    protected final Map<String, GtuData> gtus = new HashMap<>();

    /** last lane of gtus. */
    protected final Map<String, KpiLaneDirection> lastLanes = new HashMap<>();

    /** the default gtu type (for now). */
    protected final GtuTypeData defaultGtuType;

    /** the default route (for now). */
    protected final RouteData defaultRoute;

    /**
     * Main program for IMBSampler. Listens to events on the IMB bus and calculates and publishes statistics.
     * @param args the arguments with [0]=IP address, [1]=port
     * @throws IMBException in case of invalid arguments
     */
    public static void main(final String[] args) throws IMBException
    {
        if (args.length == 0)
        {
            new IMBSampler("localhost", 4000, "OTS_IMB_KPI", 1, "OTS_RT");
        }
        else
        {
            if (args.length != 5)
            {
                throw new IMBException("Use as follows: java -jar IMBSampler host port model modelId federation");
            }
            String host = args[0];
            int port = Integer.valueOf(args[1]);
            String modelName = args[2];
            int modelId = Integer.valueOf(args[3]);
            String federation = args[4];
            new IMBSampler(host, port, modelName, modelId, federation);
        }
    }

    /**
     * Constructor which starts the IMB connection, with listeners for nodes, links, lanes and gtu's.
     * @param host String; name of the IMB hub
     * @param port int; port number of the IMB hub
     * @param modelName String; local model name
     * @param modelId int; model id
     * @param federation String; federation on the IMB hub
     * @throws IMBException when a connection to the IMB hub could not be established
     */
    public IMBSampler(final String host, final int port, final String modelName, final int modelId, final String federation)
            throws IMBException
    {
        this.imbConnector = new IMBConnector(host, port, modelName, modelId, federation);

        // default GTU Type and default route
        this.defaultGtuType = new GtuTypeData("car");
        NodeData nodeA = new NodeData("NodeA", new CartesianPoint(0, 0, 0));
        NodeData nodeB = new NodeData("NodeB", new CartesianPoint(1, 1, 0));
        this.nodes.put(nodeA.getNodeName(), nodeA);
        this.nodes.put(nodeB.getNodeName(), nodeB);
        this.defaultRoute = new RouteData("Route A-B", nodeA, nodeB);

        Transceiver nodeTransceiver = new NodeTransceiver(this, this.imbConnector);
        this.imbConnector.register(nodeTransceiver.getId(), nodeTransceiver);

        Transceiver linkTransceiver = new LinkTransceiver(this, this.imbConnector);
        this.imbConnector.register(linkTransceiver.getId(), linkTransceiver);

        Transceiver laneTransceiver = new LaneTransceiver(this, this.imbConnector);
        this.imbConnector.register(laneTransceiver.getId(), laneTransceiver);

        Transceiver gtuTransceiver = new GTUTransceiver(this, this.imbConnector);
        this.imbConnector.register(gtuTransceiver.getId(), gtuTransceiver);

    }

    /**
     * @return query covering the entire N201
     */
    private Query getQuery()
    {
        // String[] southBound = new String[] { "L1a", "L2a", "L3a4a", "L5a", "L6a", "L7a", "L8a9a", "L10a11a", "L12a",
        // "L13a14a",
        // "L15a16a", "L17a", "L18a19a", "L20a21a", "L22a", "L23a24a", "L25a", "L26a", "L27a", "L28a29a", "L30a", "L31a",
        // "L32a", "L33a", "L34a", "L35a", "L36a", "L37a", "L38a", "L39a", "L40a", "L41a", "L42a", "L43a", "L44a", "L45a",
        // "L46a", "L47a48a", "L49a" };
        String[] southBound = new String[] { "L2a" };
        String[] northBound = new String[] { "L49b", "L48b47b", "L46b", "L45b", "L44b", "L43b", "L42b", "L41b", "L40b", "L39b",
                "L38b", "L37b", "L36b", "L35b", "L34b", "L33b", "L32b", "L31b", "L30b", "L29b28b", "L27b", "L26b", "L25b",
                "L24b23b", "L22b21b", "L20b", "L19b18b", "L17b16b", "L15b", "L14b13b", "L12b", "L11b", "L10b", "L9b8b", "L7b",
                "L6b", "L5b", "L4b3b", "L2b", "L1b" };
        MetaDataSet metaDataSet = new MetaDataSet();
        Set<GtuTypeDataInterface> gtuTypes = new HashSet<>();
        gtuTypes.add(new GtuTypeData("car"));
        gtuTypes.add(new GtuTypeData("bus"));
        metaDataSet.put(new MetaDataGtuType("gtuType"), gtuTypes);
        Query query = new Query(this, "N201 both directions", metaDataSet, new Frequency(2.0, FrequencyUnit.PER_MINUTE));
        // addSpaceTimeRegions(query, northBound);
        addSpaceTimeRegions(query, southBound);
        return query;
    }

    /**
     * @param query query
     * @param network network
     * @param links link names
     */
    private void addSpaceTimeRegions(final Query query, final String[] links)
    {
        for (String link : links)
        {
            query.addSpaceTimeRegionLink(this.links.get(link), KpiGtuDirectionality.DIR_PLUS, new Length(0.0, LengthUnit.SI),
                    this.links.get(link).getLength(), new Time(0.0, TimeUnit.SI), new Time(1.0, TimeUnit.HOUR));
        }
    }

    /** {@inheritDoc} */
    @Override
    public final Time now()
    {
        return this.lastTimestamp;
    }

    /** {@inheritDoc} */
    @Override
    public final void scheduleStartRecording(final Time time, final KpiLaneDirection kpiLaneDirection)
    {
        // store the start time in the internal map to indicate from which time we want to consider events.
        this.startRecordingMap.put(kpiLaneDirection, time);
    }

    /** {@inheritDoc} */
    @Override
    public final void scheduleStopRecording(final Time time, final KpiLaneDirection kpiLaneDirection)
    {
        // store the stop time in the internal map to indicate from which time we want to consider events.
        this.stopRecordingMap.put(kpiLaneDirection, time);
    }

    /** {@inheritDoc} */
    @Override
    public final void initRecording(final KpiLaneDirection kpiLaneDirection)
    {
        // Nothing to do -- we get data on all GTUs
    }

    /** {@inheritDoc} */
    @Override
    public final void finalizeRecording(final KpiLaneDirection kpiLaneDirection)
    {
        // Nothing to do -- we get data on all GTUs
    }

    /**
     * Sample the data received from a GTU.
     * @param timeStamp
     * @param gtuId
     * @param laneId
     * @param forward
     * @param longitudinalPosition
     * @param speed
     * @param acceleration
     */
    protected void sample(double timeStamp, String gtuId, String laneId, boolean forward, double longitudinalPosition,
            double speed, double acceleration)
    {
        // update clock
        updateClock(timeStamp);
        if (!this.lanes.containsKey(laneId))
        {
            // lane not part of this network
            return;
        }
        KpiLaneDirection kpiLaneDirection = new KpiLaneDirection(this.lanes.get(laneId),
                forward ? KpiGtuDirectionality.DIR_PLUS : KpiGtuDirectionality.DIR_MINUS);
        GtuData gtu = this.gtus.get(gtuId);
        if (this.lastLanes.containsKey(gtuId) && contains(this.lastLanes.get(gtuId))
                && !this.lastLanes.get(gtuId).equals(kpiLaneDirection))
        {
            processGtuRemoveEvent(this.lastLanes.get(gtuId), gtu);
        }
        if ((!this.lastLanes.containsKey(gtuId) || !this.lastLanes.get(gtuId).equals(kpiLaneDirection))
                && contains(kpiLaneDirection))
        {
            processGtuAddEvent(kpiLaneDirection, new Length(longitudinalPosition, LengthUnit.SI),
                    new Speed(speed, SpeedUnit.SI), new Acceleration(acceleration, AccelerationUnit.SI), now(), gtu);
        }
        else if (contains(kpiLaneDirection))
        {
            // move on current
            processGtuMoveEvent(kpiLaneDirection, new Length(longitudinalPosition, LengthUnit.SI),
                    new Speed(speed, SpeedUnit.SI), new Acceleration(acceleration, AccelerationUnit.SI), now(), gtu);
        }
        this.lastLanes.put(gtuId, kpiLaneDirection);
    }

    /** trigger query only once. */
    private boolean queryObtained = false;

    /**
     * Updates clock and triggers timed events.
     * @param timeStamp most recent time stamp
     */
    protected void updateClock(double timeStamp)
    {
        if (!this.queryObtained)
        {
            getQuery();
            this.queryObtained = true;
        }
        if (this.lastTimestamp.si >= timeStamp)
        {
            return;
        }
        this.lastTimestamp = new Time(timeStamp, TimeUnit.SI);
        Iterator<KpiLaneDirection> iterator = this.startRecordingMap.keySet().iterator();
        while (iterator.hasNext())
        {
            KpiLaneDirection kpiLaneDirection = iterator.next();
            if (now().ge(this.startRecordingMap.get(kpiLaneDirection)))
            {
                System.out.println(
                        "IMB sampler: started recording on lane " + ((LaneData) kpiLaneDirection.getLaneData()).getLaneName());
                startRecording(kpiLaneDirection);
            }
            iterator.remove();
        }
        iterator = this.stopRecordingMap.keySet().iterator();
        while (iterator.hasNext())
        {
            KpiLaneDirection kpiLaneDirection = iterator.next();
            if (now().ge(this.stopRecordingMap.get(kpiLaneDirection)))
            {
                System.out.println(
                        "IMB sampler: stopped recording on lane " + ((LaneData) kpiLaneDirection.getLaneData()).getLaneName());
                stopRecording(kpiLaneDirection);
            }
            iterator.remove();
        }
    }

    /* ************************************************************************************************************** */
    /* ********************************************** GTU LISTENER ************************************************** */
    /* ************************************************************************************************************** */

    /** the IMB GTU listener. */
    private static class GTUTransceiver implements Transceiver
    {
        /** the sampler. */
        private final IMBSampler sampler;

        /** The IMBConnector. */
        private final IMBConnector imbConnector;

        /**
         * @param sampler the sampler
         * @param imbConnector the connector
         */
        public GTUTransceiver(final IMBSampler sampler, final IMBConnector imbConnector)
        {
            this.imbConnector = imbConnector;
            this.sampler = sampler;
        }

        /** {@inheritDoc} */
        @Override
        @SuppressWarnings("unused")
        public void handleMessageFromIMB(String imbEventName, TByteBuffer imbPayload) throws IMBException
        {
            int imbEventTypeNr = imbPayload.readInt32();
            switch (imbEventTypeNr)
            {
                case TEventEntry.ACTION_NEW:
                {
                    double timeStamp = imbPayload.readDouble();
                    String gtuId = imbPayload.readString();
                    double x = imbPayload.readDouble();
                    double y = imbPayload.readDouble();
                    double z = imbPayload.readDouble();
                    double rotZ = imbPayload.readDouble();
                    String networkId = imbPayload.readString();
                    String linkId = imbPayload.readString();
                    // TODO laneId should be unique on its own
                    String laneId = linkId + "." + imbPayload.readString();
                    double longitudinalPosition = imbPayload.readDouble();
                    double length = imbPayload.readDouble();
                    double width = imbPayload.readDouble();
                    byte r = imbPayload.readByte();
                    byte g = imbPayload.readByte();
                    byte b = imbPayload.readByte();

                    // TODO GTU Type and Route should be part of the NEW message
                    GtuData gtuData = new GtuData(gtuId, this.sampler.defaultGtuType, this.sampler.defaultRoute);
                    this.sampler.gtus.put(gtuId, gtuData);
                    break;
                }

                case TEventEntry.ACTION_CHANGE:
                {
                    double timeStamp = imbPayload.readDouble();
                    String gtuId = imbPayload.readString();
                    double x = imbPayload.readDouble();
                    double y = imbPayload.readDouble();
                    double z = imbPayload.readDouble();
                    double rotZ = imbPayload.readDouble();
                    String networkId = imbPayload.readString();
                    String linkId = imbPayload.readString();
                    // TODO laneId should be unique on its own
                    String laneId = linkId + "." + imbPayload.readString();
                    double longitudinalPosition = imbPayload.readDouble();
                    double speed = imbPayload.readDouble();
                    double acceleration = imbPayload.readDouble();
                    String turnIndicatorStatus = imbPayload.readString();
                    boolean brakingLights = imbPayload.readBoolean();
                    double odometer = imbPayload.readDouble();
                    boolean forward = true;

                    this.sampler.sample(timeStamp, gtuId, laneId, forward, longitudinalPosition, speed, acceleration);

                    break;
                }

                case TEventEntry.ACTION_DELETE:
                {
                    // ignore for now.
                    break;
                }

                default:
                    break;
            }
        }

        @Override
        public String getId()
        {
            return "GTU";
        }

        @Override
        public Connector getConnector()
        {
            return this.imbConnector;
        }
    }

    /* ************************************************************************************************************** */
    /* ********************************************* NODE LISTENER ************************************************** */
    /* ************************************************************************************************************** */

    /** the IMB Node listener. */
    private static class NodeTransceiver implements Transceiver
    {
        /** the sampler. */
        private final IMBSampler sampler;

        /** The IMBConnector. */
        private final IMBConnector imbConnector;

        /**
         * @param sampler the sampler
         * @param imbConnector the connector
         */
        public NodeTransceiver(final IMBSampler sampler, final IMBConnector imbConnector)
        {
            this.imbConnector = imbConnector;
            this.sampler = sampler;
        }

        /** {@inheritDoc} */
        @Override
        @SuppressWarnings("unused")
        public void handleMessageFromIMB(String imbEventName, TByteBuffer imbPayload) throws IMBException
        {
            int imbEventTypeNr = imbPayload.readInt32();
            switch (imbEventTypeNr)
            {
                case TEventEntry.ACTION_NEW:
                {
                    double timeStamp = imbPayload.readDouble();
                    String networkId = imbPayload.readString();
                    String nodeId = imbPayload.readString();
                    double x = imbPayload.readDouble();
                    double y = imbPayload.readDouble();
                    double z = imbPayload.readDouble();
                    CartesianPoint p = new CartesianPoint(x, y, z);
                    NodeData nodeData = new NodeData(nodeId, p);
                    this.sampler.nodes.put(nodeId, nodeData);
                    break;
                }

                case TEventEntry.ACTION_CHANGE:
                {
                    // ignore for now.
                    break;
                }

                case TEventEntry.ACTION_DELETE:
                {
                    // ignore for now.
                    break;
                }

                default:
                    break;
            }
        }

        @Override
        public String getId()
        {
            return "Node";
        }

        @Override
        public Connector getConnector()
        {
            return this.imbConnector;
        }
    }

    /* ************************************************************************************************************** */
    /* ********************************************* LINK LISTENER ************************************************** */
    /* ************************************************************************************************************** */

    /** the IMB Link listener. */
    private static class LinkTransceiver implements Transceiver
    {
        /** the sampler. */
        private final IMBSampler sampler;

        /** The IMBConnector. */
        private final IMBConnector imbConnector;

        /**
         * @param sampler the sampler
         * @param imbConnector the connector
         */
        public LinkTransceiver(final IMBSampler sampler, final IMBConnector imbConnector)
        {
            this.imbConnector = imbConnector;
            this.sampler = sampler;
        }

        /** {@inheritDoc} */
        @Override
        @SuppressWarnings("unused")
        public void handleMessageFromIMB(String imbEventName, TByteBuffer imbPayload) throws IMBException
        {
            int imbEventTypeNr = imbPayload.readInt32();
            switch (imbEventTypeNr)
            {
                case TEventEntry.ACTION_NEW:
                {
                    double timeStamp = imbPayload.readDouble();
                    String networkId = imbPayload.readString();
                    String linkId = imbPayload.readString();
                    String startNodeId = imbPayload.readString();
                    String endNodeId = imbPayload.readString();
                    int dlNumPoints = imbPayload.readInt32();
                    double len = 0.0;
                    double x = imbPayload.readDouble();
                    double y = imbPayload.readDouble();
                    double z = imbPayload.readDouble();
                    CartesianPoint p1 = new CartesianPoint(x, y, z);
                    for (int i = 1; i < dlNumPoints; i++)
                    {
                        x = imbPayload.readDouble();
                        y = imbPayload.readDouble();
                        z = imbPayload.readDouble();
                        CartesianPoint p2 = new CartesianPoint(x, y, z);
                        len += p1.distance(p2);
                        p1 = p2;
                    }
                    Length length = new Length(len, LengthUnit.SI);
                    LinkData linkData = new LinkData(linkId, this.sampler.nodes.get(startNodeId),
                            this.sampler.nodes.get(endNodeId), length);
                    this.sampler.links.put(linkId, linkData);
                    break;
                }

                case TEventEntry.ACTION_CHANGE:
                {
                    // ignore for now.
                    break;
                }

                case TEventEntry.ACTION_DELETE:
                {
                    // ignore for now.
                    break;
                }

                default:
                    break;
            }
        }

        @Override
        public String getId()
        {
            return "Link_GTU";
        }

        @Override
        public Connector getConnector()
        {
            return this.imbConnector;
        }
    }

    /* ************************************************************************************************************** */
    /* ********************************************* LANE LISTENER ************************************************** */
    /* ************************************************************************************************************** */

    /** the IMB Lane listener. */
    private static class LaneTransceiver implements Transceiver
    {
        /** the sampler. */
        private final IMBSampler sampler;

        /** The IMBConnector. */
        private final IMBConnector imbConnector;

        /**
         * @param sampler the sampler
         * @param imbConnector the connector
         */
        public LaneTransceiver(final IMBSampler sampler, final IMBConnector imbConnector)
        {
            this.imbConnector = imbConnector;
            this.sampler = sampler;
        }

        /** {@inheritDoc} */
        @Override
        @SuppressWarnings("unused")
        public void handleMessageFromIMB(String imbEventName, TByteBuffer imbPayload) throws IMBException
        {
            int imbEventTypeNr = imbPayload.readInt32();
            switch (imbEventTypeNr)
            {
                case TEventEntry.ACTION_NEW:
                {
                    double timeStamp = imbPayload.readDouble();
                    String networkId = imbPayload.readString();
                    String linkId = imbPayload.readString();
                    // TODO laneId should be unique on its own
                    String laneId = linkId + "." + imbPayload.readString();
                    int laneNumber = imbPayload.readInt32();
                    int dlNumPoints = imbPayload.readInt32();
                    double len = 0.0;
                    double x = imbPayload.readDouble();
                    double y = imbPayload.readDouble();
                    double z = imbPayload.readDouble();
                    CartesianPoint p1 = new CartesianPoint(x, y, z);
                    for (int i = 1; i < dlNumPoints; i++)
                    {
                        x = imbPayload.readDouble();
                        y = imbPayload.readDouble();
                        z = imbPayload.readDouble();
                        CartesianPoint p2 = new CartesianPoint(x, y, z);
                        len += p1.distance(p2);
                        p1 = p2;
                    }
                    Length length = new Length(len, LengthUnit.SI);
                    LaneData laneData = new LaneData(this.sampler.links.get(linkId), laneId, length);
                    if (this.sampler.lanes.containsKey(laneId))
                    {
                        System.out.println("Lanes not unique.");
                    }
                    this.sampler.lanes.put(laneId, laneData);
                    break;
                }

                case TEventEntry.ACTION_CHANGE:
                {
                    // ignore for now.
                    break;
                }

                case TEventEntry.ACTION_DELETE:
                {
                    // ignore for now.
                    break;
                }

                default:
                    break;
            }
        }

        @Override
        public String getId()
        {
            return "Lane_GTU";
        }

        @Override
        public Connector getConnector()
        {
            return this.imbConnector;
        }
    }

}
