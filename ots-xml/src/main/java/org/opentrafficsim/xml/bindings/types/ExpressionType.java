package org.opentrafficsim.xml.bindings.types;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.djunits.value.vdouble.scalar.SIScalar;
import org.djunits.value.vdouble.scalar.base.DoubleScalar;
import org.djutils.eval.Eval;
import org.djutils.exceptions.Throw;
import org.djutils.reflection.ClassUtil;

/**
 * ExpressionType is the parent class for all types in XML that need to be parsed with the JAXB generated classes, and which may
 * be given in XML as an expression between { }. Adapters (extensions of {@code XmlAdapter}) have to deliver a subclass of this
 * class, where only the generics type and constructors are usually defined. This is required as JAXB bindings do not allow
 * generics types. This class takes care of returning a given value or the result of an evaluated expression for further XML
 * parsing in the {@code get(InputParameters)} method. To this aim, there are two constructors; one to simply provide a value,
 * and one to provide an expression.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 * @param <T> wrapped/returned value type
 */
public abstract class ExpressionType<T> implements Serializable
{

    // This class is Serializable so it can easily be cloned

    /** Serialization version UID. */
    private static final long serialVersionUID = 20251111L;

    /** Pattern to filter exception message bit that Throw.when() prepends the message with. */
    private static final Pattern THROW_PREFIX_PATTERN = Pattern.compile("^[\\w.$]+\\s*\\(\\d+\\):\\s*");

    /** The value, when given. */
    private final T value;

    /** The expression, when given. */
    private final String expression;

    /** Function to convert output from expression to the right type. */
    private final SerializableFunction<Object, T> toType;

    /** Check on result from expression. */
    private Function<T, Boolean> expressionCheck;

