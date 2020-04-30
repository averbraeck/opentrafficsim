package org.sim0mq.publisher;

import java.rmi.RemoteException;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;

import org.djunits.Throw;
import org.djutils.event.EventInterface;
import org.djutils.event.EventListenerInterface;
import org.djutils.event.EventProducerInterface;
import org.djutils.event.EventType;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;
import org.djutils.serialization.SerializationException;
import org.sim0mq.Sim0MQException;

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
public class SubscriptionHandler
{
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

    /** The currently active subsciptions. */
    private final Map<ReturnWrapper, Subscription> subscriptions = new LinkedHashMap<>();

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
        Throw.whenNull(id, "Id may not be null");
        Throw.when(
                null == eventProducerForAddRemoveOrChange
                        && (addedEventType != null || removedEventType != null || changeEventType != null),
                NullPointerException.class,
                "eventProducerForAddRemoveOrChange may not be null when any of those events is non-null");
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
     * @param returnWrapper ReturnWrapper; to send back the result
     * @throws RemoteException when communication fails
     * @throws SerializationException
     * @throws Sim0MQException
     */
    public void get(final Object[] address, final ReturnWrapper returnWrapper)
            throws RemoteException, Sim0MQException, SerializationException
    {
        sendResult(this.listTransceiver.get(address), returnWrapper);
    }

    /**
     * Return the set of supported commands.
     * @return EnumSet<Command>; the set of supported commands.
     */
    public final EnumSet<Command> subscriptionOptions()
    {
        EnumSet<Command> result = EnumSet.noneOf(Command.class);
        if (null != this.addedEventType)
        {
            result.add(Command.SUBSCRIBE_TO_ADD);
            result.add(Command.UNSUBSCRIBE_FROM_ADD);
        }
        if (null != this.removedEventType)
        {
            result.add(Command.SUBSCRIBE_TO_REMOVE);
            result.add(Command.UNSUBSCRIBE_FROM_REMOVE);
        }
        if (null != this.changeEventType)
        {
            result.add(Command.SUBSCRIBE_TO_CHANGE);
            result.add(Command.UNSUBSCRIBE_FROM_CHANGE);
        }
        if (null != this.listTransceiver)
        {
            result.add(Command.GET_CURRENT);
            result.add(Command.GET_ADDRESS_META_DATA);
            result.add(Command.GET_RESULT_META_DATA);
        }
        return result;
    }

    /**
     * Create a new subscription to ADD events.
     * @param address Object[]; the data that is required to find the correct EventProducer
     * @param eventType EventType; one of the event types that the addressed EventProducer can fire
     * @param returnWrapper ReturnWrapper; generates envelopes for the returned events
     * @throws RemoteException when communication fails
     */
    private void subscribeTo(final Object[] address, final EventType eventType, final ReturnWrapper returnWrapper)
            throws RemoteException
    {
        Throw.whenNull(eventType, "eventType may not be null");
        EventProducerInterface epi = this.eventProducerForAddRemoveOrChange.lookup(address);
        if (null != epi)
        {
            Subscription subscription = this.subscriptions.get(returnWrapper);
            if (null == subscription)
            {
                subscription = new Subscription(returnWrapper);
            }
            epi.addListener(subscription, eventType); // TODO complain if there was already a subscription?
            this.subscriptions.put(returnWrapper, subscription);
        }
        // else: Not necessarily bad; some EventProducers (e.g. GTUs) may disappear at any time
        // TODO inform the master
    }

