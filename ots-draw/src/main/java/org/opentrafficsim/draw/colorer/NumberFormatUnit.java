package org.opentrafficsim.draw.colorer;

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;

import org.djutils.exceptions.Throw;

/**
 * Number format to format number labels based on a unit.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class NumberFormatUnit extends NumberFormat
{

    /** */
    private static final long serialVersionUID = 20250902L;

    /** Unit. */
    private final String unit;

    /** Format string for double values. */
    private final String doubleFormat;

    /** Format string for long values. */
    private final String longFormat;

    /**
     * Constructor.
     * @param unit unit
     * @param decimalPlaces number of decimal places
     * @throws IllegalArgumentException when decimalPlaces &lt; 0
     */
    public NumberFormatUnit(final String unit, final int decimalPlaces)
    {
        Throw.whenNull(unit, "unit");
        Throw.when(decimalPlaces < 0, IllegalArgumentException.class, "Number of decimal places should be at least 0.");
        this.unit = unit;
        this.longFormat = "%d" + unit;
        this.doubleFormat = "%." + decimalPlaces + "f" + unit;
    }

    @Override
    public StringBuffer format(final double number, final StringBuffer toAppendTo, final FieldPosition pos)
    {
        return toAppendTo.append(String.format(this.doubleFormat, number));
    }

    @Override
    public StringBuffer format(final long number, final StringBuffer toAppendTo, final FieldPosition pos)
    {
        return toAppendTo.append(String.format(this.longFormat, number));
    }

    @Override
    public Number parse(final String source, final ParsePosition parsePosition)
    {
        return Double.valueOf(source.substring(0, source.length() - this.unit.length()));
    }

    /**
     * Returns the double format string.
     * @return double format string
     */
    public String getDoubleFormat()
    {
        return this.doubleFormat;
    }

}
