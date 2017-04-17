package org.sim0mq.federatestarter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.sim0mq.Sim0MQException;
import org.sim0mq.message.MessageStatus;
import org.sim0mq.message.SimulationMessage;
import org.zeromq.ZMQ;

/**
 * The FederateStarter start listening on the given port for messages to start components. Report back via the call-back port on
 * the status of the started components. If necessary, the FederateStarter can also forcefully stop a started (sub)process.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Mar 1, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class FederateStarter
{
    /** the name of the federation. */
    private final String federationName;

    /** the port number to listen on. */
    private final int port;

    /** the running programs this FederateStarter started. The String identifies the process (e.g., a UUID or a model id). */
    private Map<String, Process> runningProcessMap = new HashMap<>();

    /**
     * @param federationName the name of the federation
     * @param fsPort the port number to listen on
     * @throws Sim0MQException on error
     */
    public FederateStarter(final String federationName, final int fsPort) throws Sim0MQException
    {
        super();
        this.federationName = federationName;
        this.port = fsPort;

        ZMQ.Context fsContext = ZMQ.context(1);

        ZMQ.Socket fsSocket = fsContext.socket(ZMQ.REP);
        fsSocket.bind("tcp://*:" + this.port);

        while (!Thread.currentThread().isInterrupted())
        {
            // Wait for next request from the client
            byte[] request = fsSocket.recv(0);
            Object[] fields = SimulationMessage.decode(request);

            System.out.println("Received " + SimulationMessage.print(fields));

            if (fields[3].equals("FS") && fields[4].equals("FM.1"))
                try
                {
                    ProcessBuilder pb = new ProcessBuilder();

                    String workingDir = fields[14].toString();
                    Path workingPath = Files.createDirectories(Paths.get(workingDir));
                    pb.directory(workingPath.toFile());

                    String softwareCode = fields[9].toString();
                    if (softwareCode.toLowerCase().startsWith("java"))
                    {
                        softwareCode = "java";
                    }
                    
                    String argsBefore = fields[11].toString();
                    String command = fields[12].toString();
                    String argsAfter = fields[13].toString();

                    pb.command(softwareCode, argsBefore, command, argsAfter);
                    
                    String stdIn = fields[16].toString();
                    String stdOut = fields[17].toString();
                    String stdErr = fields[18].toString();
                    
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
                                System.err.println("Process started:" + process.isAlive());
                            }
                            catch (IOException exception)
                            {
                                exception.printStackTrace();
                            }
                        }
                    }.start();
                    
                    Thread.sleep(100);
                }
                catch (InterruptedException | IOException exception)
                {
                    exception.printStackTrace();
                }

            // Send reply back to client
            byte[] fs2Message = SimulationMessage.encode(federationName, "FS", "FM", "FS.2", 2L, MessageStatus.NEW, "MM1.1", "started", "");
            fsSocket.send(fs2Message, 0);
        }
        fsSocket.close();
        fsContext.term();
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
            System.err.println("Use as FederateStarter federationName portNumber");
            System.exit(-1);
        }
        String federationName = args[0];
        String sPort = args[1];
        int port = 0;
        try
        {
            port = Integer.parseInt(sPort);
        }
        catch (NumberFormatException nfe)
        {
            System.err.println("Use as FederateStarter federationName portNumber, where portNumber is a number");
            System.exit(-1);
        }
        if (port == 0 || port > 65535)
        {
            System.err.println("PortNumber should be between 1 and 65535");
            System.exit(-1);
        }

        new FederateStarter(federationName, port);
    }

}
