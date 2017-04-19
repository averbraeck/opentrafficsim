package aimsun;

import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.event.EventInterface;
import nl.tudelft.simulation.event.EventListenerInterface;
import nl.tudelft.simulation.event.EventProducer;
import nl.tudelft.simulation.language.io.URLResource;

import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.modelproperties.Property;
import org.opentrafficsim.base.modelproperties.PropertyException;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.road.network.factory.xml.XmlNetworkLaneParser;
import org.opentrafficsim.simulationengine.AbstractWrappableAnimation;
import org.opentrafficsim.simulationengine.OTSSimulationException;
import org.sim0mq.Sim0MQException;
import org.sim0mq.message.MessageStatus;
import org.sim0mq.message.SimulationMessage;
import org.zeromq.ZMQ;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 18, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class AimsunControl extends AbstractWrappableAnimation
{

    /** */
    private static final long serialVersionUID = 20160418L;

    /** The network. */
    final URL networkURL;

    /**
     * Construct a new Aimsun controlled simulation.
     * @param networkURL URL; the network
     */
    AimsunControl(final URL networkURL)
    {
        this.networkURL = networkURL;
    }

    /**
     * Program entry point.
     * @param args String[]; the command line arguments
     */
    public static void main(final String[] args)
    {
        String ip = null;
        Integer port = null;

        for (String arg : args)
        {
            int equalsPos = arg.indexOf("=");
            if (equalsPos < 0)
            {
                System.err.println("Unhandled argument \"" + arg + "\"");
            }
            String key = arg.substring(0, equalsPos);
            String value = arg.substring(equalsPos + 1);
            switch (key.toUpperCase())
            {
                case "IP":
                    ip = value;
                    break;
                case "PORT":
                    try
                    {
                        port = Integer.parseInt(value);
                    }
                    catch (NumberFormatException exception)
                    {
                        System.err.println("Bad port number \"" + value + "\"");
                        System.exit(1);
                    }
                    break;
                default:
                    System.err.println("Unhandled argument \"" + arg + "\"");
            }
        }
        if (null == ip || null == port)
        {
            System.err.println("Missing required argument(s) ip=<ip-number_or_hostname> port=<port-number>");
            System.exit(1);
        }
        ZMQ.Context context = ZMQ.context(1);

        // Socket to talk to clients
        ZMQ.Socket responder = context.socket(ZMQ.PAIR);
        String address = String.format("tcp://*:%d", port);
        responder.bind(address);

        URL url = URLResource.getResource("/aimsun/singleRoad.xml");

        AimsunControl ac = new AimsunControl(url);
        try
        {
            ac.buildAnimator(Time.ZERO, Duration.ZERO, new Duration(60.0,
                    TimeUnit.MINUTE), new ArrayList<Property<?>>(), null, true);
        }
        catch (SimRuntimeException | NamingException | OTSSimulationException | PropertyException exception1)
        {
            exception1.printStackTrace();
        }

        System.out.println("Waiting for incoming connection on port " + port);
        while (!Thread.currentThread().isInterrupted())
        {
            // Wait for next request from the client
            byte[] request = responder.recv(0);
            // this will stop even ouw own reply from getting through: responder.unbind(address);
            Object[] message;
            try
            {
                message = SimulationMessage.decode(request);
                System.out.println("AimsunControl received " + SimulationMessage.print(message));
                if (message[8].equals("shutdown"))
                {
                    System.out.println("received shutdown request");
                    break;
                }
                // send a reply
                Object[] reply = new Object[] { true, -28.2, 77000, "AimsunControl" };
                responder.send(
                        SimulationMessage.encode("IDVV14.2", "MC.1", "MM1.4", "TEST.2", 1201L, MessageStatus.NEW, reply), 0);
            }
            catch (Sim0MQException exception)
            {
                exception.printStackTrace();
            }

        }
        responder.close();
        context.term();
    }

    /** {@inheritDoc} */
    @Override
    public final String shortName()
    {
        return "AimsunControlledOTS";
    }

    /** {@inheritDoc} */
    @Override
    public final String description()
    {
        return "Aimsun controlled OTS engine";
    }

    /** {@inheritDoc} */
    @Override
    protected final OTSModelInterface makeModel(final GTUColorer colorer) throws OTSSimulationException
    {
        return new AimsunModel();
    }

    /**
     * The network.
     */
    class AimsunModel extends EventProducer implements OTSModelInterface, EventListenerInterface
    {

        /** */
        private static final long serialVersionUID = 20170419L;

        /** The network. */
        private OTSNetwork network;

        /** The simulator. */
        private SimulatorInterface<Time, Duration, OTSSimTimeDouble> simulator;

        /** {@inheritDoc} */
        @Override
        public void constructModel(final SimulatorInterface<Time, Duration, OTSSimTimeDouble> theSimulator)
                throws SimRuntimeException, RemoteException
        {
            try
            {
                this.simulator = theSimulator;
                URL url = URLResource.getResource("/aimsun/singleRoad.xml");
                XmlNetworkLaneParser nlp = new XmlNetworkLaneParser((OTSDEVSSimulatorInterface) theSimulator);
                this.network = nlp.build(url);
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
            }
        }

        /** {@inheritDoc} */
        @Override
        public SimulatorInterface<Time, Duration, OTSSimTimeDouble> getSimulator() throws RemoteException
        {
            return this.simulator;
        }

        /** {@inheritDoc} */
        @Override
        public void notify(final EventInterface event) throws RemoteException
        {
            // WIP
        }

        /** {@inheritDoc} */
        @Override
        public OTSNetwork getNetwork()
        {
            return this.network;
        }

    }

}
