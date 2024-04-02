package org.opentrafficsim.xml.bindings;

import java.lang.reflect.Field;

import org.djutils.logger.CategoryLogger;
import org.djutils.reflection.ClassUtil;
import org.opentrafficsim.xml.bindings.types.FieldType;

/**
 * StaticFieldNameAdapter converts between the XML String for a class name and the Class object. <br>
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class StaticFieldNameAdapter extends ExpressionAdapter<Field, FieldType>
{

    /** {@inheritDoc} */
    @Override
    public FieldType unmarshal(final String field)
    {
        if (isExpression(field))
        {
            return new FieldType(trimBrackets(field));
        }
        try
        {
            int dot = field.lastIndexOf(".");
            String className = field.substring(0, dot);
            String fieldName = field.substring(dot + 1);
            return new FieldType(ClassUtil.resolveField(Class.forName(className), fieldName));
        }
        catch (Exception exception)
        {
            CategoryLogger.always().error(exception, "Problem parsing Static Field '" + field + "'");
            throw new RuntimeException(exception);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String marshal(final FieldType value)
    {
        return marshal(value, (v) -> (v.getDeclaringClass().getName() + "." + v.getName()));
    }

}
