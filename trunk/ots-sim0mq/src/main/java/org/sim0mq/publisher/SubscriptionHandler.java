package org.sim0mq.publisher;

import java.rmi.RemoteException;

import org.djunits.Throw;
import org.djutils.event.EventInterface;
import org.djutils.event.EventListenerInterface;
import org.djutils.event.EventProducerInterface;
import org.djutils.event.EventType;
import org.djutils.metadata.MetaData;

/**
 * Data collection that can be listed and has subscription to change events.
 * <p>
 * Copyright (c) 2020-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2020-02-13 11:08:16 +0100 (Thu, 13 Feb 2020) $, @version $Revision: 6383 $, by $Author: pknoppers $,
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class SubscriptionHandler implements EventListenerInterface
{
    /** ... */
    private static final long serialVersionUID = 20200417L;

    /** Id of this SubscriptionHandler. */
    private final String id;

    /** Transceiver to retrieve the data right now; e.g. GTUIdTransceiver. */
    private final TransceiverInterface listTransceiver;

    /** Event producer for add, remove, or change events; e.g. the OTSNetwork. */
    private final LookupEventProducerInterface eventProducerForAddRemoveOrChange;

    /** EventType to subscribe to in order to receive creation of added object events; e.g. NETWORK.GTU_ADD_EVENT. */
    private final EventType addedEventType;

    /** EventType to subscribe to in order to receive removed object events; e.g. NETWORK.GTU_REMOVE_EVENT. */
    private final EventType removedEventType;

    /** EventType to subscript to in order to receive change of the collection, or object events. */
    private final EventType changeEventType;

    /** SubscriptionHandler that handles subscriptions to individual objects; e.g. GTU.MOVE_EVENT. */
    private final SubscriptionHandler elementSubscriptionHandler;

    /**
     * Create a new SubscriptionHandler.
     * @param id String; id of the new SubscriptionHandler
     * @param listTransceiver TransceiverInterface; transceiver to retrieve the data of <i>the addressed object</i> right now
     * @param eventProducerForAddRemoveOrChange LookupEventProducerInterface; the event producer that can emit the
     *            <code>addedEventType</code>, <code>removedEventType</code>, or <code>changeEventType</code> events
     * @param addedEventType EventType; event type that signals that a new element has been added, should be null if there is no
     *            added event type for the data
     * @param removedEventType EventType; event type that signals that an element has been removed, should be null if there is
     *            no removed event type for the data
     * @param changeEventType EventType; event type that signals that an element has been changed, should be null if there is no
     *            change event type for the data
     * @param elementSubscriptionHandler SubscriptionHandler; SubscriptionHandler for events produced by the underlying elements
     */
    SubscriptionHandler(final String id, final TransceiverInterface listTransceiver,
            final LookupEventProducerInterface eventProducerForAddRemoveOrChange, final EventType addedEventType,
            final EventType removedEventType, final EventType changeEventType,
            final SubscriptionHandler elementSubscriptionHandler)
    {
        this.id = id;
        this.listTransceiver = listTransceiver;
        this.eventProducerForAddRemoveOrChange = eventProducerForAddRemoveOrChange;
        this.addedEventType = addedEventType;
        this.removedEventType = removedEventType;
        this.changeEventType = changeEventType;
        this.elementSubscriptionHandler = elementSubscriptionHandler;
    }

    /**
     * Report what payload is required to retrieve a list of all elements, or data and what format a result would have.
     * @return MetaData; description of the payload required to retrieve a list of all elements, or data and what format a
     *         result would have
     */
    public MetaData listRequestMetaData()
    {
        return this.listTransceiver.getAddressFields();
    }

    /**
     * Report what the payload format of the result of the list transceiver.
     * @return MetaData; the payload format of the result of the list transceiver
     */
    public MetaData listResultMetaData()
    {
        return this.listTransceiver.getResultFields();
    }

    /**
     * Retrieve a data collection.
     * @param address Object[]; address of the requested data collection
     * @throws RemoteException when communication fails
     */
    public void get(final Object[] address) throws RemoteException
    {
        Object[] dataCollection = this.listTransceiver.get(address);
        // TODO transmit result to Sim0MQ master
    }

    /**
     * Create a new subscription to ADD events.
     * @param address Object[]; the data that is required to find the correct EventProducer
     * @param eventType EventType; one of the event types that the addressed EventProducer can fire
     * @throws RemoteException when communication fails
     */
    private void subscribeTo(final Object[] address, final EventType eventType) throws RemoteException
    {
        Throw.whenNull(eventType, "eventType may not be null");
        EventProducerInterface epi = this.eventProducerForAddRemoveOrChange.lookup(address);
        if (null != epi)
        {
            epi.addListener(this, eventType); // TODO complain if there was already a subscription?
        }
        // else: Not necessarily bad; some EventProducers (e.g. GTUs) may disappear at any time
        // TODO inform the master
    }

    /**
     * Cancel a subscription to ADD events.
     * @param address Object[]; the data that is required to find the correct EventProducer
     * @param eventType EventType; one of the event types that the addressed EventProducer can fire
     * @throws RemoteException when communication fails
     */
    private void unsubscribeFrom(final Object[] address, final EventType eventType) throws RemoteException
    {
        Throw.whenNull(eventType, "eventType may not be null");
        EventProducerInterface epi = this.eventProducerForAddRemoveOrChange.lookup(address);
        if (null != epi)
        {
            epi.removeListener(this, eventType); // TODO complain if there was no subscription?
        }
        // else: Not necessarily bad; some EventProducers (e.g. GTUs) may disappear at any time
        // TODO inform the master
    }

    /**
     * Retrieve the id of this SubscriptionHandler.
     * @return String; the id of this SubscriptionHandler
     */
    public final String getId()
    {
        return this.id;
    }

    /** {@inheritDoc} */
    @Override
    public void notify(final EventInterface event) throws RemoteException
    {
        MetaData metaData = event.getType().getMetaData();
        Object[] result = new Object[1 + metaData.size()];
        result[0] = event.getType().getName();
        Object payload = event.getContent();
        if (payload instanceof Object[])
        {
            for (int index = 0; index < event.getType().getMetaData().size(); index++)
            {
                result[1 + index] = ((Object[]) payload)[index];
            }
        }
        else
        {
            result[1] = payload;
        }
        sendResult(result);
    }

    /**
     * The commands that a SubscriptionHandler understands.
     */
    enum Command
    {
        /** Subscribe to add events. */
        SUBSCRIBE_TO_ADD,
        /** Subscribe to remove events. */
        SUBSCRIBE_TO_REMOVE,
        /** Subscribe to change events. */
        SUBSCRIBE_TO_CHANGE,
        /** Unsubscribe to add events. */
        UNSUBSCRIBE_TO_ADD,
        /** Unsubscribe to remove events. */
        UNSUBSCRIBE_TO_REMOVE,
        /** Unsubscribe to change events. */
        UNSUBSCRIBE_TO_CHANGE,
        /** Get current set. */
        GET_CURRENT_POPULATION;
    }

    /**
     * Execute one command.
     * @param command Command; the command
     * @param address Object[] the address of the object on which the command must be applied
     * @throws RemoteException on communication failure
     */
    public void executeCommand(final Command command, final Object[] address) throws RemoteException
    {
        switch (command)
        {
            case SUBSCRIBE_TO_ADD:
                subscribeTo(address, this.addedEventType);
                break;

            case GET_CURRENT_POPULATION:
                sendResult(this.listTransceiver.get(address));
                break;

            case SUBSCRIBE_TO_CHANGE:
                subscribeTo(address, this.changeEventType);
                break;

            case SUBSCRIBE_TO_REMOVE:
                subscribeTo(address, this.removedEventType);
                break;

            case UNSUBSCRIBE_TO_ADD:
                unsubscribeFrom(address, this.addedEventType);
                break;

            case UNSUBSCRIBE_TO_CHANGE:
                unsubscribeFrom(address, this.changeEventType);
                break;

            case UNSUBSCRIBE_TO_REMOVE:
                unsubscribeFrom(address, this.removedEventType);
                break;

            default:
                // Cannot happen
                break;

        }
    }

    /**
     * Stub. Should send data over Sim0MQ to master
     * @param data Object[]; the data to transmit
     */
    private void sendResult(final Object[] data)
    {
        if (null == data)
        {
            System.out.println("NULL");
            return;
        }
        for (int index = 0; index < data.length; index++)
        {
            System.out.println(index + "\t" + data[index]);
        }
    }

}

/**
 * Object that can find the EventProducerInterface object for an address.
 */
interface LookupEventProducerInterface
{
    /**
     * Find the EventProducerInterface with the given address.
     * @param address Object[]; the address
     * @return EventProducerInterface; can be null in case the address is (no longer) valid
     * @throws IndexOutOfBoundsException when the address has an invalid format
     */
    EventProducerInterface lookup(Object[] address) throws IndexOutOfBoundsException;
}
