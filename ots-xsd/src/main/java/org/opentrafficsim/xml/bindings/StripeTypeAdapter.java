package org.opentrafficsim.xml.bindings;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.djutils.logger.CategoryLogger;
import org.opentrafficsim.xml.bindings.types.StripeType;

/**
 * StripeType to convert between XML representations of a stripe type and its enum type. <br>
 * <br>
 * Copyright (c) 2003-2018 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
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
