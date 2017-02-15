package org.sim0mq.message;

import org.sim0mq.ZeroMQException;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Dec 15, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public final class EndianUtil
{
    /**
     * Utility class - do not instantiate.
     */
    private EndianUtil()
    {
        //
    }

    /**
     * Decode an int.
     * @param message the ZeroMQ byte array to decode
     * @param pointer the first byte to consider
     * @return the integer value
     * @throws ZeroMQException when data type is not integer
     */
    public static int decodeInt(final byte[] message, final int pointer) throws ZeroMQException
    {
        if (message[pointer] == Message.INT_32)
        {
            return decodeIntBigEndian(message, pointer + 1);
        }
        throw new ZeroMQException("decodeInt: expected int, but got data type " + message[pointer]);
    }

    /**
     * Decode a Big Endian int.
     * @param message the ZeroMQ byte array to decode
     * @param pointer the first byte to consider
     * @return the integer value
     */
    public static int decodeIntBigEndian(final byte[] message, final int pointer)
    {
        return (((message[pointer] & 0xff) << 24) | ((message[pointer + 1] & 0xff) << 16) | ((message[pointer + 2] & 0xff) << 8)
                | ((message[pointer + 3] & 0xff)));
    }

    /**
     * Decode a Little Endian int.
     * @param message the ZeroMQ byte array to decode
     * @param pointer the first byte to consider
     * @return the integer value
     */
    public static int decodeIntLittleEndian(final byte[] message, final int pointer)
    {
        return (((message[pointer + 3] & 0xff) << 24) | ((message[pointer + 2] & 0xff) << 16)
                | ((message[pointer + 1] & 0xff) << 8) | ((message[pointer] & 0xff)));
    }

    /**
     * Decode a Big Endian long.
     * @param message the ZeroMQ byte array to decode
     * @param pointer the first byte to consider
     * @return the long value
     */
    public static long decodeLongBigEndian(final byte[] message, final int pointer)
    {
        return ((((long) message[pointer]) << 56) | (((long) message[pointer + 1] & 0xff) << 48)
                | (((long) message[pointer + 2] & 0xff) << 40) | (((long) message[pointer + 3] & 0xff) << 32)
                | (((long) message[pointer + 4] & 0xff) << 24) | (((long) message[pointer + 5] & 0xff) << 16)
                | (((long) message[pointer + 6] & 0xff) << 8) | (((long) message[pointer + 7] & 0xff)));
    }

    /**
     * Decode a Little Endian long.
     * @param message the ZeroMQ byte array to decode
     * @param pointer the first byte to consider
     * @return the long value
     */
    public static long decodeLongLittleEndian(final byte[] message, final int pointer)
    {
        return ((((long) message[pointer + 7]) << 56) | (((long) message[pointer + 6] & 0xff) << 48)
                | (((long) message[pointer + 5] & 0xff) << 40) | (((long) message[pointer + 4] & 0xff) << 32)
                | (((long) message[pointer + 3] & 0xff) << 24) | (((long) message[pointer + 2] & 0xff) << 16)
                | (((long) message[pointer + 1] & 0xff) << 8) | (((long) message[pointer] & 0xff)));
    }

}
