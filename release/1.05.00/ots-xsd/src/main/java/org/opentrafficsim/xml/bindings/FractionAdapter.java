package org.opentrafficsim.xml.bindings;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.djutils.exceptions.Throw;
import org.djutils.logger.CategoryLogger;

/**
 * FractionAdapter to convert fractions as a number between 0.0 and 1.0, or as a percentage between 0% and 100%. <br>
 * <br>
 * Copyright (c) 2003-2022 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class FractionAdapter extends XmlAdapter<String, Double>
{
    /** {@inheritDoc} */
    @Override
    public Double unmarshal(final String field) throws IllegalArgumentException
    {
        try
        {
            String clean = field.replaceAll("\\s", "");

            if (clean.endsWith("%"))
            {
                double d = 0.01 * Double.parseDouble(clean.substring(0, clean.length() - 1).trim());
                Throw.when(d < 0.0 || d > 1.0, IllegalArgumentException.class,
                        "fraction must be between 0.0 and 1.0 (inclusive)");
                return d;
            }

            if (clean.matches("([0]?\\.?\\d+)|[1](\\.0*)"))
            {
                return Double.parseDouble(clean);
            }
        }
        catch (Exception exception)
        {
            CategoryLogger.always().error(exception, "Problem parsing fraction '" + field + "'");
            throw new IllegalArgumentException("Error parsing fraction " + field, exception);
        }
        CategoryLogger.always().error("Problem parsing fraction '" + field + "'");
        throw new IllegalArgumentException("Error parsing fraction " + field);
    }

    /** {@inheritDoc} */
    @Override
    public String marshal(final Double fraction) throws IllegalArgumentException
    {
        Throw.when(fraction < 0.0 || fraction > 1.0, IllegalArgumentException.class,
                "fraction must be between 0.0 and 1.0 (inclusive)");
        return "" + fraction;
    }

}
