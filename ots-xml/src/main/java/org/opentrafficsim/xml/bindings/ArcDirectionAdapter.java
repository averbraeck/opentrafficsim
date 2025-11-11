package org.opentrafficsim.xml.bindings;

import org.opentrafficsim.base.logger.Logger;
import org.opentrafficsim.xml.bindings.types.ArcDirectionType;
import org.opentrafficsim.xml.bindings.types.ArcDirectionType.ArcDirection;

/**
 * ArcDirectionAdapter to convert between XML representations of an arc direction, coded as L | LEFT | R | RIGHT | CLOCKWISE |
 * COUNTERCLOCKWISE, and an enum type.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class ArcDirectionAdapter extends ExpressionAdapter<ArcDirection, ArcDirectionType>
{

    /**
     * Constructor.
     */
    public ArcDirectionAdapter()
    {
        //
    }

    @Override
    public ArcDirectionType unmarshal(final String field) throws IllegalArgumentException
    {
        if (isExpression(field))
        {
            return new ArcDirectionType(trimBrackets(field));
        }
        try
        {
            String clean = field.replaceAll("\\s", "");
            if (clean.equals("L") || clean.equals("LEFT") || clean.equals("COUNTERCLOCKWISE"))
            {
                return new ArcDirectionType(ArcDirection.LEFT);
            }
            if (clean.equals("R") || clean.equals("RIGHT") || clean.equals("CLOCKWISE"))
            {
                return new ArcDirectionType(ArcDirection.RIGHT);
            }
        }
        catch (Exception exception)
        {
            Logger.ots().error(exception, "Problem parsing ArcDirection (LeftRight) '" + field + "'");
            throw new IllegalArgumentException("Error parsing ArcDirection (LeftRight) " + field, exception);
        }
        Logger.ots().error("Problem parsing ArcDirection (LeftRight) '" + field + "'");
        throw new IllegalArgumentException("Error parsing ArcDirection (LeftRight) " + field);
    }

    @Override
    public String marshal(final ArcDirectionType value) throws IllegalArgumentException
    {
        return marshal(value, (v) -> v.name());
    }

}
