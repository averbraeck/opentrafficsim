package org.sim0mq.publisher;

import java.io.Serializable;
import java.rmi.RemoteException;

import org.djutils.event.EventInterface;
import org.djutils.event.EventListenerInterface;
import org.djutils.event.EventProducer;
import org.djutils.event.EventProducerInterface;
import org.djutils.event.TimedEvent;
import org.djutils.event.TimedEventType;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;
import org.djutils.serialization.SerializationException;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.sim0mq.Sim0MQException;

import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

/**
 * Transceiver for simulator state change events.
 * <p>
 * Copyright (c) 2020-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2020-02-13 11:08:16 +0100 (Thu, 13 Feb 2020) $, @version $Revision: 6383 $, by $Author: pknoppers $,
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class SimulatorStateTransceiver extends AbstractTransceiver
{
    /** The simulator. */
    private final OTSSimulatorInterface simulator;

    /** Multiplexes SimulatorInterface.START_EVENT and SimulatorInterface.STOP_EVENT. */
    private final EventProducerInterface eventMultiplexer;

    /**
     * Construct a new SimulatorStateTransceiver.
     * @param simulator OTSSimulatorInterface; the simulator
     * @throws RemoteException
     */
    public SimulatorStateTransceiver(final OTSSimulatorInterface simulator) throws RemoteException
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
        return new Object[] { result };
    }

    /** Reuslt of the getLookupEventProducerInterface method. */
    private LookupEventProducerInterface lepi = new LookupEventProducerInterface()
    {
        @Override
        public EventProducerInterface lookup(final Object[] address, final ReturnWrapperImpl returnWrapper)
                throws Sim0MQException, SerializationException
        {
            String bad = AbstractTransceiver.verifyMetaData(MetaData.EMPTY, address);
            if (null != bad)
            {
                returnWrapper.nack(bad);
                return null;
            }
            return eventMultiplexer;
        }

        @Override
        public MetaData getAddressMetaData()
        {
            return MetaData.EMPTY;
        }
    };

    /**
     * Retrieve the event LookupEventProducerInterface.
     * @return EventProducerInterface; the event multiplexer
     */
    public LookupEventProducerInterface getLookupEventProducerInterface()
    {
        return this.lepi;
    }

}

/**
 * Create a subscription to SimulatorInterface.START_EVENT and SimulatorInterface.STOP_EVENT and emit a SIMULATOR_STATE_CHANGED
 * event for each.
 */
class EventMultiplexer extends EventProducer implements EventListenerInterface
{
    /** ... */
    private static final long serialVersionUID = 20200618L;

    /** The event that will be emitted for either the START_EVENT or the STOP_EVENT. */
    public static final TimedEventType SIMULATOR_STATE_CHANGED =
            new TimedEventType(new MetaData("SIMULATOR_STATE_CHANGED_EVENT", "simulator started or stopped",
                    new ObjectDescriptor[] { new ObjectDescriptor("New simulator state",
                            "New simulator state; true if running; false if stopped", Boolean.class) }));

    /**
     * @param simulator OTSSimulatorInterface; the simulator
     * @throws RemoteException
     */
    EventMultiplexer(final OTSSimulatorInterface simulator) throws RemoteException
    {
        simulator.addListener(this, SimulatorInterface.START_EVENT);
        simulator.addListener(this, SimulatorInterface.STOP_EVENT);
    }

    /** {@inheritDoc} */
    @Override
    public void notify(final EventInterface event) throws RemoteException
    {
        fireTimedEvent(SIMULATOR_STATE_CHANGED, event.getType().equals(SimulatorInterface.START_EVENT),
                ((TimedEvent<?>) event).getTimeStamp());
    }

    /** {@inheritDoc} */
    @Override
    public Serializable getSourceId()
    {
        // TODO Auto-generated method stub
        return "EventMultiplexer";
    }

}
