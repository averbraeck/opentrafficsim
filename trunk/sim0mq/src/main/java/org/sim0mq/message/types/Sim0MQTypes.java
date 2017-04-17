package org.sim0mq.message.types;

/**
 * Type numbers to encode different data types.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Mar 4, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public final class Sim0MQTypes
{
    /** byte, 8 bit signed two's complement integer. */
    public static final byte BYTE_8 = 0;

    /** short, 16 bit signed two's complement integer. */
    public static final byte SHORT_16 = 1;

    /** int, 32 bit signed two's complement integer. */
    public static final byte INT_32 = 2;

    /** long, 64 bit signed two's complement integer. */
    public static final byte LONG_64 = 3;

    /** float, single-precision 32-bit IEEE 754 floating point. */
    public static final byte FLOAT_32 = 4;

    /** float, double-precision 64-bit IEEE 754 floating point. */
    public static final byte DOUBLE_64 = 5;

    /** boolean, sent / received as a byte; 0 = false, 1 = true. */
    public static final byte BOOLEAN_8 = 6;

    /** char, 8-bit ASCII character. */
    public static final byte CHAR_8 = 7;

    /** char, 16-bit Unicode character, big endian order. */
    public static final byte CHAR_16 = 8;

    /**
     * String, number-preceded byte array of 8-bits characters. The string types are preceded by a 32-bit int indicating the
     * number of characters in the array that follows. This int is itself not preceded by a byte indicating it is an int. An
     * ASCII string "Hello" is therefore coded as follows: |9|0|0|0|5|H|e|l|l|o|
     */
    public static final byte STRING_8 = 9;

    /**
     * String, number-preceded char array of 16-bits characters, big-endian order. The string types are preceded by a 32-bit int
     * indicating the number of characters in the array that follows. This int is itself not preceded by a byte indicating it is
     * an int.
     */
    public static final byte STRING_16 = 10;

    /**
     * Number-preceded byte array. The array types are preceded by a 32-bit int indicating the number of values in the array
     * that follows. This int is itself not preceded by a byte indicating it is an int. An array of 8 bytes with numbers 1
     * through 8 is therefore coded as follows: |11|0|0|0|8|1|2|3|4|5|6|7|8|
     */
    public static final byte BYTE_8_ARRAY = 11;

    /**
     * Number-preceded short array. The array types are preceded by a 32-bit int indicating the number of values in the array
     * that follows. This int is itself not preceded by a byte indicating it is an int. An array of 8 shorts with numbers 100
     * through 107 is therefore coded as follows: |12|0|0|0|8|0|100|0|101|0|102|0|103|0|104|0|105|0|106|0|107|
     */
    public static final byte SHORT_16_ARRAY = 12;

    /**
     * Number-preceded int array. The array types are preceded by a 32-bit int indicating the number of values in the array that
     * follows. This int is itself not preceded by a byte indicating it is an int. An array of 4 ints with numbers 100 through
     * 103 is therefore coded as follows: |13|0|0|0|4|0|0|0|100|0|0|0|101|0|0|0|102|0|0|0|103|
     */
    public static final byte INT_32_ARRAY = 13;

    /**
     * Number-preceded long array. The array types are preceded by a 32-bit int indicating the number of values in the array
     * that follows. This int is itself not preceded by a byte indicating it is an int. An array of 3 longs with numbers 100
     * through 102 is therefore coded as follows: |14|0|0|0|3|0|0|0|0|0|0|0|100|0|0|0|0|0|0|0|101|0|0|0|0|0|0|0|102|
     */
    public static final byte LONG_64_ARRAY = 14;

    /**
     * Number-preceded float array. The array types are preceded by a 32-bit int indicating the number of values in the array
     * that follows. This int is itself not preceded by a byte indicating it is an int.
     */
    public static final byte FLOAT_32_ARRAY = 15;

    /**
     * Number-preceded double array. The array types are preceded by a 32-bit int indicating the number of values in the array
     * that follows. This int is itself not preceded by a byte indicating it is an int.
     */
    public static final byte DOUBLE_64_ARRAY = 16;

    /**
     * Number-preceded boolean array. The array types are preceded by a 32-bit int indicating the number of values in the array
     * that follows. This int is itself not preceded by a byte indicating it is an int.
     */
    public static final byte BOOLEAN_8_ARRAY = 17;

    /**
     * Number of rows and number of columns preceded byte matrix. The matrix types are preceded by a 32-bit int indicating the
     * number of rows, followed by a 32-bit int indicating the number of columns. These integers are not preceded by a byte
     * indicating it is an int. The number of values in the matrix that follows is rows * columns. The data is stored row by
     * row, without a separator between the rows. A matrix with 2 rows and 3 columns of bytes 1-2-4 6-7-8 is therefore coded as
     * follows: |18|0|0|0|2|0|0|0|3|0|1|0|2|0|4|0|6|0|7|0|8|<br>
     * In the language sending or receiving a matrix, the rows are denoted by the outer index, and the columns by the inner
     * index: matrix[row][col].
     */
    public static final byte BYTE_8_MATRIX = 18;

    /**
     * Number of rows and number of columns preceded short matrix. The matrix types are preceded by a 32-bit int indicating the
     * number of rows, followed by a 32-bit int indicating the number of columns. These integers are not preceded by a byte
     * indicating it is an int. The number of values in the matrix that follows is rows * columns. The data is stored row by
     * row, without a separator between the rows. A matrix with 2 rows and 3 columns of shorts 1-2-4 6-7-8 is therefore coded as
     * follows: |19|0|0|0|2|0|0|0|3|1|2|4|6|7|8|<br>
     * In the language sending or receiving a matrix, the rows are denoted by the outer index, and the columns by the inner
     * index: matrix[row][col].
     */
    public static final byte SHORT_16_MATRIX = 19;

    /**
     * Number of rows and number of columns preceded int matrix. The matrix types are preceded by a 32-bit int indicating the
     * number of rows, followed by a 32-bit int indicating the number of columns. These integers are not preceded by a byte
     * indicating it is an int. The number of values in the matrix that follows is rows * columns. The data is stored row by
     * row, without a separator between the rows. A matrix with 2 rows and 3 columns of integers 1-2-4 6-7-8 is therefore coded
     * as follows: |20|0|0|0|2|0|0|0|3|0|0|0|1|0|0|0|2|0|0|0|4|0|0|0|6|0|0|0|7|0|0|0|8|<br>
     * In the language sending or receiving a matrix, the rows are denoted by the outer index, and the columns by the inner
     * index: matrix[row][col].
     */
    public static final byte INT_32_MATRIX = 20;

    /**
     * Number of rows and number of columns preceded long matrix. The matrix types are preceded by a 32-bit int indicating the
     * number of rows, followed by a 32-bit int indicating the number of columns. These integers are not preceded by a byte
     * indicating it is an int. The number of values in the matrix that follows is rows * columns. The data is stored row by
     * row, without a separator between the rows. A matrix with 2 rows and 3 columns of long vales 1-2-4 6-7-8 is therefore
     * coded as follows:
     * |21|0|0|0|2|0|0|0|3|0|0|0|0|0|0|0|1|0|0|0|0|0|0|0|2|0|0|0|0|0|0|0|4|0|0|0|0|0|0|0|6|0|0|0|0|0|0|0|7|0|0|0|0|0|0|0|8|<br>
     * In the language sending or receiving a matrix, the rows are denoted by the outer index, and the columns by the inner
     * index: matrix[row][col].
     */
    public static final byte LONG_64_MATRIX = 21;

    /**
     * Number of rows and number of columns preceded float matrix. The matrix types are preceded by a 32-bit int indicating the
     * number of rows, followed by a 32-bit int indicating the number of columns. These integers are not preceded by a byte
     * indicating it is an int. The number of values in the matrix that follows is rows * columns. The data is stored row by
     * row, without a separator between the rows.<br>
     * In the language sending or receiving a matrix, the rows are denoted by the outer index, and the columns by the inner
     * index: matrix[row][col].
     */
    public static final byte FLOAT_32_MATRIX = 22;

    /**
     * Number of rows and number of columns preceded double matrix. The matrix types are preceded by a 32-bit int indicating the
     * number of rows, followed by a 32-bit int indicating the number of columns. These integers are not preceded by a byte
     * indicating it is an int. The number of values in the matrix that follows is rows * columns. The data is stored row by
     * row, without a separator between the rows.<br>
     * In the language sending or receiving a matrix, the rows are denoted by the outer index, and the columns by the inner
     * index: matrix[row][col].
     */
    public static final byte DOUBLE_64_MATRIX = 23;

    /**
     * Number of rows and number of columns preceded boolean matrix. The matrix types are preceded by a 32-bit int indicating
     * the number of rows, followed by a 32-bit int indicating the number of columns. These integers are not preceded by a byte
     * indicating it is an int. The number of values in the matrix that follows is rows * columns. The data is stored row by
     * row, without a separator between the rows.<br>
     * In the language sending or receiving a matrix, the rows are denoted by the outer index, and the columns by the inner
     * index: matrix[row][col].
     */
    public static final byte BOOLEAN_8_MATRIX = 24;

    /**
     * Float, stored internally in the SI unit, with a unit type and display type attached. The internal storage of the value
     * that is transmitted is always in the SI (or standard) unit, except for money where the display unit is used. The value is
     * preceded by a one-byte unit type, and a one-byte display type (or two-byte in case of the MoneyPerUnit). As an example:
     * suppose the unit indicates that the type is a length, whereas the display type indicates that the internally stored value
     * 60000.0 should be displayed as 60.0 km, this is coded as follows: |25|16|11|0x47|0x6A|0x60|0x00|
     */
    public static final byte FLOAT_32_UNIT = 25;

    /**
     * Double, stored internally in the SI unit, with a unit type and display type attached. The internal storage of the value
     * that is transmitted is always in the SI (or standard) unit, except for money where the display unit is used. The value is
     * preceded by a one-byte unit type and a one-byte display type (or two-byte in case of the MoneyPerUnit). As an example:
     * suppose the unit indicates that the type is a length, whereas the display type indicates that the internally stored value
     * 60000.0 should be displayed as 60.0 km, this is coded as follows: |26|16|11|0x47|0x6A|0x60|0x00|0x00|0x00|0x00|0x00|
     */
    public static final byte DOUBLE_64_UNIT = 26;

    /**
     * Number-preceded dense float array, stored internally in the SI unit, with a unit type and display type. After the byte
     * with value 27, the array types have a 32-bit int indicating the number of values in the array that follows. This int is
     * itself not preceded by a byte indicating it is an int. Then a one-byte unit type follows and a one-byte display type (or
     * two-byte in case of the MoneyPerUnit). The internal storage of the values that are transmitted after that always use the
     * SI (or standard) unit, except for money where the display unit is used. As an example: when we send an array of two
     * durations, 2.0 minutes and 2.5 minutes, this is coded as follows:
     * |27|0|0|0|2|25|7|0x40|0x00|0x00|0x00|0x40|0x20|0x00|0x00|
     */
    public static final byte FLOAT_32_UNIT_ARRAY = 27;

    /**
     * Number-preceded dense double array, stored internally in the SI unit, with a unit type and display type. After the byte
     * with value 28, the array types have a 32-bit int indicating the number of values in the array that follows. This int is
     * itself not preceded by a byte indicating it is an int. Then a one-byte unit type follows and a one-byte display type (or
     * two-byte in case of the MoneyPerUnit). The internal storage of the values that are transmitted after that always use the
     * SI (or standard) unit, except for money where the display unit is used. As an example: when we send an array of two
     * durations, 21.2 minutes and 21.5 minutes, this is coded as follows:
     * |28|0|0|0|2|25|7|0x40|0x35|0x33|0x33|0x3|0x33|0x33|0x33|0x40|0x35|0x80|0x00|0x00|0x00|0x00|0x00|
     */
    public static final byte DOUBLE_64_UNIT_ARRAY = 28;

    /**
     * Rows/Cols-preceded dense float array, stored internally in the SI unit, with a unit type and display type. After the byte
     * with value 29, the matrix types have a 32-bit int indicating the number of rows in the array that follows, followed by a
     * 32-bit int indicating the number of columns. These integers are not preceded by a byte indicating it is an int. Then a
     * one-byte unit type follows and a one-byte (or two-byte in case of the MoneyPerUnit) display type The internal storage of
     * the values that are transmitted after that always use the SI (or standard) unit, except for money where the display unit
     * is used. Summarized, the coding is as follows:
     * 
     * <pre>
     * |29|  |R|O|W|S|  |C|O|L|S|  |UT|  |DT|
     * |R|1|C|1|  |R|1|C|2| ... |R|1|C|n| 
     * |R|2|C|1|  |R|2|C|2| ... |R|2|C|n| 
     * ... 
     * |R|m|C|1|  |R|m|C|2| ... |R|m|C|n|
     * </pre>
     * 
     * In the language sending ore receiving a matrix, the rows are denoted by the outer index, and the columns by the inner
     * index: matrix[row][col].
     */
    public static final byte FLOAT_32_UNIT_MATRIX = 29;

    /**
     * Rows/Cols-preceded dense double array, stored internally in the SI unit, with a unit type and display type. After the
     * byte with value 30, the matrix types have a 32-bit int indicating the number of rows in the array that follows, followed
     * by a 32-bit int indicating the number of columns. These integers are not preceded by a byte indicating it is an int. Then
     * a one-byte unit type follows and a one-byte (or two-byte in case of the MoneyPerUnit) display type The internal storage
     * of the values that are transmitted after that always use the SI (or standard) unit, except for money where the display
     * unit is used. Summarized, the coding is as follows:
     * 
     * <pre>
     * |30|  |R|O|W|S|  |C|O|L|S|  |UT|  |DT|
     * |R|1|C|1|.|.|.|.|  |R|1|C|2|.|.|.|.| ... |R|1|C|n|.|.|.|.| 
     * |R|2|C|1|.|.|.|.|  |R|2|C|2|.|.|.|.| ... |R|2|C|n|.|.|.|.| 
     * ... 
     * |R|m|C|1|.|.|.|.|  |R|m|C|2|.|.|.|.| ... |R|m|C|n|.|.|.|.|
     * </pre>
     * 
     * In the language sending ore receiving a matrix, the rows are denoted by the outer index, and the columns by the inner
     * index: matrix[row][col].
     */
    public static final byte DOUBLE_64_UNIT_MATRIX = 30;

    /**
     * Number-preceded dense float array, stored internally in the SI unit, with a unique unit type and display type per row.
     * After the byte with value 31, the matrix types have a 32-bit int indicating the number of rows in the array that follows,
     * followed by a 32-bit int indicating the number of columns. These integers are not preceded by a byte indicating it is an
     * int. Then a one-byte unit type for column 1 follows and a one-byte (or two-byte in case of the MoneyPerUnit) display type
     * for column 1. Then the unit type and display type for column 2, etc. The internal storage of the values that are
     * transmitted after that always use the SI (or standard) unit, except for money where the display unit is used. Summarized,
     * the coding is as follows:
     * 
     * <pre>
     * |31|  |R|O|W|S|  |C|O|L|S|
     * |UT1|DT1|  |UT2|DT2| ... |UTn|DTn|
     * |R|1|C|1|  |R|1|C|2| ... |R|1|C|n| 
     * |R|2|C|1|  |R|2|C|2| ... |R|2|C|n| 
     * ... 
     * |R|m|C|1|  |R|m|C|2| ... |R|m|C|n|
     * </pre>
     * 
     * In the language sending or receiving a matrix, the rows are denoted by the outer index, and the columns by the inner
     * index: matrix[row][col]. This data type is ideal for, for instance, sending a time series of values, where column 1
     * indicates the time, and column 2 the value. Suppose that we have a time series of 4 values at t = {1, 2, 3, 4} hours and
     * dimensionless values v = {20.0, 40.0, 50.0, 60.0}, then the coding is as follows:
     * 
     * <pre>
     * |31|  |0|0|0|4|  |0|0|0|2|
     * |26|8|  |0|0|
     * |0x3F|0x80|0x00|0x00|  |0x41|0xA0|0x00|0x00|
     * |0x40|0x00|0x00|0x00|  |0x42|0x20|0x00|0x00|
     * |0x40|0x00|0x40|0x00|  |0x42|0x48|0x00|0x00|
     * |0x40|0x80|0x00|0x00|  |0x42|0x70|0x00|0x00|
     * </pre>
     */
    public static final byte FLOAT_32_UNIT2_ARRAY = 31;

    /**
     * Number-preceded dense double array, stored internally in the SI unit, with a unique unit type and display type per row.
     * After the byte with value 32, the matrix types have a 32-bit int indicating the number of rows in the array that follows,
     * followed by a 32-bit int indicating the number of columns. These integers are not preceded by a byte indicating it is an
     * int. Then a one-byte unit type for column 1 follows (see the table above) and a one-byte (or two-byte in case of the
     * MoneyPerUnit) display type for column 1 (see Appendix A). Then the unit type and display type for column 2, etc. The
     * internal storage of the values that are transmitted after that always use the SI (or standard) unit, except for money
     * where the display unit is used. Summarized, the coding is as follows:
     * 
     * <pre>
     * |32|  |R|O|W|S|  |C|O|L|S|
     * |UT1|DT1|  |UT2|DT2| ... |UTn|DTn|
     * |R|1|C|1|.|.|.|.|  |R|1|C|2|.|.|.|.| ... |R|1|C|n|.|.|.|.| 
     * |R|2|C|1|.|.|.|.|  |R|2|C|2|.|.|.|.| ... |R|2|C|n|.|.|.|.| 
     * ... 
     * |R|m|C|1|.|.|.|.|  |R|m|C|2|.|.|.|.| ... |R|m|C|n|.|.|.|.|
     * </pre>
     * 
     * In the language sending or receiving a matrix, the rows are denoted by the outer index, and the columns by the inner
     * index: matrix[row][col]. This data type is ideal for, for instance, sending a time series of values, where column 1
     * indicates the time, and column 2 the value. Suppose that we have a time series of 4 values at dimensionless years {2010,
     * 2011, 2012, 2013} and costs of dollars per acre of {415.7, 423.4, 428.0, 435.1}, then the coding is as follows:
     * 
     * <pre>
     * |32|  |0|0|0|4|  |0|0|0|2|
     * |0|0|  |101|150|18|
     * |0x40|0x9F|0x68|0x00|0x00|0x00|0x00|0x00|
     * |0x40|0x79|0xFB|0x33|0x33|0x33|0x33|0x33|
     * |0x40|0x9F|0x6C|0x00|0x00|0x00|0x00|0x00|
     * |0x40|0x7A|0x76|0x66|0x66|0x66|0x66|0x66|
     * |0x40|0x9F|0x70|0x00|0x00|0x00|0x00|0x00|
     * |0x40|0x7A|0xC0|0x00|0x00|0x00|0x00|0x00|
     * |0x40|0x9F|0x74|0x00|0x00|0x00|0x00|0x00|
     * |0x40|0x7A|0x91|0x99|0x99|0x99|0x99|0x9A|
     * </pre>
     */
    public static final byte DOUBLE_64_UNIT2_ARRAY = 32;

    /**
     * Utility class, cannot be instantiated.
     */
    public Sim0MQTypes()
    {
        // Utility class
    }

}
