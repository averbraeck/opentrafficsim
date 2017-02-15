package org.sim0mq.message;

import java.nio.ByteOrder;
import java.nio.charset.Charset;

import org.sim0mq.ZeroMQException;

/**
 * Message conversions. These take into account the endianness of the different values. Java is by default big-endian.
 * <p>
 * (c) copyright 2002-2016 <a href="http://www.simulation.tudelft.nl">Delft University of Technology</a>. <br>
 * BSD-style license. See <a href="http://www.simulation.tudelft.nl/dsol/3.0/license.html">DSOL License</a>. <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @version Oct 21, 2016
 */
public class Message
{
    /** byte, 8 bit signed two's complement integer. */
    protected static final byte BYTE_8 = 0;

    /** short, 16 bit signed two's complement integer. */
    protected static final byte SHORT_16 = 1;

    /** int, 32 bit signed two's complement integer. */
    protected static final byte INT_32 = 2;

    /** long, 64 bit signed two's complement integer. */
    protected static final byte LONG_64 = 3;

    /** float, single-precision 32-bit IEEE 754 floating point. */
    protected static final byte FLOAT_32 = 4;

    /** float, double-precision 64-bit IEEE 754 floating point. */
    protected static final byte DOUBLE_64 = 5;

    /** boolean, sent / received as a byte; 0 = false, 1 = true. */
    protected static final byte BOOLEAN_8 = 6;

    /** char, 8-bit ASCII character. */
    protected static final byte CHAR_8 = 7;

    /** char, 16-bit Unicode character, big endian order. */
    protected static final byte CHAR_16 = 8;

    /** String, number-preceded byte array of 8-bits characters. */
    protected static final byte STRING_8 = 9;

    /** String, number-preceded char array of 16-bits characters, big-endian order. */
    protected static final byte STRING_16 = 10;

    /** Number-preceded byte array. */
    protected static final byte BYTE_8_ARRAY = 11;

    /** hashcode of Byte class. */
    protected static final int BYTE_HC = Byte.class.hashCode();

    /** hashcode of Short class. */
    protected static final int SHORT_HC = Short.class.hashCode();

    /** hashcode of Integer class. */
    protected static final int INTEGER_HC = Integer.class.hashCode();

    /** hashcode of Long class. */
    protected static final int LONG_HC = Long.class.hashCode();

    /** hashcode of Float class. */
    protected static final int FLOAT_HC = Float.class.hashCode();

    /** hashcode of Double class. */
    protected static final int DOUBLE_HC = Double.class.hashCode();

    /** hashcode of Boolean class. */
    protected static final int BOOLEAN_HC = Boolean.class.hashCode();

    /** hashcode of Character class. */
    protected static final int CHAR_HC = Character.class.hashCode();

    /** hashcode of String class. */
    protected static final int STRING_HC = String.class.hashCode();

    /** hashcode of byte[] class. */
    protected static final int BYTE_ARRAY_HC = byte[].class.hashCode();

    /** the UTF-8 charset. */
    protected static final Charset UTF8 = Charset.forName("UTF-8");

    /** the UTF-16 charset, big endian variant. */
    protected static final Charset UTF16 = Charset.forName("UTF-16BE");

