package org.opentrafficsim.core.observers;

import nl.tno.imb.TByteBuffer;
import nl.tno.imb.TConnection;
import nl.tno.imb.TEventEntry;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Aug 19, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class IMBObserver implements Observer
{
    /** Communication link to the observers. */
    private final TConnection connection;

    /**
     * Construct a new connection for sending events to IMB.
     * @param host String; name of the IMB hub
     * @param port int; port number of the IMB hub
     * @param modelName String; local model name
     * @param modelId int; model id
     * @param federation String; federation on the IMB hub
     * @throws Exception when a connection to the IMB hub could not be established
     */
    public IMBObserver(final String host, final int port, final String modelName, final int modelId, final String federation) throws Exception
    {
        this.connection = new TConnection(host, port, modelName, modelId, federation);
        if (!this.connection.isConnected())
        {
            throw new Exception("No connection to broker");
        }
    }

    /**
     * {@inheritDoc}
     * @throws Exception
     */
    @Override
    public final boolean postMessage(final String eventName, final int eventType, final Object[] args) throws Exception
    {
        TByteBuffer payload = new TByteBuffer();
        payload.writeStart(0);
        payload.write(eventType);
        for (Object o : args)
        {
            String typeName = o.getClass().getName();
            switch (typeName)
            {
                case "java.lang.String":
                    payload.write((String) o);
                    break;

                case "java.lang.Double":
                    payload.write((Double) o);
                    break;

                default:
                    throw new ClassCastException("cannot pack object of type " + typeName + " in payload");
            }
        }
        int result = this.connection.signalEvent(eventName, TEventEntry.EK_NORMAL_EVENT, payload);
        if (result < 0)
        {
            System.err.println("IMB error in signalEvent code " + result);
        }
        return result >= 0;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "IMBObserver [connection=" + this.connection + "]";
    }

}
