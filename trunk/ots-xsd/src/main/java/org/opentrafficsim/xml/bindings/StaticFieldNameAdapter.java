package org.opentrafficsim.xml.bindings;

import javax.management.modelmbean.XMLParseException;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.djutils.logger.CategoryLogger;
import org.djutils.reflection.ClassUtil;

/**
 * StaticFieldNameAdapter converts between the XML String for a class name and the Class object. <br>
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 5, 2019 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
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
