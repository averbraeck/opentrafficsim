package org.sim0mq.publisher;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.djunits.Throw;
import org.djutils.event.EventInterface;
import org.djutils.event.EventListenerInterface;
import org.djutils.event.EventProducerInterface;
import org.djutils.event.TimedEvent;
import org.djutils.event.TimedEventType;
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

    /** TimedEventType to subscribe to in order to receive creation of added object events; e.g. NETWORK.GTU_ADD_EVENT. */
    private final TimedEventType addedEventType;

    /** TimedEventType to subscribe to in order to receive removed object events; e.g. NETWORK.GTU_REMOVE_EVENT. */
    private final TimedEventType removedEventType;

    /** TimedEventType to subscript to in order to receive change of the collection, or object events. */
    private final TimedEventType changeEventType;

    /** SubscriptionHandler that handles subscriptions to individual objects; e.g. GTU.MOVE_EVENT. */
    private final SubscriptionHandler elementSubscriptionHandler;

    /** The currently active subscriptions. */
    private final Map<ReturnWrapperImpl, Subscription> subscriptions = new LinkedHashMap<>();

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
            final LookupEventProducerInterface eventProducerForAddRemoveOrChange, final TimedEventType addedEventType,
            final TimedEventType removedEventType, final TimedEventType changeEventType,
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
     * @throws SerializationException on context error
     * @throws Sim0MQException on DSOL error
     */
    public void get(final Object[] address, final ReturnWrapperImpl returnWrapper)
            throws RemoteException, Sim0MQException, SerializationException
    {
        sendResult(this.listTransceiver.get(address, returnWrapper), returnWrapper);
    }

    /**
     * Retrieve the list transceiver (only for testing).
     * @return TransceiverInterface; the list transceiver
     */
    public TransceiverInterface getListTransceiver()
    {
        return this.listTransceiver;
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
     * Create a new subscription to ADD, REMOVE, or CHANGE events.
     * @param address Object[]; the data that is required to find the correct EventProducer
     * @param eventType TimedEventType; one of the event types that the addressed EventProducer can fire
     * @param returnWrapper ReturnWrapper; generates envelopes for the returned events
     * @return String; one liner report of the result;
     * @throws RemoteException when communication fails
     * @throws SerializationException should never happen
     * @throws Sim0MQException should never happen
     */
    private String subscribeTo(final Object[] address, final TimedEventType eventType, final ReturnWrapperImpl returnWrapper)
            throws RemoteException, Sim0MQException, SerializationException
    {
        if (null == eventType)
        {
            return "Does not support subscribe to";
        }
        String bad = AbstractTransceiver.verifyMetaData(this.eventProducerForAddRemoveOrChange.getAddressMetaData(), address);
        if (bad != null)
        {
            return "Bad address: " + bad;
        }
        EventProducerInterface epi = this.eventProducerForAddRemoveOrChange.lookup(address, returnWrapper);
        if (null != epi)
        {
            Subscription subscription = this.subscriptions.get(returnWrapper);
            if (null == subscription)
            {
                subscription = new Subscription(returnWrapper);
            }
            if (!epi.addListener(subscription, eventType))
            {
                // There was already a subscription
                return "There is already such a subscription active";
            }
            this.subscriptions.put(returnWrapper, subscription);
            return "OK; subscription created";
        }
        // Not necessarily bad; some EventProducers (e.g. GTUs) may disappear at any time
        return "Could not find event producer; has it disappeared?";
    }

    /**
     * Cancel a subscription to ADD, REMOVE, or CHANGE events.
     * @param address Object[]; the data that is required to find the correct EventProducer
     * @param eventType TimedEventType; one of the event types that the addressed EventProducer can fire
     * @param returnWrapper ReturnWrapper; the ReturnWapper that sent the results until now
     * @return String; one liner report of the result
     * @throws RemoteException when communication fails
     * @throws SerializationException should never happen
     * @throws Sim0MQException should never happen
     */
    private String unsubscribeFrom(final Object[] address, final TimedEventType eventType, final ReturnWrapperImpl returnWrapper)
            throws RemoteException, Sim0MQException, SerializationException
    {
        if (null == eventType)
        {
            return "Does not support unsubscribe from";
        }
        String bad = AbstractTransceiver.verifyMetaData(this.eventProducerForAddRemoveOrChange.getAddressMetaData(), address);
        if (bad != null)
        {
            return "Bad address: " + bad;
        }
        EventProducerInterface epi = this.eventProducerForAddRemoveOrChange.lookup(address, returnWrapper);
        if (null != epi)
        {
            Subscription subscription = this.subscriptions.get(returnWrapper);
            if (null == subscription)
            {
                // System.err.println("Could not find subscription for " + returnWrapper);
                // System.err.println("Existing subscriptions:");
                // for (ReturnWrapper rw : this.subscriptions.keySet())
                // {
                // System.err.println("\t" + rw);
                // }
                return "Cound not find a subscription to cancel";
            }
            if (!epi.removeListener(subscription, eventType))
            {
                returnWrapper.encodeReplyAndTransmit("Subscription was not found");
            }
            this.subscriptions.remove(returnWrapper);
            return "OK; subscription removed";
        }
        return "Cound not find the event producer of the subscription; has it dissapeared?";
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
        GET_RESULT_META_DATA,
        /** Get the output of the IdSource. */
        GET_LIST,
        /** Get the set of implemented commands (must - itself - always be implemented). */
        GET_COMMANDS;
    }

    /**
     * Convert a String representing a Command into that Command.
     * @param commandString String; the string
     * @return Command; the corresponding Command, or null if the <code>commandString</code> is not a valid Command
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
        return null;
    }

    /**
     * Execute one command.
     * @param command Command; the command
     * @param address Object[] the address of the object on which the command must be applied
     * @param returnWrapper ReturnWrapper; envelope generator for replies
     * @throws RemoteException on communication failure
     * @throws SerializationException on illegal type in serialization
     * @throws Sim0MQException on communication error
     */
    public void executeCommand(final Command command, final Object[] address, final ReturnWrapperImpl returnWrapper)
            throws RemoteException, Sim0MQException, SerializationException
    {
        Throw.whenNull(command, "Command may not be null");
        Throw.whenNull(returnWrapper, "ReturnWrapper may not be null");
        switch (command)
        {
            case SUBSCRIBE_TO_ADD:
                sendResult(new Object[] { subscribeTo(address, this.addedEventType, returnWrapper) }, returnWrapper);
                break;

            case SUBSCRIBE_TO_CHANGE:
                sendResult(new Object[] { subscribeTo(address, this.changeEventType, returnWrapper) }, returnWrapper);
                break;

            case SUBSCRIBE_TO_REMOVE:
                sendResult(new Object[] { subscribeTo(address, this.removedEventType, returnWrapper) }, returnWrapper);
                break;

            case UNSUBSCRIBE_FROM_ADD:
                sendResult(new Object[] { unsubscribeFrom(address, this.addedEventType, returnWrapper) }, returnWrapper);
                break;

            case UNSUBSCRIBE_FROM_CHANGE:
                sendResult(new Object[] { unsubscribeFrom(address, this.changeEventType, returnWrapper) }, returnWrapper);
                break;

            case UNSUBSCRIBE_FROM_REMOVE:
                sendResult(new Object[] { unsubscribeFrom(address, this.removedEventType, returnWrapper) }, returnWrapper);
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
                sendResult(extractObjectDescriptorClassNames(this.listTransceiver.getAddressFields().getObjectDescriptors()),
                        returnWrapper);
                break;

            case GET_RESULT_META_DATA:
                sendResult(extractObjectDescriptorClassNames(this.listTransceiver.getResultFields().getObjectDescriptors()),
                        returnWrapper);
                break;
                
            case GET_LIST:
            {
                TransceiverInterface transceiver = this.listTransceiver.getIdSource(0, returnWrapper);
                if (null == transceiver)
                {
                    sendResult(new Object[] { "No list transceiver" }, returnWrapper);
                }
                sendResult(transceiver.get(address, returnWrapper), returnWrapper);
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
     * Send data via Sim0MQ to master if (and only if) it is non-null.
     * @param data Object[]; the data to transmit
     * @param returnWrapper ReturnWrapper; envelope constructor for returned results
     * @throws SerializationException on illegal type in serialization
     * @throws Sim0MQException on communication error
     */
    private void sendResult(final Object[] data, final ReturnWrapperImpl returnWrapper)
            throws Sim0MQException, SerializationException
    {
        if (data != null)
        {
            returnWrapper.encodeReplyAndTransmit(data);
        }
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
     * @param returnWrapper ReturnWrapper; to be used to send back complaints about bad addresses, etc.
     * @return EventProducerInterface; can be null in case the address is (no longer) valid
     * @throws SerializationException when an error occurs while serializing an error response
     * @throws Sim0MQException when an error occurs while serializing an error response
     */
    EventProducerInterface lookup(Object[] address, ReturnWrapperImpl returnWrapper) throws Sim0MQException, SerializationException;

    /**
     * Return a MetaData object that can be used to verify the correctness of an address for the <code>lookup</code> method.
     * @return MetaData; to be used to verify the correctness of an address for the <code>lookup</code> method
     */
    MetaData getAddressMetaData();

}

/**
 * Handles one subscription.
 */
class Subscription implements EventListenerInterface
{
    /** ... */
    private static final long serialVersionUID = 20200428L;

    /** Generates envelopes for the messages sent over Sim0MQ. */
    private final ReturnWrapperImpl returnWrapper;

    /**
     * Construct a new Subscription.
     * @param returnWrapper ReturnWrapper; envelope generator for the messages
     */
    Subscription(final ReturnWrapperImpl returnWrapper)
    {
        this.returnWrapper = returnWrapper;
    }

    /** {@inheritDoc} */
    @Override
    public void notify(final EventInterface event) throws RemoteException
    {
        MetaData metaData = event.getType().getMetaData();
        int additionalFields = event.getType() instanceof TimedEventType ? 1 : 0;
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
