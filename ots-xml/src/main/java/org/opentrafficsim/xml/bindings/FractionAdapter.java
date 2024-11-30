package org.opentrafficsim.xml.bindings;

import org.djutils.exceptions.Throw;
import org.djutils.logger.CategoryLogger;
import org.opentrafficsim.xml.bindings.types.DoubleType;

/**
 * FractionAdapter to convert fractions as a number between 0.0 and 1.0, or as a percentage between 0% and 100%.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class FractionAdapter extends ExpressionAdapter<Double, DoubleType>
{

    @Override
    public DoubleType unmarshal(final String field) throws IllegalArgumentException
    {
        if (isExpression(field))
        {
            return new DoubleType(trimBrackets(field));
        }
        try
        {
            String clean = field.replaceAll("\\s", "");

            if (clean.endsWith("%"))
            {
                double d = 0.01 * Double.parseDouble(clean.substring(0, clean.length() - 1).trim());
                Throw.when(d < 0.0 || d > 1.0, IllegalArgumentException.class,
                        "fraction must be between 0.0 and 1.0 (inclusive)");
                return new DoubleType(d);
            }

            if (clean.matches("([0]?\\.?\\d+)|[1](\\.0*)"))
            {
                return new DoubleType(Double.parseDouble(clean));
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

    @Override
    public String marshal(final DoubleType fraction) throws IllegalArgumentException
    {
        Throw.when(!fraction.isExpression() && fraction.getValue() < 0.0 || fraction.getValue() > 1.0,
                IllegalArgumentException.class, "fraction must be between 0.0 and 1.0 (inclusive)");
        return super.marshal(fraction);
    }

}
