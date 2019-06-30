package org.opentrafficsim.imb.connector;

import java.util.LinkedHashMap;
import java.util.Map;

import org.djutils.exceptions.Throw;
import org.opentrafficsim.imb.IMBException;
import org.opentrafficsim.imb.transceiver.Transceiver;

import nl.tno.imb.TByteBuffer;
import nl.tno.imb.TConnection;
import nl.tno.imb.TEventEntry;

/**
 * Make a connection to the IMB bus, allow messages to be posted, and register callbacks from IMB to OTS.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Aug 19, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class IMBConnector implements Connector
{
    /** Communication link to the observers. */
    private final TConnection connection;

    /** Registration of callback ids from IMB to OTS. */
    protected Map<String, Transceiver> imbTransceiverMap = new LinkedHashMap<>();

    /**
     * Construct a new connection for sending events to IMB
     * @param host String; name of the IMB hub
     * @param port int; port number of the IMB hub
     * @param modelName String; local model name
     * @param modelId int; model id
     * @param federation String; federation on the IMB hub
     * @throws IMBException when a connection to the IMB hub could not be established
     */
    public IMBConnector(final String host, final int port, final String modelName, final int modelId, final String federation)
            throws IMBException
    {
        Throw.whenNull(host, "host cannot be null");
        Throw.whenNull(modelName, "modelName cannot be null");
        Throw.whenNull(modelId, "modelId cannot be null");
        Throw.whenNull(federation, "federation cannot be null");
        Throw.when(port <= 0 || port > 65535, IMBException.class, "port should be beween 1 and 65535");

        this.connection = new TConnection(host, port, modelName, modelId, federation);
        Throw.when(!this.connection.isConnected(), IMBException.class, "No connection to IMB hub on " + host + ":" + port);
    }

    /**
     * Construct an IMBConnector that re-uses an existing TConnection.
     * @param connection TConnection; the existing TConnection
     * @throws IMBException when the connection is not connected to an IMB hub
     */
    public IMBConnector(final TConnection connection) throws IMBException
    {
        Throw.whenNull(connection, "conneciton cannot be null");

        this.connection = connection;
        Throw.when(!this.connection.isConnected(), IMBException.class, "No connection to IMB hub");
    }

    /** {@inheritDoc} */
    @Override
    public void register(final String imbEventName, final Transceiver transceiver) throws IMBException
    {
        Throw.whenNull(imbEventName, "imbEventName cannot be null");
        Throw.whenNull(transceiver, "transceiver cannot be null");

        // Already registered?
        if (this.imbTransceiverMap.containsKey(imbEventName))
        {
            Throw.when(!this.imbTransceiverMap.get(imbEventName).equals(transceiver), IMBException.class,
                    "Cannot switch transceiver for imbEventName " + imbEventName);
            return;
        }
        // we receive messages including the federation name
        this.imbTransceiverMap.put(imbEventName, transceiver);

        // Link to the listening thread for incoming IMB messages.
        TEventEntry messageEvent = this.connection.subscribe(imbEventName);
        messageEvent.onNormalEvent = new TEventEntry.TOnNormalEvent()
        {
            @Override
            public void dispatch(TEventEntry aEvent, TByteBuffer aPayload)
            {
                String shortIMBEventName = aEvent.getEventName().substring(aEvent.getEventName().indexOf('.') + 1);
                if (!IMBConnector.this.imbTransceiverMap.containsKey(shortIMBEventName))
                {
                    // TODO error handling
                    System.err.println("Could not find imbEventName " + shortIMBEventName + " in imbTransceiverMap");
                }
                try
                {
                    // TODO synchronized?
                    IMBConnector.this.imbTransceiverMap.get(shortIMBEventName).handleMessageFromIMB(shortIMBEventName,
                            aPayload);
                }
                catch (IMBException exception)
                {
                    // TODO error handling
                    exception.printStackTrace();
                }
            }
        };

    }

    /** {@inheritDoc} */
    @Override
    public final boolean postIMBMessage(final String eventName, final IMBEventType imbEventType, final Object[] args)
            throws IMBException
    {
        TByteBuffer payload = new TByteBuffer();
        payload.writeStart(0);
        payload.write(imbEventType.getEventEntry());
        for (Object o : args)
        {
            Class<?> objectClass = o.getClass();
            if (objectClass.equals(String.class))
                payload.write((String) o);
            else if (objectClass.equals(Double.class))
                payload.write(((Double) o).doubleValue());
            else if (objectClass.equals(Float.class))
                payload.write(((Float) o).floatValue());
            else if (objectClass.equals(Integer.class))
                payload.write(((Integer) o).intValue());
            else if (objectClass.equals(Long.class))
                payload.write(((Long) o).longValue());
            else if (objectClass.equals(Byte.class))
                payload.write(((Byte) o).byteValue());
            else if (objectClass.equals(Character.class))
                payload.write(((Character) o).charValue());
            else if (objectClass.equals(Boolean.class))
                payload.write(((Boolean) o).booleanValue());
            else if (objectClass.equals(byte[].class))
                payload.write(new TByteBuffer((byte[]) o));
            else
                throw new IMBException("cannot pack object of type " + objectClass.toString() + " in payload");
        }
        int result = this.connection.signalEvent(eventName, TEventEntry.EK_NORMAL_EVENT, payload);
        if (result < 0)
        {
            throw new IMBException("IMB error in signalEvent code " + result);
        }
        return result >= 0;
    }

    /** {@inheritDoc} */
    @Override
    public final String getHost()
    {
        return this.connection.getRemoteHost();
    }

    /** {@inheritDoc} */
    @Override
    public final int getPort()
    {
        return this.connection.getRemotePort();
    }

    /** {@inheritDoc} */
    @Override
    public final String getModelName()
    {
        return this.connection.getOwnerName();
    }

    /** {@inheritDoc} */
    @Override
    public final int getModelId()
    {
        return this.connection.getOwnerID();
    }

    /** {@inheritDoc} */
    @Override
    public final String getFederation()
    {
        return this.connection.getFederation();
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "IMBConnector [connection=" + this.connection + "]";
    }

}
