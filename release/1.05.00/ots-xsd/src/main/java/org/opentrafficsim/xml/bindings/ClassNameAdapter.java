package org.opentrafficsim.xml.bindings;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.djutils.logger.CategoryLogger;

/**
 * ClassNameAdapter converts between the XML String for a class name and the Class object. <br>
 * <br>
 * Copyright (c) 2003-2022 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class ClassNameAdapter extends XmlAdapter<String, Class<?>>
{
    /** {@inheritDoc} */
    @Override
    public Class<?> unmarshal(final String field) throws IllegalArgumentException
    {
        try
        {
            return Class.forName(field);
        }
        catch (Exception exception)
        {
            CategoryLogger.always().error(exception, "Problem parsing classname '" + field + "'");
            throw new IllegalArgumentException("Error parsing classname " + field, exception);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String marshal(final Class<?> clazz) throws IllegalArgumentException
    {
        return clazz.getName();
    }

}
