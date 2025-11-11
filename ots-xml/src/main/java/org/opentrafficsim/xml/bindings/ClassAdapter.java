package org.opentrafficsim.xml.bindings;

import org.opentrafficsim.base.logger.Logger;
import org.opentrafficsim.xml.bindings.types.ClassType;

/**
 * ClassAdapter converts between the XML String for a class name and the Class object.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
@SuppressWarnings("rawtypes")
public class ClassAdapter extends ExpressionAdapter<Class, ClassType>
{

    /**
     * Constructor.
     */
    public ClassAdapter()
    {
        //
    }

    @Override
    public ClassType unmarshal(final String field) throws IllegalArgumentException
    {
        if (isExpression(field))
        {
            return new ClassType(trimBrackets(field));
        }
        try
        {
            return new ClassType(Class.forName(field));
        }
        catch (Exception exception)
        {
            Logger.ots().error(exception, "Problem parsing classname '" + field + "'");
            throw new IllegalArgumentException("Error parsing classname " + field, exception);
        }
    }

    @Override
    public String marshal(final ClassType clazz) throws IllegalArgumentException
    {
        return marshal(clazz, (c) -> c.getName());
    }

}
