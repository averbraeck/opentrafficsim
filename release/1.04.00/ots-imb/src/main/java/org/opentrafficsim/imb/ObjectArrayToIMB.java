package org.opentrafficsim.imb;

import nl.tno.imb.TByteBuffer;

/**
 * Transmit an array of Object to the IMB hub.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Oct 27, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class ObjectArrayToIMB
{

    /**
     * Do not instantiate
     */
    private ObjectArrayToIMB()
    {
        // Cannot be instantiated
    }

    /**
     * Convert an array of Object to an IMB payload.
     * @param data Object[]; the data to transmit
     * @return TByteBuffer; the constructed IMB payload
     * @throws IMBException
     */
    public static TByteBuffer objectArrayToIMBPayload(final Object[] data) throws IMBException
    {
        TByteBuffer payload = new TByteBuffer();
        for (Object o : data)
        {
            switch (o.getClass().getName())
            {
                case "java.lang.String":
                    payload.prepare((String) o);
                    break;

                case "java.lang.Integer":
                    payload.prepare((Integer) o);
                    break;

                case "java.lang.Long":
                    payload.prepare((Long) o);
                    break;

                case "java.lang.Double":
                    payload.prepare((Double) o);
                    break;

                case "java.lang.Float":
                    payload.prepare((Float) o);
                    break;

                case "java.lang.Byte":
                    payload.prepare((Byte) o);
                    break;

                case "java.lang.Character":
                    payload.prepare((Character) o);
                    break;

                case "java.lang.Boolean":
                    payload.prepare((Boolean) o);
                    break;

                default:
                    if (o instanceof SelfWrapper)
                    {
                        ((SelfWrapper) o).prepare(payload);
                    }
                    else
                    {
                        throw new IMBException("don't know how to prepare " + o.getClass().getName());
                    }
                    break;
            }
        }
        payload.prepareApply();
        for (Object o : data)
        {
            switch (o.getClass().getName())
            {
                case "java.lang.String":
                    payload.qWrite((String) o);
                    break;

                case "java.lang.Integer":
                    payload.qWrite((Integer) o);
                    break;

                case "java.lang.Long":
                    payload.qWrite((Long) o);
                    break;

                case "java.lang.Double":
                    payload.qWrite((Double) o);
                    break;

                case "java.lang.Float":
                    payload.qWrite((Float) o);
                    break;

                case "java.lang.Byte":
                    payload.qWrite((Byte) o);
                    break;

                case "java.lang.Character":
                    payload.qWrite((Character) o);
                    break;

                case "java.lang.Boolean":
                    payload.qWrite((Boolean) o);
                    break;

                default:
                    if (o instanceof SelfWrapper)
                    {
                        ((SelfWrapper) o).qWrite(payload);
                    }
                    else
                    {
                        // Cannot happen; would have been caught in the prepare pass
                        throw new IMBException("don't know how to qWrite " + o.getClass().getName());
                    }
                    break;
            }
        }
        return payload;
    }

}
