package org.opentrafficsim.sim0mq.publisher;

import java.io.Serializable;
import java.rmi.RemoteException;

import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.event.EventProducer;
import org.djutils.event.EventType;
import org.djutils.event.LocalEventProducer;
import org.djutils.event.TimedEvent;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;
import org.djutils.serialization.SerializationException;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.sim0mq.Sim0MQException;

import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

/**
 * Transceiver for simulator state change events.
 * <p>
 * Copyright (c) 2020-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class SimulatorStateTransceiver extends AbstractTransceiver
{
    /** The simulator. */
    private final OtsSimulatorInterface simulator;

    /** Multiplexes SimulatorInterface.START_EVENT and SimulatorInterface.STOP_EVENT. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    final EventProducer eventMultiplexer;

    /** The event that will be emitted for either the START_EVENT or the STOP_EVENT. */
    public static final EventType SIMULATOR_STATE_CHANGED = new EventType(new MetaData("SIMULATOR_STATE_CHANGED_EVENT",
            "simulator started or stopped", new ObjectDescriptor[] {new ObjectDescriptor("New simulator state",
                    "New simulator state; true if running; false if stopped", Boolean.class)}));

    /**
     * Construct a new SimulatorStateTransceiver.
     * @param simulator OtsSimulatorInterface; the simulator
     * @throws RemoteException on network error
     */
    public SimulatorStateTransceiver(final OtsSimulatorInterface simulator) throws RemoteException
    {
        super("Simulator state transceiver", MetaData.EMPTY,
                new MetaData("SIMULATOR_STATE_CHANGED_EVENT", "simulator state changed"));
        this.simulator = simulator;
        this.eventMultiplexer = new EventMultiplexer(simulator);
    }

    /** {@inheritDoc} */
    @Override
    public Object[] get(final Object[] address, final ReturnWrapper returnWrapper)
            throws RemoteException, Sim0MQException, SerializationException
    {
        AbstractTransceiver.verifyMetaData(MetaData.EMPTY, address);
        String result = null;
        if (this.simulator.isInitialized())
        {
            if (this.simulator.isStartingOrRunning())
            {
                result = "Starting or running";
            }
            else
            {
                result = "Stopping or stopped";
            }
        }
        else
        {
            result = "Not (yet) initialized";
        }
        return new Object[] {result};
    }

    /** Result of the getLookupEventProducer method. */
    private LookupEventProducer lepi = new LookupEventProducer()
    {
        @Override
        public EventProducer lookup(final Object[] address, final ReturnWrapper returnWrapper)
                throws Sim0MQException, SerializationException
        {
            String bad = AbstractTransceiver.verifyMetaData(MetaData.EMPTY, address);
            if (null != bad)
            {
                returnWrapper.nack(bad);
                return null;
            }
            return SimulatorStateTransceiver.this.eventMultiplexer;
        }

        @Override
        public MetaData getAddressMetaData()
        {
            return MetaData.EMPTY;
        }
    };

    /**
     * Retrieve the event LookupEventProducer.
     * @return EventProducerInterface; the event multiplexer
     */
    public LookupEventProducer getLookupEventProducer()
    {
        return this.lepi;
    }

}

/**
 * Create a subscription to SimulatorInterface.START_EVENT and SimulatorInterface.STOP_EVENT and emit a SIMULATOR_STATE_CHANGED
 * event for each.
 */
class EventMultiplexer extends LocalEventProducer implements EventListener
{
    /** ... */
    private static final long serialVersionUID = 20200618L;

    /**
     * @param simulator OtsSimulatorInterface; the simulator
     * @throws RemoteException on network error
     */
    EventMultiplexer(final OtsSimulatorInterface simulator) throws RemoteException
    {
        simulator.addListener(this, SimulatorInterface.START_EVENT);
        simulator.addListener(this, SimulatorInterface.STOP_EVENT);
    }

    /** {@inheritDoc} */
    @Override
    public void notify(final Event event) throws RemoteException
    {
        notifyTimedEvent(event);
    }

    /**
     * Avoid a compilation error with Java 11. Could be bug https://bugs.openjdk.java.net/browse/JDK-8206142 which is
     * unsolved...
     * @param <C> the casting class for the event timestamp
     * @param event the event to be notified of
     */
    private <C extends Serializable & Comparable<C>> void notifyTimedEvent(final Event event)
    {
        @SuppressWarnings("unchecked")
        TimedEvent<C> timedEvent = (TimedEvent<C>) event;
        fireTimedEvent(SimulatorStateTransceiver.SIMULATOR_STATE_CHANGED,
                event.getType().equals(SimulatorInterface.START_EVENT), timedEvent.getTimeStamp());
    }

}
