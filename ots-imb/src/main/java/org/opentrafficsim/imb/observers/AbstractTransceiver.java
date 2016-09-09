package org.opentrafficsim.imb.observers;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import nl.tudelft.simulation.event.EventInterface;
import nl.tudelft.simulation.event.EventListenerInterface;
import nl.tudelft.simulation.event.EventProducer;
import nl.tudelft.simulation.event.EventProducerInterface;
import nl.tudelft.simulation.event.EventType;

import org.opentrafficsim.core.Throw;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Sep 9, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public abstract class AbstractTransceiver extends EventProducer implements Transceiver
{
    /** */
    private static final long serialVersionUID = 20160909L;

    /** The IMB connector through which this transceiver communicates. */
    private final Connector connector;

    /** ... TODO */
    private Map<String, EventType> imbToOTSMap = new HashMap<>();

    /** ... TODO */
    private Map<EventType, String> otsToIMBMap = new HashMap<>();

    /**
     * Construct a new AbstractTranceiver.
     * @param connector Connector; the IMB connector through which this transceiver communicates
     */
    public AbstractTransceiver(final Connector connector)
    {
        Throw.when(null == connector, NullPointerException.class, "Connector can not be null");
        this.connector = connector;
    }

    /**
     * @param producer
     * @param eventType
     * @param imbEventName
     * @param creationPayload
     * @throws RemoteException
     */
    public final void addOTSToIMBChannel(final EventProducerInterface producer, final EventType eventType,
            final String imbEventName, Object[] creationPayload) throws RemoteException
    {
        try
        {
            this.connector.postMessage(imbEventName, Connector.IMBEventType.NEW, creationPayload);
            this.otsToIMBMap.put(eventType, imbEventName);
            producer.addListener(this, eventType);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * @param producer
     * @param eventType
     * @param imbEventName
     * @param removePayload
     * @throws RemoteException
     */
    public final void removeOTSToIMBChannel(final EventProducerInterface producer, final EventType eventType,
            final String imbEventName, Object[] removePayload) throws RemoteException
    {
        try
        {
            producer.removeListener(this, eventType);
            this.otsToIMBMap.remove(eventType);
            this.connector.postMessage(imbEventName, Connector.IMBEventType.DELETE, removePayload);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void notify(final EventInterface event) throws RemoteException
    {
        String imbEventName = this.otsToIMBMap.get(event.getType());
        if (null != imbEventName)
        {
            Object[] payload;
            if (event.getContent() instanceof Object[])
            {
                payload = (Object[]) event.getContent();
            }
            else
            {
                payload = new Object[] { event.getContent() };
            }
            try
            {
                this.connector.postMessage(imbEventName, Connector.IMBEventType.CHANGE, payload);
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
            }
        }
    }

    public void addIMBtoOTSChannel(final EventListenerInterface otsListener, final EventType eventType,
            final String imbEventName, final IMBToOTSTransformer imbToOTSTransformer)
    {
        this.imbToOTSMap.put(imbEventName, eventType);
        
    }

    /** {@inheritDoc} */
    @Override
    public final Connector getConnector()
    {
        return this.connector;
    }

}
