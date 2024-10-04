package org.opentrafficsim.xml.bindings;

import java.util.Map;

import org.djutils.logger.CategoryLogger;
import org.opentrafficsim.road.network.lane.Stripe.Type;
import org.opentrafficsim.xml.bindings.types.StripeType;

/**
 * StripeTypeAdapter to convert between XML representations of a stripe type and its enum type.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class StripeTypeAdapter extends ExpressionAdapter<Type, StripeType>
{

    /** Dictionary. */
    private final static Map<String, String> DICTIONARY =
            Map.of("|", "SOLID", ":", "DASHED", "||", "DOUBLE", "|:", "LEFT", ":|", "RIGHT");

    /** {@inheritDoc} */
    @Override
    public StripeType unmarshal(final String field) throws IllegalArgumentException
    {
        if (isExpression(field))
        {
            return new StripeType(trimBrackets(field));
        }
        try
        {
            String clean = field.replaceAll("\\s", "");
            clean = DICTIONARY.getOrDefault(clean, clean);
            return new StripeType(Type.valueOf(clean));
        }
        catch (Exception exception)
        {
            CategoryLogger.always().error(exception, "Problem parsing StripeType '" + field + "'");
            throw new IllegalArgumentException("Error parsing StripeType " + field, exception);
        }
    }

}
