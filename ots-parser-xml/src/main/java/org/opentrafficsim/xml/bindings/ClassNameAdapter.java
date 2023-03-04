package org.opentrafficsim.xml.bindings;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.djutils.logger.CategoryLogger;

/**
 * ClassNameAdapter converts between the XML String for a class name and the Class object.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
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
