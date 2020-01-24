package org.opentrafficsim.imb.transceiver;

import java.rmi.RemoteException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.djutils.event.EventInterface;
import org.djutils.event.EventProducer;
import org.djutils.event.EventProducerInterface;
import org.djutils.event.EventType;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.imb.IMBException;
import org.opentrafficsim.imb.connector.Connector;

import nl.tno.imb.TByteBuffer;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;

/**
 * Provide the basic implementation of a Transceiver from which targeted classes can extend.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Sep 9, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public abstract class AbstractTransceiver extends EventProducer implements EventTransceiver
{
    /** */
    private static final long serialVersionUID = 20160909L;

    /** An id to identify the channel, e.g., "GTU" or "Simulator Control". */
    private final String id;

    /** The IMB connector through which this transceiver communicates. */
    private final Connector connector;

    /** The simulator to schedule the incoming notifications on. */
    private final DEVSSimulatorInterface.TimeDoubleUnit simulator;

    /** The map to indicate which IMB message handler to use for a given IMB message type. */
    private Map<String, IMBMessageHandler> imbMessageHandlerMap = new LinkedHashMap<>();

    /** The map to indicate which OTS EventType is mapped to which IMB event name (String). */
    private Map<EventType, String> otsToIMBMap = new LinkedHashMap<>();

    /** The map to indicate which Transformer to use for a given OTS EventType. */
    private Map<EventType, OTSToIMBTransformer> otsTransformerMap = new LinkedHashMap<>();

    /**
     * Construct a new AbstractTranceiver.
     * @param id String; an id to identify the channel, e.g., "GTU" or "Simulator Control"
     * @param connector Connector; the IMB connector through which this transceiver communicates
     * @param simulator DEVSSimulatorInterface.TimeDoubleUnit; the simulator to schedule the incoming notifications on
     * @throws NullPointerException in case one of the arguments is null.
     */
    public AbstractTransceiver(final String id, final Connector connector,
            final DEVSSimulatorInterface.TimeDoubleUnit simulator)
    {
        Throw.whenNull(connector, "Connector can not be null");
        Throw.whenNull(id, "id can not be null");
        Throw.whenNull(simulator, "simulator can not be null");
        this.id = id;
        this.connector = connector;
        this.simulator = simulator;
    }

    /**
     * Make a connection from OTS to IMB, and and send a NEW message to IMB for the imbEventName with a corresponding payload.
     * Store the transformer to use to create the CHANGE IMB messages. The Transceiver subscribes to the relevant information of
     * the OTS EventProducer so it can send CHANGE messages from now on.
     * @param producer EventProducerInterface; the OTS event producer that notifies this Transceiver about state changes
     * @param eventType EventType; the event type that corresponds to the state change for this channel
     * @param imbEventName String; the IMB event name for the message to send
     * @param imbNewPayload Object[]; the information to send to IMB with the IMB NEW message
     * @param transformer OTSToIMBTransformer; the transformer to use for create the IMB CHANGE events from an OTS Event content
     * @throws NullPointerException in case one of the arguments is null.
     * @throws IMBException in case the mapping from an EventType to an IMB event name is different from a previous time when a
     *             mapping was registered for the same EventType (but for a different OTS EventProducer instance), when the
     *             transformer for an EventType was changed a previous time when a mapping was registered, or when the
     *             subscription to the OTS EventProducer fails.
     */
    public final void addOTSToIMBChannel(final EventProducerInterface producer, final EventType eventType,
            final String imbEventName, Object[] imbNewPayload, final OTSToIMBTransformer transformer) throws IMBException
    {
        Throw.whenNull(producer, "producer cannot be null");
        Throw.whenNull(eventType, "eventType cannot be null");
        Throw.whenNull(imbEventName, "imbEventName cannot be null");
        Throw.whenNull(imbNewPayload, "imbNewPayload cannot be null");
        Throw.whenNull(transformer, "transformer cannot be null");
        Throw.when(this.otsToIMBMap.containsKey(eventType) && !this.otsToIMBMap.get(eventType).equals(imbEventName),
                IMBException.class, "mapping of EventType to IMB name cannot be changed");
        Throw.when(this.otsTransformerMap.containsKey(eventType) && !this.otsTransformerMap.get(eventType).equals(transformer),
                IMBException.class, "mapping of EventType to Transformer cannot be changed");

        try
        {
            this.connector.postIMBMessage(imbEventName, Connector.IMBEventType.NEW, imbNewPayload);
            this.otsToIMBMap.put(eventType, imbEventName);
            this.otsTransformerMap.put(eventType, transformer);
            producer.addListener(this, eventType);
        }
        catch (RemoteException exception)
        {
            throw new IMBException(exception);
        }
    }

    /**
     * Remove a connection from OTS to IMB, and and send a DELETE message to IMB for the imbEventName with a corresponding
     * payload. The Transceiver subscription to the relevant information of the OTS EventProducer instance is removed. <br>
     * Note that the mappings of EventType to IMB Event name and of the EventType to the transformer are not removed. There can
     * be more instances of OTS EventProducer that use this channel. E.g., when all GTUs communicate through one channel using
     * the same Transformer, the mappings should not be removed when one GTU leaves the model.
     * @param producer EventProducerInterface; the OTS event producer to which we should stop listening
     * @param eventType EventType; the event type that corresponds for this channel
     * @param imbDeletePayload Object[]; the information to send to IMB with the IMB DELETE message
     * @throws NullPointerException in case one of the arguments is null.
     * @throws IMBException when the cancellation of the subscription to the OTS EventProducer fails, or when the EventType for
     *             the channel was not registered with an addOTSToIMBChannel call.
     */
    public final void removeOTSToIMBChannel(final EventProducerInterface producer, final EventType eventType,
            Object[] imbDeletePayload) throws IMBException
    {
        Throw.whenNull(producer, "producer cannot be null");
        Throw.whenNull(eventType, "eventType cannot be null");
        Throw.whenNull(imbDeletePayload, "imbDeletePayload cannot be null");
        Throw.when(!this.otsToIMBMap.containsKey(eventType), IMBException.class,
                "EventType " + eventType + " for this channel was not registered with an addOTSToIMBChannel call");

        try
        {
            producer.removeListener(this, eventType);
            this.connector.postIMBMessage(this.otsToIMBMap.get(eventType), Connector.IMBEventType.DELETE, imbDeletePayload);
            // Do not implement this.otsToIMBMap.remove(eventType), as there may be more listeners for the same EventType.
        }
        catch (Exception exception)
        {
            throw new IMBException(exception);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void notify(final EventInterface event) throws RemoteException
    {
        String imbEventName = this.otsToIMBMap.get(event.getType());
        if (null != imbEventName)
        {
            // if (!event.getType().equals(GTU.MOVE_EVENT))
            // {
            // System.out.println("About to transmit to IMB event " + imbEventName + " " + event.getContent());
            // }
            try
            {
                this.connector.postIMBMessage(imbEventName, Connector.IMBEventType.CHANGE,
                        this.otsTransformerMap.get(event.getType()).transform(event));
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
            }
        }
    }

    /**
     * Register a new channel for sending an IMB message to an OTS EventListener. Note that the listeners are not registered
     * directly as an EventListener with the addListener method. Instead, we directly call the notify(event) method on the
     * listeners.
     * @param imbEventName String; the name of the IMB event
     * @param eventType EventType; the event type that the listener subscribes to
     * @param imbToOTSTransformer IMBToOTSTransformer; the transformer that creates the event content and identifies the exact
     *            listener on the basis of the IBM event payload, e.g., on the basis of an id within the payload
     * @throws IMBException in case the registration fails
     */
    public void addIMBtoOTSChannel(final String imbEventName, final EventType eventType,
            final IMBToOTSTransformer imbToOTSTransformer) throws IMBException
    {
        Throw.whenNull(imbEventName, "imbEventName cannot be null");
        Throw.whenNull(eventType, "eventType cannot be null");
        Throw.whenNull(imbToOTSTransformer, "imbToOTSTransformer cannot be null");

        this.imbMessageHandlerMap.put(imbEventName,
                new PubSubIMBMessageHandler(imbEventName, eventType, imbToOTSTransformer, this.simulator));
        this.connector.register(imbEventName, this); // tell the connector we are interested in this IMB event
    }

    /**
     * Register that we are interested in an IMB payload, but do <b>not</b> register a listener or transformer.
     * @param imbEventName String; the name of the IMB event
     * @param imbMessageHandler IMBMessageHandler; IMBMessageHandler the message handler that takes care of the IMB message
     * @throws IMBException in case registration fails
     */
    public void addIMBtoOTSChannel(final String imbEventName, final IMBMessageHandler imbMessageHandler) throws IMBException
    {
        Throw.whenNull(imbEventName, "imbEventName cannot be null");
        Throw.whenNull(imbMessageHandler, "imbMessageHandler cannot be null");

        this.imbMessageHandlerMap.put(imbEventName, imbMessageHandler); // register the handler
        this.connector.register(imbEventName, this); // tell the connector we are interested in this IMB event
    }

    /** {@inheritDoc} */
    @Override
    public void handleMessageFromIMB(final String imbEventName, final TByteBuffer imbPayload) throws IMBException
    {
        Throw.when(!this.imbMessageHandlerMap.containsKey(imbEventName), IMBException.class,
                "Could not find IMB-to-OTS handler for IMB event name " + imbEventName);
        this.imbMessageHandlerMap.get(imbEventName).handle(imbPayload);
    }

    /** {@inheritDoc} */
    @Override
    public String getId()
    {
        return this.id;
    }

    /** {@inheritDoc} */
    @Override
    public final Connector getConnector()
    {
        return this.connector;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "AbstractTransceiver [id=" + this.id + ", connector=" + this.connector + "]";
    }

    /**
     * Retrieve the simulator.
     * @return DEVSSimulatorInterface.TimeDoubleUnit simulator
     */
    public DEVSSimulatorInterface.TimeDoubleUnit getSimulator()
    {
        return this.simulator;
    }

}
