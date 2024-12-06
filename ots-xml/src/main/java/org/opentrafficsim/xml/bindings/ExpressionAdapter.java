package org.opentrafficsim.xml.bindings;

import java.util.function.Function;

import org.djutils.exceptions.Throw;
import org.opentrafficsim.xml.bindings.types.ExpressionType;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Super class for adapters of expression types. This class performs default marshaling by using the expression, or
 * {@code toString()} on the value (or an empty {@code String} if its {@code null}). Sub-classes may overwrite the
 * {@code marshal(E value)} method, calling {@code marshal(E value, Function stringFunction)} to use a different form of
 * representing the value as a {@code String} and/or to implement value checks.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <T> value type wrapped in ExpressionType
 * @param <E> ExpressionType
 */
public abstract class ExpressionAdapter<T, E extends ExpressionType<T>> extends XmlAdapter<String, E>
{

    @Override
    public String marshal(final E value)
    {
        return marshal(value, (t) -> t == null ? "" : t.toString());
    }

    /**
     * Marshaling of {@code ExpressionType}. If the {@code ExpressionType} contains an expression, it is returned with brackets.
     * Otherwise a {@code String} representation of the contained value is returned using the supplied function.
     * @param value value.
     * @param stringFunction function to get a {@code String} representation of a contained value.
     * @return Marshaled {@code String} of an {@code ExpressionType}.
     */
    protected final String marshal(final E value, final Function<T, String> stringFunction)
    {
        if (value.isExpression())
        {
            return value.getBracedExpression();
        }
        return stringFunction.apply(value.getValue());
    }

    @Override
    public abstract E unmarshal(String v); // removes throws Exception

    /**
     * Checks whether field value is a bracketed expression.
     * @param field field value.
     * @return whether field value is a bracketed expression.
     * @throws IllegalArgumentException when the fields starts with { but does not end with }
     */
    protected static boolean isExpression(final String field) throws IllegalArgumentException
    {
        if (field != null && field.startsWith("{"))
        {
            Throw.when(!field.endsWith("}"), IllegalArgumentException.class,
                    "Field %s starts with { but does not end with }, i.e. it is not a valid expression.", field);
            return true;
        }
        return false;
    }

    /**
     * Trims the brackets from an expression.
     * @param field expression with brackets.
     * @return expression trimmed from brackets.
     * @throws IllegalArgumentException when the field is not a valid expression between brackets
     */
    protected static String trimBrackets(final String field) throws IllegalArgumentException
    {
        Throw.when(!isExpression(field), IllegalArgumentException.class,
                "Field %s is not a valid expression, cannot trim brackets.", field);
        return field.substring(1, field.length() - 1);
    }

}