    /**
     * Constructor with value.
     * @param value value.
     */
    public ExpressionType(final T value)
    {
        // value may be null
        this.value = value;
        this.expression = null;
        this.toType = null;
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
            this.toType = (o) -> (T) o.toString();
        }
        else
        {
            this.value = (T) input;
            this.expression = null;
            this.toType = null;
        }
    }

    /**
     * Sets the check on result from expression.
     * @param check check on result from expression
     */
    public void setExpressionCheck(final Function<T, Boolean> check)
    {
        this.expressionCheck = check;
    }

    /**
     * Returns the value, either directly, or from an internal expression and using the input parameters.
     * @param eval expression evaluator.
     * @return value, either directly, or from an internal expression and using the input parameters
     */
    public T get(final Eval eval)
    {
        if (this.expression == null)
        {
            return this.value;
        }
        Object object;
        try
        {
            object = eval.evaluate(this.expression);
        }
        catch (RuntimeException ex)
        {
            throw new IllegalArgumentException("Illegal argument for expression", ex);
        }
        T val;
        try
        {
            val = this.toType.apply(object);
        }
        catch (RuntimeException ex)
        {
            String message = ex.getMessage();
            if (message != null)
            {
                message = THROW_PREFIX_PATTERN.matcher(message).replaceFirst("");
            }
            throw new IllegalArgumentException("Illegal argument for expression: " + message, ex);
        }
        if (this.expressionCheck != null && !this.expressionCheck.apply(val))
        {
            // Throw.when adds line information we do not want to present to a use (exceptions from here may be presented)
            throw new IllegalArgumentException("Expression value " + val + ": not a valid value.");
        }
        return val;
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
     * Returns the static field value at the given class. This assumes that the class {@code T} has a static public field by
     * given name of type {@code T}.
     * @param <T> type of value
     * @param valueType value type
     * @param field field name
     * @return static field value at the given class
     */
    @SuppressWarnings("unchecked")
    public static <T> T fromStaticField(final Class<T> valueType, final String field)
    {
        try
        {
            return (T) ClassUtil.resolveField(valueType, field).get(null);
        }
        catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException ex)
        {
            throw new IllegalArgumentException("Unable to parse static field " + field + " from " + valueType);
        }
    }

    /**
     * Serializable version of a {@code Function}.
     * @param <O> the type of the input to the function
     * @param <T> the type of the result of the function
     */
    @FunctionalInterface
    public interface SerializableFunction<O, T> extends Function<O, T>, Serializable
    {

        /**
         * Returns a serializable function suitable to return a general type. This will:
         * <ol>
         * <li>Return the input object if it is of type {@code T}</li>
         * <li>Return the result from the string function if the input is {@code String}</li>
         * <li>Return the result from the object function</li>
         * <li>Throw an {@code IllegalArgumentException} otherwise</li>
         * </ol>
         * @param <O> limiting input type
         * @param <T> output type
         * @param valueType class of type {@code T}
         * @param fromString function to produce {@code T} from a {@code String}
         * @return composite function of the input
         */
        static <O, T> SerializableFunction<O, T> of(final Class<T> valueType, final SerializableFunction<String, T> fromString)
        {
            Throw.whenNull(valueType, "valueType");
            Throw.whenNull(fromString, "fromString");
            return (o) ->
            {
                if (valueType.isInstance(o))
                {
                    return valueType.cast(o);
                }
                if (o instanceof String str)
                {
                    return fromString.apply(str);
                }
                throw new IllegalArgumentException("Object " + o + " is not a " + valueType.getSimpleName() + " or String.");
            };
        }

        /**
         * Returns a serializable function suitable to return a numeric type. This will:
         * <ol>
         * <li>Return the input object if it is of type {@code T}</li>
         * <li>Return the result from the string function if the input is {@code String}</li>
         * <li>Return the result from the number function if the input is {@code Number}</li>
         * <li>Throw an {@code IllegalArgumentException} otherwise</li>
         * </ol>
         * @param <O> limiting input type
         * @param <T> output type
         * @param valueType class of type {@code T}
         * @param fromString function to produce {@code T} from a {@code String}
         * @param fromNumber function to produce {@code T} from a {@code Number}'s double value
         * @return composite function of the input
         */
        static <O, T> SerializableFunction<O, T> ofNumeric(final Class<T> valueType,
                final SerializableFunction<String, T> fromString, final SerializableFunction<Double, T> fromNumber)
        {
            return ofNumeric(valueType, fromString, (o) -> o.doubleValue(), fromNumber);
        }

        /**
         * Returns a serializable function suitable to return a numeric type. This will:
         * <ol>
         * <li>Return the input object if it is of type {@code T}</li>
         * <li>Return the result from the string function if the input is {@code String}</li>
         * <li>Return the result from the number function if the input is {@code Number}</li>
         * <li>Throw an {@code IllegalArgumentException} otherwise</li>
         * </ol>
         * @param <O> limiting input type
         * @param <T> output type
         * @param <N> wrapped native type
         * @param valueType class of type {@code T}
         * @param fromString function to produce {@code T} from a {@code String}
         * @param toNative function to translate {@code Number} to the correct native type {@code N}
         * @param fromNative function to produce {@code T} from a {@code Number}'s native value of type {@code N}
         * @return composite function of the input
         */
        static <O, T, N extends Number> SerializableFunction<O, T> ofNumeric(final Class<T> valueType,
                final SerializableFunction<String, T> fromString, final SerializableFunction<Number, N> toNative,
                final SerializableFunction<N, T> fromNative)
        {
            Throw.whenNull(valueType, "valueType");
            Throw.whenNull(fromString, "fromString");
            Throw.whenNull(fromNative, "fromNumber");
            return (o) ->
            {
                if (valueType.isInstance(o))
                {
                    return valueType.cast(o);
                }
                if (o instanceof SIScalar siScalar && DoubleScalar.class.isAssignableFrom(valueType))
                {
                    try
                    {
                        Method method = valueType.getMethod("ofSI", double.class);
                        @SuppressWarnings("unchecked")
                        T t = (T) method.invoke(o, siScalar.si);
                        return t;
                    }
                    catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                            | InvocationTargetException exception)
                    {
                        // fall through
                    }
                }
                if (o instanceof String str)
                {
                    return fromString.apply(str);
                }
                if (o instanceof Number num)
                {
                    Throw.when(DoubleScalar.class.isAssignableFrom(valueType) && DoubleScalar.class.isInstance(o),
                            IllegalArgumentException.class, "Value %s is not an appropriate expression result for type %s.", o,
                            valueType.getSimpleName());
                    return fromNative.apply(toNative.apply(num));
                }
                throw new IllegalArgumentException(
                        "Value " + o + " is not a " + valueType.getSimpleName() + ", String or Number.");
            };
        }

        /**
         * Returns a serializable function suitable to return a static field (including enums) type. This will:
         * <ol>
         * <li>Return the input object if it is of type {@code T}</li>
         * <li>Return the static field named by the input if the input is {@code String}</li>
         * <li>Throw an {@code IllegalArgumentException} otherwise</li>
         * </ol>
         * @param <O> limiting input type
         * @param <T> output type
         * @param valueType class of type {@code T}
         * @return composite function of the input
         */
        static <O, T> SerializableFunction<O, T> ofStaticField(final Class<T> valueType)
        {
            Throw.whenNull(valueType, "valueType");
            return (o) ->
            {
                if (valueType.isInstance(o))
                {
                    return valueType.cast(o);
                }
                if (o instanceof String str)
                {
                    return ExpressionType.fromStaticField(valueType, str);
                }
                throw new IllegalArgumentException("Object " + o + " is not a " + valueType.getSimpleName() + " or String.");
            };
        }

    }

}
