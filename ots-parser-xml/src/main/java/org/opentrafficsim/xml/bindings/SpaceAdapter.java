package org.opentrafficsim.xml.bindings;

import org.djutils.exceptions.Throw;
import org.opentrafficsim.xml.bindings.types.StringType;

/**
 * Adapter for String expression type (space, i.e. 'default' or 'preserve').
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class SpaceAdapter extends ExpressionAdapter<String, StringType>
{

    /** {@inheritDoc} */
    @Override
    public StringType unmarshal(final String field)
    {
        if (isExpression(field))
        {
            return new StringType(trimBrackets(field), true);
        }
        Throw.when(!field.equals("default") && !field.equals("preserve"), IllegalArgumentException.class,
                "Space value %s is not 'default' or 'preserve'.", field);
        return new StringType(field, false);
    }

    /** {@inheritDoc} */
    @Override
    public String marshal(final StringType value)
    {
        Throw.when(!value.isExpression() && !value.getValue().equals("default") && !value.getValue().equals("preserve"),
                IllegalArgumentException.class, "Space value %s is not 'default' or 'preserve'.", value.getValue());
        return super.marshal(value);
    }

}
