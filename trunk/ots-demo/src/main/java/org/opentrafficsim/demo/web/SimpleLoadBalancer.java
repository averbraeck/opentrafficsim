package org.opentrafficsim.demo.web;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import org.djutils.io.URLResource;
import org.djutils.serialization.SerializationException;
import org.sim0mq.Sim0MQException;
import org.sim0mq.message.MessageStatus;
import org.sim0mq.message.SimulationMessage;
import org.sim0mq.message.federatestarter.FederateStartedMessage;
import org.sim0mq.message.federationmanager.StartFederateMessage;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

/**
 * The LoadBalancer start listening on the given port for messages. It is started with a map of network nodes where
 * FederateStarters can start models, and a capacity of each node.
 * </p>
 * <p>
 * The program is called as follows: java -jar LocalLoadBalancer.jar key1=value1 key2=value2 ... The following keys are defined:
 * <ul>
 * <li><b>port:</b> the port on which the LoadBalancer listens</li>
 * <li><b>nodeFile:</b> the file that contains the node information</li>
 * </ul>
 * The nodeFile is expected to be tab-delimited with the following columns (and an example):
 * 
 * <pre>
 * nodeAddress  fsPort   maxLoad   priority
 * 10.0.0.121   5500     8         10
 * 10.0.0.122   5500     8         10
 * 10.0.0.123   5500     8         10
 * 127.0.0.1    5500     8         5
 * </pre>
 * 
 * Where nodeAddress is the network name of the node or its IP address, fsPort is the port where the FederateStarter can be
 * found on the node, maxLoad is an int indicating the maximum number of jobs the node can handle, and priority indicates which
 * nodes should be scheduled first (nodes of lower priority are only scheduled when high priority nodes are full). The example
 * above shows three nodes with priority 10, and the localhost with a lower priority. So only when 24 federates are running,
 * localhost will be used.
 * </p>
 * <p>
 * Copyright (c) 2016-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/current/license.html">OTS License</a>.
 * </p>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class SimpleLoadBalancer
{
//    /** the port number to listen on. */
//    private final int lbPort;

//    /** the nodes with federate starters that can be used. */
//    private final Set<FederateStarterNode> federateStarterNodes;

    /** the 0mq socket. */
    private ZMQ.Socket lbSocket;

    /** the 0mq context. */
    private ZContext lbContext;

    /** message count. */
    private long messageCount = 0;

