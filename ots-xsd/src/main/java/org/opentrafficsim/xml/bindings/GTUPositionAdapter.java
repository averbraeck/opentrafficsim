package org.opentrafficsim.xml.bindings;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.djutils.logger.CategoryLogger;
import org.opentrafficsim.xml.bindings.types.GTUPositionType;

/**
 * GTUPositionAdapter to convert between XML representations of a reference point on a GTU, coded as FRONT, REAR and REFERENCE,
 * and an enum type. <br>
 * <br>
 * Copyright (c) 2003-2018 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class GTUPositionAdapter extends XmlAdapter<String, GTUPositionType>
{
    /** {@inheritDoc} */
    @Override
    public GTUPositionType unmarshal(final String field) throws IllegalArgumentException
    {
        try
        {
            String clean = field.replaceAll("\\s", "");
            if (clean.equals("FRONT"))
            {
                return GTUPositionType.FRONT;
            }
            if (clean.equals("REAR"))
            {
                return GTUPositionType.REAR;
            }
            if (clean.equals("REFERENCE"))
            {
                return GTUPositionType.REFERENCE;
            }
        }
        catch (Exception exception)
        {
            CategoryLogger.always().error(exception, "Problem parsing GTUPosition '" + field + "'");
            throw new IllegalArgumentException("Error parsing GTUPosition " + field, exception);
        }
        CategoryLogger.always().error("Problem parsing GTUPosition '" + field + "'");
        throw new IllegalArgumentException("Error parsing GTUPositionType " + field);
    }

    /** {@inheritDoc} */
    @Override
    public String marshal(final GTUPositionType gtuPosition) throws IllegalArgumentException
    {
        if (gtuPosition.equals(GTUPositionType.FRONT))
            return "FRONT";
        if (gtuPosition.equals(GTUPositionType.REAR))
            return "REAR";
        return "REFERENC";
    }

}
