package org.opentrafficsim.xml.bindings;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.djutils.logger.CategoryLogger;
import org.opentrafficsim.xml.bindings.types.StripeType;

/**
 * StripeType to convert between XML representations of a stripe type and its enum type.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class StripeTypeAdapter extends XmlAdapter<String, StripeType>
{
    /** {@inheritDoc} */
    @Override
    public StripeType unmarshal(final String field) throws IllegalArgumentException
    {
        try
        {
            String clean = field.replaceAll("\\s", "");
            for (StripeType st : StripeType.values())
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
    public String marshal(final StripeType stripeType) throws IllegalArgumentException
    {
        return stripeType.name();
    }

}