    // TODO djunits, vector, matrix, google protobuf.
    /**
     * Encode the object array into a message.
     * @param content the objects to encode
     * @return the zeroMQ message to send as a byte array
     * @throws ZeroMQException on unknown data type
     */
    public static byte[] encode(final Object[] content) throws ZeroMQException
    {
        int size = 5; // int type + number of fields
        for (int i = 0; i < content.length; i++)
        {
            size++; // for the field type
            int hc = content[i].getClass().hashCode();
            if (hc == BYTE_HC)
                size += 1;
            else if (hc == SHORT_HC)
                size += 2;
            else if (hc == INTEGER_HC)
                size += 4;
            else if (hc == LONG_HC)
                size += 8;
            else if (hc == FLOAT_HC)
                size += 4;
            else if (hc == BOOLEAN_HC)
                size += 1;
            else if (hc == DOUBLE_HC)
                size += 8;
            else if (hc == CHAR_HC)
                size += 2;
            else if (hc == STRING_HC)
                size += ((String) content[i]).length() + 4;
            else if (hc == BYTE_ARRAY_HC)
                size += ((byte[]) content[i]).length + 4;
            else
                throw new ZeroMQException(
                        "Unknown data type " + content[i].getClass() + " for encoding the ZeroMQ message");
        }

        byte[] message = new byte[size];
        int pointer = 0;

        // BIG ENDIAN ENCODING

        if (ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN))
        {
            int nrFields = content.length;
            message[pointer++] = INT_32;
            message[pointer++] = (byte) ((nrFields >> 24) & 0xFF);
            message[pointer++] = (byte) ((nrFields >> 16) & 0xFF);
            message[pointer++] = (byte) ((nrFields >> 8) & 0xFF);
            message[pointer++] = (byte) (nrFields & 0xFF);

            for (int i = 0; i < content.length; i++)
            {
                Object field = content[i];
                int hc = field.getClass().hashCode();
                if (hc == BYTE_HC)
                {
                    message[pointer++] = BYTE_8;
                    message[pointer++] = (byte) field;
                }
                else if (hc == SHORT_HC)
                {
                    message[pointer++] = SHORT_16;
                    short v = (short) field;
                    message[pointer++] = (byte) (v >> 8);
                    message[pointer++] = (byte) (v);
                }
                else if (hc == INTEGER_HC)
                {
                    message[pointer++] = INT_32;
                    int v = (int) field;
                    message[pointer++] = (byte) ((v >> 24) & 0xFF);
                    message[pointer++] = (byte) ((v >> 16) & 0xFF);
                    message[pointer++] = (byte) ((v >> 8) & 0xFF);
                    message[pointer++] = (byte) (v & 0xFF);
                }
                else if (hc == LONG_HC)
                {
                    message[pointer++] = LONG_64;
                    long v = (long) field;
                    message[pointer++] = (byte) ((v >> 56) & 0xFF);
                    message[pointer++] = (byte) ((v >> 48) & 0xFF);
                    message[pointer++] = (byte) ((v >> 40) & 0xFF);
                    message[pointer++] = (byte) ((v >> 32) & 0xFF);
                    message[pointer++] = (byte) ((v >> 24) & 0xFF);
                    message[pointer++] = (byte) ((v >> 16) & 0xFF);
                    message[pointer++] = (byte) ((v >> 8) & 0xFF);
                    message[pointer++] = (byte) (v & 0xFF);
                }
                else if (hc == FLOAT_HC)
                {
                    message[pointer++] = FLOAT_32;
                    int v = Float.floatToIntBits((float) field);
                    message[pointer++] = (byte) ((v >> 24) & 0xFF);
                    message[pointer++] = (byte) ((v >> 16) & 0xFF);
                    message[pointer++] = (byte) ((v >> 8) & 0xFF);
                    message[pointer++] = (byte) (v & 0xFF);
                }
                else if (hc == DOUBLE_HC)
                {
                    message[pointer++] = DOUBLE_64;
                    long v = Double.doubleToLongBits((double) field);
                    message[pointer++] = (byte) ((v >> 56) & 0xFF);
                    message[pointer++] = (byte) ((v >> 48) & 0xFF);
                    message[pointer++] = (byte) ((v >> 40) & 0xFF);
                    message[pointer++] = (byte) ((v >> 32) & 0xFF);
                    message[pointer++] = (byte) ((v >> 24) & 0xFF);
                    message[pointer++] = (byte) ((v >> 16) & 0xFF);
                    message[pointer++] = (byte) ((v >> 8) & 0xFF);
                    message[pointer++] = (byte) (v & 0xFF);
                }
                if (hc == BOOLEAN_HC)
                {
                    message[pointer++] = BOOLEAN_8;
                    message[pointer++] = (byte) ((boolean) field ? 1 : 0);
                }
                else if (hc == CHAR_HC)
                {
                    message[pointer++] = CHAR_16;
                    char v = (char) field;
                    message[pointer++] = (byte) (v >> 8);
                    message[pointer++] = (byte) (v);
                }
                else if (hc == STRING_HC)
                {
                    message[pointer++] = STRING_8;
                    int len = ((String) field).length();
                    message[pointer++] = (byte) ((len >> 24) & 0xFF);
                    message[pointer++] = (byte) ((len >> 16) & 0xFF);
                    message[pointer++] = (byte) ((len >> 8) & 0xFF);
                    message[pointer++] = (byte) (len & 0xFF);
                    byte[] s = ((String) field).getBytes(UTF8);
                    for (byte b : s)
                    {
                        message[pointer++] = b;
                    }
                }
                else if (hc == BYTE_ARRAY_HC)
                {
                    message[pointer++] = BYTE_8_ARRAY;
                    int len = ((byte[]) field).length;
                    message[pointer++] = (byte) ((len >> 24) & 0xFF);
                    message[pointer++] = (byte) ((len >> 16) & 0xFF);
                    message[pointer++] = (byte) ((len >> 8) & 0xFF);
                    message[pointer++] = (byte) (len & 0xFF);
                    for (byte b : (byte[]) field)
                    {
                        message[pointer++] = b;
                    }
                }
            }
        }

