package aimsun;

import org.sim0mq.Sim0MQException;
import org.sim0mq.message.MessageStatus;
import org.sim0mq.message.SimulationMessage;
import org.zeromq.ZMQ;

/**
 * Test client for AimsunController.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 18, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public final class TestController
{
    /**
     * Cannot be instantiated.
     */
    private TestController()
    {
        // Do not instantiate.
    }

    /**
     * Test client for AimsunControl.
     * <p>
     * (c) copyright 2002-2017 <a href="http://www.simulation.tudelft.nl">Delft University of Technology</a>. <br>
     * BSD-style license. See <a href="http://www.simulation.tudelft.nl/dsol/3.0/license.html">DSOL License</a>. <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @version Oct 21, 2016
     */
    /**
     * @param args command line arguments
     * @throws Sim0MQException on error
     */
    public static void main(final String[] args) throws Sim0MQException
    {
        ZMQ.Context context = ZMQ.context(1);

        // Socket to talk to server
        System.out.println("Connecting to server...");

        ZMQ.Socket requester = context.socket(ZMQ.PAIR);
        requester.connect("tcp://localhost:3333");

        // Send a request
        System.out.println("Sending 1st test message");
        Object[] request = new Object[] { "test message", new Double(14.2), new Float(-28.4), new Short((short) 10) };
        requester.send(SimulationMessage.encode("IDVV14.2", "MC.1", "MM1.4", "TEST.2", 1201L, MessageStatus.NEW, request), 0);

        // Receive a reply
        byte[] reply = requester.recv(0);
        Object[] replyMessage = SimulationMessage.decode(reply);
        System.out.println("TestController received 1st reply\n" + SimulationMessage.print(replyMessage));
        try
        {
            Thread.sleep(9999);
        }
        catch (InterruptedException exception)
        {
            exception.printStackTrace();
        }
        // Send a request
        System.out.println("Sending 2nd test message");
        Object[] request2 = new Object[] { "test message 2", new Double(14.2), new Float(-28.4), new Short((short) 10) };
        requester
                .send(SimulationMessage.encode("IDVV14.2", "MC.1", "MM1.4", "TEST.222", 1201L, MessageStatus.NEW, request2), 0);

        // Receive a reply
        byte[] reply2 = requester.recv(0);
        Object[] replyMessage2 = SimulationMessage.decode(reply2);
        System.out.println("TestController received 2nd reply\n" + SimulationMessage.print(replyMessage2));

        Object[] stopRequest = new Object[] { "shutdown" };
        requester.send(
                SimulationMessage.encode("IDVV14.2", "MC.1", "MM1.4", "TEST.222", 1201L, MessageStatus.NEW, stopRequest), 0);
        requester.close();
        context.term();
    }
}
