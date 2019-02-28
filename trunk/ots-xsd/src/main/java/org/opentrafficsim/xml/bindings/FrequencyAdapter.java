package org.opentrafficsim.xml.bindings;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.djunits.value.Scalar;
import org.djunits.value.vdouble.scalar.Frequency;

/**
 * FrequencyAdapter converts between the XML String for a Frequency and the DJUnits Frequency. <br>
 * <br>
 * Copyright (c) 2003-2018 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class FrequencyAdapter extends XmlAdapter<String, Frequency>
{
    /** {@inheritDoc} */
    @Override
    public Frequency unmarshal(final String field) throws IllegalArgumentException
    {
        return Frequency.valueOf(field);
    }

    /** {@inheritDoc} */
    @Override
    public String marshal(final Frequency frequency) throws IllegalArgumentException
    {
        return Scalar.stringOf(frequency);
    }

}
