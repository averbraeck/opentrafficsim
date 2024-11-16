package org.opentrafficsim.xml.bindings;

import java.lang.reflect.Field;
import java.util.Map;

import org.djutils.logger.CategoryLogger;
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
public class StripeTypeAdapter extends ExpressionAdapter<org.opentrafficsim.road.network.lane.Stripe.StripeType, StripeType>
{

    /** Dictionary. */
    private static final Map<String, String> DICTIONARY =
            Map.of("|", "SOLID", ":", "DASHED", "||", "DOUBLE_SOLID", "::", "DOUBLE_DASHED", "|:", "LEFT", ":|", "RIGHT");

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
            Field f = org.opentrafficsim.road.network.lane.Stripe.StripeType.class.getField(clean);
            org.opentrafficsim.road.network.lane.Stripe.StripeType st =
                    (org.opentrafficsim.road.network.lane.Stripe.StripeType) f.get(null);
            return new StripeType(st);
        }
        catch (Exception exception)
        {
            CategoryLogger.always().error(exception, "Problem parsing StripeType '" + field + "'");
            throw new IllegalArgumentException("Error parsing StripeType " + field, exception);
        }
    }

}
