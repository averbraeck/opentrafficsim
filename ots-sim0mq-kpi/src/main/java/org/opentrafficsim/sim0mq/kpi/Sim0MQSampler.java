package org.opentrafficsim.sim0mq.kpi;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.kpi.sampling.KpiLaneDirection;
import org.opentrafficsim.kpi.sampling.Sampler;

/**
 * XXX ABSTRACT TEMPORARILY
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 12 okt. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public abstract class Sim0MQSampler extends Sampler
{
    /** The IMBConnector. */
    // private final IMBConnector imbConnector;

    /** Transceiver of statistics. */
    private Sim0MQKpiTransceiver sim0mqKpiTransceiver;

    /** The last received timestamp. */
    private Time lastTimestamp = Time.ZERO;

    /** The recording start times per KpiLaneDirection. */
    private final Map<KpiLaneDirection, Time> startRecordingMap = new LinkedHashMap<>();

    /** The recording stop times per KpiLaneDirection. */
    private final Map<KpiLaneDirection, Time> stopRecordingMap = new LinkedHashMap<>();

    /** the nodes. */
    protected final Map<String, NodeData> nodes = new LinkedHashMap<>();

    /** the links. */
    protected final Map<String, LinkData> links = new LinkedHashMap<>();

    /** the lanes. */
    protected final Map<String, LaneData> lanes = new LinkedHashMap<>();

    /** the gtus. */
    protected final Map<String, GtuData> gtus = new LinkedHashMap<>();

    /** last lane of gtus. */
    protected final Map<String, KpiLaneDirection> lastLanes = new LinkedHashMap<>();

    /** the default gtu type (for now). */
    // protected final GtuTypeData defaultGtuType;

    /** the default route (for now). */
    // protected final RouteData defaultRoute;

    /**
     */
    public Sim0MQSampler()
    {
        super(new LinkedHashSet<>(), new LinkedHashSet<>());
    }

    // /**
    // * Main program for IMBSampler. Listens to events on the IMB bus and calculates and publishes statistics.
    // * @param args the arguments with [0]=IP address, [1]=port
    // * @throws IMBException in case of invalid arguments
    // */
    // public static void main(final String[] args) throws IMBException
    // {
    // if (args.length == 0)
    // {
    // new Sim0MQSampler(new IMBConnector("localhost", 4000, "OTS_IMB_KPI", 1, "OTS_RT"));
    // }
    // else
    // {
    // if (args.length != 5)
    // {
    // throw new IMBException("Use as follows: java -jar IMBSampler host port model modelId federation");
    // }
    // String host = args[0];
    // int port = Integer.valueOf(args[1]);
    // String modelName = args[2];
    // int modelId = Integer.valueOf(args[3]);
    // String federation = args[4];
    // new Sim0MQSampler(new IMBConnector(host, port, modelName, modelId, federation));
    // }
    // }
    //
    // /**
    // * Constructor with listeners for nodes, links, lanes and gtu's.
    // * @param imbConnector IMB connection
    // * @throws IMBException on connection error
    // */
    // public Sim0MQSampler(IMBConnector imbConnector) throws IMBException
    // {
    // this.imbConnector = imbConnector;
    //
    // // default GTU Type and default route
    // this.defaultGtuType = new GtuTypeData("car");
    // NodeData nodeA = new NodeData("NodeA", new CartesianPoint(0, 0, 0));
    // NodeData nodeB = new NodeData("NodeB", new CartesianPoint(1, 1, 0));
    // this.nodes.put(nodeA.getNodeName(), nodeA);
    // this.nodes.put(nodeB.getNodeName(), nodeB);
    // this.defaultRoute = new RouteData("Route A-B", nodeA, nodeB);
    //
    // Transceiver nodeTransceiver = new NodeTransceiver(this, this.imbConnector);
    // this.imbConnector.register(nodeTransceiver.getId(), nodeTransceiver);
    //
    // Transceiver linkTransceiver = new LinkTransceiver(this, this.imbConnector);
    // this.imbConnector.register(linkTransceiver.getId(), linkTransceiver);
    //
    // Transceiver laneTransceiver = new LaneTransceiver(this, this.imbConnector);
    // this.imbConnector.register(laneTransceiver.getId(), laneTransceiver);
    //
    // Transceiver gtuTransceiver = new GTUTransceiver(this, this.imbConnector);
    // this.imbConnector.register(gtuTransceiver.getId(), gtuTransceiver);
    // }
    //
    // /** {@inheritDoc} */
    // @Override
    // public final Time now()
    // {
    // return this.lastTimestamp;
    // }
    //
    // /** {@inheritDoc} */
    // @Override
    // public final void scheduleStartRecording(final Time time, final KpiLaneDirection kpiLaneDirection)
    // {
    // // store the start time in the internal map to indicate from which time we want to consider events.
    // this.startRecordingMap.put(kpiLaneDirection, time);
    // }
    //
    // /** {@inheritDoc} */
    // @Override
    // public final void scheduleStopRecording(final Time time, final KpiLaneDirection kpiLaneDirection)
    // {
    // // store the stop time in the internal map to indicate from which time we want to consider events.
    // this.stopRecordingMap.put(kpiLaneDirection, time);
    // }
    //
    // /** {@inheritDoc} */
    // @Override
    // public final void initRecording(final KpiLaneDirection kpiLaneDirection)
    // {
    // // Nothing to do -- we get data on all GTUs
    // }
    //
    // /** {@inheritDoc} */
    // @Override
    // public final void finalizeRecording(final KpiLaneDirection kpiLaneDirection)
    // {
    // // Nothing to do -- we get data on all GTUs
    // }
    //
    // /**
    // * Sample the data received from a GTU.
    // * @param timeStamp time of the sampling
    // * @param gtuId gtu
    // * @param laneId lane
    // * @param forward driving direction
    // * @param longitudinalPosition position
    // * @param speed speed
    // * @param acceleration acceleration
    // */
    // protected void sample(double timeStamp, String gtuId, String laneId, boolean forward, double longitudinalPosition,
    // double speed, double acceleration)
    // {
    // // update clock
    // updateClock(timeStamp);
    // if (!this.lanes.containsKey(laneId) || !this.gtus.containsKey(gtuId))
    // {
    // // lane not part of this network, or new message of gtu not (yet) received
    // return;
    // }
    // KpiLaneDirection kpiLaneDirection = new KpiLaneDirection(this.lanes.get(laneId),
    // forward ? KpiGtuDirectionality.DIR_PLUS : KpiGtuDirectionality.DIR_MINUS);
    // GtuData gtu = this.gtus.get(gtuId);
    // if (this.lastLanes.containsKey(gtuId) && contains(this.lastLanes.get(gtuId))
    // && !this.lastLanes.get(gtuId).equals(kpiLaneDirection))
    // {
    // processGtuRemoveEvent(this.lastLanes.get(gtuId), gtu);
    // }
    // if ((!this.lastLanes.containsKey(gtuId) || !this.lastLanes.get(gtuId).equals(kpiLaneDirection))
    // && contains(kpiLaneDirection))
    // {
    // processGtuAddEvent(kpiLaneDirection, new Length(longitudinalPosition, LengthUnit.SI),
    // new Speed(speed, SpeedUnit.SI), new Acceleration(acceleration, AccelerationUnit.SI), now(), gtu);
    // }
    // else if (contains(kpiLaneDirection))
    // {
    // // TEST LOOP
    // for (Trajectory trajectory : getTrajectoryGroup(kpiLaneDirection).getTrajectories())
    // {
    // if (trajectory.getGtuId().equals(gtu.getId()))
    // {
    // float[] x = trajectory.getX();
    // float pos = (float) (kpiLaneDirection.getKpiDirection().isPlus() ? longitudinalPosition
    // : kpiLaneDirection.getLaneData().getLength().si - longitudinalPosition);
    // if (pos < x[x.length - 1])
    // {
    // System.err.println("Vehicle " + gtu.getId() + " is moving backwards on lane "
    // + kpiLaneDirection.getLaneData() + " or direction on lane is incorrect.");
    // }
    // break;
    // }
    // }
    // // END TEST LOOP
    // // move on current
    // processGtuMoveEvent(kpiLaneDirection, new Length(longitudinalPosition, LengthUnit.SI),
    // new Speed(speed, SpeedUnit.SI), new Acceleration(acceleration, AccelerationUnit.SI), now(), gtu);
    // }
    // this.lastLanes.put(gtuId, kpiLaneDirection);
    // }
    //
    // /**
    // * @param imbKpiTransceiver set imbKpiTransceiver.
    // */
    // public void setImbKpiTransceiver(Sim0MQKpiTransceiver imbKpiTransceiver)
    // {
    // this.imbKpiTransceiver = imbKpiTransceiver;
    // }
    //
    // /**
    // * Updates clock and triggers timed events.
    // * @param timeStamp most recent time stamp
    // */
    // protected void updateClock(double timeStamp)
    // {
    // if (this.lastTimestamp.si >= timeStamp && this.lastTimestamp.si > 0)
    // {
    // return;
    // }
    // if (this.imbKpiTransceiver != null)
    // {
    // this.imbKpiTransceiver.notifyTime(now());
    // }
    // this.lastTimestamp = new Time(timeStamp, TimeUnit.SI);
    // Iterator<KpiLaneDirection> iterator = this.startRecordingMap.keySet().iterator();
    // while (iterator.hasNext())
    // {
    // KpiLaneDirection kpiLaneDirection = iterator.next();
    // if (now().ge(this.startRecordingMap.get(kpiLaneDirection)))
    // {
    // startRecording(kpiLaneDirection);
    // iterator.remove();
    // }
    // }
    // iterator = this.stopRecordingMap.keySet().iterator();
    // while (iterator.hasNext())
    // {
    // KpiLaneDirection kpiLaneDirection = iterator.next();
    // if (now().ge(this.stopRecordingMap.get(kpiLaneDirection)))
    // {
    // stopRecording(kpiLaneDirection);
    // iterator.remove();
    // }
    // }
    // }
    //
    // /* ************************************************************************************************************** */
    // /* ********************************************** GTU LISTENER ************************************************** */
    // /* ************************************************************************************************************** */
    //
    // /** the IMB GTU listener. */
    // private static class GTUTransceiver implements Transceiver
    // {
    // /** the sampler. */
    // private final Sim0MQSampler sampler;
    //
    // /** The IMBConnector. */
    // private final IMBConnector imbConnector;
    //
    // /**
    // * @param sampler the sampler
    // * @param imbConnector the connector
    // */
    // public GTUTransceiver(final Sim0MQSampler sampler, final IMBConnector imbConnector)
    // {
    // this.imbConnector = imbConnector;
    // this.sampler = sampler;
    // }
    //
    // /** {@inheritDoc} */
    // @Override
    // @SuppressWarnings("unused")
    // public void handleMessageFromIMB(String imbEventName, TByteBuffer imbPayload) throws IMBException
    // {
    // int imbEventTypeNr = imbPayload.readInt32();
    // switch (imbEventTypeNr)
    // {
    // case TEventEntry.ACTION_NEW:
    // {
    // double timeStamp = imbPayload.readDouble();
    // String gtuId = imbPayload.readString();
    // double x = imbPayload.readDouble();
    // double y = imbPayload.readDouble();
    // double z = imbPayload.readDouble();
    // double rotZ = imbPayload.readDouble();
    // String networkId = imbPayload.readString();
    // String linkId = imbPayload.readString();
    // // TODO laneId should be unique on its own
    // String laneId = linkId + "." + imbPayload.readString();
    // double longitudinalPosition = imbPayload.readDouble();
    // double length = imbPayload.readDouble();
    // double width = imbPayload.readDouble();
    // byte r = imbPayload.readByte();
    // byte g = imbPayload.readByte();
    // byte b = imbPayload.readByte();
    //
    // // TODO GTU Type and Route should be part of the NEW message
    // GtuData gtuData = new GtuData(gtuId, this.sampler.defaultGtuType, this.sampler.defaultRoute);
    // this.sampler.gtus.put(gtuId, gtuData);
    // break;
    // }
    //
    // case TEventEntry.ACTION_CHANGE:
    // {
    // double timeStamp = imbPayload.readDouble();
    // String gtuId = imbPayload.readString();
    // double x = imbPayload.readDouble();
    // double y = imbPayload.readDouble();
    // double z = imbPayload.readDouble();
    // double rotZ = imbPayload.readDouble();
    // String networkId = imbPayload.readString();
    // String linkId = imbPayload.readString();
    // // TODO laneId should be unique on its own
    // String laneId = linkId + "." + imbPayload.readString();
    // double longitudinalPosition = imbPayload.readDouble();
    // double speed = imbPayload.readDouble();
    // double acceleration = imbPayload.readDouble();
    // String turnIndicatorStatus = imbPayload.readString();
    // boolean brakingLights = imbPayload.readBoolean();
    // double odometer = imbPayload.readDouble();
    // boolean forward = true;
    //
    // this.sampler.sample(timeStamp, gtuId, laneId, forward, longitudinalPosition, speed, acceleration);
    //
    // break;
    // }
    //
    // case TEventEntry.ACTION_DELETE:
    // {
    // // ignore for now.
    // break;
    // }
    //
    // default:
    // break;
    // }
    // }
    //
    // @Override
    // public String getId()
    // {
    // return "GTU";
    // }
    //
    // @Override
    // public Connector getConnector()
    // {
    // return this.imbConnector;
    // }
    // }
    //
    // /* ************************************************************************************************************** */
    // /* ********************************************* NODE LISTENER ************************************************** */
    // /* ************************************************************************************************************** */
    //
    // /** the IMB Node listener. */
    // private static class NodeTransceiver implements Transceiver
    // {
    // /** the sampler. */
    // private final Sim0MQSampler sampler;
    //
    // /** The IMBConnector. */
    // private final IMBConnector imbConnector;
    //
    // /**
    // * @param sampler the sampler
    // * @param imbConnector the connector
    // */
    // public NodeTransceiver(final Sim0MQSampler sampler, final IMBConnector imbConnector)
    // {
    // this.imbConnector = imbConnector;
    // this.sampler = sampler;
    // }
    //
    // /** {@inheritDoc} */
    // @Override
    // @SuppressWarnings("unused")
    // public void handleMessageFromIMB(String imbEventName, TByteBuffer imbPayload) throws IMBException
    // {
    // int imbEventTypeNr = imbPayload.readInt32();
    // switch (imbEventTypeNr)
    // {
    // case TEventEntry.ACTION_NEW:
    // {
    // double timeStamp = imbPayload.readDouble();
    // String networkId = imbPayload.readString();
    // String nodeId = imbPayload.readString();
    // System.out.println("Node " + nodeId + " received.");
    // double x = imbPayload.readDouble();
    // double y = imbPayload.readDouble();
    // double z = imbPayload.readDouble();
    // CartesianPoint p = new CartesianPoint(x, y, z);
    // NodeData nodeData = new NodeData(nodeId, p);
    // this.sampler.nodes.put(nodeId, nodeData);
    // break;
    // }
    //
    // case TEventEntry.ACTION_CHANGE:
    // {
    // // ignore for now.
    // break;
    // }
    //
    // case TEventEntry.ACTION_DELETE:
    // {
    // // ignore for now.
    // break;
    // }
    //
    // default:
    // break;
    // }
    // }
    //
    // @Override
    // public String getId()
    // {
    // return "Node";
    // }
    //
    // @Override
    // public Connector getConnector()
    // {
    // return this.imbConnector;
    // }
    // }
    //
    // /* ************************************************************************************************************** */
    // /* ********************************************* LINK LISTENER ************************************************** */
    // /* ************************************************************************************************************** */
    //
    // /** the IMB Link listener. */
    // private static class LinkTransceiver implements Transceiver
    // {
    // /** the sampler. */
    // private final Sim0MQSampler sampler;
    //
    // /** The IMBConnector. */
    // private final IMBConnector imbConnector;
    //
    // /**
    // * @param sampler the sampler
    // * @param imbConnector the connector
    // */
    // public LinkTransceiver(final Sim0MQSampler sampler, final IMBConnector imbConnector)
    // {
    // this.imbConnector = imbConnector;
    // this.sampler = sampler;
    // }
    //
    // /** {@inheritDoc} */
    // @Override
    // @SuppressWarnings("unused")
    // public void handleMessageFromIMB(String imbEventName, TByteBuffer imbPayload) throws IMBException
    // {
    // int imbEventTypeNr = imbPayload.readInt32();
    // switch (imbEventTypeNr)
    // {
    // case TEventEntry.ACTION_NEW:
    // {
    // double timeStamp = imbPayload.readDouble();
    // String networkId = imbPayload.readString();
    // String linkId = imbPayload.readString();
    // System.out.println("Link " + linkId + " received.");
    // String startNodeId = imbPayload.readString();
    // String endNodeId = imbPayload.readString();
    // int dlNumPoints = imbPayload.readInt32();
    // double len = 0.0;
    // double x = imbPayload.readDouble();
    // double y = imbPayload.readDouble();
    // double z = imbPayload.readDouble();
    // CartesianPoint p1 = new CartesianPoint(x, y, z);
    // for (int i = 1; i < dlNumPoints; i++)
    // {
    // x = imbPayload.readDouble();
    // y = imbPayload.readDouble();
    // z = imbPayload.readDouble();
    // CartesianPoint p2 = new CartesianPoint(x, y, z);
    // len += p1.distance(p2);
    // p1 = p2;
    // }
    // Length length = new Length(len, LengthUnit.SI);
    // LinkData linkData = new LinkData(linkId, this.sampler.nodes.get(startNodeId),
    // this.sampler.nodes.get(endNodeId), length);
    // this.sampler.links.put(linkId, linkData);
    // break;
    // }
    //
    // case TEventEntry.ACTION_CHANGE:
    // {
    // // ignore for now.
    // break;
    // }
    //
    // case TEventEntry.ACTION_DELETE:
    // {
    // // ignore for now.
    // break;
    // }
    //
    // default:
    // break;
    // }
    // }
    //
    // @Override
    // public String getId()
    // {
    // return "Link_GTU";
    // }
    //
    // @Override
    // public Connector getConnector()
    // {
    // return this.imbConnector;
    // }
    // }
    //
    // /* ************************************************************************************************************** */
    // /* ********************************************* LANE LISTENER ************************************************** */
    // /* ************************************************************************************************************** */
    //
    // /** the IMB Lane listener. */
    // private static class LaneTransceiver implements Transceiver
    // {
    // /** the sampler. */
    // private final Sim0MQSampler sampler;
    //
    // /** The IMBConnector. */
    // private final IMBConnector imbConnector;
    //
    // /**
    // * @param sampler the sampler
    // * @param imbConnector the connector
    // */
    // public LaneTransceiver(final Sim0MQSampler sampler, final IMBConnector imbConnector)
    // {
    // this.imbConnector = imbConnector;
    // this.sampler = sampler;
    // }
    //
    // /** {@inheritDoc} */
    // @Override
    // @SuppressWarnings("unused")
    // public void handleMessageFromIMB(String imbEventName, TByteBuffer imbPayload) throws IMBException
    // {
    // int imbEventTypeNr = imbPayload.readInt32();
    // switch (imbEventTypeNr)
    // {
    // case TEventEntry.ACTION_NEW:
    // {
    // double timeStamp = imbPayload.readDouble();
    // String networkId = imbPayload.readString();
    // String linkId = imbPayload.readString();
    // // TODO laneId should be unique on its own, or keep network, link and lane id separate
    // String laneId = linkId + "." + imbPayload.readString();
    // System.out.println("Lane " + laneId + " received.");
    // int laneNumber = imbPayload.readInt32();
    // int dlNumPoints = imbPayload.readInt32();
    // double len = 0.0;
    // double x = imbPayload.readDouble();
    // double y = imbPayload.readDouble();
    // double z = imbPayload.readDouble();
    // CartesianPoint p1 = new CartesianPoint(x, y, z);
    // for (int i = 1; i < dlNumPoints; i++)
    // {
    // x = imbPayload.readDouble();
    // y = imbPayload.readDouble();
    // z = imbPayload.readDouble();
    // CartesianPoint p2 = new CartesianPoint(x, y, z);
    // len += p1.distance(p2);
    // p1 = p2;
    // }
    // Length length = new Length(len, LengthUnit.SI);
    // if (!this.sampler.links.containsKey(linkId))
    // {
    // System.out.println("Link not received.");
    // }
    // LaneData laneData = new LaneData(this.sampler.links.get(linkId), laneId, length);
    // if (this.sampler.lanes.containsKey(laneId))
    // {
    // System.out.println("Lanes not unique.");
    // }
    // this.sampler.lanes.put(laneId, laneData);
    // break;
    // }
    //
    // case TEventEntry.ACTION_CHANGE:
    // {
    // // ignore for now.
    // break;
    // }
    //
    // case TEventEntry.ACTION_DELETE:
    // {
    // // ignore for now.
    // break;
    // }
    //
    // default:
    // break;
    // }
    // }
    //
    // @Override
    // public String getId()
    // {
    // return "Lane_GTU";
    // }
    //
    // @Override
    // public Connector getConnector()
    // {
    // return this.imbConnector;
    // }
    // }
    //
}
