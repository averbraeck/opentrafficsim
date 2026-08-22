package org.opentrafficsim.xml.bindings.types;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.xml.bindings.types.LengthBeginEndType.LengthBeginEnd;

/**
 * Expression type with LengthBeginEnd value.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 */
public class LengthBeginEndType extends ExpressionType<LengthBeginEnd>
{

    /** Serialization version UID. */
    private static final long serialVersionUID = 20251111L;

    /** Function to convert output from expression to the right type. */
    private static final SerializableFunction<Object, LengthBeginEnd> TO_TYPE =
            SerializableFunction.of(LengthBeginEnd.class, LengthBeginEndType::valueOf);

    /**
     * Constructor with value.
     * @param value value, may be {@code null}.
     */
    public LengthBeginEndType(final LengthBeginEnd value)
    {
        super(value);
    }

    /**
     * Constructor with expression.
     * @param expression expression.
     */
    public LengthBeginEndType(final String expression)
    {
        super(expression, TO_TYPE);
    }

    /**
     * LengthBeginEnd contains the information from the LengthBeginEndType. Examples of type instances are:<br>
     * - BEGIN<br>
     * - END<br>
     * - END - 10m<br>
     * - 25 ft<br>
     * - 0.8<br>
     * - 80%
     */
    public static class LengthBeginEnd
    {
        /** Begin or end. */
        private final boolean begin;

        /** Absolute offset or relative fraction. */
        private final boolean absolute;

        /** The offset in case absolute == true. */
        private final Length offset;

        /** The fraction in case absolute == false. */
        private final double fraction;

        /**
         * Constructor with length.
         * @param begin begin or end?
         * @param offset the offset, absolute = true
         */
        public LengthBeginEnd(final boolean begin, final Length offset)
        {
            this.begin = begin;
            this.absolute = true;
            this.offset = offset;
            this.fraction = 0.0;
        }

        /**
         * Constructor with fraction.
         * @param fraction the fraction, absolute = false
         */
        public LengthBeginEnd(final double fraction)
        {
            this.begin = true;
            this.absolute = false;
            this.offset = Length.ZERO;
            this.fraction = fraction;
        }

        /**
         * Returns whether this is from the begin.
         * @return begin
         */
        public final boolean isBegin()
        {
            return this.begin;
        }

        /**
         * Returns whether the length is absolute.
         * @return absolute
         */
        public final boolean isAbsolute()
        {
            return this.absolute;
        }

        /**
         * Returns the offset.
         * @return offset
         */
        public final Length getOffset()
        {
            return this.offset;
        }

        /**
         * Returns the fraction.
         * @return fraction
         */
        public final double getFraction()
        {
            return this.fraction;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + (this.absolute ? 1231 : 1237);
            result = prime * result + (this.begin ? 1231 : 1237);
            long temp;
            temp = Double.doubleToLongBits(this.fraction);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            result = prime * result + ((this.offset == null) ? 0 : this.offset.hashCode());
            return result;
        }

        @Override
        @SuppressWarnings("needbraces")
        public boolean equals(final Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            LengthBeginEnd other = (LengthBeginEnd) obj;
            if (this.absolute != other.absolute)
                return false;
            if (this.begin != other.begin)
                return false;
            if (Double.doubleToLongBits(this.fraction) != Double.doubleToLongBits(other.fraction))
                return false;
            if (this.offset == null)
            {
                if (other.offset != null)
                    return false;
            }
            else if (!this.offset.equals(other.offset))
                return false;
            return true;
        }

        @Override
        public String toString()
        {
            return "LengthBeginEnd [begin=" + this.begin + ", absolute=" + this.absolute + ", offset=" + this.offset
                    + ", fraction=" + this.fraction + "]";
        }

    }

    /**
     * Parses {@code String} to to the right type.
     * @param str input string
     * @return parsed output
     */
    public static LengthBeginEnd valueOf(final String str)
    {
        String clean = str.replaceAll("\\s", "").trim();
        if (clean.equals("BEGIN"))
        {
            return new LengthBeginEnd(true, Length.ZERO);
        }
        if (clean.equals("END"))
        {
            return new LengthBeginEnd(false, Length.ZERO);
        }
        if (clean.endsWith("%"))
        {
            double d = 0.01 * Double.parseDouble(clean.substring(0, clean.length() - 1).trim());
            Throw.when(d < 0.0 || d > 1.0, IllegalArgumentException.class, "fraction must be between 0.0 and 1.0 (inclusive)");
            return new LengthBeginEnd(d);
        }
        if (clean.matches("([0]?\\.?\\d+)|[1](\\.0*)"))
        {
            double d = Double.parseDouble(clean);
            return new LengthBeginEnd(d);
        }
        boolean begin = true;
        if (clean.startsWith("END-"))
        {
            begin = false;
            clean = clean.substring(4);
        }
        Throw.when(clean.startsWith("-"), IllegalArgumentException.class, "Field %s contains negative value.", str);
        Length length = Length.valueOf(clean);
        return new LengthBeginEnd(begin, length);
    }
}
