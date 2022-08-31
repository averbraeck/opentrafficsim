package org.opentrafficsim.trafficcontrol.ccol;

import java.awt.Container;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.Set;

import org.djunits.unit.DurationUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djutils.event.EventInterface;
import org.djutils.event.EventProducer;
import org.djutils.event.EventTypeInterface;
import org.opentrafficsim.core.dsol.OTSSimulator;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.object.InvisibleObjectInterface;
import org.opentrafficsim.road.network.lane.object.sensor.TrafficLightSensor;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLight;
import org.opentrafficsim.trafficcontrol.ActuatedTrafficController;
import org.opentrafficsim.trafficcontrol.TrafficControlException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.experiment.ReplicationInterface;

/**
 * Communication link with a CCOL traffic control program.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jan 23, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class CCOL extends EventProducer implements ActuatedTrafficController
{
    /** */
    private static final long serialVersionUID = 20170126L;

    /** Name of this CCOL traffic controller. */
    private final String id;

    /** The simulator. */
    private final OTSSimulator simulator;

    /** TCP port for incoming connection. */
    private static int port = 4321;

    /** The evaluation interval of a CCOL controller. */
    static final Duration EVALUATION_INTERVAL = new Duration(0.1, DurationUnit.SECOND);

    /** Socket used to listen for the incoming connection from the CCOL controller. */
    private ServerSocket serverSocket;

    /** Socket for communication with the CCOL controller. */
    private Socket clientSocket = null;

    /** Receives data from the CCOL process. */
    private BufferedReader ccolReader = null;

    /** Sends data to the CCOL process. */
    private PrintWriter ccolWriter = null;

    /** Thread that blocks until accept returns. */
    private Thread acceptThread;

    /**
     * Construct a new CCOL communication link.
     * @param id String; id of the traffic controller
     * @param controlProgram String; name of the CCOL program that this CCOL link must communicate with
     * @param trafficLights Set&lt;TrafficLight&gt;; the traffic lights. The ids of the traffic lights must end with two digits
     *            that match the stream numbers as used in the traffic control program
     * @param sensors Set&lt;TrafficLightSensor&gt;; the traffic sensors. The ids of the traffic sensors must end with three
     *            digits; the first two of those must match the stream and sensor numbers used in the traffic control program
     * @param simulator DEVSSimulator&lt;Time, Duration, SimTimeDoubleUnit&gt;; the simulation engine
     * @throws TrafficControlException on failure to initialize the connection to the external CCOL program
     * @throws SimRuntimeException on failure to schedule the first evaluation event
     */
    public CCOL(final String id, final String controlProgram, final Set<TrafficLight> trafficLights,
            final Set<TrafficLightSensor> sensors, final OTSSimulator simulator)
            throws TrafficControlException, SimRuntimeException
    {
        this.id = id;
        this.simulator = simulator;
        try
        {
            // Set up a listening socket
            this.serverSocket = new ServerSocket(port);
            Runnable acceptTask = new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        setClientSocket(CCOL.this.serverSocket.accept());
                    }
                    catch (IOException exception)
                    {
                        exception.printStackTrace();
                    }
                }
            };
            this.acceptThread = new Thread(acceptTask);
            this.acceptThread.start();
            // Start up the external CCOL program
            Runtime.getRuntime().exec(controlProgram + " localhost:" + this.serverSocket.getLocalPort());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        this.simulator.scheduleEventRel(Duration.ZERO, this, this, "step", null);
        this.simulator.addListener(this, ReplicationInterface.END_REPLICATION_EVENT);
    }

    /**
     * Set the client socket (called from the accept thread).
     * @param socket Socket; the socket returned by accept
     */
    void setClientSocket(final Socket socket)
    {
        if (null != this.clientSocket)
        {
            System.err.println("clientSocket already set");
            return;
        }
        this.clientSocket = socket;
        try
        {
            this.ccolReader = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            this.ccolWriter = new PrintWriter(this.clientSocket.getOutputStream());
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }
        // Close the server socket to release resources and ensure that we cannot accept further connections
        try
        {
            this.serverSocket.close();
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * Retrieve the client socket for shutdown.
     * @return Socket; the socket for communication with the CCOL client
     */
    Socket getClientSocket()
    {
        return this.clientSocket;
    }

    /**
     * Let the CCOL engine determine the new state of the traffic lights and update the traffic lights accordingly.
     * @throws TrafficControlException when the CCOL engine reports an error or communication with the CCOL engine fails
     * @throws SimRuntimeException when scheduling the next evaluation fails
     */
    @SuppressWarnings("unused")
    private void step() throws TrafficControlException, SimRuntimeException
    {
        // TODO time should be formatted as date, hour, etc.
        String message = String.format("STEP %s", this.simulator.getSimulatorTime());
        this.ccolWriter.print(message);
        try
        {
            String result = this.ccolReader.readLine();
            // TODO parse the result and update the state of traffic lights accordingly
            // Protocol must ensure that we know it when all updates have been received.
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }
        // Schedule the next step.
        this.simulator.scheduleEventRel(EVALUATION_INTERVAL, this, this, "step", null);
    }

    /** {@inheritDoc} */
    @Override
    public void notify(final EventInterface event) throws RemoteException
    {
        EventTypeInterface eventType = event.getType();
        if (eventType.equals(ReplicationInterface.END_REPLICATION_EVENT))
        {
            if (null != this.serverSocket)
            {
                try
                {
                    this.serverSocket.close();
                }
                catch (IOException exception)
                {
                    exception.printStackTrace();
                }
                this.serverSocket = null;
            }
            if (null != this.clientSocket)
            {
                try
                {
                    this.clientSocket.close();
                }
                catch (IOException exception)
                {
                    exception.printStackTrace();
                }
                this.clientSocket = null;
            }
        }
        // Tracing etc. not implemented yet.
    }

    /** {@inheritDoc} */
    @Override
    public String getId()
    {
        return this.id;
    }

    /** {@inheritDoc} */
    @Override
    public String getFullId()
    {
        return this.id;
    }

    /** {@inheritDoc} */
    @Override
    public void updateDetector(final String detectorId, final boolean detectingGTU)
    {
        // FIXME: format of messages is TBD
        String message = String.format("DET %s %s", detectorId, detectingGTU);
        this.ccolWriter.print(message);
    }

    /** {@inheritDoc} */
    @Override
    public InvisibleObjectInterface clone(final OTSSimulatorInterface newSimulator, final Network newNetwork)
            throws NetworkException
    {
        // FIXME: implement the clone() for CCOL
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Serializable getSourceId()
    {
        return "CCOL";
    }

    /** {@inheritDoc} */
    @Override
    public Container getDisplayContainer()
    {
        return null; // For now, CCOL does not have a display panel
    }

}
