package org.opentrafficsim.demo.web;

import java.io.IOException;
import java.net.URL;

import javax.naming.NamingException;

import org.djunits.unit.DurationUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.cli.Checkable;
import org.djutils.cli.CliIUtil;
import org.djutils.io.URLResource;
import org.djutils.serialization.SerializationException;
import org.opentrafficsim.core.dsol.OTSAnimator;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSReplication;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.demo.CircularRoadModel;
import org.opentrafficsim.demo.CrossingTrafficLightsModel;
import org.opentrafficsim.demo.NetworksModel;
import org.opentrafficsim.demo.ShortMerge;
import org.opentrafficsim.demo.StraightModel;
import org.opentrafficsim.demo.conflict.BusStreetDemo;
import org.opentrafficsim.demo.conflict.TJunctionDemo;
import org.opentrafficsim.demo.conflict.TurboRoundaboutDemo;
import org.opentrafficsim.demo.trafficcontrol.TrafCODDemo1;
import org.opentrafficsim.demo.trafficcontrol.TrafCODDemo2;
import org.sim0mq.Sim0MQException;
import org.sim0mq.message.MessageStatus;
import org.sim0mq.message.MessageUtil;
import org.sim0mq.message.SimulationMessage;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.experiment.ReplicationMode;
import picocli.CommandLine.Option;