        // LITTLE ENDIAN ENCODING

        else

        {
            int nrFields = content.length;
            message[pointer++] = INT_32;
            message[pointer++] = (byte) (nrFields & 0xFF);
            message[pointer++] = (byte) ((nrFields >> 8) & 0xFF);
            message[pointer++] = (byte) ((nrFields >> 16) & 0xFF);
            message[pointer++] = (byte) ((nrFields >> 24) & 0xFF);

            for (int i = 0; i < content.length; i++)
            {
                Object field = content[i];
                int hc = field.getClass().hashCode();
                if (hc == BYTE_HC)
                {
                    message[pointer++] = BYTE_8;
                    message[pointer++] = (byte) field;
                }
                else if (hc == SHORT_HC)
                {
                    message[pointer++] = SHORT_16;
                    short v = (short) field;
                    message[pointer++] = (byte) (v >> 8);
                    message[pointer++] = (byte) (v);
                }
                else if (hc == INTEGER_HC)
                {
                    message[pointer++] = INT_32;
                    int v = (int) field;
                    message[pointer++] = (byte) (v & 0xFF);
                    message[pointer++] = (byte) ((v >> 8) & 0xFF);
                    message[pointer++] = (byte) ((v >> 16) & 0xFF);
                    message[pointer++] = (byte) ((v >> 24) & 0xFF);
                }
                else if (hc == LONG_HC)
                {
                    message[pointer++] = LONG_64;
                    long v = (long) field;
                    message[pointer++] = (byte) (v & 0xFF);
                    message[pointer++] = (byte) ((v >> 8) & 0xFF);
                    message[pointer++] = (byte) ((v >> 16) & 0xFF);
                    message[pointer++] = (byte) ((v >> 24) & 0xFF);
                    message[pointer++] = (byte) ((v >> 32) & 0xFF);
                    message[pointer++] = (byte) ((v >> 40) & 0xFF);
                    message[pointer++] = (byte) ((v >> 48) & 0xFF);
                    message[pointer++] = (byte) ((v >> 56) & 0xFF);
                }
                else if (hc == FLOAT_HC)
                {
                    message[pointer++] = FLOAT_32;
                    int v = Float.floatToIntBits((float) field);
                    message[pointer++] = (byte) (v & 0xFF);
                    message[pointer++] = (byte) ((v >> 8) & 0xFF);
                    message[pointer++] = (byte) ((v >> 16) & 0xFF);
                    message[pointer++] = (byte) ((v >> 24) & 0xFF);
                }
                else if (hc == DOUBLE_HC)
                {
                    message[pointer++] = DOUBLE_64;
                    long v = Double.doubleToLongBits((double) field);
                    message[pointer++] = (byte) (v & 0xFF);
                    message[pointer++] = (byte) ((v >> 8) & 0xFF);
                    message[pointer++] = (byte) ((v >> 16) & 0xFF);
                    message[pointer++] = (byte) ((v >> 24) & 0xFF);
                    message[pointer++] = (byte) ((v >> 32) & 0xFF);
                    message[pointer++] = (byte) ((v >> 40) & 0xFF);
                    message[pointer++] = (byte) ((v >> 48) & 0xFF);
                    message[pointer++] = (byte) ((v >> 56) & 0xFF);
                }
                if (hc == BOOLEAN_HC)
                {
                    message[pointer++] = BOOLEAN_8;
                    message[pointer++] = (byte) ((boolean) field ? 1 : 0);
                }
                else if (hc == CHAR_HC)
                {
                    message[pointer++] = CHAR_16;
                    char v = (char) field;
                    message[pointer++] = (byte) (v >> 8);
                    message[pointer++] = (byte) (v);
                }
                else if (hc == STRING_HC)
                {
                    message[pointer++] = STRING_8;
                    int len = ((String) field).length();
                    message[pointer++] = (byte) (len & 0xFF);
                    message[pointer++] = (byte) ((len >> 8) & 0xFF);
                    message[pointer++] = (byte) ((len >> 16) & 0xFF);
                    message[pointer++] = (byte) ((len >> 24) & 0xFF);
                    byte[] s = ((String) field).getBytes(UTF8);
                    for (byte b : s)
                    {
                        message[pointer++] = b;
                    }
                }
                else if (hc == BYTE_ARRAY_HC)
                {
                    message[pointer++] = BYTE_8_ARRAY;
                    int len = ((byte[]) field).length;
                    message[pointer++] = (byte) (len & 0xFF);
                    message[pointer++] = (byte) ((len >> 8) & 0xFF);
                    message[pointer++] = (byte) ((len >> 16) & 0xFF);
                    message[pointer++] = (byte) ((len >> 24) & 0xFF);
                    for (byte b : (byte[]) field)
                    {
                        message[pointer++] = b;
                    }
                }
            }
        }