//    /**
//     * @param lbPort the port number to listen on
//     * @param federateStarterNodes the nodes with federate starters that can be used
//     * @throws Sim0MQException on error
//     * @throws SerializationException on error
//     */
//    public SimpleLoadBalancer(final int lbPort, final Set<FederateStarterNode> federateStarterNodes)
//            throws Sim0MQException, SerializationException
//    {
//        super();
//        this.lbPort = lbPort;
//        this.federateStarterNodes = federateStarterNodes;
//
//        this.lbContext = new ZContext(1);
//
//        this.lbSocket = this.lbContext.createSocket(SocketType.ROUTER);
//        this.lbSocket.bind("tcp://*:" + this.lbPort);
//
//        while (!Thread.currentThread().isInterrupted())
//        {
//            // Wait for next request from the [web] client -- first the identity (String) and the delimiter (#0)
//            String identity = this.lbSocket.recvStr();
//            this.lbSocket.recvStr();
//
//            byte[] request = this.lbSocket.recv(0);
//            Object[] fields = SimulationMessage.decode(request);
//            String messageTypeId = fields[4].toString();
//            String receiverId = fields[3].toString();
//
//            System.out.println("Received " + SimulationMessage.print(fields));
//
//            if (receiverId.equals("LB"))
//            {
//                switch (messageTypeId)
//                {
//                    case "LB.1":
//                        processStartModel(identity, fields);
//                        break;
//
//                    case "LB.2":
//                        processModelKilled(identity, fields);
//                        break;
//
//                    default:
//                        // wrong message
//                        System.err.println("Received unknown message -- not processed: " + messageTypeId);
//                }
//            }
//            else
//            {
//                // wrong receiver
//                System.err.println("Received message not intended for LB but for " + receiverId + " -- not processed: ");
//            }
//        }
//        this.lbSocket.close();
//        this.lbContext.destroy();
//    }
//
//    /**
//     * Process MC.X message and send LB.Y message back.
//     * @param identity reply id for REQ-ROUTER pattern
//     * @param fields the message
//     * @throws Sim0MQException on error
//     * @throws SerializationException on error
//     */
//    private void processStartFederateStarter(final String identity, final Object[] fields)
//            throws Sim0MQException, SerializationException
//    {
//        StartFederateMessage startFederateMessage = StartFederateMessage.createMessage(fields, "LB");
//        String error = "";
//
//        int modelPort = findFreePortNumber();
//
//        if (modelPort == -1)
//        {
//            error = "No free port number";
//        }
//
//        else
//
//        {
//            try
//            {
//                ProcessBuilder pb = new ProcessBuilder();
//
//                Path workingPath = Files.createDirectories(Paths.get(startFederateMessage.getWorkingDirectory()));
//                pb.directory(workingPath.toFile());
//
//                String softwareCode = "";
//                if (!this.softwareProperties.containsKey(startFederateMessage.getSoftwareCode()))
//                {
//                    System.err.println("Could not find software alias " + startFederateMessage.getSoftwareCode()
//                            + " in software properties file");
//                }
//                else
//                {
//                    softwareCode = this.softwareProperties.getProperty(startFederateMessage.getSoftwareCode());
//
//                    List<String> pbArgs = new ArrayList<>();
//                    pbArgs.add(softwareCode);
//                    pbArgs.add(startFederateMessage.getArgsBefore());
//                    pbArgs.add(startFederateMessage.getModelPath());
//                    pbArgs.addAll(Arrays.asList(
//                            startFederateMessage.getArgsAfter().replaceAll("%PORT%", String.valueOf(modelPort)).split(" ")));
//                    pb.command(pbArgs);
//
//                    String stdIn = startFederateMessage.getRedirectStdin();
//                    String stdOut = startFederateMessage.getRedirectStdout();
//                    String stdErr = startFederateMessage.getRedirectStderr();
//
//                    if (stdIn.length() > 0)
//                    {
//                        // TODO working dir path if not absolute?
//                        File stdInFile = new File(stdIn);
//                        pb.redirectInput(stdInFile);
//                    }
//
//                    if (stdOut.length() > 0)
//                    {
//                        // TODO working dir path if not absolute?
//                        File stdOutFile = new File(stdOut);
//                        pb.redirectOutput(stdOutFile);
//                    }
//
//                    if (stdErr.length() > 0)
//                    {
//                        // TODO working dir path if not absolute?
//                        File stdErrFile = new File(stdErr);
//                        pb.redirectError(stdErrFile);
//                    }
//
//                    new Thread()
//                    {
//                        /** {@inheritDoc} */
//                        @Override
//                        public void run()
//                        {
//                            try
//                            {
//                                Process process = pb.start();
//                                SimpleLoadBalancer.this.runningProcessMap.put(startFederateMessage.getInstanceId(), process);
//                                System.err.println("Process started:" + process.isAlive());
//                            }
//                            catch (IOException exception)
//                            {
//                                exception.printStackTrace();
//                            }
//                        }
//                    }.start();
//
//                    this.modelPortMap.put(startFederateMessage.getInstanceId(), modelPort);
//                    this.startFederateMessages.put(startFederateMessage.getInstanceId(), startFederateMessage);
//
//                    // Thread.sleep(1000);
//
//                    // wait till the model is ready...
//                    error = waitForModelStarted(startFederateMessage.getSimulationRunId(), startFederateMessage.getInstanceId(),
//                            modelPort);
//                }
//            }
//            catch (IOException exception)
//            {
//                exception.printStackTrace();
//                error = exception.getMessage();
//            }
//        }
//
//        System.out.println("SEND MESSAGE FS.2 ABOUT MODEL " + startFederateMessage.getInstanceId() + " @ port " + modelPort);
//
//        // Send reply back to client
//        this.lbSocket.sendMore(identity);
//        this.lbSocket.sendMore("");
//        //@formatter:off
//        byte[] fs2Message = new FederateStartedMessage.Builder()
//                .setSimulationRunId(startFederateMessage.getSimulationRunId())
//                .setInstanceId(startFederateMessage.getInstanceId())
//                .setSenderId("FS")
//                .setReceiverId(startFederateMessage.getSenderId())
//                .setMessageId(++this.messageCount)
//                .setStatus(error.isEmpty() ? "started" : "error")
//                .setError(error)
//                .setModelPort(modelPort)
//                .build()
//                .createByteArray();
//        this.lbSocket.send(fs2Message, 0);
//        //@formatter:on
//    }
//
//    /**
//     * Find a free port for the model.
//     * @return the first free fort number in the range startPort - endPort, inclusive
//     */
//    private int findFreePortNumber()
//    {
//        for (int port = this.startPort; port <= this.endPort; port++)
//        {
//            if (!this.modelPortMap.containsValue(port))
//            {
//                // try if the port is really free
//                ZMQ.Socket testSocket = null;
//                try
//                {
//                    testSocket = this.lbContext.createSocket(SocketType.REP);
//                    testSocket.bind("tcp://127.0.0.1:" + port);
//                    testSocket.unbind("tcp://127.0.0.1:" + port);
//                    testSocket.close();
//                    return port;
//                }
//                catch (Exception exception)
//                {
//                    // port was not free
//                    if (testSocket != null)
//                    {
//                        try
//                        {
//                            testSocket.close();
//                        }
//                        catch (Exception e)
//                        {
//                            // ignore.
//                        }
//                    }
//                }
//            }
//        }
//        return -1;
//    }
//
//    /**
//     * Wait for simulation to end using status polling with message FM.5.
//     * @param federationRunId the name of the federation
//     * @param modelId the String id of the model
//     * @param modelPort port on which the model is listening
//     * @return empty String for no error, filled String for error
//     * @throws Sim0MQException on error
//     * @throws SerializationException on error
//     */
//    private String waitForModelStarted(final Object federationRunId, final String modelId, final int modelPort)
//            throws Sim0MQException, SerializationException
//    {
//        boolean ok = true;
//        String error = "";
//        ZMQ.Socket modelSocket = null;
//        try
//        {
//            modelSocket = this.lbContext.createSocket(SocketType.REQ);
//            modelSocket.setIdentity(UUID.randomUUID().toString().getBytes());
//            modelSocket.connect("tcp://127.0.0.1:" + modelPort);
//        }
//        catch (Exception exception)
//        {
//            exception.printStackTrace();
//            ok = false;
//            error = exception.getMessage();
//        }
//
//        boolean started = false;
//        while (ok && !started)
//        {
//            byte[] fs1Message = SimulationMessage.encodeUTF8(federationRunId, "FS", modelId, "FS.1", ++this.messageCount,
//                    MessageStatus.NEW);
//            modelSocket.send(fs1Message, 0);
//
//            byte[] reply = modelSocket.recv(0);
//            Object[] replyMessage = SimulationMessage.decode(reply);
//            System.out.println("Received\n" + SimulationMessage.print(replyMessage));
//
//            if (replyMessage[4].toString().equals("MC.1") && !replyMessage[9].toString().equals("error")
//                    && !replyMessage[9].toString().equals("ended") && ((Long) replyMessage[8]).longValue() == this.messageCount)
//            {
//                if (replyMessage[9].toString().equals("started"))
//                {
//                    started = true;
//                }
//                else
//                {
//                    // wait a second
//                    try
//                    {
//                        Thread.sleep(100);
//                    }
//                    catch (InterruptedException ie)
//                    {
//                        // ignore
//                    }
//                }
//            }
//            else
//            {
//                ok = false;
//                error = replyMessage[10].toString();
//                System.err.println("Simulation start error -- status = " + replyMessage[9]);
//                System.err.println("Error message = " + replyMessage[10]);
//            }
//        }
//
//        if (modelSocket != null)
//        {
//            modelSocket.close();
//        }
//
//        return error;
//    }
//
//    /**
//     * Process FM.8 message and send FS.4 message back.
//     * @param identity reply id for REQ-ROUTER pattern
//     * @param fields the message
//     * @throws Sim0MQException on error
//     * @throws SerializationException on error
//     */
//    private void processKillFederateStarter(final String identity, final Object[] fields)
//            throws Sim0MQException, SerializationException
//    {
//        boolean status = true;
//        String error = "";
//
//        Object federationRunId = fields[1];
//        String senderId = fields[2].toString();
//
//        String modelId = fields[8].toString();
//        if (!this.modelPortMap.containsKey(modelId))
//        {
//            status = false;
//            error = "model " + modelId + " unknown -- this model is unknown to the FederateStarter";
//        }
//        else
//        {
//            int modelPort = this.modelPortMap.remove(modelId);
//            Process process = this.runningProcessMap.remove(modelId);
//
//            try
//            {
//                try
//                {
//                    ZMQ.Socket modelSocket = this.lbContext.createSocket(SocketType.REQ);
//                    modelSocket.setIdentity(UUID.randomUUID().toString().getBytes());
//                    modelSocket.connect("tcp://127.0.0.1:" + modelPort);
//
//                    byte[] fs3Message = SimulationMessage.encodeUTF8(federationRunId, "FS", modelId, "FS.3",
//                            ++this.messageCount, MessageStatus.NEW);
//                    modelSocket.send(fs3Message, 0);
//
//                    modelSocket.close();
//                }
//                catch (Exception exception)
//                {
//                    exception.printStackTrace();
//                    status = true;
//                    error = exception.getMessage();
//                }
//
//                try
//                {
//                    Thread.sleep(100);
//                }
//                catch (InterruptedException ie)
//                {
//                    // ignore
//                }
//
//                if (process != null && process.isAlive())
//                {
//                    process.destroyForcibly();
//                }
//
//                StartFederateMessage sfm = this.startFederateMessages.get(modelId);
//                if (sfm.isDeleteStdout())
//                {
//                    if (sfm.getRedirectStdout().length() > 0)
//                    {
//                        File stdOutFile = new File(sfm.getRedirectStdout());
//                        stdOutFile.delete();
//                    }
//                }
//
//                if (sfm.isDeleteStderr())
//                {
//                    if (sfm.getRedirectStderr().length() > 0)
//                    {
//                        File stdErrFile = new File(sfm.getRedirectStderr());
//                        stdErrFile.delete();
//                    }
//                }
//
//                if (sfm.isDeleteWorkingDirectory())
//                {
//                    File workingDir = new File(sfm.getWorkingDirectory());
//                    workingDir.delete();
//                }
//            }
//            catch (Exception exception)
//            {
//                exception.printStackTrace();
//                status = false;
//                error = exception.getMessage();
//            }
//
//            byte[] fs4Message = SimulationMessage.encodeUTF8(federationRunId, "FS", senderId, "FS.4", ++this.messageCount,
//                    MessageStatus.NEW, modelId, status, error);
//            this.lbSocket.sendMore(identity);
//            this.lbSocket.sendMore("");
//            this.lbSocket.send(fs4Message, 0);
//        }
//    }
//
//    /**
//     * Start listening on the given port for messages to start components. Report back via the call-back port on the status of
//     * the started components. If necessary, the FederateStarter can also forcefully stop a started (sub)process.
//     * @param args the federation name and port on which the FederateStarter is listening
//     * @throws Sim0MQException on error
//     * @throws SerializationException on error
//     */
//    public static void main(final String[] args) throws Sim0MQException, SerializationException
//    {
//        if (args.length < 4)
//        {
//            System.err.println("Use as FederateStarter portNumber software_properties_file startPort endPort");
//            System.exit(-1);
//        }
//
//        String sPort = args[0];
//        int port = 0;
//        try
//        {
//            port = Integer.parseInt(sPort);
//        }
//        catch (NumberFormatException nfe)
//        {
//            System.err.println("Use as FederateStarter portNumber, where portNumber is a number");
//            System.exit(-1);
//        }
//        if (port == 0 || port > 65535)
//        {
//            System.err.println("PortNumber should be between 1 and 65535");
//            System.exit(-1);
//        }
//
//        String propertiesFile = args[1];
//        Properties softwareProperties = new Properties();
//        InputStream propertiesStream = URLResource.getResourceAsStream(propertiesFile);
//        try
//        {
//            softwareProperties.load(propertiesStream);
//        }
//        catch (IOException | NullPointerException e)
//        {
//            System.err.println("Could not find or read software properties file " + propertiesFile);
//            System.exit(-1);
//        }
//
//        String sStartPort = args[2];
//        int startPort = 0;
//        try
//        {
//            startPort = Integer.parseInt(sStartPort);
//        }
//        catch (NumberFormatException nfe)
//        {
//            System.err.println("Use as FederateStarter pn file startPort endPort, where startPort is a number");
//            System.exit(-1);
//        }
//        if (startPort == 0 || startPort > 65535)
//        {
//            System.err.println("startPort should be between 1 and 65535");
//            System.exit(-1);
//        }
//
//        String sEndPort = args[3];
//        int endPort = 0;
//        try
//        {
//            endPort = Integer.parseInt(sEndPort);
//        }
//        catch (NumberFormatException nfe)
//        {
//            System.err.println("Use as FederateStarter pn file startPort endPort, where endPort is a number");
//            System.exit(-1);
//        }
//        if (endPort == 0 || endPort > 65535)
//        {
//            System.err.println("endPort should be between 1 and 65535");
//            System.exit(-1);
//        }
//
//        new SimpleLoadBalancer(port, softwareProperties, startPort, endPort);
//    }
//
//    /** Record with information about the nodes that have a FederateStarter running. */
//    static class FederateStarterNode implements Comparable<FederateStarterNode>
//    {
//        /** the node name or IP address where the Federate Starter resides. */
//        private final String fsNodeName;
//
//        /** the port of the Federate Starter. */
//        private final int port;
//
//        /** the maximum load on the node (e.g., the maximum number of concurrent models). */
//        private final int maxLoad;
//
//        /** the current load on the node. */
//        private int currentLoad = 0;
//
//        /** the priority of the node. Higher value is higher priority. */
//        private int priority;
//
//        /**
//         * @param fsNodeName String; the node name or IP address where the Federate Starter resides
//         * @param port int; the port of the Federate Starter
//         * @param maxLoad int; the maximum load on the node (e.g., the maximum number of concurrent models)
//         * @param priority int; the priority of the node. Higher value is higher priority.
//         */
//        FederateStarterNode(final String fsNodeName, final int port, final int maxLoad, final int priority)
//        {
//            super();
//            this.fsNodeName = fsNodeName;
//            this.port = port;
//            this.maxLoad = maxLoad;
//            this.priority = priority;
//        }
//
//        /**
//         * @return currentLoad
//         */
//        public final int getCurrentLoad()
//        {
//            return this.currentLoad;
//        }
//
//        /**
//         * @param currentLoad set currentLoad
//         */
//        public final void setCurrentLoad(final int currentLoad)
//        {
//            this.currentLoad = currentLoad;
//        }
//
//        /**
//         * @return priority
//         */
//        public final int getPriority()
//        {
//            return this.priority;
//        }
//
//        /**
//         * @param priority set priority
//         */
//        public final void setPriority(final int priority)
//        {
//            this.priority = priority;
//        }
//
//        /**
//         * @return fsNodeName
//         */
//        public final String getFsNodeName()
//        {
//            return this.fsNodeName;
//        }
//
//        /**
//         * @return port
//         */
//        public final int getPort()
//        {
//            return this.port;
//        }
//
//        /**
//         * @return maxLoad
//         */
//        public final int getMaxLoad()
//        {
//            return this.maxLoad;
//        }
//
//        /** {@inheritDoc} */
//        @Override
//        public int hashCode()
//        {
//            final int prime = 31;
//            int result = 1;
//            result = prime * result + ((this.fsNodeName == null) ? 0 : this.fsNodeName.hashCode());
//            result = prime * result + this.port;
//            return result;
//        }
//
//        /** {@inheritDoc} */
//        @SuppressWarnings("checkstyle:needbraces")
//        @Override
//        public boolean equals(final Object obj)
//        {
//            if (this == obj)
//                return true;
//            if (obj == null)
//                return false;
//            if (getClass() != obj.getClass())
//                return false;
//            FederateStarterNode other = (FederateStarterNode) obj;
//            if (this.fsNodeName == null)
//            {
//                if (other.fsNodeName != null)
//                    return false;
//            }
//            else if (!this.fsNodeName.equals(other.fsNodeName))
//                return false;
//            if (this.port != other.port)
//                return false;
//            return true;
//        }
//
//        /** {@inheritDoc} */
//        @SuppressWarnings("checkstyle:needbraces")
//        @Override
//        public int compareTo(final FederateStarterNode o)
//        {
//            // Compares this object with the specified object for order. Returns a
//            // negative integer, zero, or a positive integer as this object is less
//            // than, equal to, or greater than the specified object.
//            if (this.equals(o))
//                return 0;
//
//            // higher priority number means higher priority
//            if (this.priority > o.priority)
//                return 1;
//            if (this.priority < o.priority)
//                return -1;
//
//            // higher remaining load means higher priority
//            if (this.maxLoad - this.currentLoad > o.maxLoad - o.currentLoad)
//                return 1;
//            if (this.maxLoad - this.currentLoad < o.maxLoad - o.currentLoad)
//                return -1;
//
//            if (this.hashCode() > o.hashCode())
//                return 1;
//            if (this.hashCode() < o.hashCode())
//                return -1;
//            //
//            return 0;
//        }
//    }
}