/**
 * <p>
 * Copyright (c) 2002-2014 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved.
 * <p>
 * See for project information <a href="http://www.simulation.tudelft.nl/"> www.simulation.tudelft.nl</a>.
 * <p>
 * The DSOL project is distributed under a BSD-style license.<br>
 * @version Aug 15, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class SuperDemoWebApplication implements Checkable
{
    /** */
    private OTSSimulatorInterface simulator;

    /** */
    private OTSModelInterface model;

    /** the socket. */
    private ZMQ.Socket fsSocket;

    /** the context. */
    private ZContext fsContext;

    /** federation run id. */
    private Object federationRunId;

    /** modelId unique Id of the model that is used as the sender/receiver when communicating. */
    private String modelId;

    /** runtime. */
    private Duration runTime;

    /** warmup. */
    private Duration warmupTime;

    /** message count. */
    private long messageCount = 0;

    /** home page for the web server. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    @Option(names = {"-m", "--modelId"}, description = "Id of the model to run", required = true)
    String homePage;

    /** internet port for the web server. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    @Option(names = {"-p", "--port"}, description = "Internet port to use", defaultValue = "8081")
    int port;

    /** {@inheritDoc} */
    @Override
    public void check() throws Exception
    {
        if (this.port < 1 || this.port > 65535)
        {
            throw new Exception("Port number should be between 1 and 65535");
        }
    }

    /**
     * Construct a console application.
     * @throws SimRuntimeException on error
     * @throws NamingException on error
     * @throws Sim0MQException on error
     * @throws SerializationException on serialization problem
     * @throws IOException when TRAFCOD file cannot be found
     */
    protected void init()
            throws SimRuntimeException, NamingException, Sim0MQException, SerializationException, IOException
    {
        this.simulator = new OTSAnimator();
        this.modelId = this.modelId.trim();
        if (this.modelId.toLowerCase().contains("circularroad"))
        {
            this.model = new CircularRoadModel(this.simulator);
        }
        else if (this.modelId.toLowerCase().contains("straight"))
        {
            this.model = new StraightModel(this.simulator);
        }
        else if (this.modelId.toLowerCase().contains("shortmerge"))
        {
            this.model = new ShortMerge.ShortMergeModel(this.simulator);
        }
        else if (this.modelId.toLowerCase().contains("networksdemo"))
        {
            this.model = new NetworksModel(this.simulator);
        }
        else if (this.modelId.toLowerCase().contains("crossingtrafficlights"))
        {
            this.model = new CrossingTrafficLightsModel(this.simulator);
        }
        else if (this.modelId.toLowerCase().contains("trafcoddemosimple"))
        {
            URL url = URLResource.getResource("/TrafCODDemo1/TrafCODDemo1.xml");
            String xml = TrafCODDemo2.readStringFromURL(url);
            this.model = new TrafCODDemo1.TrafCODModel(this.simulator, "TrafCODDemo1", "TrafCODDemo1", xml);
        }
        else if (this.modelId.toLowerCase().contains("trafcoddemocomplex"))
        {
            URL url = URLResource.getResource("/TrafCODDemo2/TrafCODDemo2.xml");
            String xml = TrafCODDemo2.readStringFromURL(url);
            this.model = new TrafCODDemo2.TrafCODModel(this.simulator, "TrafCODDemo2", "TrafCODDemo2", xml);
        }
        else if (this.modelId.toLowerCase().contains("tjunction"))
        {
            this.model = new TJunctionDemo.TJunctionModel(this.simulator);
        }
        else if (this.modelId.toLowerCase().contains("busstreet"))
        {
            this.model = new BusStreetDemo.BusStreetModel(this.simulator);
        }
        else if (this.modelId.toLowerCase().contains("turboroundabout"))
        {
            this.model = new TurboRoundaboutDemo.TurboRoundaboutModel(this.simulator);
        }

        if (this.model == null)
        {
            System.err.println("Could not find model " + this.modelId);
        }
        else
        {
            startListener();
        }
    }

    /**
     * Start listening on a port.
     * @throws Sim0MQException on error
     * @throws SerializationException on serialization problem
     */
    protected void startListener() throws Sim0MQException, SerializationException
    {
        this.fsContext = new ZContext(1);

        this.fsSocket = this.fsContext.createSocket(SocketType.ROUTER);
        this.fsSocket.bind("tcp://*:" + this.port);

        System.out.println("Model started. Listening at port: " + this.port);
        System.out.flush();

        while (!Thread.currentThread().isInterrupted())
        {
            // Wait for next request from the client -- first the identity (String) and the delimiter (#0)
            String identity = this.fsSocket.recvStr();
            this.fsSocket.recvStr();

            byte[] request = this.fsSocket.recv(0);
            System.out.println(MessageUtil.printBytes(request));
            Object[] fields = SimulationMessage.decode(request);

            System.out.println("Received " + SimulationMessage.print(fields));
            System.out.flush();

            this.federationRunId = fields[1];
            String senderId = fields[2].toString();
            String receiverId = fields[3].toString();
            String messageId = fields[4].toString();
            long uniqueId = ((Long) fields[5]).longValue();

            if (receiverId.equals(this.modelId))
            {
                System.err.println("Received: " + messageId + ", payload = " + SimulationMessage.listPayload(fields));
                switch (messageId)
                {
                    case "FS.1":
                    case "FM.5":
                        processRequestStatus(identity, senderId, uniqueId);
                        break;

                    case "FM.2":
                        processSimRunControl(identity, senderId, uniqueId, fields);
                        break;

                    case "FM.3":
                        processSetParameter(identity, senderId, uniqueId, fields);
                        break;

                    case "FM.4":
                        processSimStart(identity, senderId, uniqueId);
                        break;

                    case "FM.6":
                        processRequestStatistics(identity, senderId, uniqueId, fields);
                        break;

                    case "FS.3":
                        processKillFederate();
                        break;

                    default:
                        // wrong message
                        System.err.println("Received unknown message -- not processed: " + messageId);
                }
            }
            else
            {
                // wrong receiver
                System.err.println(
                        "Received message not intended for " + this.modelId + " but for " + receiverId + " -- not processed: ");
            }
        }
    }

    /**
     * Process FS.1 message and send MC.1 message back.
     * @param identity reply id for REQ-ROUTER pattern
     * @param receiverId the receiver of the response
     * @param replyToMessageId the message to which this is the reply
     * @throws Sim0MQException on error
     * @throws SerializationException on serialization problem
     */
    private void processRequestStatus(final String identity, final String receiverId, final long replyToMessageId)
            throws Sim0MQException, SerializationException
    {
        String status = "started";
        if (this.simulator.isRunning())
        {
            status = "running";
        }
        else if (this.simulator.getSimulatorTime() != null && this.simulator.getReplication() != null
                && this.simulator.getReplication().getTreatment() != null)
        {
            if (this.simulator.getSimulatorTime().ge(this.simulator.getReplication().getTreatment().getEndTime()))
            {
                status = "ended";
            }
            else
            {
                status = "error";
            }
        }
        this.fsSocket.sendMore(identity);
        this.fsSocket.sendMore("");
        byte[] mc1Message = SimulationMessage.encodeUTF8(this.federationRunId, this.modelId, receiverId, "MC.1",
                ++this.messageCount, MessageStatus.NEW, replyToMessageId, status, "");
        this.fsSocket.send(mc1Message, 0);

        System.out.println("Sent MC.1");
        System.out.flush();
    }

    /**
     * Process FM.2 message and send MC.2 message back.
     * @param identity reply id for REQ-ROUTER pattern
     * @param receiverId the receiver of the response
     * @param replyToMessageId the message to which this is the reply
     * @param fields the message
     * @throws Sim0MQException on error
     * @throws SerializationException on serialization problem
     */
    private void processSimRunControl(final String identity, final String receiverId, final long replyToMessageId,
            final Object[] fields) throws Sim0MQException, SerializationException
    {
        boolean status = true;
        String error = "";
        try
        {
            Object runTimeField = fields[8];
            if (runTimeField instanceof Number)
            {
                this.runTime = new Duration(((Number) fields[8]).doubleValue(), DurationUnit.SI);
            }
            else if (runTimeField instanceof Duration)
            {
                this.runTime = (Duration) runTimeField;
            }
            else
            {
                throw new Sim0MQException("runTimeField " + runTimeField + " neither Number nor Duration");
            }

            Object warmupField = fields[8];
            if (warmupField instanceof Number)
            {
                this.warmupTime = new Duration(((Number) fields[9]).doubleValue(), DurationUnit.SI);
            }
            else if (warmupField instanceof Duration)
            {
                this.warmupTime = (Duration) warmupField;
            }
            else
            {
                throw new Sim0MQException("warmupField " + warmupField + " neither Number nor Duration");
            }
        }
        catch (Exception e)
        {
            status = false;
            error = e.getMessage();
        }
        byte[] mc2Message = SimulationMessage.encodeUTF8(this.federationRunId, this.modelId, receiverId, "MC.2",
                ++this.messageCount, MessageStatus.NEW, replyToMessageId, status, error);
        this.fsSocket.sendMore(identity);
        this.fsSocket.sendMore("");
        this.fsSocket.send(mc2Message, 0);

        System.out.println("Sent MC.2");
        System.out.flush();
    }

    /**
     * Process FM.3 message and send MC.2 message back.
     * @param identity reply id for REQ-ROUTER pattern
     * @param receiverId the receiver of the response
     * @param replyToMessageId the message to which this is the reply
     * @throws Sim0MQException on error
     * @throws SerializationException on serialization problem
     */
    private void processSimStart(final String identity, final String receiverId, final long replyToMessageId)
            throws Sim0MQException, SerializationException
    {
        boolean status = true;
        String error = "";
        try
        {
            OTSReplication replication = OTSReplication.create("rep1", Time.ZERO, this.warmupTime, this.runTime, this.model);
            this.simulator.initialize(replication, ReplicationMode.TERMINATING);
            // TODO: different... this.simulator.scheduleEventAbs(100.0, this, this, "terminate", null);

            this.simulator.start();
        }
        catch (Exception e)
        {
            status = false;
            error = e.getMessage();
        }

        byte[] mc2Message = SimulationMessage.encodeUTF8(this.federationRunId, this.modelId, receiverId, "MC.2",
                ++this.messageCount, MessageStatus.NEW, replyToMessageId, status, error);
        this.fsSocket.sendMore(identity);
        this.fsSocket.sendMore("");
        this.fsSocket.send(mc2Message, 0);

        System.out.println("Sent MC.2");
        System.out.flush();
    }

    /**
     * Process FM.4 message and send MC.2 message back.
     * @param identity reply id for REQ-ROUTER pattern
     * @param receiverId the receiver of the response
     * @param replyToMessageId the message to which this is the reply
     * @param fields the message
     * @throws Sim0MQException on error
     * @throws SerializationException on serialization problem
     */
    private void processSetParameter(final String identity, final String receiverId, final long replyToMessageId,
            final Object[] fields) throws Sim0MQException, SerializationException
    {
        boolean status = true;
        String error = "";
        try
        {
            String parameterName = fields[8].toString();
            Object parameterValueField = fields[9];

            // TODO: change for InputParameter
            /*-
            switch (parameterName)
            {
                case "seed":
                    this.model.seed = ((Number) parameterValueField).longValue();
                    break;
            
                case "iat":
                    this.model.iat = ((Number) parameterValueField).doubleValue();
                    break;
            
                case "servicetime":
                    this.model.serviceTime = ((Number) parameterValueField).doubleValue();
                    break;
            
                default:
                    status = false;
                    error = "Parameter " + parameterName + " unknown";
                    break;
            }
            */
        }
        catch (Exception e)
        {
            status = false;
            error = e.getMessage();
        }

        byte[] mc2Message = SimulationMessage.encodeUTF8(this.federationRunId, this.modelId, receiverId, "MC.2",
                ++this.messageCount, MessageStatus.NEW, replyToMessageId, status, error);
        this.fsSocket.sendMore(identity);
        this.fsSocket.sendMore("");
        this.fsSocket.send(mc2Message, 0);

        System.out.println("Sent MC.2");
        System.out.flush();
    }

    /**
     * Process FM.5 message and send MC.3 or MC.4 message back.
     * @param identity reply id for REQ-ROUTER pattern
     * @param receiverId the receiver of the response
     * @param replyToMessageId the message to which this is the reply
     * @param fields the message
     * @throws Sim0MQException on error
     * @throws SerializationException on serialization problem
     */
    private void processRequestStatistics(final String identity, final String receiverId, final long replyToMessageId,
            final Object[] fields) throws Sim0MQException, SerializationException
    {
        boolean ok = true;
        String error = "";
        String variableName = fields[8].toString();
        double variableValue = Double.NaN;
        try
        {
            // TODO: This probably goes away or is replaced by metadata
            /*-
            switch (variableName)
            {
                case "dN.average":
                    variableValue = this.model.dN.getSampleMean();
                    break;
            
                case "uN.average":
                    variableValue = this.model.uN.getSampleMean();
                    break;
            
                case "qN.max":
                    variableValue = this.model.qN.getMax();
                    break;
            
                default:
                    ok = false;
                    error = "Parameter " + variableName + " unknown";
                    break;
            }
            */
        }
        catch (Exception e)
        {
            ok = false;
            error = e.getMessage();
        }

        if (Double.isNaN(variableValue))
        {
            ok = false;
            error = "Parameter " + variableName + " not set to a value";
        }

        if (ok)
        {
            byte[] mc3Message = SimulationMessage.encodeUTF8(this.federationRunId, this.modelId, receiverId, "MC.3",
                    ++this.messageCount, MessageStatus.NEW, replyToMessageId, variableName, variableValue);
            this.fsSocket.sendMore(identity);
            this.fsSocket.sendMore("");
            this.fsSocket.send(mc3Message, 0);

            System.out.println("Sent MC.3");
            System.out.flush();
        }
        else
        {
            byte[] mc4Message = SimulationMessage.encodeUTF8(this.federationRunId, this.modelId, receiverId, "MC.4",
                    ++this.messageCount, MessageStatus.NEW, replyToMessageId, ok, error);
            this.fsSocket.sendMore(identity);
            this.fsSocket.sendMore("");
            this.fsSocket.send(mc4Message, 0);

            System.out.println("Sent MC.4");
            System.out.flush();
        }
    }

    /**
     * Process FS.3 message.
     */
    private void processKillFederate()
    {
        this.fsSocket.close();
        this.fsContext.destroy();
        this.fsContext.close();
        System.exit(0);
    }

    /** stop the simulation. */
    protected final void terminate()
    {
        // TODO: This probably goes away or is replaced by metadata
        /*-
        System.out.println("average queue length = " + this.model.qN.getSampleMean());
        System.out.println("average queue wait   = " + this.model.dN.getSampleMean());
        System.out.println("average utilization  = " + this.model.uN.getSampleMean());
        */
    }

    /**
     * @param args contain e.g., port number, and a model to run: SuperDemoWebpplication --port=8080 -m TJunctionDemo.
     * @throws SimRuntimeException on error
     * @throws NamingException on error
     * @throws Sim0MQException on error
     * @throws SerializationException on serialization problem
     * @throws IOException when TRAFCODDEMO file cannot be found
     */
    public static void main(final String[] args)
            throws SimRuntimeException, NamingException, Sim0MQException, SerializationException, IOException
    {
        SuperDemoWebApplication webApp = new SuperDemoWebApplication();
        CliIUtil.execute(webApp, args);
        webApp.init();
    }

}
