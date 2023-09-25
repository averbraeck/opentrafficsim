package org.opentrafficsim.xml.bindings.types;

import java.util.Objects;

import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.parameters.InputParameters;

/**
 * ExpressionType is the parent class for all types in XML that need to be parsed with the JAXB generated classes, and which may
 * be given in XML as an expression between { }. Adapters (extensions of {@code XmlAdapter}) have to deliver a subclass of this
 * class, where only the generics type and constructors are usually defined. This is required as JAXB bindings do not allow
 * generics types. This class takes care of returning a given value or the result of an evaluated expression for further XML
 * parsing in the {@code get(InputParameters)} method. To this aim, there are two constructors; one to simply provide a value,
 * and one to provide an expression.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 * @param <T> wrapped/returned value type
 */
public abstract class ExpressionType<T>
{

    /** The value, when given. */
    private final T value;

    /** The expression, when given. */
    private final String expression;

    /**
     * Constructor with value.
     * @param value T; value.
     */
    public ExpressionType(final T value)
    {
        // value may be null
        this.value = value;
        this.expression = null;
    }

    /**
     * Constructor with expression.
     * @param expression String; expression, without { }.
     */
    public ExpressionType(final String expression)
    {
        Throw.whenNull(expression, "Expression may not be null. Consider using constructor with value.");
        Throw.when(expression.contains("{") || expression.contains("}"), IllegalArgumentException.class,
                "Expression should not have { }.");
        this.value = null;
        this.expression = expression;
    }

    /**
     * Constructor specifically for the subclass that has {@code T = String}, as this creates ambiguous constructors.
     * @param input String; input, either the value or an expression, may be {@code null} as value.
     * @param isExpression boolean; whether the input is an expression.
     */
    @SuppressWarnings("unchecked")
    ExpressionType(final String input, final boolean isExpression)
    {
        if (isExpression)
        {
            Throw.whenNull(input, "Expression may not be null.");
            Throw.when(input.contains("{") || input.contains("}"), IllegalArgumentException.class,
                    "Expression should not have { }.");
            this.value = null;
            this.expression = input;
        }
        else
        {
            this.value = (T) input;
            this.expression = null;
        }
    }

    /**
     * Returns the value, either directly, or from an internal expression and using the input parameters.
     * @param inputParameters InputParameters; input parameters.
     * @return value, either directly, or from an internal expression and using the input parameters
     */
    public T get(final InputParameters inputParameters)
    {
        // TODO: rather than "eval()", evaluate expression with DJUTILS evaluator using input parameters
        return this.expression == null ? this.value : (T) eval(inputParameters);
    }
    
    /**
     * Return value, or "1 - value".
     * @param inputParameters InputParameters; input parameters.
     * @return value of expression.
     */
    @Deprecated
    private Object eval(final InputParameters inputParameters)
    {
        if (this.expression.startsWith("1.0 - "))
        {
            return 1.0 - (double) inputParameters.getValue(this.expression.substring(6));
        }
        return inputParameters.getValue(this.expression);
    }

    /**
     * Returns whether this instance wraps an expression (or a value otherwise).
     * @return boolean; whether this instance wraps an expression (or a value otherwise)
     */
    public boolean isExpression()
    {
        return this.expression != null;
    }

    /**
     * Returns the expression.
     * @return String; expression.
     */
    public String getExpression()
    {
        Throw.when(this.expression == null, IllegalStateException.class,
                "Expression requested for expression type that wraps a value. Use !isExpression() to check.");
        return this.expression;
    }

    /**
     * Returns the expression enclosed in brackets { }. This is useful to marshal an expression value in an adapter.
     * @return String; expression enclosed in brackets { }.
     */
    public String getBracedExpression()
    {
        return "{" + getExpression() + "}";
    }

    /**
     * Returns the wrapped value.
     * @return wrapped value.
     */
    public T getValue()
    {
        Throw.when(this.expression != null, IllegalStateException.class,
                "Direct value requested for expression type that wraps an expression. Use isExpression() to check.");
        return this.value;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        return Objects.hash(this.expression, this.value);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        ExpressionType<?> other = (ExpressionType<?>) obj;
        return Objects.equals(this.expression, other.expression) && Objects.equals(this.value, other.value);
    }

}
