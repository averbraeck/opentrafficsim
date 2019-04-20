package org.opentrafficsim.xml.bindings;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.djunits.value.Scalar;
import org.djunits.value.vdouble.scalar.Angle;

/**
 * AngleAdapter converts between the XML String for an Angle and the DJUnits Angle. <br>
 * <br>
 * Copyright (c) 2003-2018 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class AngleAdapter extends XmlAdapter<String, Angle>
{
    /** {@inheritDoc} */
    @Override
    public Angle unmarshal(final String field) throws IllegalArgumentException
    {
        return Angle.valueOf(field);
    }

    /** {@inheritDoc} */
    @Override
    public String marshal(final Angle angle) throws IllegalArgumentException
    {
        return Scalar.textualStringOfDefaultLocale(angle);
    }

}
