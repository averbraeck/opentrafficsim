package org.sim0mq.federationmanager;

import org.sim0mq.Sim0MQException;
import org.sim0mq.message.MessageStatus;
import org.sim0mq.message.SimulationMessage;
import org.zeromq.ZMQ;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Apr 11, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class FederationManager
{
    /**
     * Send an FM.1 message to the FederateStarter.
     * @param federationName the name of the federation
     * @param fmPort the port number to listen on
     * @param fsPort the port where the federate starter can be reached
     * @throws Sim0MQException on error
     */
    public FederationManager(final String federationName, final int fmPort, final int fsPort) throws Sim0MQException
    {
        ZMQ.Context fmContext = ZMQ.context(1);
        ZMQ.Socket fmSocket = fmContext.socket(ZMQ.REQ);
        
        // Start model e:/MM1/mmm1.jar
        byte[] fm1Message = SimulationMessage.encode(federationName, "FM", "FS", "FM.1", 1L, MessageStatus.NEW, "MM1.1", "java8+", "",
                "-jar", "e:/MM1/mm1.jar", "5502", "e:/MM1", (short) 5502, "", "e:/MM1/out.txt", "e:/MM1/err.txt", false, false, false);
        
        fmSocket.connect("tcp://127.0.0.1:" + fsPort);
        fmSocket.send(fm1Message);
        
        byte[] reply = fmSocket.recv(0);
        Object[] replyMessage = SimulationMessage.decode(reply);
        System.out.println("Received\n" + SimulationMessage.print(replyMessage));

        fmSocket.close();
        fmContext.term();
    }

    /**
     * @param args parameters for main
     * @throws Sim0MQException on error
     */
    public static void main(String[] args) throws Sim0MQException
    {
        if (args.length < 2)
        {
            System.err.println("Use as FederationManager federationName federationManagerPortNumber federateStarterPortNumber");
            System.exit(-1);
        }
        String federationName = args[0];

        String fmsPort = args[1];
        int fmPort = 0;
        try
        {
            fmPort = Integer.parseInt(fmsPort);
        }
        catch (NumberFormatException nfe)
        {
            System.err.println("Use as FederationManager fedNname fmPort fsPort, where fmPort is a number");
            System.exit(-1);
        }
        if (fmPort == 0 || fmPort > 65535)
        {
            System.err.println("fmPort should be between 1 and 65535");
            System.exit(-1);
        }

        String fssPort = args[2];
        int fsPort = 0;
        try
        {
            fsPort = Integer.parseInt(fssPort);
        }
        catch (NumberFormatException nfe)
        {
            System.err.println("Use as FederationManager fedNname fmPort fsPort, where fsPort is a number");
            System.exit(-1);
        }
        if (fsPort == 0 || fsPort > 65535)
        {
            System.err.println("fsPort should be between 1 and 65535");
            System.exit(-1);
        }

        new FederationManager(federationName, fmPort, fsPort);

    }

}
