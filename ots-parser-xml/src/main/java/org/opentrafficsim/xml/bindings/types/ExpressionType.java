package org.opentrafficsim.xml.bindings.types;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Function;

import org.djutils.eval.Eval;
import org.djutils.exceptions.Throw;

/**
 * ExpressionType is the parent class for all types in XML that need to be parsed with the JAXB generated classes, and which may
 * be given in XML as an expression between { }. Adapters (extensions of {@code XmlAdapter}) have to deliver a subclass of this
 * class, where only the generics type and constructors are usually defined. This is required as JAXB bindings do not allow
 * generics types. This class takes care of returning a given value or the result of an evaluated expression for further XML
 * parsing in the {@code get(InputParameters)} method. To this aim, there are two constructors; one to simply provide a value,
 * and one to provide an expression.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <T> wrapped/returned value type
 */
@SuppressWarnings("serial")
public abstract class ExpressionType<T> implements Serializable
{

    /** Function to forward expression output as is. */
    private static final transient SerializableFunction<?, ?> AS_IS = (o) -> o;

    /** The value, when given. */
    private final T value;

    /** The expression, when given. */
    private final String expression;

    /** Function to convert output from expression to the right type. */
    private final SerializableFunction<Object, T> toType;

    /**
     * Constructor with value.
     * @param value value.
     */
    @SuppressWarnings("unchecked")
    public ExpressionType(final T value)
    {
        // value may be null
        this.value = value;
        this.expression = null;
        this.toType = (SerializableFunction<Object, T>) AS_IS;
    }

    /**
     * Constructor with value and type function.
     * @param value value.
     * @param toType function to convert output from expression to the right type.
     */
    public ExpressionType(final T value, final SerializableFunction<Object, T> toType)
    {
        // value may be null
        this.value = value;
        this.expression = null;
        this.toType = toType;
    }

    /**
     * Constructor with expression.
     * @param expression expression, without { }.
     */
    @SuppressWarnings("unchecked")
    public ExpressionType(final String expression)
    {
        Throw.whenNull(expression, "Expression may not be null. Consider using constructor with value.");
        Throw.when(expression.contains("{") || expression.contains("}"), IllegalArgumentException.class,
                "Expression should not have { }.");
        this.value = null;
        this.expression = expression;
        this.toType = (SerializableFunction<Object, T>) AS_IS;
    }

    /**
     * Constructor with expression and type function.
     * @param expression expression, without { }.
     * @param toType function to convert output from expression to the right type.
     */
    public ExpressionType(final String expression, final SerializableFunction<Object, T> toType)
    {
        Throw.whenNull(expression, "Expression may not be null. Consider using constructor with value.");
        Throw.when(expression.contains("{") || expression.contains("}"), IllegalArgumentException.class,
                "Expression should not have { }.");
        this.value = null;
        this.expression = expression;
        this.toType = toType;
    }

    /**
     * Constructor specifically for the subclass that has {@code T = String}, as this creates ambiguous constructors.
     * @param input input, either the value or an expression, may be {@code null} as value.
     * @param isExpression whether the input is an expression.
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
        this.toType = (o) -> (T) o.toString();
    }

    /**
     * Returns the value, either directly, or from an internal expression and using the input parameters.
     * @param eval expression evaluator.
     * @return value, either directly, or from an internal expression and using the input parameters
     */
    public T get(final Eval eval)
    {
        return this.expression == null ? this.value : this.toType.apply(eval.evaluate(this.expression));
    }

    /**
     * Returns whether this instance wraps an expression (or a value otherwise).
     * @return whether this instance wraps an expression (or a value otherwise)
     */
    public boolean isExpression()
    {
        return this.expression != null;
    }

    /**
     * Returns the expression.
     * @return expression.
     */
    public String getExpression()
    {
        Throw.when(this.expression == null, IllegalStateException.class,
                "Expression requested for expression type that wraps a value. Use !isExpression() to check.");
        return this.expression;
    }

    /**
     * Returns the expression enclosed in brackets { }. This is useful to marshal an expression value in an adapter.
     * @return expression enclosed in brackets { }.
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

    @Override
    public int hashCode()
    {
        return Objects.hash(this.expression, this.value);
    }

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

    /**
     * Serializable version of a {@code Function}.
     * @param <O> the type of the input to the function
     * @param <T> the type of the result of the function
     */
    public interface SerializableFunction<O, T> extends Function<O, T>, Serializable
    {
    }

}