        return message;
    }

    /**
     * Decode the message into an object array.
     * @param message the ZeroMQ byte array to decode
     * @return an array of objects of the right type
     * @throws ZeroMQException on unknown data type
     */
    public static Object[] decode(final byte[] message) throws ZeroMQException
    {
        int pointer = 0;
        int nrFields = EndianUtil.decodeInt(message, 0);
        pointer += 5;

        Object[] array = new Object[nrFields];
        for (int i = 0; i < nrFields; i++)
        {
            byte type = message[pointer++];

            if (type == BYTE_8)
            {
                array[i] = message[pointer];
                pointer += 1;
            }
            else if (type == SHORT_16)
            {
                array[i] = (short) (((message[pointer] & 0xff) << 8) | ((message[pointer + 1] & 0xff)));
                pointer += 2;
            }
            else if (type == INT_32)
            {
                array[i] = EndianUtil.decodeIntBigEndian(message, pointer);
                pointer += 4;
            }
            else if (type == LONG_64)
            {
                array[i] = EndianUtil.decodeLongBigEndian(message, pointer);
                pointer += 8;
            }
            else if (type == FLOAT_32)
            {
                int bits = EndianUtil.decodeIntBigEndian(message, pointer);
                array[i] = Float.intBitsToFloat(bits);
                pointer += 4;
            }
            else if (type == DOUBLE_64)
            {
                long bits = EndianUtil.decodeLongBigEndian(message, pointer);
                array[i] = Double.longBitsToDouble(bits);
                pointer += 4;
            }
            else if (type == BOOLEAN_8)
            {
                array[i] = message[i] != 0;
                pointer += 1;
            }
            else if (type == CHAR_16)
            {
                // TODO array[i] = 
                pointer += 2;
            }
        }

        return array;
    }

}
