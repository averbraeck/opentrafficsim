package org.sim0mq.test;

import org.sim0mq.Sim0MQException;
import org.sim0mq.message.MessageStatus;
import org.sim0mq.message.util.SimulationMessage;
import org.zeromq.ZMQ;

/**
 * Server example for JeroMQ / ZeroMQ.
 * <p>
 * (c) copyright 2002-2016 <a href="http://www.simulation.tudelft.nl">Delft University of Technology</a>. <br>
 * BSD-style license. See <a href="http://www.simulation.tudelft.nl/dsol/3.0/license.html">DSOL License</a>. <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @version Oct 21, 2016
 */
public class Server
{
    /**
     * @param args command line arguments
     * @throws Sim0MQException on error
     */
    public static void main(String[] args) throws Sim0MQException
    {
        ZMQ.Context context = ZMQ.context(1);

        // Socket to talk to clients
        ZMQ.Socket responder = context.socket(ZMQ.REP);
        responder.bind("tcp://*:5556");

        while (!Thread.currentThread().isInterrupted())
        {
            // Wait for next request from the client
            byte[] request = responder.recv(0);
            Object[] message = SimulationMessage.decode(request);
            System.out.println("Received " + SimulationMessage.print(message));

            // send a reply
            Object[] reply = new Object[] { true, -28.2, 77000, "Bangladesh" };
            responder.send(SimulationMessage.encode("IDVV14.2", "MC.1", "MM1.4", "TEST.2", 1201L, MessageStatus.NEW, reply), 0);
        }
        responder.close();
        context.term();
    }
}
