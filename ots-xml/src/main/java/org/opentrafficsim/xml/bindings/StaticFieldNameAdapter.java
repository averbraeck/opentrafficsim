package org.opentrafficsim.xml.bindings;

import java.lang.reflect.Field;

import org.opentrafficsim.xml.bindings.types.FieldType;

/**
 * StaticFieldNameAdapter converts between the XML String for a class name and the Class object. <br>
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Alexander Verbraeck
 * @author Peter Knoppers
 * @author Wouter Schakel
 */
public class StaticFieldNameAdapter extends ExpressionAdapter<Field, FieldType>
{

    /**
     * Constructor.
     */
    public StaticFieldNameAdapter()
    {
        //
    }

    @Override
    public FieldType unmarshal(final String field)
    {
        if (isExpression(field))
        {
            return new FieldType(trimBrackets(field));
        }
        return new FieldType(FieldType.valueOf(field));
    }

    @Override
    public String marshal(final FieldType value)
    {
        return marshalAsExpressionOrValue(value, (v) -> (v.getDeclaringClass().getName() + "." + v.getName()));
    }

}
