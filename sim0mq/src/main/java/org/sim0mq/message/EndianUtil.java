package org.sim0mq.message;

import java.nio.ByteOrder;

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
    /** do we want to send the messages in big endian? */
    public static boolean defaultBigEndian = true;
    
    /** is the platform big endian? */
    public static final boolean platformBigEndian = ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN);

    /**
     * Utility class - do not instantiate.
     */
    private EndianUtil()
    {
        //
    }

    /**
     * Decode a short.
     * @param message the ZeroMQ byte array to decode
     * @param pointer the first byte to consider
     * @return the short value
     */
    public static short decodeShort(final byte[] message, final int pointer)
    {
        if (defaultBigEndian)
        {
            return (short) (((message[pointer] & 0xff) << 8) | ((message[pointer + 1] & 0xff)));
        }
        else
        {
            return (short) (((message[pointer + 1] & 0xff) << 8) | ((message[pointer] & 0xff)));
        }
    }

    /**
     * Decode a int.
     * @param message the ZeroMQ byte array to decode
     * @param pointer the first byte to consider
     * @return the integer value
     */
    public static int decodeInt(final byte[] message, final int pointer)
    {
        if (defaultBigEndian)
        {
            return (((message[pointer] & 0xff) << 24) | ((message[pointer + 1] & 0xff) << 16)
                    | ((message[pointer + 2] & 0xff) << 8) | ((message[pointer + 3] & 0xff)));
        }
        else
        {
            return (((message[pointer + 3] & 0xff) << 24) | ((message[pointer + 2] & 0xff) << 16)
                    | ((message[pointer + 1] & 0xff) << 8) | ((message[pointer] & 0xff)));
        }
    }

    /**
     * Decode a long.
     * @param message the ZeroMQ byte array to decode
     * @param pointer the first byte to consider
     * @return the long value
     */
    public static long decodeLong(final byte[] message, final int pointer)
    {
        if (defaultBigEndian)
        {
            return ((((long) message[pointer]) << 56) | (((long) message[pointer + 1] & 0xff) << 48)
                    | (((long) message[pointer + 2] & 0xff) << 40) | (((long) message[pointer + 3] & 0xff) << 32)
                    | (((long) message[pointer + 4] & 0xff) << 24) | (((long) message[pointer + 5] & 0xff) << 16)
                    | (((long) message[pointer + 6] & 0xff) << 8) | (((long) message[pointer + 7] & 0xff)));
        }
        else
        {
            return ((((long) message[pointer + 7]) << 56) | (((long) message[pointer + 6] & 0xff) << 48)
                    | (((long) message[pointer + 5] & 0xff) << 40) | (((long) message[pointer + 4] & 0xff) << 32)
                    | (((long) message[pointer + 3] & 0xff) << 24) | (((long) message[pointer + 2] & 0xff) << 16)
                    | (((long) message[pointer + 1] & 0xff) << 8) | (((long) message[pointer] & 0xff)));
        }
    }

    /**
     * Decode a float.
     * @param message the ZeroMQ byte array to decode
     * @param pointer the first byte to consider
     * @return the float value
     */
    public static float decodeFloat(final byte[] message, final int pointer)
    {
        int bits = decodeInt(message, pointer);
        return Float.intBitsToFloat(bits);
    }

    /**
     * Decode a double.
     * @param message the ZeroMQ byte array to decode
     * @param pointer the first byte to consider
     * @return the double value
     */
    public static double decodeDouble(final byte[] message, final int pointer)
    {
        long bits = decodeLong(message, pointer);
        return Double.longBitsToDouble(bits);
    }

    /**
     * Decode a char (16 bits).
     * @param message the ZeroMQ byte array to decode
     * @param pointer the first byte to consider
     * @return the short value
     */
    public static char decodeChar(final byte[] message, final int pointer)
    {
        return (char) decodeShort(message, pointer);
    }

    /**
     * Decode a String including the length int from the message byte array
     * @param message the message byte array
     * @param pointer the start position in the array
     * @return the Java String at position pointer
     */
    public static String decodeUTF8String(final byte[] message, final int pointer)
    {
        int len = decodeInt(message, pointer);
        char[] c = new char[len];
        for (int i = 0; i < len; i++)
        {
            c[i] = (char) message[pointer + i + 4];
        }
        return String.copyValueOf(c);
    }

    /**
     * Decode a String including the length int from the message byte array
     * @param message the message byte array
     * @param pointer the start position in the array
     * @return the Java String at position pointer
     */
    public static String decodeUTF16String(final byte[] message, final int pointer)
    {
        int len = decodeInt(message, pointer);
        char[] c = new char[len];
        for (int i = 0; i < len; i++)
        {
            c[i] = decodeChar(message, 2 * i + 4);
        }
        return String.copyValueOf(c);
    }

    /**
     * Encode a short into a message buffer.
     * @param v the variable to encode
     * @param message the message buffer to encode the variable into
     * @param pointer the pointer to start writing
     * @return the new pointer after writing
     */
    public static int encodeShort(final short v, final byte[] message, final int pointer)
    {
        int p = pointer;
        if (defaultBigEndian)
        {
            message[p++] = (byte) (v >> 8);
            message[p++] = (byte) (v);
        }
        else
        {
            message[p++] = (byte) (v);
            message[p++] = (byte) (v >> 8);
        }
        return p;
    }

    /**
     * Encode a char (16 bits) into a message buffer.
     * @param v the variable to encode
     * @param message the message buffer to encode the variable into
     * @param pointer the pointer to start writing
     * @return the new pointer after writing
     */
    public static int encodeChar(final char v, final byte[] message, final int pointer)
    {
        return encodeShort((short) v, message, pointer);
    }

    /**
     * Encode a int into a message buffer.
     * @param v the variable to encode
     * @param message the message buffer to encode the variable into
     * @param pointer the pointer to start writing
     * @return the new pointer after writing
     */
    public static int encodeInt(final int v, final byte[] message, final int pointer)
    {
        int p = pointer;
        if (defaultBigEndian)
        {
            message[p++] = (byte) ((v >> 24) & 0xFF);
            message[p++] = (byte) ((v >> 16) & 0xFF);
            message[p++] = (byte) ((v >> 8) & 0xFF);
            message[p++] = (byte) (v & 0xFF);
        }
        else
        {
            message[p++] = (byte) (v & 0xFF);
            message[p++] = (byte) ((v >> 8) & 0xFF);
            message[p++] = (byte) ((v >> 16) & 0xFF);
            message[p++] = (byte) ((v >> 24) & 0xFF);
        }
        return p;
    }

    /**
     * Encode a long into a message buffer.
     * @param v the variable to encode
     * @param message the message buffer to encode the variable into
     * @param pointer the pointer to start writing
     * @return the new pointer after writing
     */
    public static int encodeLong(final long v, final byte[] message, final int pointer)
    {
        int p = pointer;
        if (defaultBigEndian)
        {
            message[p++] = (byte) ((v >> 56) & 0xFF);
            message[p++] = (byte) ((v >> 48) & 0xFF);
            message[p++] = (byte) ((v >> 40) & 0xFF);
            message[p++] = (byte) ((v >> 32) & 0xFF);
            message[p++] = (byte) ((v >> 24) & 0xFF);
            message[p++] = (byte) ((v >> 16) & 0xFF);
            message[p++] = (byte) ((v >> 8) & 0xFF);
            message[p++] = (byte) (v & 0xFF);
        }
        else
        {
            message[p++] = (byte) (v & 0xFF);
            message[p++] = (byte) ((v >> 8) & 0xFF);
            message[p++] = (byte) ((v >> 16) & 0xFF);
            message[p++] = (byte) ((v >> 24) & 0xFF);
            message[p++] = (byte) ((v >> 32) & 0xFF);
            message[p++] = (byte) ((v >> 40) & 0xFF);
            message[p++] = (byte) ((v >> 48) & 0xFF);
            message[p++] = (byte) ((v >> 56) & 0xFF);
        }
        return p;
    }

    /**
     * Encode a float into a message buffer.
     * @param v the variable to encode
     * @param message the message buffer to encode the variable into
     * @param pointer the pointer to start writing
     * @return the new pointer after writing
     */
    public static int encodeFloat(final float v, final byte[] message, final int pointer)
    {
        int vint = Float.floatToIntBits(v);
        return encodeInt(vint, message, pointer);
    }

    /**
     * Encode a double into a message buffer.
     * @param v the variable to encode
     * @param message the message buffer to encode the variable into
     * @param pointer the pointer to start writing
     * @return the new pointer after writing
     */
    public static int encodeDouble(final double v, final byte[] message, final int pointer)
    {
        long vlong = Double.doubleToLongBits(v);
        return encodeLong(vlong, message, pointer);
    }

    /**
     * Return a long encoded as a byte array.
     * @param v the long variable to encode
     * @return the byte array.
     */
    public static byte[] longToByteArray(final long v)
    {
        byte[] message = new byte[8];
        int pointer = 0;
        encodeLong(v, message, pointer);
        return message;
    }
    
    /**
     * Return an int encoded as a byte array.
     * @param v the int variable to encode
     * @return the byte array.
     */
    public static byte[] intToByteArray(final int v)
    {
        byte[] message = new byte[4];
        int pointer = 0;
        encodeInt(v, message, pointer);
        return message;
    }
    
    /**
     * Return a double encoded as a byte array.
     * @param v the double variable to encode
     * @return the byte array.
     */
    public static byte[] doubleToByteArray(final double v)
    {
        byte[] message = new byte[8];
        int pointer = 0;
        encodeDouble(v, message, pointer);
        return message;
    }
    
    /**
     * Return a float encoded as a byte array.
     * @param v the float variable to encode
     * @return the byte array.
     */
    public static byte[] floatToByteArray(final float v)
    {
        byte[] message = new byte[4];
        int pointer = 0;
        encodeFloat(v, message, pointer);
        return message;
    }
    
    /**
     * @return defaultBigEndian
     */
    public static final boolean isDefaultBigEndian()
    {
        return defaultBigEndian;
    }

    /**
     * @param defaultBigEndian set defaultBigEndian
     */
    public static final void setDefaultBigEndian(final boolean defaultBigEndian)
    {
        EndianUtil.defaultBigEndian = defaultBigEndian;
    }

    /**
     * @return platformbigendian
     */
    public static final boolean isPlatformBigEndian()
    {
        return platformBigEndian;
    }

}