    /**
     * Cancel a subscription to ADD events.
     * @param address Object[]; the data that is required to find the correct EventProducer
     * @param eventType EventType; one of the event types that the addressed EventProducer can fire
     * @param returnWrapper ReturnWrapper; the ReturnWapper that sent the results until now
     * @throws RemoteException when communication fails
     */
    private void unsubscribeFrom(final Object[] address, final EventType eventType, final ReturnWrapper returnWrapper)
            throws RemoteException
    {
        Throw.whenNull(eventType, "eventType may not be null");
        EventProducerInterface epi = this.eventProducerForAddRemoveOrChange.lookup(address);
        if (null != epi)
        {
            Subscription subscription = this.subscriptions.get(returnWrapper);
            Throw.whenNull(subscription, "No subscription found that can be unsubscribed");
            epi.removeListener(subscription, eventType); // TODO complain if there was no subscription?
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
        UNSUBSCRIBE_FROM_ADD,
        /** Unsubscribe to remove events. */
        UNSUBSCRIBE_FROM_REMOVE,
        /** Unsubscribe to change events. */
        UNSUBSCRIBE_FROM_CHANGE,
        /** Get current set (if a collection), c.q. state (if properties of one object). */
        GET_CURRENT,
        /** Get the address meta data. */
        GET_ADDRESS_META_DATA,
        /** Get the result meta data. */
        GET_RESULT_META_DATA;
    }

    /**
     * Execute one command.
     * @param command Command; the command
     * @param address Object[] the address of the object on which the command must be applied
     * @param returnWrapper ReturnWrapper; envelope generator for replies
     * @throws RemoteException on communication failure
     * @throws SerializationException
     * @throws Sim0MQException
     */
    public void executeCommand(final Command command, final Object[] address, final ReturnWrapper returnWrapper)
            throws RemoteException, Sim0MQException, SerializationException
    {
        Throw.whenNull(command, "Command may not be null");
        Throw.whenNull(returnWrapper, "ReturnWrapper may not be null");
        switch (command)
        {
            case SUBSCRIBE_TO_ADD:
                subscribeTo(address, this.addedEventType, returnWrapper);
                break;

            case SUBSCRIBE_TO_CHANGE:
                subscribeTo(address, this.changeEventType, returnWrapper);
                break;

            case SUBSCRIBE_TO_REMOVE:
                subscribeTo(address, this.removedEventType, returnWrapper);
                break;

            case UNSUBSCRIBE_FROM_ADD:
                unsubscribeFrom(address, this.addedEventType, returnWrapper);
                break;

            case UNSUBSCRIBE_FROM_CHANGE:
                unsubscribeFrom(address, this.changeEventType, returnWrapper);
                break;

            case UNSUBSCRIBE_FROM_REMOVE:
                unsubscribeFrom(address, this.removedEventType, returnWrapper);
                break;

            case GET_CURRENT:
                sendResult(this.listTransceiver.get(address), returnWrapper);
                break;

            case GET_ADDRESS_META_DATA:
                sendResult(extractObjectDescriptorClassNames(this.listTransceiver.getAddressFields().getObjectDescriptors()),
                        returnWrapper);
                break;

            case GET_RESULT_META_DATA:
                sendResult(extractObjectDescriptorClassNames(this.listTransceiver.getResultFields().getObjectDescriptors()),
                        returnWrapper);
                break;

            default:
                // Cannot happen
                break;

        }
    }

    /**
     * Extract the class names from an array of ObjectDescriptor.
     * @param objectDescriptors ObjectDescriptor[]; the array of ObjectDescriptor
     * @return Object[]; the class names
     */
    private Object[] extractObjectDescriptorClassNames(final ObjectDescriptor[] objectDescriptors)
    {
        Object[] result = new Object[objectDescriptors.length];
        for (int index = 0; index < objectDescriptors.length; index++)
        {
            result[index] = objectDescriptors[index].getObjectClass().getName();
        }
        return result;
    }

    /**
     * Stub. Should send data over Sim0MQ to master
     * @param data Object[]; the data to transmit
     * @param returnWrapper ReturnWrapper; envelope constructor for returned results
     * @throws SerializationException
     * @throws Sim0MQException
     */
    private void sendResult(final Object[] data, final ReturnWrapper returnWrapper)
            throws Sim0MQException, SerializationException
    {
        returnWrapper.encodeReplyAndTransmit(data);
        // if (null == data)
        // {
        // System.out.println("NULL");
        // return;
        // }
        // for (int index = 0; index < data.length; index++)
        // {
        // System.out.println(index + "\t" + data[index]);
        // }
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "SubscriptionHandler [id=" + id + ", listTransceiver=" + listTransceiver + ", eventProducerForAddRemoveOrChange="
                + eventProducerForAddRemoveOrChange + ", addedEventType=" + addedEventType + ", removedEventType="
                + removedEventType + ", changeEventType=" + changeEventType + ", elementSubscriptionHandler="
                + elementSubscriptionHandler + "]";
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

/**
 * Handles one subscription.
 */
class Subscription implements EventListenerInterface
{
    /** ... */
    private static final long serialVersionUID = 20200428L;

    /** Generates envelopes for the messages sent over Sim0MQ. */
    private final ReturnWrapper returnWrapper;

    /**
     * Construct a new Subscription.
     * @param returnWrapper ReturnWrapper; envelope generator for the messages
     */
    Subscription(final ReturnWrapper returnWrapper)
    {
        this.returnWrapper = returnWrapper;
    }

    /** {@inheritDoc} */
    @Override
    public void notify(final EventInterface event) throws RemoteException
    {
        // TODO: figure out how to include the time stamp if event is a TimedEvent.
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
        try
        {
            this.returnWrapper.encodeReplyAndTransmit(result);
        }
        catch (Sim0MQException | SerializationException e)
        {
            e.printStackTrace();
        }
    }

}
