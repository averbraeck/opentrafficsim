package org.opentrafficsim.xml.bindings.types;

import org.djunits.value.vdouble.scalar.Length;

/**
 * LengthBeginEnd contains the information from the LengthBeginEndType. Examples of type instances are<br>
 * - BEGIN <br>
 * - END <br>
 * - END - 10m <br>
 * - 25 ft <br>
 * - 0.8 <br>
 * - 80% <br>
 * <br>
 * Copyright (c) 2003-2018 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class LengthBeginEnd
{
    /** begin or end? */
    private final boolean begin;

    /** absolute offset or relative fraction? */
    private final boolean absolute;

    /** the offset in case absolute == true. */
    private final Length offset;

    /** the fraction in case absolute == false. */
    private final double fraction;

    /**
     * @param begin boolean; begin or end?
     * @param offset the offset; absolute = true
     */
    public LengthBeginEnd(final boolean begin, final Length offset)
    {
        this.begin = begin;
        this.absolute = true;
        this.offset = offset;
        this.fraction = 0.0;
    }

    /**
     * @param fraction the fraction; absolute = false
     */
    public LengthBeginEnd(final double fraction)
    {
        this.begin = true;
        this.absolute = false;
        this.offset = Length.ZERO;
        this.fraction = fraction;
    }

    /**
     * @return begin
     */
    public final boolean isBegin()
    {
        return this.begin;
    }

    /**
     * @return absolute
     */
    public final boolean isAbsolute()
    {
        return this.absolute;
    }

    /**
     * @return offset
     */
    public final Length getOffset()
    {
        return this.offset;
    }

    /**
     * @return fraction
     */
    public final double getFraction()
    {
        return this.fraction;
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj)
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

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "LengthBeginEnd [begin=" + this.begin + ", absolute=" + this.absolute + ", offset=" + this.offset + ", fraction="
                + this.fraction + "]";
    }

}
