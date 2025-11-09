package org.opentrafficsim.sim0mq.publisher;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.event.EventProducer;
import org.djutils.event.EventType;
import org.djutils.event.TimedEvent;
import org.djutils.exceptions.Throw;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;
import org.djutils.serialization.SerializationException;
import org.sim0mq.Sim0MQException;

/**
 * Data collection that can be listed and has subscription to change events.
 * <p>
 * Copyright (c) 2020-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public class SubscriptionHandler
{
    /** Id of this SubscriptionHandler. */
    private final String id;

    /** Transceiver to retrieve the data right now; e.g. GtuIdTransceiver. */
    private final TransceiverInterface listTransceiver;

    /** Event producer for add, remove, or change events; e.g. the Network. */
    private final LookupEventProducer eventProducerForAddRemoveOrChange;

    /** EventType to subscribe to in order to receive creation of added object events; e.g. Network.GTU_ADD_EVENT. */
    private final EventType addedEventType;

    /** EventType to subscribe to in order to receive removed object events; e.g. Network.GTU_REMOVE_EVENT. */
    private final EventType removedEventType;

    /** EventType to subscript to in order to receive change of the collection, or object events. */
    private final EventType changeEventType;

    /** SubscriptionHandler that handles subscriptions to individual objects; e.g. GTU.MOVE_EVENT. */
    private final SubscriptionHandler elementSubscriptionHandler;

    /** The currently active subscriptions. */
    private final Map<ReturnWrapper, Subscription> subscriptions = new LinkedHashMap<>();

    /**
     * Create a new SubscriptionHandler.
     * @param id id of the new SubscriptionHandler
     * @param listTransceiver transceiver to retrieve the data of <i>the addressed object</i> right now
     * @param eventProducerForAddRemoveOrChange the event producer that can emit the <code>addedEventType</code>,
     *            <code>removedEventType</code>, or <code>changeEventType</code> events
     * @param addedEventType event type that signals that a new element has been added, should be null if there is no added
     *            event type for the data
     * @param removedEventType event type that signals that an element has been removed, should be null if there is no removed
     *            event type for the data
     * @param changeEventType event type that signals that an element has been changed, should be null if there is no change
     *            event type for the data
     * @param elementSubscriptionHandler SubscriptionHandler for events produced by the underlying elements
     */
    public SubscriptionHandler(final String id, final TransceiverInterface listTransceiver,
            final LookupEventProducer eventProducerForAddRemoveOrChange, final EventType addedEventType,
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
     * @return description of the payload required to retrieve a list of all elements, or data and what format a result would
     *         have
     */
    public MetaData listRequestMetaData()
    {
        return this.listTransceiver.getAddressFields();
    }

    /**
     * Report what the payload format of the result of the list transceiver.
     * @return the payload format of the result of the list transceiver
     */
    public MetaData listResultMetaData()
    {
        return this.listTransceiver.getResultFields();
    }

    /**
     * Retrieve a data collection.
     * @param address address of the requested data collection
     * @param returnWrapper to send back the result
     * @throws RemoteException when communication fails
     * @throws SerializationException on context error
     * @throws Sim0MQException on DSOL error
     */
    public void get(final Object[] address, final ReturnWrapper returnWrapper)
            throws RemoteException, Sim0MQException, SerializationException
    {
        sendResult(this.listTransceiver.get(address, returnWrapper), returnWrapper);
    }

    /**
     * Retrieve the list transceiver (only for testing).
     * @return the list transceiver
     */
    public TransceiverInterface getListTransceiver()
    {
        return this.listTransceiver;
    }

    /**
     * Return the set of supported commands.
     * @return the set of supported commands.
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
     * Create a new subscription to ADD, REMOVE, or CHANGE events.
     * @param address the data that is required to find the correct EventProducer
     * @param eventType one of the event types that the addressed EventProducer can fire
     * @param returnWrapper generates envelopes for the returned events
     * @throws RemoteException when communication fails
     * @throws SerializationException should never happen
     * @throws Sim0MQException should never happen
     */
    private void subscribeTo(final Object[] address, final EventType eventType, final ReturnWrapper returnWrapper)
            throws RemoteException, Sim0MQException, SerializationException
    {
        if (null == eventType)
        {
            returnWrapper.nack("Does not support subscribe to");
            return;
        }
        String bad = AbstractTransceiver.verifyMetaData(this.eventProducerForAddRemoveOrChange.getAddressMetaData(), address);
        if (bad != null)
        {
            returnWrapper.nack("Bad address: " + bad);
            return;
        }
        EventProducer epi = this.eventProducerForAddRemoveOrChange.lookup(address, returnWrapper);
        if (null == epi)
        {
            // Not necessarily bad; some EventProducers (e.g. GTUs) may disappear at any time
            return; // NACK has been sent by this.eventProducerForAddRemoveOrChange.lookup
        }
        Subscription subscription = this.subscriptions.get(returnWrapper);
        if (null == subscription)
        {
            subscription = new Subscription(returnWrapper);
            this.subscriptions.put(returnWrapper, subscription);
        }
        if (epi.addListener(subscription, eventType))
        {
            returnWrapper.ack("Subscription created");
        }
        else
        {
            // There was already a subscription?
            returnWrapper.ack("There was already such a subscription active");
        }
        // FIXME: if the subscription is an an Object that later disappears, the subscription map will still consume memory for
        // that subscription. That could add up to a lot of memory ...
    }

    /**
     * Cancel a subscription to ADD, REMOVE, or CHANGE events.
     * @param address the data that is required to find the correct EventProducer
     * @param eventType one of the event types that the addressed EventProducer can fire
     * @param returnWrapper the ReturnWapper that sent the results until now
     * @throws RemoteException when communication fails
     * @throws SerializationException should never happen
     * @throws Sim0MQException should never happen
     */
    private void unsubscribeFrom(final Object[] address, final EventType eventType, final ReturnWrapper returnWrapper)
            throws RemoteException, Sim0MQException, SerializationException
    {
        if (null == eventType)
        {
            returnWrapper.nack("Does not support unsubscribe from");
            return;
        }
        String bad = AbstractTransceiver.verifyMetaData(this.eventProducerForAddRemoveOrChange.getAddressMetaData(), address);
        if (bad != null)
        {
            returnWrapper.nack("Bad address: " + bad);
            return;
        }
        EventProducer epi = this.eventProducerForAddRemoveOrChange.lookup(address, returnWrapper);
        if (null == epi)
        {
            returnWrapper.nack("Cound not find the event producer of the subscription; has it dissapeared?");
            return;
        }
        Subscription subscription = this.subscriptions.get(returnWrapper);
        if (null == subscription)
        {
            returnWrapper.nack("Cound not find a subscription to cancel");
        }
        else if (!epi.removeListener(subscription, eventType))
        {
            returnWrapper.nack("Subscription was not found");
        }
        else
        {
            this.subscriptions.remove(returnWrapper);
            returnWrapper.ack("Subscription removed");
        }
    }

    /**
     * Retrieve the id of this SubscriptionHandler.
     * @return the id of this SubscriptionHandler
     */
    public final String getId()
    {
        return this.id;
    }

    /**
     * The commands that a SubscriptionHandler understands.
     */
    public enum Command
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
        GET_RESULT_META_DATA,
        /** Get the output of the IdSource. */
        GET_LIST,
        /** Get the set of implemented commands (must - itself - always be implemented). */
        GET_COMMANDS;
    }

    /**
     * Convert a String representing a Command into that Command.
     * @param commandString the string
     * @return the corresponding Command, or null if the <code>commandString</code> is not a valid Command
     */
    public static Command lookupCommand(final String commandString)
    {
        if ("GET_ADDRESS_META_DATA".equals(commandString))
        {
            return Command.GET_ADDRESS_META_DATA;
        }
        else if ("GET_CURRENT".equals(commandString))
        {
            return Command.GET_CURRENT;
        }
        else if ("GET_RESULT_META_DATA".equals(commandString))
        {
            return Command.GET_RESULT_META_DATA;
        }
        else if ("GET_RESULT_META_DATA".equals(commandString))
        {
            return Command.GET_RESULT_META_DATA;
        }
        else if ("SUBSCRIBE_TO_ADD".equals(commandString))
        {
            return Command.SUBSCRIBE_TO_ADD;
        }
        else if ("SUBSCRIBE_TO_CHANGE".equals(commandString))
        {
            return Command.SUBSCRIBE_TO_CHANGE;
        }
        else if ("SUBSCRIBE_TO_REMOVE".equals(commandString))
        {
            return Command.SUBSCRIBE_TO_REMOVE;
        }
        else if ("UNSUBSCRIBE_FROM_ADD".equals(commandString))
        {
            return Command.UNSUBSCRIBE_FROM_ADD;
        }
        else if ("UNSUBSCRIBE_FROM_REMOVE".equals(commandString))
        {
            return Command.UNSUBSCRIBE_FROM_REMOVE;
        }
        else if ("UNSUBSCRIBE_FROM_CHANGE".equals(commandString))
        {
            return Command.UNSUBSCRIBE_FROM_CHANGE;
        }
        else if ("GET_LIST".contentEquals(commandString))
        {
            return Command.GET_LIST;
        }
        else if ("GET_COMMANDS".contentEquals(commandString))
        {
            return Command.GET_COMMANDS;
        }
        System.err.println("Could not find command with name \"" + commandString + "\"");
        return null;
    }

    /**
     * Execute one command.
     * @param command the command
     * @param address Object[] the address of the object on which the command must be applied
     * @param returnWrapper envelope generator for replies
     * @throws RemoteException on communication failure
     * @throws SerializationException on illegal type in serialization
     * @throws Sim0MQException on communication error
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
            {
                Object[] result = this.listTransceiver.get(address, returnWrapper);
                if (null != result)
                {
                    sendResult(result, returnWrapper);
                }
                // TODO else?
                break;
            }

            case GET_ADDRESS_META_DATA:
                if (null == this.listTransceiver)
                {
                    returnWrapper.nack("The " + this.id + " SubscriptionHandler does not support immediate replies");
                }
                sendResult(extractObjectDescriptorClassNames(this.listTransceiver.getAddressFields().getObjectDescriptors()),
                        returnWrapper);
                break;

            case GET_RESULT_META_DATA:
                if (null == this.listTransceiver)
                {
                    returnWrapper.nack("The " + this.id + " SubscriptionHandler does not support immediate replies");
                }
                sendResult(extractObjectDescriptorClassNames(this.listTransceiver.getResultFields().getObjectDescriptors()),
                        returnWrapper);
                break;

            case GET_LIST:
            {
                if (this.listTransceiver.hasIdSource())
                {
                    sendResult(this.listTransceiver.getIdSource(address.length, returnWrapper).get(null, returnWrapper),
                            returnWrapper);
                }
                else
                {
                    sendResult(new Object[] {"No list transceiver exists in " + getId()}, returnWrapper);
                }
                break;
            }

            case GET_COMMANDS:
                List<String> resultList = new ArrayList<>();
                if (null != this.addedEventType)
                {
                    resultList.add(Command.SUBSCRIBE_TO_ADD.toString());
                    resultList.add(Command.UNSUBSCRIBE_FROM_ADD.toString());
                }
                if (null != this.removedEventType)
                {
                    resultList.add(Command.SUBSCRIBE_TO_REMOVE.toString());
                    resultList.add(Command.UNSUBSCRIBE_FROM_REMOVE.toString());

                }
                if (null != this.changeEventType)
                {
                    resultList.add(Command.SUBSCRIBE_TO_CHANGE.toString());
                    resultList.add(Command.UNSUBSCRIBE_FROM_CHANGE.toString());
                }
                if (this.listTransceiver.getAddressFields() != null)
                {
                    resultList.add(Command.GET_ADDRESS_META_DATA.toString());
                }
                if (this.listTransceiver.getResultFields() != null)
                {
                    resultList.add(Command.GET_RESULT_META_DATA.toString());
                }
                if (null != this.listTransceiver)
                {
                    resultList.add(Command.GET_LIST.toString());
                }
                resultList.add(Command.GET_COMMANDS.toString());
                Object[] result = new Object[resultList.size()];
                for (int index = 0; index < result.length; index++)
                {
                    result[index] = resultList.get(index);
                }
                returnWrapper.encodeReplyAndTransmit(result);
                break;

            default:
                // Cannot happen
                break;
        }
    }

    /**
     * Extract the class names from an array of ObjectDescriptor.
     * @param objectDescriptors the array of ObjectDescriptor
     * @return the class names
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
     * Send data via Sim0MQ to master if (and only if) it is non-null.
     * @param data the data to transmit
     * @param returnWrapper envelope constructor for returned results
     * @throws SerializationException on illegal type in serialization
     * @throws Sim0MQException on communication error
     */
    private void sendResult(final Object[] data, final ReturnWrapper returnWrapper)
            throws Sim0MQException, SerializationException
    {
        if (data != null)
        {
            returnWrapper.encodeReplyAndTransmit(data);
        }
    }

    @Override
    public String toString()
    {
        return "SubscriptionHandler [id=" + this.id + ", listTransceiver=" + this.listTransceiver
                + ", eventProducerForAddRemoveOrChange=" + this.eventProducerForAddRemoveOrChange + ", addedEventType="
                + this.addedEventType + ", removedEventType=" + this.removedEventType + ", changeEventType="
                + this.changeEventType + ", elementSubscriptionHandler=" + this.elementSubscriptionHandler + "]";
    }

}

/**
 * Handles one subscription.
 */
class Subscription implements EventListener
{
    /** ... */
    private static final long serialVersionUID = 20200428L;

    /** Generates envelopes for the messages sent over Sim0MQ. */
    private final ReturnWrapper returnWrapper;

    /**
     * Construct a new Subscription.
     * @param returnWrapper envelope generator for the messages
     */
    Subscription(final ReturnWrapper returnWrapper)
    {
        this.returnWrapper = returnWrapper;
    }

    @Override
    public void notify(final Event event)
    {
        MetaData metaData = event.getType().getMetaData();
        int additionalFields = event.getType() instanceof EventType ? 1 : 0;
        Object[] result = new Object[additionalFields + metaData.size()];
        // result[0] = event.getType().getName();
        if (additionalFields > 0)
        {
            result[0] = ((TimedEvent<?>) event).getTimeStamp();
        }
        Object payload = event.getContent();
        if (payload instanceof Object[])
        {
            for (int index = 0; index < event.getType().getMetaData().size(); index++)
            {
                result[additionalFields + index] = ((Object[]) payload)[index];
            }
        }
        else
        {
            result[additionalFields] = payload;
        }
        // TODO verify the composition of the result. Problem: no access to the metadata here
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
