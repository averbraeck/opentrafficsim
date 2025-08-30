package org.opentrafficsim.trafficcontrol.ccol;

import java.awt.Container;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.Set;

import org.djunits.unit.DurationUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djutils.event.Event;
import org.djutils.event.EventType;
import org.djutils.event.LocalEventProducer;
import org.djutils.exceptions.Try;
import org.opentrafficsim.core.dsol.OtsSimulator;
import org.opentrafficsim.road.network.lane.object.detector.TrafficLightDetector;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLight;
import org.opentrafficsim.trafficcontrol.ActuatedTrafficController;
import org.opentrafficsim.trafficcontrol.TrafficControlException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.experiment.Replication;

/**
 * Communication link with a CCOL traffic control program.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class Ccol extends LocalEventProducer implements ActuatedTrafficController
{
    /** */
    private static final long serialVersionUID = 20170126L;

    /** Name of this CCOL traffic controller. */
    private final String id;

    /** The simulator. */
    private final OtsSimulator simulator;

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
     * @param id id of the traffic controller
     * @param controlProgram name of the CCOL program that this CCOL link must communicate with
     * @param trafficLights the traffic lights. The ids of the traffic lights must end with two digits that match the stream
     *            numbers as used in the traffic control program
     * @param sensors the traffic sensors. The ids of the traffic sensors must end with three digits; the first two of those
     *            must match the stream and sensor numbers used in the traffic control program
     * @param simulator the simulation engine
     * @throws TrafficControlException on failure to initialize the connection to the external CCOL program
     * @throws SimRuntimeException on failure to schedule the first evaluation event
     */
    public Ccol(final String id, final String controlProgram, final Set<TrafficLight> trafficLights,
            final Set<TrafficLightDetector> sensors, final OtsSimulator simulator)
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
                        setClientSocket(Ccol.this.serverSocket.accept());
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
        this.simulator.scheduleEventRel(Duration.ZERO, () -> Try.execute(() -> step(), "Exception during Ccol step."));
        this.simulator.addListener(this, Replication.END_REPLICATION_EVENT);
    }

    /**
     * Set the client socket (called from the accept thread).
     * @param socket the socket returned by accept
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
     * @return the socket for communication with the CCOL client
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
        this.simulator.scheduleEventRel(EVALUATION_INTERVAL, () -> Try.execute(() -> step(), "Exception during Ccol step."));
    }

    @Override
    public void notify(final Event event) throws RemoteException
    {
        EventType eventType = event.getType();
        if (eventType.equals(Replication.END_REPLICATION_EVENT))
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

    @Override
    public String getId()
    {
        return this.id;
    }

    @Override
    public String getFullId()
    {
        return this.id;
    }

    @Override
    public void updateDetector(final String detectorId, final boolean detectingGTU)
    {
        // FIXME: format of messages is TBD
        String message = String.format("DET %s %s", detectorId, detectingGTU);
        this.ccolWriter.print(message);
    }

    @Override
    public Container getDisplayContainer()
    {
        return null; // For now, CCOL does not have a display panel
    }

}
