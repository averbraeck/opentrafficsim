package org.opentrafficsim.core.value;

/**
 * Format a floating point number in a reasonable way. <br>
 * I've experienced problems with the %g conversions that caused array bounds violations. Those versions of the JRE that do
 * <b>not</b> throw such Exceptions use one digit less than specified in the %g conversions. <br >
 * TODO: check how to always format numbers corresponding to the Locale used.
 * <p>
 * Copyright (c) 2002-2014 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved.
 * <p>
 * See for project information <a href="http://www.simulation.tudelft.nl/"> www.simulation.tudelft.nl</a>.
 * <p>
 * The OpenTrafficSim project is distributed under the following BSD-style license:<br>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
 * conditions are met:
 * <ul>
 * <li>Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.</li>
 * <li>Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.</li>
 * <li>Neither the name of Delft University of Technology, nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written permission.</li>
 * </ul>
 * This software is provided by the copyright holders and contributors "as is" and any express or implied warranties, including,
 * but not limited to, the implied warranties of merchantability and fitness for a particular purpose are disclaimed. In no
 * event shall the copyright holder or contributors be liable for any direct, indirect, incidental, special, exemplary, or
 * consequential damages (including, but not limited to, procurement of substitute goods or services; loss of use, data, or
 * profits; or business interruption) however caused and on any theory of liability, whether in contract, strict liability, or
 * tort (including negligence or otherwise) arising in any way out of the use of this software, even if advised of the
 * possibility of such damage.
 * @version Jul 1, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public final class Format
{
    /** Default total width of formatted value. */
    public static final int DEFAULTSIZE = 9;

    /** Default number of fraction digits. */
    public static final int DEFAULTPRECISION = 3;

    /**
     * This class should never be instantiated.
     */
    private Format()
    {
        // Prevent instantiation of this class
    }

    /**
     * Build a format string.
     * @param width Integer; number of characters in the result
     * @param precision Integer; number of fractional digits in the results
     * @param converter String; the format conversion specifier
     * @return String; suitable for formatting a float or double
     */
    private static String formatString(final int width, final int precision, final String converter)
    {
        return String.format("%%%d.%d%s", width, precision, converter);
    }

    /**
     * Format a floating point value.
     * @param value Float; the value to format
     * @param width Integer; number of characters in the result
     * @param precision Integer; number of fractional digits in the result
     * @return String; the formatted floating point value
     */
    public static String format(final float value, final int width, final int precision)
    {
        if (0 == value || Math.abs(value) > 0.01 && Math.abs(value) < 999.0)
        {
            return String.format(formatString(width, precision, "f"), value);
        }
        return String.format(formatString(width, precision, "e"), value);
    }

    /**
     * Format a floating point value.
     * @param value Float; the value to format
     * @param size Integer; number of characters in the result
     * @return String; the formatted floating point value
     */
    public static String format(final float value, final int size)
    {
        return Format.format(value, size, Format.DEFAULTPRECISION);
    }

    /**
     * Format a floating point value.
     * @param value Float; the value to format
     * @return String; the formatted floating point value
     */
    public static String format(final float value)
    {
        return format(value, Format.DEFAULTSIZE, Format.DEFAULTPRECISION);
    }

    /**
     * Format a floating point value.
     * @param value Double; the value to format
     * @param width Integer; number of characters in the result
     * @param precision Integer; number of fractional digits in the result
     * @return String; the formatted floating point value
     */
    public static String format(final double value, final int width, final int precision)
    {
        if (0 == value || Math.abs(value) > 0.01 && Math.abs(value) < 999.0)
        {
            return String.format(formatString(width, precision, "f"), value);
        }
        return String.format(formatString(width, precision, "e"), value);
    }

    /**
     * Format a floating point value.
     * @param value Double; the value to format
     * @param size Integer; number of characters in the result
     * @return String; the formatted floating point value
     */
    public static String format(final double value, final int size)
    {
        return Format.format(value, size, Format.DEFAULTPRECISION);
    }

    /**
     * Format a floating point value.
     * @param value Double; the value to format
     * @return String; the formatted floating point value
     */
    public static String format(final double value)
    {
        return format(value, Format.DEFAULTSIZE, Format.DEFAULTPRECISION);
    }

}
