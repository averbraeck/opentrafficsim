package org.sim0mq.federatestarter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.sim0mq.Sim0MQException;
import org.sim0mq.message.MessageStatus;
import org.sim0mq.message.SimulationMessage;
import org.zeromq.ZMQ;

import nl.tudelft.simulation.language.io.URLResource;

/**
 * The FederateStarter start listening on the given port for messages to start components. Report back via the call-back port on
 * the status of the started components. If necessary, the FederateStarter can also forcefully stop a started (sub)process.
 * <p>
 * Copyright (c) 2016-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://sim0mq.org/docs/current/license.html">Sim0MQ License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Mar 1, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class FederateStarter
{
    /** the port number to listen on. */
    private final int port;

    /** the running programs this FederateStarter started. The String identifies the process (e.g., a UUID or a model id). */
    protected Map<String, Process> runningProcessMap = new HashMap<>();

    /** the ports where the models listen. The String identifies the process (e.g., a UUID or a model id). */
    private Map<String, Integer> modelPortMap = new HashMap<>();

    /** the software properties. */
    final Properties softwareProperties;

    /** the 0mq socket. */
    private ZMQ.Socket fsSocket;

    /** the 0mq context. */
    private ZMQ.Context fsContext;

    /** message count. */
    private long messageCount = 0;

    /**
     * @param fsPort the port number to listen on
     * @param softwareProperties the software properties to use
     * @throws Sim0MQException on error
     */
    public FederateStarter(final int fsPort, final Properties softwareProperties) throws Sim0MQException
    {
        super();
        this.softwareProperties = softwareProperties;
        this.port = fsPort;

        this.fsContext = ZMQ.context(1);

        this.fsSocket = this.fsContext.socket(ZMQ.ROUTER);
        this.fsSocket.bind("tcp://*:" + this.port);

        while (!Thread.currentThread().isInterrupted())
        {
            // Wait for next request from the client -- first the identity (String) and the delimiter (#0)
            String identity = this.fsSocket.recvStr();
            this.fsSocket.recvStr();

            byte[] request = this.fsSocket.recv(0);
            Object[] fields = SimulationMessage.decode(request);

            System.out.println("Received " + SimulationMessage.print(fields));

            Object federationRunId = fields[1];
            String senderId = fields[2].toString();
            String receiverId = fields[3].toString();
            String messageId = fields[4].toString();
            long uniqueId = ((Long) fields[5]).longValue();

            if (receiverId.equals("FS"))
            {
                switch (messageId)
                {
                    case "FM.1":
                        processStartFederate(identity, federationRunId, senderId, uniqueId, fields);
                        break;

                    case "FM.8":
                        processKillFederate(identity, federationRunId, senderId, uniqueId, fields);
                        break;

                    case "FM.9":
                        // processKillAllFederates(senderId, uniqueId);
                        break;

                    default:
                        // wrong message
                        System.err.println("Received unknown message -- not processed: " + messageId);
                }
            }
            else
            {
                // wrong receiver
                System.err.println("Received message not intended for FS but for " + receiverId + " -- not processed: ");
            }
        }
        this.fsSocket.close();
        this.fsContext.term();
    }

    /**
     * Process FM.2 message and send MC.2 message back.
     * @param identity reply id for REQ-ROUTER pattern
     * @param federationRunId the federation id
     * @param senderId the receiver of the response
     * @param replyToMessageId the message to which this is the reply
     * @param fields the message
     * @throws Sim0MQException on error
     */
    private void processStartFederate(final String identity, final Object federationRunId, final String senderId,
            final long replyToMessageId, final Object[] fields) throws Sim0MQException
    {
        String modelId = fields[8].toString();
        String error = "";

        try
        {
            ProcessBuilder pb = new ProcessBuilder();

            String workingDir = fields[13].toString();
            Path workingPath = Files.createDirectories(Paths.get(workingDir));
            pb.directory(workingPath.toFile());

            String softwareAlias = fields[9].toString();
            String softwareCode = "";
            if (!this.softwareProperties.containsKey(softwareAlias))
            {
                System.err.println("Could not find software alias " + softwareAlias + " in software properties file");
            }
            else
            {
                softwareCode = this.softwareProperties.getProperty(softwareAlias);

                String argsBefore = fields[10].toString();
                String command = fields[11].toString();
                String argsAfter = fields[12].toString();

                List<String> pbArgs = new ArrayList<>();
                pbArgs.add(softwareCode);
                pbArgs.add(argsBefore);
                pbArgs.add(command);
                pbArgs.addAll(Arrays.asList(argsAfter.split(" ")));
                pb.command(pbArgs);

                int modelPort = ((Number) fields[14]).intValue();

                String stdIn = fields[15].toString();
                String stdOut = fields[16].toString();
                String stdErr = fields[17].toString();

                if (stdIn.length() > 0)
                {
                    // TODO working dir path if not absolute?
                    File stdInFile = new File(stdIn);
                    pb.redirectInput(stdInFile);
                }

                if (stdOut.length() > 0)
                {
                    // TODO working dir path if not absolute?
                    File stdOutFile = new File(stdOut);
                    pb.redirectOutput(stdOutFile);
                }

                if (stdErr.length() > 0)
                {
                    // TODO working dir path if not absolute?
                    File stdErrFile = new File(stdErr);
                    pb.redirectError(stdErrFile);
                }

                new Thread()
                {
                    /** {@inheritDoc} */
                    @Override
                    public void run()
                    {
                        try
                        {
                            Process process = pb.start();
                            FederateStarter.this.runningProcessMap.put(modelId, process);
                            System.err.println("Process started:" + process.isAlive());
                        }
                        catch (IOException exception)
                        {
                            exception.printStackTrace();
                        }
                    }
                }.start();

                this.modelPortMap.put(modelId, modelPort);

                // wait till the model is ready...
                error = waitForModelStarted(federationRunId, modelId, modelPort);
            }
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
            error = exception.getMessage();
        }

        // Send reply back to client
        this.fsSocket.sendMore(identity);
        this.fsSocket.sendMore("");
        byte[] fs2Message = SimulationMessage.encode(federationRunId, "FS", senderId, "FS.2", ++this.messageCount,
                MessageStatus.NEW, modelId, error.isEmpty() ? "started" : "error", error);
        this.fsSocket.send(fs2Message, 0);
    }

    /**
     * Wait for simulation to end using status polling with message FM.5.
     * @param federationRunId the name of the federation
     * @param modelId the String id of the model
     * @param modelPort port on which the model is listening
     * @return empty String for no error, filled String for error
     * @throws Sim0MQException on error
     */
    private String waitForModelStarted(final Object federationRunId, final String modelId, final int modelPort)
            throws Sim0MQException
    {
        boolean ok = true;
        String error = "";
        ZMQ.Socket modelSocket = null;
        try
        {
            modelSocket = this.fsContext.socket(ZMQ.REQ);
            modelSocket.setIdentity(UUID.randomUUID().toString().getBytes());
            modelSocket.connect("tcp://127.0.0.1:" + modelPort);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
            ok = false;
            error = exception.getMessage();
        }

        boolean started = false;
        while (ok && !started)
        {
            byte[] fs1Message =
                    SimulationMessage.encode(federationRunId, "FS", modelId, "FS.1", ++this.messageCount, MessageStatus.NEW);
            modelSocket.send(fs1Message, 0);

            byte[] reply = modelSocket.recv(0);
            Object[] replyMessage = SimulationMessage.decode(reply);
            System.out.println("Received\n" + SimulationMessage.print(replyMessage));

            if (replyMessage[4].toString().equals("MC.1") && !replyMessage[9].toString().equals("error")
                    && !replyMessage[9].toString().equals("ended") && ((Long) replyMessage[8]).longValue() == this.messageCount)
            {
                if (replyMessage[9].toString().equals("started"))
                {
                    started = true;
                }
                else
                {
                    // wait a second
                    try
                    {
                        Thread.sleep(100);
                    }
                    catch (InterruptedException ie)
                    {
                        // ignore
                    }
                }
            }
            else
            {
                ok = false;
                error = replyMessage[10].toString();
                System.err.println("Simulation start error -- status = " + replyMessage[9]);
                System.err.println("Error message = " + replyMessage[10]);
            }
        }

        if (modelSocket != null)
        {
            modelSocket.close();
        }

        return error;
    }

    /**
     * Process FM.8 message and send FS.4 message back.
     * @param identity reply id for REQ-ROUTER pattern
     * @param federationRunId the federation id
     * @param senderId the receiver of the response
     * @param replyToMessageId the message to which this is the reply
     * @param fields the message
     * @throws Sim0MQException on error
     */
    private void processKillFederate(final String identity, final Object federationRunId, final String senderId,
            final long replyToMessageId, final Object[] fields) throws Sim0MQException
    {
        boolean status = true;
        String error = "";

        String modelId = fields[8].toString();
        if (!this.modelPortMap.containsKey(modelId))
        {
            status = false;
            error = "model " + modelId + " unknown -- this model is unknown to the FederateStarter";
        }
        else
        {
            int modelPort = this.modelPortMap.remove(modelId);
            Process process = this.runningProcessMap.remove(modelId);

            try
            {
                try
                {
                    ZMQ.Socket modelSocket = this.fsContext.socket(ZMQ.REQ);
                    modelSocket.setIdentity(UUID.randomUUID().toString().getBytes());
                    modelSocket.connect("tcp://127.0.0.1:" + modelPort);

                    byte[] fs3Message = SimulationMessage.encode(federationRunId, "FS", modelId, "FS.3", ++this.messageCount,
                            MessageStatus.NEW);
                    modelSocket.send(fs3Message, 0);

                    modelSocket.close();
                }
                catch (Exception exception)
                {
                    exception.printStackTrace();
                    status = true;
                    error = exception.getMessage();
                }

                try
                {
                    Thread.sleep(100);
                }
                catch (InterruptedException ie)
                {
                    // ignore
                }

                if (process != null && process.isAlive())
                {
                    process.destroyForcibly();
                }
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
                status = false;
                error = exception.getMessage();
            }

            byte[] fs4Message = SimulationMessage.encode(federationRunId, "FS", senderId, "FS.4", ++this.messageCount,
                    MessageStatus.NEW, replyToMessageId, status, error);
            this.fsSocket.sendMore(identity);
            this.fsSocket.sendMore("");
            this.fsSocket.send(fs4Message, 0);
        }
    }

    /**
     * Start listening on the given port for messages to start components. Report back via the call-back port on the status of
     * the started components. If necessary, the FederateStarter can also forcefully stop a started (sub)process.
     * @param args the federation name and port on which the FederateStarter is listening
     * @throws Sim0MQException on error
     */
    public static void main(String[] args) throws Sim0MQException
    {
        if (args.length < 2)
        {
            System.err.println("Use as FederateStarter portNumber software_properties_file");
            System.exit(-1);
        }
        String sPort = args[0];
        int port = 0;
        try
        {
            port = Integer.parseInt(sPort);
        }
        catch (NumberFormatException nfe)
        {
            System.err.println("Use as FederateStarter portNumber, where portNumber is a number");
            System.exit(-1);
        }
        if (port == 0 || port > 65535)
        {
            System.err.println("PortNumber should be between 1 and 65535");
            System.exit(-1);
        }

        String propertiesFile = args[1];
        Properties softwareProperties = new Properties();
        InputStream propertiesStream = URLResource.getResourceAsStream(propertiesFile);
        try
        {
            softwareProperties.load(propertiesStream);
        }
        catch (IOException e)
        {
            System.err.println("Could not find software properties file " + propertiesFile);
            System.exit(-1);
        }

        new FederateStarter(port, softwareProperties);
    }

}
