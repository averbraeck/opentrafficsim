package org.opentrafficsim.xml.bindings;

import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.djutils.logger.CategoryLogger;
import org.opentrafficsim.road.network.lane.Stripe.Type;

/**
 * StripeType to convert between XML representations of a stripe type and its enum type.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class StripeTypeAdapter extends XmlAdapter<String, Type>
{
    /** Dictionary. */
    private final static Map<String, String> DICTIONARY =
            Map.of("|", "SOLID", ":", "DASHED", "||", "DOUBLE", "|:", "LEFT", ":|", "RIGHT");

    /** {@inheritDoc} */
    @Override
    public Type unmarshal(final String field) throws IllegalArgumentException
    {
        try
        {
            String clean = field.replaceAll("\\s", "");
            clean = DICTIONARY.getOrDefault(clean, clean);
            for (Type st : Type.values())
            {
                if (clean.equals(st.name()))
                {
                    return st;
                }
            }
        }
        catch (Exception exception)
        {
            CategoryLogger.always().error(exception, "Problem parsing StripeType '" + field + "'");
            throw new IllegalArgumentException("Error parsing StripeType " + field, exception);
        }
        CategoryLogger.always().error("Problem parsing StripeType '" + field + "'");
        throw new IllegalArgumentException("Error parsing StripeType " + field);
    }

    /** {@inheritDoc} */
    @Override
    public String marshal(final Type stripeType) throws IllegalArgumentException
    {
        return stripeType.name();
    }

}
