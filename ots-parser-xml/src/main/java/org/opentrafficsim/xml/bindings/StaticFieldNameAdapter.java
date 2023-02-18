package org.opentrafficsim.xml.bindings;

import javax.management.modelmbean.XMLParseException;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.djutils.logger.CategoryLogger;
import org.djutils.reflection.ClassUtil;

/**
 * StaticFieldNameAdapter converts between the XML String for a class name and the Class object. <br>
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class StaticFieldNameAdapter extends XmlAdapter<String, Object>
{

    /** {@inheritDoc} */
    @Override
    public Object unmarshal(final String field) throws Exception
    {
        try
        {
            int dot = field.lastIndexOf(".");
            String className = field.substring(0, dot);
            String fieldName = field.substring(dot + 1);
            return ClassUtil.resolveField(Class.forName(className), fieldName).get(null);
        }
        catch (Exception exception)
        {
            CategoryLogger.always().error(exception, "Problem parsing Static Field '" + field + "'");
            throw exception;
        }
    }

    /** {@inheritDoc} */
    @Override
    public String marshal(final Object v) throws Exception
    {
        throw new XMLParseException("Unable to marshal an object to it's original static field location.");
    }

}
