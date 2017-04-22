package org.sim0mq.message.util;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.djunits.value.vdouble.scalar.AbstractDoubleScalar;
import org.djunits.value.vdouble.scalar.MoneyPerArea;
import org.djunits.value.vdouble.scalar.MoneyPerEnergy;
import org.djunits.value.vdouble.scalar.MoneyPerLength;
import org.djunits.value.vdouble.scalar.MoneyPerMass;
import org.djunits.value.vdouble.scalar.MoneyPerVolume;
import org.djunits.value.vfloat.scalar.AbstractFloatScalar;
import org.sim0mq.Sim0MQException;
import org.sim0mq.message.types.Sim0MQTypes;

import nl.tudelft.simulation.language.Throw;

/**
 * Message conversions. These take into account the endianness of the different values. Java is by default big-endian.
 * <p>
 * Copyright (c) 2016-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://sim0mq.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Mar 1, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public final class TypedMessage
{
    /** version of the protocol, magic number. */
    protected static final String version = "SIM01";

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

    /** hashcode of short[] class. */
    protected static final int SHORT_ARRAY_HC = short[].class.hashCode();

    /** hashcode of int[] class. */
    protected static final int INT_ARRAY_HC = int[].class.hashCode();

    /** hashcode of long[] class. */
    protected static final int LONG_ARRAY_HC = long[].class.hashCode();

    /** hashcode of float[] class. */
    protected static final int FLOAT_ARRAY_HC = float[].class.hashCode();

    /** hashcode of double[] class. */
    protected static final int DOUBLE_ARRAY_HC = double[].class.hashCode();

    /** hashcode of boolean[] class. */
    protected static final int BOOLEAN_ARRAY_HC = boolean[].class.hashCode();

    /** hashcode of byte[][] class. */
    protected static final int BYTE_MATRIX_HC = byte[][].class.hashCode();

    /** hashcode of short[][] class. */
    protected static final int SHORT_MATRIX_HC = short[][].class.hashCode();

    /** hashcode of int[][] class. */
    protected static final int INT_MATRIX_HC = int[][].class.hashCode();

    /** hashcode of long[][] class. */
    protected static final int LONG_MATRIX_HC = long[][].class.hashCode();

    /** hashcode of float[][] class. */
    protected static final int FLOAT_MATRIX_HC = float[][].class.hashCode();

    /** hashcode of double[][] class. */
    protected static final int DOUBLE_MATRIX_HC = double[][].class.hashCode();

    /** hashcode of boolean[][] class. */
    protected static final int BOOLEAN_MATRIX_HC = boolean[][].class.hashCode();

    /** the UTF-8 charset. */
    protected static final Charset UTF8 = Charset.forName("UTF-8");

    /** the UTF-16 charset, big endian variant. */
    protected static final Charset UTF16 = Charset.forName("UTF-16BE");

    /**
     * Do not instantiate this utility class.
     */
    private TypedMessage()
    {
        // Utility class; do not instantiate.
    }

    /**
     * Encode the object array into a Big Endian message. Use UTF8 for the characters and for the String.
     * @param content the objects to encode
     * @return the zeroMQ message to send as a byte array
     * @throws Sim0MQException on unknown data type
     */
    public static byte[] encode(final Object... content) throws Sim0MQException
    {
        return encode(true, content);
    }

    /**
     * Encode the object array into a Big Endian message. Field 0 will always be encoded as UTF-8 as "magic number".
     * @param utf8 whether to encode String fields (except field 0) and characters in utf8 or not
     * @param content the objects to encode
     * @return the zeroMQ message to send as a byte array
     * @throws Sim0MQException on unknown data type
     */
    public static byte[] encode(final boolean utf8, final Object... content) throws Sim0MQException
    {
        int size = 0;
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
            else if (hc == DOUBLE_HC)
                size += 8;
            else if (hc == BOOLEAN_HC)
                size += 1;
            else if (hc == CHAR_HC && utf8)
                size += 1;
            else if (hc == CHAR_HC && !utf8)
                size += 2;
            else if (hc == STRING_HC && (utf8 || i == 0))
                size += ((String) content[i]).length() + 4;
            else if (hc == STRING_HC && !utf8)
                size += 2 * ((String) content[i]).length() + 4;
            else if (hc == BYTE_ARRAY_HC)
                size += ((byte[]) content[i]).length + 4;
            else if (hc == SHORT_ARRAY_HC)
                size += 2 * ((short[]) content[i]).length + 4;
            else if (hc == INT_ARRAY_HC)
                size += 4 * ((int[]) content[i]).length + 4;
            else if (hc == LONG_ARRAY_HC)
                size += 8 * ((long[]) content[i]).length + 4;
            else if (hc == FLOAT_ARRAY_HC)
                size += 4 * ((float[]) content[i]).length + 4;
            else if (hc == DOUBLE_ARRAY_HC)
                size += 8 * ((double[]) content[i]).length + 4;
            else if (hc == BOOLEAN_ARRAY_HC)
                size += ((boolean[]) content[i]).length + 4;
            else if (hc == BYTE_MATRIX_HC)
                size += ((byte[][]) content[i]).length * ((byte[][]) content[i])[0].length + 4;
            else if (hc == SHORT_MATRIX_HC)
                size += 2 * ((short[][]) content[i]).length * ((short[][]) content[i])[0].length + 4;
            else if (hc == INT_MATRIX_HC)
                size += 4 * ((int[][]) content[i]).length * ((int[][]) content[i])[0].length + 4;
            else if (hc == LONG_MATRIX_HC)
                size += 8 * ((long[][]) content[i]).length * ((long[][]) content[i])[0].length + 4;
            else if (hc == FLOAT_MATRIX_HC)
                size += 4 * ((float[][]) content[i]).length * ((float[][]) content[i])[0].length + 4;
            else if (hc == DOUBLE_MATRIX_HC)
                size += 8 * ((double[][]) content[i]).length * ((double[][]) content[i])[0].length + 4;
            else if (hc == BOOLEAN_MATRIX_HC)
                size += ((boolean[][]) content[i]).length * ((boolean[][]) content[i])[0].length + 4;
            else if (content[i] instanceof AbstractFloatScalar)
                size += 6 + extraByteMoneyPerQuantity(content[i]);
            else if (content[i] instanceof AbstractDoubleScalar)
                size += 10 + extraByteMoneyPerQuantity(content[i]);
            // else if (content[i] instanceof AbstractFloatVector)
            // size += 0; TODO
            // else if (content[i] instanceof AbstractDoubleVector)
            // size += 0; TODO
            else
                throw new Sim0MQException("Unknown data type " + content[i].getClass() + " for encoding the ZeroMQ message");
        }

        byte[] message = new byte[size];
        int pointer = 0;

        for (int i = 0; i < content.length; i++)
        {
            Object field = content[i];
            int hc = field.getClass().hashCode();
            if (hc == BYTE_HC)
            {
                message[pointer++] = Sim0MQTypes.BYTE_8;
                message[pointer++] = (byte) field;
            }
            else if (hc == SHORT_HC)
            {
                message[pointer++] = Sim0MQTypes.SHORT_16;
                short v = (short) field;
                pointer = EndianUtil.encodeShort(v, message, pointer);
            }
            else if (hc == INTEGER_HC)
            {
                message[pointer++] = Sim0MQTypes.INT_32;
                int v = (int) field;
                pointer = EndianUtil.encodeInt(v, message, pointer);
            }
            else if (hc == LONG_HC)
            {
                message[pointer++] = Sim0MQTypes.LONG_64;
                long v = (long) field;
                pointer = EndianUtil.encodeLong(v, message, pointer);
            }
            else if (hc == FLOAT_HC)
            {
                message[pointer++] = Sim0MQTypes.FLOAT_32;
                float v = (float) field;
                pointer = EndianUtil.encodeFloat(v, message, pointer);
            }
            else if (hc == DOUBLE_HC)
            {
                message[pointer++] = Sim0MQTypes.DOUBLE_64;
                double v = (double) field;
                pointer = EndianUtil.encodeDouble(v, message, pointer);
            }
            if (hc == BOOLEAN_HC)
            {
                message[pointer++] = Sim0MQTypes.BOOLEAN_8;
                message[pointer++] = (byte) ((boolean) field ? 1 : 0);
            }
            else if (hc == CHAR_HC && utf8)
            {
                message[pointer++] = Sim0MQTypes.CHAR_8;
                char v = (char) field;
                message[pointer++] = (byte) (v & 0xFF);
            }
            else if (hc == CHAR_HC && !utf8)
            {
                message[pointer++] = Sim0MQTypes.CHAR_16;
                char v = (char) field;
                pointer = EndianUtil.encodeChar(v, message, pointer);
            }
            else if (hc == STRING_HC && (utf8 || i == 0))
            {
                message[pointer++] = Sim0MQTypes.STRING_8;
                int len = ((String) field).length();
                pointer = EndianUtil.encodeInt(len, message, pointer);
                byte[] s = ((String) field).getBytes(UTF8);
                for (byte b : s)
                {
                    message[pointer++] = b;
                }
            }
            else if (hc == STRING_HC && !utf8)
            {
                message[pointer++] = Sim0MQTypes.STRING_16;
                int len = ((String) field).length();
                pointer = EndianUtil.encodeInt(len, message, pointer);
                byte[] s = ((String) field).getBytes(UTF16);
                for (byte b : s)
                {
                    message[pointer++] = b;
                }
            }
            else if (hc == BYTE_ARRAY_HC)
            {
                message[pointer++] = Sim0MQTypes.BYTE_8_ARRAY;
                int len = ((byte[]) field).length;
                pointer = EndianUtil.encodeInt(len, message, pointer);
                for (byte v : (byte[]) field)
                {
                    message[pointer++] = v;
                }
            }
            else if (hc == SHORT_ARRAY_HC)
            {
                message[pointer++] = Sim0MQTypes.SHORT_16_ARRAY;
                int len = ((short[]) field).length;
                pointer = EndianUtil.encodeInt(len, message, pointer);
                for (short v : (short[]) field)
                {
                    pointer = EndianUtil.encodeShort(v, message, pointer);
                }
            }
            else if (hc == INT_ARRAY_HC)
            {
                message[pointer++] = Sim0MQTypes.INT_32_ARRAY;
                int len = ((int[]) field).length;
                pointer = EndianUtil.encodeInt(len, message, pointer);
                for (int v : (int[]) field)
                {
                    pointer = EndianUtil.encodeInt(v, message, pointer);
                }
            }
            else if (hc == LONG_ARRAY_HC)
            {
                message[pointer++] = Sim0MQTypes.LONG_64_ARRAY;
                int len = ((long[]) field).length;
                pointer = EndianUtil.encodeInt(len, message, pointer);
                for (long v : (long[]) field)
                {
                    pointer = EndianUtil.encodeLong(v, message, pointer);
                }
            }
            else if (hc == FLOAT_ARRAY_HC)
            {
                message[pointer++] = Sim0MQTypes.FLOAT_32_ARRAY;
                int len = ((float[]) field).length;
                pointer = EndianUtil.encodeInt(len, message, pointer);
                for (float v : (float[]) field)
                {
                    pointer = EndianUtil.encodeFloat(v, message, pointer);
                }
            }
            else if (hc == DOUBLE_ARRAY_HC)
            {
                message[pointer++] = Sim0MQTypes.DOUBLE_64_ARRAY;
                int len = ((int[]) field).length;
                pointer = EndianUtil.encodeInt(len, message, pointer);
                for (double v : (double[]) field)
                {
                    pointer = EndianUtil.encodeDouble(v, message, pointer);
                }
            }
            else if (hc == BOOLEAN_ARRAY_HC)
            {
                message[pointer++] = Sim0MQTypes.BOOLEAN_8_ARRAY;
                int len = ((boolean[]) field).length;
                pointer = EndianUtil.encodeInt(len, message, pointer);
                for (boolean v : (boolean[]) field)
                {
                    message[pointer++] = (byte) (v ? 1 : 0);
                }
            }
            else if (hc == BYTE_MATRIX_HC)
            {
                message[pointer++] = Sim0MQTypes.BYTE_8_MATRIX;
                byte[][] array = (byte[][]) field;
                int rows = array.length;
                pointer = EndianUtil.encodeInt(rows, message, pointer);
                int cols = array[0].length;
                pointer = EndianUtil.encodeInt(cols, message, pointer);
                for (int row = 0; row < rows; row++)
                {
                    byte[] vRow = array[row];
                    for (byte v : vRow)
                    {
                        message[pointer++] = v;
                    }
                }
            }
            else if (hc == SHORT_MATRIX_HC)
            {
                message[pointer++] = Sim0MQTypes.SHORT_16_MATRIX;
                short[][] array = (short[][]) field;
                int rows = array.length;
                pointer = EndianUtil.encodeInt(rows, message, pointer);
                int cols = array[0].length;
                pointer = EndianUtil.encodeInt(cols, message, pointer);
                for (int row = 0; row < rows; row++)
                {
                    short[] vRow = array[row];
                    for (short v : vRow)
                    {
                        pointer = EndianUtil.encodeShort(v, message, pointer);
                    }
                }
            }
            else if (hc == INT_MATRIX_HC)
            {
                message[pointer++] = Sim0MQTypes.INT_32_MATRIX;
                int[][] array = (int[][]) field;
                int rows = array.length;
                pointer = EndianUtil.encodeInt(rows, message, pointer);
                int cols = array[0].length;
                pointer = EndianUtil.encodeInt(cols, message, pointer);
                for (int row = 0; row < rows; row++)
                {
                    int[] vRow = array[row];
                    for (int v : vRow)
                    {
                        pointer = EndianUtil.encodeInt(v, message, pointer);
                    }
                }
            }
            else if (hc == LONG_MATRIX_HC)
            {
                message[pointer++] = Sim0MQTypes.LONG_64_MATRIX;
                long[][] array = (long[][]) field;
                int rows = array.length;
                pointer = EndianUtil.encodeInt(rows, message, pointer);
                int cols = array[0].length;
                pointer = EndianUtil.encodeInt(cols, message, pointer);
                for (int row = 0; row < rows; row++)
                {
                    long[] vRow = array[row];
                    for (long v : vRow)
                    {
                        pointer = EndianUtil.encodeLong(v, message, pointer);
                    }
                }
            }
            else if (hc == FLOAT_MATRIX_HC)
            {
                message[pointer++] = Sim0MQTypes.FLOAT_32_MATRIX;
                float[][] array = (float[][]) field;
                int rows = array.length;
                pointer = EndianUtil.encodeInt(rows, message, pointer);
                int cols = array[0].length;
                pointer = EndianUtil.encodeInt(cols, message, pointer);
                for (int row = 0; row < rows; row++)
                {
                    float[] vRow = array[row];
                    for (float v : vRow)
                    {
                        pointer = EndianUtil.encodeFloat(v, message, pointer);
                    }
                }
            }
            else if (hc == DOUBLE_MATRIX_HC)
            {
                message[pointer++] = Sim0MQTypes.DOUBLE_64_MATRIX;
                double[][] array = (double[][]) field;
                int rows = array.length;
                pointer = EndianUtil.encodeInt(rows, message, pointer);
                int cols = array[0].length;
                pointer = EndianUtil.encodeInt(cols, message, pointer);
                for (int row = 0; row < rows; row++)
                {
                    double[] vRow = array[row];
                    for (double v : vRow)
                    {
                        pointer = EndianUtil.encodeDouble(v, message, pointer);
                    }
                }
            }
            else if (hc == BOOLEAN_MATRIX_HC)
            {
                message[pointer++] = Sim0MQTypes.BOOLEAN_8_MATRIX;
                boolean[][] array = (boolean[][]) field;
                int rows = array.length;
                pointer = EndianUtil.encodeInt(rows, message, pointer);
                int cols = array[0].length;
                pointer = EndianUtil.encodeInt(cols, message, pointer);
                for (int row = 0; row < rows; row++)
                {
                    boolean[] vRow = array[row];
                    for (boolean v : vRow)
                    {
                        message[pointer++] = (byte) (v ? 1 : 0);
                    }
                }
            }
            else if (content[i] instanceof AbstractFloatScalar)
            {
                // TODO
            }
            else if (content[i] instanceof AbstractDoubleScalar)
            {
                // TODO
            }
        }

        return message;
    }

    /**
     * Decode the message into an object array.
     * @param message the ZeroMQ byte array to decode
     * @return an array of objects of the right type
     * @throws Sim0MQException on unknown data type
     */
    public static Object[] decode(final byte[] message) throws Sim0MQException
    {
        // magic number
        int pointer = 0;
        Throw.when(message.length < 10, Sim0MQException.class, "Message length < 10");
        byte char0 = message[0];
        Throw.when(char0 != 9, Sim0MQException.class, "Message does not start with an UTF8 string");
        int magicLen = EndianUtil.decodeInt(message, 1);
        Throw.when(magicLen < 0, Sim0MQException.class, "Length of magic number < 0");
        Throw.when(Double.isNaN(magicLen), Sim0MQException.class, "Length of magic number = NaN");
        Throw.when(Double.isInfinite(magicLen), Sim0MQException.class, "Length of magic number = Infinite");
        Throw.when(magicLen > 10, Sim0MQException.class, "Length of magic number > 10");
        String magicNumber = EndianUtil.decodeUTF8String(message, 1);
        Throw.when(!magicNumber.startsWith("SIM"), Sim0MQException.class,
                "Magic number does not start with SIM but with " + magicNumber);
        Throw.when(!magicNumber.equals(version), Sim0MQException.class,
                "Message version " + magicNumber + " not compatible with this software version " + version);
        pointer += 5 + magicLen;

        List<Object> list = new ArrayList<>();
        list.add(magicNumber);

        while (pointer < message.length)
        {
            byte type = message[pointer++];

            if (type == Sim0MQTypes.BYTE_8)
            {
                list.add(message[pointer]);
                pointer += 1;
            }
            else if (type == Sim0MQTypes.SHORT_16)
            {
                list.add(EndianUtil.decodeShort(message, pointer));
                pointer += 2;
            }
            else if (type == Sim0MQTypes.INT_32)
            {
                list.add(EndianUtil.decodeInt(message, pointer));
                pointer += 4;
            }
            else if (type == Sim0MQTypes.LONG_64)
            {
                list.add(EndianUtil.decodeLong(message, pointer));
                pointer += 8;
            }
            else if (type == Sim0MQTypes.FLOAT_32)
            {
                list.add(EndianUtil.decodeFloat(message, pointer));
                pointer += 4;
            }
            else if (type == Sim0MQTypes.DOUBLE_64)
            {
                list.add(EndianUtil.decodeDouble(message, pointer));
                pointer += 8;
            }
            else if (type == Sim0MQTypes.BOOLEAN_8)
            {
                byte b = message[pointer];
                list.add(b == 0 ? false : true);
                pointer += 1;
            }
            else if (type == Sim0MQTypes.CHAR_8)
            {
                list.add((char) message[pointer]);
                pointer += 1;
            }
            else if (type == Sim0MQTypes.CHAR_16)
            {
                list.add(EndianUtil.decodeChar(message, pointer));
                pointer += 2;
            }
            else if (type == Sim0MQTypes.STRING_8)
            {
                String s = EndianUtil.decodeUTF8String(message, pointer);
                list.add(s);
                pointer += 4 + s.length();
            }
            else if (type == Sim0MQTypes.STRING_16)
            {
                String s = EndianUtil.decodeUTF16String(message, pointer);
                list.add(s);
                pointer += 4 + 2 * s.length();
            }
            else if (type == Sim0MQTypes.BYTE_8_ARRAY)
            {

            }
            else if (type == Sim0MQTypes.SHORT_16_ARRAY)
            {

            }
            else if (type == Sim0MQTypes.INT_32_ARRAY)
            {

            }
            else if (type == Sim0MQTypes.LONG_64_ARRAY)
            {

            }
            else if (type == Sim0MQTypes.FLOAT_32_ARRAY)
            {

            }
            else if (type == Sim0MQTypes.DOUBLE_64_ARRAY)
            {

            }
            else if (type == Sim0MQTypes.BOOLEAN_8_ARRAY)
            {

            }
            else if (type == Sim0MQTypes.BYTE_8_MATRIX)
            {

            }
            else if (type == Sim0MQTypes.SHORT_16_MATRIX)
            {

            }
            else if (type == Sim0MQTypes.INT_32_MATRIX)
            {

            }
            else if (type == Sim0MQTypes.LONG_64_MATRIX)
            {

            }
            else if (type == Sim0MQTypes.FLOAT_32_MATRIX)
            {

            }
            else if (type == Sim0MQTypes.DOUBLE_64_MATRIX)
            {

            }
            else if (type == Sim0MQTypes.BOOLEAN_8_MATRIX)
            {

            }
            else if (type == Sim0MQTypes.FLOAT_32_UNIT)
            {

            }
            else if (type == Sim0MQTypes.DOUBLE_64_UNIT)
            {

            }

        }

        Object[] array = list.toArray();
        return array;
    }

    /**
     * Indicate whether an extra byte is needed for a Money per quantity type.
     * @param o the object to check
     * @return 0 or 1 to indicate whether an extra byte is needed
     */
    private static int extraByteMoneyPerQuantity(final Object o)
    {
        if (o instanceof MoneyPerArea || o instanceof MoneyPerEnergy || o instanceof MoneyPerLength || o instanceof MoneyPerMass
                || o instanceof MoneyPerMass || o instanceof MoneyPerVolume)
        {
            return 1;
        }
        return 0;
    }

    /**
     * Return a readable string with the bytes in a byte[] message.
     * @param bytes byte[]; the byte array to display
     * @return String; a readable string with the bytes in a byte[] message
     */
    public static String printBytes(final byte[] bytes)
    {
        StringBuffer s = new StringBuffer();
        s.append("|");
        for (int b : bytes)
        {
            if (b < 0)
            {
                b += 128;
            }
            if (b >= 32 && b <= 127)
            {
                s.append("#" + Integer.toString(b, 16).toUpperCase() + "(" + (char) (byte) b + ")|");
            }
            else
            {
                s.append("#" + Integer.toString(b, 16).toUpperCase() + "|");
            }
        }
        return s.toString();
    }
}
