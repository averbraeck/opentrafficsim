package org.opentrafficsim.xml.bindings;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.djunits.value.Scalar;
import org.djunits.value.vdouble.scalar.Length;

/**
 * LengthAdapter converts between the XML String for a Length and the DJUnits Length. The length should be positive. <br>
 * <br>
 * Copyright (c) 2003-2018 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class LengthAdapter extends XmlAdapter<String, Length>
{
    /** {@inheritDoc} */
    @Override
    public Length unmarshal(final String field) throws IllegalArgumentException
    {
        if (field.trim().startsWith("-"))
        {
            throw new IllegalArgumentException("Length cannot be negative: " + field);
        }
        return Length.valueOf(field);
    }

    /** {@inheritDoc} */
    @Override
    public String marshal(final Length length) throws IllegalArgumentException
    {
        if (length.lt(Length.ZERO))
        {
            throw new IllegalArgumentException("Length cannot be negative: " + length);
        }
        return Scalar.textualStringOfDefaultLocale(length);
    }

}
